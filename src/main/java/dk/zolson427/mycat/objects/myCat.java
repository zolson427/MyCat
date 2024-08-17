package dk.zolson427.mycat.objects;

import dk.zolson427.mycat.MyCat;
import dk.zolson427.mycat.utils.ColorUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Cat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class myCat {
    private static final String ANGRY_MODE = ChatColor.GRAY + "[" + ChatColor.RED + "⚔" + ChatColor.GRAY + "]";
    private static final String DEFENCE_MODE = ChatColor.GRAY + "[" + ChatColor.GREEN + "⛨" + ChatColor.GRAY + "]";
    private static final List<Sound> PETTING_SOUNDS = Arrays.asList(Sound.ENTITY_CAT_PURREOW, Sound.ENTITY_CAT_AMBIENT);

    private UUID catId;
    private UUID catOwnerId;
    private int catIdentifier;
    private String catName;
    private int level;
    private int experience;
    private Date birthday;
    private Location location;
    private DyeColor collarColor;
    private ChatColor nameColor;
    private Boolean isDead;
    private Boolean isAngry;

    private final String pattern = "dd-MM-yyyy HH:mm";
    private final DateFormat formatter = new SimpleDateFormat(pattern);
    private final static Random random = new Random();

    // For new cats
    public myCat(Cat cat, Player catOwner, int catUID, int level) {
        this(cat, catOwner, null, null, catUID, level);
    }

    // For new, already-tamed cats with old data
    public myCat(Cat cat, Player catOwner, String customName, DyeColor collarColorImport, int catUID, Integer level) {
        // The UUID of the Cat
        this.catId = cat.getUniqueId();

        // The UUID of the Cat's owner (Player)
        this.catOwnerId = catOwner.getUniqueId();

        // Generate an ID for the Cat
        this.catIdentifier = catUID;

        // Generate a new name for the Cat
        if (customName == null || customName.isEmpty()) {
            this.catName = MyCat.getCatManager().newCatName();
        } else {
            // We set the cat name
            this.catName = customName;
            // We run the get cat name function to get the name without the color codes and levels, just in case
            this.catName = getCatName();
        }

        // Generate a random Collar Color and set the Cat's Color
        if (collarColorImport == null && MyCat.instance().randomCollarColor) {
            cat.setCollarColor(ColorUtils.randomDyeColor());
        }

        this.collarColor = cat.getCollarColor();
        this.nameColor = ColorUtils.getChatColorFromDyeColor(collarColor);

        // Save the Cat's last seen location
        this.location = cat.getLocation();

        // Give the Cat a level
        if (MyCat.instance().useLevels) {
            if (level == null || level == 0) {
                this.level = 1;
            } else {
                this.level = level;
            }
            this.experience = 0;
        }

        // Set the current time as the Cat's birthday
        this.birthday = new Date();

        // Sync the data to the database
        syncCat();
    }

    // For old, already created, cats
    public myCat(Cat cat) {
        this(cat.getUniqueId(), Objects.requireNonNull(cat.getOwner()).getUniqueId());
    }

    // For old, already created, cats
    public myCat(UUID catUUID, UUID playerUUID) {
        if (MyCat.getCatManager().getCatsConfig().contains(catUUID.toString())) {
            this.catId = catUUID;
            this.catOwnerId = playerUUID;
            this.catIdentifier = getIdentifier();
            this.birthday = getBirthday();
            this.level = getLevel();
            this.experience = getExperience();
            this.catName = getCatName();
            this.nameColor = getCatColor();
            this.isDead = isDead();
            this.isAngry = isAngry();
            this.location = getCatLocation();

            syncCat();
        }
    }

    public String getCatName() {
        String name = catName;
        if (catName == null || catName.isEmpty()) {
            name = MyCat.getCatManager().getCatsConfig().getString(catId.toString() + ".Name");

            if (name == null || name.isEmpty()) {
                Cat cat = getCat();
                if (cat != null) {
                    name = cat.getCustomName();
                }

                if (name == null || name.isEmpty()) {
                    name = MyCat.getCatManager().newCatName();
                }
            }
        }

        // Remove color codes from the name
        name = ChatColor.stripColor(name);

        // Remove the level
        name = name.replaceAll("\\[\\d+]", "");
        // Remove the mode
        name = name.replaceAll("\\[[⚔⛨]]", "");

        // And strip string
        name = name.trim();

        return name;
    }

    public Cat getCat() {
        Entity catEntity = MyCat.instance().getServer().getEntity(catId);
        if (catEntity == null || !catEntity.isValid() || !(catEntity instanceof Cat)) {
            return null;
        }

        return (Cat) catEntity;
    }

    /**
     * Reassigns the cat to another UUID.
     * NOTE: Deletes the old cat from the data!
     *
     * @param catId the new UUID
     */
    public void setUUID(UUID catId) {
        UUID oldID = this.catId;
        this.catId = catId;

        syncCat();

        MyCat.getCatManager().removeCat(oldID);
    }

    public void setIsAngry(boolean isAngry) {
        this.isAngry = isAngry;
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Angry", this.isAngry);

        MyCat.getCatManager().saveTimed();
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Birthday", formatter.format(this.birthday));

        MyCat.getCatManager().saveTimed();
    }

    public Date getBirthday() {
        if (birthday != null) {
            return birthday;
        }

        if (MyCat.getCatManager().getCatsConfig().getString(catId.toString() + ".Birthday") == null) {
            setBirthday(new Date());
        }

        try {
            return formatter.parse(MyCat.getCatManager().getCatsConfig().getString(catId.toString() + ".Birthday"));
        } catch (ParseException e) {
            MyCat.instance().log("Failed to parse cat birthday.");
            return null;
        }
    }

    public ChatColor getCatColor() {
        if (nameColor != null) {
            return nameColor;
        }

        Cat cat = getCat();
        if (cat == null) {
            if (MyCat.getCatManager().getCatsConfig().getString(catId.toString() + ".NameChatColor") != null) {
                return ChatColor.valueOf(MyCat.getCatManager().getCatsConfig().getString(catId.toString() + ".NameChatColor"));
            }
            return ChatColor.WHITE;
        }

        return ColorUtils.getChatColorFromDyeColor(cat.getCollarColor());
    }

    public boolean setCatColor(DyeColor color) {
        if (color == null) {
            return false;
        }
        if (MyCat.getCatManager().getCatsConfig().contains(catId.toString())) {
            this.nameColor = ColorUtils.getChatColorFromDyeColor(color);
            MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".NameChatColor", nameColor.name());

            if (catName == null) {
                this.catName = getCatName();
            }

            return setCatCustomName();
        }
        MyCat.getCatManager().saveTimed();
        return true;
    }

    public void pet(Player player) {
        if (player == null || (!player.isOp() && !MyCat.getPermissionsManager().hasPermission(player, "mycat.pet"))) {
            return;
        }

        if (!player.getUniqueId().equals(catOwnerId) && !MyCat.getPermissionsManager().hasPermission(player, "mycat.pet.others")) {
            return;
        }

        Cat cat = getCat();
        if (cat == null) {
            return;
        }

        MyCat.instance().logDebug("Petting cat.");
        String pettingString = MyCat.instance().pettingString.replace("{catNameColor}", "&" + getCatColor().getChar()).replace("{catName}", getCatName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', pettingString));

        if (random.nextInt(10) == 1) {
            cat.playEffect(EntityEffect.CAT_TAME_SUCCESS);
            pettingString = MyCat.instance().pettingSplashString.replace("{catNameColor}", "&" + getCatColor().getChar()).replace("{catName}", getCatName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', pettingString));
        }
        MyCat.getParticleUtils().newPettingParticle(cat);
        Sound sound = PETTING_SOUNDS.get(random.nextInt(PETTING_SOUNDS.size()));
        player.playSound(player.getLocation(), sound, 3.0F, 1.0F);
    }

    public void toggleMode() {
        setAngryMode(!isAngry());
    }

    public void setAngryMode(boolean angry) {
        MyCat.instance().logDebug("Toggling cat mode.");
        this.setIsAngry(angry);
        setCatCustomName();
        MyCat.getCatManager().saveTimed();
    }

    public void sit(boolean sit) {
        Cat cat = getCat();
        if (cat == null) {
            return;
        }

        cat.setSitting(sit);
        getCatLocation();
    }

    public boolean isDead() {
        if (isDead != null) {
            return isDead;
        }
        return MyCat.getCatManager().getCatsConfig().getBoolean(catId.toString() + ".Dead", false);
    }

    public void setDead(boolean dead) {
        isDead = dead;
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Dead", isDead);

        MyCat.getCatManager().saveTimed();
    }

    public boolean isAngry() {
        if (isAngry != null) {
            return isAngry;
        }

        return MyCat.getCatManager().getCatsConfig().getBoolean(catId.toString() + ".Angry", false);
    }

    public boolean setCatName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (MyCat.getCatManager().getCatsConfig().contains(catId.toString())) {
            this.catName = name;
            MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Name", catName);

            setCatCustomName();
        }
        MyCat.getCatManager().saveTimed();
        return true;
    }

    public Location getCatLocation() {
        Cat cat = getCat();
        if (cat == null) {
            if (MyCat.getCatManager().getCatsConfig().getString(catId.toString() + ".LastSeen.World") != null) {
                String lastSeenWorld = MyCat.getCatManager().getCatsConfig().getString(catId.toString() + ".LastSeen.World");
                if (lastSeenWorld == null) {
                    return null;
                }
                return new Location(MyCat.instance().getServer().getWorld(lastSeenWorld), MyCat.getCatManager().getCatsConfig().getInt(catId.toString() + ".LastSeen.X"), MyCat.getCatManager().getCatsConfig().getInt(catId.toString() + ".LastSeen.Y"), MyCat.getCatManager().getCatsConfig().getInt(catId.toString() + ".LastSeen.Z"));
            }
            return null;
        }

        saveCatLocation();
        return this.location;
    }

    public boolean saveCatLocation() {
        Cat cat = getCat();
        if (cat == null) {
            return false;
        }

        this.location = cat.getLocation();
        if (this.location.getWorld() != null) {
            MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".LastSeen.World", location.getWorld().getName());
        }
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".LastSeen.X", location.getX());
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".LastSeen.Y", location.getY());
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".LastSeen.Z", location.getZ());

        MyCat.getCatManager().saveTimed();
        return true;
    }

    public Location getLastCatLocation() {
        return location;
    }

    public UUID getCatId() {
        return catId;
    }

    public UUID getOwnerId() {
        return catOwnerId;
    }

    public boolean setOwner(Player player) {
        if (player == null) {
            return false;
        }

        Cat cat = getCat();

        if (cat == null) {
            return false;
        }

        if (catOwnerId.equals(player.getUniqueId())) {
            // Same owner
            return false;
        }

        if (MyCat.getCatManager().getCatsConfig().contains(catId.toString())) {
            this.catOwnerId = player.getUniqueId();
            this.catIdentifier = MyCat.getCatManager().generateNewId(player.getUniqueId());
            MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Owner", catOwnerId);
            MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".ID", catIdentifier);
            cat.setOwner(player);
        }

        MyCat.getCatManager().saveTimed();
        return true;
    }

    public int getRevivalPrice() {
        return level * MyCat.instance().revivalPrice;
    }

    public int getLevel() {
        if (level != 0) {
            return level;
        }
        return MyCat.getCatManager().getCatsConfig().getInt(catId.toString() + ".Level.Level", 1);
    }

    public void setLevel(int lvl) {
        if (level != lvl) {
            this.level = lvl;
        }

        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Level.Level", lvl);
    }

    public int getExperience() {
        if (experience != 0) {
            return experience;
        }
        return MyCat.getCatManager().getCatsConfig().getInt(catId.toString() + ".Level.Experience", 0);
    }

    public void giveExperience(int exp) {
        int currentExp = getExperience();
        setExperience(currentExp + exp);
    }

    public void setExperience(int exp) {
        this.experience = exp;
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Level.Experience", exp);

        int levelBefore = level;
        int newLevel = 1;
        Map<Integer, LevelFactory.Level> levels = MyCat.instance().catLevels;

        for (Map.Entry<Integer, LevelFactory.Level> levelSet : levels.entrySet()) {
            int levelInt = levelSet.getKey();
            int levelExp = levelSet.getValue().exp;
            // Amount of exp must be higher or equals to exp to level
            // The level must be higher than the level the Cat had before
            // The new level variable must be smaller than the level checking against
            if (exp >= levelExp && levelInt > levelBefore && newLevel < levelInt) {
                MyCat.instance().logDebug("Iterating through levels... Possible new level: " + levelInt);
                newLevel = levelInt;
            }
        }

        if (newLevel != 1) {
            MyCat.instance().logDebug("Setting new level to level: " + newLevel + "! Old level: " + this.level);
            this.level = newLevel;
        }

        if (levelBefore < level) {
            setLevel(level);
            MyCat.getCatManager().handleNewLevel(this);
        }

        MyCat.getCatManager().saveTimed();
    }

    public String getCatCustomName() {
        if (MyCat.getCatManager().getCatsConfig().contains(catId.toString())) {
            if (MyCat.instance().useLevels && MyCat.instance().showLevelsInNametag) {
                return (nameColor + catName + ChatColor.GRAY + " [" + ChatColor.GOLD + this.level + ChatColor.GRAY + "]" + (isAngry() ? ANGRY_MODE : DEFENCE_MODE));
            }
            else {
                return nameColor + catName;
            }
        }
        return null;
    }

    public boolean setCatCustomName() {
        MyCat.instance().logDebug("Setting custom name... catId: " + catId);
        Cat cat = getCat();
        if (cat == null) {
            MyCat.instance().logDebug("Retuning false!");
            return false;
        }

        if (MyCat.getCatManager().getCatsConfig().contains(catId.toString())) {
            String customName = getCatCustomName();
            MyCat.instance().logDebug("Setting customName to: " + customName);
            cat.setCustomName(customName);

            cat.setCustomNameVisible(!MyCat.instance().onlyShowNametagOnHover);
            MyCat.instance().logDebug("Returning true!");
            return true;
        }
        MyCat.instance().logDebug("Retuning false!");
        return false;
    }

    public int getIdentifier() {
        if (catIdentifier > 0) {
            return catIdentifier;
        }
        return MyCat.getCatManager().getCatsConfig().getInt(catId.toString() + ".ID", -1);
    }

    public boolean setIdentifier(int id) {
        // If the ID is already used, return false
        for (String catIdString : MyCat.getCatManager().getCatsConfig().getKeys(false)) {
            if (MyCat.getCatManager().getCatsConfig().getString(catIdString + ".ID") == null) continue;
            if (MyCat.getCatManager().getCatsConfig().getString(catIdString + ".Owner") == null) continue;

            if (Integer.toString(id).equals(MyCat.getCatManager().getCatsConfig().getString(catIdString + ".ID"))
                    && catOwnerId.toString().equals(MyCat.getCatManager().getCatsConfig().getString(catIdString + ".Owner"))) {
                return false;
            }
        }
        // Otherwise, apply the new ID
        this.catIdentifier = id;
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".ID", catIdentifier);
        return true;
    }

    public boolean setHealth() {
        Cat cat = getCat();

        if (cat == null) {
            MyCat.instance().logDebug("Failed to set Cat health, Cat entity is null!");
            return false;
        }

        int catsLevel = getLevel();
        if (catsLevel < 1) {
            MyCat.instance().logDebug("Level was under 1, setting level to 1");
            catsLevel = 1;
        }

        LevelFactory.Level level = MyCat.instance().catLevels.get(catsLevel);
        if (level == null) {
            MyCat.instance().logDebug("Level object is null, returning!");
            return false;
        }

        double health = level.health;
        if (health < 5D) {
            health = 5D;
        }

        AttributeInstance catMaxHealth = cat.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (catMaxHealth == null) {
            return false;
        }

        MyCat.instance().logDebug("Cat Maxhealth Before: " + catMaxHealth.getValue());
        catMaxHealth.setBaseValue(health);
        cat.setHealth(catMaxHealth.getValue());
        MyCat.instance().logDebug("Cat Maxhealth After: " + catMaxHealth.getValue());
        return true;
    }

    public boolean setDamage() {
        Cat cat = getCat();

        if (cat == null) {
            MyCat.instance().logDebug("Failed to set Cat damage, Cat entity is null or invalid!");
            return false;
        }

        int catsLevel = getLevel();
        if (catsLevel < 1) {
            MyCat.instance().logDebug("Level was under 1, setting level to 1");
            catsLevel = 1;
        }

        LevelFactory.Level level = MyCat.instance().catLevels.get(catsLevel);
        if (level == null) {
            MyCat.instance().logDebug("Level object is null, returning!");
            return false;
        }

        double damage = level.damage;
        if (damage < 1.0D) {
            damage = 1.0D;
        }

        AttributeInstance catDamage = cat.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (catDamage == null) {
            return false;
        }
        MyCat.instance().logDebug("Cat Damage Before: " + catDamage.getValue());
        catDamage.setBaseValue(damage);
        MyCat.instance().logDebug("Cat Damage After: " + catDamage.getValue());
        return true;
    }

    public void syncCat() {
        Cat cat = getCat();

        // The UUID of the Cat's owner (Player)
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Owner", catOwnerId.toString());
        // ID for the Cat
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".ID", catIdentifier);
        // Name for the Cat
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Name", catName);

        // Generate a random Collar Color and set the Cat's Color
        if (collarColor == null) {
            if (cat != null) {
                this.collarColor = cat.getCollarColor();
            } else {
                if (MyCat.instance().randomCollarColor) {
                    this.collarColor = ColorUtils.randomDyeColor();
                }
            }

            if (collarColor != null) {
                this.nameColor = ColorUtils.getChatColorFromDyeColor(collarColor);
                MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".NameChatColor", nameColor.name());
            }
        } else {
            if (nameColor == null) {
                this.nameColor = ColorUtils.getChatColorFromDyeColor(collarColor);
            }
            MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".NameChatColor", nameColor.name());
        }

        // Save the Cat's last seen location
        this.location = getCatLocation();

        // Give the Cat a level
        if (MyCat.instance().useLevels) {
            MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Level.Level", level);
            MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Level.Experience", experience);
        }

        // Set the Cat's birthday
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Birthday", formatter.format(birthday));

        // Set angry
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Angry", (isAngry != null) ? isAngry : false);
        // Set dead to false (I hope LOL)
        MyCat.getCatManager().getCatsConfig().set(catId.toString() + ".Dead", (isDead != null) ? isDead : false);

        MyCat.getCatManager().saveTimed();
    }

    public boolean updateCat() {
        boolean customNameSet = setCatCustomName();

        if (!MyCat.instance().useLevels) {
            MyCat.instance().logDebug("Not updating health and damage for cat, levels are disabled!");
            return customNameSet;
        }

        return customNameSet && (setHealth() && setDamage());
    }
}
