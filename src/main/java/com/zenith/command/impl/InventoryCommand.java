package com.zenith.command.impl;

import com.github.steveice10.mc.protocol.data.game.inventory.ClickItemAction;
import com.github.steveice10.mc.protocol.data.game.inventory.ContainerActionType;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.Proxy;
import com.zenith.cache.data.inventory.Container;
import com.zenith.command.*;
import com.zenith.discord.Embed;
import com.zenith.module.impl.PlayerSimulation;
import discord4j.rest.util.Color;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.zenith.Shared.*;
import static java.util.Arrays.asList;

public class InventoryCommand extends Command {
    private static final String inventoryAscii =
"""
```

╔═══╦═══════════╗
║ 5 ║    ███    ║   ╔═══╦═══╗
╠═══╣    ███    ║   ║ 1 ║ 2 ║   ╔═══╗
║ 6 ║  ███████  ║   ╠═══╬═══╣   ║ 0 ║
╠═══╣  ███████  ║   ║ 3 ║ 4 ║   ╚═══╝
║ 7 ║  ███████  ║   ╚═══╩═══╝
╠═══╣    ███    ╠═══╗
║ 8 ║    ███    ║45 ║
╚═══╩═══════════╩═══╝
╔═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╗
║ 9 ║10 ║11 ║12 ║13 ║14 ║15 ║16 ║17 ║
╠═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╣
║18 ║19 ║20 ║21 ║22 ║23 ║24 ║25 ║26 ║
╠═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╣
║27 ║28 ║29 ║30 ║31 ║32 ║33 ║34 ║35 ║
╚═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╝
╔═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╗
║36 ║37 ║38 ║39 ║40 ║41 ║42 ║43 ║44 ║
╚═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╝

```
""";

    private static final String inventoryAsciiFormatter =
        """
        ```
        
        ╔═══╦═══════════╗
        ║%6$2s ║    ███    ║   ╔═══╦═══╗
        ╠═══╣    ███    ║   ║%2$2s ║%3$2s ║   ╔═══╗
        ║%7$2s ║  ███████  ║   ╠═══╬═══╣   ║%1$2s ║
        ╠═══╣  ███████  ║   ║%4$2s ║%5$2s ║   ╚═══╝
        ║%8$2s ║  ███████  ║   ╚═══╩═══╝
        ╠═══╣    ███    ╠═══╗
        ║%9$2s ║    ███    ║%46$2s ║
        ╚═══╩═══════════╩═══╝
        ╔═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╗
        ║%10$2s ║%11$2s ║%12$2s ║%13$2s ║%14$2s ║%15$2s ║%16$2s ║%17$2s ║%18$2s ║
        ╠═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╣
        ║%19$2s ║%20$2s ║%21$2s ║%22$2s ║%23$2s ║%24$2s ║%25$2s ║%26$2s ║%27$2s ║
        ╠═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╣
        ║%28$2s ║%29$2s ║%30$2s ║%31$2s ║%32$2s ║%33$2s ║%34$2s ║%35$2s ║%36$2s ║
        ╚═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╝
        ╔═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╗
        ║%37$2s ║%38$2s ║%39$2s ║%40$2s ║%41$2s ║%42$2s ║%43$2s ║%44$2s ║%45$2s ║
        ╚═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╝
        
        ```
        """;

    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.full(
            "inventory",
            CommandCategory.INFO,
            "Shows the player inventory",
            asList(
                "",
                "show",
                "hold <slot>",
                "swap <from> <to>",
                "drop <slot>"
            ),
            asList("inv")
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("inventory")
            .executes(c -> {
                if (!verifyLoggedIn(c.getSource().getEmbed())) return;
                printInvAscii(c.getSource().getMultiLineOutput(), true);
            })
            .then(literal("show").executes(c -> {
                printInvAscii(c.getSource().getMultiLineOutput(), false);
            }))
            .then(literal("hold").then(argument("slot", integer(36, 44)).executes(c -> {
                var client = Proxy.getInstance().getClient();
                if (!verifyAbleToDoInvActions(c.getSource().getEmbed())) return 1;
                var slot = c.getArgument("slot", Integer.class);
                client.sendAsync(new ServerboundSetCarriedItemPacket(slot - 36));
                logInvDelayed();
                c.getSource().setNoOutput(true);
                return 1;
            })))
            .then(literal("swap")
                      .then(argument("from", integer(0, 45)).then(argument("to", integer(0, 45)).executes(c -> {
                          if (!verifyAbleToDoInvActions(c.getSource().getEmbed())) return 1;
                          var from = c.getArgument("from", Integer.class);
                          var to = c.getArgument("to", Integer.class);
                          var sim = MODULE_MANAGER.get(PlayerSimulation.class);
                          sim.addTask(() -> {
                              var fromStack = CACHE.getPlayerCache().getPlayerInventory().get(from);
                              var toStack = CACHE.getPlayerCache().getPlayerInventory().get(to);
                              sim.sendClientPacketAsync(new ServerboundContainerClickPacket(
                                  0,
                                  CACHE.getPlayerCache().getActionId().getAndIncrement(),
                                  from,
                                  ContainerActionType.CLICK_ITEM,
                                  ClickItemAction.LEFT_CLICK,
                                  fromStack,
                                  Int2ObjectMaps.singleton(from, null)
                              ));
                              sim.sendClientPacketAsync(new ServerboundContainerClickPacket(
                                  0,
                                  CACHE.getPlayerCache().getActionId().getAndIncrement(),
                                  to,
                                  ContainerActionType.CLICK_ITEM,
                                  ClickItemAction.LEFT_CLICK,
                                  toStack,
                                  Int2ObjectMaps.singleton(to, fromStack)
                              ));
                              sim.sendClientPacketAsync(new ServerboundContainerClickPacket(
                                  0,
                                  CACHE.getPlayerCache().getActionId().getAndIncrement(),
                                  from,
                                  ContainerActionType.CLICK_ITEM,
                                  ClickItemAction.LEFT_CLICK,
                                  null,
                                  Int2ObjectMaps.singleton(from, toStack)
                              ));
                              logInvDelayed();
                          });
                          c.getSource().setNoOutput(true);
                          return 1;
                      }))))
            .then(literal("drop")
                      .then(argument("slot", integer(0, 44)).executes(c -> {
                          if (!verifyAbleToDoInvActions(c.getSource().getEmbed())) return 1;
                          var slot = c.getArgument("slot", Integer.class);
                          var stack = CACHE.getPlayerCache().getPlayerInventory().get(slot);
                          if (stack == Container.EMPTY_STACK) {
                              c.getSource().getEmbed()
                                  .title("Error")
                                  .description("Slot: " + slot + " is empty")
                                  .color(Color.RUBY);
                              return 1;
                          }
                          var sim = MODULE_MANAGER.get(PlayerSimulation.class);
                          sim.addTask(() -> {
                              sim.sendClientPacketAsync(new ServerboundContainerClickPacket(
                                  0,
                                  CACHE.getPlayerCache().getActionId().getAndIncrement(),
                                  slot,
                                  ContainerActionType.DROP_ITEM,
                                  ClickItemAction.LEFT_CLICK,
                                  stack,
                                  Int2ObjectMaps.singleton(slot, null)
                              ));
                              logInvDelayed();
                          });
                          c.getSource().setNoOutput(true);
                          return 1;
                      })));
    }

