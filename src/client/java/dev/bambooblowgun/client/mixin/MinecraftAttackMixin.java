package dev.bambooblowgun.client.mixin;
import dev.bambooblowgun.BlowgunMod;
import dev.bambooblowgun.FirePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void blowgun$fire(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = (Minecraft)(Object)this;
        if (client.player == null || !client.player.getMainHandItem().is(BlowgunMod.BLOWGUN)) return;
        // Intercept all left clicks before vanilla dispatches air/entity/block attacks.
        cir.setReturnValue(false);
        if (!client.player.isSpectator() && !client.player.isUsingItem()) {
            dev.bambooblowgun.client.BlowgunTrigger.setHeld(client, true);
        }
    }
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void blowgun$stopMining(boolean held, CallbackInfo ci) {
        Minecraft client = (Minecraft)(Object)this;
        if (client.player != null && client.player.getMainHandItem().is(BlowgunMod.BLOWGUN)) {
            if (client.gameMode != null) client.gameMode.stopDestroyBlock();
            ci.cancel();
        }
    }
}