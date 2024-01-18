package fr.traqueur.solrac.unclaimfinder.tasks;

import fr.traqueur.solrac.SolracUnclaimFinder;
import fr.traqueur.solrac.unclaimfinder.UnclaimFinderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
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

        int fromX = (origin.getX() - radius);
        int toX = fromX + (radius * 2 + 1) - 1;
        int fromZ = (origin.getZ() - radius);
        int toZ = fromZ + (radius * 2 + 1) - 1;

        int xmin = Math.min(fromX, toX), xmax = Math.max(fromX, toX), zmin = Math.min(fromZ, toZ), zmax = Math.max(fromZ, toZ);

        List<Thread> threads = new ArrayList<>();
        List<ChunkSnapshot> chunkSnapshots = new ArrayList<>();
        AtomicInteger atomicPercent = new AtomicInteger();

        for (int x = xmin; x <= xmax; x++) {
            for (int z = zmin; z <= zmax; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                ChunkSnapshot snapshot;
                if (!chunk.isLoaded()) {
                    chunk.load();
                    snapshot = chunk.getChunkSnapshot(false, false, false);
                    chunk.unload();
                } else {
                    snapshot = chunk.getChunkSnapshot();
                }
                chunkSnapshots.add(snapshot);
            }
        }

        for (ChunkSnapshot chunk: chunkSnapshots) {
            try {
                Runnable runnable = () -> {
                    for (int y = YMIN; y <= YMAX; y++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Material blockMaterial = chunk.getBlockType(x, y,z);
                                if(this.unclaimFinderManager.getBlocksDetectByUnclaimFinder().containsKey(blockMaterial)) {
                                    atomicPercent.addAndGet(this.unclaimFinderManager.getBlocksDetectByUnclaimFinder().get(blockMaterial));
                                }
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
