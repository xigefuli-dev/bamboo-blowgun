package dev.bambooblowgun.mixin;
import dev.bambooblowgun.DartEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
public abstract class DartHurtVoiceMixin {
    @Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
    private void blowgun$replaceHurtVoice(DamageSource source, CallbackInfo ci) {
        if (source.getDirectEntity() instanceof DartEntity dart && dart.isVenom()) ci.cancel();
    }
}