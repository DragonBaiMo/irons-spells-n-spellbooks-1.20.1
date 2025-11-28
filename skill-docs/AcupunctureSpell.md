### AcupunctureSpell (irons_spellbooks:acupuncture)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 25 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 1 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 0 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 20.0 | 默认冷却时间 (秒) |
| baseNeedles | INT | 4 | 基础针数 |
| needlesPerLevel | INT | 1 | 每级新增针数 |
| damageBase | DOUBLE | 1.0 | 基础伤害 |
| damagePerPower | DOUBLE | 1.0 | 每点威力附加伤害 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
