/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Items that implement this (or have it as a capability) can be loaded into crossbows.<p>
 * Note: {@link CapabilityCrossbowAmmo#get} can be used to get the ItemStack's ICrossbowAmmo instance.
 *
 * @since 1.1.0
 * @author jbred
 *
 */
@FunctionalInterface
public interface ICrossbowAmmo
{
    /**
     * @return the created projectile instance upon firing a crossbow. The projectile's velocity is handled by the crossbow itself.
     */
    @Nullable
    IProjectile createCrossbowProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile);

    /**
     * @return whether this can be loaded while held.
     */
    default boolean isHeldCrossbowAmmo(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) { return true; }

    /**
     * @return whether this can be loaded while in the user's inventory.
     */
    default boolean isInventoryCrossbowAmmo(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) { return true; }

    /**
     * Used to alter the final speed of the projectile fired.
     */
    default float velocityMultiplier(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) { return 1; }
}
