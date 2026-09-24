package dev.bambooblowgun;
/** Vanilla Quick Charge shaves one tick per level; level III bottoms out at 11 ticks (0.55 s). */
public final class ShotCadence {
    public int nextCooldown(int quickChargeLevel) { return Math.max(11,14-Math.clamp(quickChargeLevel,0,3)); }
}
