package dev.bambooblowgun.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.bambooblowgun.DartEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
public final class DartRenderer extends EntityRenderer<DartEntity, DartRenderer.State> {
    private final ItemModelResolver resolver;
    public DartRenderer(EntityRendererProvider.Context context) { super(context); resolver = context.getItemModelResolver(); }
    public static final class State extends EntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float pitch, yaw;
    }
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(DartEntity dart, State state, float partialTick) {
        super.extractRenderState(dart, state, partialTick);
        state.pitch = dart.getXRot(partialTick);
        state.yaw = dart.getYRot(partialTick);
        resolver.updateForNonLiving(state.item, dart.getItem(), ItemDisplayContext.NONE, dart);
    }
    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        // The solid model points along -Z; rotate into the projectile's actual flight direction.
        pose.mulPose(Axis.YP.rotationDegrees(180 + state.yaw));
        pose.mulPose(Axis.XP.rotationDegrees(state.pitch));
        pose.scale(0.65f, 0.65f, 0.65f);
        state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }
}