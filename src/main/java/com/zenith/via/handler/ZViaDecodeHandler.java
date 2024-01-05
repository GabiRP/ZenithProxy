package com.zenith.via.handler;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.zenith.network.client.ClientSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

@ChannelHandler.Sharable
public class ZViaDecodeHandler extends MessageToMessageDecoder<ByteBuf> {
    private final UserConnection info;
    private final ClientSession client;

    public ZViaDecodeHandler(UserConnection info, final ClientSession client) {
        this.info = info;
        this.client = client;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        try {
            if (!info.checkIncomingPacket()) throw CancelDecoderException.generate(null);
            if (!info.shouldTransformPacket()) {
                out.add(bytebuf.retain());
                return;
            }

            ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
            try {
                info.transformIncoming(transformedBuf, CancelDecoderException::generate);
                out.add(transformedBuf.retain());
            } finally {
                transformedBuf.release();
            }
        } catch (final Exception e) {
            if (e instanceof CancelCodecException) {
                out.add(bytebuf.retain());
                return;
            }
            if (!this.client.callPacketError(e))
                throw e;
        }
    }
}