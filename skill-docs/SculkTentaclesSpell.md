### SculkTentaclesSpell (irons_spellbooks:sculk_tentacles)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 150 | 基础魔力消耗 |
| manaCostPerLevel | INT | 50 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 3 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 30.0 | 默认冷却时间 (秒) |
| targetRange | DOUBLE | 48.0 | 目标选取距离 |
| damageMultiplier | DOUBLE | 1.0 | 伤害倍率 (基于技能威力) |
| baseRings | INT | 1 | 基础环数 |
| ringsPerLevel | INT | 1 | 每级增加的环数 |
| tentaclesBase | INT | 2 | 第一环触手数量 |
| tentaclesPerRingIncrement | INT | 2 | 每环额外触手数 |
| ringSpacing | DOUBLE | 1.3 | 环间距 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
