package com.bettercontent.revival;

import com.bettercontent.revival.api.RevivalApi;
import com.bettercontent.revival.api.event.PlayerBleedOutEvent;
import com.bettercontent.revival.api.event.PlayerDownedEvent;
import com.bettercontent.revival.api.event.PlayerFinishedEvent;
import com.bettercontent.revival.api.event.PlayerRevivedEvent;
import com.bettercontent.revival.network.RevivalNetwork;
import com.bettercontent.revival.network.StateSyncPacket;
import com.bettercontent.revival.state.RevivalState;
import com.bettercontent.revival.state.RevivalTuning;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RevivalManager {
    private static final Map<UUID, Set<UUID>> HELPERS = new HashMap<>();
    private static final Set<UUID> GIVING_UP = new HashSet<>();
    private static final Set<UUID> TERMINATING = new HashSet<>();
    private static final ResourceKey<DamageType> BLED_OUT = damageKey("bled_out");
    private static final ResourceKey<DamageType> FINISHED = damageKey("finished");

    private RevivalManager() {}

    public static void down(ServerPlayer player, DamageSource source) {
        if (RevivalApi.isDowned(player)) return;
        String damageType = source.typeHolder().unwrapKey().map(key -> key.location().toString()).orElse("");
        save(player, new RevivalState(RevivalConfig.BLEED_TICKS.get(), damageType));
        player.stopRiding();
        player.fallDistance = 0.0f;
        player.setForcedPose(Pose.SWIMMING);
        player.setHealth(Math.min(player.getMaxHealth(), 10.0f));
        player.getFoodData().setFoodLevel(6);
        sync(player, true);
        MinecraftForge.EVENT_BUS.post(new PlayerDownedEvent(player, source));
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!RevivalApi.isDowned(player)) continue;
            tickDowned(server, player);
        }
    }

    private static void tickDowned(MinecraftServer server, ServerPlayer player) {
        RevivalState state = load(player);
        Set<UUID> helpers = validHelpers(server, player);
        RevivalTuning tuning = new RevivalTuning(
                RevivalConfig.REVIVE_TICKS.get(),
                RevivalConfig.DECAY_GRACE_TICKS.get(),
                RevivalConfig.DECAY_PER_TICK.get().floatValue(),
                RevivalConfig.GIVE_UP_UNLOCK_TICKS.get(),
                RevivalConfig.GIVE_UP_HOLD_TICKS.get()
        );

        player.setForcedPose(Pose.SWIMMING);
        if (player.getFoodData().getFoodLevel() != 6) player.getFoodData().setFoodLevel(6);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 2, false, false, false));
        helpers.forEach(id -> {
            ServerPlayer helper = server.getPlayerList().getPlayer(id);
            if (helper != null) helper.causeFoodExhaustion(RevivalConfig.HELPER_EXHAUSTION_PER_TICK.get().floatValue());
        });

        RevivalState.TickResult result = state.tick(helpers.size(), GIVING_UP.contains(player.getUUID()), tuning);
        save(player, state);
        if (player.tickCount % RevivalConfig.WHISPER_INTERVAL_TICKS.get() == 0) {
            player.level().playSound(null, player, RevivalMod.DOWNED_WHISPER.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (player.tickCount % 5 == 0) sync(player, true);

        switch (result) {
            case REVIVED -> revive(player);
            case GAVE_UP -> terminate(player, null, false);
            case BLED_OUT -> {
                MinecraftForge.EVENT_BUS.post(new PlayerBleedOutEvent(player));
                terminate(player, null, false);
            }
            case ACTIVE -> { }
        }
    }

    public static void setAidIntent(ServerPlayer helper, UUID targetId, boolean active) {
        removeHelper(helper.getUUID());
        if (!active || RevivalApi.isDowned(helper)) return;
        ServerPlayer target = helper.server.getPlayerList().getPlayer(targetId);
        if (!validInteraction(helper, target)) return;
        HELPERS.computeIfAbsent(targetId, ignored -> new HashSet<>()).add(helper.getUUID());
        sync(target, true);
    }

    public static void setGiveUpIntent(ServerPlayer player, boolean active) {
        if (active && RevivalApi.isDowned(player)) GIVING_UP.add(player.getUUID());
        else GIVING_UP.remove(player.getUUID());
    }

    public static void finish(ServerPlayer attacker, UUID targetId) {
        ServerPlayer target = attacker.server.getPlayerList().getPlayer(targetId);
        if (!validInteraction(attacker, target)) return;
        attacker.swing(InteractionHand.MAIN_HAND, true);
        target.level().broadcastEntityEvent(target, (byte) 2);
        target.level().playSound(null, target, SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0f, 0.82f);
        target.level().playSound(null, target, SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 0.7f, 1.1f);
        target.level().playSound(null, target, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.55f, 0.72f);
        MinecraftForge.EVENT_BUS.post(new PlayerFinishedEvent(target, attacker));
        terminate(target, attacker, true);
    }

    public static boolean isTerminating(ServerPlayer player) {
        return TERMINATING.contains(player.getUUID());
    }

    public static void prepareNormalDeath(ServerPlayer player) {
        clearState(player);
    }

    public static void onLogout(ServerPlayer player) {
        removeHelper(player.getUUID());
        if (RevivalApi.isDowned(player)) terminate(player, null, false);
        GIVING_UP.remove(player.getUUID());
    }

    public static void sync(ServerPlayer player, boolean downed) {
        RevivalState state = downed ? load(player) : null;
        int helperCount = HELPERS.getOrDefault(player.getUUID(), Set.of()).size();
        StateSyncPacket packet = state == null
                ? new StateSyncPacket(player.getUUID(), false, 0, 0, 0, 0, 0)
                : new StateSyncPacket(player.getUUID(), true, state.ticksLeft(), state.downedTicks(),
                        state.reviveProgress(), state.giveUpTicks(), helperCount);
        RevivalNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }

    private static void revive(ServerPlayer player) {
        clearState(player);
        player.setHealth(Math.min(player.getMaxHealth(), RevivalConfig.REVIVED_HEALTH_HALF_HEARTS.get().floatValue()));
        int duration = RevivalConfig.RECOVERY_TICKS.get();
        if (duration > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
        }
        player.level().playSound(null, player, SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.45f, 1.2f);
        player.level().playSound(null, player, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.35f, 1.3f);
        MinecraftForge.EVENT_BUS.post(new PlayerRevivedEvent(player));
    }

    private static void terminate(ServerPlayer target, ServerPlayer attacker, boolean finished) {
        String originalType = RevivalApi.isDowned(target) ? load(target).originalDamageType() : "";
        clearState(target);
        TERMINATING.add(target.getUUID());
        DamageSource source = finished
                ? damageSource(target.serverLevel(), FINISHED, attacker)
                : originalDamageSource(target.serverLevel(), originalType);
        target.setHealth(Math.max(1.0f, target.getHealth()));
        target.hurt(source, Float.MAX_VALUE);
        if (target.isAlive()) target.hurt(damageSource(target.serverLevel(), BLED_OUT, attacker), Float.MAX_VALUE);
        TERMINATING.remove(target.getUUID());
    }

    private static DamageSource originalDamageSource(ServerLevel level, String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location != null) {
            ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, location);
            Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(key).orElse(null);
            if (holder != null) return new DamageSource(holder);
        }
        return damageSource(level, BLED_OUT, null);
    }

    private static DamageSource damageSource(ServerLevel level, ResourceKey<DamageType> key, ServerPlayer attacker) {
        Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
        return attacker == null ? new DamageSource(holder) : new DamageSource(holder, attacker);
    }

    private static boolean validInteraction(ServerPlayer helper, ServerPlayer target) {
        if (helper == null || target == null || helper == target || !helper.isAlive() || !target.isAlive()) return false;
        if (!RevivalApi.isDowned(target) || RevivalApi.isDowned(helper) || helper.level() != target.level()) return false;
        double max = RevivalConfig.REVIVE_DISTANCE.get();
        if (helper.distanceToSqr(target) > max * max || !helper.hasLineOfSight(target)) return false;
        Vec3 toTarget = target.getEyePosition().subtract(helper.getEyePosition()).normalize();
        return helper.getLookAngle().dot(toTarget) >= 0.94;
    }

    private static Set<UUID> validHelpers(MinecraftServer server, ServerPlayer target) {
        Set<UUID> helpers = HELPERS.computeIfAbsent(target.getUUID(), ignored -> new HashSet<>());
        Iterator<UUID> iterator = helpers.iterator();
        while (iterator.hasNext()) {
            ServerPlayer helper = server.getPlayerList().getPlayer(iterator.next());
            if (!validInteraction(helper, target)) iterator.remove();
        }
        return helpers;
    }

    private static void removeHelper(UUID helperId) {
        HELPERS.values().forEach(set -> set.remove(helperId));
        HELPERS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private static RevivalState load(ServerPlayer player) {
        return RevivalState.load(player.getPersistentData().getCompound(RevivalState.ROOT_TAG));
    }

    private static void save(ServerPlayer player, RevivalState state) {
        player.getPersistentData().put(RevivalState.ROOT_TAG, state.save());
    }

    private static void clearState(ServerPlayer player) {
        player.getPersistentData().remove(RevivalState.ROOT_TAG);
        player.setForcedPose(null);
        HELPERS.remove(player.getUUID());
        removeHelper(player.getUUID());
        GIVING_UP.remove(player.getUUID());
        sync(player, false);
    }

    private static ResourceKey<DamageType> damageKey(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RevivalMod.MOD_ID, path));
    }
}
