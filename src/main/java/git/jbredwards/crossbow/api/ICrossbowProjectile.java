/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;

import javax.annotation.Nullable;

/**
 * Common code for projectile entities that can be shot by crossbows. Implementing this is recommended, but not required.
 * <p>
 * This interface is automatically applied to
 * {@link net.minecraft.entity.projectile.EntityArrow EntityArrow} and {@link net.minecraft.entity.item.EntityFireworkRocket EntityFireworkRocket}
 * at runtime, which both delegate to {@link git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData ICrossbowArrowData} and
 * {@link git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData ICrossbowFireworkData} by default.
 * </p>
 *
 * @since 1.2.0
 * @author jbred
 *
 */
public interface ICrossbowProjectile extends IProjectile
{
    /**
     * @return True if this projectile was shot from a crossbow.
     * @since 1.2.0
     */
    boolean wasShotByCrossbow();

    /**
     * Setter for {@link ICrossbowProjectile#wasShotByCrossbow()}.
     * @since 1.2.0
     */
    void setShotByCrossbow(final boolean shotByCrossbow);

    /**
     * @return The entity that shot this projectile.
     * @since 1.2.0
     */
    @Nullable
    Entity getShooter();

    /**
     * Setter for {@link ICrossbowProjectile#getShooter()}.
     * @since 1.2.0
     */
    void setShooter(@Nullable final Entity shooter);
}
