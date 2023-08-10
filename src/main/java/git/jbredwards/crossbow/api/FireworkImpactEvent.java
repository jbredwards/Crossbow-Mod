/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nonnull;

/**
 * This event is fired when a firework entity impacts something.
 * This event is fired via {@link git.jbredwards.crossbow.mod.asm.transformer.TransformerEntityFireworkRocket.Hooks#handleCollision(EntityFireworkRocket) TransformerEntityFireworkRocket::handleCollision}.<p>
 *
 * This event is cancelable. When canceled, the impact will not be processed.
 * Killing or other handling of the entity after event cancellation is up to the modder.
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS MinecraftForge.EVENT_BUS}.
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
