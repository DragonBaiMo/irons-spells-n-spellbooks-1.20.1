package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.manager.SpellCastManager;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class CastCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = dispatcher.register(Commands.literal("iss")
                .then(Commands.literal("cast")
                        .requires((p) -> p.hasPermission(2))
                        .then(Commands.argument("casters", EntityArgument.entities())
                                .then(Commands.argument("spell", SpellArgument.spellArgument())
                                        .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", String.class)))
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", String.class), IntegerArgumentType.getInteger(context, "level")))
                                                // 分支1：使用实体选择器作为目标
                                                .then(Commands.argument("target", EntityArgument.entity())
                                                        .executes((context) -> castSpell(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "casters"),
                                                                context.getArgument("spell", String.class),
                                                                IntegerArgumentType.getInteger(context, "level"),
                                                                EntityArgument.getEntity(context, "target")
                                                        ))
                                                        .then(Commands.argument("json", StringArgumentType.greedyString())
                                                                .executes((context) -> castSpellWithJson(
                                                                        context.getSource(),
                                                                        EntityArgument.getEntities(context, "casters"),
                                                                        context.getArgument("spell", String.class),
                                                                        IntegerArgumentType.getInteger(context, "level"),
                                                                        StringArgumentType.getString(context, "json"),
                                                                        EntityArgument.getEntity(context, "target")
                                                                ))))
                                                // 分支2：使用UUID作为目标
                                                .then(Commands.argument("target_uuid", UUIDArgument.uuid())
                                                        .executes((context) -> castSpellWithUuid(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "casters"),
                                                                context.getArgument("spell", String.class),
                                                                IntegerArgumentType.getInteger(context, "level"),
                                                                UUIDArgument.getUUID(context, "target_uuid")
                                                        ))
                                                        .then(Commands.argument("json", StringArgumentType.greedyString())
                                                                .executes((context) -> castSpellWithUuidAndJson(
                                                                        context.getSource(),
                                                                        EntityArgument.getEntities(context, "casters"),
                                                                        context.getArgument("spell", String.class),
                                                                        IntegerArgumentType.getInteger(context, "level"),
                                                                        StringArgumentType.getString(context, "json"),
                                                                        UUIDArgument.getUUID(context, "target_uuid")
                                                                ))))
                                                .then(Commands.argument("json", StringArgumentType.greedyString())
                                                        .executes((context) -> castSpellWithJson(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "casters"),
                                                                context.getArgument("spell", String.class),
                                                                IntegerArgumentType.getInteger(context, "level"),
                                                                StringArgumentType.getString(context, "json"),
                                                                null
                                                        ))))
                                        .then(Commands.argument("function value", FunctionArgument.functions())
                                                .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", String.class), FunctionArgument.getFunctions(context, "function value"))))
                                ))
                )
        );
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, Collection<CommandFunction> functions) {
        int i = 0;

        for (CommandFunction commandfunction : functions) {
            i += source.getServer().getFunctions().execute(commandfunction, source.withSuppressedOutput().withMaximumPermission(2));
        }
        return castSpell(source, targets, spellId, i);
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId) {
        return castSpell(source, targets, spellId, 1);
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, int spellLevel) {
        return castSpell(source, targets, spellId, spellLevel, null);
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, int spellLevel, Entity explicitTarget) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }

        var spell = SpellRegistry.getSpell(spellId);
        if (spell == null || targets.isEmpty()) {
            return 0;
        }

        // 优化：避免不必要的迭代和check
        for (Entity target : targets) {
            if (target instanceof ServerPlayer serverPlayer) {
                // 如果有显式指定的目标，设置到magicData
                if (explicitTarget != null && explicitTarget instanceof LivingEntity livingTarget) {
                    MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                    magicData.setAdditionalCastData(new TargetEntityCastData(livingTarget));
                }
                spell.attemptInitiateCast(ItemStack.EMPTY, spellLevel, source.getLevel(), serverPlayer, CastSource.COMMAND, false, "command");
            } else if (target instanceof IMagicEntity castingMob) {
                castingMob.initiateCastSpell(spell, spellLevel);
            } else if (target instanceof LivingEntity livingEntity) {
                var magicData = MagicData.getPlayerMagicData(livingEntity);

                if (!spell.checkPreCastConditions(source.getLevel(), spellLevel, livingEntity, magicData)) {
                    return 0;
                }

                //if (spell.getCastType() == CastType.INSTANT) {
                spell.onCast(source.getLevel(), spellLevel, livingEntity, CastSource.COMMAND, magicData);
                spell.onServerCastComplete(source.getLevel(), spellLevel, livingEntity, magicData, false);
                //} else {
                //    int effectiveCastTime = spell.getEffectiveCastTime(spellLevel, livingEntity);
                //    magicData.initiateCast(spell, spellLevel, effectiveCastTime, CastSource.MOB, "command");
                //    spell.onServerPreCast(source.getLevel(), spellLevel, livingEntity, magicData);
                //}
            }
        }
        return 1;
    }

    private static int castSpellWithJson(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, int spellLevel, String jsonParams) {
        return castSpellWithJson(source, targets, spellId, spellLevel, jsonParams, null);
    }

    private static int castSpellWithJson(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, int spellLevel, String jsonParams, Entity explicitTarget) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }

        int successCount = 0;
        for (Entity target : targets) {
            if (target instanceof ServerPlayer serverPlayer) {
                // 如果有显式指定的目标，设置到magicData
                if (explicitTarget != null && explicitTarget instanceof LivingEntity livingTarget) {
                    MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                    magicData.setAdditionalCastData(new TargetEntityCastData(livingTarget));
                }
                if (SpellCastManager.castSpell(serverPlayer, spellId, spellLevel, jsonParams)) {
                    successCount++;
                }
            } else {
                source.sendFailure(Component.literal("非玩家实体暂不支持参数化施法"));
            }
        }

        if (successCount > 0) {
            int finalSuccessCount = successCount;
            source.sendSuccess(() -> Component.literal(String.format("成功为 %d 个目标施放技能", finalSuccessCount)), true);
        }

        return successCount;
    }

    private static int castSpellWithUuid(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, int spellLevel, java.util.UUID targetUuid) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }

        var spell = SpellRegistry.getSpell(spellId);
        Entity targetEntity = source.getLevel().getEntity(targetUuid);

        if (targetEntity == null) {
            source.sendFailure(Component.literal("未找到UUID对应的实体"));
            return 0;
        }

        for (Entity target : targets) {
            if (target instanceof ServerPlayer serverPlayer) {
                if (targetEntity instanceof LivingEntity livingTarget) {
                    MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                    magicData.setAdditionalCastData(new TargetEntityCastData(livingTarget));
                }
                spell.attemptInitiateCast(ItemStack.EMPTY, spellLevel, source.getLevel(), serverPlayer, CastSource.COMMAND, false, "command");
            } else if (target instanceof IMagicEntity castingMob) {
                castingMob.initiateCastSpell(spell, spellLevel);
            } else if (target instanceof LivingEntity livingEntity) {
                var magicData = MagicData.getPlayerMagicData(livingEntity);

                if (!spell.checkPreCastConditions(source.getLevel(), spellLevel, livingEntity, magicData)) {
                    return 0;
                }

                spell.onCast(source.getLevel(), spellLevel, livingEntity, CastSource.COMMAND, magicData);
                spell.onServerCastComplete(source.getLevel(), spellLevel, livingEntity, magicData, false);
            }
        }
        return 1;
    }

    private static int castSpellWithUuidAndJson(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, int spellLevel, String jsonParams, java.util.UUID targetUuid) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }

        Entity targetEntity = source.getLevel().getEntity(targetUuid);

        if (targetEntity == null) {
            source.sendFailure(Component.literal("未找到UUID对应的实体"));
            return 0;
        }

        int successCount = 0;
        for (Entity target : targets) {
            if (target instanceof ServerPlayer serverPlayer) {
                if (targetEntity instanceof LivingEntity livingTarget) {
                    MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                    magicData.setAdditionalCastData(new TargetEntityCastData(livingTarget));
                }
                if (SpellCastManager.castSpell(serverPlayer, spellId, spellLevel, jsonParams)) {
                    successCount++;
                }
            } else {
                source.sendFailure(Component.literal("非玩家实体暂不支持参数化施法"));
            }
        }

        if (successCount > 0) {
            int finalSuccessCount = successCount;
            source.sendSuccess(() -> Component.literal(String.format("成功为 %d 个目标施放技能", finalSuccessCount)), true);
        }

        return successCount;
    }
}