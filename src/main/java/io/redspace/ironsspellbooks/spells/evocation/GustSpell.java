package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.AirborneEffect;
import io.redspace.ironsspellbooks.entity.spells.gust.GustCollider;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
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
public class GustSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "gust");
    private float range = 8f;
    private float strengthScale = .2f;
    private float kickbackScale = .25f;
    private float damageScale = 1f;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.strength", String.format("%s%%", (int) (getStrength(spellLevel, caster) * 100 / getStrength(1, null)))),
                Component.translatable("ui.irons_spellbooks.impact_damage", Utils.stringTruncation(AirborneEffect.getDamageFromLevel(spellLevel), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public GustSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 30;
        this.range = 8f;
        this.strengthScale = .2f;
        this.kickbackScale = .25f;
        this.damageScale = 1f;
        
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
        return Optional.of(SoundRegistry.GUST_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.GUST_CAST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float range = getRange(spellLevel, entity);
        float strength = getStrength(spellLevel, entity);

        GustCollider gust = new GustCollider(level, entity);
        gust.setPos(entity.position().add(0, entity.getEyeHeight() * .7, 0).add(entity.getForward().normalize().scale(2f)));
        gust.range = range;
        gust.strength = strength;
        gust.amplifier = spellLevel - 1;
        level.addFreshEntity(gust);
        gust.setDealDamageActive();
        gust.tick();

        float kickback = (float) entity.getBoundingBox().getCenter().distanceToSqr(Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, 3.5f).getLocation());
        kickback = Mth.clamp(1 / (kickback + 1) - .11f, 0f, .95f);
        if (kickback > 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().subtract(entity.getLookAngle().scale(kickback * spellLevel * kickbackScale)));
            entity.resetFallDistance();
            entity.hurtMarked = true;
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getRange(int spellLevel, LivingEntity caster) {
        return range;
    }

    public float getStrength(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * strengthScale;
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * damageScale;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_WAVY_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST_FINISH;
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        return target.distanceToSqr(mob) > getRange(spellLevel, mob) * getRange(spellLevel, mob) * 1.25;
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
                .optional("range", ParameterType.FLOAT, this.range, "施法范围")
                .optional("strengthScale", ParameterType.FLOAT, this.strengthScale, "击退强度系数")
                .optional("kickbackScale", ParameterType.FLOAT, this.kickbackScale, "自我后坐力系数")
                .optional("damageScale", ParameterType.FLOAT, this.damageScale, "落地伤害系数")
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
        ExtraParams previous = new ExtraParams(this.range, this.strengthScale, this.kickbackScale, this.damageScale);
        this.range = parameters.getFloat("range", this.range);
        this.strengthScale = parameters.getFloat("strengthScale", this.strengthScale);
        this.kickbackScale = parameters.getFloat("kickbackScale", this.kickbackScale);
        this.damageScale = parameters.getFloat("damageScale", this.damageScale);
        return previous;
    }

    private void restoreExtraParameters(ExtraParams previous) {
        this.range = previous.range();
        this.strengthScale = previous.strengthScale();
        this.kickbackScale = previous.kickbackScale();
        this.damageScale = previous.damageScale();
    }

    private record ExtraParams(float range, float strengthScale, float kickbackScale, float damageScale) {
    }
}
