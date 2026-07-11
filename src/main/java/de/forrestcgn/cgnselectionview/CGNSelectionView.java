package de.forrestcgn.cgnselectionview;

import de.forrestcgn.cgnselectionview.command.SelectionViewCommands;
import de.forrestcgn.cgnselectionview.config.SelectionViewConfig;
import de.forrestcgn.cgnselectionview.permission.SelectionViewPermissions;
import de.forrestcgn.cgnselectionview.service.SelectionViewService;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;

@Mod(CGNSelectionView.MOD_ID)
public final class CGNSelectionView {
    public static final String MOD_ID = "cgn_selection_view";
    public static final String VERSION = "0.2.5";

    private final SelectionViewConfig config;
    private final SelectionViewService selectionViewService;

    public CGNSelectionView() {
        config = new SelectionViewConfig();
        selectionViewService = new SelectionViewService(config);

        NeoForge.EVENT_BUS.addListener(this::registerPermissionNodes);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(selectionViewService::onServerTick);
        NeoForge.EVENT_BUS.addListener(selectionViewService::onPlayerLogout);
    }

    private void registerPermissionNodes(PermissionGatherEvent.Nodes event) {
        SelectionViewPermissions.register(event);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        SelectionViewCommands.register(event.getDispatcher(), selectionViewService, config);
    }
}
