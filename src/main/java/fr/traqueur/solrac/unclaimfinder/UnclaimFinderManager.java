package fr.traqueur.solrac.unclaimfinder;

import fr.traqueur.solrac.SolracUnclaimFinder;
import fr.traqueur.solrac.unclaimfinder.exceptions.MaterialNotExistException;
import fr.traqueur.solrac.unclaimfinder.exceptions.UnclaimFinderNotExistException;
import fr.traqueur.solrac.unclaimfinder.tasks.UnclaimFinderScanTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UnclaimFinderManager {

    public final SolracUnclaimFinder plugin;
    private final HashMap<Material, Integer> blocksDetectByUnclaimFinder;

    private Material UNCLAIMFINDER_MATERIAL;
    private int UNCLAIMFINDER_CUSTOMMODELDATA;
    private int UNCLAIMFINDER_RADIUS;

    public UnclaimFinderManager(SolracUnclaimFinder plugin) {
        this.plugin = plugin;
        this.blocksDetectByUnclaimFinder = new HashMap<>();
    }

    public void init() throws UnclaimFinderNotExistException, MaterialNotExistException {
        FileConfiguration config = this.plugin.getConfig();
        String materialName = config.getString("unclaimfinder.material");
        int data = config.getInt("unclaimfinder.custommodeldata");
        int radius = config.getInt("unclaimfinder.radius");

        if(materialName == null) {
            throw new UnclaimFinderNotExistException();
        }

        Material material = Material.getMaterial(materialName);
        if(material == null) {
            throw new UnclaimFinderNotExistException();
        }

        UNCLAIMFINDER_CUSTOMMODELDATA = data;
        UNCLAIMFINDER_MATERIAL = material;
        UNCLAIMFINDER_RADIUS = radius;
        this.generateBlockList();
    }

    private void generateBlockList() throws MaterialNotExistException {
        FileConfiguration config = this.plugin.getConfig();
        if (config.contains("blocks")) {
            Set<String> keys = config.getConfigurationSection("blocks").getKeys(false);
            for (String key : keys) {
                String path = "blocks." + key + ".";

                String materialName = config.getString(path + "material");
                if(materialName == null) {
                    throw new MaterialNotExistException();
                }
                Material material = Material.getMaterial(materialName);
                int percent = config.getInt(path + "percent");
                this.blocksDetectByUnclaimFinder.put(material, percent);
            }
        }
    }

    public boolean isUnclaimFinder(ItemStack item) {
        if(item == null || item.isEmpty() || !item.hasItemMeta()){
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return (item.getType() == UNCLAIMFINDER_MATERIAL) && meta.hasCustomModelData()
                && (meta.getCustomModelData() == UNCLAIMFINDER_CUSTOMMODELDATA);
    }

    public void handle(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getItemInMainHand();
        Action action = event.getAction();

        if(!this.isUnclaimFinder(item)) {
            return;
        }

        if(!action.isRightClick()) {
            return;
        }

        Title title = Title.title(Component.text(""), Component.text("Calcul en cours..."));
        player.showTitle(title);

        event.setCancelled(true);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new UnclaimFinderScanTask(this.plugin, event));

    }

    public HashMap<Material, Integer> getBlocksDetectByUnclaimFinder() {
        return blocksDetectByUnclaimFinder;
    }

    public int getUNCLAIMFINDER_RADIUS() {
        return UNCLAIMFINDER_RADIUS;
    }
}
