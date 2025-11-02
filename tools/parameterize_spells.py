import ast
import json
import re
from collections import OrderedDict
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SPELLS_DIR = ROOT / "src" / "main" / "java" / "io" / "redspace" / "ironsspellbooks" / "spells"
RESOURCES_PATH = ROOT / "src" / "main" / "resources" / "data" / "irons_spellbooks"
DOC_PATH = ROOT / "docs" / "技能参数文档.md"

FIELDS = [
    ("baseManaCost", int),
    ("manaCostPerLevel", int),
    ("baseSpellPower", int),
    ("spellPowerPerLevel", int),
    ("castTime", int),
]

IMPORTS = [
    "import io.redspace.ironsspellbooks.api.spells.parameters.IParameterizedSpell;",
    "import io.redspace.ironsspellbooks.api.spells.parameters.ParameterType;",
    "import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterConfig;",
    "import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterLoader;",
    "import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameterSchema;",
    "import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;",
]

SKIP_FILES = {"NoneSpell.java"}

COOLDOWN_PATTERN = re.compile(r"\.setCooldownSeconds\(([^)]+)\)")
RESOURCE_PATTERN = re.compile(r"ResourceLocation\.fromNamespaceAndPath\(IronsSpellbooks\.MODID, \"([^\"]+)\"\)")
ASSIGN_PATTERN = re.compile(r"\s*this\.(baseManaCost|manaCostPerLevel|baseSpellPower|spellPowerPerLevel|castTime)\s*=\s*([^;]+);\s*")
CLASS_PATTERN = re.compile(r"public\s+class\s+(\w+)\s+extends\s+(Abstract(?:Eldritch)?Spell)")
ALLOWED_BASES = {"AbstractSpell", "AbstractEldritchSpell"}
DOC_FIELDS = [
    ("manaCost / baseManaCost", "baseManaCost", "INT", "基础魔力消耗"),
    ("manaCostPerLevel", "manaCostPerLevel", "INT", "每级魔力消耗增量"),
    ("power / baseSpellPower", "baseSpellPower", "INT", "基础技能威力"),
    ("levelScaling / spellPowerPerLevel", "spellPowerPerLevel", "INT", "每级技能威力增量"),
    ("castTime", "castTime", "INT", "施法时间 (tick)"),
    ("cooldown", "cooldown", "DOUBLE", "默认冷却时间 (秒)")
]


def safe_eval(expr: str):
    expr = expr.strip()
    if expr.endswith("f"):
        expr = expr[:-1]
    node = ast.parse(expr, mode="eval")
    allowed_nodes = (
        ast.Expression,
        ast.BinOp,
        ast.UnaryOp,
        ast.Num,
        ast.Load,
        ast.Add,
        ast.Sub,
        ast.Mult,
        ast.Div,
        ast.Mod,
        ast.Pow,
        ast.FloorDiv,
        ast.USub,
        ast.UAdd,
        ast.Constant,
        ast.Call,
        ast.Attribute,
        ast.Name,
    )
    for subnode in ast.walk(node):
        if not isinstance(subnode, allowed_nodes):
            raise ValueError(f"Unsupported expression: {expr}")
        if isinstance(subnode, ast.Call):
            raise ValueError(f"Function calls not allowed: {expr}")
        if isinstance(subnode, ast.Attribute):
            raise ValueError(f"Attributes not allowed: {expr}")
        if isinstance(subnode, ast.Name):
            raise ValueError(f"Names not allowed in expression: {expr}")
    return eval(compile(node, filename="<expr>", mode="eval"))


def insert_imports(content: str) -> str:
    existing_imports = set()
    for imp in IMPORTS:
        if imp in content:
            existing_imports.add(imp)
    if len(existing_imports) == len(IMPORTS):
        return content
    lines = content.splitlines()
    package_index = 0
    for idx, line in enumerate(lines):
        if line.startswith("import "):
            package_index = idx
            break
    else:
        package_index = 1
    insertion_index = package_index
    while insertion_index < len(lines) and lines[insertion_index].startswith("import "):
        insertion_index += 1
    new_imports = [imp for imp in IMPORTS if imp not in existing_imports]
    if not new_imports:
        return content
    updated_lines = lines[:insertion_index] + new_imports + lines[insertion_index:]
    return "\n".join(updated_lines)


