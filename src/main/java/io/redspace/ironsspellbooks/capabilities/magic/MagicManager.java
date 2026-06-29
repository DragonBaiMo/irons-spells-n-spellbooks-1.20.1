package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.IMagicManager;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.manager.SpellCastManager;
import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnClientCast;
import io.redspace.ironsspellbooks.spells.parameters.JsonCastData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.network.ClientboundSyncCooldown;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.*;

public class MagicManager implements IMagicManager {
    public static final int MANA_REGEN_TICKS = 10;
    public static final int CONTINUOUS_CAST_TICK_INTERVAL = 10;

    public boolean regenPlayerMana(ServerPlayer serverPlayer, MagicData playerMagicData) {
        int playerMaxMana = (int) serverPlayer.getAttributeValue(MAX_MANA.get());
        var mana = playerMagicData.getMana();
        if (mana != playerMaxMana) {
            float playerManaRegenMultiplier = (float) serverPlayer.getAttributeValue(MANA_REGEN.get());
//            var increment = (1 + (playerMaxMana - 100) * 0.005f) * playerManaRegenMultiplier;
            var increment = playerMaxMana * 0.01f * playerManaRegenMultiplier;
            playerMagicData.setMana(Mth.clamp(playerMagicData.getMana() + increment, 0, playerMaxMana));
            return true;
        } else {
            return false;
        }
    }


    public void tick(Level level) {
        boolean doManaRegen = level.getServer().getTickCount() % MANA_REGEN_TICKS == 0;

        // 优化：避免stream().toList()创建中间列表，直接迭代
        for (Player player : level.players()) {
            if (player instanceof ServerPlayer serverPlayer) {
                MagicData playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
                playerMagicData.getPlayerCooldowns().tick(1);
                playerMagicData.getPlayerRecasts().tick(2);

                if (playerMagicData.isCasting()) {
                    playerMagicData.handleCastDuration();
                    var spell = SpellRegistry.getSpell(playerMagicData.getCastingSpellId());
                    if ((spell.getCastType() == CastType.LONG && !serverPlayer.isUsingItem()) || spell.getCastType() == CastType.INSTANT) {
                        if (playerMagicData.getCastDurationRemaining() <= 0) {
                            if (playerMagicData.getCastSource() == CastSource.COMMAND) {
                                handlePendingCommandCast(serverPlayer, spell, playerMagicData);
                            } else {
                                spell.castSpell(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData.getCastSource(), true);
                                if (playerMagicData.getCastSource() == CastSource.SCROLL) {
                                    Scroll.attemptRemoveScrollAfterCast(serverPlayer);
                                }
                                spell.onServerCastComplete(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData, false);
                            }
                        }
                    } else if (spell.getCastType() == CastType.CONTINUOUS) {
                        if ((playerMagicData.getCastDurationRemaining() + 1) % CONTINUOUS_CAST_TICK_INTERVAL == 0) {
                            if (playerMagicData.getCastDurationRemaining() < CONTINUOUS_CAST_TICK_INTERVAL || (playerMagicData.getCastSource().consumesMana() && playerMagicData.getMana() - spell.getManaCost(playerMagicData.getCastingSpellLevel()) * 2 < 0)) {
                                spell.castSpell(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData.getCastSource(), true);

                                if (playerMagicData.getCastSource() == CastSource.SCROLL) {
                                    Scroll.attemptRemoveScrollAfterCast(serverPlayer);
                                }

                                spell.onServerCastComplete(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData, false);

                            } else {
                                spell.castSpell(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData.getCastSource(), false);
                            }
                        }
                    }

                    if (playerMagicData.isCasting()) {
                        spell.onServerCastTick(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData);
                    }
                }

                if (doManaRegen) {
                    if (regenPlayerMana(serverPlayer, playerMagicData)) {
                        Messages.sendToPlayer(new ClientboundSyncMana(playerMagicData), serverPlayer);
                    }
                }
            }
        }
    }

    public void addCooldown(ServerPlayer serverPlayer, AbstractSpell spell, CastSource castSource) {
        if (castSource == CastSource.SCROLL)
            return;
        int effectiveCooldown = getEffectiveSpellCooldown(spell, serverPlayer, castSource);

        MagicData.getPlayerMagicData(serverPlayer).getPlayerCooldowns().addCooldown(spell, effectiveCooldown);
        Messages.sendToPlayer(new ClientboundSyncCooldown(spell.getSpellId(), effectiveCooldown), serverPlayer);
    }

