package dev.bambooblowgun;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class BlowgunGameTests {
    private ServerPlayer player(GameTestHelper h) {
        var profile = new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "blowgun-test");
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false);
        ServerPlayer p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), profile, cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection, p, cookie);
        p.setGameMode(GameType.SURVIVAL);
        p.getInventory().clearContent();
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BlowgunMod.BLOWGUN));
        p.setPos(h.absoluteVec(new Vec3(1, 2, 1)));
        return p;
    }
    @GameTest public void consumesExactlyOneAndRejectsCooldown(GameTestHelper h) {
        var p = player(h);
        p.getInventory().setItem(1, new ItemStack(BlowgunMod.DART, 3));
        BlowgunMod.fire(p);
        h.assertTrue(p.getInventory().getItem(1).getCount() == 2, "one shot must consume exactly one dart");
        h.assertTrue(p.getCooldowns().isOnCooldown(p.getMainHandItem()), "successful shot must start cooldown");
        BlowgunMod.fire(p);
        h.assertTrue(p.getInventory().getItem(1).getCount() == 2, "duplicate request during cooldown must not consume ammo");
        h.succeed();
    }
    @GameTest public void emptyAndWrongAmmoDoNotFire(GameTestHelper h) {
        var p = player(h);
        BlowgunMod.fire(p);
        h.assertFalse(p.getCooldowns().isOnCooldown(p.getMainHandItem()), "empty shot must not start cooldown");
        p.getInventory().setItem(1, new ItemStack(Items.ARROW, 3));
        BlowgunMod.fire(p);
        h.assertTrue(p.getInventory().getItem(1).getCount() == 3, "ordinary arrows must not be consumed");
        h.assertFalse(p.getCooldowns().isOnCooldown(p.getMainHandItem()), "wrong ammo must not fire");
        h.succeed();
    }
    @GameTest public void offhandAmmoAndLastDart(GameTestHelper h) {
        var p = player(h);
        p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(BlowgunMod.DART));
        BlowgunMod.fire(p);
        h.assertTrue(p.getOffhandItem().isEmpty(), "offhand last dart must be consumed");
        h.assertTrue(p.getCooldowns().isOnCooldown(p.getMainHandItem()), "offhand ammo must fire");
        h.succeed();
    }
    @GameTest public void wrongWeaponAndSpectatorDoNotConsume(GameTestHelper h) {
        var p = player(h);
        p.getInventory().setItem(1, new ItemStack(BlowgunMod.DART, 3));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        BlowgunMod.fire(p);
        h.assertTrue(p.getInventory().getItem(1).getCount() == 3, "stale request after weapon switch must not fire");
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BlowgunMod.BLOWGUN));
        p.setGameMode(GameType.SPECTATOR);
        BlowgunMod.fire(p);
        h.assertTrue(p.getInventory().getItem(1).getCount() == 3, "spectators must not consume ammo");
        h.succeed();
    }
    @GameTest public void dartDropsFasterThanVanillaArrow(GameTestHelper h) {
        var dart = new DartEntity(BlowgunMod.DART_ENTITY, h.getLevel());
        var arrow = new Arrow(EntityTypes.ARROW, h.getLevel());
        Vec3 pos = h.absoluteVec(new Vec3(1, 20, 1));
        dart.setPos(pos); arrow.setPos(pos);
        dart.setDeltaMovement(0.1, 0, 0); arrow.setDeltaMovement(0.1, 0, 0);
        for (int i = 0; i < 5; i++) { dart.tick(); arrow.tick(); }
        double dartDrop = pos.y - dart.getY();
        double arrowDrop = pos.y - arrow.getY();
        h.assertTrue(arrowDrop > 0, "vanilla control must fall");
        h.assertTrue(dartDrop > arrowDrop * 2.8, "actual dart ticks must drop about three times faster");
        h.succeed();
    }
    private static class ImpactDart extends DartEntity {
        ImpactDart(net.minecraft.world.level.Level level) { super(BlowgunMod.DART_ENTITY, level); }
        void hit(net.minecraft.world.entity.Entity target) { super.onHitEntity(new net.minecraft.world.phys.EntityHitResult(target)); }
    }
    private static class ImpactArrow extends Arrow {
        ImpactArrow(net.minecraft.world.level.Level level) { super(EntityTypes.ARROW, level); }
        void hit(net.minecraft.world.entity.Entity target) { super.onHitEntity(new net.minecraft.world.phys.EntityHitResult(target)); }
    }
    @GameTest public void actualImpactDamageAndKnockbackRatios(GameTestHelper h) {
        var control = h.spawn(EntityTypes.COW, 1, 2, 1);
        var target = h.spawn(EntityTypes.COW, 1, 2, 3);
        control.setNoAi(true); target.setNoAi(true);
        control.setOnGround(true); target.setOnGround(true);
        control.setDeltaMovement(Vec3.ZERO); target.setDeltaMovement(Vec3.ZERO);
        float controlHp = control.getHealth(), targetHp = target.getHealth();
        var arrow = new ImpactArrow(h.getLevel());
        var dart = new ImpactDart(h.getLevel());
        arrow.setDeltaMovement(1.8, 0, 0); dart.setDeltaMovement(1.8, 0, 0);
        arrow.hit(control); dart.hit(target);
        float oldDamage = controlHp - control.getHealth(), newDamage = targetHp - target.getHealth();
        h.assertTrue(oldDamage > 0, "control arrow must damage target");
        h.assertTrue(Math.abs(newDamage / oldDamage - 0.95) < 0.0001, "actual health damage must decrease exactly 5 percent");
        double oldKnockback = control.getDeltaMovement().horizontalDistance();
        double newKnockback = target.getDeltaMovement().horizontalDistance();
        h.assertTrue(oldKnockback > 0, "control must knock back target");
        h.assertTrue(Math.abs(newKnockback / oldKnockback - 0.75) < 0.0001, "actual horizontal knockback must decrease 25 percent");
        h.assertTrue(Math.abs(target.getDeltaMovement().y / control.getDeltaMovement().y - 0.75) < 0.0001, "grounded upward knockback must decrease 25 percent");
        h.succeed();
    }
    @GameTest public void fireRateAllowsCompleteSound(GameTestHelper h) {
        var cadence = new ShotCadence();
        int ticks = 0;
        for (int i = 0; i < 41; i++) {
            int delay = cadence.nextCooldown();
            h.assertTrue(delay == 14, "each cooldown must allow the 0.64 second firing sound to finish");
            ticks += delay;
        }
        h.assertTrue(ticks == 574, "41 shots must require 574 ticks at 0.7 seconds per shot");
        h.succeed();
    }
    @GameTest public void venomStagesDecayAndMilk(GameTestHelper h) {
        var target=EntityTypes.IRON_GOLEM.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        target.setInvulnerable(true);
        var v=VenomState.of(target);
        v.hit(); h.assertTrue(v.stage()==1,"first hit must activate stage one");
        for(int i=0;i<4;i++)v.hit(); h.assertTrue(v.stage()==2,"five hits must activate stage two");
        new ItemStack(Items.MILK_BUCKET).finishUsingItem(h.getLevel(),target);h.assertTrue(v.stacks()==5,"milk effect removal must not clear custom venom");
        for(int i=0;i<79;i++)v.tick();h.assertTrue(v.stacks()==5,"no early decay");
        v.tick();h.assertTrue(v.stacks()==4 && v.stage()==1,"80 ticks decays exactly one and drops stage");
        for(int i=0;i<8;i++)v.hit();h.assertTrue(v.stage()==3 && v.locked(),"12 stacks lock stage three");
        for(int i=0;i<960;i++)v.tick();h.assertTrue(v.stacks()==12 && v.stage()==3,"red venom locks twelve stacks permanently");
        v.hit();h.assertTrue(v.stacks()==12,"further hits cannot exceed twelve locked stacks");
        target.setHealth(0);v.tick();h.assertTrue(v.stage()==0,"death clears venom");h.succeed();
    }
    @GameTest public void venomDamageAndSaveReload(GameTestHelper h) {
        var target=EntityTypes.IRON_GOLEM.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        var v=VenomState.of(target);for(int i=0;i<5;i++)v.hit();
        float before=target.getHealth();target.hurtServer(h.getLevel(),target.damageSources().magic(),4);
        h.assertTrue(Math.abs(before-target.getHealth()-4.6f)<0.001,"five stacks amplify four damage to 4.6");
        target.invulnerableTime=0;before=target.getHealth();for(int i=0;i<20;i++)v.tick();
        h.assertTrue(Math.abs(before-target.getHealth()-1)<0.001,"stage two deals one damage without recursive amplification");
        for(int i=0;i<7;i++)v.hit();
        var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess());
        target.saveWithoutId(out);
        var restored=EntityTypes.IRON_GOLEM.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        restored.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
        h.assertTrue(VenomState.of(restored).stacks()==12 && VenomState.of(restored).locked(),"entity save reload must preserve permanent venom");h.succeed();
    }
    @GameTest public void venomBatchCraftingConsumesSixteen(GameTestHelper h) {
        var p=player(h);var grid=p.inventoryMenu.getCraftSlots();
        grid.setItem(0,new ItemStack(BlowgunMod.DART,32));
        grid.setItem(1,net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,net.minecraft.world.item.alchemy.Potions.POISON));
        grid.setItem(2,net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,net.minecraft.world.item.alchemy.Potions.HARMING));
        h.assertTrue(VenomRecipe.INSTANCE.matches(grid.asCraftInput(),h.getLevel()),"correct ingredients must match");
        var slot=p.inventoryMenu.getResultSlot();
        h.assertTrue(slot.getItem().is(BlowgunMod.VENOM_DART) && slot.getItem().getCount()==16,"recipe manager must produce sixteen");
        var result=slot.remove(16);slot.onTake(p,result);
        h.assertTrue(grid.getItem(0).getCount()==16,"one craft must consume sixteen darts, not one");
        h.assertTrue(grid.getItem(1).is(Items.GLASS_BOTTLE) && grid.getItem(2).is(Items.GLASS_BOTTLE),"both bottles must be returned");
        grid.setItem(0,new ItemStack(BlowgunMod.DART,15));
        grid.setItem(1,net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,net.minecraft.world.item.alchemy.Potions.POISON));
        grid.setItem(2,net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,net.minecraft.world.item.alchemy.Potions.HARMING));
        h.assertFalse(VenomRecipe.INSTANCE.matches(grid.asCraftInput(),h.getLevel()),"fifteen darts must not match");
        h.assertTrue(slot.getItem().isEmpty(),"insufficient darts must have no craft output");h.succeed();
    }

    @GameTest public void venomCrafterAndCachedCounts(GameTestHelper h) throws Exception {
        var pos=new net.minecraft.core.BlockPos(1,2,1);h.setBlock(pos,net.minecraft.world.level.block.Blocks.CRAFTER);
        var absolute=h.absolutePos(pos);
        var crafter=(net.minecraft.world.level.block.entity.CrafterBlockEntity)h.getLevel().getBlockEntity(absolute);
        crafter.setItem(0,new ItemStack(BlowgunMod.DART,15));
        crafter.setItem(1,net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,net.minecraft.world.item.alchemy.Potions.POISON));
        crafter.setItem(2,net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,net.minecraft.world.item.alchemy.Potions.HARMING));
        h.assertTrue(net.minecraft.world.level.block.CrafterBlock.getPotentialResults(h.getLevel(),crafter.asCraftInput()).isEmpty(),"crafter must reject fifteen");
        crafter.setItem(0,new ItemStack(BlowgunMod.DART,32));
        h.assertTrue(net.minecraft.world.level.block.CrafterBlock.getPotentialResults(h.getLevel(),crafter.asCraftInput()).isPresent(),"cached miss must not reject a replenished batch");
        var method=net.minecraft.world.level.block.CrafterBlock.class.getDeclaredMethod("dispenseFrom",net.minecraft.world.level.block.state.BlockState.class,net.minecraft.server.level.ServerLevel.class,net.minecraft.core.BlockPos.class);
        method.setAccessible(true);method.invoke(net.minecraft.world.level.block.Blocks.CRAFTER,h.getLevel().getBlockState(absolute),h.getLevel(),absolute);
        h.assertTrue(crafter.getItem(0).getCount()==16,"automated crafter must also consume sixteen darts");
        h.assertTrue(crafter.getItem(1).isEmpty() && crafter.getItem(2).isEmpty(),"automated crafter must consume both potions");h.succeed();
    }
    @GameTest public void venomAmmoPriorityAndProjectileSave(GameTestHelper h) {
        var p=player(h);p.getInventory().setItem(1,new ItemStack(BlowgunMod.DART,10));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(BlowgunMod.VENOM_DART,2));
        BlowgunMod.fire(p);
        h.assertTrue(p.getOffhandItem().getCount()==1 && p.getInventory().getItem(1).getCount()==10,"offhand selects venom ammo");
        var dart=new DartEntity(h.getLevel(),p,new ItemStack(BlowgunMod.VENOM_DART),new ItemStack(BlowgunMod.BLOWGUN));
        var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess());dart.saveWithoutId(out);
        var restored=new DartEntity(BlowgunMod.DART_ENTITY,h.getLevel());restored.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
        h.assertTrue(restored.isVenom() && restored.getItem().is(BlowgunMod.VENOM_DART) && restored.getPickupItemStackOrigin().is(BlowgunMod.VENOM_DART),"venom model and pickup must survive save reload");h.succeed();
    }

    private static class VenomImpactDart extends DartEntity {
        VenomImpactDart(net.minecraft.world.entity.LivingEntity owner) { super(owner.level(),owner,new ItemStack(BlowgunMod.VENOM_DART),new ItemStack(BlowgunMod.BLOWGUN)); }
        void hit(net.minecraft.world.entity.Entity target) { super.onHitEntity(new net.minecraft.world.phys.EntityHitResult(target)); }
    }
    @GameTest public void actualVenomImpactStacksAfterDamage(GameTestHelper h) {
        var p=player(h);var target=EntityTypes.IRON_GOLEM.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        var normal=new ImpactDart(h.getLevel());normal.setDeltaMovement(1.8,0,0);normal.hit(target);
        h.assertTrue(VenomState.of(target).stacks()==0,"ordinary dart must not apply venom");
        target.invulnerableTime=0;float before=target.getHealth();
        var first=new VenomImpactDart(p);first.setDeltaMovement(1.8,0,0);first.hit(target);
        float firstDamage=before-target.getHealth();
        h.assertTrue(Math.abs(firstDamage-3.8f)<0.001,"venom first impact retains ordinary dart damage");
        h.assertTrue(VenomState.of(target).stacks()==1,"accepted venom impact must add one stack");
        target.invulnerableTime=0;before=target.getHealth();
        var second=new VenomImpactDart(p);second.setDeltaMovement(1.8,0,0);second.hit(target);
        h.assertTrue(Math.abs((before-target.getHealth())/firstDamage-1.03)<0.001,"next impact uses existing stack only");
        h.assertTrue(VenomState.of(target).stacks()==2,"second venom impact must add one more stack");
        target.setInvulnerable(true);target.invulnerableTime=0;
        var blocked=new VenomImpactDart(p);blocked.setDeltaMovement(1.8,0,0);blocked.hit(target);
        h.assertTrue(VenomState.of(target).stacks()==2,"rejected damage must not apply venom");h.succeed();
    }

    private int balanceShots(GameTestHelper h, boolean venom, int interval, float health) {
        var p=player(h);
        var target=EntityTypes.IRON_GOLEM.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        target.setNoAi(true);target.setNoGravity(true);target.setPos(h.absoluteVec(new Vec3(1,4,1)));target.setHealth(health);
        int shots=0;
        while(target.isAlive() && shots<80) {
            if(venom) {var dart=new VenomImpactDart(p);dart.setBaseDamage(2);dart.setDeltaMovement(1.8,0,0);dart.hit(target);}
            else {var dart=new ImpactDart(h.getLevel());dart.setBaseDamage(2);dart.setDeltaMovement(1.8,0,0);dart.hit(target);}
            shots++;
            for(int tick=0;tick<interval && target.isAlive();tick++) {target.setDeltaMovement(Vec3.ZERO);target.tick();}
        }
        return shots;
    }
    @GameTest public void sustainedBalanceComparison(GameTestHelper h) {
        int ordinary=balanceShots(h,false,14,100),venom=balanceShots(h,true,14,100);
        int ordinarySmall=balanceShots(h,false,14,20),venomSmall=balanceShots(h,true,14,20);
        System.out.println("BLOWGUN_BALANCE golem ordinary="+ordinary+" venom="+venom+"; 20HP ordinary="+ordinarySmall+" venom="+venomSmall);
        h.assertTrue(ordinary==27,"ordinary close-range golem baseline must remain 27");
        h.assertTrue(venom>20 && venom<ordinary,"venom keeps an upgrade advantage with reduced extra damage");
        h.assertTrue(venomSmall<=ordinarySmall,"the upgraded dart must not become weaker on small targets");h.succeed();
    }

    @GameTest public void venomTicksIgnoreArmorAndOwnAmplification(GameTestHelper h) {
        int[] stacks={1,5,12};float[] expected={0.5f,1f,4f};
        for(int i=0;i<stacks.length;i++) {
            var target=EntityTypes.IRON_GOLEM.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(30);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS).setBaseValue(20);
            var v=VenomState.of(target);for(int n=0;n<stacks[i];n++)v.hit();
            float before=target.getHealth();for(int tick=0;tick<20;tick++)v.tick();
            h.assertTrue(Math.abs(before-target.getHealth()-expected[i])<0.001,"each poison stage must ignore armor and must not amplify its own damage");
        }
        h.succeed();
    }
}