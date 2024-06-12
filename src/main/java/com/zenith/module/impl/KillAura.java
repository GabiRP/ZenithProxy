package com.zenith.module.impl;

import com.zenith.cache.data.entity.Entity;
import com.zenith.cache.data.entity.EntityPlayer;
import com.zenith.cache.data.entity.EntityStandard;
import com.zenith.event.module.ClientBotTick;
import com.zenith.feature.world.Pathing;
import com.zenith.mc.item.ItemRegistry;
import com.zenith.util.math.MathHelper;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ByteEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundInteractPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket;

import javax.annotation.Nullable;
import java.util.Set;

import static com.github.rfresh2.EventConsumer.of;
import static com.zenith.Shared.*;

public class KillAura extends AbstractInventoryModule {

    private static final Set<EntityType> hostileEntities = ReferenceOpenHashSet.of(
        EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.DROWNED, EntityType.ELDER_GUARDIAN,
        EntityType.ENDER_DRAGON, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GUARDIAN,
        EntityType.HOGLIN, EntityType.HUSK, EntityType.ILLUSIONER, EntityType.FIREBALL, EntityType.MAGMA_CUBE,
        EntityType.PHANTOM, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.RAVAGER,
        EntityType.SHULKER, EntityType.SHULKER_BULLET, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
        EntityType.SMALL_FIREBALL, EntityType.SPIDER, EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR,
        EntityType.WARDEN, EntityType.WITCH, EntityType.WITHER, EntityType.ZOGLIN, EntityType.ZOMBIE,
        EntityType.ZOMBIE_VILLAGER
    );
    private static final Set<EntityType> neutralEntities = ReferenceOpenHashSet.of(
        EntityType.BEE, EntityType.DOLPHIN, EntityType.ENDERMAN, EntityType.FOX, EntityType.GOAT, EntityType.IRON_GOLEM,
        EntityType.LLAMA, EntityType.PANDA, EntityType.POLAR_BEAR, EntityType.TRADER_LLAMA, EntityType.WOLF,
        EntityType.ZOMBIFIED_PIGLIN
    );
    private int delay = 0;
    private boolean isAttacking = false;
    private EquipmentSlot weaponSlot = EquipmentSlot.MAIN_HAND;
    private static final int MOVEMENT_PRIORITY = 500;
    private final IntSet swords = IntSet.of(
        ItemRegistry.DIAMOND_SWORD.id(),
        ItemRegistry.NETHERITE_SWORD.id(),
        ItemRegistry.IRON_SWORD.id()
    );
    private final IntSet axes = IntSet.of(
        ItemRegistry.NETHERITE_AXE.id(),
        ItemRegistry.DIAMOND_AXE.id(),
        ItemRegistry.IRON_AXE.id()
    );

    public KillAura() {
        super(false, 1, MOVEMENT_PRIORITY);
    }

    public boolean isActive() {
        return CONFIG.client.extra.killAura.enabled && isAttacking;
    }

    @Override
    public void subscribeEvents() {
        EVENT_BUS.subscribe(
            this,
            of(ClientBotTick.class, this::handleClientTick),
            of(ClientBotTick.Stopped.class, this::handleBotTickStopped),
            of(ClientBotTick.class, -30000 + MOVEMENT_PRIORITY, this::handlePostTick)
        );
    }

    @Override
    public boolean shouldBeEnabled() {
        return CONFIG.client.extra.killAura.enabled;
    }

    public void handleClientTick(final ClientBotTick event) {
        if (CACHE.getPlayerCache().getThePlayer().isAlive()
                && !MODULE.get(AutoEat.class).isEating()) {
            if (delay > 0) return;
            // find non-friended players or hostile mobs within 3.5 blocks
            final Entity target = findTarget();
            // rotate to target
            if (target != null && MODULE.get(PlayerSimulation.class).isOnGround()) {
                isAttacking = true;
                if (switchToWeapon())
                    rotateTo(target);
                PATHING.stop(MOVEMENT_PRIORITY-1);
            } else {
                isAttacking = false;
            }
        }
    }


