### BlazeStormSpell (irons_spellbooks:blaze_storm)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 5 | 基础魔力消耗 |
| manaCostPerLevel | INT | 1 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 5 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 55 | 基础施法时间 (tick) |
| castTimePerLevel | INT | 5 | 每级额外施法时间 (tick) |
| cooldown | DOUBLE | 20.0 | 默认冷却时间 (秒) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。实际施法时间=基础施法时间 + castTimePerLevel * 技能等级。
