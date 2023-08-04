/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm.transformer;

import com.google.common.base.Predicate;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.IClassTransformer;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public final class TransformerEntityArrow implements IClassTransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        // EntityArrow
        if("net.minecraft.entity.projectile.EntityArrow".equals(transformedName)) {

        }

        return basicClass;
    }

    public static final class Hooks
    {
        @Nonnull
        public static Predicate<Entity> canArrowHit(@Nonnull Entity arrow) {
            final ICrossbowArrowData cap = ICrossbowArrowData.get(arrow);
            return cap == null ? Entity::canBeCollidedWith : target -> target.canBeCollidedWith()
                    && (cap.getPiercedEntities() == null || !cap.getPiercedEntities().contains(target.getEntityId()));
        }
    }
}
