package dk.zolson427.mycat.managers;

import java.io.File;
import java.util.*;

import dk.zolson427.mycat.MyCat;
import dk.zolson427.mycat.objects.myCat;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;

import org.bukkit.scheduler.BukkitRunnable;

public class CatManager {
    private final MyCat plugin;
    private FileConfiguration catsConfig = null;
    private File catsConfigFile = null;
    private final Random random = new Random();
    private long lastSaveTime = 0L;

    private final HashMap<UUID, HashMap<String, Object>> catTrades = new HashMap<>();

    public CatManager(MyCat plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (this.catsConfigFile == null) {
            this.catsConfigFile = new File(this.plugin.getDataFolder(), "cats.yml");
        }
        this.catsConfig = YamlConfiguration.loadConfiguration(this.catsConfigFile);
        this.plugin.log("Loaded " + this.catsConfig.getKeys(false).size() + " cats.");
    }

    public void save() {
        this.lastSaveTime = System.currentTimeMillis();
        if ((this.catsConfig == null) || (this.catsConfigFile == null)) {
            return;
        }
        try {
            this.catsConfig.save(this.catsConfigFile);
        } catch (Exception ex) {
            this.plugin.log("Could not save config to " + this.catsConfigFile + ": " + ex.getMessage());
        }
    }

    public void saveTimed() {
        if (plugin.instantSave) {
            save();
            return;
        }

        if (System.currentTimeMillis() - this.lastSaveTime < 180000L) {
            return;
        }

        save();
    }

    public FileConfiguration getCatsConfig() {
        return catsConfig;
    }

    public boolean isCat(UUID catId) {
        if (!(plugin.getServer().getEntity(catId) instanceof org.bukkit.entity.Cat)) {
            return false;
        }
        return catsConfig.contains(catId.toString());
    }

    public void removeCat(UUID catId) {
        if (catsConfig.contains(catId.toString())) {
            catsConfig.set(catId.toString(), null);
            saveTimed();
        }
    }

    public boolean isUUIDDeadCat(UUID uuid) {
        return catsConfig.contains(uuid.toString()) && catsConfig.getBoolean(uuid + ".isDead");
    }

    public void catDied(UUID catId) {
        if (plugin.allowRevival) {
            myCat myCat = getCat(catId);
            if (myCat != null) {
                myCat.setDead(true);
            }
        } else {
            removeCat(catId);
        }
    }

    public int catsOwned(Player player) {
        return catsOwned(player.getUniqueId());
    }

    public int catsOwned(UUID playerId) {
        int cats = 0;
        for (String catUUID : catsConfig.getKeys(false)) {
            plugin.logDebug(catUUID);
            String ownerIdString = catsConfig.getString(catUUID + ".Owner");
            if (ownerIdString != null) {
                UUID ownerId = UUID.fromString(ownerIdString);
                if (ownerId.equals(playerId)) {
                    cats++;
                }
            }
        }
        return cats;
    }

    public boolean canTameMoreCats(Player player) {
        if (player.isOp()) {
            return true;
        }

        int currentlyOwned = getCats(player.getUniqueId()).size();
        for (int i = 0; i <= currentlyOwned; i++) {
            if (MyCat.getPermissionsManager().hasPermission(player, "mycat.limit." + i)) {
                return false;
            }
        }
        return true;
    }

    public myCat newCat(Cat cat, Player catOwner) {
        int catID = generateNewId(catOwner.getUniqueId());
        return new myCat(cat, catOwner, catID, 1);
    }

    public myCat newCat(Cat cat, Player catOwner, String customName, DyeColor collarColor) {
        int catID = generateNewId(catOwner.getUniqueId());
        return new myCat(cat, catOwner, customName, collarColor, catID, null);
    }

    public UUID getCatOwnerId(UUID catId) {
        if (!catsConfig.contains(catId.toString())) {
            return null;
        }
        String ownerIdString = catsConfig.getString(catId + ".Owner");
        if (ownerIdString == null) {
            return null;
        }
        return UUID.fromString(ownerIdString);
    }

    public myCat getCat(UUID catId) {
        if (isCat(catId)) {
            return new myCat(catId, getCatOwnerId(catId));
        }
        return null;
    }

    public myCat getCat(int catIdentifier, UUID ownerId) {
        for (String catIdString : catsConfig.getKeys(false)) {
            if (Objects.equals(catsConfig.getString(catIdString + ".ID"), Integer.toString(catIdentifier)) && ownerId.equals(getCatOwnerId(UUID.fromString(catIdString)))) {
                UUID catId = UUID.fromString(catIdString);
                return new myCat(catId, ownerId);
            }
        }

        return null;
    }

    public List<myCat> getCats() {
        List<myCat> myCats = new ArrayList<>();

        for (String catIdString : catsConfig.getKeys(false)) {
            UUID catId = UUID.fromString(catIdString);
            myCats.add(new myCat((Cat) Objects.requireNonNull(plugin.getServer().getEntity(catId))));
        }

        return myCats;
    }

    public List<myCat> getCats(UUID ownerId) {
        List<myCat> myCats = new ArrayList<>();

        for (String catIdString : catsConfig.getKeys(false)) {
            if (ownerId.equals(getCatOwnerId(UUID.fromString(catIdString)))) {
                UUID catId = UUID.fromString(catIdString);
                myCats.add(new myCat(catId, ownerId));
            }
        }

        return myCats;
    }

    public List<myCat> getAliveCats(UUID ownerId) {
        List<myCat> myCats = new ArrayList<>();
        for (myCat myCat : getCats(ownerId)) {
            if (!myCat.isDead()) {
                myCats.add(myCat);
            }
        }
        return myCats;
    }

