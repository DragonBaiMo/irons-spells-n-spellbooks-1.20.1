### FlamingStrikeSpell (irons_spellbooks:flaming_strike)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 30 | 基础魔力消耗 |
| manaCostPerLevel | INT | 15 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 5 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 2 | 每级技能威力增量 |
| castTime | INT | 10 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| arcRadius | FLOAT | 3.25 | 挥击半径 |
| arcDistance | FLOAT | 1.9 | 前探距离 |
| fireTimeSeconds | FLOAT | 3.0 | 点燃时长 (秒) |
| weaponDamageScale | FLOAT | 1.0 | 武器伤害系数 |
| bonusDamageScale | FLOAT | 1.0 | 技能威力系数 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
