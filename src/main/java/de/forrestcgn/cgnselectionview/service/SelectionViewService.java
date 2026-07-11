package de.forrestcgn.cgnselectionview.service;

import de.forrestcgn.cgnselectionview.render.CuboidParticleRenderer;
import de.forrestcgn.cgnselectionview.worldedit.WorldEditSelectionReader;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SelectionViewService {
    private static final int RENDER_INTERVAL_TICKS = 10;

    private final Set<UUID> enabledPlayers = new HashSet<>();
    private int ticksUntilRender = RENDER_INTERVAL_TICKS;

    public boolean isEnabled(UUID playerId) {
        return enabledPlayers.contains(playerId);
    }

    public void setEnabled(UUID playerId, boolean enabled) {
        if (enabled) {
            enabledPlayers.add(playerId);
        } else {
            enabledPlayers.remove(playerId);
        }
    }

    public boolean toggle(UUID playerId) {
        if (enabledPlayers.remove(playerId)) {
            return false;
        }

        enabledPlayers.add(playerId);
        return true;
    }

    public void onServerTick(ServerTickEvent.Post event) {
        if (enabledPlayers.isEmpty()) {
            return;
        }

        ticksUntilRender--;
        if (ticksUntilRender > 0) {
            return;
        }
        ticksUntilRender = RENDER_INTERVAL_TICKS;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!enabledPlayers.contains(player.getUUID())) {
                continue;
            }

            WorldEditSelectionReader.getCuboidSelection(player)
                    .ifPresent(region -> CuboidParticleRenderer.render(player, region));
        }
    }
}
