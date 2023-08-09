/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api.entity;

import git.jbredwards.crossbow.api.util.QuaternionUtils;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import git.jbredwards.crossbow.mod.common.init.CrossbowSounds;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

/**
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ICrossbowUser
{
    /**
     *
     * @param crossbow
     * @return
     */
    @Nonnull
    ItemStack findAmmo(@Nonnull ItemStack crossbow);

    /**
     *
     * @param charging
     */
    void setCharging(boolean charging);

    /**
     *
     * @param crossbow
     * @param projectile
     * @param multishotOffset
     */
    void shootAtTarget(@Nonnull ItemStack crossbow, @Nonnull IProjectile projectile, float multishotOffset);

    /**
     *
     * @param target
     * @param crossbow
     * @param projectile
     * @param multishotOffset
     */
    default void shoot(@Nonnull EntityLivingBase user, @Nonnull EntityLivingBase target, @Nonnull ItemStack crossbow, @Nonnull IProjectile projectile, float multishotOffset) {
        final double x = target.posX - user.posX;
        final double z = target.posZ - user.posZ;
        final double y = target.getEntityBoundingBox().minY + target.height / 3 - ((Entity)projectile).posY + Math.sqrt(x * x + z * z) * 0.2;

        final Vec3d velocity = QuaternionUtils.getMultishotVector(user, new Vec3d(x, y, z), multishotOffset);
        projectile.shoot(velocity.x, velocity.y, velocity.z, 1.6f, 14 - user.world.getDifficulty().getId() * 4);
        user.playSound(CrossbowSounds.ITEM_CROSSBOW_SHOOT, 1, 1 / (user.getRNG().nextFloat() * 0.4f + 0.8f));
    }

    /**
     *
     * @param user
     * @param velocity
     */
    default void performAICrossbowAttack(@Nonnull EntityLivingBase user, float velocity) {
        final ItemStack mainHand = user.getHeldItemMainhand();
        final ICrossbowProjectiles mainCap = ICrossbowProjectiles.get(mainHand);
        if(mainCap != null) {
            ItemCrossbow.shootAll(user.world, user, mainHand, mainCap, velocity, 14 - user.world.getDifficulty().getId() * 4);
            return;
        }

        final ItemStack offHand = user.getHeldItemOffhand();
        final ICrossbowProjectiles offCap = ICrossbowProjectiles.get(offHand);
        if(offCap != null) ItemCrossbow.shootAll(user.world, user, offHand, offCap, velocity, 14 - user.world.getDifficulty().getId() * 4);
    }
}