    public List<myCat> getDeadCats(UUID ownerId) {
        List<myCat> myCats = new ArrayList<>();
        for (myCat myCat : getCats(ownerId)) {
            if (myCat.isDead()) {
                myCats.add(myCat);
            }
        }
        return myCats;
    }

    public boolean handleNewTrade(myCat myCat, Player receiver, double price) {
        if (catTrades.containsKey(receiver.getUniqueId())) {
            return false;
        }

        if (!canTameMoreCats(receiver)) {
            return false;
        }

        HashMap<String, Object> catTrade = new HashMap<>();
        catTrade.put("time", System.currentTimeMillis());
        catTrade.put("cat", myCat.getCatId());
        catTrade.put("price", price);

        catTrades.put(receiver.getUniqueId(), catTrade);

        // Delete the trade after 30 seconds if it still exists
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasTrade(receiver.getUniqueId())) {
                    catTrades.remove(receiver.getUniqueId());
                    receiver.sendMessage(ChatColor.GOLD + "Cat trade expired!");

                    Player sender = plugin.getServer().getPlayer(myCat.getOwnerId());
                    if (sender != null) {
                        sender.sendMessage(ChatColor.GOLD + "Recipient didn't answer to cat trade!");
                    }
                }
            }
        }.runTaskLaterAsynchronously(plugin, 20L * 30L);

        return true;
    }

    public boolean hasTrade(UUID recipient) {
        return catTrades.containsKey(recipient);
    }

    public HashMap<String, Object> getTrade(UUID recipient) {
        if (!hasTrade(recipient)) {
            return new HashMap<>();
        }

        return catTrades.get(recipient);
    }

    public myCat getTradeCat(UUID recipient) {
        HashMap<String, Object> trade = getTrade(recipient);
        if (trade == null || trade.isEmpty()) {
            return null;
        }

        return getCat((UUID) trade.get("cat"));
    }

    public Double getTradePrice(UUID recipient) {
        if (!hasTrade(recipient)) {
            return null;
        }

        return (double) catTrades.get(recipient).get("price");
    }

    public boolean acceptTrade(Player accepter) {
        HashMap<String, Object> catTrade = getTrade(accepter.getUniqueId());

        if (catTrade == null || catTrade.isEmpty()) {
            return false;
        }

        myCat myCat = getCat((UUID) catTrade.get("cat"));

        if (myCat == null) {
            return false;
        }

        double price = (double) catTrade.get("price");
        if (price > 0.0D) {
            if (MyCat.getEconomy() == null) {
                plugin.log("No economy provider, failed to trade cats!");
                return false;
            }

            if (!MyCat.getEconomy().has(accepter, price)) {
                return false;
            }
            MyCat.getEconomy().withdrawPlayer(accepter, price);
        }

        myCat.setOwner(accepter);

        catTrades.remove(accepter.getUniqueId());
        return true;
    }

    public boolean denyTrade(Player denier) {
        if (!hasTrade(denier.getUniqueId())) {
            return false;
        }

        catTrades.remove(denier.getUniqueId());
        return true;
    }

    public String newCatName() {
        int catNameNumber = random.nextInt(plugin.catNames.size());
        return plugin.catNames.get(catNameNumber);
    }

    public boolean setNewId(myCat myCat, int id) {
        // If another cat is already using the ID
        if (!myCat.setIdentifier(id)) {
            myCat myCat2 = getCat(id, myCat.getOwnerId());
            if (myCat2.setIdentifier(generateNewId(myCat.getOwnerId()))) {
                return myCat.setIdentifier(id);
            }
        } else {
            return true;
        }
        return false;
    }

    public int generateNewId(UUID catOwnerId) {
        int id = 1;
        List<myCat> myCats = MyCat.getCatManager().getCats(catOwnerId);

        if (!myCats.isEmpty()) {
            plugin.logDebug("Running new generator for ID");

            while (true) {
                plugin.logDebug("Running loop - Cats size: " + myCats.size());
                boolean isUsed = false;
                for (myCat myCat : myCats) {
                    plugin.logDebug("Current cat: " + myCat.getCatName() + " - " + myCat.getIdentifier() + " ID to search: " + id);
                    if (myCat.getIdentifier() == id) {
                        plugin.logDebug("ID already used - ID: " + id);
                        isUsed = true;
                        break;
                    }
                }
                if (!isUsed) {
                    plugin.logDebug("Found a free ID: " + id);
                    break;
                }
                id++;
            }
            plugin.logDebug("ok");
        } else {
            plugin.logDebug("Cats list is empty!");
        }

        plugin.logDebug("Returning ID: " + id);
        return id;
    }

    public void handleNewLevel(myCat myCat) {
        plugin.logDebug("Cat levelup! Level before: " + (myCat.getLevel() - 1) + " - Level now: " + myCat.getLevel());
        UUID ownerId = myCat.getOwnerId();
        Player owner = plugin.getServer().getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            String levelupString = plugin.levelUpString.replace("{chatPrefix}", plugin.getChatPrefix()).replace("{catNameColor}", "&" + myCat.getCatColor().getChar()).replace("{catName}", myCat.getCatName()).replace("{level}", Integer.toString(myCat.getLevel()));
            owner.sendMessage(ChatColor.translateAlternateColorCodes('&', levelupString));

            MyCat.getParticleUtils().newLevelUpParticle(plugin.getServer().getEntity(myCat.getCatId()));
            Sound sound;
            if (plugin.levelUpSound == null) {
                plugin.logDebug("Couldn't load the levelup sound, took Purr!");
                sound = Sound.ENTITY_CAT_PURR;
            } else {
                sound = Sound.valueOf(plugin.levelUpSound);
            }

            owner.playSound(owner.getLocation(), sound, 3.0F, 1.0F);
        }

        myCat.updateCat();
    }
}