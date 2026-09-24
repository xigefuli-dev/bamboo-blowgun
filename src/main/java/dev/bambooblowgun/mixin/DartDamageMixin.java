package dev.bambooblowgun.mixin;
import dev.bambooblowgun.BlowgunMod;
import dev.bambooblowgun.DartEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
@Mixin(AbstractArrow.class)
public abstract class DartDamageMixin {
    // Scale after vanilla's ceil, so a 4 HP hit actually becomes 3.8 HP.
    @ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
    private float blowgun$scaleImpact(float damage) {
        return (Object)this instanceof DartEntity ? (float)(damage * BlowgunMod.DAMAGE_MULTIPLIER) : damage;
    }
}