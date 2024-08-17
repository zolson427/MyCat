package dk.zolson427.mycat.listeners;

import dk.zolson427.mycat.MyCat;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

import java.util.List;

public class CatMainListener_1_18 implements Listener {
    private final MyCat plugin;

    public CatMainListener_1_18(MyCat p) {
        this.plugin = p;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityLoad(EntitiesLoadEvent event) {
        CatMainListener.checkForCats(event.getEntities().toArray(new Entity[0]));
    }

    // If entities are unloaded, check if any of them are tameables
    @EventHandler(priority = EventPriority.LOWEST)
    public void oneEntityUnload(EntitiesUnloadEvent event) {
        if (!plugin.automaticTeleportation) {
            return;
        }

        List<Entity> entities = event.getEntities();

        if (plugin.experimentalTeleport) {
            MyCat.getTeleportationManager().teleportEntities(entities, null, "EntityUnload");
        } else {
            MyCat.getTeleportationManager().doTeleportEntities(entities, null);
        }
    }
}
