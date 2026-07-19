package com.bettercontent.revival.state;

import java.util.Set;

public final class RevivalRules {
    private static final Set<String> BYPASS_DAMAGE_TYPES = Set.of(
            "gorgon",
            "sgcraft:transient",
            "sgcraft:iris",
            "vampirism_dbno",
            "hordes:infection",
            "minecraft:generic_kill"
    );

    private RevivalRules() {}

    public static boolean bypassesDownedState(String damageTypeId) {
        if (damageTypeId == null) return false;
        String normalized = damageTypeId.startsWith("death.attack.")
                ? damageTypeId.substring("death.attack.".length())
                : damageTypeId;
        return BYPASS_DAMAGE_TYPES.contains(normalized);
    }

    public static boolean livingAttackerDamage(Object causingEntity) {
        return causingEntity instanceof net.minecraft.world.entity.LivingEntity;
    }
}
