package de.forrestcgn.cgnselectionview.worldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.neoforge.NeoForgeAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class WorldEditSelectionReader {
    private WorldEditSelectionReader() {
    }

    public static Optional<CuboidRegion> getCuboidSelection(ServerPlayer serverPlayer) {
        Player worldEditPlayer = NeoForgeAdapter.get().fromNativePlayer(serverPlayer);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);

        try {
            Region region = session.getSelection(worldEditPlayer.getWorld());
            if (region instanceof CuboidRegion cuboidRegion) {
                return Optional.of(cuboidRegion);
            }
        } catch (IncompleteRegionException ignored) {
            // No complete selection yet.
        }

        return Optional.empty();
    }
}
