package dk.zolson427.mycat;

import dk.zolson427.mycat.listeners.CatMainListener_1_18;
import dk.zolson427.mycat.objects.LevelFactory;
import dk.zolson427.mycat.objects.LevelFactory.Level;
import dk.zolson427.mycat.listeners.DamageListener;
import dk.zolson427.mycat.listeners.CatMainListener;
import dk.zolson427.mycat.managers.CommandManager;
import dk.zolson427.mycat.managers.CatManager;
import dk.zolson427.mycat.managers.PermissionsManager;
import dk.zolson427.mycat.managers.TeleportationManager;
import dk.zolson427.mycat.tasks.AttackModeTask;
import dk.zolson427.mycat.tasks.DistanceTask;
import dk.zolson427.mycat.utils.ParticleUtils;
import dk.zolson427.mycat.utils.versioning.Version;
import dk.zolson427.mycat.utils.versioning.VersionFactory;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import java.util.*;

import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/*           THIS PLUGIN IS HEAVILY INSPIRED BY           */
/*                   DOGONFIRE'S MYHORSE                  */
/*														  */
/*          https://github.com/DogOnFire/MyHorse          */
public class MyCat extends JavaPlugin {
    private static MyCat instance;
    public static boolean pluginEnabled = false;
    private static final int pluginId = 18606;

    public boolean vaultEnabled = false;

    public static Server server = null;
    public boolean debug = false;
    public boolean instantSave = false;
    public boolean automaticTeleportation = true;
    public boolean teleportOnWorldChange = true;
    public boolean teleportAllTameables = false;
    public boolean experimentalTeleport = false;
    private boolean playerDistanceCheck = true;
    public boolean expandedSearch = false;
    public boolean onlyShowNametagOnHover = false;
    public boolean showLevelsInNametag = true;

    public boolean allowPlayerKillExp = true;
    public boolean allowNametagRename = true;
    public boolean allowRevival = true;
    public int revivalPrice = 200;
    public boolean revivalUsingPlayerExp = true;
    public boolean allowArrowDamage = false;
    public double lifesteal = 0.25D;

    public String levelUpSound = "ENTITY_CAT_PURR";
    public String levelUpString = "&5&l[{chatPrefix}] &r&5Your cat, {catNameColor}{catName}&5, just leveled up to &dLevel {level}&5!";
    public String cannotTeleportTameableString = "&c&l[{chatPrefix}] &r&cHello! Looks like you just teleported away from your Pet(s)! " +
            "They can sadly not find a safe place to stay, so they are staying behind for now :( They will be waiting for you where you left them...";
    public String newCatString = "&6&l[{chatPrefix}] &r&6Congratulations with your new cat, {catNameColor}{catName}&6!";
    public String deadCatString = "&c&l[{chatPrefix}] &r&cYour cat, {catNameColor}{catName}&c, just passed away... {catNameColor}{catName}&c lived for {time}{deadCatLevelString}.";
    public String deadCatLevelString = ", and got to &4Level {level}&c";
    public String commandComehereString = "&6&l[{chatPrefix}] &r&6Come here! Good catgo, {catNameColor}{catName}&6!";
    public String tameLimitString = "&c&l[{chatPrefix}] &r&cTaming failed! Looks like you have reached your limit of cats! You can maybe set some dead cats free, or revive some?";
    public String pettingString = "&6Who's a good cat?! {catNameColor}{catName}&6 is!";
    public String pettingSplashString = "{catNameColor}{catName}&6 splashes water all over you!";

    private static MyCat plugin;
    private static FileConfiguration config = null;
    private static PermissionsManager permissionsManager = null;
    private static CatManager catManager = null;
    private static TeleportationManager teleportationManager = null;
    private static LevelFactory levelFactory = null;
    private static ParticleUtils particleUtils = null;

