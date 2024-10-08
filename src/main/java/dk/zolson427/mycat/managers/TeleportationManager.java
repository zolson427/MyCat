package dk.zolson427.mycat.managers;

import dk.zolson427.mycat.MyCat;
import dk.zolson427.mycat.objects.myCat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TeleportationManager {
    private final MyCat plugin;
    private final List<UUID> teleportingEntities = new ArrayList<>();
    private final List<Chunk> entityChunks = new ArrayList<>();

    public TeleportationManager(MyCat p) {
        this.plugin = p;
    }

    // Experimental function
    public void teleportEntities(List<Entity> entities, Location location, String reason) {
        // Let's start by actually seeing whether there is a tameable in the entity list, before we create a new thread pool
        boolean foundTameable = false;
        for (Entity e : entities) {
            // All tameables
            if (e instanceof Sittable && e instanceof Tameable && !teleportingEntities.contains(e.getUniqueId())) {
                foundTameable = true;
                break;
            }
        }
        if (!foundTameable) {
            return;
        }

        // Create new threads to run the teleporting
//        plugin.logDebug("Creating a new threadpool! - " + reason);
//        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        if (reason.equals("PlayerTeleport")) {
            // Do the teleport task 3 ticks after the player has teleported
            new BukkitRunnable() {
                @Override
                public void run() {
                    doTeleportEntities(entities, location, reason);
                }
            }.runTaskLater(this.plugin, 3);
        } else {
            doTeleportEntities(entities, location, reason);
        }

        // Let's remove the entities we just teleported from the list of teleporting entities, once the thread pool has terminated
        new BukkitRunnable() {
            @Override
            public void run() {
//                if (executor.isTerminated()) {
//                    plugin.logDebug("The thread pool is terminated successfully. Removing teleporting entities! - " + reason);
                    for (Entity e : entities) {
                        teleportingEntities.remove(e.getUniqueId());
                    }

                    for (Chunk c : entityChunks) {
                        c.unload(true);
                    }

                    this.cancel();
//                }
            }
        }.runTaskTimer(this.plugin, 20L * 10L, 20L);
    }

    // Experimental function
    private void doTeleportEntities(List<Entity> entities, Location location, String reason) {
        Location safeLocation = null;

        for (Entity e : entities) {
            // All tameables
            if (e instanceof Sittable && e instanceof Tameable && !teleportingEntities.contains(e.getUniqueId()) && !((Sittable) e).isSitting()) {
                teleportingEntities.add(e.getUniqueId());
                HashMap<Boolean, Location> teleportResult = teleportTameableExperimental(e, safeLocation, location);

                // If the first entity didn't find a safe location
                Boolean triedTeleporting = (Boolean) teleportResult.keySet().toArray()[0];
                safeLocation = (Location) teleportResult.values().toArray()[0];

                if (triedTeleporting && safeLocation == null) {
                    break;
                }
            }
        }

//        // Remember to shut down the executor!
//        executor.shutdown();
//        plugin.logDebug("Shutdown of thread pool initiated! - " + reason);
    }

    // Experimental function
    private HashMap<Boolean, Location> teleportTameableExperimental(Entity e, Location safeLocation, Location searchLocation) {
        // We use this map to store whether it even tried searching for a location, and then to return with a safe location, so we don't have to search again
        HashMap<Boolean, Location> teleportResult = new HashMap<>();

        Tameable tameableEntity = (Tameable) e;

        if (tameableEntity == null || tameableEntity.isDead() || !(tameableEntity.getOwner() instanceof Player)) {
            teleportResult.put(false, safeLocation);
            return teleportResult;
        }

        Sittable sittingEntity = (Sittable) e;
        Player player = (Player) tameableEntity.getOwner();

        if (player == null || !player.isOnline() || (!player.isOp() && !MyCat.getPermissionsManager().hasPermission(player, "mycat.teleport"))) {
            teleportResult.put(false, safeLocation);
            return teleportResult;
        }

        // If it's a cat, or if the config allows all tameables to teleport
        boolean isCat = (e.getType().equals(EntityType.CAT) && MyCat.getCatManager().isCat(tameableEntity.getUniqueId()));
        if (!isCat && !plugin.teleportAllTameables) {
            teleportResult.put(false, safeLocation);
            return teleportResult;
        }

        // If the tameable is sitting, or is in another world (without teleport on world change on)
        if (sittingEntity.isSitting() || (!tameableEntity.getWorld().equals(player.getWorld()) && !plugin.teleportOnWorldChange)) {
            teleportResult.put(false, safeLocation);
            return teleportResult;
        }

        plugin.logDebug("An entity that needs to be teleported was found! Running teleporting procedure!");
        plugin.logDebug("UUID: " + tameableEntity.getUniqueId());
        plugin.logDebug("Owner: " + ((Player) tameableEntity.getOwner()).getDisplayName());
        // If cat, save location of the cat
        if (isCat) {
            MyCat.getCatManager().getCat(tameableEntity.getUniqueId()).saveCatLocation();
        }

        // Begin teleport procedure!!
        if (safeLocation == null) {
            // If a search location is provided, like a player that is starting to teleport
            if (searchLocation == null) {
                searchLocation = player.getLocation();
            }
            if (!isSafeLocation(searchLocation)) {
                plugin.logDebug("Whoops, seems like our player isn't at a safe location, let's find a good spot for the tameable...");
                searchLocation = searchSafeLocation(searchLocation);
                if (searchLocation == null) {
                    plugin.logDebug("Did not find a safe place to teleport the tameable! Keeping tameable at unloaded chunks!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.cannotTeleportTameableString.replace("{chatPrefix}", plugin.getChatPrefix())));
                    teleportResult.put(true, null);
                    return teleportResult;
                }
            }
            safeLocation = searchLocation;
        } else {
            plugin.logDebug("Using an already found safe location!");
        }

        plugin.logDebug("Teleporting to a safe location! - X:" + safeLocation.getX() + " Y:" + safeLocation.getY() + " Z:" + safeLocation.getZ());

        Location finalSafeLocation = safeLocation;
        BukkitRunnable teleport = new BukkitRunnable() {
            public void run() {
                boolean teleported = true;

                plugin.logDebug("Teleporting...");

                // Load the teleportation location and tameable entity location
                Chunk teleportChunk = finalSafeLocation.getChunk();
                if (!teleportChunk.isLoaded()) {
                    if (teleportChunk.load(false)) {
                        plugin.logDebug("Loaded the teleportation chunk sucessfully, no generate!");
                    } else if (teleportChunk.load(true)) {
                        plugin.logDebug("Loaded the teleportation chunk sucessfully, generated!");
                    }
                }
                Chunk entityChunk = tameableEntity.getLocation().getChunk();
                if (!entityChunk.isLoaded()) {
                    if (!entityChunks.contains(entityChunk)) {
                        if (entityChunk.load(false)) {
                            plugin.logDebug("Loaded the entity chunk sucessfully, no generate!");
                            entityChunks.add(entityChunk);
                        } else if (entityChunk.load(true)) {
                            plugin.logDebug("Loaded the entity chunk sucessfully, generated!");
                            entityChunks.add(entityChunk);
                        }
                    }
                }

                if (!tameableEntity.teleport(finalSafeLocation)) {
                    teleported = false;
                }

                // If the entity got teleported
                if (teleported) {
                    if (sittingEntity.isSitting()) {
                        sittingEntity.setSitting(false);
                    }
                }
            }
        };

        plugin.logDebug("Adding teleport runnable to the bukkitrunnable!");
        teleport.runTask(this.plugin);

        // Return the found location
        teleportResult.put(true, safeLocation);
        return teleportResult;
    }

    // NON-EXPERIMENTAL FUNCTIONS
    public void doTeleportEntities(List<Entity> entities, Location location) {
        Location safeLocation = null;

        for (Entity e : entities) {
            // All tameables
            if (e instanceof Sittable && e instanceof Tameable && !((Sittable) e).isSitting()) {
                HashMap<Boolean, Location> teleportResult = teleportTameable(e, safeLocation, location);

                // If the first entity didn't find a safe location
                Boolean triedTeleporting = (Boolean) teleportResult.keySet().toArray()[0];
                safeLocation = (Location) teleportResult.values().toArray()[0];

                if (triedTeleporting && safeLocation == null) {
                    break;
                }
            }
        }
    }

    private HashMap<Boolean, Location> teleportTameable(Entity e, Location safeLocation, Location searchLocation) {
        // We use this map to store whether it even tried searching for a location, and then to return with a safe location, so we don't have to search again
        HashMap<Boolean, Location> teleportResult = new HashMap<>();

        Tameable tameableEntity = (Tameable) e;

        if (tameableEntity == null || tameableEntity.isDead() || !(tameableEntity.getOwner() instanceof Player)) {
            teleportResult.put(false, safeLocation);
            return teleportResult;
        }

        Sittable sittingEntity = (Sittable) e;
        Player player = (Player) tameableEntity.getOwner();

        if (player == null || !player.isOnline() || (!player.isOp() && !MyCat.getPermissionsManager().hasPermission(player, "mycat.teleport"))) {
            teleportResult.put(false, safeLocation);
            return teleportResult;
        }

        // If it's a cat, or if the config allows all tameables to teleport
        boolean isCat = (e.getType().equals(EntityType.CAT) && MyCat.getCatManager().isCat(tameableEntity.getUniqueId()));
        if (!isCat && !plugin.teleportAllTameables) {
            teleportResult.put(false, safeLocation);
            return teleportResult;
        }

        // If the tameable is sitting, or is in another world (without teleport on world change on)
        if (sittingEntity.isSitting() || (!tameableEntity.getWorld().equals(player.getWorld()) && !plugin.teleportOnWorldChange)) {
            teleportResult.put(false, safeLocation);
            return teleportResult;
        }

        // If cat, save location of the cat
        if (isCat) {
            MyCat.getCatManager().getCat(tameableEntity.getUniqueId()).saveCatLocation();
        }

        // Begin teleport procedure!!
        if (safeLocation == null) {
            // If a search location is provided, like a player that is starting to teleport
            if (searchLocation == null) {
                searchLocation = player.getLocation();
            }
            if (!isSafeLocation(searchLocation)) {
                plugin.logDebug("Whoops, seems like our player isn't at a safe location, let's find a good spot for the tameable...");
                searchLocation = searchSafeLocation(searchLocation);
                if (searchLocation == null) {
                    plugin.logDebug("Did not find a safe place to teleport the tameable! Keeping tameable at unloaded chunks!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.cannotTeleportTameableString.replace("{chatPrefix}", plugin.getChatPrefix())));
                    teleportResult.put(true, null);
                    return teleportResult;
                }
            }
            safeLocation = searchLocation;
        }

        plugin.logDebug("It's a safe location, teleporting!");

        // Load the teleportation location and tameable entity location
        Chunk teleportChunk = safeLocation.getChunk();
        if (!teleportChunk.isLoaded()) {
            if (teleportChunk.load(false)) {
                plugin.logDebug("Loaded the teleportation chunk sucessfully, no generate!");
            } else if (teleportChunk.load(true)) {
                plugin.logDebug("Loaded the teleportation chunk sucessfully, generated!");
            }
        }
        Chunk entityChunk = tameableEntity.getLocation().getChunk();
        if (!entityChunk.isLoaded()) {
            if (!entityChunks.contains(entityChunk)) {
                if (entityChunk.load(false)) {
                    plugin.logDebug("Loaded the entity chunk sucessfully, no generate!");
                    entityChunks.add(entityChunk);
                } else if (entityChunk.load(true)) {
                    plugin.logDebug("Loaded the entity chunk sucessfully, generated!");
                    entityChunks.add(entityChunk);
                }
            }
        }

        tameableEntity.teleport(safeLocation);

        myCat cat = MyCat.getCatManager().getCat(tameableEntity.getUniqueId());
        if (cat != null) {
            cat.updateCat();
        }

        if (sittingEntity.isSitting()) {
            sittingEntity.setSitting(false);
        }

        // Return the found location
        teleportResult.put(true, safeLocation);
        return teleportResult;
    }

    private Location searchSafeLocation(Location loc) {
        if (plugin.expandedSearch) {
            double y;
            double x;
            double z;
            plugin.logDebug("Starting safe location search!");
            for (z = 0; z <= 2; z++) {
                loc.setZ(loc.getZ() + z);
                //plugin.logDebug("Setting 1 Current location = X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
                for (x = 0; x <= 2; x++) {
                    loc.setX(loc.getX() + x);
                    //plugin.logDebug("Setting 2 Current location = X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
                    for (y = 255; y > 1; y--) {
                        loc.setY(y);
                        //plugin.logDebug("Setting 3 Current location = X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
                        if (isSafeLocation(loc)) {
                            //plugin.logDebug("is safe");
                            return loc;
                        }
                        //plugin.logDebug("not safe");
                    }
                }
            }
        } else {
            double y;
            for (y = 255; y > 1; y--) {
                loc.setY(y);
                //plugin.logDebug("Current location = X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
                if (isSafeLocation(loc)) {
                    //plugin.logDebug("is safe");
                    return loc;
                }
                //plugin.logDebug("not safe");
            }
        }

        return null;
    }

    private boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        Block ground = feet.getRelative(BlockFace.DOWN);
        //plugin.logDebug("Feet: " + feet.getType().toString());
        //plugin.logDebug("Ground: " + ground.getType().toString());

        return (isTransparent(feet.getType()) && (ground.getType().isSolid() || ground.getType() == Material.WATER));
    }

    private boolean isTransparent(Material materialType) {
        switch (materialType) {
            case AIR:
            case GRASS:
            case OAK_SAPLING:
            case SPRUCE_SAPLING:
            case JUNGLE_SAPLING:
            case BIRCH_SAPLING:
            case ACACIA_SAPLING:
            case DARK_OAK_SAPLING:
            case DEAD_BUSH:
            case VINE:
            case LILY_PAD:
            case LILAC:
            case ROSE_BUSH:
            case TALL_GRASS:
            case PEONY:
            case OAK_SIGN:
            case SPRUCE_SIGN:
            case BIRCH_SIGN:
            case JUNGLE_SIGN:
            case ACACIA_SIGN:
            case DARK_OAK_SIGN:
            case SUNFLOWER:
            case WHITE_CARPET:
            case ORANGE_CARPET:
            case MAGENTA_CARPET:
            case LIGHT_BLUE_CARPET:
            case YELLOW_CARPET:
            case LIME_CARPET:
            case PINK_CARPET:
            case GRAY_CARPET:
            case LIGHT_GRAY_CARPET:
            case CYAN_CARPET:
            case PURPLE_CARPET:
            case BLUE_CARPET:
            case BROWN_CARPET:
            case GREEN_CARPET:
            case RED_CARPET:
            case BLACK_CARPET:
            case DANDELION:
            case POPPY:
            case BLUE_ORCHID:
            case ALLIUM:
            case AZURE_BLUET:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case CORNFLOWER:
            case LILY_OF_THE_VALLEY:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case TORCH:
            case REDSTONE_TORCH:
            case SNOW:
            case LARGE_FERN:
            case FERN:
            case BAMBOO:
            case SUGAR_CANE:
            case WHEAT:
            case TRIPWIRE:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case NETHER_WART:
            case BEETROOTS:
                return true;
            default:
                return false;
        }
    }
}
