/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm.transformer;

import com.google.common.base.Predicate;
import git.jbredwards.crossbow.mod.asm.ASMHandler;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Adds arrow piercing functionality
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public final class TransformerEntityArrow implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        // EntityArrow
        if("net.minecraft.entity.projectile.EntityArrow".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, ClassReader.SKIP_FRAMES);

            methods:
            for(final MethodNode method : classNode.methods) {
                /*
                 * onUpdate: (changes are around lines 267-288)
                 * Old code:
                 * Entity entity = this.findEntityOnPath(vec3d1, vec3d);
                 *
                 * if (entity != null)
                 * {
                 *     raytraceresult = new RayTraceResult(entity);
                 * }
                 *
                 * if (raytraceresult != null && raytraceresult.entityHit instanceof EntityPlayer)
                 * {
                 *     EntityPlayer entityplayer = (EntityPlayer)raytraceresult.entityHit;
                 *
                 *     if (this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(entityplayer))
                 *     {
                 *         raytraceresult = null;
                 *     }
                 * }
                 *
                 * if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult))
                 * {
                 *     this.onHit(raytraceresult);
                 * }
                 *
                 * New code:
                 * // Add pierce functionality
                 * Hooks.applyArrowCollision(this, vec3d1, vec3d, raytraceresult);
                 */
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "onUpdate" : "func_70071_h_")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "findEntityOnPath" : "func_184551_a")) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 6));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityArrow$Hooks", "applyArrowCollision", "(Lnet/minecraft/entity/projectile/EntityArrow;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/RayTraceResult;)V", false));

                            AbstractInsnNode insnToCheck = insn.getNext().getNext();
                            while(insnToCheck.getOpcode() != INVOKEVIRTUAL || !((MethodInsnNode)insnToCheck).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getIsCritical" : "func_70241_g")) {
                                method.instructions.remove(insn.getNext());
                                insnToCheck = insn.getNext().getNext();
                            }

                            method.instructions.remove(insn);
                            break;
                        }
                    }
                }

                // onHit
                else if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "onHit" : "func_184549_a")) {
                    final LabelNode first = new LabelNode();
                    final LabelNode last = new LabelNode();
                    method.instructions.insert(first);
                    method.instructions.add(last);
                    method.localVariables.add(new LocalVariableNode("cap", "Lgit/jbredwards/crossbow/mod/common/capability/ICrossbowArrowData;", null, first, last, 10));

                    int soundFieldIndex = 0;
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * Old code:
                         * Entity entity = raytraceResultIn.entityHit;
                         *
                         * New code:
                         * // Cache capability for later use
                         * Entity entity = raytraceResultIn.entityHit;
                         * ICrossbowArrowData cap = ICrossbowArrowData.get(this);
                         */
                        if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "entityHit" : "field_72308_g")) {
                            method.instructions.insert(insn.getNext(), new VarInsnNode(ASTORE, 10));
                            method.instructions.insert(insn.getNext(), new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/common/capability/ICrossbowArrowData", "get", "(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)Lgit/jbredwards/crossbow/mod/common/capability/ICrossbowArrowData;", false));
                            method.instructions.insert(insn.getNext(), new VarInsnNode(ALOAD, 0));
                        }
                        /*
                         * Old code:
                         * if (!this.world.isRemote)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Only increment arrow count if no more entities will be pierced (wouldn't make sense for only one arrow to render on multiple entities, right?)
                         * if (!this.world.isRemote && Hooks.isLastEntity(cap))
                         * {
                         *     ...
                         * }
                         */
                        else if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "isRemote" : "field_72995_K") && insn.getNext().getNext().getOpcode() != ALOAD) {
                            final LabelNode label = ((JumpInsnNode)insn.getNext()).label;
                            method.instructions.insert(insn.getNext(), new JumpInsnNode(IFEQ, label));
                            method.instructions.insert(insn.getNext(), new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityArrow$Hooks", "isLastEntity", "(Lgit/jbredwards/crossbow/mod/common/capability/ICrossbowArrowData;)Z", false));
                            method.instructions.insert(insn.getNext(), new VarInsnNode(ALOAD, 10));
                        }
                        /*
                         * Old code:
                         * if (!(entity instanceof EntityEnderman))
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Don't kill this arrow if it can pierce more entities
                         * if (!(entity instanceof EntityEnderman) && Hooks.isLastEntity(cap))
                         * {
                         *     ...
                         * }
                         */
                        else if(insn.getOpcode() == INSTANCEOF && ((TypeInsnNode)insn).desc.equals("net/minecraft/entity/monster/EntityEnderman") && insn.getPrevious().getPrevious().getOpcode() != IFEQ) {
                            final LabelNode label = ((JumpInsnNode)insn.getNext()).label;
                            method.instructions.insert(insn.getNext(), new JumpInsnNode(IFEQ, label));
                            method.instructions.insert(insn.getNext(), new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityArrow$Hooks", "isLastEntity", "(Lgit/jbredwards/crossbow/mod/common/capability/ICrossbowArrowData;)Z", false));
                            method.instructions.insert(insn.getNext(), new VarInsnNode(ALOAD, 10));
                        }
                        /*
                         * Old code:
                         * this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                         * ...
                         * this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                         *
                         * New code:
                         * //
                         * this.playSound(cap.getHitSound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                         * ...
                         * this.playSound(Hooks.onHitBlock(cap, SoundEvents.ENTITY_ARROW_HIT), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                         */
                        else if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "ENTITY_ARROW_HIT" : "field_187731_t")) {
                            if(soundFieldIndex ++== 0) {
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 10));
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEINTERFACE, "git/jbredwards/crossbow/mod/common/capability/ICrossbowArrowData", "getHitSound", "()Lnet/minecraft/util/SoundEvent;", true));
                                method.instructions.remove(insn);
                            }
                            else {
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 10));
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityArrow$Hooks", "onHitBlock", "(Lgit/jbredwards/crossbow/mod/common/capability/ICrossbowArrowData;)Lnet/minecraft/util/SoundEvent;", false));
                                method.instructions.remove(insn);
                                break;
                            }
                        }
                    }
                }

                /*
                 * findEntityOnPath: (changes are around line 504)
                 * Old code:
                 * List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D), ARROW_TARGETS);
                 *
                 * New code:
                 * // Chance predicate to one that accounts for pierced entities
                 * List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D), Hooks.predicate(this, ARROW_TARGETS));
                 */
                else if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "findEntityOnPath" : "func_184551_a")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "ARROW_TARGETS" : "field_184553_f")) {
                            ASMHandler.LOGGER.debug("transforming - EntityArrow::findEntityOnPath");
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityArrow$Hooks", "predicate", "(Lnet/minecraft/entity/Entity;Lcom/google/common/base/Predicate;)Lcom/google/common/base/Predicate;", false));
                            break methods;
                        }
                    }
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    @Mod.EventBusSubscriber(modid = Crossbow.MODID)
    public static final class Hooks
    {
        public static void applyArrowCollision(@Nonnull EntityArrow arrow, @Nonnull Vec3d start, @Nonnull Vec3d end, @Nullable RayTraceResult trace) {
            final ICrossbowArrowData cap = ICrossbowArrowData.get(arrow);
            assert cap != null; //should always pass

            while(!arrow.isDead) {
                Entity entity = arrow.findEntityOnPath(start, end);
                if(entity != null) trace = new RayTraceResult(entity);

                if(trace != null && trace.entityHit instanceof EntityPlayer) {
                    final EntityPlayer player = (EntityPlayer)trace.entityHit;
                    if(arrow.shootingEntity instanceof EntityPlayer && !((EntityPlayer)arrow.shootingEntity).canAttackPlayer(player)) {
                        trace = null;
                        entity = null;
                    }
                }

                if(trace != null && !ForgeEventFactory.onProjectileImpact(arrow, trace)) {
                    arrow.onHit(trace);
                    arrow.isAirBorne = true;
                }

                if(entity == null || cap.getPierceLevel() <= 0) break;
                trace = null;
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public static void cachePiercedEntity(@Nonnull ProjectileImpactEvent.Arrow event) {
            if(event.getRayTraceResult().entityHit != null) {
                final ICrossbowArrowData cap = ICrossbowArrowData.get(event.getArrow());
                if(cap != null && cap.getPierceLevel() > 0) {
                    if(cap.getPiercedEntities() == null) cap.setPiercedEntities(new IntOpenHashSet(5));
                    if(cap.getPiercedEntities().size() > cap.getPierceLevel()) {
                        event.getArrow().setDead();
                        event.setCanceled(true);
                        return;
                    }

                    cap.getPiercedEntities().add(event.getRayTraceResult().entityHit.getEntityId());
                    if(event.getArrow().world.isRemote) event.setCanceled(true); //fixes a weird arrow motion desync
                }
            }
        }

        public static boolean isLastEntity(@Nonnull ICrossbowArrowData cap) {
            return cap.getPiercedEntities() == null || cap.getPiercedEntities().size() > cap.getPierceLevel();
        }

        @Nonnull
        public static SoundEvent onHitBlock(@Nonnull ICrossbowArrowData cap) {
            final SoundEvent hitSound = cap.getHitSound();
            cap.setHitSound(SoundEvents.ENTITY_ARROW_HIT);
            cap.setPierceLevel(0);
            cap.setPiercedEntities(null);

            return hitSound;
        }

        @SuppressWarnings("Guava")
        @Nonnull
        public static Predicate<Entity> predicate(@Nonnull Entity arrow, @Nonnull Predicate<Entity> original) {
            final ICrossbowArrowData cap = ICrossbowArrowData.get(arrow);
            return cap == null ? original : target -> original.apply(target) && (cap.getPiercedEntities() == null || !cap.getPiercedEntities().contains(target.getEntityId()));
        }
    }
}
