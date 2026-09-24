package dev.bambooblowgun.client.mixin;
import dev.bambooblowgun.BlowgunMod;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public abstract class BlowgunPlayerPoseMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void blowgun$pose(AvatarRenderState state, CallbackInfo ci) {
        if (state.isSpectator || state.isUsingItem || !state.getMainHandItemStack().is(BlowgunMod.BLOWGUN)) return;
        PlayerModel model = (PlayerModel)(Object)this;
        boolean right = state.mainArm == HumanoidArm.RIGHT;
        ModelPart arm = right ? model.rightArm : model.leftArm;
        arm.xRot = model.head.xRot - 1.95f;
        arm.yRot = model.head.yRot + (right ? -0.45f : 0.45f);
        arm.zRot = 0;
        // The offhand remains available for shields or other items.
    }
}