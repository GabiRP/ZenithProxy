package com.zenith.network.client.handler.postoutgoing;

import com.zenith.Shared;
import com.zenith.network.client.ClientSession;
import com.zenith.network.registry.ClientEventLoopPacketHandler;
import com.zenith.network.registry.PostOutgoingPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundResourcePackPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerStatusOnlyPacket;

import static com.zenith.Shared.CACHE;
public class PostOutgoingResourcePackHandler implements ClientEventLoopPacketHandler<ServerboundResourcePackPacket, ClientSession> {

    @Override
    public boolean applyAsync(final ServerboundResourcePackPacket packet, final ClientSession session) {
        Shared.DEFAULT_LOG.atInfo().log("sent resourcepack response");
        return true;
    }
}
