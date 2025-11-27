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
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Crossbow.MODID, value = Side.CLIENT)
final class FirstPersonHandler
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void hideFirstPersonHand(@Nonnull final RenderSpecificHandEvent event) {
        @Nonnull final EntityPlayerSP player = Minecraft.getMinecraft().player;

        // Hide opposite hand while crossbow is reloading.
        if(player.isHandActive()) {
            if(player.getActiveHand() != event.getHand() && ICrossbowProjectiles.get(player.getActiveItemStack()) != null) event.setCanceled(true);
        }

        // Hide off-hand if main-hand has a loaded crossbow.
        else if(event.getHand() == EnumHand.OFF_HAND) {
            @Nullable final ICrossbowProjectiles cap = ICrossbowProjectiles.get(Minecraft.getMinecraft().getItemRenderer().itemStackMainHand);
            if(cap != null && !cap.isEmpty()) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void renderHeldCrossbow(@Nonnull final RenderSpecificHandEvent event) {
        @Nonnull final ItemStack stack = event.getItemStack();
        @Nullable final ICrossbowProjectiles cap = stack.isEmpty() ? null : ICrossbowProjectiles.get(stack);
        if(cap != null) {
            GlStateManager.pushMatrix();

            @Nonnull final EntityPlayerSP player = Minecraft.getMinecraft().player;
            @Nonnull final ItemRenderer renderer = Minecraft.getMinecraft().getItemRenderer();
            @Nonnull final EnumHandSide arm = event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();

            final boolean isRightArm = arm == EnumHandSide.RIGHT;
            final int armOffset = isRightArm ? 1 : -1;

            // Render offset for reloading crossbow.
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
                // Apply vanilla render offsets.
                final float swingProgress = event.getSwingProgress();
                GlStateManager.translate(-0.4 * Math.sin(Math.sqrt(swingProgress) * Math.PI) * armOffset, 0.2 * Math.sin(Math.sqrt(swingProgress) * Math.PI * 2), -0.2 * Math.sin(swingProgress * Math.PI));
                renderer.transformSideFirstPerson(arm, event.getEquipProgress());
                renderer.transformFirstPerson(arm, swingProgress);

                // Position loaded crossbow at the center.
                if(event.getHand() == EnumHand.MAIN_HAND && swingProgress < 0.001 && !cap.isEmpty()) {
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
