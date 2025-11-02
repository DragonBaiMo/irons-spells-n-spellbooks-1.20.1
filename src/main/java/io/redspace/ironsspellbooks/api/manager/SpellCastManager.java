package io.redspace.ironsspellbooks.api.manager;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastOptions;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnCastFinished;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnCastStarted;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.network.ClientboundUpdateCastingState;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnClientCast;
import io.redspace.ironsspellbooks.spells.parameters.JsonCastData;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 管理员施法旁路管理器。
 */
public final class SpellCastManager {
    private SpellCastManager() {
    }

    public static boolean castSpell(ServerPlayer player, String spellId, int level, String jsonParams) {
        try {
            SpellParameters params = SpellParameters.fromJson(jsonParams);
            return castSpellInternal(player, spellId, level, params);
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("JSON 解析失败: " + e.getMessage()).withStyle(ChatFormatting.RED));
            IronsSpellbooks.LOGGER.error("施法指令 JSON 解析错误", e);
            return false;
        }
    }

    public static boolean castSpell(ServerPlayer player, String spellId, int level, SpellParameters params) {
        return castSpellInternal(player, spellId, level, params);
    }

    private static boolean castSpellInternal(ServerPlayer player, String spellId, int level, SpellParameters params) {
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null || spell == SpellRegistry.none()) {
            player.sendSystemMessage(Component.literal("技能不存在: " + spellId).withStyle(ChatFormatting.RED));
            return false;
        }

        if (!(spell instanceof IParameterizedSpell parameterizedSpell)) {
            player.sendSystemMessage(Component.literal("技能 '" + spell.getSpellName() + "' 不支持参数化施法，回退默认逻辑")
                    .withStyle(ChatFormatting.YELLOW));
            return spell.attemptInitiateCast(ItemStack.EMPTY,
                    level,
                    player.level(),
                    player,
                    CastSource.COMMAND,
                    false,
                    "command");
        }

        SpellParameterSchema schema = parameterizedSpell.getParameterSchema();
        SpellParameterSchema.ValidationResult validation = schema.validate(params);
        if (!validation.success()) {
            player.sendSystemMessage(validation.toComponent().copy().withStyle(ChatFormatting.RED));
            return false;
        }

        SpellParameters normalized = validation.normalized();
        SpellParameterConfig baseline = spell.snapshotParameters();
        SpellParameterConfig resolved = SpellParameterLoader.resolve(spell.getSpellId(), normalized, baseline);
        CastOptions options = CastOptions.fromParameters(normalized, CastSource.COMMAND);
        MagicData magicData = MagicData.getPlayerMagicData(player);
        SpellParameterConfig previousParameters = spell.applyParameterOverrides(resolved);
        boolean castBarInitiated = false;
        boolean castSucceeded = false;
        JsonCastData castData = new JsonCastData(normalized);

        try {
            if (!options.bypassConditions) {
                CastResult result = spell.canBeCastedBy(level, CastSource.COMMAND, magicData, player);
                if (!result.isSuccess()) {
                    if (result.message != null) {
                        player.sendSystemMessage(result.message.copy().withStyle(ChatFormatting.RED));
                    }
                    return false;
                }
                if (!spell.checkPreCastConditions(player.level(), level, player, magicData)) {
                    player.sendSystemMessage(Component.literal("施法前置条件未满足").withStyle(ChatFormatting.RED));
                    return false;
                }
            }

            if (options.showCastBar) {
                int effectiveCastTime = spell.getEffectiveCastTime(level, player);
                if (effectiveCastTime > 0 && !magicData.isCasting()) {
                    magicData.initiateCast(spell, level, effectiveCastTime, CastSource.COMMAND, "command");
                    castBarInitiated = true;
                    Messages.sendToPlayer(new ClientboundUpdateCastingState(spell.getSpellId(), level, effectiveCastTime, CastSource.COMMAND, "command"), player);
                    Messages.sendToPlayersTrackingEntity(new ClientboundOnCastStarted(player.getUUID(), spell.getSpellId(), level), player, true);
                }
            }

            parameterizedSpell.onCastWithParameters(player.level(), level, player, CastSource.COMMAND, magicData, normalized);

            if (options.consumeMana) {
                int manaCost = calculateManaCost(spell, resolved, level);
                if (manaCost > 0) {
                    magicData.setMana(Math.max(0, magicData.getMana() - manaCost));
                    Messages.sendToPlayer(new ClientboundSyncMana(magicData), player);
                }
            }

            if (options.triggerCooldown) {
                applyCooldown(player, spell, resolved);
            }

            if (options.playEffects) {
                Messages.sendToPlayer(new ClientboundOnClientCast(spell.getSpellId(), level, CastSource.COMMAND, castData), player);
            }

            spell.onServerCastComplete(player.level(), level, player, magicData, false);
            player.sendSystemMessage(Component.literal("成功施放技能: " + spell.getSpellName()).withStyle(ChatFormatting.GREEN));
            castSucceeded = true;
            return true;
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("施法过程中发生错误: " + e.getMessage()).withStyle(ChatFormatting.RED));
            IronsSpellbooks.LOGGER.error("参数化施法执行错误", e);
            return false;
        } finally {
            if (!castSucceeded && castBarInitiated) {
                magicData.resetCastingState();
                Messages.sendToPlayersTrackingEntity(new ClientboundOnCastFinished(player.getUUID(), spell.getSpellId(), true), player, true);
            }
            spell.restoreParameters(previousParameters);
        }
    }

    public static boolean castSpellDirect(LivingEntity caster, AbstractSpell spell, int level,
                                          SpellParameters params, CastOptions options) {
        if (!(spell instanceof IParameterizedSpell parameterizedSpell)) {
            return false;
        }
        SpellParameterSchema.ValidationResult validation = parameterizedSpell.getParameterSchema().validate(params);
        if (!validation.success()) {
            IronsSpellbooks.LOGGER.warn("直接施法参数校验失败: {}", validation.toComponent().getString());
            return false;
        }

        SpellParameters normalized = validation.normalized();
        MagicData magicData = MagicData.getPlayerMagicData(caster);
        SpellParameterConfig baseline = spell.snapshotParameters();
        SpellParameterConfig resolved = SpellParameterLoader.resolve(spell.getSpellId(), normalized, baseline);
        SpellParameterConfig previous = spell.applyParameterOverrides(resolved);
        try {
            parameterizedSpell.onCastWithParameters(caster.level(), level, caster, CastSource.COMMAND, magicData, normalized);
            return true;
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("直接施法失败", e);
            return false;
        } finally {
            spell.restoreParameters(previous);
        }
    }

    private static int calculateManaCost(AbstractSpell spell, SpellParameterConfig config, int level) {
        double multiplier = ServerConfigs.getSpellConfig(spell).manaMultiplier();
        return (int) ((config.baseManaCost() + config.manaCostPerLevel() * Math.max(level - 1, 0)) * multiplier);
    }

    private static void applyCooldown(ServerPlayer player, AbstractSpell spell, SpellParameterConfig config) {
        int baseTicks = (int) Math.round(config.cooldownSeconds() * 20.0);
        int defaultTicks = spell.getSpellCooldown();
        int effectiveDefault = MagicManager.getEffectiveSpellCooldown(spell, player, CastSource.COMMAND);

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
}
