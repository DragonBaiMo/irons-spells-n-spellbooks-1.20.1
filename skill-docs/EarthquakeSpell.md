### EarthquakeSpell (irons_spellbooks:earthquake)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 40 | 施法时间 (tick) |
| cooldown | DOUBLE | 16.0 | 默认冷却时间 (秒) |
| durationTicks | INT | 240 | 持续时间 (tick) |
| radiusBase | FLOAT | 4.0 | 基础半径 |
| radiusScale | FLOAT | 4.0 | 半径系数 |
| damageScale | FLOAT | 0.25 | 伤害系数 |
| slownessOffset | INT | 2 | 减速等级扣减 |
| slownessScale | FLOAT | 1.0 | 减速等级系数 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
