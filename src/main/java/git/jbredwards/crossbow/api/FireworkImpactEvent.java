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
