# /iss cast 指令使用指南

## 修改说明

已将原本的 `/cast` 命令修改为 `/iss cast`，所有参数保持不变。

## 基础用法

### 1. 基础施法（不消耗蓝）
```
/iss cast @s fireball
/iss cast @s fireball 5
```
- 默认情况下，命令施法 **不消耗蓝量**
- 默认情况下，命令施法 **不触发冷却**
- 默认情况下，命令施法 **绕过前置条件**（如蓝量检查、冷却检查等）

### 2. 指定目标施法（解决瞄准问题）

对于需要目标的技能，可以通过三种方式显式指定目标：

#### 方式1：使用实体选择器
```
/iss cast @s poison_splash 5 @e[type=zombie,limit=1]
```

支持所有实体选择器：
```
/iss cast @s polymorph 10 @e[type=iron_golem,distance=..5,limit=1]
/iss cast @s root 8 @p[distance=..10]
```

#### 方式2：使用UUID（精确指定）
```
/iss cast @s lightning_lance 10 550e8400-e29b-41d4-a716-446655440000
```

获取生物UUID的方法：
- 使用 `/data get entity <目标>` 查看实体的UUID
- 或者使用F3调试屏幕查看

#### 方式3：使用射线检测（不推荐）
如果不指定目标，系统会使用射线检测寻找目标。但由于命令在服务端执行，无法准确获取客户端鼠标指向，射线检测可能会失败。

```
/iss cast @s poison_splash 5
```

**为什么推荐显式指定目标？**
- 命令在服务端执行，无法实时获取客户端鼠标指向
- 射线检测可能因玩家朝向不准确而失败
- 显式指定目标可以100%确保技能命中

### 3. 让命令施法消耗蓝量

使用 JSON 参数控制施法选项：

```
/iss cast @s fireball 5 {"consumeMana": true}
```

### 4. 完整控制选项

JSON 参数支持以下字段：

```json
{
  "consumeMana": true,        // 是否消耗蓝量（默认 false）
  "triggerCooldown": true,    // 是否触发冷却（默认 false）
  "playEffects": true,        // 是否播放特效（默认 true）
  "bypassConditions": false,  // 是否绕过前置条件（默认 true）
  "showCastBar": true         // 是否显示施法条（默认 false）
}
```

### 6. 结合目标参数和 JSON 选项

```
/iss cast @s lightning_lance 10 @e[type=!player,distance=..30,limit=1,sort=nearest] {"consumeMana": true, "triggerCooldown": true}

/iss cast @s magic_missile 8 6ec9c4c9-fc5c-4f42-b2c9-6b745d4764e2 {"consumeMana": true, "damage": 15.0}
```

### 4. 实用示例

#### 示例 1：完全模拟玩家正常施法
```
/iss cast @s fireball 5 {"consumeMana": true, "triggerCooldown": true, "bypassConditions": false, "showCastBar": true}
```
- 消耗蓝量
- 触发冷却
- 检查前置条件（蓝量、冷却等）
- 显示施法条

#### 示例 2：消耗蓝但不触发冷却（适合连续施法测试）
```
/iss cast @s fireball 5 {"consumeMana": true}
```

#### 示例 3：管理员强制施法（绕过一切限制）
```
/iss cast @s fireball 10
```
- 不消耗蓝
- 不触发冷却
- 绕过所有前置条件
- 即使等级超过上限也可施放

#### 示例 4：多目标施法
```
/iss cast @a[distance=..10] heal 3 {"consumeMana": true}
```
- 对半径10格内所有玩家施放治疗术
- 每个玩家都消耗自己的蓝量

## 耗蓝计算公式

技能的蓝耗按以下公式计算：

```
实际蓝耗 = (基础蓝耗 + 每级蓝耗 × (等级 - 1)) × 配置倍率
```

- **基础蓝耗**：技能的 `baseManaCost` 属性
- **每级蓝耗**：技能的 `manaCostPerLevel` 属性
- **配置倍率**：服务器配置文件中的 `manaMultiplier`（默认为 1.0）

### 例子：火球术

假设火球术配置：
- 基础蓝耗 = 20
- 每级蓝耗 = 5
- 配置倍率 = 1.0

那么：
- 1级火球术：(20 + 5 × 0) × 1.0 = 20 蓝
- 3级火球术：(20 + 5 × 2) × 1.0 = 30 蓝
- 5级火球术：(20 + 5 × 4) × 1.0 = 40 蓝

## 特殊说明

### 1. 重施法（Recast）技能
如果技能支持重施法（如召唤类技能），第一次施放消耗蓝，后续重施法 **不消耗蓝**。

### 2. 参数化施法
某些技能支持更高级的参数化施法，可以自定义技能行为：

```
/iss cast @s magic_missile 5 {
  "consumeMana": true,
  "damage": 15.0,
  "projectileCount": 10,
  "burstCount": 3
}
```

具体支持的参数取决于技能实现。

### 3. 非玩家实体
- 普通施法：支持 `ServerPlayer`, `IMagicEntity`, `LivingEntity`
- 参数化施法：**仅支持玩家**（`ServerPlayer`）

## CastSource 类型说明

不同的施法来源有不同的默认行为：

| CastSource | 消耗蓝 | 触发冷却 | 绕过条件 | 显示施法条 |
|------------|--------|----------|----------|------------|
| SPELLBOOK  | ✓      | ✓        | ✗        | ✓          |
| SCROLL     | ✗      | ✗        | ✗        | ✗          |
| SWORD      | ✓      | ✓        | ✗        | ✓          |
| **COMMAND**| **✗**  | **✗**    | **✓**    | **✗**      |
| MOB        | ✗      | ✗        | ✗        | ✗          |

命令施法默认 **不消耗蓝**，需要通过 JSON 参数手动开启。

## 常见问题

**Q: 为什么命令施法不消耗蓝？**  
A: 这是设计行为。命令施法用于管理员测试和调试，默认不消耗资源。需要时可通过 `{"consumeMana": true}` 开启。

**Q: 如何检查玩家当前蓝量？**  
A: 使用 `/iss mana` 命令（如果已实现）。

**Q: 可以自定义技能的蓝耗吗？**  
A: 参数化施法支持覆盖 `baseManaCost` 和 `manaCostPerLevel`：
```
/iss cast @s fireball 5 {"baseManaCost": 10, "manaCostPerLevel": 2, "consumeMana": true}
```

**Q: JSON 参数写错了会怎样？**  
A: 会收到错误提示："JSON 解析失败: ..."，技能不会施放。

## 权限要求

使用 `/iss cast` 命令需要 **权限等级 2**（即 OP 权限）。
