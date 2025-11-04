package io.redspace.ironsspellbooks.api.spells.parameters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.redspace.ironsspellbooks.IronsSpellbooks;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public final class SpellParameterLoader {
    private enum ParameterKey {
        BASE_MANA_COST,
        MANA_COST_PER_LEVEL,
        BASE_SPELL_POWER,
        SPELL_POWER_PER_LEVEL,
        CAST_TIME,
        COOLDOWN
    }

    private record LoadedConfig(SpellParameterConfig config, EnumSet<ParameterKey> presentKeys) {
    }

    private static final String RESOURCE_PATH = "data/" + IronsSpellbooks.MODID + "/spell_parameters.json";
    private static Map<String, LoadedConfig> CONFIGS;

    private SpellParameterLoader() {
    }

    public static SpellParameterConfig get(String spellId) {
        ensureLoaded();
        LoadedConfig loaded = CONFIGS.get(spellId);
        return loaded != null ? loaded.config() : SpellParameterConfig.DEFAULT;
    }

    public static boolean hasConfig(String spellId) {
        ensureLoaded();
        return CONFIGS.containsKey(spellId);
    }

    public static SpellParameterConfig resolve(String spellId, SpellParameters parameters, SpellParameterConfig fallback) {
        ensureLoaded();
        LoadedConfig loaded = CONFIGS.get(spellId);
        SpellParameterConfig base = loaded != null ? loaded.config() : SpellParameterConfig.DEFAULT;
        EnumSet<ParameterKey> present = loaded != null ? loaded.presentKeys() : EnumSet.noneOf(ParameterKey.class);
        SpellParameterConfig effective = mergeWithFallback(base, fallback, present);
        return effective.withOverrides(parameters);
    }

    private static void ensureLoaded() {
        if (CONFIGS != null) {
            return;
        }
        Map<String, LoadedConfig> map = new HashMap<>();
        try (InputStream stream = SpellParameterLoader.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            if (stream != null) {
                JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    EnumSet<ParameterKey> present = EnumSet.noneOf(ParameterKey.class);
                    int baseManaCost = 0;
                    if (obj.has("baseManaCost")) {
                        baseManaCost = obj.get("baseManaCost").getAsInt();
                        present.add(ParameterKey.BASE_MANA_COST);
                    }
                    int manaCostPerLevel = 0;
                    if (obj.has("manaCostPerLevel")) {
                        manaCostPerLevel = obj.get("manaCostPerLevel").getAsInt();
                        present.add(ParameterKey.MANA_COST_PER_LEVEL);
                    }
                    int baseSpellPower = 0;
                    if (obj.has("baseSpellPower")) {
                        baseSpellPower = obj.get("baseSpellPower").getAsInt();
                        present.add(ParameterKey.BASE_SPELL_POWER);
                    }
                    int spellPowerPerLevel = 0;
                    if (obj.has("spellPowerPerLevel")) {
                        spellPowerPerLevel = obj.get("spellPowerPerLevel").getAsInt();
                        present.add(ParameterKey.SPELL_POWER_PER_LEVEL);
                    }
                    int castTime = 0;
                    if (obj.has("castTime")) {
                        castTime = obj.get("castTime").getAsInt();
                        present.add(ParameterKey.CAST_TIME);
                    }
                    double cooldown = 0d;
                    if (obj.has("cooldown")) {
                        cooldown = obj.get("cooldown").getAsDouble();
                        present.add(ParameterKey.COOLDOWN);
                    }
                    SpellParameterConfig config = new SpellParameterConfig(baseManaCost, manaCostPerLevel, baseSpellPower, spellPowerPerLevel, castTime, cooldown);
                    map.put(entry.getKey(), new LoadedConfig(config, present));
                }
            } else {
                IronsSpellbooks.LOGGER.warn("spell_parameters.json 未找到，使用默认参数");
            }
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("加载 spell_parameters.json 失败", e);
        }
        CONFIGS = Collections.unmodifiableMap(map);
    }

    private static SpellParameterConfig mergeWithFallback(SpellParameterConfig base,
                                                          SpellParameterConfig fallback,
                                                          EnumSet<ParameterKey> present) {
        if (fallback == null) {
            return base;
        }

        int baseManaCost = present.contains(ParameterKey.BASE_MANA_COST) ? base.baseManaCost() : fallback.baseManaCost();
        int manaCostPerLevel = present.contains(ParameterKey.MANA_COST_PER_LEVEL) ? base.manaCostPerLevel() : fallback.manaCostPerLevel();
        int baseSpellPower = present.contains(ParameterKey.BASE_SPELL_POWER) ? base.baseSpellPower() : fallback.baseSpellPower();
        int spellPowerPerLevel = present.contains(ParameterKey.SPELL_POWER_PER_LEVEL) ? base.spellPowerPerLevel() : fallback.spellPowerPerLevel();
        int castTime = present.contains(ParameterKey.CAST_TIME) ? base.castTime() : fallback.castTime();
        double cooldown = present.contains(ParameterKey.COOLDOWN) ? base.cooldownSeconds() : fallback.cooldownSeconds();

        return new SpellParameterConfig(baseManaCost, manaCostPerLevel, baseSpellPower, spellPowerPerLevel, castTime, cooldown);
    }
}
