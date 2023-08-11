/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm.transformer;

import com.google.common.base.Predicates;
import git.jbredwards.crossbow.api.FireworkImpactEvent;
import git.jbredwards.crossbow.mod.asm.ASMHandler;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Comparator;
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
            new ClassReader(basicClass).accept(classNode, ClassReader.SKIP_FRAMES);

            methods:
            for(final MethodNode method : classNode.methods) {
                /*
                 * Constructor: (changes are around lines 40 & 68)
                 * Old code:
                 * this.setSize(0.25F, 0.25F);
                 *
                 * New code:
                 * // Disable vanilla collision, this causes fireworks to sometimes skim the sides of blocks
                 * this.setSize(0.25F, 0.25F);
                 * this.noClip = true;
                 */
                if(method.name.equals("<init>")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setSize" : "func_70105_a")) {
                            ASMHandler.LOGGER.debug("transforming - EntityFireworkRocket::<init>");
                            final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new InsnNode(ICONST_1));
                            list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/Entity", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "noClip" : "field_70145_X", "Z"));
                            method.instructions.insert(insn, list);
                            break;
                        }
                    }
                }

                // onUpdate
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "onUpdate" : "func_70071_h_")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
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
                        if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("SELF")) {
                            ASMHandler.LOGGER.debug("transforming - EntityFireworkRocket::onUpdate");
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityFireworkRocket$Hooks", "correctVelocity", "(Lnet/minecraft/entity/Entity;)V", false));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        }

                        /*
                         * onUpdate: (changes are around line 204)
                         * Old code:
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Explode on contact with an entity or block
                         * {
                         *     ...
                         *     Hooks.handleCollision(this);
                         * }
                         */
                        else if(insn.getOpcode() == RETURN) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityFireworkRocket$Hooks", "handleCollision", "(Lnet/minecraft/entity/item/EntityFireworkRocket;)V", false));
                            break;
                        }
                    }
                }

                /*
                 * dealExplosionDamage: (changes are around lines 223 & 249)
                 * Old code:
                 * this.boostedEntity.attackEntityFrom(DamageSource.FIREWORKS, (float)(5 + nbttaglist.tagCount() * 2));
                 * ...
                 * entitylivingbase.attackEntityFrom(DamageSource.FIREWORKS, f1);
                 *
                 * New code:
                 * // Damage source accounts for the shooter
                 * this.boostedEntity.attackEntityFrom(Hooks.fireworkDamageSource(this), (float)(5 + nbttaglist.tagCount() * 2));
                 * ...
                 * entitylivingbase.attackEntityFrom(Hooks.fireworkDamageSource(this), f1);
                 */
                else if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "dealExplosionDamage" : "func_191510_k")) {
                    int index = 0;
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "FIREWORKS" : "field_191552_t")) {
                            if(index ++== 0) ASMHandler.LOGGER.debug("transforming - EntityFireworkRocket::dealExplosionDamage");
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityFireworkRocket$Hooks", "fireworkDamageSource", "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/DamageSource;", false));
                            method.instructions.remove(insn);
                            if(index == 2) break methods;
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
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
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

        @Nonnull
        public static DamageSource fireworkDamageSource(@Nonnull Entity entity) {
            final ICrossbowFireworkData cap = ICrossbowFireworkData.get(entity);
            return cap != null ? new EntityDamageSourceIndirect(DamageSource.FIREWORKS.damageType, entity, cap.getOwner()).setExplosion() : DamageSource.FIREWORKS;
        }

        @SuppressWarnings({"Guava", "unchecked"})
        public static void handleCollision(@Nonnull EntityFireworkRocket firework) {
            if(firework.isEntityAlive()) {
                final Vec3d start = new Vec3d(firework.posX, firework.posY, firework.posZ);
                final Vec3d end = new Vec3d(firework.posX + firework.motionX, firework.posY + firework.motionY, firework.posZ + firework.motionZ);

                final ICrossbowFireworkData cap = ICrossbowFireworkData.get(firework);
                final RayTraceResult trace = firework.world.getEntitiesInAABBexcluding(firework, firework.getEntityBoundingBox().expand(firework.motionX, firework.motionY, firework.motionZ),
                                Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, entity -> entity.canBeCollidedWith() && firework.boostedEntity != entity && (firework.ticksExisted > 5 || cap == null || cap.getOwner() != entity)))
                        .stream()
                        .map(entity -> {
                            final RayTraceResult collision = entity.getEntityBoundingBox().grow(0.3).calculateIntercept(start, end);
                            return collision == null ? Pair.of((Entity)null, 0) : Pair.of(entity, start.distanceTo(collision.hitVec));
                        })
                        .filter(collision -> collision.getKey() != null)
                        .min(Comparator.comparingDouble(collision -> collision.getValue().doubleValue()))
                        .map(entry -> new RayTraceResult(entry.getKey())).orElseGet(() -> firework.world.rayTraceBlocks(start, end, false, true, false));

                if(trace != null && !MinecraftForge.EVENT_BUS.post(new FireworkImpactEvent(firework, trace))) {
                    if(!firework.world.isRemote) {
                        if(trace.entityHit == null) {
                            final IBlockState state = firework.world.getBlockState(trace.getBlockPos());
                            state.getBlock().onEntityCollision(firework.world, trace.getBlockPos(), state, firework);
                        }

                        firework.world.setEntityState(firework, (byte)17);
                        firework.dealExplosionDamage();
                        firework.setDead();
                    }

                    firework.isAirBorne = true;
                }
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