def modify_class_declaration(content: str) -> str:
    match = CLASS_PATTERN.search(content)
    if not match:
        return content
    class_name = match.group(1)
    base_class = match.group(2)
    if base_class not in ALLOWED_BASES:
        return content
    start = match.start()
    brace_index = content.find("{", start)
    class_declaration = content[start:brace_index]
    if "implements IParameterizedSpell" in class_declaration:
        sanitized = re.sub(r"(implements IParameterizedSpell)(?:\s+implements IParameterizedSpell)+", r"\1", class_declaration)
        sanitized = sanitized.replace("\\1", "")
        return content[:start] + sanitized + content[brace_index:]
    original = match.group(0)
    replacement = f"public class {class_name} extends {base_class} implements IParameterizedSpell"
    return content.replace(original, replacement, 1)


def modify_constructor(content: str, class_name: str) -> str:
    pattern = re.compile(rf"(public\s+{class_name}\s*\(\)\s*{{)(.*?)(\n\s*}})", re.DOTALL)
    def replacer(match: re.Match) -> str:
        body = match.group(2)
        new_block = (
            "\n        SpellParameterConfig defaults = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);\n"
            "        if (SpellParameterLoader.hasConfig(getSpellId())) {\n"
            "            SpellParameterConfig parameters = SpellParameterLoader.resolve(getSpellId(), SpellParameters.empty(), defaults);\n"
            "            this.baseManaCost = parameters.baseManaCost();\n"
            "            this.manaCostPerLevel = parameters.manaCostPerLevel();\n"
            "            this.baseSpellPower = parameters.baseSpellPower();\n"
            "            this.spellPowerPerLevel = parameters.spellPowerPerLevel();\n"
            "            this.castTime = parameters.castTime();\n"
            "            this.defaultConfig.cooldownInSeconds = parameters.cooldownSeconds();\n"
            "        }\n"
        )
        if "SpellParameterLoader.resolve(getSpellId(), SpellParameters.empty(), defaults)" in body:
            return match.group(1) + body + match.group(3)
        marker = "SpellParameterConfig parameters = SpellParameterLoader.get(getSpellId());"
        if marker in body:
            start = body.index(marker)
            end = body.find("\n\n", start)
            if end == -1:
                end = len(body)
            else:
                end += 2
            rest = body[end:]
            if rest.startswith("        }\n\n"):
                rest = rest[len("        }\n"):]
            body = body[:start] + new_block + rest
        else:
            body = body + new_block
        return match.group(1) + body + match.group(3)

    return pattern.sub(replacer, content, count=1)


def remove_method(content: str, marker: str) -> str:
    index = content.find(marker)
    while index != -1:
        brace_index = content.find('{', index)
        if brace_index == -1:
            break
        depth = 1
        i = brace_index + 1
        while i < len(content) and depth > 0:
            if content[i] == '{':
                depth += 1
            elif content[i] == '}':
                depth -= 1
            i += 1
        content = content[:index] + content[i:]
        index = content.find(marker)
    return content


