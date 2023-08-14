/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm.transformer.modded;

import git.jbredwards.crossbow.api.ICrossbow;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Allows quivers from the Spartan Weaponry mod to interact with this mod's crossbows.
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public final class TransformerSpartanWeaponry implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if(transformedName.startsWith("com.oblivioussp.spartanweaponry.util.QuiverHelper$")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals("isWeapon")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == INSTANCEOF) {
                            method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/modded/TransformerSpartanWeaponry$Hooks", "orCrossbow", "(ZLnet/minecraft/item/ItemStack;)Z", false));
                            method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
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
        public static boolean orCrossbow(boolean flag, @Nonnull ItemStack stack) { return flag || stack.getItem() instanceof ICrossbow; }
    }
}
