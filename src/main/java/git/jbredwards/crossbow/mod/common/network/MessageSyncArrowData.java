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
    public boolean wasShotFromCrossbow;

    public MessageSyncArrowData() {}
    public MessageSyncArrowData(int arrowIdIn, @Nonnull ICrossbowArrowData dataIn) {
        arrowId = arrowIdIn;
        piercingLvl = dataIn.getPierceLevel();
        wasShotFromCrossbow = dataIn.wasShotByCrossbow();
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf) {
        arrowId = new PacketBuffer(buf).readVarInt();
        piercingLvl = new PacketBuffer(buf).readVarInt();
        wasShotFromCrossbow = buf.readBoolean();
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf) {
        new PacketBuffer(buf).writeVarInt(arrowId).writeVarInt(piercingLvl);
        buf.writeBoolean(wasShotFromCrossbow);
    }

    public enum Handler implements IMessageHandler<MessageSyncArrowData, IMessage>
    {
        INSTANCE;

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull MessageSyncArrowData message, @Nonnull MessageContext ctx) {
            if(ctx.side.isClient()) handleSync(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        static void handleSync(@Nonnull MessageSyncArrowData message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final ICrossbowArrowData cap = ICrossbowArrowData.get(Minecraft.getMinecraft().world.getEntityByID(message.arrowId));
                if(cap != null) {
                    cap.setPierceLevel(message.piercingLvl);
                    cap.setShotByCrossbow(message.wasShotFromCrossbow);
                }
            });
        }
    }
}
