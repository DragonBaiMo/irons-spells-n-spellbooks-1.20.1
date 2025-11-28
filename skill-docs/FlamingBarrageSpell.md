### FlamingBarrageSpell (irons_spellbooks:flaming_barrage)


| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| manaCost / baseManaCost | INT | 80 | 基础魔力消耗 |
| manaCostPerLevel | INT | 5 | 每级魔力消耗增量 |
| power / baseSpellPower | INT | 3 | 基础技能威力 |
| levelScaling / spellPowerPerLevel | INT | 2 | 每级技能威力增量 |
| castTime | INT | 0 | 施法时间 (tick) |
| cooldown | DOUBLE | 15.0 | 默认冷却时间 (秒) |
| recastCount | INT | 5 | 重铸次数 |
| recastIntervalTicks | INT | 80 | 重铸间隔 (tick) |
| inaccuracyMin | FLOAT | 0.2 | 最小散射 |
| inaccuracyMax | FLOAT | 1.4 | 最大散射 |

**说明**: 参数可在管理员通道中通过 SpellParameters 进行覆盖。
