package de.forrestcgn.cgnselectionview.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.forrestcgn.cgnselectionview.CGNSelectionView;
import de.forrestcgn.cgnselectionview.service.SelectionViewService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SelectionViewCommands {
    private SelectionViewCommands() {
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            SelectionViewService service
    ) {
        dispatcher.register(
                Commands.literal("cgnsv")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> showInfo(context.getSource(), service))
                        .then(Commands.literal("info")
                                .executes(context -> showInfo(context.getSource(), service)))
                        .then(Commands.literal("on")
                                .executes(context -> setEnabled(context.getSource(), service, true)))
                        .then(Commands.literal("off")
                                .executes(context -> setEnabled(context.getSource(), service, false)))
                        .then(Commands.literal("toggle")
                                .executes(context -> toggle(context.getSource(), service)))
        );
    }

    private static int setEnabled(
            CommandSourceStack source,
            SelectionViewService service,
            boolean enabled
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        service.setEnabled(player.getUUID(), enabled);

        source.sendSuccess(() -> Component.literal(enabled
                ? "[CGNSV] Anzeige aktiviert. / Visualization enabled."
                : "[CGNSV] Anzeige deaktiviert. / Visualization disabled."
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int toggle(
            CommandSourceStack source,
            SelectionViewService service
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        boolean enabled = service.toggle(player.getUUID());

        source.sendSuccess(() -> Component.literal(enabled
                ? "[CGNSV] Anzeige aktiviert. / Visualization enabled."
                : "[CGNSV] Anzeige deaktiviert. / Visualization disabled."
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int showInfo(CommandSourceStack source, SelectionViewService service) {
        ServerPlayer player = source.getPlayer();
        String status = player == null
                ? "nur für Spieler / players only"
                : service.isEnabled(player.getUUID())
                        ? "aktiv / enabled"
                        : "inaktiv / disabled";

        source.sendSuccess(() -> Component.literal(
                "CGN SelectionView v" + CGNSelectionView.VERSION + " | Status: " + status
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Befehle / Commands: /cgnsv on | off | toggle | info"
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Unterstützt: WorldEdit-Quader / Supported: WorldEdit cuboids"
        ), false);
        return Command.SINGLE_SUCCESS;
    }
}
