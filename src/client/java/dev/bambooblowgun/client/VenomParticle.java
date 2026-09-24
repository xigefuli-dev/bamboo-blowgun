package dev.bambooblowgun.client;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
public final class VenomParticle extends SingleQuadParticle {
    private final float initialSize;
    private final boolean drop;
    private final SpriteSet sprites;
    public VenomParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites,int color,boolean drop) {
        super(level,x,y,z,vx,vy,vz,sprites.first());
        this.drop=drop;this.sprites=sprites;
        setColor(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f);
        lifetime=drop?18:48; gravity=drop?0.12f:-0.003f; friction=0.93f;
        quadSize=initialSize=drop?0.055f:0.8f+random.nextFloat()*0.3f;
        alpha=drop?0.9f:0.02f;
        xd=vx;yd=vy;zd=vz;
    }
    @Override public void tick() { super.tick(); if(!drop) { float life=(float)age/lifetime;alpha=0.5f*Math.min(1f,age/5f)*(1f-life);quadSize=initialSize*(1f+life*0.3f);setSpriteFromAge(sprites); } }
    @Override protected Layer getLayer() { return Layer.TRANSLUCENT; }
    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites,int color,boolean drop) { return (type,level,x,y,z,vx,vy,vz,random) -> new VenomParticle(level,x,y,z,vx,vy,vz,sprites,color,drop); }
}
