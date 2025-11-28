### SonicBoomSpell (irons_spellbooks:sonic_boom)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 110 | 基础魔力消耗 |
| manaCostPerLevel | INT | 50 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 20 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 8 | 每级技能威力增量 |
| castTime | INT | 30 | 施法时间 (tick) |
| cooldown | DOUBLE | 25.0 | 默认冷却时间 (秒) |
| baseRange | DOUBLE | 15.0 | 基础射程 |
| rangePerLevel | DOUBLE | 5.0 | 每级射程增量 |
| beamWidth | DOUBLE | 0.4 | 伤害判定宽度 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
