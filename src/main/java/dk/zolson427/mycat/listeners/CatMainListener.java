package dk.zolson427.mycat.listeners;

import dk.zolson427.mycat.objects.myCat;
import dk.zolson427.mycat.objects.LevelFactory.Level;
import dk.zolson427.mycat.MyCat;
import net.md_5.bungee.api.ChatColor;
import dk.zolson427.mycat.utils.TimeUtils;

import java.util.*;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CatMainListener implements Listener {
    private final MyCat plugin;

    public CatMainListener(MyCat p) {
        this.plugin = p;
    }

    @EventHandler
    public void onEntityTameEvent(EntityTameEvent event) {
        if (event.getEntity().getType() != EntityType.CAT || !(event.getOwner() instanceof Player)) {
            return;
        }

        Player owner = (Player) event.getOwner();

        if (!MyCat.getCatManager().canTameMoreCats(owner)) {
            owner.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.tameLimitString.replace("{chatPrefix}", plugin.getChatPrefix())));
            event.setCancelled(true);
            return;
        }

        Cat cat = (Cat) event.getEntity();

        // Make the task for getting the cat, we want it to load in first...
        BukkitRunnable newTamedCat = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.logDebug("Running newTamedCat BukkitRunnable...");
                myCat myCat = MyCat.getCatManager().newCat(cat, owner);
                plugin.logDebug("New cat! Name: " + myCat.getCatName() + " - CatId: " + myCat.getCatId() + " - Owner: " + Objects.requireNonNull(plugin.getServer().getPlayer(myCat.getOwnerId())).getName() + " - OwnerId: " + myCat.getOwnerId());
                Location catLocation = myCat.getCatLocation();
                plugin.logDebug("Cat Location = X: " + catLocation.getX() + " Y: " + catLocation.getY() + " Z: " + catLocation.getZ());

                if (!myCat.updateCat()) {
                    plugin.logDebug("Could not set custom cat name, health and attack, cancelling event!");
                    event.setCancelled(true);
                    return;
                }
                plugin.logDebug("Finished setting custom cat name! Tame successful!");

				/*owner.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.GOLD + "Congratulations with your new cat, "
		+ cat.getCatColor() + cat.getCatName() + ChatColor.GOLD + "!");*/
                String newCatString = plugin.newCatString.replace("{chatPrefix}", plugin.getChatPrefix()).replace("{catNameColor}", "&" + myCat.getCatColor().getChar()).replace("{catName}", myCat.getCatName());
                owner.sendMessage(ChatColor.translateAlternateColorCodes('&', newCatString));
            }
        };

        // Run the CatgoMaker task
        newTamedCat.runTaskLater(plugin, 2);
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.CAT || !MyCat.getCatManager().isCat(event.getEntity().getUniqueId())) {
            return;
        }

        myCat myCat = MyCat.getCatManager().getCat(event.getEntity().getUniqueId());
        Player owner = plugin.getServer().getPlayer(myCat.getOwnerId());

        if (owner != null && owner.isOnline()) {
            Date catBirthday = myCat.getBirthday();
            Date today = new Date();
            long diff = Math.abs(today.getTime() - catBirthday.getTime());
            String time = TimeUtils.parseMillisToUFString(diff);

            String levelText = "";
            if (plugin.useLevels) {
                //levelText = ", and got to " + ChatColor.DARK_RED + "Level " + cat.getLevel() + ChatColor.RED + ".";
                levelText = plugin.deadCatLevelString.replace("{level}", Integer.toString(myCat.getLevel()));
            }

			/*owner.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Your cat, " + cat.getCatColor() + cat.getCatName() + ChatColor.RED + 
					", just passed away... " + cat.getCatColor() + cat.getCatName() + ChatColor.RED + " lived for " + time + levelText);*/
            String deadCatString = plugin.deadCatString.replace("{chatPrefix}", plugin.getChatPrefix()).replace("{catNameColor}", "&" + myCat.getCatColor().getChar()).replace("{catName}", myCat.getCatName()).replace("{time}", time).replace("{deadCatLevelString}", levelText);
            owner.sendMessage(ChatColor.translateAlternateColorCodes('&', deadCatString));
        }

        MyCat.getCatManager().catDied(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onCatPlayerInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Cat)) {
            return;
        }

        myCat myCat = MyCat.getCatManager().getCat(entity.getUniqueId());

        if (myCat == null) {
            plugin.logDebug("Cat is null!");
        }

        EquipmentSlot hand = event.getHand();
        Player player = event.getPlayer();
        Cat cat = (Cat) entity;
        ItemStack item = null;

        if (hand.equals(EquipmentSlot.HAND) && player.getEquipment() != null) {
            item = player.getEquipment().getItemInMainHand();
        } else if (hand.equals(EquipmentSlot.OFF_HAND) && player.getEquipment() != null) {
            item = player.getEquipment().getItemInOffHand();
        } else {
            plugin.logDebug("No item in hand.");
        }

        saveCatLocation(myCat, cat);

        // 0 = OK
        // 1 = Failed
        // 2 = Cancel event
        if (onCatPlayerInteractAlreadyTamed(myCat, cat, player) == 2) {
            plugin.logDebug("AlreadyTamed event: Cancelled");
            event.setCancelled(true);
            return;
        }

        if (onCatPlayerInteractPet(myCat, item, player) == 2) {
            plugin.logDebug("Petting event: Cancelled");
            event.setCancelled(true);
            return;
        }

        if (onCatPlayerInteractChangeColor(myCat, cat, item, player) == 2) {
            plugin.logDebug("ChangeColor event: Cancelled");
            event.setCancelled(true);
            return;
        }

        if (onCatPlayerInteractFeed(myCat, cat, item, player) == 2) {
            plugin.logDebug("Feed event: Cancelled");
            event.setCancelled(true);
            return;
        }

        if (onCatPlayerInteractRename(myCat, item, player) == 2) {
            plugin.logDebug("Rename event: Cancelled");
            event.setCancelled(true);
        }
    }

    public void saveCatLocation(myCat myCat, Cat cat) {
        if (myCat != null && cat != null) {
            plugin.logDebug("Saved cat location!");
            myCat.getCatLocation();
        }
    }

    public int onCatPlayerInteractAlreadyTamed(myCat myCat, Cat cat, Player player) {
        if (myCat != null || player == null) {
            return 0; // OK
        }

        // Make the cat into a cat, if it's tamed
        if (cat.isValid() && cat.isTamed() && cat.getOwner() != null && cat.getOwner() instanceof Player) {
            Player owner = (Player) cat.getOwner();

            if (!owner.getUniqueId().equals(player.getUniqueId())) {
                return 1; // Error
            }

            if (!MyCat.getCatManager().canTameMoreCats(owner)) {
                owner.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.tameLimitString.replace("{chatPrefix}", plugin.getChatPrefix())));
                return 1; // Error
            }

            String customName = cat.getCustomName();
            DyeColor collarColor = cat.getCollarColor();

            if ((customName == null || customName.isEmpty()) && collarColor == DyeColor.RED) {
                // No custom name or collar color
                myCat = MyCat.getCatManager().newCat(cat, owner, null, null);
            } else if (customName == null || customName.isEmpty()) {
                // No custom name
                myCat = MyCat.getCatManager().newCat(cat, owner, null, collarColor);
            } else if (collarColor == DyeColor.RED) {
                // No collar color
                myCat = MyCat.getCatManager().newCat(cat, owner, customName, null);
            } else {
                // Custom name and collar color
                myCat = MyCat.getCatManager().newCat(cat, owner, customName, collarColor);
            }
            plugin.logDebug("New already-tamed cat! Name: " + myCat.getCatName() + " - CatId: " + myCat.getCatId() + " - Owner: " + Objects.requireNonNull(plugin.getServer().getPlayer(myCat.getOwnerId())).getName() + " - OwnerId: " + myCat.getOwnerId());
            Location catLocation = myCat.getCatLocation();
            plugin.logDebug("Cat Location = X: " + catLocation.getX() + " Y: " + catLocation.getY() + " Z: " + catLocation.getZ());

            if (!myCat.updateCat()) {
                return 1; // Error
            }

            String newCatString = plugin.newCatString.replace("{chatPrefix}", plugin.getChatPrefix()).replace("{catNameColor}", "&" + myCat.getCatColor().getChar()).replace("{catName}", myCat.getCatName());
            owner.sendMessage(ChatColor.translateAlternateColorCodes('&', newCatString));
        }
        return 0; // OK
    }

    public int onCatPlayerInteractFeed(myCat myCat, Cat cat, ItemStack item, Player player) {
        if (myCat == null || item == null) {
            return 0; // OK
        }

        // Check for food
        double healthPoints = 0.0;
        switch (item.getType()) {
            case CHICKEN:
            case COOKED_CHICKEN:
                healthPoints = 1.0;
                break;
            case PORKCHOP:
            case COOKED_PORKCHOP:
            case BEEF:
            case COOKED_BEEF:
            case MUTTON:
            case COOKED_MUTTON:
            case RABBIT:
            case COOKED_RABBIT:
            case ROTTEN_FLESH:
                healthPoints = 2.0;
                break;
            default:
                if (player.isSneaking()) {
                    myCat.pet(player);
                    return 2; // Cancel event
                }
                break;
        }

        if (healthPoints != 0.0) {
            plugin.logDebug("Item is food!");
            int catsLevel = myCat.getLevel();
            if (catsLevel < 1) {
                plugin.logDebug("Level was under 1, setting level to 1");
                catsLevel = 1;
            }

            Level level = plugin.catLevels.get(catsLevel);
            if (level == null) {
                plugin.logDebug("Level object is null, returning!");
                return 1; // Error
            }

            double health = level.health;
            if (health < 10.0) {
                health = 10.0;
            }

            AttributeInstance catMaxHealth = cat.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (catMaxHealth != null && catMaxHealth.getValue() != health) {
                catMaxHealth.setBaseValue(health);
            }

            if (cat.getHealth() >= 20.0 && cat.getHealth() < health) {
                // Avoid health overflow, so get min value
                cat.setHealth(Math.min(cat.getHealth() + healthPoints, health));
                plugin.logDebug("Gave the cat, " + myCat.getCatName() + ", " + healthPoints + " in health.");
                if (player.getGameMode() != GameMode.CREATIVE) {
                    item.setAmount(item.getAmount() - 1);
                }
                return 2; // Cancel event
            }
        }
        return 0; // OK
    }

    public int onCatPlayerInteractChangeColor(myCat myCat, Cat cat, ItemStack item, Player player) {
        if (myCat == null || cat == null || item == null || player == null) {
            return 0; // OK
        }

        if (!myCat.getOwnerId().equals(player.getUniqueId())) {
            return 1; // Error
        }

        DyeColor dc = null;
        switch (item.getType()) {
            case BLACK_DYE:
                dc = DyeColor.BLACK;
                break;
            case BLUE_DYE:
                dc = DyeColor.BLUE;
                break;
            case BROWN_DYE:
                dc = DyeColor.BROWN;
                break;
            case CYAN_DYE:
                dc = DyeColor.CYAN;
                break;
            case GRAY_DYE:
                dc = DyeColor.GRAY;
                break;
            case GREEN_DYE:
                dc = DyeColor.GREEN;
                break;
            case LIGHT_BLUE_DYE:
                dc = DyeColor.LIGHT_BLUE;
                break;
            case LIGHT_GRAY_DYE:
                dc = DyeColor.LIGHT_GRAY;
                break;
            case LIME_DYE:
                dc = DyeColor.LIME;
                break;
            case MAGENTA_DYE:
                dc = DyeColor.MAGENTA;
                break;
            case ORANGE_DYE:
                dc = DyeColor.ORANGE;
                break;
            case PINK_DYE:
                dc = DyeColor.PINK;
                break;
            case PURPLE_DYE:
                dc = DyeColor.PURPLE;
                break;
            case RED_DYE:
                dc = DyeColor.RED;
                break;
            case YELLOW_DYE:
                dc = DyeColor.YELLOW;
                break;
            case WHITE_DYE:
                dc = DyeColor.WHITE;
                break;
            default:
                break;
        }

        if (dc != null) {
            // Set collar color

            if (cat.getCollarColor().equals(dc)) {
                plugin.logDebug("Collar color is the same as dye color, returning!");
                return 0; // OK
            }

            myCat.setCatColor(dc);
        }
        return 0; // OK
    }

    public int onCatPlayerInteractPet(myCat myCat, ItemStack item, Player player) {
        if (myCat == null || item == null || player == null) {
            return 0; // OK
        }

        if (item.getType().equals(Material.AIR)) {
            if (player.isSneaking()) {
                myCat.pet(player);
                return 2; // Cancel event
            }
        }
        return 0; // OK
    }

    public int onCatPlayerInteractRename(myCat myCat, ItemStack item, Player player) {
        if (myCat == null || item == null || player == null) {
            return 0; // OK
        }

        // Check if the player has a name_tag equipped
        if (item.getType().equals(Material.NAME_TAG) && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            if (!plugin.allowNametagRename || !myCat.getOwnerId().equals(player.getUniqueId())) {
                plugin.logDebug("NametagRename is disabled or not owner trying to rename cat!");
                return 2; // Cancel event
            }

            myCat.setCatName(item.getItemMeta().getDisplayName());
            plugin.logDebug("Set the Cat's name to: " + item.getItemMeta().getDisplayName());
            if (player.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }
            return 2; // Cancel event
        }
        return 0; // OK
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreedEvent(EntityBreedEvent event) {
        if (!(event.getEntity() instanceof Cat) || /*(!MyCat.getCatManager().isCat(event.getMother().getUniqueId())) || (!MyCat.getCatManager().isCat(event.getFather().getUniqueId())) ||*/ !(event.getBreeder() instanceof Player)) {
            plugin.logDebug("Entity breed return!");
            return;
        }

        Cat cat = (Cat) event.getEntity();
        Player ownerFind;

        if (cat.getOwner() != null) {
            ownerFind = (Player) cat.getOwner();
        } else {
            ownerFind = (Player) event.getBreeder();
        }

        if (ownerFind == null) {
            plugin.logDebug("Cat owner is null, returning!");
            return;
        }

        final Player owner = ownerFind;

        // Make the task for getting the catgo, we want it to load in first...
        BukkitRunnable newCatBreed = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.logDebug("Running newCatBreed BukkitRunnable...");
                myCat myCat = MyCat.getCatManager().newCat(cat, owner);
                plugin.logDebug("New cat! Name: " + myCat.getCatName() + " - CatId: " + myCat.getCatId() + " - Owner: " + Objects.requireNonNull(plugin.getServer().getPlayer(myCat.getOwnerId())).getName() + " - OwnerId: " + myCat.getOwnerId());
                Location catLocation = myCat.getCatLocation();
                plugin.logDebug("Cat Location = X: " + catLocation.getX() + " Y: " + catLocation.getY() + " Z: " + catLocation.getZ());

                if (!myCat.updateCat()) {
                    plugin.logDebug("Could not set custom cat name, health and attack, cancelling event!");
                    event.setCancelled(true);
                    return;
                }
                plugin.logDebug("Finished setting custom cat name! Breed sucessfull!");

				/*owner.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.GOLD + "Congratulations with your new cat, "
						+ cat.getCatColor() + cat.getCatName() + ChatColor.GOLD + "!");*/
                String newCatString = plugin.newCatString.replace("{chatPrefix}", plugin.getChatPrefix()).replace("{catNameColor}", "&" + myCat.getCatColor().getChar()).replace("{catName}", myCat.getCatName());
                owner.sendMessage(ChatColor.translateAlternateColorCodes('&', newCatString));
            }
        };

        // Run the CatgoMaker task
        newCatBreed.runTaskLater(plugin, 2);
    }

    // package-private
    static void checkForCats(Entity[] entities) {
        for (Entity e : entities) {
            if (e != null && e.getType().equals(EntityType.CAT)) {
                String customName = "UNKNOWN NAME";
                if (e.getCustomName() != null) {
                    customName = e.getCustomName();
                }
                MyCat.instance().logDebug("There is a cat in the loaded chunk! Name: " + customName);

                Cat cat = (Cat) e;
                if (MyCat.getCatManager().isCat(cat.getUniqueId())) {
                    MyCat.instance().logDebug("Updated loaded cat with health and damage!");
                    MyCat.getCatManager().getCat(cat.getUniqueId()).updateCat();
                }
            }
        }
    }


