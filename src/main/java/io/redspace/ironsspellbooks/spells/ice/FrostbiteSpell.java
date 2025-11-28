package io.redspace.ironsspellbooks.spells.ice;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleProjectile;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
public class FrostbiteSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "frostbite");
    private float percentDamageScale = .01f;
    private int icicleShardCount = 8;
    private float shardDamageSplit = 1f;
    private float castRange = 48f;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.percent_damage", Utils.stringTruncation(getPercentDamage(spellLevel, caster) * 100, 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(0)
            .build();

    public FrostbiteSpell() {
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 75;
        this.spellPowerPerLevel = 15;
        this.castTime = 40;
        this.baseManaCost = 100;
        this.percentDamageScale = .01f;
        this.icicleShardCount = 8;
        this.shardDamageSplit = 1f;
        this.castRange = 48f;
        
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
        return CastType.LONG;
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
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, (int) castRange, .15f);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData != null && playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetingData) {
            LivingEntity target = targetingData.getTarget((ServerLevel) level);
            if (target != null) {
                float i = playerMagicData.getCastCompletionPercent();
                float f = entity.tickCount;
                float distance = entity.distanceTo(target);
                float density = 2;
                Vec3 start = target.position().add(0, entity.getBbHeight() * .5f, 0);
                Vec3 delta = entity.position().subtract(target.position()).normalize();
                start = start.add(delta.scale(i * distance));
                MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE,
                        start.x + beamNoise((f + i + 50) * 1.25f) * .25f,
                        start.y + beamNoise((f + i) * 1.5f) * .25f,
                        start.z + beamNoise((f + i + 84) * 1.25f) * .25f,
                        1, 0, 0, 0, 0, false);
            }
        }
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
    }

    private float beamNoise(float f) {
        f = f % 360;
        return (Mth.cos(f * .25f) * 2f + Mth.sin(f) + Mth.cos(2 * f) * .25f) * .4f;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetingData) {
            LivingEntity target = targetingData.getTarget((ServerLevel) level);
            if (target != null) {
                float damage = getPercentDamage(spellLevel, entity) * target.getTicksFrozen() / 20f;
                DamageSources.applyDamage(target, damage, getDamageSource(entity));
                target.setTicksFrozen(0);
                MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, target.getX(), target.getY() + target.getBbHeight() * .5f, target.getZ(), 35, target.getBbWidth() * .5f, target.getBbHeight() * .5f, target.getBbWidth() * .5f, .03, false);
                if (target.isDeadOrDying()) {
                    spawnIcicleShards(target.getBoundingBox().getCenter(), damage, entity);
                    //TODO: this may be dangerous as hell
                    target.discard();
                }
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnIcicleShards(Vec3 origin, float damage, LivingEntity owner) {
        int count = icicleShardCount;
        int offset = 360 / count;
        for (int i = 0; i < count; i++) {

            Vec3 motion = new Vec3(0, 0, 0.55);
            motion = motion.xRot(30 * Mth.DEG_TO_RAD);
            motion = motion.yRot(offset * i * Mth.DEG_TO_RAD);

            IcicleProjectile shard = new IcicleProjectile(owner.level, owner);
            shard.setDamage(damage * shardDamageSplit / count);
            shard.setDeltaMovement(motion);

            Vec3 spawn = origin.add(motion.multiply(1, 0, 1).normalize().scale(.5f));
            var angle = Utils.rotationFromDirection(motion);

            shard.moveTo(spawn.x, spawn.y - shard.getBoundingBox().getYsize() / 2, spawn.z, angle.y, angle.x);
            owner.level.addFreshEntity(shard);
        }
    }

    public float getPercentDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * percentDamageScale;
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
                .optional("percentDamageScale", ParameterType.FLOAT, this.percentDamageScale, "冻结伤害百分比系数")
                .optional("icicleShardCount", ParameterType.INT, this.icicleShardCount, "碎冰弹数量")
                .optional("shardDamageSplit", ParameterType.FLOAT, this.shardDamageSplit, "碎冰伤害分摊比例")
                .optional("castRange", ParameterType.FLOAT, this.castRange, "施法距离")
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
        ExtraParams previous = new ExtraParams(this.percentDamageScale, this.icicleShardCount, this.shardDamageSplit, this.castRange);
        this.percentDamageScale = parameters.getFloat("percentDamageScale", this.percentDamageScale);
        this.icicleShardCount = parameters.getInt("icicleShardCount", this.icicleShardCount);
        this.shardDamageSplit = parameters.getFloat("shardDamageSplit", this.shardDamageSplit);
        this.castRange = parameters.getFloat("castRange", this.castRange);
        return previous;
    }

    private void restoreExtraParameters(ExtraParams previous) {
        this.percentDamageScale = previous.percentDamageScale();
        this.icicleShardCount = previous.icicleShardCount();
        this.shardDamageSplit = previous.shardDamageSplit();
        this.castRange = previous.castRange();
    }

    private record ExtraParams(float percentDamageScale, int icicleShardCount, float shardDamageSplit, float castRange) {
    }
}