def ensure_methods(content: str, class_name: str) -> str:
    content = remove_method(content, "@Override\n    public SpellParameterSchema getParameterSchema(")
    content = remove_method(content, "@Override\n    public void onCastWithParameters(")
    content = re.sub(r"}\s*finally\s*\{.*?}\s*", "", content, flags=re.DOTALL)
    insertion_point = content.rfind('}')
    method_block = (
        "\n    @Override\n"
        "    public SpellParameterSchema getParameterSchema() {\n"
        "        SpellParameterConfig defaults = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);\n"
        "        SpellParameterConfig parameters = SpellParameterLoader.resolve(getSpellId(), SpellParameters.empty(), defaults);\n"
        "        return SpellParameterSchema.builder()\n"
        "                .optional(\"baseManaCost\", ParameterType.INT, parameters.baseManaCost(), \"基础魔力消耗\")\n"
        "                .alias(\"manaCost\", \"baseManaCost\")\n"
        "                .optional(\"manaCostPerLevel\", ParameterType.INT, parameters.manaCostPerLevel(), \"每级魔力增量\")\n"
        "                .optional(\"baseSpellPower\", ParameterType.INT, parameters.baseSpellPower(), \"基础技能威力\")\n"
        "                .alias(\"power\", \"baseSpellPower\")\n"
        "                .optional(\"spellPowerPerLevel\", ParameterType.INT, parameters.spellPowerPerLevel(), \"每级威力增量\")\n"
        "                .alias(\"levelScaling\", \"spellPowerPerLevel\")\n"
        "                .optional(\"castTime\", ParameterType.INT, parameters.castTime(), \"施法时间 (tick)\")\n"
        "                .optional(\"cooldown\", ParameterType.DOUBLE, parameters.cooldownSeconds(), \"默认冷却 (秒)\")\n"
        "                .build();\n"
        "    }\n\n"
        "    @Override\n"
        "    public void onCastWithParameters(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, SpellParameters parameters) {\n"
        "        SpellParameterConfig fallback = new SpellParameterConfig(this.baseManaCost, this.manaCostPerLevel, this.baseSpellPower, this.spellPowerPerLevel, this.castTime, this.defaultConfig.cooldownInSeconds);\n"
        "        SpellParameterConfig config = SpellParameterLoader.resolve(getSpellId(), parameters, fallback);\n"
        "        SpellParameterConfig previous = this.applyParameterOverrides(config);\n"
        "        try {\n"
        "            this.onCast(level, spellLevel, entity, castSource, playerMagicData);\n"
        "        } finally {\n"
        "            this.restoreParameters(previous);\n"
        "        }\n"
        "    }\n"
    )
    return content[:insertion_point] + method_block + "}\n"


