**Lives plugin**
=================

This is a repository of my 1.15.2 Minecraft plugin.


## What does this plugin do? ##
This plugin adds every player lives. When someone looses his last live, his gamemode is changed to spectator until the lives counter is reset. You loose a live if you die and you can get it by using a certain item.

## Commands ##

**Default commands:**
 - **/lives (Player)** (Alias: /l) - tells you how many lives you (or specified player) have
 - **/lives get [Player]** - tells you how many lives the player has
 - **/lives extract (n)** (Alias: /l ex) - extracts n of your lives to an item (def 1)
 - **/lives revive [Player]** (Alias: /l rev) - revives a player for some lives
 - **/lives status** - tells if lives counting is on or off
 - **/lives help** - shows this list


**Administrator commands:**
- **/la reset [n] (revive)** - resets lives counter for everyone to n lives, if run with revive option, revives everyone
- **/la give [Player] (n)** - gives you (or specified player) n live items (def 1)
- **/la set [Player] [n]** - sets players lives to n
- **/la add [Player] [n]** - adds player n lives
- **/la remove [Player] [n]** - removes n lives from player
- **/la addeveryone [n]** (Alias: /la addev) - adds everyone n lives
- **/la admin_revive [Player]** (Alias: /la arev) - revives a player "for free"
- **/la [start | stop]** - stops/starts lives counting
- **/la [save | load]** - saves/loads lives to/from file
- **/la scoreboard [show | hide | update]** (Alias: /la score) - shows/hides/updates the lives scoreboard
- **/la help** - shows this list


## Permissions ##
Plugins Permissions:

  - **lives.***
      Gives you all plugin permissions
      default: op
  - **lives.set**:
      Allows you to reset, set and add players lives.
      default: op
  - **lives.get**:
      description: Allows you to see other peoples lives.
      default: everyone
  - **lives.give**:
      Allows you to give you the live item.
      default: op
  - **lives.control**:
      Allows you to turn counting lives on and off.
      default: op
  - **lives.save**:
      Allows you to save/load lives to/from file.
      default: op
  - **lives.moveItem**:
      Allows you to move the life item in inventory.
      default: op
  - **lives.scoreboard**:
      Allows you to show/hide scoreboard
      default: op
  - **lives.revive**:
      Allows you to revive people even if it's turned off.
      default: op
  - **lives.revive.admin**:
      Allows you to revive people "for free"
      default: op
