### FireflySwarmSpell (irons_spellbooks:firefly_swarm)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 40 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 6 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 30 | 施法时间 (tick) |
| cooldown | DOUBLE | 20.0 | 默认冷却时间 (秒) |
| radius | FLOAT | 2.0 | 作用半径 |
| damageScale | FLOAT | 0.33333334 | 伤害系数 |
| targetSearchRange | FLOAT | 32.0 | 目标搜索距离 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
