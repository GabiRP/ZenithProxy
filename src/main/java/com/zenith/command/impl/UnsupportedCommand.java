package com.zenith.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.Proxy;
import com.zenith.command.Command;
import com.zenith.command.CommandUsage;
import com.zenith.command.brigadier.CommandCategory;
import com.zenith.command.brigadier.CommandContext;
import com.zenith.command.brigadier.CommandSource;
import com.zenith.discord.Embed;

import static com.zenith.Shared.CONFIG;
import static com.zenith.command.brigadier.CustomStringArgumentType.getString;
import static com.zenith.command.brigadier.CustomStringArgumentType.wordWithChars;
import static com.zenith.command.brigadier.ToggleArgumentType.getToggle;
import static com.zenith.command.brigadier.ToggleArgumentType.toggle;
import static com.zenith.discord.DiscordBot.escape;
import static com.zenith.util.Config.Authentication.AccountType.OFFLINE;
import static java.util.Arrays.asList;

public class UnsupportedCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.args(
            "unsupported",
            CommandCategory.MANAGE,
            """
            Unsupported settings that cause critical security issues.
            
            Do not use edit these unless you absolutely understand what you are doing.
            
            No user support will be provided if you modify any of these settings.
            
            All subcommands are only usable from the terminal.
            """,
            asList(
                "whitelist on/off",
                "verifyUsers on/off",
                "auth type offline",
                "auth offlineUsername <username>"
            )
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("unsupported")
            .requires(c -> Command.validateCommandSource(c, CommandSource.TERMINAL))
            .then(literal("whitelist").then(argument("toggle", toggle()).executes(c -> {
                CONFIG.server.extra.whitelist.enable = getToggle(c, "toggle");
                c.getSource().getEmbed()
                    .title("Whitelist " + toggleStrCaps(CONFIG.server.extra.whitelist.enable));
                return OK;
            })))
            .then(literal("verifyUsers").then(argument("toggle", toggle()).executes(c -> {
                CONFIG.server.verifyUsers = getToggle(c, "toggle");
                c.getSource().getEmbed()
                    .title("Verify Users " + toggleStrCaps(CONFIG.server.verifyUsers));
                return OK;
            })))
            .then(literal("auth")
                      .then(literal("type").then(literal("offline").executes(c -> {
                          CONFIG.authentication.accountType = OFFLINE;
                            c.getSource().getEmbed()
                                .title("Authentication Type Set");
                          Proxy.getInstance().cancelLogin();
                          Proxy.getInstance().getAuthenticator().clearAuthCache();
                      })))
                      .then(literal("offlineUsername").then(argument("username", wordWithChars()).executes(c -> {
                            CONFIG.authentication.username = getString(c, "username");
                            c.getSource().getEmbed()
                                .title("Offline Username Set");
                            Proxy.getInstance().cancelLogin();
                            Proxy.getInstance().getAuthenticator().clearAuthCache();
                            return OK;
                      }))));
    }

    @Override
    public void postPopulate(Embed builder) {
        builder
            .addField("Whitelist", toggleStr(CONFIG.server.extra.whitelist.enable), false)
            .addField("Verify Users", toggleStr(CONFIG.server.verifyUsers), false)
            .addField("Offline Authentication", toggleStr(CONFIG.authentication.accountType == OFFLINE), false)
            .addField("Offline Username", escape(CONFIG.authentication.username), false)
            .primaryColor();
    }
}
