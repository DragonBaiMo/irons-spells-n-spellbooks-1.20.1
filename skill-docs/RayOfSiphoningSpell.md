### RayOfSiphoningSpell (irons_spellbooks:ray_of_siphoning)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 8 | 基础魔力消耗 |
| manaCostPerLevel | INT | 1 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 4 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 100 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| range | DOUBLE | 12.0 | 最大射程 |
| damageMultiplier | DOUBLE | 0.25 | 每点威力伤害倍率 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
