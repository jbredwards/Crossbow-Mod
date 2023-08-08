/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm.transformer;

import git.jbredwards.crossbow.mod.asm.ASMHandler;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Only use crossbow when the button is released
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public final class TransformerEntityLivingBase implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        // EntityLivingBase
        if("net.minecraft.entity.EntityLivingBase".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            /*
             * updateActiveHand: (changes are around line 2960)
             * Old code:
             * if (--this.activeItemStackUseCount <= 0 && !this.world.isRemote)
             * {
             *     ...
             * }
             *
             * New code:
             * // Don't reset active hand if holding a crossbow
             * if (--this.activeItemStackUseCount <= 0 && !Hooks.isCrossbow(this, this.world.isRemote))
             * {
             *     ...
             * }
             */
            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "updateActiveHand" : "func_184608_ct")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "isRemote" : "field_72995_K")) {
                            ASMHandler.LOGGER.debug("transforming - EntityLivingBase::updateActiveHand");
                            method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerEntityLivingBase$Hooks", "isCrossbow", "(ZLnet/minecraft/entity/EntityLivingBase;)Z", false));
                            method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
                            break methods;
                        }
                    }
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    public static final class Hooks
    {
        public static boolean isCrossbow(boolean isRemote, @Nonnull EntityLivingBase entity) {
            return isRemote || entity.getActiveItemStack().getItem() instanceof ItemCrossbow;
        }
    }
}
