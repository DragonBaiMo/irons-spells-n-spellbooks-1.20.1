### RayOfFrostSpell (irons_spellbooks:ray_of_frost)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 25 | 基础魔力消耗 |
| manaCostPerLevel | INT | 15 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 6 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| range | DOUBLE | 30.0 | 最大射程 |
| damageMultiplier | DOUBLE | 1.5 | 伤害倍率 (基于技能威力) |
| freezePerPower | INT | 15 | 每点威力附加冻结时长 (tick) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
