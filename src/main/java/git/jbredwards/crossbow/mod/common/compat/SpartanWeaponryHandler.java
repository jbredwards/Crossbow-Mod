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

package git.jbredwards.crossbow.mod.common.compat;

import com.oblivioussp.spartanweaponry.item.ItemBolt;
import com.oblivioussp.spartanweaponry.util.ConfigHandler;
import git.jbredwards.crossbow.api.capability.ICrossbowAmmo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public final class SpartanWeaponryHandler
{
    @Nonnull
    public static ICrossbowAmmo createAmmoHandler() {
        return new ICrossbowAmmo() {
            @Nullable
            @Override
            public IProjectile createCrossbowProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) {
                return ((ItemBolt)projectile.getItem()).createBolt(user.world, projectile, user);
            }

            @Override
            public float velocityMultiplier(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) {
                return ConfigHandler.crossbowBaseBoltSpeed;
            }
        };
    }

    public static boolean isBolt(@Nonnull Item item) { return item instanceof ItemBolt; }
}
