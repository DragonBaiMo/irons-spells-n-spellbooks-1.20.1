package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
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
public class EvasionSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "evasion");
    private int durationSeconds = 60;
    private float baseHits = 1f;
    private float hitsPerPower = 1f;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.hits_dodged", getHitCount(spellLevel, caster)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(180)
            .build();

    public EvasionSpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 40;
        this.durationSeconds = 60;
        this.baseHits = 1f;
        this.hitsPerPower = 1f;
        
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
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.EVASION.get(), durationSeconds * 20, getHitCount(spellLevel, entity) - 1, false, false, true));
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
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
                .optional("durationSeconds", ParameterType.INT, this.durationSeconds, "持续时间 (秒)")
                .optional("baseHits", ParameterType.FLOAT, this.baseHits, "基础可闪避次数")
                .optional("hitsPerPower", ParameterType.FLOAT, this.hitsPerPower, "每点威力增加的闪避次数")
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
        ExtraParams previous = new ExtraParams(this.durationSeconds, this.baseHits, this.hitsPerPower);
        this.durationSeconds = parameters.getInt("durationSeconds", this.durationSeconds);
        this.baseHits = parameters.getFloat("baseHits", this.baseHits);
        this.hitsPerPower = parameters.getFloat("hitsPerPower", this.hitsPerPower);
        return previous;
    }

    private void restoreExtraParameters(ExtraParams previous) {
        this.durationSeconds = previous.durationSeconds();
        this.baseHits = previous.baseHits();
        this.hitsPerPower = previous.hitsPerPower();
    }

    private record ExtraParams(int durationSeconds, float baseHits, float hitsPerPower) {
    }

    private int getHitCount(int spellLevel, LivingEntity caster) {
        return Math.max(1, (int) (baseHits + getSpellPower(spellLevel, caster) * hitsPerPower));
    }
}
