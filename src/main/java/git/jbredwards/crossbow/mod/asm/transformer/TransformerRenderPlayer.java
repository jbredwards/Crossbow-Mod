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

package git.jbredwards.crossbow.mod.asm.transformer;

import git.jbredwards.crossbow.api.ICrossbow;
import git.jbredwards.crossbow.mod.asm.ASMHandler;
import git.jbredwards.crossbow.mod.client.model.CrossbowArmPose;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Set crossbow ArmPos
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public final class TransformerRenderPlayer implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        // RenderPlayer
        if("net.minecraft.client.renderer.entity.RenderPlayer".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            /*
             * setModelVisibilities: (changes are around lines 108 & 127)
             * Old code:
             * modelbiped$armpose = ModelBiped.ArmPose.ITEM;
             * ...
             * modelbiped$armpose1 = ModelBiped.ArmPose.ITEM;
             *
             * New code:
             * // Add functionality for crossbow action enum
             * modelbiped$armpose = Hooks.crossbowPoseOrDefault(ModelBiped.ArmPose.ITEM, clientPlayer, itemstack, itemstack1, EnumHand.MAIN_HAND);
             * ...
             * modelbiped$armpose1 = Hooks.crossbowPoseOrDefault(ModelBiped.ArmPose.ITEM, clientPlayer, itemstack, itemstack1, EnumHand.OFF_HAND);
             */
            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setModelVisibilities" : "func_177137_d")) {
                    int index = 0;
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("ITEM")) {
                            ASMHandler.LOGGER.debug("transforming - RenderPlayer::setModelVisibilities");
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 4));
                            method.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, "net/minecraft/util/EnumHand", index ++== 0 ? "MAIN_HAND" : "OFF_HAND", "Lnet/minecraft/util/EnumHand;"));
                            method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerRenderPlayer$Hooks", "crossbowPoseOrDefault", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;Lnet/minecraft/client/model/ModelBiped$ArmPose;)Lnet/minecraft/client/model/ModelBiped$ArmPose;", false));
                            if(index == 2) break methods;
                        }
                    }
                }
            }

            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    public static final class Hooks
    {
        @Nonnull
        @SideOnly(Side.CLIENT)
        public static ModelBiped.ArmPose crossbowPoseOrDefault(@Nonnull EntityLivingBase entity, @Nonnull ItemStack mainItem, @Nonnull ItemStack offItem, @Nonnull EnumHand hand, @Nonnull ModelBiped.ArmPose defaultPose) {
            if(entity.isSwingInProgress) return defaultPose;
            else if(entity.getItemInUseCount() > 0) {
                if((hand == EnumHand.MAIN_HAND ? mainItem : offItem).getItemUseAction() == ICrossbow.CROSSBOW_ACTION && hand == entity.getActiveHand()) return CrossbowArmPose.CHARGE;
            }
            else {
                if(hand == EnumHand.OFF_HAND && mainItem.getItemUseAction() != EnumAction.NONE) return defaultPose;
                final ICrossbowProjectiles cap = ICrossbowProjectiles.get(hand == EnumHand.MAIN_HAND ? mainItem : offItem);
                if(cap != null && !cap.isEmpty()) return CrossbowArmPose.HOLD;
            }

            return defaultPose;
        }
    }
}
