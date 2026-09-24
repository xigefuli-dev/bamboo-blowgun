package dev.bambooblowgun.mixin;
import dev.bambooblowgun.BlowgunMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Enchantment.class)
public abstract class BlowgunVanillaEnchantmentMixin {
    @Unique private boolean blowgun$allowed(ItemStack stack) {
        if(!stack.is(BlowgunMod.BLOWGUN))return false;
        var self=(Enchantment)(Object)this;
        var contents=self.description().getContents();
        if(contents instanceof TranslatableContents translated && translated.getKey().startsWith("enchantment.bamboo_blowgun."))return true;
        return !self.getEffects(EnchantmentEffectComponents.ITEM_DAMAGE).isEmpty()
            || !self.getEffects(EnchantmentEffectComponents.REPAIR_WITH_XP).isEmpty()
            || self.effects().has(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME);
    }
    @Inject(method="isPrimaryItem",at=@At("HEAD"),cancellable=true)
    private void blowgun$primary(ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        if(stack.is(BlowgunMod.BLOWGUN))cir.setReturnValue(blowgun$allowed(stack));
    }
    @Inject(method="isSupportedItem",at=@At("HEAD"),cancellable=true)
    private void blowgun$supported(ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        if(stack.is(BlowgunMod.BLOWGUN))cir.setReturnValue(blowgun$allowed(stack));
    }
}
