package dk.zolson427.mycat.managers;

import java.text.DecimalFormat;
import java.util.*;

import dk.zolson427.mycat.MyCat;
import dk.zolson427.mycat.objects.myCat;
import dk.zolson427.mycat.objects.LevelFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Cat;
import org.bukkit.util.StringUtil;

public class CommandManager {
    private final MyCat plugin;

    public CommandManager(MyCat p) {
        this.plugin = p;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!plugin.isEnabled()) {
            return false;
        }

        Player player = null;

        if ((sender instanceof Player)) {
            player = (Player) sender;
        }

        if ((cmd.getName().equalsIgnoreCase("mycat")) || (cmd.getName().equalsIgnoreCase("md")) || (cmd.getName().equalsIgnoreCase("cat")) || (cmd.getName().equalsIgnoreCase("cats"))) {
            if ((args.length == 0) && (player != null)) {
                commandHelp(sender);
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player == null) {
                        plugin.reloadSettings();
                        this.plugin.log("Reloaded the configurations.");

                        return true;
                    }

                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.reload"))) {
                        return false;
                    }

                    this.plugin.reloadSettings();
                    sender.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ":" + ChatColor.AQUA + " Reloaded the configurations.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("save")) {
                    if (player == null) {
                        plugin.saveSettings();
                        this.plugin.log("Saved the configurations.");

                        return true;
                    }

                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.save"))) {
                        return false;
                    }

                    this.plugin.saveSettings();
                    sender.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ":" + ChatColor.AQUA + " Saved the configurations.");
                    return true;
                }
                if ((args[0].equalsIgnoreCase("help")) && (player != null)) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.help"))) {
                        return false;
                    }

                    commandList(sender);
                    return true;
                }
                if (((args[0].equalsIgnoreCase("cats")) || (args[0].equalsIgnoreCase("list"))) && (player != null)) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.cats"))) {
                        return false;
                    }

                    commandCatList(sender);
                    return true;
                }
                if (args[0].equalsIgnoreCase("dead") && (player != null)) {
                    if (!plugin.allowRevival || ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.dead")))) {
                        return false;
                    }

                    commandCatDead(sender);
                    return true;
                }
                if (args[0].equalsIgnoreCase("tradeaccept") && (player != null)) {
                    if (((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.trade")))) {
                        return false;
                    }

                    commandTradeAccept(sender);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("tradedeny") || args[0].equalsIgnoreCase("tradedecline")) && (player != null)) {
                    if (((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.trade")))) {
                        return false;
                    }

                    commandTradeDeny(sender);
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Not a MyCat Command! Check /mycat help");
            } else if ((args.length == 2) && (player != null)) {
                if ((args[0].equalsIgnoreCase("putdown")) || (args[0].equalsIgnoreCase("kill"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.putdown"))) {
                        return false;
                    }
                    int catIdentifier;
                    try {
                        catIdentifier = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                        return true;
                    }

                    commandCatPutdown(sender, catIdentifier);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("free")) || (args[0].equalsIgnoreCase("setfree")) || (args[0].equalsIgnoreCase("release"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.free"))) {
                        return false;
                    }

                    int catIdentifier;
                    try {
                        catIdentifier = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                        return true;
                    }

                    commandCatFree(sender, catIdentifier);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("stats")) || (args[0].equalsIgnoreCase("info"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.stats"))) {
                        return false;
                    }
                    int catIdentifier;
                    try {
                        catIdentifier = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                        return true;
                    }

                    commandCatStats(sender, catIdentifier);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("comehere"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.comehere"))) {
                        return false;
                    }

                    int catIdentifier;
                    if (args[1].equalsIgnoreCase("all")) {
                        catIdentifier = -1;
                    } else {
                        try {
                            catIdentifier = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                            return true;
                        }
                    }

                    commandCatComehere(sender, catIdentifier);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("sit"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.sit"))) {
                        return false;
                    }

                    int catIdentifier;
                    if (args[1].equalsIgnoreCase("all")) {
                        catIdentifier = -1;
                    } else {
                        try {
                            catIdentifier = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                            return true;
                        }
                    }

                    commandCatStandSit(sender, catIdentifier, true);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("stand"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.sit"))) {
                        return false;
                    }

                    int catIdentifier;
                    if (args[1].equalsIgnoreCase("all")) {
                        catIdentifier = -1;
                    } else {
                        try {
                            catIdentifier = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                            return true;
                        }
                    }

                    commandCatStandSit(sender, catIdentifier, false);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("attack"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.togglemode"))) {
                        return false;
                    }

                    int catIdentifier;
                    if (args[1].equalsIgnoreCase("all")) {
                        catIdentifier = -1;
                    } else {
                        try {
                            catIdentifier = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                            return true;
                        }
                    }

                    commandCatDefendAttack(sender, catIdentifier, true);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("defend"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.togglemode"))) {
                        return false;
                    }

                    int catIdentifier;
                    if (args[1].equalsIgnoreCase("all")) {
                        catIdentifier = -1;
                    } else {
                        try {
                            catIdentifier = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                            return true;
                        }
                    }

                    commandCatDefendAttack(sender, catIdentifier, false);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("revive"))) {
                    if (!plugin.allowRevival || ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.revive")))) {
                        return false;
                    }

                    //check is args1 is a number
                    int catIdentifier;
                    try {
                        catIdentifier = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                        return true;
                    }

                    commandReviveCat(player, sender, catIdentifier);
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Not a MyCat Command! Check /mycat help");
            } else if ((args.length == 3) && (player != null)) {
                if ((args[0].equalsIgnoreCase("editlevel") || args[0].equalsIgnoreCase("setlevel"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.editlevel"))) {
                        return false;
                    }

                    //check is args1 is a number
                    int catIdentifier;
                    int catLevel;
                    try {
                        catIdentifier = Integer.parseInt(args[1]);
                        catLevel = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                        return true;
                    }

                    commandEditLevel(sender, catIdentifier, catLevel);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("setid")) || (args[0].equalsIgnoreCase("changeid"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.setid"))) {
                        return false;
                    }

                    commandCatSetId(sender, args);
                    return true;
                }

                if ((args[0].equalsIgnoreCase("rename"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.rename"))) {
                        return false;
                    }

                    commandCatRename(sender, args);
                    return true;
                }

                if ((args[0].equalsIgnoreCase("trade"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.trade"))) {
                        return false;
                    }

                    //check is args1 is a number
                    int catIdentifier;
                    String recipientName = args[2];
                    try {
                        catIdentifier = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                        return true;
                    }

                    Player recipient = plugin.getServer().getPlayer(recipientName);
                    if (recipient == null) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a player with that name!");
                        return true;
                    }
                    if (recipient.getUniqueId().equals(((Player) sender).getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "You cannot send a trade request to yourself!");
                        return true;
                    }

                    commandTrade(sender, catIdentifier, recipient, 0.0D);
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Not a MyCat Command! Check /mycat help");
            } else if ((args.length == 4) && (player != null)) {
                if ((args[0].equalsIgnoreCase("trade"))) {
                    if ((!player.isOp()) && (!MyCat.getPermissionsManager().hasPermission(player, "mycat.trade"))) {
                        return false;
                    }

                    //check is args1 is a number
                    int catIdentifier;
                    String recipientName = args[2];
                    double price;
                    try {
                        catIdentifier = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
                        return true;
                    }
                    try {
                        price = Double.parseDouble(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid price!");
                        return true;
                    }

                    if (price < 0.0D) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid price!");
                        return true;
                    }

                    Player recipient = plugin.getServer().getPlayer(recipientName);
                    if (recipient == null) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a player with that name!");
                        return true;
                    }
                    if (recipient.getUniqueId().equals(((Player) sender).getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "You cannot send a trade request to yourself!");
                        return true;
                    }

                    commandTrade(sender, catIdentifier, recipient, price);
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Not a MyCat Command! Check /mycat help");
            } else {
                sender.sendMessage(ChatColor.RED + "Too many arguments! Check /mycat help");
            }
        }
        return true;
    }

    private boolean commandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "---------------- " + plugin.getDescription().getFullName() + " ----------------");
        sender.sendMessage(ChatColor.AQUA + "By Zack (zolson427)");
        sender.sendMessage(ChatColor.AQUA + "");
        int catsOwned = MyCat.getCatManager().catsOwned((Player) sender);
        String cats = " cats!";
        if (catsOwned == 1) {
            cats = " cat!";
        }
        sender.sendMessage(ChatColor.AQUA + "You currently own " + ChatColor.WHITE + catsOwned + ChatColor.AQUA + cats);
        sender.sendMessage(ChatColor.AQUA + "");
        sender.sendMessage(ChatColor.AQUA + "Use " + ChatColor.WHITE + "/mycat help" + ChatColor.AQUA + " for a list of commands!");

        return true;
    }

    private boolean commandList(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "---------------- " + this.plugin.getDescription().getFullName() + " ----------------");
        sender.sendMessage(ChatColor.AQUA + "/mycat" + ChatColor.WHITE + " - Basic info");
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.help"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat help" + ChatColor.WHITE + " - This command");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.reload"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat reload" + ChatColor.WHITE + " - Reloads the MyCat system");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.save"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat save" + ChatColor.WHITE + " - Saves the current changes to the MyCat system");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.cats"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat list" + ChatColor.WHITE + " - View a list with your current Cats");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.putdown"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat putdown <id>" + ChatColor.WHITE + " - Kills a cat you own");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.free"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat release <id>" + ChatColor.WHITE + " - Set one of your Cats free!");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.comehere"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat comehere <id>" + ChatColor.WHITE + " - Forces your Cat to teleport to your location");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.sit"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat sit <id | all>" + ChatColor.WHITE + " - Tells your cat(s) to sit and keep their position(s)");
                sender.sendMessage(ChatColor.AQUA + "/mycat stand <id | all>" + ChatColor.WHITE + " - Tells your cat(s) to stand up and roam free");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.togglemode"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat attack <id | all>" + ChatColor.WHITE + " - Tells your cat(s) to attack any mobs nearby");
                sender.sendMessage(ChatColor.AQUA + "/mycat defend <id | all>" + ChatColor.WHITE + " - Tells your cat(s) to defend you");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.trade"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat trade <id> <player> [price]" + ChatColor.WHITE + " - Send a trade request to another player");
                sender.sendMessage(ChatColor.AQUA + "/mycat tradeaccept" + ChatColor.WHITE + " - Accept a trade");
                sender.sendMessage(ChatColor.AQUA + "/mycat tradedecline" + ChatColor.WHITE + " - Decline a trade");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.stats"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat info <id>" + ChatColor.WHITE + " - Gets stats and other info about a Cat you own");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.rename"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat rename <id> <name>" + ChatColor.WHITE + " - Renames a Cat you own");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.setid"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat setid <id> <newid>" + ChatColor.WHITE + " - Assigns a custom ID to a Cat you own");
            }
            if ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.editlevel"))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat setlevel <id> <level>" + ChatColor.WHITE + " - Sets a cat's level");
            }
            if (plugin.allowRevival && ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.dead")))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat dead" + ChatColor.WHITE + " - List of Cats that have gone to another world");
            }
            if (plugin.allowRevival && ((sender.isOp()) || (MyCat.getPermissionsManager().hasPermission(player, "mycat.revive")))) {
                sender.sendMessage(ChatColor.AQUA + "/mycat revive <id>" + ChatColor.WHITE + " - Resurrects a Cat for a fee");
            }
        }

        return true;
    }

    private boolean commandCatList(CommandSender sender) {
        // Sort the cats after their ID (identifier)
        TreeMap<Integer, myCat> catsSorted = new TreeMap<>();
        for (myCat myCat : MyCat.getCatManager().getAliveCats(((Player) sender).getUniqueId())) {
            catsSorted.put(myCat.getIdentifier(), myCat);
        }
        DecimalFormat df = new DecimalFormat("#.#");

        sender.sendMessage(ChatColor.YELLOW + "---------------- " + this.plugin.getDescription().getFullName() + " ----------------");
        for (Map.Entry<Integer, myCat> entry : catsSorted.entrySet()) {
            Cat cat = (Cat) plugin.getServer().getEntity(entry.getValue().getCatId());
            String healthString = "";
            if (cat != null) {
                double health = cat.getHealth();
                AttributeInstance maxHealthInstance = cat.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                double maxHealth;
                if (maxHealthInstance != null) {
                    maxHealth = maxHealthInstance.getValue();
                }
                else {
                    maxHealth = health;
                }
                healthString = " " + ChatColor.BLUE + "(HP: " + df.format(health) + "/" + df.format(maxHealth) + ")";
            }

            sender.sendMessage(ChatColor.AQUA + "#" + entry.getValue().getIdentifier() + ChatColor.WHITE + " - " + ChatColor.AQUA + entry.getValue().getCatName() + healthString);
        }
        return true;
    }

    private boolean commandCatDead(CommandSender sender) {
        // Sort the cats after their ID (identifier)
        TreeMap<Integer, myCat> catsSorted = new TreeMap<>();
        for (myCat myCat : MyCat.getCatManager().getDeadCats(((Player) sender).getUniqueId())) {
            catsSorted.put(myCat.getIdentifier(), myCat);
        }

        sender.sendMessage(ChatColor.YELLOW + "---------------- " + this.plugin.getDescription().getFullName() + " ----------------");
        for (Map.Entry<Integer, myCat> entry : catsSorted.entrySet()) {
            sender.sendMessage(ChatColor.AQUA + "#" + entry.getValue().getIdentifier() + ChatColor.WHITE + " - " + ChatColor.AQUA + entry.getValue().getCatName() + ChatColor.WHITE + " LVL " + entry.getValue().getLevel() + " " + ChatColor.GREEN + " $" + entry.getValue().getRevivalPrice());
        }
        return true;
    }

    private boolean commandCatRename(CommandSender sender, String[] args) {
        int catIdentifier;
        try {
            catIdentifier = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please enter a valid ID! Check /mycat cats");
            return false;
        }

        StringBuilder name = new StringBuilder(args[2]);

        for (int i = 3; i < args.length; i++) {
            name.append(" ").append(args[i]);
        }

        myCat myCat = MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId());
        if (myCat == null) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a Cat with that ID! Check /mycat cats");
            return false;
        }

        if ((name.length() == 0) || name.length() > 16) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please choose a name between 1 and 16 characters for your Cat!");
            return false;
        }

        if (!myCat.setCatName(name.toString())) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "An error occured! Could not set Cat name!");
            return false;
        }

        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.AQUA + "From now on, I will call you " + myCat.getCatColor() + myCat.getCatName() + ChatColor.RESET + ChatColor.AQUA + "!");

        return true;
    }

    private boolean commandCatSetId(CommandSender sender, String[] args) {
        int catIdentifier;
        try {
            catIdentifier = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Invalid ID! Type /mycat cats to find the ID.");
            return false;
        }
        String new_id = args[2];

        myCat myCat = MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId());
        if (myCat == null) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a Cat with that ID! Check /mycat cats");
            return false;
        }

        if (new_id.isEmpty() || new_id.length() > 10) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Please choose an identifier between 1 and 10 figures for your Cat!");
            return false;
        }

        int id;
        try {
            id = Integer.parseInt(new_id);
        } catch (NumberFormatException e) {
            plugin.logDebug("Error while trying to format ID from string: " + e);
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Your Cat's identifier can only consist of numbers!");
            return false;
        }

        if (!MyCat.getCatManager().setNewId(myCat, id)) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "An error occurred! Could not set new Cat ID!");
            return false;
        }

        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.AQUA + "New Cat ID successfully set!");

        return true;
    }

    private boolean commandCatPutdown(CommandSender sender, int catIdentifier) {
        myCat myCat = MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId());
        if (myCat == null) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a Cat with that ID! Check /mycat cats");
            return false;
        }

        Cat cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());

        if (cat == null) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Aww bonkers! Seems like your Cat cannot be found... Is it loaded?");
            return false;
        }

        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.AQUA + "I'm sorry my friend...");
        cat.setHealth(0);

        return true;
    }

    private boolean commandCatFree(CommandSender sender, int catIdentifier) {
        myCat myCat = MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId());

        if (myCat == null) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a Cat with that ID! Check /mycat cats");
            return false;
        }

        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.AQUA + "It was great having you here, " + myCat.getCatColor() + myCat.getCatName() + ChatColor.RESET + ChatColor.AQUA + "...");

        Cat cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());

        if (cat != null) {
            cat.setCustomName("");
            cat.setCustomNameVisible(false);
            if (cat.isSitting()) {
                cat.setSitting(false);
            }
            cat.setTamed(false);
        }

        MyCat.getCatManager().removeCat(myCat.getCatId());

        return true;
    }

    private boolean commandCatDefendAttack(CommandSender sender, int catIdentifier, boolean toAttack) {
        List<myCat> myCats = new ArrayList<>();

        String mode = "Defend me";
        if (toAttack) {
            mode = "Attack";
        }

        if (catIdentifier == -1) {
            myCats = MyCat.getCatManager().getAliveCats(((Player) sender).getUniqueId());
            sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.AQUA + mode + ", my cats!");
        } else {
            myCats.add(MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId()));
            if (myCats.get(0) != null) {
                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.AQUA + mode + ", " + myCats.get(0).getCatColor() + myCats.get(0).getCatName() + ChatColor.RESET + ChatColor.AQUA + "!");
            }
        }

        for (myCat myCat : myCats) {
            if (myCat == null) {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a Cat with that ID! Check /mycat cats");
                return false;
            }

            myCat.setAngryMode(toAttack);
        }

        return true;
    }

    private boolean commandCatStandSit(CommandSender sender, int catIdentifier, boolean toSit) {
        List<myCat> myCats = new ArrayList<>();

        String mode = "Stand";
        if (toSit) {
            mode = "Sit";
        }

        if (catIdentifier == -1) {
            myCats = MyCat.getCatManager().getAliveCats(((Player) sender).getUniqueId());
            sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.AQUA + mode + ", my cats!");
        } else {
            myCats.add(MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId()));
            if (myCats.get(0) != null) {
                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.AQUA + mode + ", " + myCats.get(0).getCatColor() + myCats.get(0).getCatName() + ChatColor.RESET + ChatColor.AQUA + "!");
            }
        }

        for (myCat myCat : myCats) {
            if (myCat == null) {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a Cat with that ID! Check /mycat cats");
                return false;
            }

            myCat.sit(toSit);
        }

        return true;
    }

    private boolean commandCatStats(CommandSender sender, int catIdentifier) {
        myCat myCat = MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId());
        if (myCat == null) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET + ChatColor.RED + "Could not find a Cat with that ID! Check /mycat cats");
            return false;
        }

        Cat cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());
        DecimalFormat df = new DecimalFormat("#.#");

        sender.sendMessage(ChatColor.YELLOW + "---------------- " + this.plugin.getDescription().getFullName() + " ----------------");

        sender.sendMessage(ChatColor.AQUA + "Name: " + myCat.getCatColor() + myCat.getCatName());

        if (plugin.useLevels) {
            sender.sendMessage(ChatColor.AQUA + "Level: " + ChatColor.WHITE + myCat.getLevel());

            // Calculate and make experience string
            String experienceString;
            double exp = myCat.getExperience();
            double maxExp = 0;

            Map<Integer, LevelFactory.Level> levels = plugin.catLevels;

            for (Map.Entry<Integer, LevelFactory.Level> levelSet : levels.entrySet()) {
                int levelInt = levelSet.getKey();
                int levelExp = levelSet.getValue().exp;

                // If experience is under the experience needed to level up
                if (exp < levelExp) {
                    // If there is a level under the current one, check if the exp is over or equals to the value of that levelup
                    if (levels.containsKey((levelInt - 1)) && exp >= levels.get((levelInt - 1)).exp) {
                        maxExp = levelExp;
                        break;
                    }
                    // Exp is under needed, and there is no level under. Lowest level found. User is at lowest level then

                    maxExp = levelExp;
                    break;
                } else if (exp > levelExp && !levels.containsKey((levelInt + 1))) // Highest level
                {
                    if (levels.get((levelInt - 1)) == null) {
                        plugin.logDebug("Something went wrong! Last level, there is no level under! Return!");
                        return false;
                    }
                    maxExp = levelExp;
                }
            }

            plugin.logDebug("Exp: " + exp + " - MaxExp: " + maxExp);
            if (maxExp != 0) {
                double percent = (exp / maxExp) * 100;
                plugin.logDebug("Current percent: " + percent);

                experienceString = calculatePercentString(percent) + ChatColor.AQUA + "" + ChatColor.BOLD + " [" + ChatColor.DARK_AQUA + df.format(exp) +
                        ChatColor.AQUA + "" + ChatColor.BOLD + "/" + ChatColor.RESET + ChatColor.AQUA + df.format(maxExp) + ChatColor.AQUA + "" + ChatColor.BOLD + "]";
                sender.sendMessage(ChatColor.AQUA + "Experience: " + experienceString);
            }
        }

        if (cat != null) {
            // Health graphics
            double health = cat.getHealth();
            AttributeInstance maxHealthInstance = cat.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double maxHealth;
            if (maxHealthInstance != null) {
                maxHealth = maxHealthInstance.getValue();
            }
            else {
                maxHealth = health;
            }

            double percent = (health / maxHealth) * 100;

            String healthString = calculatePercentString(percent);

            sender.sendMessage(ChatColor.AQUA + "Health: " + healthString + ChatColor.AQUA + "" + ChatColor.BOLD + " [" + ChatColor.DARK_AQUA + df.format(health) +
                    ChatColor.AQUA + "" + ChatColor.BOLD + "/" + ChatColor.RESET + ChatColor.AQUA + df.format(maxHealth) + ChatColor.AQUA + "" + ChatColor.BOLD + "]");

            AttributeInstance attackDamage = cat.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            sender.sendMessage(ChatColor.AQUA + "Damage: " + ChatColor.WHITE + (attackDamage != null ? attackDamage.getValue() : 0) + " HP");
        }

        Location catLoc = myCat.getCatLocation();
        if (catLoc != null) {
            sender.sendMessage(ChatColor.AQUA + "Last Seen at: " + ChatColor.DARK_AQUA + "World: " + ChatColor.WHITE + (catLoc.getWorld() != null ? catLoc.getWorld().getName() : "Unknown World") +
                    ChatColor.DARK_AQUA + " X: " + ChatColor.WHITE + df.format(catLoc.getX()) + ChatColor.DARK_AQUA + " Y: " + ChatColor.WHITE + df.format(catLoc.getY()) +
                    ChatColor.DARK_AQUA + " Z: " + ChatColor.WHITE + df.format(catLoc.getZ()));
        }

        return true;
    }

    private String calculatePercentString(double percent) {
        String percentString = "==========";

        if (percent >= 10.0 && percent < 19.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "=" + ChatColor.AQUA + "=========";
        } else if (percent >= 20.0 && percent < 29.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "==" + ChatColor.AQUA + "========";
        } else if (percent >= 30.0 && percent < 39.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "===" + ChatColor.AQUA + "=======";
        } else if (percent >= 40.0 && percent < 49.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "====" + ChatColor.AQUA + "=====";
        } else if (percent >= 50.0 && percent < 59.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "=====" + ChatColor.AQUA + "=====";
        } else if (percent >= 60.0 && percent < 69.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "======" + ChatColor.AQUA + "====";
        } else if (percent >= 70.0 && percent < 79.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "=======" + ChatColor.AQUA + "===";
        } else if (percent >= 80.0 && percent < 89.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "========" + ChatColor.AQUA + "==";
        } else if (percent >= 90.0 && percent < 99.9) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "=========" + ChatColor.AQUA + "=";
        } else if (percent >= 100.0) {
            percentString = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "==========";
        }

        return ChatColor.AQUA + "" + ChatColor.BOLD + "[" + percentString + ChatColor.AQUA + "" + ChatColor.BOLD + "]";
    }

    private boolean commandReviveCat(Player player, CommandSender sender, int catIdentifier) {
        List<myCat> myCats = MyCat.getCatManager().getDeadCats(player.getUniqueId());
        myCat deadMyCat = null;
        for (myCat myCat : myCats) {
            if (myCat.getIdentifier() == catIdentifier) {
                deadMyCat = myCat;
                break;
            }
        }
        if (deadMyCat == null) {
            sender.sendMessage(ChatColor.RED + "You don't have a dead cat with that ID.");
            return false;
        }

        int revivalPrice = deadMyCat.getRevivalPrice();
        if (!plugin.revivalUsingPlayerExp) {
            if (MyCat.getEconomy() != null) {
                if (!MyCat.getEconomy().has(player, revivalPrice)) {
                    sender.sendMessage(ChatColor.RED + "You don't have enough funds to resurrect the cat.");
                    return false;
                }
                MyCat.getEconomy().withdrawPlayer(player, revivalPrice);
            }
        } else {
            if (player.getLevel() < revivalPrice) {
                sender.sendMessage(ChatColor.RED + "You don't have enough power to resurrect the cat. You need " + ChatColor.GOLD + (revivalPrice - player.getLevel()) + ChatColor.RED + " more levels!");
                return false;
            }
            player.setLevel(player.getLevel() - revivalPrice);
        }

        Cat cat = player.getWorld().spawn(player.getLocation(), Cat.class);
        cat.setOwner(player);
        cat.setSitting(true);
        cat.setTamed(true);

        deadMyCat.setUUID(cat.getUniqueId());
        deadMyCat.setDead(false);
        deadMyCat.updateCat();

        sender.sendMessage(ChatColor.GREEN + "Your cat has been resurrected.");
        return true;
    }

    private boolean commandCatComehere(CommandSender sender, int catIdentifier) {
        List<myCat> myCats = new ArrayList<>();

        if (catIdentifier == -1) {
            myCats = MyCat.getCatManager().getAliveCats(((Player) sender).getUniqueId());
        } else {
            myCats.add(MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId()));
        }

        for (myCat myCat : myCats) {
            if (myCat == null) {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " +
                        ChatColor.RESET + ChatColor.RED + "Could not find a Cat with that ID! Check /mycat cats");
                return false;
            }

            Cat cat = null;

            Location catLocation = myCat.getCatLocation();
            boolean useLocation = false;
            if (catLocation == null) {
                cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());
                if (cat == null) {
                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET +
                            ChatColor.RED + "Aww bonkers! Seems like your Cat cannot be found... Sorry about that!");
                    return false;
                }
            } else {
                if (catLocation.getChunk().load(false)) {
                    plugin.logDebug("Loaded the chunk sucessfully, no generate!");
                } else if (catLocation.getChunk().load(true)) {
                    plugin.logDebug("Loaded the chunk sucessfully, generated!");
                } else {
                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET +
                            ChatColor.RED + "Aww bonkers! Seems like your Cat is at a location that cannot be loaded right now!");
                    return false;
                }
                useLocation = true;
            }

            cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());

            if (cat == null) {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET +
                        ChatColor.RED + "Aww bonkers! Seems like your Cat cannot be found...");
                plugin.logDebug("Could not find Cat, even though chunks should be loaded...");
                return false;
            }

            Location playerLoc = ((Player) sender).getLocation();
            cat.teleport(playerLoc);
            cat.setSitting(false);

            String comehereString = plugin.commandComehereString.replace("{chatPrefix}", plugin.getChatPrefix()).replace("{catNameColor}", "&" + myCat.getCatColor().getChar()).replace("{catName}", myCat.getCatName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', comehereString));
		/*sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[" + plugin.getChatPrefix() + "] " + ChatColor.RESET +
				ChatColor.GOLD + "Come here! Good catgo, " + cat.getCatName() + "!");*/

            myCat.updateCat();

            if (useLocation) {
                catLocation.getChunk().unload(true);
                plugin.logDebug("Unloaded the chunk sucessfully!");
            }
        }

        return true;
    }

    private boolean commandTradeAccept(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = ((Player) sender);

        CatManager catManager = MyCat.getCatManager();
        if (!catManager.hasTrade(player.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "You don't have any pending trades!");
            return false;
        }

        double tradePrice = catManager.getTradePrice(player.getUniqueId());
        if (tradePrice > 0.0D) {
            if (MyCat.getEconomy() == null) {
                sender.sendMessage(ChatColor.GOLD + "No economy provider found, you cannot trade cats for cash!");
                return false;
            }

            if (!MyCat.getEconomy().has(player, tradePrice)) {
                sender.sendMessage(ChatColor.GOLD + "You don't have enough money to trade for that cat!");
                return false;
            }
        }

        if (!catManager.canTameMoreCats(player)) {
            sender.sendMessage(ChatColor.GOLD + "You cannot own any more cats!");
            return false;
        }

        myCat myCat = catManager.getTradeCat(player.getUniqueId());
        Cat cat = null;

        if (myCat != null) {
            cat = (Cat) plugin.getServer().getEntity(myCat.getCatId());
        }

        if (cat == null || !cat.isValid() || cat.isDead()) {
            sender.sendMessage(ChatColor.GOLD + "The cat you are trying to accept cannot be found! Is it dead?");
            return false;
        }

        catManager.acceptTrade(player);
        sender.sendMessage(ChatColor.GOLD + "You successfully accepted the trade request!");
        Player tradeSender = plugin.getServer().getPlayer(myCat.getOwnerId());
        if (tradeSender != null) {
            tradeSender.sendMessage(ChatColor.GOLD + "Cat trade request got accepted by recipient!");
        }
        return true;
    }

    private boolean commandTradeDeny(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = ((Player) sender);

        CatManager catManager = MyCat.getCatManager();
        if (!catManager.hasTrade(player.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "You don't have any pending trades!");
            return false;
        }

        myCat myCat = catManager.getTradeCat(player.getUniqueId());

        catManager.denyTrade(player);
        sender.sendMessage(ChatColor.GOLD + "You successfully denied the trade request!");
        if (myCat != null) {
            Player tradeSender = plugin.getServer().getPlayer(myCat.getOwnerId());
            if (tradeSender != null) {
                tradeSender.sendMessage(ChatColor.GOLD + "Cat trade request got denied by recipient!");
            }
        }
        return true;
    }

    private boolean commandTrade(CommandSender sender, int catIdentifier, Player receiver, double price) {
        CatManager catManager = MyCat.getCatManager();

        myCat myCat = catManager.getCat(catIdentifier, ((Player) sender).getUniqueId());
        if (myCat == null) {
            sender.sendMessage(ChatColor.GOLD + "Could not find a cat with this ID!");
            return false;
        }
        if (myCat.isDead()) {
            sender.sendMessage(ChatColor.GOLD + "You cannot trade dead cats!");
            return false;
        }

        if (price > 0.0D) {
            if (MyCat.getEconomy() == null) {
                sender.sendMessage(ChatColor.GOLD + "No economy provider found, you cannot trade cats for cash!");
                return false;
            }

            if (!MyCat.getEconomy().has(receiver, price)) {
                sender.sendMessage(ChatColor.GOLD + "It doesn't look like the receiver will be able to.. afford that..");
                return false;
            }
        }

        if (catManager.hasTrade(receiver.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "The receiver already has a trade pending!");
            return false;
        }

        if (!receiver.isOp() && !MyCat.getPermissionsManager().hasPermission(receiver, "mycat.trade")) {
            sender.sendMessage(ChatColor.GOLD + "The receiver doesn't have permissions to trade!");
            return false;
        }

        if (!catManager.canTameMoreCats(receiver)) {
            sender.sendMessage(ChatColor.GOLD + "The receiver has reached their cat limit!");
            return false;
        }

        if (catManager.handleNewTrade(myCat, receiver, price)) {
            DecimalFormat df = new DecimalFormat("#.#");
            receiver.sendMessage(ChatColor.GOLD + "You have received a cat trade request from " + ChatColor.AQUA +
                    ((Player) sender).getDisplayName() + ChatColor.GOLD + "!\n" + ChatColor.AQUA +
                    ((Player) sender).getDisplayName() + ChatColor.GOLD + " is offering " + myCat.getCatColor() +
                    myCat.getCatName() + ChatColor.GOLD + (plugin.useLevels ? (" (" + ChatColor.AQUA + "Level " +
                    myCat.getLevel() + ChatColor.GOLD + ")") : "") + " for " + ChatColor.AQUA +
                    (price > 0.0D ? (df.format(price) + "$") : "free") + ChatColor.GOLD + "!\n\n" +
                    ChatColor.GOLD + "Accept the trade request with " + ChatColor.AQUA + "/md tradeaccept\n" +
                    ChatColor.GOLD + "Decline the trade request with " + ChatColor.AQUA + "/md tradedecline\n" +
                    ChatColor.GOLD + "Request expires in 30 seconds!");
            sender.sendMessage(ChatColor.GOLD + "Trade request successfully sent!");
        }
        return true;
    }

    private boolean commandEditLevel(CommandSender sender, int catIdentifier, int catLevel) {
        myCat myCat = MyCat.getCatManager().getCat(catIdentifier, ((Player) sender).getUniqueId());
        if (myCat == null) {
            sender.sendMessage(ChatColor.GOLD + "Could not find a cat with this ID!");
            return false;
        }
        if (catLevel < 1 || catLevel > 100) {
            sender.sendMessage(ChatColor.GOLD + "Level must be between 0 and 100!");
        }
        myCat.setLevel(catLevel);
        myCat.updateCat();
        MyCat.getCatManager().handleNewLevel(myCat);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender == null || alias == null || args == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (args.length == 1 && (cmd.getName().equalsIgnoreCase("mycat") || cmd.getName().equalsIgnoreCase("cat") || cmd.getName().equalsIgnoreCase("cats") || cmd.getName().equalsIgnoreCase("md"))) {
            List<String> arg1 = new ArrayList<>();
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.help"))) {
                arg1.add("help");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.reload"))) {
                arg1.add("reload");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.save"))) {
                arg1.add("save");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.list"))) {
                arg1.add("list");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.cats"))) {
                arg1.add("cats");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.putdown"))) {
                arg1.add("putdown");
                arg1.add("kill");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.comehere"))) {
                arg1.add("comehere");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.stats"))) {
                arg1.add("info");
                arg1.add("stats");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.free"))) {
                arg1.add("free");
                arg1.add("release");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.rename"))) {
                arg1.add("rename");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.setid"))) {
                arg1.add("setid");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.sit"))) {
                arg1.add("sit");
                arg1.add("stand");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.togglemode"))) {
                arg1.add("attack");
                arg1.add("defend");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.trade"))) {
                arg1.add("trade");
                arg1.add("tradeaccept");
                arg1.add("tradedecline");
                arg1.add("tradedeny");
            }
            if (plugin.allowRevival && (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.dead")))) {
                arg1.add("dead");
            }
            if (plugin.allowRevival && (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.revive")))) {
                arg1.add("revive");
            }
            if (player == null || (player.isOp() || MyCat.getPermissionsManager().hasPermission(player, "mycat.editlevel"))) {
                arg1.add("setlevel");
            }
            Iterable<String> FIRST_ARGUMENTS = arg1;
            StringUtil.copyPartialMatches(args[0], FIRST_ARGUMENTS, result);
        } else if (args.length == 2) {
            List<String> arg2 = new ArrayList<>();

            if (player != null && (args[0].equalsIgnoreCase("setlevel") || args[0].equalsIgnoreCase("release") || args[0].equalsIgnoreCase("free") || args[0].equalsIgnoreCase("setfree") || args[0].equalsIgnoreCase("setid"))) {
                List<myCat> myCats = MyCat.getCatManager().getCats(player.getUniqueId());
                for (myCat myCat : myCats) {
                    arg2.add(Integer.toString(myCat.getIdentifier()));
                }
            }

            if (player != null && (args[0].equalsIgnoreCase("putdown") || args[0].equalsIgnoreCase("rename") || args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("kill") || args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("trade"))) {
                List<myCat> myCats = MyCat.getCatManager().getAliveCats(player.getUniqueId());
                for (myCat myCat : myCats) {
                    arg2.add(Integer.toString(myCat.getIdentifier()));
                }
            }

            if (player != null && (plugin.allowRevival && args[0].equalsIgnoreCase("revive"))) {
                List<myCat> myCats = MyCat.getCatManager().getDeadCats(player.getUniqueId());
                for (myCat myCat : myCats) {
                    arg2.add(Integer.toString(myCat.getIdentifier()));
                }
            }

            if (player != null && (args[0].equalsIgnoreCase("sit") || args[0].equalsIgnoreCase("stand") || args[0].equalsIgnoreCase("comehere") || args[0].equalsIgnoreCase("attack") || args[0].equalsIgnoreCase("defend"))) {
                arg2.add("all");

                List<myCat> myCats = MyCat.getCatManager().getAliveCats(player.getUniqueId());
                for (myCat myCat : myCats) {
                    arg2.add(Integer.toString(myCat.getIdentifier()));
                }
            }

            Iterable<String> SECOND_ARGUMENTS = arg2;
            StringUtil.copyPartialMatches(args[1], SECOND_ARGUMENTS, result);
        } else if (args.length == 3) {
            List<String> arg3 = new ArrayList<>();

            if (args[0].equalsIgnoreCase("rename")) {
                arg3.add("<name>");
            } else if (args[0].equalsIgnoreCase("setid")) {
                arg3.add("<custom_id>");
            } else if (args[0].equalsIgnoreCase("setlevel")) {
                arg3.add("<level>");
            } else if (args[0].equalsIgnoreCase("trade")) {
                // idk how, uhm, good this might be
                Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
                for (Player playerObj : players) {
                    if (playerObj != null) {
                        arg3.add(playerObj.getName());
                    }
                }
            }

            Iterable<String> THIRD_ARGUMENTS = arg3;
            StringUtil.copyPartialMatches(args[2], THIRD_ARGUMENTS, result);
        } else if (args.length == 4) {
            List<String> arg4 = new ArrayList<>();

            if (args[0].equalsIgnoreCase("trade")) {
                arg4.add("[price]");
            }

            Iterable<String> FOURTH_ARGUMENTS = arg4;
            StringUtil.copyPartialMatches(args[3], FOURTH_ARGUMENTS, result);
        }

        Collections.sort(result);
        return result;
    }
}