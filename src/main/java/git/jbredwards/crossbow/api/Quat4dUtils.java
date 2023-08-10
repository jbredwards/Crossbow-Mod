/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;

/**
 * Utility class that contains some helpful functions relating to {@link Quat4d}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public final class Quat4dUtils
{
    private Quat4dUtils() {}

    @Nonnull
    public static Quat4d createQuat4d(@Nonnull Vec3d pos, double angle, boolean degrees) {
        final Quat4d ret = new Quat4d();
        ret.set(new AxisAngle4d(pos.x, pos.y, pos.z, degrees ? Math.toRadians(angle) : angle));
        return ret;
    }

    @Nonnull
    public static Vec3d getMultishotVector(@Nonnull Entity user, double multishotOffset) {
        return transform(user.getLookVec(), createQuat4d(getOppositeLookVec(user), multishotOffset, true));
    }

    @Nonnull
    public static Vec3d getMultishotVector(@Nonnull Entity user, @Nonnull Vec3d target, double multishotOffset) {
        target = target.normalize();
        Vec3d product = target.crossProduct(new Vec3d(0, 1, 0));

        if(product.lengthSquared() <= 1.0E-7) product = target.crossProduct(getOppositeLookVec(user));
        return transform(target, createQuat4d(transform(target, createQuat4d(product, 90, true)), multishotOffset, true));
    }

    @Nonnull
    public static Vec3d getOppositeLookVec(@Nonnull Entity entity) {
        return entity.getVectorForRotation(entity.rotationPitch - 90, entity.rotationYaw);
    }

    @Nonnull
    public static Vec3d transform(@Nonnull Vec3d original, @Nonnull Quat4d transformation) {
        final Quat4d ret = new Quat4d(transformation);
        ret.mul(new Quat4d(original.x, original.y, original.z, 0));

        final Quat4d scale = new Quat4d(transformation);
        scale.conjugate();
        ret.mul(scale);
        return new Vec3d(ret.x, ret.y, ret.z);
    }
}
