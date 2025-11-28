### RootSpell (irons_spellbooks:root)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 45 | 基础魔力消耗 |
| manaCostPerLevel | INT | 3 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 5 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 40 | 施法时间 (tick) |
| cooldown | DOUBLE | 35.0 | 默认冷却时间 (秒) |
| maxTargetRange | DOUBLE | 32.0 | 最大选取距离 |
| durationPerPower | INT | 20 | 每点威力对应的束缚时长 (tick) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
