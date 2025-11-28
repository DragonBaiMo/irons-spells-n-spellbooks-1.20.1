### FireballSpell (irons_spellbooks:fireball)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 60 | 基础魔力消耗 |
| manaCostPerLevel | INT | 15 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 1 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 40 | 施法时间 (tick) |
| cooldown | DOUBLE | 25.0 | 默认冷却时间 (秒) |
| damageBase | FLOAT | 5.0 | 基础伤害加值 |
| damageScale | FLOAT | 5.0 | 伤害系数 |
| radiusBase | INT | 2 | 基础半径 |
| radiusPerPower | FLOAT | 1.0 | 每点威力增加半径 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
