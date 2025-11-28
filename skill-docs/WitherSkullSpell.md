### WitherSkullSpell (irons_spellbooks:wither_skull)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 20 | 基础魔力消耗 |
| manaCostPerLevel | INT | 2 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 12 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 1.0 | 默认冷却时间 (秒) |
| baseSpeed | DOUBLE | 8.0 | 基础速度系数 (最终速度除以100) |
| speedPerLevel | DOUBLE | 1.0 | 每级速度增量系数 |
| damageMultiplier | DOUBLE | 0.5 | 伤害倍率 (基于技能威力) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
