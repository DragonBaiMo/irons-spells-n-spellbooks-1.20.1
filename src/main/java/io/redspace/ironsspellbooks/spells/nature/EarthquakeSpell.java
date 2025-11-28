package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.EarthquakeAoe;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
public class EarthquakeSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "earthquake");
    private int durationTicks = 20 * 12;
    private float radiusBase = 4f;
    private float radiusScale = 4f;
    private float damageScale = .25f;
    private int slownessOffset = 2;
    private float slownessScale = 1f;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.slowness_effect", getSlownessAmplifier(spellLevel, caster) + 1),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(16)
            .build();

    public EarthquakeSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 40;
        this.baseManaCost = 50;
        this.durationTicks = 20 * 12;
        this.radiusBase = 4f;
        this.radiusScale = 4f;
        this.damageScale = .25f;
        this.slownessOffset = 2;
        this.slownessScale = 1f;
        
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
        return Optional.of(SoundRegistry.EARTHQUAKE_LOOP.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.EARTHQUAKE_CAST.get());
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, 32, .15f, false);
        return true;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 spawn = null;
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            var target = castTargetingData.getTarget((ServerLevel) world);
            if (target != null)
                spawn = target.position();
        }
        if (spawn == null) {
            spawn = Utils.raycastForEntity(world, entity, 32, true, .15f).getLocation();
            spawn = Utils.moveToRelativeGroundLevel(world, spawn, 6);
        }

        int duration = durationTicks;
        float radius = getRadius(spellLevel, entity);

        EarthquakeAoe aoeEntity = new EarthquakeAoe(world);
        aoeEntity.moveTo(spawn);
        aoeEntity.setOwner(entity);
        aoeEntity.setCircular();
        aoeEntity.setRadius(radius);
        aoeEntity.setDuration(duration);
        aoeEntity.setDamage(getDamage(spellLevel, entity));
        aoeEntity.setSlownessAmplifier(getSlownessAmplifier(spellLevel, entity));
        world.addFreshEntity(aoeEntity);

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * damageScale;
    }

    private float getRadius(int spellLevel, LivingEntity caster) {
        return radiusBase + radiusScale * getEntityPowerMultiplier(caster);
    }

    private int getSlownessAmplifier(int spellLevel, LivingEntity caster) {
        return Math.max(0, (int) (getDamage(spellLevel, caster) * slownessScale) - slownessOffset);
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
                .optional("durationTicks", ParameterType.INT, this.durationTicks, "持续时间 (tick)")
                .optional("radiusBase", ParameterType.FLOAT, this.radiusBase, "基础半径")
                .optional("radiusScale", ParameterType.FLOAT, this.radiusScale, "半径系数")
                .optional("damageScale", ParameterType.FLOAT, this.damageScale, "伤害系数")
                .optional("slownessOffset", ParameterType.INT, this.slownessOffset, "减速等级扣减")
                .optional("slownessScale", ParameterType.FLOAT, this.slownessScale, "减速等级系数")
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
        ExtraParams previous = new ExtraParams(this.durationTicks, this.radiusBase, this.radiusScale, this.damageScale, this.slownessOffset, this.slownessScale);
        this.durationTicks = parameters.getInt("durationTicks", this.durationTicks);
        this.radiusBase = parameters.getFloat("radiusBase", this.radiusBase);
        this.radiusScale = parameters.getFloat("radiusScale", this.radiusScale);
        this.damageScale = parameters.getFloat("damageScale", this.damageScale);
        this.slownessOffset = parameters.getInt("slownessOffset", this.slownessOffset);
        this.slownessScale = parameters.getFloat("slownessScale", this.slownessScale);
        return previous;
    }

    private void restoreExtraParameters(ExtraParams previous) {
        this.durationTicks = previous.durationTicks();
        this.radiusBase = previous.radiusBase();
        this.radiusScale = previous.radiusScale();
        this.damageScale = previous.damageScale();
        this.slownessOffset = previous.slownessOffset();
        this.slownessScale = previous.slownessScale();
    }

    private record ExtraParams(int durationTicks, float radiusBase, float radiusScale, float damageScale, int slownessOffset, float slownessScale) {
    }
}
