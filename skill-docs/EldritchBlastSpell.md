### EldritchBlastSpell (irons_spellbooks:eldritch_blast)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 90 | 基础魔力消耗 |
| manaCostPerLevel | INT | 15 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 15 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 0 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| recastBase | INT | 2 | 重铸基础次数 |
| recastPerLevel | INT | 1 | 每级追加重铸次数 |
| recastIntervalTicks | INT | 80 | 重铸间隔 (tick) |
| range | FLOAT | 30.0 | 施法距离 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
