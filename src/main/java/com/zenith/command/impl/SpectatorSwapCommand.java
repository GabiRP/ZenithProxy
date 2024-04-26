package com.zenith.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.zenith.Proxy;
import com.zenith.command.Command;
import com.zenith.command.CommandUsage;
import com.zenith.command.brigadier.CommandCategory;
import com.zenith.command.brigadier.CommandContext;

import java.util.Optional;

import static com.zenith.Shared.CONFIG;

public class SpectatorSwapCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.simple(
            "swap",
            CommandCategory.MODULE,
            """
            Swaps the current controlling player to spectator mode.
            """
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        // todo: requires?
        return command("swap").executes(c -> {
            var player = Proxy.getInstance().getActivePlayer();
            if (player == null) {
                c.getSource().getEmbed()
                    .title("Unable to Swap")
                    .errorColor()
                    .description("No player is currently controlling the proxy account");
                return;
            }
            if (CONFIG.server.viaversion.enabled) {
                Optional<ProtocolVersion> viaClientProtocolVersion = Via.getManager().getConnectionManager().getConnectedClients().values().stream()
                    .filter(client -> client.getChannel() == player.getSession().getChannel())
                    .map(con -> con.getProtocolInfo().getProtocolVersion())
                    .map(ProtocolVersion::getProtocol)
                    .findFirst();
                // TODO: uncomment when via updated
//                if (viaClientProtocolVersion.isPresent() && viaClientProtocolVersion.get() < ProtocolVersion.v1_20_5.getProtocol()) {
//                    c.getSource().getEmbed()
//                        .title("Unsupported Client MC Version")
//                        .addField("Client Version", viaClientProtocolVersion.get().getName(), false)
//                        .addField("Error", "The client version must be at least 1.20.5 to switch servers", false);
//                    return;
//                }
            }
            player.transferToSpectator(CONFIG.server.getProxyAddressForTransfer(), CONFIG.server.getProxyPortForTransfer());
            var currentProfile = player.getProfileCache().getProfile();
            c.getSource().getEmbed()
                .title("Swap Sent")
                .primaryColor()
                .addField("Player", currentProfile != null ? currentProfile.getName() : "Unknown", false);
        });
    }
}