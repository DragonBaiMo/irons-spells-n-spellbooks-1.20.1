package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.TargetedTargetAreaCastData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;
import io.redspace.ironsspellbooks.api.spells.parameters.ParameterType;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@AutoSpellConfig
public class SlowSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "slow");
    private static final String PARAM_TARGET_RANGE = "targetRange";
    private static final String PARAM_RADIUS = "radius";
    private static final String PARAM_MAX_TARGETS = "maxTargets";
    private static final String PARAM_DURATION_PER_POWER = "durationPerPower";
    private static final String PARAM_BASE_AMPLIFIER = "baseAmplifier";
    private static final String PARAM_AMPLIFIER_PER_LEVEL = "amplifierPerLevel";
    private float targetRange = 32;
    private float radius = 3f;
    private int maxTargets = 5;
    private int durationPerPower = 20;
    private int baseAmplifier = 0;
    private int amplifierPerLevel = 1;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.slowed", Utils.stringTruncation((1 + getAmplifier(spellLevel, caster)) * .1f * 100, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.max_victims", maxTargets)
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(4)
            .setCooldownSeconds(80)
            .build();

    public SlowSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 4;
        this.castTime = 30;
        this.baseManaCost = 50;
        
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
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (Utils.preCastTargetHelper(level, entity, playerMagicData, this, (int) targetRange, .35f)) {
            float radius = this.radius;
            var target = ((TargetEntityCastData) playerMagicData.getAdditionalCastData()).getTarget((ServerLevel) level);
            var area = TargetedAreaEntity.createTargetAreaEntity(level, target.position(), radius, MobEffects.MOVEMENT_SLOWDOWN.getColor(), target);
            playerMagicData.setAdditionalCastData(new TargetedTargetAreaCastData(target, area));
            return true;
        }
        return false;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetedTargetAreaCastData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) world);
            if (targetEntity != null) {
                float radius = 3;
                AtomicInteger targets = new AtomicInteger(0);
                targetEntity.level.getEntitiesOfClass(LivingEntity.class, targetEntity.getBoundingBox().inflate(radius)).forEach((victim) -> {
                    if (targets.get() < maxTargets && victim != entity && victim.distanceToSqr(targetEntity) < radius * radius && !DamageSources.isFriendlyFireBetween(entity, victim)) {
                        victim.addEffect(new MobEffectInstance(MobEffectRegistry.SLOWED.get(), getDuration(spellLevel, entity), getAmplifier(spellLevel, entity)));
                        targets.incrementAndGet();
                    }
                });
            }
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public int getAmplifier(int spellLevel, LivingEntity caster) {
        return baseAmplifier + amplifierPerLevel * (spellLevel - 1);
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * durationPerPower);
    }

    @Override
    public Vector3f getTargetingColor() {
        return Utils.deconstructRGB(MobEffects.MOVEMENT_SLOWDOWN.getColor());
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
                .optional(PARAM_TARGET_RANGE, ParameterType.DOUBLE, targetRange, "目标选取距离")
                .optional(PARAM_RADIUS, ParameterType.DOUBLE, radius, "减速范围半径")
                .optional(PARAM_MAX_TARGETS, ParameterType.INT, maxTargets, "最多命中目标数")
                .optional(PARAM_DURATION_PER_POWER, ParameterType.INT, durationPerPower, "每点威力对应的持续时间 (tick)")
                .optional(PARAM_BASE_AMPLIFIER, ParameterType.INT, baseAmplifier, "基础减速等级")
                .optional(PARAM_AMPLIFIER_PER_LEVEL, ParameterType.INT, amplifierPerLevel, "每级增加的减速等级")
                .build();
    }

    @Override
    public void onCastWithParameters(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, SpellParameters parameters) {
        SpellParameterConfig fallback = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);
        SpellParameterConfig config = SpellParameterLoader.resolve(getSpellId(), parameters, fallback);
        SpellParameterConfig previous = this.applyParameterOverrides(config);
        try {
            this.targetRange = (float) parameters.getDouble(PARAM_TARGET_RANGE, this.targetRange);
            this.radius = (float) parameters.getDouble(PARAM_RADIUS, this.radius);
            this.maxTargets = parameters.getInt(PARAM_MAX_TARGETS, this.maxTargets);
            this.durationPerPower = parameters.getInt(PARAM_DURATION_PER_POWER, this.durationPerPower);
            this.baseAmplifier = parameters.getInt(PARAM_BASE_AMPLIFIER, this.baseAmplifier);
            this.amplifierPerLevel = parameters.getInt(PARAM_AMPLIFIER_PER_LEVEL, this.amplifierPerLevel);
            this.onCast(level, spellLevel, entity, castSource, playerMagicData);
        } finally {
            this.targetRange = 32;
            this.radius = 3f;
            this.maxTargets = 5;
            this.durationPerPower = 20;
            this.baseAmplifier = 0;
            this.amplifierPerLevel = 1;
            this.restoreParameters(previous);
        }
    }
}
