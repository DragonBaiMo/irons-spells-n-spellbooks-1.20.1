### DivineSmiteSpell (irons_spellbooks:divine_smite)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 30 | 基础魔力消耗 |
| manaCostPerLevel | INT | 15 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 3 | 每级技能威力增量 |
| castTime | INT | 16 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| smiteRadius | FLOAT | 2.2 | 冲击半径 |
| smiteRange | FLOAT | 1.7 | 落点前推距离 |
| particleYOffset | FLOAT | 2.0 | 落点向下探测高度 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
