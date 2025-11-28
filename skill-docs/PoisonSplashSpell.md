### PoisonSplashSpell (irons_spellbooks:poison_splash)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 40 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 8 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 15 | 施法时间 (tick) |
| cooldown | DOUBLE | 20.0 | 默认冷却时间 (秒) |
| targetRange | DOUBLE | 32.0 | 目标选择最大距离 |
| damageMultiplier | DOUBLE | 1.0 | 伤害倍率 (基于技能威力) |
| durationBase | INT | 100 | 基础持续时间 (tick) |
| durationPerLevel | INT | 40 | 每级持续时间增量 (tick) |
| cloudRadius | DOUBLE | 2.0 | 毒云半径 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
