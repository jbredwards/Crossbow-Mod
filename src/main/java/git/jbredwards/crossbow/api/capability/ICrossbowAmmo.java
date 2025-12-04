/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api.capability;

import git.jbredwards.crossbow.mod.common.Crossbow;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

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
     * @since 1.2.0
     */
    @Nonnull
    Set<ModelResourceLocation> AMMO_MODELS = new HashSet<>();

    /**
     * All models returned by this must be added to {@link ICrossbowAmmo#AMMO_MODELS AMMO_MODELS} during fml pre-init.
     * @return The model location used by loaded Crossbows to render this ammo ItemStack.
     * @since 1.2.0
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default ModelResourceLocation getAmmoModelLocation(@Nullable final EntityLivingBase user, @Nonnull final ItemStack crossbow, @Nonnull final ItemStack projectile) {
        return new ModelResourceLocation(new ResourceLocation(Crossbow.MODID, "crossbow"), "arrow");
    }

    /**
     * @return A map of colors used by loaded Crossbows to render this ammo ItemStack, where the keys are tint indexes.
     * @since 1.2.0
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Int2IntMap getAmmoModelColor(@Nullable final EntityLivingBase user, @Nonnull final ItemStack crossbow, @Nonnull final ItemStack projectile) {
        return Int2IntMaps.EMPTY_MAP;
    }

    /**
     * @return Whether this can be loaded while held.
     * @since 1.1.0
     */
    default boolean isHeldCrossbowAmmo(@Nonnull final EntityLivingBase user, @Nonnull final ItemStack crossbow, @Nonnull final ItemStack projectile) {
        return true;
    }

    /**
     * @return Whether this can be loaded while in the user's inventory.
     * @since 1.1.0
     */
    default boolean isInventoryCrossbowAmmo(@Nonnull final EntityLivingBase user, @Nonnull final ItemStack crossbow, @Nonnull final ItemStack projectile) {
        return true;
    }

    /**
     * Used by Crossbow to alter the final speed of the projectile fired.
     * @since 1.1.0
     */
    default float velocityMultiplier(@Nonnull final EntityLivingBase user, @Nonnull final ItemStack crossbow, @Nonnull final ItemStack projectile) {
        return 1;
    }

    /**
     * @return the created projectile instance upon firing a crossbow. The projectile's velocity is handled by the crossbow itself.
     * @since 1.1.0
     */
    @Nullable
    IProjectile createCrossbowProjectile(@Nonnull final EntityLivingBase user, @Nonnull final ItemStack crossbow, @Nonnull final ItemStack projectile);

    /**
     * A default implementation for {@link ItemArrow} items.
     *
     * @since 1.2.0
     * @author jbred
     *
     */
    interface Arrow extends ICrossbowAmmo
    {
        @Nullable
        @Override
        default IProjectile createCrossbowProjectile(@Nonnull final EntityLivingBase user, @Nonnull final ItemStack crossbow, @Nonnull final ItemStack projectile) {
            return ((ItemArrow)projectile.getItem()).createArrow(user.world, projectile, user);
        }
    }
}
