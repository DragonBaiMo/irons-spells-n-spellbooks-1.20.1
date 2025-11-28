### CleanseSpell (irons_spellbooks:cleanse)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 100 | 基础魔力消耗 |
| manaCostPerLevel | INT | 0 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 0 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 0 | 每级技能威力增量 |
| castTime | INT | 60 | 施法时间 (tick) |
| cooldown | DOUBLE | 60.0 | 默认冷却时间 (秒) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。净化半径固定为 2 格，施法瞄准提示与实际生效范围一致。
