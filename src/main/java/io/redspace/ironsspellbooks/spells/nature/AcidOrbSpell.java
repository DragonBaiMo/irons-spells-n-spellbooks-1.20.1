package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrb;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;
import io.redspace.ironsspellbooks.api.spells.parameters.ParameterType;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class AcidOrbSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "acid_orb");
    private float radiusPerPower = 3f;
    private int rendAmplifierOffset = 2;
    private double rendDurationSecondsPerPower = 20d;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.rend", Utils.stringTruncation((getRendAmplifier(spellLevel, caster) + 1) * 5, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getRendDuration(spellLevel, caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public AcidOrbSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 15;
        this.baseManaCost = 30;
        
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
        return Optional.of(SoundRegistry.ACID_ORB_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.ACID_ORB_CAST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        AcidOrb orb = new AcidOrb(level, entity);
        orb.setPos(entity.position().add(0, entity.getEyeHeight() - orb.getBoundingBox().getYsize() * .5f, 0).add(entity.getForward()));
        orb.shoot(entity.getLookAngle());
        orb.setDeltaMovement(orb.getDeltaMovement().add(0, 0.2, 0));
        orb.setExplosionRadius(getRadius(spellLevel, entity));
        orb.setRendLevel(getRendAmplifier(spellLevel, entity));
        orb.setRendDuration(getRendDuration(spellLevel, entity));
        level.addFreshEntity(orb);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getRadius(int spellLevel, LivingEntity caster) {
        return (float) (getSpellPower(spellLevel, caster) * radiusPerPower);
    }

    public int getRendAmplifier(int spellLevel, LivingEntity caster) {
        return spellLevel + rendAmplifierOffset;
    }

    public int getRendDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * rendDurationSecondsPerPower * 20);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_SPIT_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SPIT_FINISH_ANIMATION;
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        return target.getAttributeValue(Attributes.ARMOR) < 4;
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
                .optional("radiusPerPower", ParameterType.DOUBLE, radiusPerPower, "每点威力对应的爆炸半径")
                .optional("rendAmplifierOffset", ParameterType.INT, rendAmplifierOffset, "破甲等级基础偏移值")
                .optional("rendDurationSecondsPerPower", ParameterType.DOUBLE, rendDurationSecondsPerPower, "每点威力附加的破甲持续秒数")
                .build();
    }

    @Override
    public void onCastWithParameters(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, SpellParameters parameters) {
        SpellParameterConfig fallback = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);
        SpellParameterConfig config = SpellParameterLoader.resolve(getSpellId(), parameters, fallback);
        SpellParameterConfig previous = this.applyParameterOverrides(config);
        float previousRadiusPerPower = this.radiusPerPower;
        int previousRendAmplifierOffset = this.rendAmplifierOffset;
        double previousRendDurationSecondsPerPower = this.rendDurationSecondsPerPower;
        this.radiusPerPower = (float) parameters.getDouble("radiusPerPower", this.radiusPerPower);
        this.rendAmplifierOffset = parameters.getInt("rendAmplifierOffset", this.rendAmplifierOffset);
        this.rendDurationSecondsPerPower = parameters.getDouble("rendDurationSecondsPerPower", this.rendDurationSecondsPerPower);
        try {
            this.onCast(level, spellLevel, entity, castSource, playerMagicData);
        } finally {
            this.radiusPerPower = previousRadiusPerPower;
            this.rendAmplifierOffset = previousRendAmplifierOffset;
            this.rendDurationSecondsPerPower = previousRendDurationSecondsPerPower;
            this.restoreParameters(previous);
        }
    }
}
