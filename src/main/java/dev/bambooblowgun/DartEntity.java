package dev.bambooblowgun;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public class DartEntity extends AbstractArrow implements ItemSupplier {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> LIGHTNESS = net.minecraft.network.syncher.SynchedEntityData.defineId(DartEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> LIMPING = net.minecraft.network.syncher.SynchedEntityData.defineId(DartEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> VENOM = net.minecraft.network.syncher.SynchedEntityData.defineId(DartEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(VENOM, false); builder.define(LIGHTNESS, 0); builder.define(LIMPING, 0); }
    public boolean isVenom() { return getEntityData().get(VENOM); }
    public void setVenom(boolean venom) { getEntityData().set(VENOM, venom); }
    public int getLimpingLevel() { return getEntityData().get(LIMPING); }
    public void setLimpingLevel(int level) { getEntityData().set(LIMPING, Math.clamp(level,0,3)); }
    public void setLightnessLevel(int level) { getEntityData().set(LIGHTNESS, Math.clamp(level,0,3)); }
    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out) { super.addAdditionalSaveData(out); out.putBoolean("VenomDart", isVenom()); out.putInt("BlowgunLightness",getEntityData().get(LIGHTNESS)); out.putInt("BlowgunLimping",getLimpingLevel()); }
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in) { super.readAdditionalSaveData(in); getEntityData().set(VENOM,in.getBooleanOr("VenomDart",false)); getEntityData().set(LIGHTNESS,Math.clamp(in.getIntOr("BlowgunLightness",0),0,3)); getEntityData().set(LIMPING,Math.clamp(in.getIntOr("BlowgunLimping",0),0,3)); }
    @Override public void tick() {
        super.tick();
        if (isVenom() && !isInGround() && level().isClientSide()) {
            for (int i=0;i<2;i++) level().addParticle(VenomParticles.DROP, getX()-getDeltaMovement().x*i*0.4, getY()-getDeltaMovement().y*i*0.4, getZ()-getDeltaMovement().z*i*0.4, 0, -0.015, 0);
        }
    }

    public DartEntity(EntityType<? extends DartEntity> type, Level level) { super(type, level); }
    public DartEntity(Level level, LivingEntity owner, ItemStack ammo, ItemStack weapon) {
        super(BlowgunMod.DART_ENTITY, owner, level, ammo, weapon);
        getEntityData().set(VENOM, ammo.is(BlowgunMod.VENOM_DART));
    }
    @Override protected double getDefaultGravity() { return BlowgunMod.DART_GRAVITY - getEntityData().get(LIGHTNESS) * 0.03; }
    @Override protected ItemStack getDefaultPickupItem() { return new ItemStack(isVenom() ? BlowgunMod.VENOM_DART : BlowgunMod.DART); }
    @Override public ItemStack getItem() { return getDefaultPickupItem(); }
}