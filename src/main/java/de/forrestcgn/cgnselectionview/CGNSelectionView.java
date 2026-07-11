package de.forrestcgn.cgnselectionview;

import de.forrestcgn.cgnselectionview.command.SelectionViewCommands;
import de.forrestcgn.cgnselectionview.service.SelectionViewService;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(CGNSelectionView.MOD_ID)
public final class CGNSelectionView {
    public static final String MOD_ID = "cgn_selection_view";
    public static final String VERSION = "0.2.1";

    private final SelectionViewService selectionViewService = new SelectionViewService();

    public CGNSelectionView() {
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(selectionViewService::onServerTick);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        SelectionViewCommands.register(event.getDispatcher(), selectionViewService);
    }
}
