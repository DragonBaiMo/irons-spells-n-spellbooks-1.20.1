### TelekinesisSpell (irons_spellbooks:telekinesis)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 25 | 基础魔力消耗 |
| manaCostPerLevel | INT | 0 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 4 | 每级技能威力增量 |
| castTime | INT | 140 | 施法时间 (tick) |
| cooldown | DOUBLE | 35.0 | 默认冷却时间 (秒) |
| rangeBase | DOUBLE | 12.0 | 基础抓取距离 |
| rangePerLevel | DOUBLE | 2.0 | 每级抓取距离增量 |
| forceScale | DOUBLE | 0.6 | 牵引强度倍率 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
