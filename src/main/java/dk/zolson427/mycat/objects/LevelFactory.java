package dk.zolson427.mycat.objects;

import dk.zolson427.mycat.MyCat;

public class LevelFactory {
    private final MyCat plugin;

    public LevelFactory(MyCat plugin) {
        this.plugin = plugin;
    }

    public class Level {
        public int level;
        public int exp;
        public double health;
        public double damage;

        public Level(int levelInt, int expNeeded, double healthStat, double damageStat) {
            this.level = levelInt;
            this.exp = expNeeded;
            this.health = healthStat;
            this.damage = damageStat;
            plugin.logDebug("New level created! Level: " + level + " Exp: " + exp + " Health: " + health + " Damage: " + damage);
        }
    }

    public Level newLevel(int level, int exp, double health, double damage) {
        return new Level(level, exp, health, damage);
    }
}