    public boolean randomCollarColor = true;
    public boolean useLevels = true;
    public List<String> catNames = Arrays.asList(
            "Queen", "King", "Gato", "Charlie", "Max", "Milo", "Ollie", "Toby", "Teddy", "Molly", "Rosie", "Bella",
            "Abby", "Addie", "Alexis", "Alice", "Allie", "Alyssa", "Amber", "Angel", "Anna", "Annie", "Ariel", "Ashley",
            "Aspen", "Athena", "Autumn", "Ava", "Avery", "Baby", "Bailey", "Basil", "Bean", "Bella", "Belle", "Betsy",
            "Betty", "Bianca", "Birdie", "Biscuit", "Blondie", "Blossom", "Bonnie", "Brandy", "Brooklyn", "Brownie", "Buffy",
            "Callie", "Camilla", "Candy", "Carla", "Carly", "Carmela", "Casey", "Cassie", "Chance", "Chanel", "Chloe",
            "Cinnamon", "Cleo", "Coco", "Cookie", "Cricket", "Daisy", "Dakota", "Dana", "Daphne", "Darla", "Darlene",
            "Delia", "Delilah", "Diamond", "Diva", "Dixie", "Dolly", "Duchess", "Eden", "Eddie", "Ella", "Ellie",
            "Elsa", "Emma", "Emmy", "Eva", "Faith", "Fanny", "Fern", "Fiona", "Foxy", "Gabby", "Gemma", "Georgia", "Gia",
            "Gidget", "Gigi", "Ginger", "Goldie", "Grace", "Gracie", "Greta", "Gypsy", "Hailey", "Hannah", "Harley", "Harper",
            "Hazel", "Heidi", "Hershey", "Holly", "Honey", "Hope", "Ibby", "Inez", "Isabella", "Ivy", "Izzy", "Jackie", "Jada",
            "Jade", "Jasmine", "Jenna", "Jersey", "Jessie", "Jill", "Josie", "Julia", "Juliet", "Juno", "Kali", "Kallie",
            "Karma", "Kate", "Katie", "Kayla", "Kelsey", "Khloe", "Kiki", "Kira", "Koko", "Kona", "Lacy", "Lady", "Layla",
            "Leia", "Lena", "Lexi", "Libby", "Liberty", "Lily", "Lizzy", "Lola", "London", "Lucky", "Lulu", "Luna", "Mabel",
            "Mackenzie", "Macy", "Maddie", "Madison", "Maggie", "Maisy", "Mandy", "Marley", "Matilda", "Mattie", "Maya",
            "Mia", "Mika", "Mila", "Miley", "Millie", "Mimi", "Minnie", "Missy", "Misty", "Mitzi", "Mocha", "Molly", "Morgan",
            "Moxie", "Muffin", "Mya", "Nala", "Nell", "Nellie", "Nikki", "Nina", "Noel", "Nola", "Nori", "Olive", "Olivia",
            "Oreo", "Paisley", "Pandora", "Paris", "Peaches", "Peanut", "Pearl", "Pebbles", "Penny", "Pepper", "Phoebe",
            "Piper", "Pippa", "Pixie", "Polly", "Poppy", "Precious", "Princess", "Priscilla", "Raven", "Reese", "Riley",
            "Rose", "Rosie", "Roxy", "Ruby", "Sadie", "Sage", "Sally", "Sam", "Samantha", "Sammie", "Sandy", "Sasha",
            "Sassy", "Savannah", "Scarlet", "Shadow", "Sheba", "Shelby", "Shiloh", "Sierra", "Sissy", "Sky", "Smokey",
            "Snickers", "Sophia", "Sophie", "Star", "Stella", "Sugar", "Suki", "Summer", "Sunny", "Sweetie", "Sydney",
            "Tasha", "Tessa", "Tilly", "Tootsie", "Trixie", "Violet", "Willow", "Winnie", "Xena", "Zelda", "Zoe", "Abe",
            "Abbott", "Ace", "Aero", "Aiden", "AJ", "Albert", "Alden", "Alex", "Alfie", "Alvin", "Amos", "Andy", "Angus",
            "Apollo", "Archie", "Aries", "Artie", "Ash", "Austin", "Axel", "Bailey", "Bandit", "Barkley", "Barney", "Baron",
            "Baxter", "Bear", "Beau", "Benji", "Benny", "Bentley", "Billy", "Bingo", "Blake", "Blaze", "Blue", "Bo", "Boomer",
            "Brady", "Brody", "Brownie", "Bruce", "Bruno", "Brutus", "Bubba", "Buck", "Buddy", "Buster", "Butch", "Buzz",
            "Cain", "Captain", "Carter", "Cash", "Casper", "Champ", "Chance", "Charlie", "Chase", "Chester", "Chewy", "Chico",
            "Chief", "Chip", "CJ", "Clifford", "Clyde", "Coco", "Cody", "Colby", "Cooper", "Copper", "Damien", "Dane", "Dante",
            "Denver", "Dexter", "Diego", "Diesel", "Dodge", "Drew", "Duke", "Dylan", "Eddie", "Eli", "Elmer", "Emmett", "Evan",
            "Felix", "Finn", "Fisher", "Flash", "Frankie", "Freddy", "Fritz", "Gage", "George", "Gizmo", "Goose", "Gordie",
            "Griffin", "Gunner", "Gus", "Hank", "Harley", "Harvey", "Hawkeye", "Henry", "Hoss", "Huck", "Hunter", "Iggy",
            "Ivan", "Jack", "Jackson", "Jake", "Jasper", "Jax", "Jesse", "Joey", "Johnny", "Judge", "Kane", "King", "Kobe",
            "Koda", "Lenny", "Leo", "Leroy", "Levi", "Lewis", "Logan", "Loki", "Louie", "Lucky", "Luke", "Marley", "Marty",
            "Maverick", "Max", "Maximus", "Mickey", "Miles", "Milo", "Moe", "Moose", "Morris", "Murphy", "Ned", "Nelson",
            "Nero", "Nico", "Noah", "Norm", "Oakley", "Odie", "Odin", "Oliver", "Ollie", "Oreo", "Oscar", "Otis", "Otto",
            "Ozzy", "Pablo", "Parker", "Peanut", "Pepper", "Petey", "Porter", "Prince", "Quincy", "Radar", "Ralph", "Rambo",
            "Ranger", "Rascal", "Rebel", "Reese", "Reggie", "Remy", "Rex", "Ricky", "Rider", "Riley", "Ringo", "Rocco",
            "Rockwell", "Rocky", "Romeo", "Rosco", "Rudy", "Rufus", "Rusty", "Sam", "Sammy", "Samson", "Sarge", "Sawyer",
            "Scooby", "Scooter", "Scout", "Scrappy", "Shadow", "Shamus", "Shiloh", "Simba", "Simon", "Smoky", "Snoopy",
            "Sparky", "Spencer", "Spike", "Spot", "Stanley", "Stewie", "Storm", "Taco", "Tank", "Taz", "Teddy", "Tesla",
            "Theo", "Thor", "Titus", "TJ", "Toby", "Trapper", "Tripp", "Tucker", "Tyler", "Tyson", "Vince", "Vinnie",
            "Wally", "Walter", "Watson", "Willy", "Winston", "Woody", "Wrigley", "Wyatt", "Yogi", "Yoshi", "Yukon",
            "Zane", "Zeus", "Ziggy");

