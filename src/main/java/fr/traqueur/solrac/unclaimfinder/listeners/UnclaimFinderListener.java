package fr.traqueur.solrac.unclaimfinder.listeners;

import fr.traqueur.solrac.unclaimfinder.UnclaimFinderManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class UnclaimFinderListener implements Listener {

    private UnclaimFinderManager unclaimFinderManager;

    public UnclaimFinderListener(UnclaimFinderManager unclaimFinderManager) {
        this.unclaimFinderManager = unclaimFinderManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        this.unclaimFinderManager.handle(event);
    }
}
