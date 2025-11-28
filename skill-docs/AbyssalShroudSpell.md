### AbyssalShroudSpell (irons_spellbooks:abyssal_shroud)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 300 | 基础魔力消耗 |
| manaCostPerLevel | INT | 20 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 6 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 6 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 200.0 | 默认冷却时间 (秒) |
| durationSecondsPerPower | DOUBLE | 1.0 | 每点威力对应的持续时间 (秒) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
