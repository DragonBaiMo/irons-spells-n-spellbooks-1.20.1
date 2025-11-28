### ShieldSpell (irons_spellbooks:shield)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 35 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 5 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 10 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 8.0 | 默认冷却时间 (秒) |
| placementRange | DOUBLE | 5.0 | 放置距离 |
| shieldBase | DOUBLE | 10.0 | 护盾基础生命 |
| shieldPowerMultiplier | DOUBLE | 1.0 | 护盾生命倍率 (基于技能威力) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
