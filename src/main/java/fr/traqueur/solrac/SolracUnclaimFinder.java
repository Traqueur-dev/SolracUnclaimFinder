package fr.traqueur.solrac;

import fr.traqueur.solrac.unclaimfinder.UnclaimFinderManager;
import fr.traqueur.solrac.unclaimfinder.exceptions.MaterialNotExistException;
import fr.traqueur.solrac.unclaimfinder.exceptions.UnclaimFinderNotExistException;
import fr.traqueur.solrac.unclaimfinder.listeners.UnclaimFinderListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SolracUnclaimFinder extends JavaPlugin {

    private UnclaimFinderManager unclaimFinderManager;

    @Override
    public void onEnable() {

        if(!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }

        this.saveDefaultConfig();

        this.unclaimFinderManager = new UnclaimFinderManager(this);
        try {
            this.unclaimFinderManager.init();
        } catch (UnclaimFinderNotExistException | MaterialNotExistException e) {
            e.printStackTrace();
            this.getServer().shutdown();
        }

        Bukkit.getPluginManager().registerEvents(new UnclaimFinderListener(this.unclaimFinderManager), this);
    }

    @Override
    public void onDisable() {
        this.saveConfig();
    }

    public UnclaimFinderManager getUnclaimFinderManager() {
        return unclaimFinderManager;
    }
}
