package dev.bambooblowgun;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
public final class BlowgunSounds {
    public static final SoundEvent FIRE = register("fire");
    public static final SoundEvent FIRE_QUICK_CHARGE_1 = register("fire_quick_charge_1");
    public static final SoundEvent FIRE_QUICK_CHARGE_2 = register("fire_quick_charge_2");
    public static final SoundEvent FIRE_QUICK_CHARGE_3 = register("fire_quick_charge_3");

    public static final SoundEvent HIT = register("hit");
    private static SoundEvent register(String name) {
        var id = BlowgunMod.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
    public static void initialize() {}
    public static void fire(Player shooter, int quickChargeLevel) {
        if (shooter.isSilent()) return;

        // Raising Minecraft's playback pitch speeds up the complete sound; the clip is
        // never shortened or cut off. These stepped increases fit each cadence.
        // Companion clips counter most of the pitch rise caused by the faster playback.
        SoundEvent sound = switch (Math.clamp(quickChargeLevel, 0, 3)) {
            case 1 -> FIRE_QUICK_CHARGE_1;
            case 2 -> FIRE_QUICK_CHARGE_2;
            case 3 -> FIRE_QUICK_CHARGE_3;
            default -> FIRE;
        };
        float playbackRate = switch (Math.clamp(quickChargeLevel, 0, 3)) {
            case 1 -> 1.03f;
            case 2 -> 1.08f;
            case 3 -> 1.17f;
            default -> 1.0f;
        };
        shooter.level().playSound(null, shooter, sound, SoundSource.PLAYERS, 0.8f, playbackRate);
    }
    public static void hit(Entity target) {
        if (!(target instanceof LivingEntity) || target.isSilent()) return;
        target.level().playSound(null, target, HIT, target.getSoundSource(), 0.8f, 1.0f);
    }
}
