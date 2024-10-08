package dk.zolson427.mycat.utils.versioning;

import org.bukkit.plugin.Plugin;

// From DogOnFire's Versioning in Werewolf
// https://github.com/DogOnFire/Werewolf
public class TesterFactory {
    private TesterFactory() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressWarnings("rawtypes")
    public static Tester getNewTester(Plugin plugin) {
        if (plugin == null) {
            return (Tester<Plugin>) t -> false;
        } else {
            return (Tester<Plugin>) Plugin::isEnabled;
        }
    }

    @SuppressWarnings("rawtypes")
    public static Tester getDefaultTester() {
        return t -> true;
    }

}