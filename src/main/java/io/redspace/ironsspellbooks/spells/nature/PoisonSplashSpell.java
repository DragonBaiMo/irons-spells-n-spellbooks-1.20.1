package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.poison_cloud.PoisonSplash;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;
import io.redspace.ironsspellbooks.api.spells.parameters.ParameterType;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class PoisonSplashSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "poison_splash");
    private static final String PARAM_TARGET_RANGE = "targetRange";
    private static final String PARAM_DAMAGE_MULTIPLIER = "damageMultiplier";
    private static final String PARAM_DURATION_BASE = "durationBase";
    private static final String PARAM_DURATION_PER_LEVEL = "durationPerLevel";
    private static final String PARAM_CLOUD_RADIUS = "cloudRadius";
    private float targetRange = 32;
    private float damageMultiplier = 1f;
    private int durationBase = 100;
    private int durationPerLevel = 40;
    private float cloudRadius = 2f;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public PoisonSplashSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 40;
        
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
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.POISON_SPLASH_BEGIN.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.POISON_CAST.get());
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, (int) targetRange, .35f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 spawn = null;

        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            spawn = castTargetingData.getTargetPosition((ServerLevel) level);
        }
        if (spawn == null) {
            HitResult raycast = Utils.raycastForEntity(level, entity, targetRange, true);
            if (raycast.getType() == HitResult.Type.ENTITY) {
                spawn = ((EntityHitResult) raycast).getEntity().position();
            } else {
                spawn = Utils.moveToRelativeGroundLevel(level, raycast.getLocation().subtract(entity.getForward().normalize()).add(0, 2, 0), 5);
            }
        }

        PoisonSplash poisonSplash = new PoisonSplash(level);
        poisonSplash.setOwner(entity);
        poisonSplash.moveTo(spawn);
        poisonSplash.setDamage(getDamage(spellLevel, entity));
        poisonSplash.setEffectDuration(getDuration(spellLevel, entity));
        poisonSplash.setRadius(cloudRadius);
        level.addFreshEntity(poisonSplash);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return this.getSpellPower(spellLevel, entity) * damageMultiplier;
    }

    private int getDuration(int spellLevel, LivingEntity entity) {
        return durationBase + spellLevel * durationPerLevel;
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
                .optional(PARAM_TARGET_RANGE, ParameterType.DOUBLE, targetRange, "目标选择最大距离")
                .optional(PARAM_DAMAGE_MULTIPLIER, ParameterType.DOUBLE, damageMultiplier, "伤害倍率 (基于技能威力)")
                .optional(PARAM_DURATION_BASE, ParameterType.INT, durationBase, "基础持续时间 (tick)")
                .optional(PARAM_DURATION_PER_LEVEL, ParameterType.INT, durationPerLevel, "每级持续时间增量 (tick)")
                .optional(PARAM_CLOUD_RADIUS, ParameterType.DOUBLE, cloudRadius, "毒云半径")
                .build();
    }

    @Override
    public void onCastWithParameters(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, SpellParameters parameters) {
        SpellParameterConfig fallback = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);
        SpellParameterConfig config = SpellParameterLoader.resolve(getSpellId(), parameters, fallback);
        SpellParameterConfig previous = this.applyParameterOverrides(config);
        try {
            this.targetRange = (float) parameters.getDouble(PARAM_TARGET_RANGE, this.targetRange);
            this.damageMultiplier = (float) parameters.getDouble(PARAM_DAMAGE_MULTIPLIER, this.damageMultiplier);
            this.durationBase = parameters.getInt(PARAM_DURATION_BASE, this.durationBase);
            this.durationPerLevel = parameters.getInt(PARAM_DURATION_PER_LEVEL, this.durationPerLevel);
            this.cloudRadius = (float) parameters.getDouble(PARAM_CLOUD_RADIUS, this.cloudRadius);
            this.onCast(level, spellLevel, entity, castSource, playerMagicData);
        } finally {
            this.targetRange = 32;
            this.damageMultiplier = 1f;
            this.durationBase = 100;
            this.durationPerLevel = 40;
            this.cloudRadius = 2f;
            this.restoreParameters(previous);
        }
    }
}
