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

import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Parent class for {@link Pre} and {@link Post}.<p>
 * All children of this event are fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS MinecraftForge.EVENT_BUS}.
 *
 * @since 1.2.0
 * @author jbred
 *
 */
public abstract class CreateCrossbowProjectileEvent extends LivingEvent
{
    @Nonnull final ItemStack crossbow, projectile;
    @Nonnull final IProjectile projectileAsEntity;

    protected CreateCrossbowProjectileEvent(@Nonnull final EntityLivingBase userIn, @Nonnull final ItemStack crossbowIn, @Nonnull final ItemStack projectileIn, @Nonnull final IProjectile projectileAsEntityIn) {
        super(userIn);
        crossbow = crossbowIn;
        projectile = projectileIn;
        projectileAsEntity = projectileAsEntityIn;
        Preconditions.checkArgument(projectileAsEntityIn instanceof Entity);
    }

    /**
     * This event is fired right as a crossbow creates a projectile instance.
     * This event is fired via {@link ICrossbow#createProjectileFromStack}.<p>
     *
     * This event is cancelable. When canceled, {@link CreateCrossbowProjectileEvent.Pre#newProjectileEntity} will be used.
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS MinecraftForge.EVENT_BUS}.
     *
     * @since 1.2.0
     * @author jbred
     *
     */
    @Cancelable
    public static class Pre extends CreateCrossbowProjectileEvent
    {
        @Nullable IProjectile newProjectileEntity;
        public Pre(@Nonnull final EntityLivingBase userIn, @Nonnull final ItemStack crossbowIn, @Nonnull final ItemStack projectileIn, @Nonnull final IProjectile projectileAsEntityIn) {
            super(userIn, crossbowIn, projectileIn, projectileAsEntityIn);
        }

        @Nonnull
        public ItemStack getCrossbow() { return crossbow; }

        @Nonnull
        public ItemStack getProjectile() { return projectile; }

        @Nonnull
        public IProjectile getProjectileAsEntity() { return projectileAsEntity; }

        @Nullable
        public IProjectile getNewProjectileEntity() { return newProjectileEntity; }
        public void setNewProjectileEntity(@Nullable final IProjectile projectile) { newProjectileEntity = projectile; }
    }

    /**
     * This event is fired right before a crossbow's projectile instance is added to the world.
     * This event is fired via {@link ICrossbow#shoot}, and is intended to allow modders to make final modifications to the projectile entity.<p>
     *
     * This event is not cancelable.
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS MinecraftForge.EVENT_BUS}.
     *
     * @since 1.2.0
     * @author jbred
     *
     */
    public static class Post extends CreateCrossbowProjectileEvent
    {
        public Post(@Nonnull final EntityLivingBase userIn, @Nonnull final ItemStack crossbowIn, @Nonnull final ItemStack projectileIn, @Nonnull final IProjectile projectileAsEntityIn) {
            super(userIn, crossbowIn, projectileIn, projectileAsEntityIn);
        }

        @Nonnull
        public ItemStack getCrossbow() { return crossbow; }

        @Nonnull
        public ItemStack getProjectile() { return projectile; }

        @Nonnull
        public IProjectile getProjectileAsEntity() { return projectileAsEntity; }
    }
}
