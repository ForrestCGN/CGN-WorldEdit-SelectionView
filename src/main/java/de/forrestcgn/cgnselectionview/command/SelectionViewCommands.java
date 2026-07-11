package de.forrestcgn.cgnselectionview.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.forrestcgn.cgnselectionview.CGNSelectionView;
import de.forrestcgn.cgnselectionview.config.SelectionViewConfig;
import de.forrestcgn.cgnselectionview.permission.SelectionViewPermissions;
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
            SelectionViewService service,
            SelectionViewConfig config
    ) {
        dispatcher.register(
                Commands.literal("cgnsv")
                        .requires(source -> SelectionViewPermissions.canViewInfo(source)
                                || SelectionViewPermissions.canToggle(source)
                                || SelectionViewPermissions.canReload(source))
                        .executes(context -> showInfoIfAllowed(context.getSource(), service, config))
                        .then(Commands.literal("info")
                                .requires(SelectionViewPermissions::canViewInfo)
                                .executes(context -> showInfo(context.getSource(), service, config)))
                        .then(Commands.literal("on")
                                .requires(SelectionViewPermissions::canToggle)
                                .executes(context -> setEnabled(context.getSource(), service, config, true)))
                        .then(Commands.literal("off")
                                .requires(SelectionViewPermissions::canToggle)
                                .executes(context -> setEnabled(context.getSource(), service, config, false)))
                        .then(Commands.literal("toggle")
                                .requires(SelectionViewPermissions::canToggle)
                                .executes(context -> toggle(context.getSource(), service, config)))
                        .then(Commands.literal("reload")
                                .requires(SelectionViewPermissions::canReload)
                                .executes(context -> reload(context.getSource(), service, config)))
        );
    }

    private static int showInfoIfAllowed(
            CommandSourceStack source,
            SelectionViewService service,
            SelectionViewConfig config
    ) {
        if (!SelectionViewPermissions.canViewInfo(source)) {
            source.sendFailure(Component.literal(
                    "[CGNSV] Keine Berechtigung für Informationen. / No permission to view information."
            ));
            return 0;
        }

        return showInfo(source, service, config);
    }

    private static int setEnabled(
            CommandSourceStack source,
            SelectionViewService service,
            SelectionViewConfig config,
            boolean enabled
    ) throws CommandSyntaxException {
        if (!config.values().allowPlayerToggle()) {
            source.sendFailure(Component.literal(
                    "[CGNSV] Umschalten ist in der Config deaktiviert. / Toggling is disabled in the config."
            ));
            return 0;
        }

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
            SelectionViewService service,
            SelectionViewConfig config
    ) throws CommandSyntaxException {
        if (!config.values().allowPlayerToggle()) {
            source.sendFailure(Component.literal(
                    "[CGNSV] Umschalten ist in der Config deaktiviert. / Toggling is disabled in the config."
            ));
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        boolean enabled = service.toggle(player.getUUID());

        source.sendSuccess(() -> Component.literal(enabled
                ? "[CGNSV] Anzeige aktiviert. / Visualization enabled."
                : "[CGNSV] Anzeige deaktiviert. / Visualization disabled."
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(
            CommandSourceStack source,
            SelectionViewService service,
            SelectionViewConfig config
    ) {
        SelectionViewConfig.ReloadResult result = config.reload();
        if (!result.success()) {
            source.sendFailure(Component.literal(
                    "[CGNSV] Config konnte nicht geladen werden / Config reload failed: " + result.message()
            ));
            return 0;
        }

        service.onConfigReload();
        source.sendSuccess(() -> Component.literal(
                "[CGNSV] " + result.message() + " Datei / File: " + config.path()
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int showInfo(
            CommandSourceStack source,
            SelectionViewService service,
            SelectionViewConfig config
    ) {
        ServerPlayer player = source.getPlayer();
        String status = player == null
                ? "nur für Spieler / players only"
                : service.isEnabled(player.getUUID())
                        ? "aktiv / enabled"
                        : "inaktiv / disabled";

        SelectionViewConfig.Values values = config.values();

        source.sendSuccess(() -> Component.literal(
                "CGN SelectionView v" + CGNSelectionView.VERSION + " | Status: " + status
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Standard / Default: " + values.defaultEnabled()
                        + " | Distanz / Distance: " + values.renderDistance()
                        + " | Intervall / Interval: " + values.renderIntervalTicks() + " Ticks"
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Partikel / Particles: Kante / Edge=" + values.edgeParticleStyle().configName()
                        + " | Raster / Grid=" + values.gridParticleStyle().configName()
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Befehle / Commands: /cgnsv on | off | toggle | info | reload"
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Unterstützt: WorldEdit-Quader / Supported: WorldEdit cuboids"
        ), false);
        return Command.SINGLE_SUCCESS;
    }
}
