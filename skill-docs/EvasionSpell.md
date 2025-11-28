### EvasionSpell (irons_spellbooks:evasion)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 40 | 基础魔力消耗 |
| manaCostPerLevel | INT | 20 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 0 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 180.0 | 默认冷却时间 (秒) |
| durationSeconds | INT | 60 | 持续时间 (秒) |
| baseHits | FLOAT | 1.0 | 基础可闪避次数 |
| hitsPerPower | FLOAT | 1.0 | 每点威力增加的闪避次数 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
