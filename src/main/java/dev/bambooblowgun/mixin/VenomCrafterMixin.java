package dev.bambooblowgun.mixin;
import dev.bambooblowgun.*;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Optional;
@Mixin(CrafterBlock.class)
public abstract class VenomCrafterMixin {
    @Inject(method="dispenseFrom",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/entity/CrafterBlockEntity;getItems()Lnet/minecraft/core/NonNullList;"))
    private void blowgun$consumeBatch(BlockState state,ServerLevel level,BlockPos pos,CallbackInfo ci,@Local CrafterBlockEntity crafter,@Local RecipeHolder<?> recipe) {
        if (recipe.value() instanceof VenomRecipe) for(var stack:crafter.getItems()) if(stack.is(BlowgunMod.DART)) stack.shrink(15);
    }
    // Vanilla's recipe cache ignores item counts; batch recipes must check the live count.
    @Inject(method="getPotentialResults",at=@At("HEAD"),cancellable=true)
    private static void blowgun$validateCount(ServerLevel level,CraftingInput input,CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
        if(input.items().stream().anyMatch(stack -> stack.is(BlowgunMod.DART)))
            cir.setReturnValue(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,level));
    }
}
