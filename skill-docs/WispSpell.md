### WispSpell (irons_spellbooks:wisp)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 15 | 基础魔力消耗 |
| manaCostPerLevel | INT | 2 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 5 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 1 | 每级技能威力增量 |
| castTime | INT | 20 | 施法时间 (tick) |
| cooldown | DOUBLE | 3.0 | 默认冷却时间 (秒) |
| wispLifetimeTicks | INT | 200 | 幽火存在的最长时间 (tick) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
