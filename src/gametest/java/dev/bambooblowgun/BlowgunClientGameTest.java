package dev.bambooblowgun;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
public class BlowgunClientGameTest implements FabricClientGameTest {
    private static class TestImpactDart extends DartEntity {
        TestImpactDart(net.minecraft.world.entity.LivingEntity owner) { super(owner.level(),owner,new ItemStack(BlowgunMod.VENOM_DART),new ItemStack(BlowgunMod.BLOWGUN)); }
        void hit(net.minecraft.world.entity.Entity target) { super.onHitEntity(new net.minecraft.world.phys.EntityHitResult(target)); }
    }
    @Override public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                p.setGameMode(GameType.SURVIVAL);
                p.getInventory().clearContent();
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BlowgunMod.BLOWGUN));
                p.getInventory().setItem(1, new ItemStack(BlowgunMod.DART, 32));
                p.containerMenu.broadcastChanges();
            });
            context.waitFor(c -> c.player != null && c.player.getMainHandItem().is(BlowgunMod.BLOWGUN)
                && c.player.getInventory().getItem(1).getCount() == 32);
            context.runOnClient(c -> { c.player.setXRot(-70); c.player.setYRot(0); });
            var sounds = new java.util.concurrent.CopyOnWriteArrayList<net.minecraft.client.resources.sounds.SoundInstance>();
            net.minecraft.client.sounds.SoundEventListener listener = (sound, event, range) -> sounds.add(sound);
            context.runOnClient(c -> c.getSoundManager().addListener(listener));
            context.waitTicks(3);
            context.getInput().pressMouse(0);
            context.waitFor(c -> c.player.getInventory().getItem(1).getCount() == 31);
            context.waitTicks(10);
            context.getInput().holdMouseFor(0, 50);
            context.waitTicks(3);
            final int[] remaining = new int[1];
            context.runOnClient(c -> {
                remaining[0] = c.player.getInventory().getItem(1).getCount();
                if (remaining[0] > 28 || remaining[0] < 27)
                    throw new AssertionError("held attack must repeatedly fire at the configured cadence: " + remaining[0]);
                for (String sound : new String[]{"fire", "hit"}) {
                    if (c.getSoundManager().getSoundEvent(BlowgunMod.id(sound)) == null)
                        throw new AssertionError("missing sound event " + sound);
                }
            });
            context.runOnClient(c -> {
                for (String id : new String[]{"fire"}) {
                    var sound = sounds.stream().filter(x -> x.getIdentifier().equals(BlowgunMod.id(id))).findFirst().orElseThrow(() -> new AssertionError("sound did not play: " + id));
                    if (Math.abs(sound.getX() - c.player.getX()) > 0.1 || Math.abs(sound.getZ() - c.player.getZ()) > 0.1)
                        throw new AssertionError("firing sound must originate at shooter");
                }
            });
            context.waitTicks(20);
            context.runOnClient(c -> {
                if (c.player.getInventory().getItem(1).getCount() != remaining[0])
                    throw new AssertionError("released attack must stop firing");
                c.player.setXRot(0);
            });
            context.waitTicks(5);
            context.takeScreenshot("blowgun-first-person-3d");
            context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(5);
            context.takeScreenshot("blowgun-mouth-pose");
            context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                var level = p.level();
                var mannequin = net.minecraft.world.entity.EntityTypes.MANNEQUIN.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                mannequin.setPos(p.getX(), p.getY(), p.getZ() + 3);
                mannequin.setYRot(90); mannequin.setYBodyRot(90); mannequin.setYHeadRot(90);
                mannequin.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(BlowgunMod.BLOWGUN));
                level.addFreshEntity(mannequin);
                var dart = new DartEntity(BlowgunMod.DART_ENTITY, level);
                dart.setPos(p.getX() - 0.35, p.getEyeY(), p.getZ() + 1.4);
                dart.setNoGravity(true); dart.setNoPhysics(true);
                dart.setYRot(90); dart.yRotO = 90;
                dart.setXRot(0); dart.xRotO = 0;
                level.addFreshEntity(dart);
            });
            context.waitTicks(8);
            context.takeScreenshot("blowgun-model-side-and-dart");
            final double[] targetPosition = new double[3];
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                var target = net.minecraft.world.entity.EntityTypes.COW.create(p.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                target.setPos(p.getX() + 4, p.getY(), p.getZ());
                target.setNoAi(true);
                p.level().addFreshEntity(target);
                targetPosition[0] = target.getX(); targetPosition[1] = target.getY(); targetPosition[2] = target.getZ();
            });
            context.waitTicks(5);
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                var target = p.level().getEntitiesOfClass(net.minecraft.world.entity.animal.cow.Cow.class, p.getBoundingBox().inflate(6)).getFirst();
                var dart = new TestImpactDart(p);
                dart.setDeltaMovement(0, 0, 1.8);
                dart.hit(target);
            });
            context.waitFor(c -> sounds.stream().anyMatch(x -> x.getIdentifier().equals(BlowgunMod.id("hit"))));
            context.runOnClient(c -> {
                var sound = sounds.stream().filter(x -> x.getIdentifier().equals(BlowgunMod.id("hit"))).findFirst().orElseThrow();
                if (Math.abs(sound.getX() - targetPosition[0]) > 0.2 || Math.abs(sound.getZ() - targetPosition[2]) > 0.2)
                    throw new AssertionError("impact sound must originate at victim");
                c.getSoundManager().removeListener(listener);
            });
            world.getServer().runOnServer(server -> {
                var p=world.getConnection().getServerPlayer();p.setGameMode(GameType.CREATIVE);
                for(var e:p.level().getEntities(p,p.getBoundingBox().inflate(8))) e.discard();
                p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(BlowgunMod.VENOM_DART,16));
                var dart=new DartEntity(p.level(),p,new ItemStack(BlowgunMod.VENOM_DART),new ItemStack(BlowgunMod.BLOWGUN));
                dart.setPos(p.getX()+0.35,p.getEyeY(),p.getZ()+1.4);dart.setNoGravity(true);dart.setNoPhysics(true);dart.setYRot(90);dart.yRotO=90;p.level().addFreshEntity(dart);
                for(int stage=1;stage<=3;stage++) {
                    var cow=net.minecraft.world.entity.EntityTypes.COW.create(p.level(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    cow.setPos(p.getX()+(stage-2)*2,p.getY(),p.getZ()+5);cow.setNoAi(true);cow.setInvulnerable(true);
                    int count=stage==1?1:stage==2?5:12;for(int i=0;i<count;i++)VenomState.of(cow).hit();p.level().addFreshEntity(cow);
                }
                p.containerMenu.broadcastChanges();
            });
            context.waitTicks(40);
            context.takeScreenshot("venom-three-stages-and-dart");
            for(int stage=1;stage<=3;stage++) {
                final int expected=stage;
                world.getServer().runOnServer(server -> {
                    var v=VenomState.of(world.getConnection().getServerPlayer());v.clear();
                    int count=expected==1?1:expected==2?5:12;for(int i=0;i<count;i++)v.hit();
                });
                context.waitFor(c -> VenomState.of(c.player).stage()==expected);
                context.waitTicks(8);
                context.takeScreenshot("venom-first-person-stage-"+stage);
            }

        }
    }
}