package dev.bambooblowgun.mixin;
import dev.bambooblowgun.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.storage.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
public abstract class VenomLivingMixin implements VenomCarrier {
    @Unique private VenomState blowgun$state;
    @Inject(method="<clinit>",at=@At("TAIL")) private static void blowgun$registerData(CallbackInfo ci) { VenomState.initialize(); }
    public VenomState blowgun$venom() { if (blowgun$state == null) blowgun$state = new VenomState((LivingEntity)(Object)this); return blowgun$state; }
    @Inject(method="defineSynchedData", at=@At("TAIL"))
    private void blowgun$define(SynchedEntityData.Builder builder, CallbackInfo ci) { builder.define(VenomState.STACKS, 0); builder.define(VenomState.LOCKED, false); }
    @Inject(method="tick", at=@At("TAIL")) private void blowgun$tick(CallbackInfo ci) { blowgun$venom().tick(); }
    @ModifyVariable(method="hurtServer", at=@At("HEAD"), argsOnly=true, ordinal=0)
    private float blowgun$amplify(float amount) { return blowgun$venom().amplify(amount); }
    @Inject(method="addAdditionalSaveData",at=@At("TAIL")) private void blowgun$save(ValueOutput out, CallbackInfo ci) { blowgun$venom().save(out); }
    @Inject(method="readAdditionalSaveData",at=@At("TAIL")) private void blowgun$load(ValueInput in, CallbackInfo ci) { blowgun$venom().load(in); }
}
