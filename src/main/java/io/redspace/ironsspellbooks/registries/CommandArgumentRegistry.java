package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.command.SpellArgument;
import io.redspace.ironsspellbooks.command.UUIDArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CommandArgumentRegistry {
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES.getRegistryName(), IronsSpellbooks.MODID);

    private static final RegistryObject<SingletonArgumentInfo<SpellArgument>> SPELL_COMMAND_ARGUMENT_TYPE = ARGUMENT_TYPES.register("spell", () -> ArgumentTypeInfos.registerByClass(SpellArgument.class, SingletonArgumentInfo.contextFree(SpellArgument::spellArgument)));

    private static final RegistryObject<SingletonArgumentInfo<UUIDArgument>> UUID_COMMAND_ARGUMENT_TYPE = ARGUMENT_TYPES.register("uuid", () -> ArgumentTypeInfos.registerByClass(UUIDArgument.class, SingletonArgumentInfo.contextFree(UUIDArgument::uuid)));

    public static void register(IEventBus modEventBus) {
        ARGUMENT_TYPES.register(modEventBus);
    }
}
