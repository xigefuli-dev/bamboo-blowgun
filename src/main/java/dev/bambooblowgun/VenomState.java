package dev.bambooblowgun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.*;
public final class VenomState {
    public static final EntityDataAccessor<Integer> STACKS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> LOCKED = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    public static void initialize() {}
    private final LivingEntity target;
    private int decayTicks, damageTicks, limpingLevel, appliedLimpingLevel = -1;
    private static final net.minecraft.resources.Identifier LIMPING_MODIFIER_ID = BlowgunMod.id("venom_limping");
    public boolean dealingPoisonDamage;
    public VenomState(LivingEntity target) { this.target = target; }
    public static VenomState of(LivingEntity target) { return ((VenomCarrier)target).blowgun$venom(); }
    public int stacks() { return target.getEntityData().get(STACKS); }
    public boolean locked() { return target.getEntityData().get(LOCKED); }
    public int stage() { return locked() ? 3 : stacks() >= 5 ? 2 : stacks() > 0 ? 1 : 0; }
    public void hit() { hit(0); }
    public void hit(int limping) {
        if (!target.isAlive() || locked()) return;
        if (stacks() == 0 && !locked()) { decayTicks = 0; damageTicks = 0; limpingLevel = 0; }
        limpingLevel = Math.max(limpingLevel, Math.clamp(limping,0,3));
        int next = stacks() + 1;
        target.getEntityData().set(STACKS, next);
        if (next >= 12) target.getEntityData().set(LOCKED, true);
    }
    public float amplify(float damage) { return dealingPoisonDamage ? damage : damage * (1.0f + stacks() * 0.03f); }
    public void clear() { target.getEntityData().set(STACKS, 0); target.getEntityData().set(LOCKED, false); decayTicks = damageTicks = limpingLevel = 0; updateLimpingModifier(); }
    private void updateLimpingModifier() {
        var attribute=target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if(attribute==null)return;
        int level=stacks()>0 ? limpingLevel : 0;
        if(level==appliedLimpingLevel)return;
        attribute.removeModifier(LIMPING_MODIFIER_ID);
        if(level>0) attribute.addOrUpdateTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(LIMPING_MODIFIER_ID,-0.05*level,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        appliedLimpingLevel=level;
    }
    public void tick() {
        if (!(target.level() instanceof ServerLevel level)) return;
        if (!target.isAlive()) { clear(); return; }
        updateLimpingModifier();
        int stage = stage();
        if (stage == 0) return;
        if (++damageTicks >= 20) {
            damageTicks = 0;
            dealingPoisonDamage = true;
            try { target.hurtServer(level, target.damageSources().magic(), stage == 3 ? 4f : stage == 2 ? 1f : 0.5f); }
            finally { dealingPoisonDamage = false; }
        }
        if (target.isAlive() && target.tickCount % 3 == 0) {
            var particle = stage == 3 ? VenomParticles.RED : stage == 2 ? VenomParticles.MAGENTA : VenomParticles.PURPLE;
            level.sendParticles(particle, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 4 + stage * 2,
                target.getBbWidth() * 0.38, target.getBbHeight() * 0.25, target.getBbWidth() * 0.38, 0.008);
        }
        if (!locked() && ++decayTicks >= 80) {
            decayTicks = 0;
            if (stacks() > 0) target.getEntityData().set(STACKS, stacks() - 1);
        }
    }
    public void save(ValueOutput out) { out.putInt("BlowgunVenom", stacks()); out.putBoolean("BlowgunVenomLocked", locked()); out.putInt("BlowgunVenomDecay", decayTicks); out.putInt("BlowgunVenomPulse", damageTicks); out.putInt("BlowgunLimpingLevel",limpingLevel); }
    public void load(ValueInput in) { target.getEntityData().set(STACKS, Math.clamp(in.getIntOr("BlowgunVenom",0),0,12)); target.getEntityData().set(LOCKED,in.getBooleanOr("BlowgunVenomLocked",false)); if (locked() || stacks() == 12) { target.getEntityData().set(STACKS,12); target.getEntityData().set(LOCKED,true); } decayTicks = Math.clamp(in.getIntOr("BlowgunVenomDecay",0),0,79); damageTicks = Math.clamp(in.getIntOr("BlowgunVenomPulse",0),0,19); limpingLevel=Math.clamp(in.getIntOr("BlowgunLimpingLevel",0),0,3); appliedLimpingLevel=-1; }
}