    private void logInvDelayed() {
        SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
            final List<String> output = new ArrayList<>();
            printInvAscii(output, false);
            CommandOutputHelper.logMultiLineOutput(output);
        }, 1, TimeUnit.SECONDS);
    }

    private void printInvAscii(final List<String> multiLineOutput, final boolean showAllSlotIds) {
        var playerInv = CACHE.getPlayerCache().getPlayerInventory();
        var sb = new StringBuilder();
        var slotsWithItems = new String[46];
        Arrays.fill(slotsWithItems, "");
        sb.append("```\n");
        var heldSlot = CACHE.getPlayerCache().getHeldItemSlot() + 36;
        for (int i = 0; i < playerInv.size(); i++) {
            var itemStack = playerInv.get(i);
            if (itemStack == Container.EMPTY_STACK) continue;
            slotsWithItems[i] = i + "";
            var itemData = ITEMS_MANAGER.getItemData(itemStack.getId());
            sb.append("  ").append(i).append(" -> ");
            sb.append(itemData.getName());
            if (itemStack.getAmount() > 1) sb.append(" ").append(itemStack.getAmount()).append("x ");
            if (i == heldSlot) sb.append(" (Held)");
            sb.append("\n");
        }
        sb.append("\n```");
        var items = sb.toString();
        if (showAllSlotIds)
            multiLineOutput.add(inventoryAscii);
        else
            multiLineOutput.add(String.format(inventoryAsciiFormatter, (Object[]) slotsWithItems));
        if (items.isEmpty()) {
            multiLineOutput.add("Empty!");
        } else {
            multiLineOutput.add(items);
        }
    }

    private boolean verifyAbleToDoInvActions(final Embed embed) {
        return verifyLoggedIn(embed) && verifyNoActivePlayer(embed);
    }

    private boolean verifyNoActivePlayer(final Embed embed) {
        var client = Proxy.getInstance().getClient();
        if (client == null || !Proxy.getInstance().isConnected()) {
            embed
                .title("Error")
                .description("Not logged in!")
                .color(Color.RUBY);
            return false;
        }
        return true;
    }

    private boolean verifyLoggedIn(final Embed embed) {
        var client = Proxy.getInstance().getClient();
        if (client == null || !Proxy.getInstance().isConnected()) {
            embed
                .title("Error")
                .description("Not logged in!")
                .color(Color.RUBY);
            return false;
        }
        return true;
    }
}
