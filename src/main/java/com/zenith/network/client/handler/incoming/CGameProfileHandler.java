package com.zenith.network.client.handler.incoming;

import com.github.steveice10.mc.protocol.data.ProtocolState;
import com.github.steveice10.mc.protocol.packet.login.clientbound.ClientboundGameProfilePacket;
import com.zenith.network.client.ClientSession;
import com.zenith.network.registry.PacketHandler;

public class CGameProfileHandler implements PacketHandler<ClientboundGameProfilePacket, ClientSession> {
    @Override
    public ClientboundGameProfilePacket apply(final ClientboundGameProfilePacket packet, final ClientSession session) {
        session.getPacketProtocol().setState(ProtocolState.GAME);
        return null;
    }
}