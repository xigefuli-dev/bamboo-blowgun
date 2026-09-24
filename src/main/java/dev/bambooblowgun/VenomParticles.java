package dev.bambooblowgun;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
public final class VenomParticles {
    public static final SimpleParticleType PURPLE = register("venom_purple"), MAGENTA = register("venom_magenta"), RED = register("venom_red"), DROP = register("venom_drop");
    private static SimpleParticleType register(String name) { return Registry.register(BuiltInRegistries.PARTICLE_TYPE, BlowgunMod.id(name), FabricParticleTypes.simple(true)); }
    public static void initialize() {}
}
