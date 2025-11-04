package io.redspace.ironsspellbooks.api.spells.parameters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpellParameters {
    private final Map<String, Object> parameters;

    public SpellParameters() {
        this.parameters = new HashMap<>();
    }

    private SpellParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    private SpellParameters(Map<String, Object> parameters, boolean copy) {
        this.parameters = copy ? new HashMap<>(parameters) : parameters;
    }

    public static SpellParameters fromJson(String jsonString) {
        Map<String, Object> params = new HashMap<>();
        if (jsonString == null || jsonString.isEmpty()) {
            return new SpellParameters(params);
        }
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            params.put(entry.getKey(), parseJsonValue(entry.getValue()));
        }
        return new SpellParameters(params);
    }

    public static SpellParameters fromMap(Map<String, Object> values) {
        return new SpellParameters(values, true);
    }

    public static SpellParameters fromNBT(CompoundTag tag) {
        Map<String, Object> params = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            params.put(key, parseNbtValue(tag.getTagType(key), tag, key));
        }
        return new SpellParameters(params);
    }

    public static SpellParameters empty() {
        return new SpellParameters();
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer i) {
                tag.putInt(entry.getKey(), i);
            } else if (value instanceof Float f) {
                tag.putFloat(entry.getKey(), f);
            } else if (value instanceof Double d) {
                tag.putDouble(entry.getKey(), d);
            } else if (value instanceof String s) {
                tag.putString(entry.getKey(), s);
            } else if (value instanceof Boolean b) {
                tag.putBoolean(entry.getKey(), b);
            } else if (value instanceof Vec3 vec) {
                CompoundTag vecTag = new CompoundTag();
                vecTag.putDouble("x", vec.x);
                vecTag.putDouble("y", vec.y);
                vecTag.putDouble("z", vec.z);
                tag.put(entry.getKey(), vecTag);
            } else if (value instanceof UUID uuid) {
                CompoundTag uuidTag = new CompoundTag();
                uuidTag.putUUID("value", uuid);
                tag.put(entry.getKey(), uuidTag);
            }
        }
        return tag;
    }

    public boolean has(String key) {
        return parameters.containsKey(key);
    }

    public int getInt(String key, int defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return defaultValue;
    }

    public float getFloat(String key, float defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number number) {
            return number.floatValue();
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof String str) {
            return str;
        }
        return defaultValue;
    }

    public Vec3 getVec3(String key, @Nullable Vec3 defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Vec3 vec3) {
            return vec3;
        }
        return defaultValue;
    }

    @Nullable
    public UUID getUUID(String key) {
        Object value = parameters.get(key);
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public Map<String, Object> asMap() {
        return parameters;
    }

    private static Object parseJsonValue(JsonElement value) {
        if (value.isJsonPrimitive()) {
            var primitive = value.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                if (primitive.getAsString().contains(".")) {
                    return primitive.getAsDouble();
                } else {
                    return primitive.getAsInt();
                }
            } else if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isString()) {
                String raw = primitive.getAsString();
                try {
                    return UUID.fromString(raw);
                } catch (IllegalArgumentException ignored) {
                    return raw;
                }
            }
        } else if (value.isJsonArray() && value.getAsJsonArray().size() == 3) {
            var array = value.getAsJsonArray();
            return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
        }
        return null;
    }

    private static Object parseNbtValue(byte type, CompoundTag tag, String key) {
        return switch (type) {
            case 3 -> tag.getInt(key);
            case 5 -> tag.getFloat(key);
            case 6 -> tag.getDouble(key);
            case 8 -> tag.getString(key);
            case 1 -> tag.getBoolean(key);
            case 10 -> {
                CompoundTag nested = tag.getCompound(key);
                if (nested.contains("x") && nested.contains("y") && nested.contains("z")) {
                    yield new Vec3(nested.getDouble("x"), nested.getDouble("y"), nested.getDouble("z"));
                }
                if (nested.hasUUID("value")) {
                    yield nested.getUUID("value");
                }
                yield nested;
            }
            default -> null;
        };
    }
}
