package de.forrestcgn.cgnselectionview.permission;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

public final class SelectionViewPermissions {
    private static final String PERMISSION_NAMESPACE = "cgnselectionview";

    public static final PermissionNode<Boolean> USE = createNode(
            "use",
            true,
            "CGN SelectionView verwenden",
            "Erlaubt den Zugriff auf die persönlichen CGN-SelectionView-Befehle."
    );
    public static final PermissionNode<Boolean> TOGGLE = createNode(
            "toggle",
            true,
            "CGN SelectionView umschalten",
            "Erlaubt /cgnsv on, off und toggle für die eigene Sitzung."
    );
    public static final PermissionNode<Boolean> INFO = createNode(
            "info",
            true,
            "CGN SelectionView Informationen",
            "Erlaubt /cgnsv und /cgnsv info."
    );
    public static final PermissionNode<Boolean> RELOAD = createNode(
            "reload",
            false,
            "CGN SelectionView Config neu laden",
            "Erlaubt /cgnsv reload ohne Operatorrechte."
    );

    private SelectionViewPermissions() {
    }

    public static void register(PermissionGatherEvent.Nodes event) {
        event.addNodes(USE, TOGGLE, INFO, RELOAD);
    }

    public static boolean canViewInfo(CommandSourceStack source) {
        return isPlayerAllowed(source, USE) && isPlayerAllowed(source, INFO)
                || Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source);
    }

    public static boolean canToggle(CommandSourceStack source) {
        return isPlayerAllowed(source, USE) && isPlayerAllowed(source, TOGGLE);
    }

    public static boolean canReload(CommandSourceStack source) {
        if (Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source)) {
            return true;
        }

        ServerPlayer player = source.getPlayer();
        return player != null && hasPermission(player, RELOAD);
    }

    private static boolean isPlayerAllowed(CommandSourceStack source, PermissionNode<Boolean> node) {
        ServerPlayer player = source.getPlayer();
        return player != null && hasPermission(player, node);
    }

    private static boolean hasPermission(ServerPlayer player, PermissionNode<Boolean> node) {
        try {
            return PermissionAPI.getPermission(player, node);
        } catch (RuntimeException ignored) {
            return node.getDefaultResolver().resolve(player, player.getUUID());
        }
    }

    private static PermissionNode<Boolean> createNode(
            String path,
            boolean defaultValue,
            String readableName,
            String description
    ) {
        return new PermissionNode<>(
                PERMISSION_NAMESPACE,
                path,
                PermissionTypes.BOOLEAN,
                (player, playerUUID, context) -> defaultValue
        ).setInformation(Component.literal(readableName), Component.literal(description));
    }
}