    public final Map<Integer, Level> catLevels = new HashMap<>();

    private static Economy economy = null;
    private CommandManager commands = null;
    private String chatPrefix = "MyCat";
    public String serverName = "Your Server";

    public static MyCat instance() {
        return instance;
    }

    public static PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public static CatManager getCatManager() {
        return catManager;
    }

    public static TeleportationManager getTeleportationManager() {
        return teleportationManager;
    }

    public static LevelFactory getLevelFactory() {
        return levelFactory;
    }

    public static ParticleUtils getParticleUtils() {
        return particleUtils;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public String getChatPrefix() {
        return chatPrefix;
    }

    public void sendInfo(Player player, String message) {
        if (player == null) {
            log(message);
        } else {
            player.sendMessage(message);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    @Override
    public void onDisable() {
        saveSettings();
        reloadSettings();

        pluginEnabled = false;
    }

    @Override
    public void onEnable() {
        CatMainListener tameListener;
        CatMainListener_1_18 tameListener_1_18;
        DamageListener damageListener;

        plugin = this;
        instance = this;
        server = getServer();
        config = getConfig();

        this.commands = new CommandManager(this);

        pluginEnabled = true;

        tameListener = new CatMainListener(this);
        tameListener_1_18 = new CatMainListener_1_18(this);
        damageListener = new DamageListener(this);
        catManager = new CatManager(this);
        teleportationManager = new TeleportationManager(this);
        levelFactory = new LevelFactory(this);

        PluginManager pm = getServer().getPluginManager();

        // Check for Vault
        Plugin vaultPlugin = pm.getPlugin("Vault");
        if (vaultPlugin != null && vaultPlugin.isEnabled()) {
            log("Vault detected.");
            setupEconomy();
            RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
            RegisteredServiceProvider<Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(Chat.class);
            if (permissionProvider == null || chatProvider == null) {
                plugin.log("A permission provider or a chat provider was not found! Will not enable the vault integration!");
            } else {
                this.vaultEnabled = true;
            }
        } else {
            log("Vault not found.");
        }

        permissionsManager = new PermissionsManager(this);
        particleUtils = new ParticleUtils(this);

        reloadSettings();
        saveSettings();

        permissionsManager.load();
        Version version = VersionFactory.getServerVersion();

        getServer().getPluginManager().registerEvents(tameListener, this);
        if (version.isCompatible("1.18")) {
            getServer().getPluginManager().registerEvents(tameListener_1_18, this);
        }
        getServer().getPluginManager().registerEvents(damageListener, this);

        // The cat distance checker, might take some extra powerrr. Checks every ~30 seconds. Starts after 1,5 minutes.
        if (playerDistanceCheck && automaticTeleportation) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new DistanceTask(this), 20L * 60L, 20L * 10L);
        }

        // Attack mode / angry checker
        // keeps the cat on a target
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new AttackModeTask(this), 20L * 30L, 20L * 2L);

        // Enable metrics
        Metrics metrics = new Metrics(this, pluginId);
    }

