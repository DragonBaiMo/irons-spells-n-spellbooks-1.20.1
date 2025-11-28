### ArrowVolleySpell (irons_spellbooks:arrow_volley)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 40 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 0 | 每级技能威力增量 |
| castTime | INT | 30 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| baseRows | INT | 4 | 基础箭阵行数 |
| rowsPerLevel | INT | 1 | 每级额外行数 |
| arrowsPerRowBase | DOUBLE | 5.0 | 基础每行箭数 |
| arrowsPerRowPerLevel | DOUBLE | 0.5 | 每级增加的每行箭数 |
| damageMultiplier | DOUBLE | 0.25 | 伤害系数 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
