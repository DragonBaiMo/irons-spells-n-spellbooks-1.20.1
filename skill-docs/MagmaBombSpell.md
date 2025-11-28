### MagmaBombSpell (irons_spellbooks:magma_bomb)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 30 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 3 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 12.0 | 默认冷却时间 (秒) |
| radiusBase | FLOAT | 3.0 | 爆炸半径基础值 |
| radiusPowerScale | FLOAT | 1.0 | 爆炸半径威力系数 |
| directDamageMultiplier | FLOAT | 1.0 | 直击伤害威力倍率 |
| aoeDamageBase | FLOAT | 1.0 | 范围伤害基础值 |
| aoeDamageRatio | FLOAT | 0.1 | 范围伤害威力系数 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
