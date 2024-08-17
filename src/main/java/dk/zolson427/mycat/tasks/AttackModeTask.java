package dk.zolson427.mycat.tasks;

import dk.zolson427.mycat.MyCat;
import dk.zolson427.mycat.objects.myCat;
import org.bukkit.entity.*;

import java.util.List;

public class AttackModeTask implements Runnable {
    private final MyCat plugin;

    public AttackModeTask(MyCat instance) {
        this.plugin = instance;
    }

    @Override
    public void run() {
        plugin.logDebug("Running the angry cat target checker!");
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (myCat myCat : MyCat.getCatManager().getAliveCats((player.getUniqueId()))) {
                if (myCat.isAngry()) {
                    Cat cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());
                    // If the cat has no target
                    if (cat != null && !cat.isSitting() && cat.getTarget() == null) {
                        double distance;

                        // if they are in two seperate worlds, it's safe to say that the distance is above 20 lol
                        if (!player.getWorld().getUID().equals(cat.getWorld().getUID())) {
                            distance = 1000;
                        } else {
                            distance = player.getLocation().distance(cat.getLocation());
                        }

                        // If distance is below or equal to 20, find a new target near the player
                        if (distance <= 20.0) {
                            List<Entity> entities = player.getNearbyEntities(13, 13, 13);
                            // Get the closest target
                            double lastDistance = Double.MAX_VALUE;
                            Entity closest = null;
                            for (Entity entity : entities) {
                                double distanceToTarget = player.getLocation().distance(entity.getLocation());
                                if (entity instanceof Monster && distanceToTarget < lastDistance) {
                                    lastDistance = distanceToTarget;
                                    closest = entity;
                                }
                            }
                            if (closest != null) {
                                cat.setTarget((LivingEntity) closest);
                            }
                        }
                    }
                }
            }
        }
    }
}
