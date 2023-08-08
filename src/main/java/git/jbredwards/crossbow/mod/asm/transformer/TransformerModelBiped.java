/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm.transformer;

import git.jbredwards.crossbow.mod.asm.ASMHandler;
import git.jbredwards.crossbow.mod.client.model.CrossbowArmPose;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Render crossbow hand animations
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public final class TransformerModelBiped implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        // ModelBiped
        if("net.minecraft.client.model.ModelBiped".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            /*
             * setRotationAngles: (changes are around line 280)
             * Old code:
             * copyModelAngles(this.bipedHead, this.bipedHeadwear);
             *
             * New code:
             * // Apply crossbow arm rotations
             * Hooks.crossbowArmRotation(this, entityIn);
             * copyModelAngles(this.bipedHead, this.bipedHeadwear);
             */
            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setRotationAngles" : "func_78087_a")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        AbstractInsnNode target = insn;
                        for(int i = 0; i < 4 && target.getPrevious() != null; i++) target = target.getPrevious();
                        if(target.getOpcode() == INVOKESTATIC && ((MethodInsnNode)target).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "copyModelAngles" : "func_178685_a")) {
                            ASMHandler.LOGGER.debug("transforming - ModelBiped::setRotationAngles");
                            method.instructions.insertBefore(target, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(target, new VarInsnNode(ALOAD, 7));
                            method.instructions.insertBefore(target, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/crossbow/mod/asm/transformer/TransformerModelBiped$Hooks", "crossbowArmRotation", "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V", false));
                            break methods;
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
        @SideOnly(Side.CLIENT)
        public static void crossbowArmRotation(@Nonnull ModelBiped model, @Nonnull Entity entityIn) {
            if(!(entityIn instanceof EntityLivingBase)) return;

            final EntityLivingBase entity = (EntityLivingBase)entityIn;
            final float pullTime = ItemCrossbow.getPullTime(entity.getActiveItemStack());

            if(model.rightArmPose == CrossbowArmPose.CHARGE) {
                model.bipedRightArm.rotateAngleY = -0.8f;
                model.bipedRightArm.rotateAngleX = -0.97079635f;
                model.bipedLeftArm.rotateAngleX = -0.97079635f;
                final float angle = MathHelper.clamp(entity.getItemInUseMaxCount(), 0, pullTime);
                model.bipedLeftArm.rotateAngleY = 0.4f + angle / pullTime * 0.45f;
                model.bipedLeftArm.rotateAngleX = model.bipedLeftArm.rotateAngleX + angle / pullTime * (-(float)Math.PI / 2 - model.bipedLeftArm.rotateAngleX);
            }

            else if(model.leftArmPose == CrossbowArmPose.CHARGE) {
                model.bipedLeftArm.rotateAngleY = 0.8f;
                model.bipedRightArm.rotateAngleX = -0.97079635f;
                model.bipedLeftArm.rotateAngleX = -0.97079635f;
                final float angle = MathHelper.clamp(entity.getItemInUseMaxCount(), 0, pullTime);
                model.bipedRightArm.rotateAngleY = -0.4f + angle / pullTime * -0.45f;
                model.bipedRightArm.rotateAngleX = model.bipedLeftArm.rotateAngleX + angle / pullTime * (-(float)Math.PI / 2 - model.bipedLeftArm.rotateAngleX);
            }

            if(model.rightArmPose == CrossbowArmPose.HOLD && model.swingProgress <= 0) {
                model.bipedRightArm.rotateAngleY = -0.3f + model.bipedHead.rotateAngleY;
                model.bipedLeftArm.rotateAngleY = 0.6f + model.bipedHead.rotateAngleY;
                model.bipedRightArm.rotateAngleX = -(float)Math.PI / 2 + model.bipedHead.rotateAngleX + 0.1f;
                model.bipedLeftArm.rotateAngleX = -1.5f + model.bipedHead.rotateAngleX;
            }

            else if(model.leftArmPose == CrossbowArmPose.HOLD) {
                model.bipedRightArm.rotateAngleY = -0.6f + model.bipedHead.rotateAngleY;
                model.bipedLeftArm.rotateAngleY = 0.3f + model.bipedHead.rotateAngleY;
                model.bipedRightArm.rotateAngleX = -1.5f + model.bipedHead.rotateAngleX;
                model.bipedLeftArm.rotateAngleX = -(float)Math.PI / 2 + model.bipedHead.rotateAngleX + 0.1f;
            }
        }
    }
}
