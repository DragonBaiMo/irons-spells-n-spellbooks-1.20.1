package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.goals.HomeOwner;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;
import io.redspace.ironsspellbooks.api.spells.parameters.ParameterType;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

@AutoSpellConfig
public class RecallSpell extends AbstractSpell implements IParameterizedSpell  {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "recall");

    public RecallSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 100;
        
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
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(300)
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
        return Optional.of(SoundRegistry.RECALL_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ENDERMAN_TELEPORT);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        playSound(getCastFinishSound(), entity);
        if (entity instanceof ServerPlayer serverPlayer) {
            ServerLevel respawnLevel = ((ServerLevel) world).getServer().getLevel(serverPlayer.getRespawnDimension());
            respawnLevel = respawnLevel == null ? world.getServer().overworld() : respawnLevel;
            var spawnLocation = findSpawnPosition(respawnLevel, serverPlayer);
            //IronsSpellbooks.LOGGER.debug("Recall.onCast findSpawnLocation: {}", spawnLocation);
            if (spawnLocation.isPresent()) {
                Vec3 vec3 = spawnLocation.get();
                //IronsSpellbooks.LOGGER.debug("Recall.onCast.a dimension: {} -> {}", serverPlayer.level.dimension(), respawnLevel.dimension());
                if (serverPlayer.level.dimension() != respawnLevel.dimension()) {
                    serverPlayer.changeDimension(respawnLevel, new PortalTeleporter(vec3));
                } else {
                    serverPlayer.teleportTo(vec3.x, vec3.y, vec3.z);
                }
            } else {
                respawnLevel = world.getServer().overworld();
                //IronsSpellbooks.LOGGER.debug("Recall.onCast.b dimension: {} -> {}", serverPlayer.level.dimension(), respawnLevel.dimension());
                if (serverPlayer.level.dimension() != respawnLevel.dimension()) {
                    serverPlayer.changeDimension(respawnLevel, new PortalTeleporter(Vec3.ZERO));
                }
                serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
                var pos = respawnLevel.getSharedSpawnPos();
                serverPlayer.teleportTo(pos.getX(), pos.getY(), pos.getZ());
            }
        } else if (entity instanceof HomeOwner homeOwner && homeOwner.getHome() != null) {
            //no dimension check because lazy
            var pos = homeOwner.getHome();
            entity.teleportTo(pos.getX(), pos.getY() + .15, pos.getZ());
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public static void ambientParticles(LivingEntity entity, SyncedSpellData spellData) {
        float f = entity.tickCount * .125f;
        Vec3 trail1 = new Vec3(Mth.cos(f), Mth.sin(f * 2), Mth.sin(f)).normalize()/*.scale(1.5f + Mth.sin(f) * .5f)*/;
        Vec3 trail2 = new Vec3(Mth.sin(f), Mth.cos(f * 2), Mth.cos(f)).normalize()/*.scale(1.5f + Mth.cos(f) * .5f)*/;
        Vec3 trail3 = trail1.multiply(trail2).normalize().scale(1f + (Mth.sin(f) + Mth.cos(f)) * .5f);
        Vec3 pos = entity.getBoundingBox().getCenter();
        entity.level.addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail1.x, pos.y + trail1.y, pos.z + trail1.z, 0, 0, 0);
        entity.level.addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail2.x, pos.y + trail2.y, pos.z + trail2.z, 0, 0, 0);
        entity.level.addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail3.x, pos.y + trail3.y, pos.z + trail3.z, 0, 0, 0);
    }

    @Override
    public void playSound(Optional<SoundEvent> sound, Entity entity) {
        sound.ifPresent(soundEvent -> entity.playSound(soundEvent, 2.0f, 1f));
    }

    /**
     * Adapted from vanilla {@link Player#findRespawnPositionAndUseSpawnBlock(ServerLevel, BlockPos, float, boolean, boolean)}
     */
    public static Optional<Vec3> findSpawnPosition(ServerLevel level, ServerPlayer player) {
        BlockPos spawnBlockpos = player.getRespawnPosition();
        if (spawnBlockpos == null) {
            return Optional.empty();
        }
        BlockState blockstate = level.getBlockState(spawnBlockpos);
        Block block = blockstate.getBlock();
        if (block instanceof RespawnAnchorBlock && blockstate.getValue(RespawnAnchorBlock.CHARGE) > 0 && RespawnAnchorBlock.canSetSpawn(level)) {
            //IronsSpellbooks.LOGGER.debug("RecallSpell.findSpawnPosition.respawnAnchor");
            return RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, spawnBlockpos);
        } else if (block instanceof BedBlock && BedBlock.canSetSpawn(level)) {
            //IronsSpellbooks.LOGGER.debug("RecallSpell.findSpawnPosition.bed");
            return BedBlock.findStandUpPosition(EntityType.PLAYER, level, spawnBlockpos, player.getDirection(), player.getYRot());
        } else {
            return Optional.empty();
//            boolean flag = block.isPossibleToRespawnInThis();
//            boolean flag1 = level.getBlockState(spawnBlockpos.above()).getBlock().isPossibleToRespawnInThis();
//            return flag && flag1 ? Optional.of(new Vec3((double)spawnBlockpos.getX() + 0.5D, (double)spawnBlockpos.getY() + 0.1D, (double)spawnBlockpos.getZ() + 0.5D)) : Optional.empty();
        }
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }

    @Override
    public boolean stopSoundOnCancel() {
        return true;
    }

    //TODO: replace with portal's teleporter on merge with recast?
    public static class PortalTeleporter implements ITeleporter {
        private final Vec3 destinationPosition;

        PortalTeleporter(Vec3 destinationPosition) {
            this.destinationPosition = destinationPosition;
        }

        @Override
        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
            entity.fallDistance = 0;
            return repositionEntity.apply(false);
        }

        @Override
        public @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
            return new PortalInfo(destinationPosition, Vec3.ZERO, entity.getYRot(), entity.getXRot());
        }

        @Override
        public boolean isVanilla() {
            return false;
        }

        @Override
        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
            return false;
        }
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
                .build();
    }

    @Override
    public void onCastWithParameters(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, SpellParameters parameters) {
        SpellParameterConfig fallback = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);
        SpellParameterConfig config = SpellParameterLoader.resolve(getSpellId(), parameters, fallback);
        SpellParameterConfig previous = this.applyParameterOverrides(config);
        try {
            this.onCast(level, spellLevel, entity, castSource, playerMagicData);
        } finally {
            this.restoreParameters(previous);
        }
    }
}
