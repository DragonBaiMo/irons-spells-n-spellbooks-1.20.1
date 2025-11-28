### GustSpell (irons_spellbooks:gust)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 30 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 10 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 15 | 施法时间 (tick) |
| cooldown | DOUBLE | 12.0 | 默认冷却时间 (秒) |
| range | FLOAT | 8.0 | 施法范围 |
| strengthScale | FLOAT | 0.2 | 击退强度系数 |
| kickbackScale | FLOAT | 0.25 | 自我后坐力系数 |
| damageScale | FLOAT | 1.0 | 落地伤害系数 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
