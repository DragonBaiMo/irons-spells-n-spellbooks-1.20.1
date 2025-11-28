### StarfallSpell (irons_spellbooks:starfall)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 5 | 基础魔力消耗 |
| manaCostPerLevel | INT | 1 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 160 | 施法时间 (tick) |
| cooldown | DOUBLE | 16.0 | 默认冷却时间 (秒) |
| radius | DOUBLE | 6.0 | 落星覆盖半径 |
| damageMultiplier | DOUBLE | 0.5 | 伤害倍率 (基于技能威力) |
| explosionRadius | DOUBLE | 2.0 | 流星爆炸半径 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
