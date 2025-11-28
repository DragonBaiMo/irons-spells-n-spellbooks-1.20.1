### FireboltSpell (irons_spellbooks:firebolt)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 10 | 基础魔力消耗 |
| manaCostPerLevel | INT | 2 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 12 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 1.0 | 默认冷却时间 (秒) |
| damageScale | FLOAT | 0.5 | 伤害系数 |
| fireTimeSeconds | FLOAT | 3.0 | 点燃时长 (秒) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
