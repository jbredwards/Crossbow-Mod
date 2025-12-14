/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.crossbow.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.IMob;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows entities that attack using crossbows to have custom behavior.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ICrossbowUser
{
    /**
     * Most of the time calls {@link ICrossbow#isHeldProjectile} with a fallback to {@link ItemStack#EMPTY}.
     * @return The ammo ItemStack from the user's inventory to be loaded into this crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Nonnull
    ItemStack findAmmo(@Nonnull final ItemStack crossbow);

    /**
     * Notifies the client on whether this entity is charging a crossbow. The code for this varies for each entity.
     * <p>
     * Helpful note: If you're using {@link net.minecraft.client.model.ModelBiped}, you can set the arm poses to
     * {@link git.jbredwards.crossbow.mod.client.model.CrossbowArmPose} values to use built-in crossbow animations!
     * </p>
     * @since 1.0.0
     */
    void setCharging(final boolean charging);

    /**
     * Used by {@link ICrossbow#shoot}. Prepares the projectile to be fired. May be overriden.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.2.0
     */
    default void shootAtTarget(@Nonnull final ItemStack crossbow, @Nonnull final IProjectile projectile, final float velocity, final float divergence, final double multishotOffset) {
        if(this instanceof EntityLiving) shoot((EntityLiving)this, ((EntityLiving)this).getAttackTarget(), crossbow, projectile, velocity, divergence, multishotOffset);
    }

    /**
     * Utility method intended to be called by {@link ICrossbowUser#shootAtTarget}. May be overriden.
     *
     * @throws NullPointerException If any parameters aside from target are null.
     * @since 1.2.0
     */
    default void shoot(@Nonnull final EntityLivingBase user, @Nullable final EntityLivingBase target, @Nonnull final ItemStack crossbow, @Nonnull final IProjectile projectile, final float velocity, final float divergence, final double multishotOffset) {
        @Nonnull final Vec3d direction;
        if(target == null) direction = Quat4dUtils.getMultishotVector(user, multishotOffset);
        else {
            final double x = target.posX - user.posX;
            final double z = target.posZ - user.posZ;
            final double y = target.getEntityBoundingBox().minY + target.height / 3 - ((Entity)projectile).posY + Math.sqrt(x * x + z * z) * 0.2;
            direction = Quat4dUtils.getMultishotVector(user, new Vec3d(x, y, z), multishotOffset);
        }

        projectile.shoot(direction.x, direction.y, direction.z, velocity, divergence);
        user.playSound(((ICrossbow)crossbow.getItem()).getShootSound(user, crossbow, projectile, multishotOffset), 1, 1 / (user.getRNG().nextFloat() * 0.4f + 0.8f));
    }

    /**
     * Utility method intended to be called by your entity's ranged attack ai. May be overriden.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    default void performAICrossbowAttack(@Nonnull final EntityLivingBase user, final float velocity) {
        if(!ICrossbow.shootAll(user, user.getHeldItemMainhand(), velocity, 14 - user.world.getDifficulty().getId() * 4))
            ICrossbow.shootAll(user, user.getHeldItemOffhand(), velocity, 14 - user.world.getDifficulty().getId() * 4);
    }

    /**
     * @return True if this entity damages crossbows when firing them.
     * @since 1.2.0
     */
    default boolean damagesCrossbow() {
        return !(this instanceof IMob);
    }

    /**
     * @return True if this entity has infinite crossbow ammo.
     * @since 1.2.0
     */
    default boolean infiniteAmmo() {
        return this instanceof IMob;
    }

    /**
     * @return The fallback ammo ItemStack for when {@link ICrossbowUser#findAmmo} is empty and {@link ICrossbowUser#infiniteAmmo} is true.
     * @since 1.2.0
     */
    @Nonnull
    default ItemStack getInfiniteAmmo() {
        return new ItemStack(Items.ARROW);
    }

    /**
     * Legacy method. Use above method instead with velocity and divergence.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Deprecated
    default void shootAtTarget(@Nonnull final ItemStack crossbow, @Nonnull final IProjectile projectile, final double multishotOffset) {
        shootAtTarget(crossbow, projectile, 1.6f, 14 - ((Entity)this).world.getDifficulty().getId() * 4, multishotOffset);
    }

    /**
     * Legacy method. Use above method instead with velocity and divergence.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Deprecated
    default void shoot(@Nonnull final EntityLivingBase user, @Nonnull final EntityLivingBase target, @Nonnull final ItemStack crossbow, @Nonnull final IProjectile projectile, final double multishotOffset) {
        shoot(user, target, crossbow, projectile, 1.6f, 14 - user.world.getDifficulty().getId() * 4, multishotOffset);
    }
}
