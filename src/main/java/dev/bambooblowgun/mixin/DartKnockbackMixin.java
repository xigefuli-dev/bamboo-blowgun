package dev.bambooblowgun.mixin;
import com.llamalad7.mixinextras.sugar.Local;
import dev.bambooblowgun.BlowgunMod;
import dev.bambooblowgun.DartEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
@Mixin(LivingEntity.class)
public abstract class DartKnockbackMixin {
    @ModifyArg(method = "dealDefaultKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDDLnet/minecraft/world/damagesource/DamageSource;F)V"), index = 0)
    private double blowgun$scaleKnockback(double power, @Local(argsOnly = true) DamageSource source) {
        return source.getDirectEntity() instanceof DartEntity ? power * BlowgunMod.KNOCKBACK_MULTIPLIER : power;
    }
}