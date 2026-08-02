package com.bettercontent.revival;

import com.bettercontent.revival.api.RevivalApi;
import com.bettercontent.revival.state.RevivalRules;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class RevivalForgeEvents {
    private RevivalForgeEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isCreative() || player.isSpectator()) return;
        if (RevivalManager.isTerminating(player)) return;

        if (RevivalApi.isDowned(player)) {
            if (RevivalRules.livingAttackerDamage(event.getSource().getEntity())) {
                event.setCanceled(true);
                player.setHealth(1.0f);
            } else {
                RevivalManager.prepareNormalDeath(player);
            }
            return;
        }

        DamageSource source = event.getSource();
        String id = source.typeHolder().unwrapKey().map(key -> key.location().toString()).orElse(source.getMsgId());
        if (RevivalRules.bypassesDownedState(id) || RevivalRules.bypassesDownedState(source.getMsgId())) return;
        event.setCanceled(true);
        RevivalManager.down(player, source);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !RevivalApi.isDowned(player)) return;
        if (RevivalRules.livingAttackerDamage(event.getSource().getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("revival")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("test")
                        .then(Commands.literal("down")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                                context.getSource().getOnlinePlayerNames(), builder))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getServer().getPlayerList()
                                                    .getPlayerByName(StringArgumentType.getString(context, "player"));
                                            if (player == null || RevivalApi.isDowned(player)) return 0;
                                            RevivalManager.down(player, player.damageSources().generic());
                                            return 1;
                                        })))));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) RevivalManager.tick(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RevivalManager.onLogout(player);
    }

    @SubscribeEvent
    public static void onTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && RevivalApi.isDowned(target)) RevivalManager.sync(target, true);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && RevivalApi.isDowned(player)) RevivalManager.sync(player, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && RevivalApi.isDowned(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && RevivalApi.isDowned(player)) event.setCanceled(true);
    }
}
