/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm.transformer;

import git.jbredwards.crossbow.mod.asm.ASMHandler;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Allows fireworks to act as projectiles
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public final class TransformerEntityFireworkRocket implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        // EntityFireworkRocket
        if("net.minecraft.entity.item.EntityFireworkRocket".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            /*
             * onUpdate: (changes are around line 158)
             * Old code:
             * this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
             *
             * New code:
             * // Remove hardcoded velocity if fired by a crossbow
             * Hooks.correctVelocity(this);
             * this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
             */
            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "onUpdate" : "func_70071_h_")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("SELF")) {
                            ASMHandler.LOGGER.debug("transforming - EntityFireworkRocket::onUpdate");
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityFireworkRocket$Hooks", "correctVelocity", "(Lnet/minecraft/entity/Entity;)V", false));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            break methods;
                        }
                    }
                }
            }

            /*
             * @ASMGenerated
             * public void shoot(double x, double y, double z, float velocity, float inaccuracy)
             * {
             *     Hooks.shoot(this, this.rand, x, y, z, velocity, inaccuracy);
             * }
             */
            ASMHandler.LOGGER.debug("transforming - EntityFireworkRocket::shoot");
            classNode.interfaces.add("net/minecraft/entity/IProjectile");
            final MethodNode method = new MethodNode(ACC_PUBLIC, FMLLaunchHandler.isDeobfuscatedEnvironment() ? "shoot" : "func_70186_c", "(DDDFF)V", null, null);
            final GeneratorAdapter methodAdapter = new GeneratorAdapter(method, ACC_PUBLIC, method.name, method.desc);
            methodAdapter.visitVarInsn(ALOAD, 0);
            methodAdapter.visitVarInsn(ALOAD, 0);
            methodAdapter.visitFieldInsn(GETFIELD, "net/minecraft/entity/Entity", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "rand" : "field_70146_Z", "Ljava/util/Random;");
            methodAdapter.visitVarInsn(DLOAD, 1);
            methodAdapter.visitVarInsn(DLOAD, 3);
            methodAdapter.visitVarInsn(DLOAD, 5);
            methodAdapter.visitVarInsn(FLOAD, 7);
            methodAdapter.visitVarInsn(FLOAD, 8);
            methodAdapter.visitMethodInsn(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityFireworkRocket$Hooks", "shoot", "(Lnet/minecraft/entity/Entity;Ljava/util/Random;DDDFF)V", false);
            methodAdapter.visitInsn(RETURN);
            classNode.methods.add(method);

            //writes the changes
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    public static final class Hooks
    {
        public static void correctVelocity(@Nonnull Entity entity) {
            final ICrossbowFireworkData cap = ICrossbowFireworkData.get(entity);
            if(cap != null && cap.wasShotByCrossbow()) {
                entity.motionX /= 1.15;
                entity.motionY -= 0.04;
                entity.motionZ /= 1.15;
            }
        }

        public static void shoot(@Nonnull Entity entity, @Nonnull Random rand, double x, double y, double z, float velocity, float inaccuracy) {
            final double sqrt = Math.sqrt(x * x + y * y + z * z);

            x /= sqrt;
            x += rand.nextGaussian() * 0.0075 * inaccuracy;
            x *= velocity;

            y /= sqrt;
            y += rand.nextGaussian() * 0.0075 * inaccuracy;
            y *= velocity;

            z /= sqrt;
            z += rand.nextGaussian() * 0.0075 * inaccuracy;
            z *= velocity;

            entity.motionX = x;
            entity.motionY = y;
            entity.motionZ = z;
        }
    }
}
