package dev.bambooblowgun;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public final class VenomRecipe extends CustomRecipe {
    public static final VenomRecipe INSTANCE = new VenomRecipe();
    public static final RecipeSerializer<VenomRecipe> SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BlowgunMod.id("venom_dart"), new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE)));
    public static void initialize() {}
    @Override public RecipeSerializer<VenomRecipe> getSerializer() { return SERIALIZER; }
    @Override public boolean matches(CraftingInput in, Level level) {
        int darts=0,poison=0,harm=0;
        for (var stack : in.items()) {
            if (stack.isEmpty()) continue;
            if (stack.is(BlowgunMod.DART) && stack.getCount() >= 16) { darts++; continue; }
            if (!stack.is(Items.POTION)) return false;
            var potion=stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (potion.is(Potions.POISON) || potion.is(Potions.LONG_POISON) || potion.is(Potions.STRONG_POISON)) poison++;
            else if (potion.is(Potions.HARMING) || potion.is(Potions.STRONG_HARMING)) harm++;
            else return false;
        }
        return darts==1 && poison==1 && harm==1;
    }
    @Override public ItemStack assemble(CraftingInput in) { return new ItemStack(BlowgunMod.VENOM_DART,16); }
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput in) {
        var out=NonNullList.withSize(in.size(),ItemStack.EMPTY);
        for(int i=0;i<in.size();i++) if(in.getItem(i).is(Items.POTION)) out.set(i,new ItemStack(Items.GLASS_BOTTLE));
        return out;
    }
}
