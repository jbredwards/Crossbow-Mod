/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;

/**
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public final class QuaternionUtils
{
    private QuaternionUtils() {}

    @Nonnull
    public static Vec3d getMultishotVector(@Nonnull EntityLivingBase user, float multishotOffset) {
        return transform(user.getLookVec(), createQuat4d(user.getVectorForRotation(user.rotationPitch - 90, user.rotationYaw), multishotOffset, true));
    }

    @Nonnull
    public static Vec3d getMultishotVector(@Nonnull EntityLivingBase user, @Nonnull Vec3d target, float multishotOffset) {
        target = target.normalize();

        Vec3d product = target.crossProduct(new Vec3d(0, 1, 0));
        if(product.lengthSquared() <= 1.0E-7) product = target.crossProduct(user.getVectorForRotation(user.rotationPitch - 90, user.rotationYaw));

        return product;
        //final Quat4d quat = createQuat4d(product, 90, true);

    }

    @Nonnull
    public static Quat4d createQuat4d(@Nonnull Vec3d vec, double angle, boolean degrees) {
        final Quat4d ret = new Quat4d();
        ret.set(new AxisAngle4d(vec.x, vec.y, vec.z, degrees ? Math.toRadians(angle) : angle));
        return ret;
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
