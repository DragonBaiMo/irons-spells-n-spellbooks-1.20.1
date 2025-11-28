### HeatSurgeSpell (irons_spellbooks:heat_surge)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 8 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 10 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 2 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 45.0 | 默认冷却时间 (秒) |
| radiusBase | FLOAT | 6.0 | 爆发半径基础值 |
| radiusPerLevel | FLOAT | 0.5 | 爆发半径每级增量 |
| rendBase | INT | 1 | 撕裂等级基础值 |
| rendPerLevel | INT | 1 | 撕裂等级每级增量 |
| durationPerPower | FLOAT | 20.0 | 持续时间系数 (tick/威力) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
