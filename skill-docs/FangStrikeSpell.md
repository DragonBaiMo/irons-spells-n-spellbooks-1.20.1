### FangStrikeSpell (irons_spellbooks:fang_strike)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 30 | 基础魔力消耗 |
| manaCostPerLevel | INT | 3 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 6 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 15 | 施法时间 (tick) |
| cooldown | DOUBLE | 5.0 | 默认冷却时间 (秒) |
| baseCount | INT | 7 | 基础獠牙数量 |
| countPerLevel | INT | 1 | 每级追加数量 |
| spacing | FLOAT | 1.0 | 獠牙间距 |
| delayDivisor | INT | 3 | 生成延迟除数 |
| groundSearchMaxSteps | INT | 8 | 地面搜索最大步数 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
