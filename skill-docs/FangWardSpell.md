### FangWardSpell (irons_spellbooks:fang_ward)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 45 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 15 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| ringsBase | INT | 2 | 基础环数 |
| ringSpacing | FLOAT | 1.5 | 环间距 |
| fangsFirstRing | INT | 5 | 第一圈獠牙数量 |
| groundSearchMaxSteps | INT | 5 | 地面搜索最大步数 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