def create_support_files():
    target_dir = ROOT / "src" / "main" / "java" / "io" / "redspace" / "ironsspellbooks" / "api" / "spells" / "parameters"
    target_dir.mkdir(parents=True, exist_ok=True)
    files = {
        "ParameterType.java": '''package io.redspace.ironsspellbooks.api.spells.parameters;

import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.function.Function;

public enum ParameterType {
    INT("整数", JsonElement::getAsInt, tag -> tag.getInt("value")),
    FLOAT("浮点数", JsonElement::getAsFloat, tag -> tag.getFloat("value")),
    DOUBLE("双精度", JsonElement::getAsDouble, tag -> tag.getDouble("value")),
    STRING("字符串", JsonElement::getAsString, tag -> tag.getString("value")),
    BOOLEAN("布尔值", JsonElement::getAsBoolean, tag -> tag.getBoolean("value")),
    VEC3("三维坐标", ParameterType::parseVec3FromJson, ParameterType::parseVec3FromNBT),
    UUID("实体UUID", json -> UUID.fromString(json.getAsString()), tag -> tag.getUUID("value")),
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

    private static Vec3 parseVec3FromJson(JsonElement element) {
        var array = element.getAsJsonArray();
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }

    private static Vec3 parseVec3FromNBT(CompoundTag tag) {
        return new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    }
}
''',
        "SpellParameters.java": '''package io.redspace.ironsspellbooks.api.spells.parameters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
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

    public static SpellParameters fromNBT(CompoundTag tag) {
        Map<String, Object> params = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            params.put(key, parseNbtValue(tag.getTagType(key), tag, key));
        }
        return new SpellParameters(params);
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
''',
        "SpellParameterSchema.java": '''package io.redspace.ironsspellbooks.api.spells.parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpellParameterSchema {
    private final Map<String, ParameterDefinition> definitions;

    private SpellParameterSchema(Map<String, ParameterDefinition> definitions) {
        this.definitions = definitions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, ParameterDefinition> getDefinitions() {
        return definitions;
    }

    public record ParameterDefinition(String name, ParameterType type, boolean required, Object defaultValue, String description) {
    }

    public static class Builder {
        private final Map<String, ParameterDefinition> definitions = new LinkedHashMap<>();

        public Builder required(String name, ParameterType type, String description) {
            definitions.put(name, new ParameterDefinition(name, type, true, null, description));
            return this;
        }

        public Builder optional(String name, ParameterType type, Object defaultValue, String description) {
            definitions.put(name, new ParameterDefinition(name, type, false, defaultValue, description));
            return this;
        }

        public SpellParameterSchema build() {
            return new SpellParameterSchema(Map.copyOf(definitions));
        }
    }
}
''',
        "IParameterizedSpell.java": '''package io.redspace.ironsspellbooks.api.spells.parameters;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface IParameterizedSpell {
    SpellParameterSchema getParameterSchema();

    void onCastWithParameters(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, SpellParameters parameters);
}
''',
        "SpellParameterConfig.java": '''package io.redspace.ironsspellbooks.api.spells.parameters;

public record SpellParameterConfig(int baseManaCost, int manaCostPerLevel, int baseSpellPower, int spellPowerPerLevel, int castTime, double cooldownSeconds) {
    public static final SpellParameterConfig DEFAULT = new SpellParameterConfig(0, 0, 0, 0, 0, 0d);

    public SpellParameterConfig withOverrides(SpellParameters parameters) {
        return new SpellParameterConfig(
                parameters.getInt("baseManaCost", baseManaCost),
                parameters.getInt("manaCostPerLevel", manaCostPerLevel),
                parameters.getInt("baseSpellPower", baseSpellPower),
                parameters.getInt("spellPowerPerLevel", spellPowerPerLevel),
                parameters.getInt("castTime", castTime),
                parameters.getDouble("cooldown", cooldownSeconds)
        );
    }
}
''',
        "SpellParameterLoader.java": '''package io.redspace.ironsspellbooks.api.spells.parameters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.redspace.ironsspellbooks.IronsSpellbooks;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SpellParameterLoader {
    private static final String RESOURCE_PATH = "data/" + IronsSpellbooks.MODID + "/spell_parameters.json";
    private static Map<String, SpellParameterConfig> CONFIGS;

    private SpellParameterLoader() {
    }

    public static SpellParameterConfig get(String spellId) {
        ensureLoaded();
        return CONFIGS.getOrDefault(spellId, SpellParameterConfig.DEFAULT);
    }

    public static SpellParameterConfig resolve(String spellId, SpellParameters parameters) {
        SpellParameterConfig base = get(spellId);
        return base.withOverrides(parameters);
    }

    private static void ensureLoaded() {
        if (CONFIGS != null) {
            return;
        }
        Map<String, SpellParameterConfig> map = new HashMap<>();
        try (InputStream stream = SpellParameterLoader.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            if (stream != null) {
                JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    int baseManaCost = obj.has("baseManaCost") ? obj.get("baseManaCost").getAsInt() : 0;
                    int manaCostPerLevel = obj.has("manaCostPerLevel") ? obj.get("manaCostPerLevel").getAsInt() : 0;
                    int baseSpellPower = obj.has("baseSpellPower") ? obj.get("baseSpellPower").getAsInt() : 0;
                    int spellPowerPerLevel = obj.has("spellPowerPerLevel") ? obj.get("spellPowerPerLevel").getAsInt() : 0;
                    int castTime = obj.has("castTime") ? obj.get("castTime").getAsInt() : 0;
                    double cooldown = obj.has("cooldown") ? obj.get("cooldown").getAsDouble() : 0d;
                    map.put(entry.getKey(), new SpellParameterConfig(baseManaCost, manaCostPerLevel, baseSpellPower, spellPowerPerLevel, castTime, cooldown));
                }
            } else {
                IronsSpellbooks.LOGGER.warn("spell_parameters.json 未找到，使用默认参数");
            }
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("加载 spell_parameters.json 失败", e);
        }
        CONFIGS = Collections.unmodifiableMap(map);
    }
}
'''
    }
    for filename, content in files.items():
        target_file = target_dir / filename
        if not target_file.exists():
            target_file.write_text(content, encoding='utf-8')


