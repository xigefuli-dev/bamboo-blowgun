package dev.bambooblowgun.client;
import dev.bambooblowgun.BlowgunMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class BlowgunClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        var particles=net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance();
        particles.register(dev.bambooblowgun.VenomParticles.PURPLE,sprites -> VenomParticle.provider(sprites,0x7130A5,false));
        particles.register(dev.bambooblowgun.VenomParticles.MAGENTA,sprites -> VenomParticle.provider(sprites,0xBF269F,false));
        particles.register(dev.bambooblowgun.VenomParticles.RED,sprites -> VenomParticle.provider(sprites,0xD51B35,false));
        particles.register(dev.bambooblowgun.VenomParticles.DROP,sprites -> VenomParticle.provider(sprites,0x4D086E,true));
        EntityRendererRegistry.register(BlowgunMod.DART_ENTITY, DartRenderer::new);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(BlowgunTrigger::tick);
    }
}