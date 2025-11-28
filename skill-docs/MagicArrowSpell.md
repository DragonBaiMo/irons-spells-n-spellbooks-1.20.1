### MagicArrowSpell (irons_spellbooks:magic_arrow)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 40 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 10 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 2 | 每级技能威力增量 |
| castTime | INT | 30 | 施法时间 (tick) |
| cooldown | DOUBLE | 8.0 | 默认冷却时间 (秒) |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
