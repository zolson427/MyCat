<h1 align="center">MyCat</h1>
<div align="center">

Tamed myCats.

___

## Main Plugin Features
*	Cats teleport upon chunk-unloading, on player-teleport as well as if the player is more than 200 blocks away from their myCat, so that the Cats can always follow their owner. Requires the Cat to not sit (all other Tameables can also be set to teleport on chunk-unload and player-teleportation (not distance) in the config).
*	500+ unique randomly generated myCat names.
*	Cats can gain experience and level up by killing mobs.
*	Configurable option for gaining XP from killing players.
*	Cats get more health and deal more damage the higher their level is.
*	A beautiful overview over your Cat's stats, including XP, health, damage and last known location.
*	Update the color of your Cat's nametag when their collar gets coloured.
*	Puppies, from breeding two Cats, become Cats in MyCat as well.
*	Nametag and name updates when the Cats collar gets coloured, or the Cat gets a nametag applied (deny the rename permission for nametag-only).
*	The ability to add currently existing Tamed Wolves to the MyCat system, and register them as new Cats (interact with them).
*	Configurable all the way from the sound on LevelUp to the messages displayed (excluding error messages for now...)

## Permissions
```YAML
mycat.*:
    description: Gives access to all MyCat commands
    default: false
mycat.limit.*:
  description: Give limits to the amount of myCats a player can tame.
  default: false
mycat.teleport:
  description: Player's tamed wolves will teleport to the player
  default: true
mycat.help:
  description: Player can view the MyCat command list
  default: true
mycat.putdown:
  description: Player can kill their Cat with a command
  default: true
mycat.comehere:
  description: Player can force their Cats to load and teleport to the position of the player
  default: true
mycat.myCats:
  description: Player can get an overview about their Cats
  default: true
mycat.stats:
  description: Player can view stats about their Cats
  default: true
mycat.rename:
  description: Player can rename their Cats
  default: true
mycat.sit:
  description: Player can have myCats sit and stand up
  default: true
mycat.setid:
  description: Player can set a custom ID to their Cats
  default: true
mycat.togglemode:
  description: Player can toggle myCats between attack and defend mode
  default: true
mycat.pet:
  description: Player can pet their myCats
  default: true
mycat.pet.others:
  description: Player can pet others myCats
  default: true
mycat.editlevel:
  description: Player can set the level of their myCats
  default: false
mycat.dead:
  description: Player can see the list of dead myCats
  default: true
mycat.revive:
  description: Player can revive dead myCats
  default: true
mycat.free:
  description: Player can set their Cats free
  default: true
mycat.trade:
  description: Players can trade their myCats
  default: true
mycat.reload:
  description: Player can reload the configuration(s)
  default: false
mycat.save:
  description: Player can save the configuration(s)
  default: false
```