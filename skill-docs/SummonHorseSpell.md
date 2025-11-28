### SummonHorseSpell (irons_spellbooks:summon_horse)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 2 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 2 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 20.0 | 默认冷却时间 (秒) |
| summonDurationTicks | INT | 12000 | 召唤物持续时间 (tick) |
| speedMin | DOUBLE | 0.2 | 最小移动速度 |
| speedMax | DOUBLE | 0.45 | 最大移动速度 |
| jumpMin | DOUBLE | 0.6 | 最小跳跃力 |
| jumpMax | DOUBLE | 1.0 | 最大跳跃力 |
| healthMin | DOUBLE | 10.0 | 最小生命值 |
| healthMax | DOUBLE | 40.0 | 最大生命值 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