    private void handlePostTick(ClientBotTick post) {
        if (delay > 0) {
            delay--;
            return;
        }
        if (!isAttacking) return; // todo: store attack target as a nullable weak ref?
        final Entity target = findTarget();
        if (target == null) return;
        if (hasRotation(target))
            attack(target);
        delay = CONFIG.client.extra.killAura.attackDelayTicks;
    }

    @Nullable
    private Entity findTarget() {
        for (Entity entity : CACHE.getEntityCache().getEntities().values()) {
            if (!validTarget(entity)) continue;
            if (CACHE.getPlayerCache().distanceToSelf(entity) > CONFIG.client.extra.killAura.attackRange) continue;
            return entity;
        }
        return null;
    }

    private boolean validTarget(Entity entity) {
        if (CONFIG.client.extra.killAura.targetPlayers && entity instanceof EntityPlayer player) {
            if (player.isSelfPlayer()) return false;
            return !PLAYER_LISTS.getFriendsList().contains(player.getUuid())
                && !PLAYER_LISTS.getWhitelist().contains(player.getUuid())
                && !PLAYER_LISTS.getSpectatorWhitelist().contains(player.getUuid());

        } else if (entity instanceof EntityStandard e) {
            if (CONFIG.client.extra.killAura.targetHostileMobs) {
                if (hostileEntities.contains(e.getEntityType())) return true;
            }
            if (CONFIG.client.extra.killAura.targetArmorStands) {
                if (e.getEntityType() == EntityType.ARMOR_STAND) return true;
            }
            if (CONFIG.client.extra.killAura.targetNeutralMobs) {
                if (neutralEntities.contains(e.getEntityType())) {
                    if (CONFIG.client.extra.killAura.onlyNeutralAggressive) {
                        // https://wiki.vg/Entity_metadata#Mob
                        var byteMetadata = entity.getMetadata().get(15);
                        if (byteMetadata == null) return false;
                        if (byteMetadata instanceof ByteEntityMetadata byteData) {
                            var data = byteData.getPrimitiveValue() & 0x04;
                            return data != 0;
                        }
                        return false;
                    }
                    return true;
                }
            }
            if (CONFIG.client.extra.killAura.targetCustom) {
                return CONFIG.client.extra.killAura.customTargets.contains(e.getEntityType());
            }
        }
        return false;
    }

    public void handleBotTickStopped(final ClientBotTick.Stopped event) {
        delay = 0;
        isAttacking = false;
    }

    private void attack(final Entity entity) {
        sendClientPacketsAsync(
            new ServerboundInteractPacket(entity.getEntityId(), InteractAction.ATTACK, false),
            new ServerboundSwingPacket(weaponSlot == EquipmentSlot.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND)
        );
    }

    private void rotateTo(Entity entity) {
        var rotation = Pathing.shortestRotationTo(entity);
        PATHING.rotate(rotation.getX(), rotation.getY(), MOVEMENT_PRIORITY);
    }

    private boolean hasRotation(final Entity entity) {
        var rotation = Pathing.shortestRotationTo(entity);
        var sim = MODULE.get(PlayerSimulation.class);
        boolean yawNear = MathHelper.isNear(sim.getYaw(), rotation.getX(), 0.1f);
        boolean pitchNear = MathHelper.isNear(sim.getPitch(), rotation.getY(), 0.1f);
        return yawNear && pitchNear;
    }

    public boolean switchToWeapon() {
        if (!CONFIG.client.extra.killAura.switchWeapon) return true;
        delay = doInventoryActions();
        var hand = getHand();
        weaponSlot = hand == Hand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.MAIN_HAND;
        return delay == 0;
    }

    private boolean isWeapon(int id) {
        return swords.contains(id) || axes.contains(id);
    }

    @Override
    public boolean itemPredicate(final ItemStack itemStack) {
        return isWeapon(itemStack.getId());
    }
}