def generate_json(spell_records):
    RESOURCES_PATH.mkdir(parents=True, exist_ok=True)
    data = OrderedDict()
    for record in sorted(spell_records, key=lambda x: x['spell_id']):
        data[record['spell_id']] = OrderedDict([
            ("class", record['class_name']),
            ("baseManaCost", record['baseManaCost']),
            ("manaCostPerLevel", record['manaCostPerLevel']),
            ("baseSpellPower", record['baseSpellPower']),
            ("spellPowerPerLevel", record['spellPowerPerLevel']),
            ("castTime", record['castTime']),
            ("cooldown", record['cooldown'])
        ])
    (RESOURCES_PATH / "spell_parameters.json").write_text(json.dumps(data, indent=2, ensure_ascii=False), encoding='utf-8')


def generate_doc(spell_records):
    header = [
        "# Iron's Spells 'n Spellbooks - 技能参数文档",
        "",
        f"*最后更新：{datetime.now().strftime('%Y-%m-%d')}*",
        "",
        "本文件由参数化改造脚本自动生成，列出全部技能的核心参数，可通过 JSON 参数覆盖。",
        "",
        "---",
        "",
    ]
    sections = []
    for record in sorted(spell_records, key=lambda x: x['spell_id']):
        sections.extend([
            f"### {record['class_name']} ({record['spell_id']})",
            "",
            "| 参数名 | 类型 | 默认值 | 说明 |",
            "|--------|------|--------|------|",
        ])
        for display, key, type_name, description in DOC_FIELDS:
            sections.append(f"| {display} | {type_name} | {record[key]} | {description} |")
        sections.extend([
            "",
            "**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。",
            "",
        ])
    DOC_PATH.write_text("\n".join(header + sections), encoding='utf-8')


def main():
    spell_records = []
    for path in sorted(SPELLS_DIR.rglob("*Spell.java")):
        if path.name in SKIP_FILES:
            continue
        content = path.read_text()
        class_match = CLASS_PATTERN.search(content)
        if not class_match:
            continue
        class_name = class_match.group(1)
        base_class = class_match.group(2)
        if base_class not in ALLOWED_BASES:
            continue
        resource_match = RESOURCE_PATTERN.search(content)
        if not resource_match:
            continue
        spell_id_path = resource_match.group(1)
        spell_id = f"irons_spellbooks:{spell_id_path}"
        cooldown_match = COOLDOWN_PATTERN.search(content)
        cooldown_value = 0
        if cooldown_match:
            cooldown_value = safe_eval(cooldown_match.group(1))
        field_values = {field: 0 for field, _ in FIELDS}
        for assign_match in ASSIGN_PATTERN.finditer(content):
            field_name = assign_match.group(1)
            value_expr = assign_match.group(2)
            if any(c.isalpha() for c in value_expr):
                continue
            value = safe_eval(value_expr)
            field_values[field_name] = value
        modified = content
        modified_after_imports = insert_imports(modified)
        modified_after_class = modify_class_declaration(modified_after_imports)
        modified_after_constructor = modify_constructor(modified_after_class, class_name)
        modified_after_methods = ensure_methods(modified_after_constructor, class_name)
        if modified_after_methods != content:
            print(f"Updated {path.relative_to(ROOT)}")
        path.write_text(modified_after_methods, encoding='utf-8')
        spell_records.append({
            'spell_id': spell_id,
            'class_name': class_name,
            'baseManaCost': int(field_values['baseManaCost']),
            'manaCostPerLevel': int(field_values['manaCostPerLevel']),
            'baseSpellPower': int(field_values['baseSpellPower']),
            'spellPowerPerLevel': int(field_values['spellPowerPerLevel']),
            'castTime': int(field_values['castTime']),
            'cooldown': float(cooldown_value),
        })
    create_support_files()
    generate_json(spell_records)
    generate_doc(spell_records)


if __name__ == "__main__":
    main()
