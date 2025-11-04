package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.spells.parameters.SpellParameters;

/**
 * 施法选项配置
 * 控制施法过程中的行为（魔法消耗、冷却、特效等）
 */
public class CastOptions {
    public final boolean consumeMana;
    public final boolean triggerCooldown;
    public final boolean playEffects;
    public final boolean bypassConditions;
    public final boolean showCastBar;

    private CastOptions(boolean consumeMana, boolean triggerCooldown,
                        boolean playEffects, boolean bypassConditions,
                        boolean showCastBar) {
        this.consumeMana = consumeMana;
        this.triggerCooldown = triggerCooldown;
        this.playEffects = playEffects;
        this.bypassConditions = bypassConditions;
        this.showCastBar = showCastBar;
    }

    public static CastOptions fromSource(CastSource source) {
        return switch (source) {
            case SPELLBOOK -> new CastOptions(true, true, true, false, true);
            case SCROLL -> new CastOptions(false, false, true, false, false);
            case SWORD -> new CastOptions(true, true, true, false, true);
            case COMMAND -> new CastOptions(false, false, true, true, false);
            case MOB -> new CastOptions(false, false, true, false, false);
            case NONE -> new CastOptions(false, false, false, false, false);
        };
    }

    public static CastOptions fromParameters(SpellParameters params, CastSource source) {
        CastOptions defaults = fromSource(source);
        return new CastOptions(
                params.getBoolean("consumeMana", defaults.consumeMana),
                params.getBoolean("triggerCooldown", defaults.triggerCooldown),
                params.getBoolean("playEffects", defaults.playEffects),
                params.getBoolean("bypassConditions", defaults.bypassConditions),
                params.getBoolean("showCastBar", defaults.showCastBar)
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean consumeMana = false;
        private boolean triggerCooldown = false;
        private boolean playEffects = true;
        private boolean bypassConditions = false;
        private boolean showCastBar = false;

        public Builder consumeMana(boolean value) {
            this.consumeMana = value;
            return this;
        }

        public Builder triggerCooldown(boolean value) {
            this.triggerCooldown = value;
            return this;
        }

        public Builder playEffects(boolean value) {
            this.playEffects = value;
            return this;
        }

        public Builder bypassConditions(boolean value) {
            this.bypassConditions = value;
            return this;
        }

        public Builder showCastBar(boolean value) {
            this.showCastBar = value;
            return this;
        }

        public CastOptions build() {
            return new CastOptions(consumeMana, triggerCooldown, playEffects, bypassConditions, showCastBar);
        }
    }
}
