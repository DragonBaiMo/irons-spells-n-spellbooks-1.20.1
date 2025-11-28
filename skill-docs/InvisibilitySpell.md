### InvisibilitySpell (irons_spellbooks:invisibility)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 35 | 基础魔力消耗 |
| manaCostPerLevel | INT | 8 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 10 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 5 | 每级技能威力增量 |
| castTime | INT | 40 | 施法时间 (tick) |
| cooldown | DOUBLE | 45.0 | 默认冷却时间 (秒) |
| durationPerPower | FLOAT | 20.0 | 持续时间系数 (tick/威力) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
