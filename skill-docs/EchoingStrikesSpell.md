### EchoingStrikesSpell (irons_spellbooks:echoing_strikes)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 20 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 5 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 60.0 | 默认冷却时间 (秒) |
| baseAmplifier | INT | 4 | 基础增益等级 |
| amplifierPerLevel | FLOAT | 1.0 | 每级增益系数 |
| radius | FLOAT | 3.0 | 作用半径 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