//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void onChunkPopulate(ChunkPopulateEvent event)
//	{
//		checkChunkCats(event.getChunk());
//	}

    // Load cats
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        Entity[] entities = event.getChunk().getEntities();
        if (entities.length != 0) {
            checkForCats(entities);
        }
    }

    // If the player is teleporting, this would be used in regions that might be loaded already by other players, or spawn regions
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!plugin.automaticTeleportation) {
            return;
        }

        Player player = event.getPlayer();

        List<Entity> entities = new ArrayList<>();

        // Check whether the player has any cats
        for (myCat myCat : MyCat.getCatManager().getAliveCats(player.getUniqueId())) {
            Cat cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());
            if (cat != null && !cat.isSilent()) {
                entities.add(cat);
            }
        }

        if (plugin.experimentalTeleport) {
            MyCat.getTeleportationManager().teleportEntities(entities, event.getTo(), "PlayerTeleport");
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    MyCat.getTeleportationManager().doTeleportEntities(entities, event.getTo());
                }
            }.runTaskLater(this.plugin, 3);
        }

    }

    // If a chunk is unloading, check if there are any tameables inside it
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!plugin.automaticTeleportation) {
            return;
        }

        List<Entity> entities = Arrays.asList(event.getChunk().getEntities());

        if (plugin.experimentalTeleport) {
            MyCat.getTeleportationManager().teleportEntities(entities, null, "ChunkUnload");
        } else {
            MyCat.getTeleportationManager().doTeleportEntities(entities, null);
        }
    }

    // When a cat enters a portal, deny the teleport, since it's annoying
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalTeleport(EntityPortalEvent event) {
        if (!plugin.automaticTeleportation) {
            return;
        }

        Entity entity = event.getEntity();

        if (MyCat.getCatManager().isCat(entity.getUniqueId())) {
            // Make sure that the portal isn't the end portal... pretty sure there's lava under that lol
            if (event.getTo() != null && event.getTo().getWorld() != null) {
                if (event.getTo().getWorld().getEnvironment() != World.Environment.THE_END) {
                    MyCat.instance().logDebug("Stopped a cat from teleporting through a portal.");
                    event.setCancelled(true);
                }
            }
        }
    }
}
