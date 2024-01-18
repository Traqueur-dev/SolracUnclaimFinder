package fr.traqueur.solrac.unclaimfinder.tasks;

import fr.traqueur.solrac.SolracUnclaimFinder;
import fr.traqueur.solrac.unclaimfinder.UnclaimFinderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UnclaimFinderScanTask implements Runnable {

    private final PlayerInteractEvent event;
    private final UnclaimFinderManager unclaimFinderManager;
    private final SolracUnclaimFinder plugin;

    private final int YMAX = 319;
    private final int YMIN = -64;

    public UnclaimFinderScanTask(SolracUnclaimFinder plugin, PlayerInteractEvent event) {
        this.plugin = plugin;
        this.unclaimFinderManager = plugin.getUnclaimFinderManager();
        this.event = event;
    }

    @Override
    public void run() {
        Player player = event.getPlayer();
        Chunk origin = player.getChunk();
        World world = origin.getWorld();
        int radius = this.unclaimFinderManager.getUNCLAIMFINDER_RADIUS();

        int fromX = (origin.getX() - radius) * 16;
        int toX = fromX + 16 * (radius * 2 + 1) - 1;
        int fromZ = (origin.getZ() - radius) * 16;
        int toZ = fromZ + 16 * (radius * 2 + 1) - 1;

        int xmin = Math.min(fromX, toX), xmax = Math.max(fromX, toX), zmin = Math.min(fromZ, toZ), zmax = Math.max(fromZ, toZ);

        List<Thread> threads = new ArrayList<>();
        AtomicInteger atomicPercent = new AtomicInteger();
        for (int y = YMIN; y <= YMAX; y++) {
            try {
                int finalY = y;
                Runnable runnable = () -> {
                    for (int x = xmin; x <= xmax; x++) {
                        for (int z = zmin; z <= zmax; z++) {
                            Material blockMaterial = world.getBlockAt(x, finalY,z).getType();
                            if(this.unclaimFinderManager.getBlocksDetectByUnclaimFinder().containsKey(blockMaterial)) {
                                atomicPercent.addAndGet(this.unclaimFinderManager.getBlocksDetectByUnclaimFinder().get(blockMaterial));
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                threads.add(thread);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            int percentage = Math.min(atomicPercent.get(), 100);
            Title title = Title.title(Component.text(""), Component.text(percentage + "%"));
            player.showTitle(title);
        });

    }
}
