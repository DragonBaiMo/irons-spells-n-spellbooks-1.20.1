### SacrificeSpell (irons_spellbooks:sacrifice)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 25 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 2 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 1.0 | 默认冷却时间 (秒) |
| targetRange | DOUBLE | 25.0 | 可选取召唤物的最大距离 |
| healthDamageRatio | DOUBLE | 0.5 | 消耗目标生命的伤害系数 |
| explosionRadiusBase | DOUBLE | 3.0 | 基础爆炸半径 |
| explosionRadiusHealthScale | DOUBLE | 0.5 | 爆炸半径随生命的比例 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
