/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.client;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Crossbow.MODID, value = Side.CLIENT)
final class ClientEventHandler
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void applyOverrides(@Nonnull RenderSpecificHandEvent event) {
        final EntityPlayerSP player = Minecraft.getMinecraft().player;
        final ItemRenderer renderer = Minecraft.getMinecraft().getItemRenderer();
        final boolean isMainHand = event.getHand() == EnumHand.MAIN_HAND;

        if(player.isHandActive()) {
            if(ICrossbowProjectiles.get(player.getActiveItemStack()) != null) {
                final boolean isActiveMainHand = player.getActiveHand() == EnumHand.MAIN_HAND;
                if(!isActiveMainHand && isMainHand) event.setCanceled(true);
                else if(isActiveMainHand && !isMainHand) event.setCanceled(true);
            }

            else if(!isMainHand && player.getActiveHand() == EnumHand.MAIN_HAND) {
                final ICrossbowProjectiles cap = ICrossbowProjectiles.get(renderer.itemStackOffHand);
                if(cap != null && !cap.isEmpty()) event.setCanceled(true);
            }
        }

        else {
            if(!isMainHand) {
                final ICrossbowProjectiles cap = ICrossbowProjectiles.get(renderer.itemStackMainHand);
                if(cap != null && !cap.isEmpty()) {
                    event.setCanceled(true);
                    return;
                }
            }

            final ICrossbowProjectiles cap = ICrossbowProjectiles.get(renderer.itemStackOffHand);
            if(cap != null && !cap.isEmpty()) {
                if(isMainHand && renderer.itemStackMainHand.isEmpty()) event.setCanceled(true);
                else if(!isMainHand) event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    static void renderHeldCrossbow(@Nonnull RenderSpecificHandEvent event) {
        final ICrossbowProjectiles cap = event.getItemStack().isEmpty() ? null : ICrossbowProjectiles.get(event.getItemStack());
        if(cap != null) {
            GlStateManager.pushMatrix();
            final EntityPlayerSP player = Minecraft.getMinecraft().player;
            final ItemRenderer renderer = Minecraft.getMinecraft().getItemRenderer();
            final ItemStack stack = event.getItemStack();

            final EnumHandSide arm = event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
            final boolean isRightArm = arm == EnumHandSide.RIGHT;
            final int armOffset = isRightArm ? 1 : -1;

            if(player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == event.getHand()) {
                renderer.transformSideFirstPerson(arm, event.getEquipProgress());
                GlStateManager.translate(armOffset * -0.4785682, -0.094387, 0.05731531);
                GlStateManager.rotate(-11.935f, 1, 0, 0);
                GlStateManager.rotate(armOffset * 65.3f, 0, 1, 0);
                GlStateManager.rotate(armOffset * -9.785f, 0, 0, 1);

                final float useTime = stack.getMaxItemUseDuration() - (player.getItemInUseCount() - event.getPartialTicks() + 1);
                final float pullTime = Math.min(useTime / (stack.getMaxItemUseDuration() - 3), 1);

                if(pullTime > 0.1) GlStateManager.translate(0, (pullTime - 0.1) * Math.sin((useTime - 0.1) * 1.3) * 0.004, 0);
                GlStateManager.translate(0, 0, pullTime * 0.04);
                GlStateManager.scale(1, 1, 1 + pullTime * 0.2);
                GlStateManager.rotate(armOffset * 45, 0, -1, 0);
            }

            else {
                GlStateManager.translate(
                        armOffset * -0.4 * Math.sin(Math.sqrt(event.getSwingProgress()) * Math.PI),
                        0.2 * Math.sin(Math.sqrt(event.getSwingProgress()) * Math.PI * 2),
                        -0.2 * Math.sin(event.getSwingProgress() * Math.PI)
                );

                renderer.transformSideFirstPerson(arm, event.getEquipProgress());
                renderer.transformFirstPerson(arm, event.getSwingProgress());
                if(!cap.isEmpty() && event.getSwingProgress() < 0.001) {
                    GlStateManager.translate(armOffset * -0.641864, 0, 0);
                    GlStateManager.rotate(armOffset * 10, 0, 1, 0);
                }
            }

            renderer.renderItemSide(player, stack, isRightArm ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightArm);
            event.setCanceled(true);
            GlStateManager.popMatrix();
        }
    }
}
