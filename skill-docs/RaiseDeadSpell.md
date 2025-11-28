### RaiseDeadSpell (irons_spellbooks:raise_dead)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 10 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 3 | 每级技能威力增量 |
| castTime | INT | 30 | 施法时间 (tick) |
| cooldown | DOUBLE | 150.0 | 默认冷却时间 (秒) |
| summonDurationTicks | INT | 12000 | 亡灵持续时间 (tick) |
| baseRadius | DOUBLE | 1.5 | 召唤基础环半径 |
| radiusPerLevel | DOUBLE | 0.185 | 每级半径增量 |
| skeletonChance | DOUBLE | 0.3 | 生成骷髅概率 (0-1) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
