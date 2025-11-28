### FrostwaveSpell (irons_spellbooks:frostwave)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 10 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 3 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 45.0 | 默认冷却时间 (秒) |
| radiusBase | FLOAT | 6.0 | 基础半径 |
| radiusPerLevel | FLOAT | 0.75 | 每级增加半径 |
| durationPerPower | FLOAT | 20.0 | 每点威力增加持续 (tick) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
