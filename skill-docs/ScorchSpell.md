### ScorchSpell (irons_spellbooks:scorch)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 12.0 | 默认冷却时间 (秒) |
| targetRange | DOUBLE | 32.0 | 目标选取最大距离 |
| radius | DOUBLE | 2.5 | 爆炸/火场半径 |
| damageMultiplier | DOUBLE | 1.0 | 伤害倍率 (基于技能威力) |
| fireFieldDurationTicks | INT | 200 | 火场持续时间 (tick) |
| fireFieldDamageMultiplier | DOUBLE | 0.1 | 火场伤害倍率 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
