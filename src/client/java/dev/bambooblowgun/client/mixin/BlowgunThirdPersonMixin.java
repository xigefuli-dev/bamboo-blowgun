package dev.bambooblowgun.client.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.bambooblowgun.BlowgunMod;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerItemInHandLayer.class)
public abstract class BlowgunThirdPersonMixin extends ItemInHandLayer<AvatarRenderState, PlayerModel> {
    protected BlowgunThirdPersonMixin(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }
    @Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At("HEAD"), cancellable = true)
    private void blowgun$mouth(AvatarRenderState state, ItemStackRenderState item, ItemStack stack, HumanoidArm arm, PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (arm != state.mainArm || !stack.is(BlowgunMod.BLOWGUN) || state.isUsingItem || state.isSpectator) return;
        ci.cancel();
        pose.pushPose();
        getParentModel().root().translateAndRotate(pose);
        getParentModel().head.translateAndRotate(pose);
        // Mouth endpoint is at head-local (0,-2,-4) pixels; the hollow tube extends forward.
        pose.translate(0, -0.125f, -0.75f);
        item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
    }
}