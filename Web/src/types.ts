export interface SkillParam {
    key: string;
    aliases: string[];
    type: string;
    defaultValue: string;
    description: string;
}

export interface Skill {
    displayName: string;
    fullId: string;
    commandId: string;
    description: string;
    params: SkillParam[];
}

export interface CastConfig {
    caster: string;
    level: number;
    target: string;
    consumeMana: boolean;
    triggerCooldown: boolean;
    playEffects: boolean;
    bypassConditions: boolean;
    showCastBar: boolean;
}

export interface ParamOverride {
    [key: string]: string | number | boolean;
}
