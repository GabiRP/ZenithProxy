/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2016-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.zenith.server.handler.spectator.incoming.movement;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import com.zenith.server.ServerConnection;
import com.zenith.util.handler.HandlerRegistry;
import com.zenith.util.spectator.entity.SpectatorEntityEnderDragon;
import lombok.NonNull;

public class PlayerPositionRotationSpectatorHandler implements HandlerRegistry.IncomingHandler<ClientPlayerPositionRotationPacket, ServerConnection> {
    @Override
    public boolean apply(@NonNull ClientPlayerPositionRotationPacket packet, @NonNull ServerConnection session) {
        session.getSpectatorPlayerCache()
                .setX(packet.getX())
                .setY(packet.getY())
                .setZ(packet.getZ())
                .setYaw((float) packet.getYaw())
                .setPitch((float) packet.getPitch());
        PlayerPositionRotationSpectatorHandler.updateSpectatorPosition(session);
        return false;
    }

    @Override
    public Class<ClientPlayerPositionRotationPacket> getPacketClass() {
        return ClientPlayerPositionRotationPacket.class;
    }

    // might move this elsewhere, kinda awkward being here
    public static void updateSpectatorPosition(final ServerConnection selfSession) {
        float yaw = getYaw(selfSession);
        selfSession.getProxy().getServerConnections().stream()
                .filter(connection -> !connection.equals(selfSession))
                .forEach(connection -> {
                    connection.send(new ServerEntityTeleportPacket(
                            selfSession.getSpectatorEntityId(),
                            selfSession.getSpectatorPlayerCache().getX(),
                            selfSession.getSpectatorPlayerCache().getY(),
                            selfSession.getSpectatorPlayerCache().getZ(),
                            yaw,
                            selfSession.getSpectatorPlayerCache().getPitch(),
                            false
                    ));
                    connection.send(new ServerEntityHeadLookPacket(
                            selfSession.getSpectatorEntityId(),
                            yaw
                    ));
                });
        selfSession.send(new ServerEntityTeleportPacket(
                selfSession.getSpectatorEntityId(),
                selfSession.getSpectatorPlayerCache().getX(),
                selfSession.getSpectatorPlayerCache().getY(),
                selfSession.getSpectatorPlayerCache().getZ(),
                yaw,
                selfSession.getSpectatorPlayerCache().getPitch(),
                false
        ));
        selfSession.send(new ServerEntityHeadLookPacket(
                selfSession.getSpectatorEntityId(),
                yaw
        ));
    }

    public static float getYaw(final ServerConnection serverConnection) {
        // idk why but dragon is 180 degrees off from what you'd expect
        if (serverConnection.getSpectatorEntity() instanceof SpectatorEntityEnderDragon) {
            return serverConnection.getSpectatorPlayerCache().getYaw() - 180f;
        } else {
            return serverConnection.getSpectatorPlayerCache().getYaw();
        }
    }
}
