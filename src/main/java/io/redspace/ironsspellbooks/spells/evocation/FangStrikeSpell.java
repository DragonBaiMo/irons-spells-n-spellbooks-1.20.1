package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ExtendedEvokerFang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
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
public class FangStrikeSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "fang_strike");
    private int baseCount = 7;
    private int countPerLevel = 1;
    private float spacing = 1f;
    private int delayDivisor = 3;
    private int groundSearchMaxSteps = 8;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.fang_count", getCount(spellLevel, caster)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(5)
            .build();

    public FangStrikeSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 30;
        this.baseCount = 7;
        this.countPerLevel = 1;
        this.spacing = 1f;
        this.delayDivisor = 3;
        this.groundSearchMaxSteps = 8;
        
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
        return Optional.of(SoundEvents.EVOKER_PREPARE_ATTACK);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 forward = entity.getForward().multiply(1, 0, 1).normalize();
        Vec3 start = entity.getEyePosition().add(forward.scale(spacing * 1.5f));

        for (int i = 0; i < getCount(spellLevel, entity); i++) {
            Vec3 spawn = start.add(forward.scale(i * spacing));
            spawn = new Vec3(spawn.x, getGroundLevel(world, spawn, groundSearchMaxSteps), spawn.z);
            if (!world.getBlockState(BlockPos.containing(spawn).below()).isAir()) {
                int delay = delayDivisor > 0 ? i / delayDivisor : 0;
                ExtendedEvokerFang fang = new ExtendedEvokerFang(world, spawn.x, spawn.y, spawn.z, (entity.getYRot() - 90) * Mth.DEG_TO_RAD, delay, entity, getDamage(spellLevel, entity));
                world.addFreshEntity(fang);
            }
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getGroundLevel(Level level, Vec3 start, int maxSteps) {
        if (!level.getBlockState(BlockPos.containing(start)).isAir()) {
            for (int i = 0; i < maxSteps; i++) {
                start = start.add(0, 1, 0);
                if (level.getBlockState(BlockPos.containing(start)).isAir())
                    break;
            }
        }
        //Vec3 upper = level.clip(new ClipContext(start, start.add(0, maxSteps, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getLocation();
        Vec3 lower = level.clip(new ClipContext(start, start.add(0, maxSteps * -2, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getLocation();
        return (int) lower.y;
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        float f = this.getCount(spellLevel, mob) * spacing * 1.2f;
        return mob.distanceToSqr(target) > (f * f);
    }

    private int getCount(int spellLevel, LivingEntity entity) {
        return baseCount + spellLevel * countPerLevel;
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity);
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
                .optional("baseCount", ParameterType.INT, this.baseCount, "基础獠牙数量")
                .optional("countPerLevel", ParameterType.INT, this.countPerLevel, "每级追加数量")
                .optional("spacing", ParameterType.FLOAT, this.spacing, "獠牙间距")
                .optional("delayDivisor", ParameterType.INT, this.delayDivisor, "生成延迟除数")
                .optional("groundSearchMaxSteps", ParameterType.INT, this.groundSearchMaxSteps, "地面搜索最大步数")
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
        ExtraParams previous = new ExtraParams(this.baseCount, this.countPerLevel, this.spacing, this.delayDivisor, this.groundSearchMaxSteps);
        this.baseCount = parameters.getInt("baseCount", this.baseCount);
        this.countPerLevel = parameters.getInt("countPerLevel", this.countPerLevel);
        this.spacing = parameters.getFloat("spacing", this.spacing);
        this.delayDivisor = parameters.getInt("delayDivisor", this.delayDivisor);
        this.groundSearchMaxSteps = parameters.getInt("groundSearchMaxSteps", this.groundSearchMaxSteps);
        return previous;
    }

    private void restoreExtraParameters(ExtraParams previous) {
        this.baseCount = previous.baseCount();
        this.countPerLevel = previous.countPerLevel();
        this.spacing = previous.spacing();
        this.delayDivisor = previous.delayDivisor();
        this.groundSearchMaxSteps = previous.groundSearchMaxSteps();
    }

    private record ExtraParams(int baseCount, int countPerLevel, float spacing, int delayDivisor, int groundSearchMaxSteps) {
    }
}