    private void handlePendingCommandCast(ServerPlayer serverPlayer, AbstractSpell spell, MagicData playerMagicData) {
        var pending = SpellCastManager.consumePendingCast(serverPlayer.getUUID());
        if (pending == null || !pending.spellId().equals(spell.getSpellId()) || !(spell instanceof IParameterizedSpell parameterizedSpell)) {
            spell.castSpell(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData.getCastSource(), true);
            spell.onServerCastComplete(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData, false);
            return;
        }

        SpellParameterSchema.ValidationResult validation = parameterizedSpell.getParameterSchema().validate(pending.params());
        if (!validation.success()) {
            serverPlayer.sendSystemMessage(validation.toComponent().copy().withStyle(ChatFormatting.RED));
            spell.onServerCastComplete(serverPlayer.level, playerMagicData.getCastingSpellLevel(), serverPlayer, playerMagicData, true);
            return;
        }

        SpellParameters normalized = validation.normalized();
        SpellParameterConfig baseline = spell.snapshotParameters();
        SpellParameterConfig resolved = SpellParameterLoader.resolve(spell.getSpellId(), normalized, baseline);
        SpellParameterConfig previous = spell.applyParameterOverrides(resolved);
        boolean castSucceeded = false;
        try {
            parameterizedSpell.onCastWithParameters(serverPlayer.level(), pending.level(), serverPlayer, CastSource.COMMAND, playerMagicData, normalized);

            if (pending.options().consumeMana) {
                int manaCost = calculateCommandManaCost(spell, resolved, pending.level());
                if (manaCost > 0) {
                    playerMagicData.setMana(Math.max(0, playerMagicData.getMana() - manaCost));
                    Messages.sendToPlayer(new ClientboundSyncMana(playerMagicData), serverPlayer);
                }
            }

            if (pending.options().triggerCooldown) {
                applyCommandCooldown(serverPlayer, spell, resolved);
            }

            if (pending.options().playEffects) {
                var broadcastData = playerMagicData.getAdditionalCastData();
                if (broadcastData == null) {
                    broadcastData = new JsonCastData(normalized);
                }
                Messages.sendToPlayer(new ClientboundOnClientCast(spell.getSpellId(), pending.level(), CastSource.COMMAND, broadcastData), serverPlayer);
            }

            castSucceeded = true;
            spell.onServerCastComplete(serverPlayer.level, pending.level(), serverPlayer, playerMagicData, false);
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("延迟施法执行异常", e);
            serverPlayer.sendSystemMessage(Component.literal("施法过程中发生错误: " + e.getMessage()).withStyle(ChatFormatting.RED));
            spell.onServerCastComplete(serverPlayer.level, pending.level(), serverPlayer, playerMagicData, true);
        } finally {
            spell.restoreParameters(previous);
        }
    }

    private int calculateCommandManaCost(AbstractSpell spell, SpellParameterConfig config, int level) {
        double multiplier = ServerConfigs.getSpellConfig(spell).manaMultiplier();
        return (int) ((config.baseManaCost() + config.manaCostPerLevel() * Math.max(level - 1, 0)) * multiplier);
    }

    private void applyCommandCooldown(ServerPlayer player, AbstractSpell spell, SpellParameterConfig config) {
        int baseTicks = (int) Math.round(config.cooldownSeconds() * 20.0);
        int defaultTicks = spell.getSpellCooldown();
        int effectiveDefault = getEffectiveSpellCooldown(spell, player, CastSource.COMMAND);

        int cooldownTicks;
        if (baseTicks <= 0) {
            cooldownTicks = effectiveDefault;
        } else if (defaultTicks > 0 && effectiveDefault > 0) {
            cooldownTicks = Math.max(1, Math.round(baseTicks / (float) defaultTicks * effectiveDefault));
        } else {
            cooldownTicks = baseTicks;
        }

        MagicData magicData = MagicData.getPlayerMagicData(player);
        magicData.getPlayerCooldowns().addCooldown(spell, cooldownTicks);
        magicData.getPlayerCooldowns().syncToPlayer(player);
    }

    public void clearCooldowns(ServerPlayer serverPlayer) {
        MagicData.getPlayerMagicData(serverPlayer).getPlayerCooldowns().clearCooldowns();
        MagicData.getPlayerMagicData(serverPlayer).getPlayerCooldowns().syncToPlayer(serverPlayer);
    }

    public static int getEffectiveSpellCooldown(AbstractSpell spell, Player player, CastSource castSource) {
        double playerCooldownModifier = player.getAttributeValue(COOLDOWN_REDUCTION.get());

        float itemCoolDownModifer = 1;
        if (castSource == CastSource.SWORD) {
            itemCoolDownModifer = ServerConfigs.SWORDS_CD_MULTIPLIER.get().floatValue();
        }
        return (int) (spell.getSpellCooldown() * (2 - Utils.softCapFormula(playerCooldownModifier)) * itemCoolDownModifer);
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }
}
