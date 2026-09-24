package dev.bambooblowgun.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.bambooblowgun.BlowgunSounds;
import dev.bambooblowgun.DartEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(AbstractArrow.class)
public abstract class DartSoundMixin {
    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean blowgun$targetSound(Entity target, DamageSource source, float damage, Operation<Boolean> original) {
        boolean accepted = original.call(target, source, damage);
        if (accepted && (Object)this instanceof DartEntity dart && dart.isVenom() && !dart.level().isClientSide()) {
            BlowgunSounds.hit(target);
            if (target instanceof net.minecraft.world.entity.LivingEntity living) dev.bambooblowgun.VenomState.of(living).hit(dart.getLimpingLevel());
        }
        return accepted;
    }
    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void blowgun$removeArrowImpact(AbstractArrow arrow, SoundEvent sound, float volume, float pitch, Operation<Void> original) {
        if (!(arrow instanceof DartEntity dart) || !dart.isVenom()) original.call(arrow, sound, volume, pitch);
    }
}