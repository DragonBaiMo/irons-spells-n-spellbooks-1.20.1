### SummonPolarBearSpell (irons_spellbooks:summon_polar_bear)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 50 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 4 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 180.0 | 默认冷却时间 (秒) |
| summonDurationTicks | INT | 12000 | 召唤物持续时间 (tick) |
| bearBaseHealth | DOUBLE | 20.0 | 基础生命值 |
| bearHealthPerLevel | DOUBLE | 4.0 | 每级生命增量 |
| bearDamageMultiplier | DOUBLE | 1.0 | 伤害倍率 (基于技能威力) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
