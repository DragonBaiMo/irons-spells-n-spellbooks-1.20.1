package io.redspace.ironsspellbooks.api.spells.parameters;

import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public enum ParameterType {
    INT("整数", JsonElement::getAsInt, tag -> tag.getInt("value")),
    FLOAT("浮点数", JsonElement::getAsFloat, tag -> tag.getFloat("value")),
    DOUBLE("双精度", JsonElement::getAsDouble, tag -> tag.getDouble("value")),
    STRING("字符串", JsonElement::getAsString, tag -> tag.getString("value")),
    BOOLEAN("布尔值", JsonElement::getAsBoolean, tag -> tag.getBoolean("value")),
    VEC3("三维坐标", ParameterType::parseVec3FromJson, ParameterType::parseVec3FromNBT),
    UUID("实体UUID", json -> java.util.UUID.fromString(json.getAsString()), tag -> tag.getUUID("value")),
    ENTITY_SELECTOR("实体选择器", JsonElement::getAsString, tag -> tag.getString("value")),
    DIMENSION("维度", JsonElement::getAsString, tag -> tag.getString("value"));

    private final String displayName;
    private final Function<JsonElement, Object> jsonParser;
    private final Function<CompoundTag, Object> nbtParser;

    ParameterType(String displayName, Function<JsonElement, Object> jsonParser, Function<CompoundTag, Object> nbtParser) {
        this.displayName = displayName;
        this.jsonParser = jsonParser;
        this.nbtParser = nbtParser;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Object parseFromJson(JsonElement element) {
        return jsonParser.apply(element);
    }

    public Object parseFromNBT(CompoundTag tag) {
        return nbtParser.apply(tag);
    }

    public boolean isValueValid(Object value) {
        if (value == null) {
            return false;
        }
        return switch (this) {
            case INT -> value instanceof Number;
            case FLOAT -> value instanceof Number;
            case DOUBLE -> value instanceof Number;
            case STRING -> true;
            case BOOLEAN -> value instanceof Boolean || value instanceof Number;
            case VEC3 -> value instanceof Vec3;
            case UUID -> value instanceof java.util.UUID || value instanceof String;
            case ENTITY_SELECTOR, DIMENSION -> true;
        };
    }

    public Object normalize(Object value) {
        if (value == null) {
            return null;
        }
        return switch (this) {
            case INT -> ((Number) value).intValue();
            case FLOAT -> ((Number) value).floatValue();
            case DOUBLE -> ((Number) value).doubleValue();
            case STRING -> value.toString();
            case BOOLEAN -> value instanceof Boolean bool ? bool : ((Number) value).intValue() != 0;
            case VEC3 -> (Vec3) value;
            case UUID -> value instanceof java.util.UUID uuid ? uuid : java.util.UUID.fromString(value.toString());
            case ENTITY_SELECTOR, DIMENSION -> value.toString();
        };
    }

    private static Vec3 parseVec3FromJson(JsonElement element) {
        var array = element.getAsJsonArray();
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }

    private static Vec3 parseVec3FromNBT(CompoundTag tag) {
        return new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    }
}
