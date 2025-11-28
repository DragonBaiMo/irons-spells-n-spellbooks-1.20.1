### FrostStepSpell (irons_spellbooks:frost_step)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 15 | 基础魔力消耗 |
| manaCostPerLevel | INT | 3 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 14 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 3 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 10.0 | 默认冷却时间 (秒) |
| distanceScale | FLOAT | 0.65 | 距离系数 |
| shadowDamageScale | FLOAT | 0.33333334 | 残影碎裂伤害系数 |
| shadowDurationTicks | INT | 60 | 残影存活时间 (tick) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
