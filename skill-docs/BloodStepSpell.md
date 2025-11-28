### BloodStepSpell (irons_spellbooks:blood_step)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 30 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 12 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 4 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 5.0 | 默认冷却时间 (秒) |
| invisDurationTicks | INT | 100 | 隐身持续时间 (tick) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