    public void log(String message) {
        plugin.getLogger().info(message);
    }

    public void logDebug(String message) {
        if (this.debug) {
            plugin.getLogger().info("[Debug] " + message);
        }
    }

    public void reloadSettings() {
        reloadConfig();
        loadSettings();
    }

    public void loadSettings() {
        config = getConfig();

        this.debug = config.getBoolean("Settings.Debug", false);
        this.serverName = config.getString("Settings.ServerName", "Your Server");
        this.chatPrefix = config.getString("Settings.ChatPrefix", "MyCat");
        this.instantSave = config.getBoolean("Settings.InstantSaveConfig", false);
        this.automaticTeleportation = config.getBoolean("Settings.AutomaticTeleportation", true);
        this.teleportAllTameables = config.getBoolean("Settings.TeleportAllTameables", false);
        this.experimentalTeleport = config.getBoolean("Settings.EnableExperimentalTeleport", false);
        this.playerDistanceCheck = config.getBoolean("Settings.PlayerDistanceCheck", true);
        this.expandedSearch = config.getBoolean("Settings.ExpandedSearch", false);
        this.randomCollarColor = config.getBoolean("CatSettings.RandomCollarColor", true);
        this.useLevels = config.getBoolean("CatSettings.UseLevels", true);
        this.teleportOnWorldChange = config.getBoolean("CatSettings.TeleportOnWorldChange", true);
        this.onlyShowNametagOnHover = config.getBoolean("CatSettings.OnlyShowNametagOnHover", false);
        this.showLevelsInNametag = config.getBoolean("CatSettings.ShowLevelsInNametag", true);
        this.allowPlayerKillExp = config.getBoolean("CatSettings.AllowPlayerKillExp", true);
        this.allowNametagRename = config.getBoolean("CatSettings.AllowNametagRename", true);
        this.allowRevival = config.getBoolean("CatSettings.AllowRevival", true);
        this.revivalPrice = config.getInt("CatSettings.RevivalPricePerLevel", 200);
        this.revivalUsingPlayerExp = config.getBoolean("CatSettings.RevivalUsingPlayerExp", false);
        this.allowArrowDamage = config.getBoolean("CatSettings.AllowArrowDamage", false);
        this.lifesteal = config.getDouble("CatSettings.Lifesteal", 0.25D);
        if (config.contains("CatSettings.CatNames") && !config.getStringList("CatSettings.CatNames").isEmpty()) {
            this.catNames = config.getStringList("CatSettings.CatNames");
        }

        // Levels
        if (config.getConfigurationSection("CatSettings.Levels") != null) {
            this.catLevels.clear();
            ConfigurationSection levelsSection = config.getConfigurationSection("CatSettings.Levels");
            if (levelsSection != null) {
                for (String level : levelsSection.getKeys(false)) {
                    if (config.getConfigurationSection("CatSettings.Levels." + level) != null) {
                        int exp = config.getInt("CatSettings.Levels." + level + ".Experience");
                        double health = config.getInt("CatSettings.Levels." + level + ".Health");
                        double damage = config.getInt("CatSettings.Levels." + level + ".Damage");

                        this.catLevels.put(Integer.parseInt(level), getLevelFactory().newLevel(Integer.parseInt(level), exp, health, damage));
                    }
                }
            }
        } else {
            // Put levels into the hashmap
            // Level format - [level, experience]
            this.catLevels.clear();
            this.catLevels.put(1, getLevelFactory().newLevel(1, 0, 20, 4));
            this.catLevels.put(2, getLevelFactory().newLevel(2, 10, 21, 5));
            this.catLevels.put(3, getLevelFactory().newLevel(3, 100, 22, 6));
            this.catLevels.put(4, getLevelFactory().newLevel(4, 200, 23, 7));
            this.catLevels.put(5, getLevelFactory().newLevel(5, 500, 24, 8));
            this.catLevels.put(6, getLevelFactory().newLevel(6, 1000, 26, 11));
            this.catLevels.put(7, getLevelFactory().newLevel(7, 2000, 29, 13));
            this.catLevels.put(8, getLevelFactory().newLevel(8, 3000, 31, 15));
            this.catLevels.put(9, getLevelFactory().newLevel(9, 4000, 33, 17));
            this.catLevels.put(10, getLevelFactory().newLevel(10, 5000, 36, 20));
        }

        // Messages and sounds
        this.levelUpSound = config.getString("PlayerInteraction.LevelUpSound", "ENTITY_CAT_PURR");
        this.levelUpString = config.getString("PlayerInteraction.LevelUpString", "&5&l[{chatPrefix}] &r&5Your cat, {catNameColor}{catName}&5, just leveled up to &dLevel {level}&5!");
        this.cannotTeleportTameableString = config.getString("PlayerInteraction.CannotTeleportTameableString", "&c&l[{chatPrefix}] &r&cHello! Looks like you just teleported away from your Pet(s)! " +
                "They can sadly not find a safe place to stay, so they are staying behind for now :( They will be waiting for you where you left them...");
        this.newCatString = config.getString("PlayerInteraction.NewCatString", "&6&l[{chatPrefix}] &r&6Congratulations with your new cat, {catNameColor}{catName}&6!");
        this.deadCatString = config.getString("PlayerInteraction.DeadCatString", "&c&l[{chatPrefix}] &r&cYour cat, {catNameColor}{catName}&c, just passed away... {catNameColor}{catName}&c lived for {time}{deadCatLevelString}.");
        this.deadCatLevelString = config.getString("PlayerInteraction.DeadCatLevelString", ", and got to &4Level {level}&c");
        this.commandComehereString = config.getString("PlayerInteraction.CommandComehereString", "&6&l[{chatPrefix}] &r&6Come here! Good catgo, {catNameColor}{catName}&6!");

        catManager.load();
    }

