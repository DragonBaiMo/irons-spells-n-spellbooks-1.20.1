### ChainLightningSpell (irons_spellbooks:chain_lightning)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 25 | 基础魔力消耗 |
| manaCostPerLevel | INT | 7 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 6 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 20.0 | 默认冷却时间 (秒) |
| baseConnections | INT | 3 | 基础跳跃次数 |
| connectionsPerLevel | INT | 1 | 每级额外跳跃次数 |
| rangeBase | DOUBLE | 1.0 | 基础跳跃距离 |
| rangePerPower | DOUBLE | 0.5 | 每点威力附加跳跃距离 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
