package de.forrestcgn.cgnselectionview;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(CGNSelectionView.MOD_ID)
public final class CGNSelectionView {
    public static final String MOD_ID = "cgn_selection_view";
    public static final String VERSION = "0.1.0";

    public CGNSelectionView() {
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("wesv")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> showInfo(context.getSource()))
                        .then(Commands.literal("info")
                                .executes(context -> showInfo(context.getSource())))
        );
    }

    private int showInfo(net.minecraft.commands.CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
                "CGN SelectionView v" + VERSION
                        + " | Grundmodul aktiv / Base module active"
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Auswahl-Anzeige folgt im nächsten Schritt. / Selection rendering follows in the next step."
        ), false);
        return Command.SINGLE_SUCCESS;
    }
}
