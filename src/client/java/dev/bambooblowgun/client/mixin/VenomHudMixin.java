package dev.bambooblowgun.client.mixin;
import dev.bambooblowgun.VenomState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Hud.class)
public abstract class VenomHudMixin {
    @Inject(method="extractRenderState",at=@At("HEAD"))
    private void blowgun$venomVeil(GuiGraphicsExtractor graphics,DeltaTracker delta,CallbackInfo ci) {
        var client=Minecraft.getInstance();
        if(client.player==null || !client.player.isAlive() || !client.options.getCameraType().isFirstPerson()) return;
        var venom=VenomState.of(client.player);int stage=venom.stage();if(stage==0)return;
        int rgb=stage==3?0xD51B35:stage==2?0xBF269F:0x7130A5;
        int w=graphics.guiWidth(),h=graphics.guiHeight();
        graphics.fill(0,0,w,h,((5+stage*3)<<24)|rgb);
        int edge=h/4;
        graphics.fillGradient(0,0,w,edge,((20+stage*10)<<24)|rgb,rgb);
        graphics.fillGradient(0,h-edge,w,h,rgb,((20+stage*10)<<24)|rgb);
        graphics.text(client.font,net.minecraft.network.chat.Component.translatable("hud.bamboo_blowgun.venom",stage,venom.stacks()),8,h-56,0xFFEEEEEE);
    }
}
