### ShockwaveSpell (irons_spellbooks:shockwave)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 70 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 16 | 施法时间 (tick) |
| cooldown | DOUBLE | 30.0 | 默认冷却时间 (秒) |
| baseRadius | DOUBLE | 8.0 | 基础半径 |
| radiusPerLevel | DOUBLE | 1.0 | 每级半径增量 |
| baseDamage | DOUBLE | 4.0 | 基础伤害 |
| damagePerPower | DOUBLE | 0.75 | 每点威力的伤害加成 |
| maxTargets | INT | -1 | 最多命中目标数 (-1 表示无限制) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
