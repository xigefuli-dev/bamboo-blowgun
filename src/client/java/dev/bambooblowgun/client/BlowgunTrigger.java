package dev.bambooblowgun.client;
import dev.bambooblowgun.BlowgunMod;
import dev.bambooblowgun.TriggerPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
public final class BlowgunTrigger {
    private static LocalPlayer sessionPlayer;
    private static boolean sentHeld;
    public static void setHeld(Minecraft client, boolean held) {
        if (sessionPlayer != client.player) { sessionPlayer = client.player; sentHeld = false; }
        if (client.player == null || !ClientPlayNetworking.canSend(TriggerPayload.TYPE)) { sentHeld = false; return; }
        if (held != sentHeld) {
            ClientPlayNetworking.send(new TriggerPayload(held));
            sentHeld = held;
        }
    }
    public static void tick(Minecraft client) {
        boolean held = client.player != null && client.player.isAlive() && !client.player.isSpectator()
            && !client.player.isUsingItem() && client.player.getMainHandItem().is(BlowgunMod.BLOWGUN)
            && client.mouseHandler.isMouseGrabbed() && client.isWindowActive() && !client.isPaused()
            && client.options.keyAttack.isDown();
        setHeld(client, held);
    }
}