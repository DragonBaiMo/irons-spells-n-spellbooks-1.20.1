### HealingCircleSpell (irons_spellbooks:healing_circle)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 40 | 基础魔力消耗 |
| manaCostPerLevel | INT | 10 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 2 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 25.0 | 默认冷却时间 (秒) |
| radius | FLOAT | 5.0 | 治疗半径 |
| duration | INT | 200 | 持续时间 (tick) |
| healMultiplier | FLOAT | 0.25 | 治疗系数 (威力倍率) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
