package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.*;
import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;
import io.redspace.ironsspellbooks.api.spells.parameters.ParameterType;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;

import java.util.List;

@AutoSpellConfig
public class FlamingBarrageSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "flaming_barrage");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();
    private int recastCount = 5;
    private int recastIntervalTicks = 80;
    private float inaccuracyMin = .2f;
    private float inaccuracyMax = 1.4f;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", (int) (getRecastCount(spellLevel, caster)))
        );
    }

    public FlamingBarrageSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 80;
        this.recastCount = 5;
        this.recastIntervalTicks = 80;
        this.inaccuracyMin = .2f;
        this.inaccuracyMax = 1.4f;
        
        SpellParameterConfig defaults = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);
        if (SpellParameterLoader.hasConfig(getSpellId())) {
            SpellParameterConfig parameters = SpellParameterLoader.resolve(getSpellId(), SpellParameters.empty(), defaults);
            this.baseManaCost = parameters.baseManaCost();
            this.manaCostPerLevel = parameters.manaCostPerLevel();
            this.baseSpellPower = parameters.baseSpellPower();
            this.spellPowerPerLevel = parameters.spellPowerPerLevel();
            this.castTime = parameters.castTime();
            this.defaultConfig.cooldownInSeconds = parameters.cooldownSeconds();
        }

    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return recastCount;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 64, .15f);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetEntityCastData) {
            var recasts = playerMagicData.getPlayerRecasts();
            if (!recasts.hasRecastForSpell(getSpellId())) {
                recasts.addRecast(new RecastInstance(getSpellId(), spellLevel, getRecastCount(spellLevel, entity), recastIntervalTicks, castSource, new MultiTargetEntityCastData(targetEntityCastData.getTarget((ServerLevel) level))), playerMagicData);
            } else {
                var instance = recasts.getRecastInstance(this.getSpellId());
                if (instance != null && instance.getCastData() instanceof MultiTargetEntityCastData targetingData) {
                    targetingData.addTarget(targetEntityCastData.getTargetUUID());
                }
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        var level = serverPlayer.level;
        Vec3 origin = serverPlayer.getEyePosition().add(serverPlayer.getForward().normalize().scale(.2f));
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 2.0f, 1.0f);
        if (castDataSerializable instanceof MultiTargetEntityCastData targetingData) {
            targetingData.getTargets().forEach(uuid -> {
                var target = (LivingEntity) ((ServerLevel) serverPlayer.level).getEntity(uuid);
                if (target != null) {
                    SmallMagicFireball fireball = new SmallMagicFireball(level, serverPlayer);
                    fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
                    var vec = target.getBoundingBox().getCenter().subtract(serverPlayer.getEyePosition()).normalize();
                    var inaccuracy = (float) Mth.clampedLerp(inaccuracyMin, inaccuracyMax, target.position().distanceToSqr(serverPlayer.position()) / (32 * 32));
                    fireball.shoot(vec.scale(.75f), inaccuracy);
                    fireball.setDamage(getDamage(recastInstance.getSpellLevel(), serverPlayer));
                    fireball.setHomingTarget(target);
                    level.addFreshEntity(fireball);
                }
            });
        }
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new MultiTargetEntityCastData();
    }

    

    

    

    

    

    

    @Override
    public SpellParameterSchema getParameterSchema() {
        SpellParameterConfig defaults = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);
        SpellParameterConfig parameters = SpellParameterLoader.resolve(getSpellId(), SpellParameters.empty(), defaults);
        return SpellParameterSchema.builder()
                .optional("baseManaCost", ParameterType.INT, parameters.baseManaCost(), "基础魔力消耗")
                .alias("manaCost", "baseManaCost")
                .optional("manaCostPerLevel", ParameterType.INT, parameters.manaCostPerLevel(), "每级魔力增量")
                .optional("baseSpellPower", ParameterType.INT, parameters.baseSpellPower(), "基础技能威力")
                .alias("power", "baseSpellPower")
                .optional("spellPowerPerLevel", ParameterType.INT, parameters.spellPowerPerLevel(), "每级威力增量")
                .alias("levelScaling", "spellPowerPerLevel")
                .optional("castTime", ParameterType.INT, parameters.castTime(), "施法时间 (tick)")
                .optional("cooldown", ParameterType.DOUBLE, parameters.cooldownSeconds(), "默认冷却 (秒)")
                .optional("recastCount", ParameterType.INT, this.recastCount, "重铸次数")
                .optional("recastIntervalTicks", ParameterType.INT, this.recastIntervalTicks, "重铸间隔 (tick)")
                .optional("inaccuracyMin", ParameterType.FLOAT, this.inaccuracyMin, "最小散射")
                .optional("inaccuracyMax", ParameterType.FLOAT, this.inaccuracyMax, "最大散射")
                .build();
    }

    @Override
    public void onCastWithParameters(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, SpellParameters parameters) {
        SpellParameterConfig fallback = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);
        SpellParameterConfig config = SpellParameterLoader.resolve(getSpellId(), parameters, fallback);
        SpellParameterConfig previous = this.applyParameterOverrides(config);
        ExtraParams prevExtra = applyExtraParameters(parameters);
        try {
            this.onCast(level, spellLevel, entity, castSource, playerMagicData);
        } finally {
            restoreExtraParameters(prevExtra);
            this.restoreParameters(previous);
        }
    }

    private ExtraParams applyExtraParameters(SpellParameters parameters) {
        ExtraParams previous = new ExtraParams(this.recastCount, this.recastIntervalTicks, this.inaccuracyMin, this.inaccuracyMax);
        this.recastCount = parameters.getInt("recastCount", this.recastCount);
        this.recastIntervalTicks = parameters.getInt("recastIntervalTicks", this.recastIntervalTicks);
        this.inaccuracyMin = parameters.getFloat("inaccuracyMin", this.inaccuracyMin);
        this.inaccuracyMax = parameters.getFloat("inaccuracyMax", this.inaccuracyMax);
        return previous;
    }

    private void restoreExtraParameters(ExtraParams previous) {
        this.recastCount = previous.recastCount();
        this.recastIntervalTicks = previous.recastIntervalTicks();
        this.inaccuracyMin = previous.inaccuracyMin();
        this.inaccuracyMax = previous.inaccuracyMax();
    }

    private record ExtraParams(int recastCount, int recastIntervalTicks, float inaccuracyMin, float inaccuracyMax) {
    }
}
