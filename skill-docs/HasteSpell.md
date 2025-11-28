### HasteSpell (irons_spellbooks:haste)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 30 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 5 | 每级技能威力增量 |
| castTime | INT | 30 | 施法时间 (tick) |
| cooldown | DOUBLE | 80.0 | 默认冷却时间 (秒) |
| hasteRadius | FLOAT | 3.0 | 生效半径 |
| maxTargets | INT | 5 | 最大友方数量 |
| durationPerPower | FLOAT | 20.0 | 持续时间系数 (tick/威力) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
