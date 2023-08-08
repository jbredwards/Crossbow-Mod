/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api.entity;

import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ICrossbowUser
{
    void setCharging(boolean charging);

    void shootAtTarget(@Nonnull ItemStack crossbow, @Nonnull IProjectile projectile, float multishotOffset);

    @Nonnull
    ItemStack findAmmo(@Nonnull ItemStack crossbow);
}
