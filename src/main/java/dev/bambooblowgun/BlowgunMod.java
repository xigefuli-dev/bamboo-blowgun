package dev.bambooblowgun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BlowgunMod implements ModInitializer {
    public static final String MOD_ID = "bamboo_blowgun";
    public static final double DAMAGE_MULTIPLIER = 0.95;
    public static final double KNOCKBACK_MULTIPLIER = 0.75;
    private static final java.util.Set<ServerPlayer> HELD_TRIGGERS = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    private static final java.util.Map<ServerPlayer, ShotCadence> CADENCES = new java.util.WeakHashMap<>();
    public static final float SHOT_SPEED = 1.8f;
    public static final double DART_GRAVITY = 0.15;
    public static final Item BLOWGUN = Registry.register(BuiltInRegistries.ITEM,
        ResourceKey.create(Registries.ITEM, id("blowgun")), new Item(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, id("blowgun"))).durability(192).enchantable(15)));
    public static final Item DART = registerItem("dart", 64);
    public static final Item VENOM_DART = registerItem("venom_dart", 64);
    public static final EntityType<DartEntity> DART_ENTITY = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, id("dart"),
        EntityType.Builder.<DartEntity>of(DartEntity::new, MobCategory.MISC)
            .sized(0.2f, 0.2f).clientTrackingRange(4).updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, id("dart"))));

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
    private static Item registerItem(String name, int stackSize) {
        var key = ResourceKey.create(Registries.ITEM, id(name));
        return Registry.register(BuiltInRegistries.ITEM, key,
            new Item(new Item.Properties().setId(key).stacksTo(stackSize)));
    }

    @Override public void onInitialize() {
        BlowgunSounds.initialize();
        VenomParticles.initialize();
        VenomRecipe.initialize();
        PayloadTypeRegistry.serverboundPlay().register(FirePayload.TYPE, FirePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TriggerPayload.TYPE, TriggerPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TriggerPayload.TYPE, (payload, context) -> setTrigger(context.player(), payload.held()));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                if (HELD_TRIGGERS.contains(player)) fire(player);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(FirePayload.TYPE, (payload, context) -> fire(context.player()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(BLOWGUN);
            entries.accept(DART);
            entries.accept(VENOM_DART);
        });
    }

    public static void setTrigger(ServerPlayer player, boolean held) {
        if (held) {
            HELD_TRIGGERS.add(player);
            fire(player);
        } else {
            HELD_TRIGGERS.remove(player);
        }
    }

    /** Server-authoritative: clients never choose ammunition, velocity or damage. */
    public static void fire(ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        if (!player.isAlive() || player.isSpectator() || player.isUsingItem()
            || !weapon.is(BLOWGUN) || player.getCooldowns().isOnCooldown(weapon)) return;
        ItemStack offhand = player.getOffhandItem();
        ItemStack ammo = offhand.is(DART) || offhand.is(VENOM_DART) ? offhand : ItemStack.EMPTY;
        for (int slot = 0; ammo.isEmpty() && slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if ((candidate.is(DART) || candidate.is(VENOM_DART)) && !candidate.isEmpty()) { ammo = candidate; break; }
        }
        boolean creative = player.getAbilities().instabuild;
        if (ammo.isEmpty() && !creative) return;
        var level = player.level();
        var dart = new DartEntity(level, player, ammo.isEmpty() ? new ItemStack(DART) : ammo.copyWithCount(1), weapon.copy());
        dart.pickup = creative ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        dart.setBaseDamage(2.0);
        int lightness = BlowgunEnchantments.level(weapon, player, BlowgunEnchantments.LIGHTNESS);
        int limping = BlowgunEnchantments.level(weapon, player, BlowgunEnchantments.LIMPING);
        int quickCharge = BlowgunEnchantments.level(weapon, player, BlowgunEnchantments.QUICK_CHARGE);
        boolean venomTip = BlowgunEnchantments.level(weapon, player, BlowgunEnchantments.VENOM_TIP) > 0;
        if (venomTip && ammo.is(DART)) dart.setVenom(true);
        dart.setLightnessLevel(lightness);
        dart.setLimpingLevel(limping);
        dart.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, SHOT_SPEED, 0.6f);
        if (!level.addFreshEntity(dart)) return;
        if (!creative) {
            ammo.shrink(1);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
        player.getCooldowns().addCooldown(weapon, CADENCES.computeIfAbsent(player, ignored -> new ShotCadence()).nextCooldown(quickCharge));
        weapon.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        BlowgunSounds.fire(player, quickCharge);
    }
}
