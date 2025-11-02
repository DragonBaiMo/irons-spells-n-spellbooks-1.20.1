package io.redspace.ironsspellbooks.spells.parameters;

import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * JSON 参数数据载体，用于在施法过程中传递 JSON 参数。
 */
public class JsonCastData implements ICastDataSerializable {
    private CompoundTag tag;

    public JsonCastData() {
        this.tag = new CompoundTag();
    }

    public JsonCastData(CompoundTag tag) {
        this.tag = tag == null ? new CompoundTag() : tag.copy();
    }

    public JsonCastData(SpellParameters parameters) {
        this.tag = parameters.toNBT();
    }

    public CompoundTag getTag() {
        return tag;
    }

    public SpellParameters toSpellParameters() {
        return SpellParameters.fromNBT(tag);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeNbt(tag);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        CompoundTag read = buffer.readNbt();
        this.tag = read == null ? new CompoundTag() : read;
    }

    @Override
    public CompoundTag serializeNBT() {
        return tag.copy();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.tag = nbt == null ? new CompoundTag() : nbt.copy();
    }

    @Override
    public void reset() {
        this.tag = new CompoundTag();
    }
}
