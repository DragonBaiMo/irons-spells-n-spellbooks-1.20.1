### PoisonArrowSpell (irons_spellbooks:poison_arrow)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 40 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 5 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| projectileSpeed | DOUBLE | 2.5 | 弹速 |
| aoeDurationTicks | INT | 200 | 毒云持续时间 (tick) |
| damageMultiplier | DOUBLE | 1.0 | 直伤倍率 (基于技能威力) |
| aoeDamageMultiplier | DOUBLE | 0.185 | 毒云伤害倍率 (基于技能威力) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
