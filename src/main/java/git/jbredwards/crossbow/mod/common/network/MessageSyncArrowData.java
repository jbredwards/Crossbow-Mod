/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.network;

import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class MessageSyncArrowData implements IMessage
{
    public int arrowId, piercingLvl;

    public MessageSyncArrowData() {}
    public MessageSyncArrowData(int arrowIdIn, int piercingLvlIn) {
        arrowId = arrowIdIn;
        piercingLvl = piercingLvlIn;
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf) {
        arrowId = new PacketBuffer(buf).readVarInt();
        piercingLvl = new PacketBuffer(buf).readVarInt();
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf) {
        new PacketBuffer(buf).writeVarInt(arrowId).writeVarInt(piercingLvl);
    }

    public enum Handler implements IMessageHandler<MessageSyncArrowData, IMessage>
    {
        INSTANCE;

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull MessageSyncArrowData message, @Nonnull MessageContext ctx) {
            handleSync(message.arrowId, message.piercingLvl);
            return null;
        }

        @SideOnly(Side.CLIENT)
        static void handleSync(int arrowId, int piercingLvl) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final ICrossbowArrowData cap = ICrossbowArrowData.get(Minecraft.getMinecraft().world.getEntityByID(arrowId));
                if(cap != null) cap.setPierceLevel(piercingLvl);
            });
        }
    }
}
