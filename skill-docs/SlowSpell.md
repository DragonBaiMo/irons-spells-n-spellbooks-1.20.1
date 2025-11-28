### SlowSpell (irons_spellbooks:slow)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 20 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 4 | 每级技能威力增量 |
| castTime | INT | 30 | 施法时间 (tick) |
| cooldown | DOUBLE | 80.0 | 默认冷却时间 (秒) |
| targetRange | DOUBLE | 32.0 | 目标选取距离 |
| radius | DOUBLE | 3.0 | 减速范围半径 |
| maxTargets | INT | 5 | 最多命中目标数 |
| durationPerPower | INT | 20 | 每点威力对应的持续时间 (tick) |
| baseAmplifier | INT | 0 | 基础减速等级 |
| amplifierPerLevel | INT | 1 | 每级增加的减速等级 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