    public void saveSettings() {
        config.set("Settings.ServerName", this.serverName);
        config.set("Settings.Debug", this.debug);
        config.set("Settings.ChatPrefix", this.chatPrefix);
        config.set("Settings.InstantSaveConfig", this.instantSave);
        config.set("Settings.AutomaticTeleportation", this.automaticTeleportation);
        config.set("Settings.ExpandedSearch", this.expandedSearch);
        config.set("Settings.EnableExperimentalTeleport", this.experimentalTeleport);
        config.set("Settings.PlayerDistanceCheck", this.playerDistanceCheck);
        config.set("CatSettings.RandomCollarColor", this.randomCollarColor);
        config.set("CatSettings.UseLevels", this.useLevels);
        config.set("CatSettings.TeleportOnWorldChange", this.teleportOnWorldChange);
        config.set("Settings.TeleportAllTameables", this.teleportAllTameables);
        config.set("CatSettings.OnlyShowNametagOnHover", this.onlyShowNametagOnHover);
        config.set("CatSettings.ShowLevelsInNametag", this.showLevelsInNametag);
        config.set("CatSettings.AllowPlayerKillExp", this.allowPlayerKillExp);
        config.set("CatSettings.AllowNametagRename", this.allowNametagRename);
        config.set("CatSettings.catNames", this.catNames);
        config.set("CatSettings.AllowRevival", this.allowRevival);
        config.set("CatSettings.RevivalPricePerLevel", this.revivalPrice);
        config.set("CatSettings.RevivalUsingPlayerExp", this.revivalUsingPlayerExp);
        config.set("CatSettings.AllowArrowDamage", this.allowArrowDamage);
        config.set("CatSettings.Lifesteal", this.lifesteal);

        // Levels
        for (Integer level : this.catLevels.keySet()) {
            Level levelObject = this.catLevels.get(level);
            config.set("CatSettings.Levels." + level + ".Experience", levelObject.exp);
            config.set("CatSettings.Levels." + level + ".Health", levelObject.health);
            config.set("CatSettings.Levels." + level + ".Damage", levelObject.damage);
        }

        // Messages and sounds
        config.set("PlayerInteraction.LevelUpSound", this.levelUpSound);
        config.set("PlayerInteraction.LevelUpString", this.levelUpString);
        config.set("PlayerInteraction.CannotTeleportTameableString", this.cannotTeleportTameableString);
        config.set("PlayerInteraction.NewCatString", this.newCatString);
        config.set("PlayerInteraction.DeadCatString", this.deadCatString);
        config.set("PlayerInteraction.DeadCatLevelString", this.deadCatLevelString);
        config.set("PlayerInteraction.CommandComehereString", this.commandComehereString);

        saveConfig();
        catManager.save();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return this.commands.onCommand(sender, cmd, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        return this.commands.onTabComplete(sender, cmd, alias, args);
    }
}