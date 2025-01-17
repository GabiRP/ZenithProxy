package com.zenith.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.Command;
import com.zenith.command.CommandUsage;
import com.zenith.command.brigadier.CommandCategory;
import com.zenith.command.brigadier.CommandContext;
import com.zenith.feature.world.World;
import com.zenith.feature.world.raycast.RaycastHelper;
import com.zenith.module.impl.PlayerSimulation;

import static com.zenith.Shared.MODULE;

public class RaycastCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.simple(
            "raycast",
            CommandCategory.INFO,
            "Debug testing command. Gets the block or entity the player is currently looking at."
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("raycast").executes(c -> {
           var result = RaycastHelper.playerBlockOrEntityRaycast(4.5);
           var embed = c.getSource().getEmbed();
           embed.title("Raycast Result")
               .addField("Hit", result.hit(), false)
               .primaryColor();
           if (result.isBlock()) {
                embed.addField("Block", result.block().block().toString(), false)
                     .addField("Pos", result.block().x() + ", " + result.block().y() + ", " + result.block().z(), false)
                     .addField("Direction", result.block().direction().name(), false);
                if (result.hit()) {
                    embed.addField("State", World.getBlockState(result.block().x(), result.block().y(), result.block().z()).toString(), false);
                }
           } else if (result.isEntity()) {
               var type = result.entity().entityType();
               embed.addField("Entity", type != null ? type : "N/A", false);
           }
        })
            .then(literal("e").executes(c -> {
                var result = RaycastHelper.playerEntityRaycast(4.5);
                c.getSource().getEmbed()
                    .title("Raycast Result")
                    .addField("Hit", result.hit(), false)
                    .addField("Entity", result.entity() != null ? result.entityType() != null ? result.entityType() : "N/A" : "N/A", false)
                    .addField("ID", result.entity() != null ? result.entity().getEntityId() : "N/A", false)
                    .primaryColor();
            }))
            .then(literal("b").executes(c -> {
                var result = RaycastHelper.playerBlockRaycast(MODULE.get(PlayerSimulation.class).getBlockReachDistance(), false);
                c.getSource().getEmbed()
                    .title("Raycast Result")
                    .addField("Hit", result.hit(), false)
                    .addField("Block", result.block().toString(), false)
                    .addField("Pos", result.x() + ", " + result.y() + ", " + result.z(), false)
                    .addField("Direction", result.direction().name(), false)
                    .primaryColor();
                if (result.hit()) {
                    c.getSource().getEmbed().addField("State", World.getBlockState(result.x(), result.y(), result.z()).toString(), false);
                }
            }));
    }
}
