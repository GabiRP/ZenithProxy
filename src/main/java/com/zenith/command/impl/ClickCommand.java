package com.zenith.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.Proxy;
import com.zenith.command.Command;
import com.zenith.command.CommandUsage;
import com.zenith.command.brigadier.CommandCategory;
import com.zenith.command.brigadier.CommandContext;
import com.zenith.discord.Embed;
import com.zenith.feature.world.Input;
import com.zenith.util.Config.Client.Extra.Click.HoldRightClickMode;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.zenith.Shared.CONFIG;
import static com.zenith.Shared.PATHING;
import static com.zenith.command.brigadier.ToggleArgumentType.getToggle;
import static com.zenith.command.brigadier.ToggleArgumentType.toggle;
import static java.util.Arrays.asList;

public class ClickCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.args(
            "click",
            CommandCategory.MODULE,
            """
            Simulates a click to the block or entity in front of you
            """,
            asList(
                "left",
                "left hold",
                "right",
                "right hold",
                "right hold <mainHand/offHand/alternate>",
                "right hold interval <ticks>",
                "reach add <float>",
                "hold forceRotation on/off",
                "hold forceRotation <yaw> <pitch>",
                "stop"
            )
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("click")
            .then(literal("stop").executes(c -> {
                CONFIG.client.extra.click.holdLeftClick = false;
                CONFIG.client.extra.click.holdRightClick = false;
                c.getSource().getEmbed()
                    .title("Click Hold Off")
                    .primaryColor();
                return OK;
            }))
            .then(literal("left").requires((ctx) -> isClientConnected()).executes(c -> {
                var input = new Input();
                input.leftClick = true;
                PATHING.move(input, 100000);
                c.getSource().getEmbed()
                    .title("Left Clicked")
                    .primaryColor();
                return 1;
            })
                      .then(literal("hold").executes(c -> {
                          CONFIG.client.extra.click.holdLeftClick = true;
                          CONFIG.client.extra.click.holdRightClick = false;
                          c.getSource().getEmbed()
                              .title("Left Click Hold")
                              .primaryColor();
                          return OK;
                      })))
            .then(literal("right").requires((ctx) -> isClientConnected()).executes(c -> {
                var input = new Input();
                input.rightClick = true;
                PATHING.move(input, 100000);
                c.getSource().getEmbed()
                    .title("Right Clicked")
                    .primaryColor();
                return 1;
            })
                      .then(literal("hold")
                                .executes(c -> {
                                    CONFIG.client.extra.click.holdLeftClick = false;
                                    CONFIG.client.extra.click.holdRightClick = true;
                                    CONFIG.client.extra.click.holdRightClickMode = HoldRightClickMode.MAIN_HAND;
                                    c.getSource().getEmbed()
                                        .title("Right Click Hold")
                                        .primaryColor();
                                    return OK;
                                })
                                .then(literal("mainHand").executes(c -> {
                                    CONFIG.client.extra.click.holdLeftClick = false;
                                    CONFIG.client.extra.click.holdRightClick = true;
                                    CONFIG.client.extra.click.holdRightClickMode = HoldRightClickMode.MAIN_HAND;
                                    c.getSource().getEmbed()
                                        .title("Right Click Hold (Main Hand)")
                                        .primaryColor();
                                    return OK;
                                }))
                                .then(literal("offHand").executes(c -> {
                                    CONFIG.client.extra.click.holdLeftClick = false;
                                    CONFIG.client.extra.click.holdRightClick = true;
                                    CONFIG.client.extra.click.holdRightClickMode = HoldRightClickMode.OFF_HAND;
                                    c.getSource().getEmbed()
                                        .title("Right Click Hold (Offhand)")
                                        .primaryColor();
                                    return OK;
                                }))
                                .then(literal("alternate").executes(c -> {
                                    CONFIG.client.extra.click.holdLeftClick = false;
                                    CONFIG.client.extra.click.holdRightClick = true;
                                    CONFIG.client.extra.click.holdRightClickMode = HoldRightClickMode.ALTERNATE_HANDS;
                                    c.getSource().getEmbed()
                                        .title("Right Click Hold (Alternate)")
                                        .primaryColor();
                                    return OK;
                                }))
                                .then(literal("interval").then(argument("ticks", integer(0, 100)).executes(c -> {
                                    CONFIG.client.extra.click.holdRightClickInterval = getInteger(c, "ticks");
                                    c.getSource().getEmbed()
                                        .title("Right Click Hold Interval Set")
                                        .primaryColor();
                                    return OK;
                                })))))
            .then(literal("reach").then(literal("add").then(argument("reach", floatArg(0, 10)).executes(c -> {
                float f = getFloat(c, "reach");
                CONFIG.client.extra.click.additionalBlockReach = f;
                c.getSource().getEmbed()
                    .title("Additional Reach Set")
                    .primaryColor();
                return OK;
            }))))
            .then(literal("hold")
                      .then(literal("forceRotation")
                                .then(argument("toggle", toggle()).executes(c -> {
                                    CONFIG.client.extra.click.hasRotation = getToggle(c, "toggle");
                                    c.getSource().getEmbed()
                                        .title("Hold Force Rotation Set")
                                        .primaryColor();
                                    return OK;
                                }))
                                .then(argument("yaw", floatArg(-180, 180)).then(argument("pitch", floatArg(-90, 90)).executes(c -> {
                                    CONFIG.client.extra.click.hasRotation = true;
                                    CONFIG.client.extra.click.rotationYaw = getFloat(c, "yaw");
                                    CONFIG.client.extra.click.rotationPitch = getFloat(c, "pitch");
                                    c.getSource().getEmbed()
                                        .title("Hold Force Rotation Set")
                                        .primaryColor();
                                    return OK;
                                })))));
    }

    private boolean isClientConnected() {
        return Proxy.getInstance().isConnected();
    }

    @Override
    public void postPopulate(Embed embed) {
        embed
            .addField("Click Hold", CONFIG.client.extra.click.holdLeftClick ? "Left" : CONFIG.client.extra.click.holdRightClick ? "Right" : "off", false)
            .addField("Click Hold Force Rotation", toggleStr(CONFIG.client.extra.click.hasRotation) + (
                CONFIG.client.extra.click.hasRotation
                    ? " [" + String.format("%.2f", CONFIG.client.extra.click.rotationYaw) + ", " + String.format("%.2f", CONFIG.client.extra.click.rotationPitch) + "]"
                    : ""), false)
            .addField("Right Click Hold Mode", rightClickHoldModeToString(CONFIG.client.extra.click.holdRightClickMode), false)
            .addField("Right Click Hold Interval", CONFIG.client.extra.click.holdRightClickInterval + " ticks", false)
            .addField("Additional Reach", CONFIG.client.extra.click.additionalBlockReach, false)
            .primaryColor();
    }

    private String rightClickHoldModeToString(HoldRightClickMode mode) {
        return switch (mode) {
            case MAIN_HAND -> "mainHand";
            case OFF_HAND -> "offHand";
            case ALTERNATE_HANDS -> "alternate";
        };
    }
}
