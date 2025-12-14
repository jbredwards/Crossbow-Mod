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

import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
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
public class MessageSyncFireworkData implements IMessage
{
    public int fireworkId;
    public boolean wasShotFromCrossbow, shotAtAngle;

    public MessageSyncFireworkData() {}
    public MessageSyncFireworkData(int fireworkIdIn, @Nonnull ICrossbowFireworkData dataIn) {
        fireworkId = fireworkIdIn;
        wasShotFromCrossbow = dataIn.wasShotByCrossbow();
        shotAtAngle = dataIn.isShotAtAngle();
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf) {
        fireworkId = new PacketBuffer(buf).readVarInt();
        wasShotFromCrossbow = buf.readBoolean();
        shotAtAngle = buf.readBoolean();
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf) {
        new PacketBuffer(buf).writeVarInt(fireworkId);
        buf.writeBoolean(wasShotFromCrossbow).writeBoolean(shotAtAngle);
    }

    public enum Handler implements IMessageHandler<MessageSyncFireworkData, IMessage>
    {
        INSTANCE;

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull MessageSyncFireworkData message, @Nonnull MessageContext ctx) {
            if(ctx.side.isClient()) handleSync(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        static void handleSync(@Nonnull MessageSyncFireworkData message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final ICrossbowFireworkData cap = ICrossbowFireworkData.get(Minecraft.getMinecraft().world.getEntityByID(message.fireworkId));
                if(cap != null) {
                    cap.setShotByCrossbow(message.wasShotFromCrossbow);
                    cap.setShotAtAngle(message.shotAtAngle);
                }
            });
        }
    }
}
