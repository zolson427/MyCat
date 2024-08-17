package dk.zolson427.mycat.listeners;

import dk.zolson427.mycat.MyCat;

import dk.zolson427.mycat.objects.myCat;
import dk.zolson427.mycat.objects.LevelFactory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {
    private final MyCat plugin;

    public DamageListener(MyCat p) {
        this.plugin = p;
    }

    @EventHandler
    public void onCatEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() != EntityType.CAT || !(MyCat.getCatManager().isCat(e.getEntity().getUniqueId()))) {
            return;
        }
        EntityType type = e.getDamager().getType();
        if (type == EntityType.PLAYER) {
            myCat cat = MyCat.getCatManager().getCat(e.getEntity().getUniqueId());
            if (cat.getOwnerId().equals(e.getDamager().getUniqueId())) {
                e.setCancelled(true);
            }
        }
        if (type == EntityType.ARROW && !plugin.allowArrowDamage) {
            e.setCancelled(true);
        }

        // TODO something with a Cat's equipped armor to lower damage caused
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCatEntityDamage2(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() != EntityType.CAT || !(MyCat.getCatManager().isCat(e.getDamager().getUniqueId())) || plugin.lifesteal == 0.0D) {
            return;
        }

        if (e.getFinalDamage() > 0) {
            double healthPoints = e.getFinalDamage() * plugin.lifesteal;

            if (healthPoints > 0) {
                plugin.logDebug("Lifesteal event, cat stole " + healthPoints + " health!");

                Cat cat = (Cat) e.getDamager();
                myCat myCat = MyCat.getCatManager().getCat(cat.getUniqueId());

                int catsLevel = myCat.getLevel();
                if (catsLevel < 1) {
                    plugin.logDebug("Level was under 1, setting level to 1");
                    catsLevel = 1;
                }

                LevelFactory.Level level = plugin.catLevels.get(catsLevel);
                if (level == null) {
                    plugin.logDebug("Level object is null, returning!");
                    return;
                }

                double health = level.health;
                if (health < 10.0) {
                    health = 10.0;
                }

                AttributeInstance catMaxHealth = cat.getAttribute(Attribute.GENERIC_MAX_HEALTH);

                if (catMaxHealth != null && catMaxHealth.getValue() != health) {
                    catMaxHealth.setBaseValue(health);
                }

                if (cat.getHealth() < health) {
                    // If health would overflow, just set full health
                    cat.setHealth(Math.min(cat.getHealth() + healthPoints, health));
                    plugin.logDebug("Gave the cat, " + myCat.getCatName() + ", " + healthPoints + " in health.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            if (damageEvent.getDamager() instanceof Cat && damageEvent.getEntity() instanceof LivingEntity) {
                if (!(MyCat.getCatManager().isCat(damageEvent.getDamager().getUniqueId())) || (damageEvent.getFinalDamage() < ((LivingEntity) damageEvent.getEntity()).getHealth())) {
                    return;
                }

                plugin.logDebug("Cat has killed " + event.getEntityType() + " with a final blow dealing " + event.getFinalDamage() + " HP!");
                if (plugin.useLevels) {
                    int gainedExp;
                    switch (event.getEntityType()) {
                        case BAT:
                        case AXOLOTL:
                        case BEE:
                        case OCELOT:
                        case CAT:
                        case PARROT:
                        case COD:
                        case CHICKEN:
                        case FOX:
                        case SILVERFISH:
                        case PUFFERFISH:
                        case RABBIT:
                        case SALMON:
                        case SHULKER:
                        case SLIME:
                        case SNOWMAN:
                        case SPIDER:
                        case ZOMBIE:
                        case SKELETON:
                        case SQUID:
                        case GLOW_SQUID:
                        case TROPICAL_FISH:
                        case TURTLE:
                        case FROG:
                        case ALLAY:
                            gainedExp = 5;
                            break;
                        case COW:
                        case MUSHROOM_COW:
                        case PIG:
                        case SHEEP:
                        case WOLF:
                        case WANDERING_TRADER:
                        case VILLAGER:
                        case STRAY:
                            gainedExp = 8;
                            break;
                        case CAVE_SPIDER:
                        case PANDA:
                        case CREEPER:
                        case DROWNED:
                        case DOLPHIN:
                        case WITHER_SKELETON:
                        case STRIDER:
                        case DONKEY:
                        case GOAT:
                        case CAMEL:
                        case WITCH:
                        case SKELETON_HORSE:
                        case VEX:
                        case VINDICATOR:
                        case HORSE:
                        case TRADER_LLAMA:
                        case HUSK:
                        case LLAMA:
                        case MULE:
                        case ZOMBIE_HORSE:
                        case ZOMBIE_VILLAGER:
                        case SNIFFER:
                        case PIGLIN:
                        case ZOMBIFIED_PIGLIN:
                        case POLAR_BEAR:
                            gainedExp = 20;
                            break;
                        case ENDERMITE:
                        case GUARDIAN:
                        case MAGMA_CUBE:
                        case PIGLIN_BRUTE:
                        case ZOGLIN:
                        case PHANTOM:
                        case PILLAGER:
                        case ENDERMAN:
                            gainedExp = 40;
                            break;
                        case BLAZE:
                        case EVOKER:
                        case GHAST:
                        case HOGLIN:
                        case ILLUSIONER:
                            gainedExp = 60;
                            break;
                        case IRON_GOLEM:
                        case RAVAGER:
                            gainedExp = 70;
                            break;
                        case PLAYER:
                            if (plugin.allowPlayerKillExp) {
                                gainedExp = 70;
                            } else {
                                gainedExp = 0;
                            }
                            break;
                        case ELDER_GUARDIAN:
                        case GIANT:
                            gainedExp = 90;
                            break;
                        case WITHER:
                        case WARDEN:
                        case ENDER_DRAGON:
                            gainedExp = 200;
                            break;

                        default:
                            gainedExp = 0;
                            break;
                    }

                    // Give the Cat the experience
                    myCat myCat = MyCat.getCatManager().getCat(damageEvent.getDamager().getUniqueId());
                    plugin.logDebug("Giving " + myCat.getCatName() + " " + gainedExp + " experience!");
                    myCat.giveExperience(gainedExp);
                }
            }
        }
    }
}
