package com.bettercontent.downedplayerrevival;

import com.bettercontent.downedplayerrevival.api.RevivalApi;
import com.bettercontent.downedplayerrevival.state.DownedPlayerConstraints;
import com.bettercontent.downedplayerrevival.state.RevivalRules;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
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
        event.getDispatcher().register(Commands.literal("downedplayerrevival")
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
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START
                && event.player instanceof ServerPlayer player
                && RevivalApi.isDowned(player)) {
            DownedPlayerConstraints.enforce(player);
        }
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity() instanceof ServerPlayer player && RevivalApi.isDowned(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && RevivalApi.isDowned(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onToss(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !RevivalApi.isDowned(player)) return;
        var stack = event.getEntity().getItem();
        player.getInventory().add(stack);
        if (!stack.isEmpty() && player.containerMenu.getCarried().isEmpty()) {
            player.containerMenu.setCarried(stack.copy());
            stack.setCount(0);
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && RevivalApi.isDowned(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExperiencePickup(PlayerXpEvent.PickupXp event) {
        if (event.getEntity() instanceof ServerPlayer player && RevivalApi.isDowned(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && RevivalApi.isDowned(player)) player.closeContainer();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMount(EntityMountEvent event) {
        if (event.isMounting()
                && event.getEntityMounting() instanceof ServerPlayer player
                && RevivalApi.isDowned(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !RevivalApi.isDowned(player)) return;
        var motion = player.getDeltaMovement();
        if (motion.y > 0.0) player.setDeltaMovement(motion.x, 0.0, motion.z);
    }
}
