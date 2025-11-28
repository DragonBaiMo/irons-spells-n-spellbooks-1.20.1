### FrostbiteSpell (irons_spellbooks:frostbite)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 100 | 基础魔力消耗 |
| manaCostPerLevel | INT | 50 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 75 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 15 | 每级技能威力增量 |
| castTime | INT | 40 | 施法时间 (tick) |
| cooldown | DOUBLE | 0.0 | 默认冷却时间 (秒) |
| percentDamageScale | FLOAT | 0.01 | 冻结伤害百分比系数 |
| icicleShardCount | INT | 8 | 碎冰弹数量 |
| shardDamageSplit | FLOAT | 1.0 | 碎冰伤害分摊比例 |
| castRange | FLOAT | 48.0 | 施法距离 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
