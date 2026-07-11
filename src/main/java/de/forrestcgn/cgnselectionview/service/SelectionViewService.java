package de.forrestcgn.cgnselectionview.service;

import de.forrestcgn.cgnselectionview.config.SelectionViewConfig;
import de.forrestcgn.cgnselectionview.render.CuboidParticleRenderer;
import de.forrestcgn.cgnselectionview.worldedit.WorldEditSelectionReader;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SelectionViewService {
    private final SelectionViewConfig config;
    private final Map<UUID, Boolean> sessionPreferences = new HashMap<>();
    private int ticksUntilRender = 1;

    public SelectionViewService(SelectionViewConfig config) {
        this.config = config;
    }

    public boolean isEnabled(UUID playerId) {
        SelectionViewConfig.Values values = config.values();
        if (!values.allowPlayerToggle()) {
            return values.defaultEnabled();
        }
        return sessionPreferences.getOrDefault(playerId, values.defaultEnabled());
    }

    public void setEnabled(UUID playerId, boolean enabled) {
        sessionPreferences.put(playerId, enabled);
    }

    public boolean toggle(UUID playerId) {
        boolean enabled = !isEnabled(playerId);
        sessionPreferences.put(playerId, enabled);
        return enabled;
    }

    public void onConfigReload() {
        ticksUntilRender = 1;
    }

    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        sessionPreferences.remove(event.getEntity().getUUID());
    }

    public void onServerTick(ServerTickEvent.Post event) {
        ticksUntilRender--;
        if (ticksUntilRender > 0) {
            return;
        }

        SelectionViewConfig.Values values = config.values();
        ticksUntilRender = values.renderIntervalTicks();

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!isEnabled(player.getUUID())) {
                continue;
            }

            WorldEditSelectionReader.getCuboidSelection(player)
                    .ifPresent(region -> CuboidParticleRenderer.render(player, region, values));
        }
    }
}
