package dev.bambooblowgun.client.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.bambooblowgun.BlowgunMod;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandRenderer.class)
public abstract class BlowgunFirstPersonMixin {
    @Shadow public abstract void renderItem(LivingEntity player, ItemStack item, ItemDisplayContext context, PoseStack pose, SubmitNodeCollector collector, int light);
    @Shadow private void renderPlayerArm(PoseStack pose, SubmitNodeCollector collector, int light, float equip, float swing, HumanoidArm arm) {}
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void blowgun$holdAtMouth(AbstractClientPlayer player, float partial, float pitch, InteractionHand hand, float attack, ItemStack stack, float equip, PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (hand != InteractionHand.MAIN_HAND || !stack.is(BlowgunMod.BLOWGUN) || player.isUsingItem() || player.isScoping()) return;
        ci.cancel();
        int sign = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        pose.pushPose();
        pose.translate(0.18f * sign, -0.24f, -0.85f);
        pose.scale(0.6f, 0.6f, 0.6f);
        renderItem(player, stack, ItemDisplayContext.NONE, pose, collector, light);
        pose.popPose();
    }
}