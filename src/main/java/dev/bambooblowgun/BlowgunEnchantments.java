package dev.bambooblowgun;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.resources.Identifier;
public final class BlowgunEnchantments {
    public static final ResourceKey<Enchantment> QUICK_CHARGE = ResourceKey.create(
        Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("minecraft", "quick_charge"));
    public static final ResourceKey<Enchantment> LIGHTNESS=key("lightness"), LIMPING=key("limping"), VENOM_TIP=key("venom_tip");
    private static ResourceKey<Enchantment> key(String path){return ResourceKey.create(Registries.ENCHANTMENT,BlowgunMod.id(path));}
    public static int level(ItemStack stack,LivingEntity wearer,ResourceKey<Enchantment> key){
        var holder=wearer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
        return holder.map(value -> EnchantmentHelper.getItemEnchantmentLevel(value,stack)).orElse(0);
    }
}
