package io.redspace.ironsspellbooks.api.spells.parameters;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpellParameterSchema {
    private static final Map<String, ParameterDefinition> GLOBAL_DEFINITIONS;

    static {
        Builder builder = new Builder();
        builder.optional("consumeMana", ParameterType.BOOLEAN, false, "是否消耗法力");
        builder.optional("triggerCooldown", ParameterType.BOOLEAN, true, "是否触发冷却");
        builder.optional("playEffects", ParameterType.BOOLEAN, true, "是否播放特效");
        builder.optional("bypassConditions", ParameterType.BOOLEAN, false, "是否忽略前置条件");
        builder.optional("showCastBar", ParameterType.BOOLEAN, false, "是否显示施法条");
        GLOBAL_DEFINITIONS = Collections.unmodifiableMap(builder.definitions);
    }

    private final Map<String, ParameterDefinition> definitions;
    private final Map<String, String> aliasToPrimary;

    private SpellParameterSchema(Map<String, ParameterDefinition> definitions, Map<String, String> aliasToPrimary) {
        this.definitions = definitions;
        this.aliasToPrimary = aliasToPrimary;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, ParameterDefinition> getDefinitions() {
        return definitions;
    }

    public ParameterDefinition getDefinition(String key) {
        return definitions.get(key);
    }

    public String resolveKey(String key) {
        if (definitions.containsKey(key) || GLOBAL_DEFINITIONS.containsKey(key)) {
            return key;
        }
        return aliasToPrimary.get(key);
    }

    public ValidationResult validate(SpellParameters parameters) {
        Map<String, Object> normalizedValues = new LinkedHashMap<>();
        List<Component> errors = new ArrayList<>();

        for (Map.Entry<String, Object> entry : parameters.asMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String canonical = resolveKey(key);
            if (canonical == null) {
                errors.add(Component.literal("未知参数: " + key).withStyle(ChatFormatting.RED));
                continue;
            }

            ParameterDefinition definition = definitions.getOrDefault(canonical, GLOBAL_DEFINITIONS.get(canonical));
            if (definition == null) {
                errors.add(Component.literal("未定义的参数: " + canonical).withStyle(ChatFormatting.RED));
                continue;
            }

            if (!definition.type().isValueValid(value)) {
                errors.add(Component.literal("参数 '" + key + "' 的类型不匹配，期望 " + definition.type().getDisplayName())
                        .withStyle(ChatFormatting.RED));
                continue;
            }

            Object normalized = definition.type().normalize(value);
            if (normalized == null) {
                errors.add(Component.literal("参数 '" + key + "' 无法转换为有效值").withStyle(ChatFormatting.RED));
                continue;
            }

            if (normalizedValues.containsKey(canonical)) {
                errors.add(Component.literal("参数 '" + canonical + "' 被重复定义").withStyle(ChatFormatting.RED));
                continue;
            }

            normalizedValues.put(canonical, normalized);
        }

        for (ParameterDefinition definition : definitions.values()) {
            if (definition.required() && !normalizedValues.containsKey(definition.name())) {
                errors.add(Component.literal("缺少必填参数: " + definition.name()).withStyle(ChatFormatting.RED));
            } else if (!normalizedValues.containsKey(definition.name()) && definition.defaultValue() != null) {
                normalizedValues.put(definition.name(), definition.defaultValue());
            }
        }

        if (!errors.isEmpty()) {
            var message = Component.literal("");
            for (Component error : errors) {
                message.append(error);
                message.append(Component.literal("\n"));
            }
            return ValidationResult.failure(message);
        }

        return ValidationResult.success(SpellParameters.fromMap(normalizedValues));
    }

    public record ParameterDefinition(String name,
                                      ParameterType type,
                                      boolean required,
                                      Object defaultValue,
                                      String description,
                                      List<String> aliases) {
    }

    public static class Builder {
        private final Map<String, ParameterDefinition> definitions = new LinkedHashMap<>();
        private final Map<String, String> aliasToPrimary = new LinkedHashMap<>();
        private final Map<String, List<String>> aliasBuckets = new LinkedHashMap<>();

        public Builder required(String name, ParameterType type, String description) {
            addDefinition(name, type, true, null, description);
            return this;
        }

        public Builder optional(String name, ParameterType type, Object defaultValue, String description) {
            addDefinition(name, type, false, defaultValue, description);
            return this;
        }

        public Builder alias(String alias, String primary) {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(alias, "alias");
            if (!definitions.containsKey(primary) && !GLOBAL_DEFINITIONS.containsKey(primary)) {
                throw new IllegalArgumentException("别名指向的参数不存在: " + primary);
            }
            if (definitions.containsKey(alias) || GLOBAL_DEFINITIONS.containsKey(alias) || aliasToPrimary.containsKey(alias)) {
                throw new IllegalArgumentException("别名重复定义: " + alias);
            }
            aliasToPrimary.put(alias, primary);
            aliasBuckets.computeIfAbsent(primary, key -> new ArrayList<>()).add(alias);
            return this;
        }

        private void addDefinition(String name, ParameterType type, boolean required, Object defaultValue, String description) {
            definitions.put(name, new ParameterDefinition(name, type, required, defaultValue, description, ImmutableList.of()));
        }

        public SpellParameterSchema build() {
            Map<String, ParameterDefinition> builtDefinitions = new LinkedHashMap<>();
            for (Map.Entry<String, ParameterDefinition> entry : definitions.entrySet()) {
                List<String> aliases = aliasBuckets.getOrDefault(entry.getKey(), List.of());
                builtDefinitions.put(entry.getKey(), new ParameterDefinition(
                        entry.getValue().name(),
                        entry.getValue().type(),
                        entry.getValue().required(),
                        entry.getValue().defaultValue(),
                        entry.getValue().description(),
                        ImmutableList.copyOf(aliases)
                ));
            }

            Map<String, String> aliasMapping = new LinkedHashMap<>(aliasToPrimary);
            return new SpellParameterSchema(Collections.unmodifiableMap(builtDefinitions), Collections.unmodifiableMap(aliasMapping));
        }
    }

    public record ValidationResult(boolean success, Component message, SpellParameters normalized) {
        public static ValidationResult success(SpellParameters normalized) {
            return new ValidationResult(true, Component.literal("参数校验通过"), normalized);
        }

        public static ValidationResult failure(Component message) {
            return new ValidationResult(false, message, SpellParameters.empty());
        }

        public Component toComponent() {
            return message;
        }
    }
}
