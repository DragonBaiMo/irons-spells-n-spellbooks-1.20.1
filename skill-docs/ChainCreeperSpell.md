### ChainCreeperSpell (irons_spellbooks:chain_creeper)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 40 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 5 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 0 | 每级技能威力增量 |
| castTime | INT | 30 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| baseCount | INT | 3 | 基础爆炸头数量 |
| countPerLevel | INT | 1 | 每级额外数量 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
