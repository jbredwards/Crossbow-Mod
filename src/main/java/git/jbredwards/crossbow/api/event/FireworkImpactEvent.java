/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api.event;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nonnull;

/**
 *
 * @since 1.0.0
 * @author jbred
 *
 */
@Cancelable
public class FireworkImpactEvent extends ProjectileImpactEvent
{
    @Nonnull
    private final EntityFireworkRocket firework;
    public FireworkImpactEvent(@Nonnull EntityFireworkRocket fireworkIn, @Nonnull RayTraceResult rayIn) {
        super(fireworkIn, rayIn);
        firework = fireworkIn;
    }

    @Nonnull
    public EntityFireworkRocket getFireworkRocket() { return firework; }
}
