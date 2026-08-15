package com.bettercontent.downedplayerrevival.client;

import com.bettercontent.downedplayerrevival.RevivalConfig;
import com.bettercontent.downedplayerrevival.RevivalMod;
import com.bettercontent.downedplayerrevival.network.AidIntentPacket;
import com.bettercontent.downedplayerrevival.network.FinishPacket;
import com.bettercontent.downedplayerrevival.network.GiveUpIntentPacket;
import com.bettercontent.downedplayerrevival.network.RevivalNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = RevivalMod.MOD_ID, value = Dist.CLIENT)
public final class ClientRevivalInput {
    private static UUID aidTarget;
    private static boolean giveUpActive;

    private ClientRevivalInput() {}

    @SubscribeEvent
    public static void onMouse(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player local = minecraft.player;
        if (local == null || minecraft.screen != null) return;

        if (ClientRevivalState.isDowned(local.getUUID())) {
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT || event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                event.setCanceled(true);
            }
            return;
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (event.getAction() == GLFW.GLFW_RELEASE && aidTarget != null) {
                stopAid();
                event.setCanceled(true);
                return;
            }
            Player target = targetedDownedPlayer(minecraft);
            if (event.getAction() == GLFW.GLFW_PRESS && target != null) {
                aidTarget = target.getUUID();
                RevivalNetwork.CHANNEL.sendToServer(new AidIntentPacket(aidTarget, true));
                event.setCanceled(true);
            }
            return;
        }

        int finishModifiers = GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SHIFT;
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && event.getAction() == GLFW.GLFW_PRESS
                && (event.getModifiers() & finishModifiers) == finishModifiers) {
            Player target = targetedDownedPlayer(minecraft);
            if (target != null) {
                event.setCanceled(true);
                local.swing(InteractionHand.MAIN_HAND);
                RevivalNetwork.CHANNEL.sendToServer(new FinishPacket(target.getUUID()));
            }
        }
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && ClientRevivalState.isDowned(minecraft.player.getUUID())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && ClientRevivalState.isDowned(minecraft.player.getUUID())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientRevivalState.tick(minecraft);
        Player local = minecraft.player;
        if (local == null) return;

        if (aidTarget != null) {
            boolean rightHeld = GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            Player aimedAt = targetedDownedPlayer(minecraft);
            if (!rightHeld || !ClientRevivalState.isDowned(aidTarget) || aimedAt == null || !aimedAt.getUUID().equals(aidTarget)) stopAid();
        }

        boolean downed = ClientRevivalState.isDowned(local.getUUID());
        if (downed) {
            clearActionKey(minecraft.options.keyAttack);
            clearActionKey(minecraft.options.keyUse);
            clearActionKey(minecraft.options.keyJump);
            clearActionKey(minecraft.options.keySprint);
            clearActionKey(minecraft.options.keyInventory);
            clearActionKey(minecraft.options.keySwapOffhand);
            clearActionKey(minecraft.options.keyDrop);
            clearActionKey(minecraft.options.keyPickItem);
            for (KeyMapping hotbarKey : minecraft.options.keyHotbarSlots) clearActionKey(hotbarKey);
            if (minecraft.screen instanceof AbstractContainerScreen<?>) minecraft.setScreen(null);
        }
        int downedTicks = ClientRevivalState.get(local.getUUID()).map(packet -> packet.downedTicks()).orElse(0);
        boolean shouldGiveUp = downed
                && downedTicks >= RevivalConfig.GIVE_UP_UNLOCK_TICKS.get()
                && minecraft.options.keyShift.isDown();
        if (shouldGiveUp != giveUpActive) {
            giveUpActive = shouldGiveUp;
            RevivalNetwork.CHANNEL.sendToServer(new GiveUpIntentPacket(giveUpActive));
        }
    }

    public static UUID aidTarget() { return aidTarget; }

    public static void targetCleared(UUID playerId) {
        if (playerId.equals(aidTarget)) stopAid();
    }

    public static void reset() {
        aidTarget = null;
        giveUpActive = false;
    }

    private static Player targetedDownedPlayer(Minecraft minecraft) {
        if (!(minecraft.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player target)) return null;
        return ClientRevivalState.isDowned(target.getUUID()) ? target : null;
    }

    private static void stopAid() {
        if (aidTarget != null) RevivalNetwork.CHANNEL.sendToServer(new AidIntentPacket(aidTarget, false));
        aidTarget = null;
    }

    private static void clearActionKey(KeyMapping key) {
        key.setDown(false);
        while (key.consumeClick()) { }
    }
}
