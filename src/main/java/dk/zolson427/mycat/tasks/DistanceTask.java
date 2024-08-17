package dk.zolson427.mycat.tasks;

import dk.zolson427.mycat.MyCat;
import dk.zolson427.mycat.objects.myCat;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DistanceTask implements Runnable {
    private final MyCat plugin;

    public DistanceTask(MyCat instance) {
        this.plugin = instance;
    }

    @Override
    public void run() {
        plugin.logDebug("Running the distance checker!");
        List<Entity> entities = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (myCat myCat : MyCat.getCatManager().getAliveCats((player.getUniqueId()))) {
                Cat cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());
                if (cat != null && !cat.isSitting()) {
                    double distance;
                    // if they are in two seperate worlds, it's safe to say that the distance is above 30 lol
                    if (!player.getWorld().getUID().equals(cat.getWorld().getUID())) {
                        distance = 1000;
                    } else {
                        distance = player.getLocation().distance(cat.getLocation());
                    }

                    // A quick dirty check for ground below player
                    if (distance >= 30.0 && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                        if (!plugin.experimentalTeleport) {
                            cat.teleport(player);
                        } else {
                            entities.add(cat);
                        }
                    }
                }
            }
        }

        if (plugin.experimentalTeleport) {
            MyCat.getTeleportationManager().teleportEntities(entities, null, "DistanceChecker");
        }
    }
}
