package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.SummonedHorse;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;
import io.redspace.ironsspellbooks.api.spells.parameters.ParameterType;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;

import java.util.Optional;

@AutoSpellConfig
public class SummonHorseSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "summon_horse");
    private static final String PARAM_SUMMON_DURATION = "summonDurationTicks";
    private static final String PARAM_SPEED_MIN = "speedMin";
    private static final String PARAM_SPEED_MAX = "speedMax";
    private static final String PARAM_JUMP_MIN = "jumpMin";
    private static final String PARAM_JUMP_MAX = "jumpMax";
    private static final String PARAM_HEALTH_MIN = "healthMin";
    private static final String PARAM_HEALTH_MAX = "healthMax";

    private int summonDurationTicks = 20 * 60 * 10;
    private float speedMin = .2f;
    private float speedMax = .45f;
    private float jumpMin = .6f;
    private float jumpMax = 1f;
    private float healthMin = 10;
    private float healthMax = 40;

    public SummonHorseSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
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

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(20)
            .build();

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
        return Optional.of(SoundEvents.ILLUSIONER_PREPARE_MIRROR);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ILLUSIONER_MIRROR_MOVE);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        int summonTime = summonDurationTicks;
        Vec3 spawn = entity.position();
        Vec3 forward = entity.getForward().normalize().scale(1.5f);
        spawn.add(forward.x, 0.15f, forward.z);

        //Teleport pre-existing or create new horse
        var horses = world.getEntitiesOfClass(SummonedHorse.class, entity.getBoundingBox().inflate(100), (summonedHorse) -> summonedHorse.getSummoner() == entity && !summonedHorse.isDeadOrDying());
        SummonedHorse horse = horses.size() > 0 ? horses.get(0) : new SummonedHorse(world, entity);

        horse.setPos(spawn);
        horse.removeEffectNoUpdate(MobEffectRegistry.SUMMON_HORSE_TIMER.get());
        horse.forceAddEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_HORSE_TIMER.get(), summonTime, 0, false, false, false), null);
        setAttributes(horse, getSpellPower(spellLevel, entity));

        world.addFreshEntity(horse);
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_HORSE_TIMER.get(), summonTime, 0, false, false, true));

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private void setAttributes(AbstractHorse horse, float power) {
        int maxPower = baseSpellPower + (ServerConfigs.getSpellConfig(this).maxLevel() - 1) * spellPowerPerLevel;
        float quality = power / (float) maxPower;

        horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(quality, speedMin, speedMax));
        horse.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(Mth.lerp(quality, jumpMin, jumpMax));
        horse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Mth.lerp(quality, healthMin, healthMax));
        if (!horse.isDeadOrDying())
            horse.setHealth(horse.getMaxHealth());
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
                .optional(PARAM_SUMMON_DURATION, ParameterType.INT, summonDurationTicks, "召唤物持续时间 (tick)")
                .optional(PARAM_SPEED_MIN, ParameterType.DOUBLE, speedMin, "最小移动速度")
                .optional(PARAM_SPEED_MAX, ParameterType.DOUBLE, speedMax, "最大移动速度")
                .optional(PARAM_JUMP_MIN, ParameterType.DOUBLE, jumpMin, "最小跳跃力")
                .optional(PARAM_JUMP_MAX, ParameterType.DOUBLE, jumpMax, "最大跳跃力")
                .optional(PARAM_HEALTH_MIN, ParameterType.DOUBLE, healthMin, "最小生命值")
                .optional(PARAM_HEALTH_MAX, ParameterType.DOUBLE, healthMax, "最大生命值")
                .build();
    }

    @Override
    public void onCastWithParameters(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, SpellParameters parameters) {
        SpellParameterConfig fallback = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);
        SpellParameterConfig config = SpellParameterLoader.resolve(getSpellId(), parameters, fallback);
        SpellParameterConfig previous = this.applyParameterOverrides(config);
        try {
            this.summonDurationTicks = parameters.getInt(PARAM_SUMMON_DURATION, this.summonDurationTicks);
            this.speedMin = (float) parameters.getDouble(PARAM_SPEED_MIN, this.speedMin);
            this.speedMax = (float) parameters.getDouble(PARAM_SPEED_MAX, this.speedMax);
            this.jumpMin = (float) parameters.getDouble(PARAM_JUMP_MIN, this.jumpMin);
            this.jumpMax = (float) parameters.getDouble(PARAM_JUMP_MAX, this.jumpMax);
            this.healthMin = (float) parameters.getDouble(PARAM_HEALTH_MIN, this.healthMin);
            this.healthMax = (float) parameters.getDouble(PARAM_HEALTH_MAX, this.healthMax);
            this.onCast(level, spellLevel, entity, castSource, playerMagicData);
        } finally {
            this.summonDurationTicks = 20 * 60 * 10;
            this.speedMin = .2f;
            this.speedMax = .45f;
            this.jumpMin = .6f;
            this.jumpMax = 1f;
            this.healthMin = 10;
            this.healthMax = 40;
            this.restoreParameters(previous);
        }
    }
}
