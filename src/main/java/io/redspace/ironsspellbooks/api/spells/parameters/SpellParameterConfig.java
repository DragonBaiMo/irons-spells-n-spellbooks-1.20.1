package io.redspace.ironsspellbooks.api.spells.parameters;

public record SpellParameterConfig(int baseManaCost, int manaCostPerLevel, int baseSpellPower, int spellPowerPerLevel, int castTime, double cooldownSeconds) {
    public static final SpellParameterConfig DEFAULT = new SpellParameterConfig(0, 0, 0, 0, 0, 0d);

    public SpellParameterConfig withOverrides(SpellParameters parameters) {
        return new SpellParameterConfig(
                parameters.getInt("baseManaCost", baseManaCost),
                parameters.getInt("manaCostPerLevel", manaCostPerLevel),
                parameters.getInt("baseSpellPower", baseSpellPower),
                parameters.getInt("spellPowerPerLevel", spellPowerPerLevel),
                parameters.getInt("castTime", castTime),
                parameters.getDouble("cooldown", cooldownSeconds)
        );
    }
}
