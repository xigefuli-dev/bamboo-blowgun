package dev.bambooblowgun.mixin;
import dev.bambooblowgun.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
@Mixin(ResultSlot.class)
public abstract class VenomCraftingMixin {
    @Shadow @Final private CraftingContainer craftSlots;
    @Unique private boolean blowgun$batch;
    @Inject(method="onTake",at=@At("HEAD"))
    private void blowgun$batch(Player player, ItemStack result, CallbackInfo ci) { blowgun$batch = result.is(BlowgunMod.VENOM_DART) && VenomRecipe.INSTANCE.matches(craftSlots.asCraftInput(),player.level()); }
    @WrapOperation(method="onTake",at=@At(value="INVOKE",target="Lnet/minecraft/world/inventory/CraftingContainer;removeItem(II)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack blowgun$consume(CraftingContainer grid,int slot,int count,Operation<ItemStack> original) { return original.call(grid,slot,blowgun$batch && grid.getItem(slot).is(BlowgunMod.DART) ? 16 : count); }
}
