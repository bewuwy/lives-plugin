**Lives plugin**
=================

This is a repository of my 1.15.2 Minecraft plugin.


## What does this plugin do? ##
This plugin adds every player lives. When someone looses his last live, his gamemode is changed to spectator until the lives counter is reset. You loose a live if you die and you can get it by using a certain item.

## Commands ##
You can see all commands in-game by using /lives help admin

**Default commands:**
- **/lives (Player)** (Alias: /l) - tells you how many lives you (or specified player) have
- **/lives get [Player]** - tells you how many lives the player has
- **/lives extract (n)** (Alias: /l ex) - extracts n of your lives to an item (def 1)
- **/lives revive [Player]** (Alias: /l rev) - revives a player for some lives
- **/lives status** - tells if lives counting is on or off
- **/lives help (admin)** - shows list of commands in-game (def without admin commands)


**Administrator commands:**
- **/lives reset (n) (revive)** - resets lives counter for everyone to n lives (def 3), if run with revive options, revives everyone
- **/lives give (Player) (n)** - gives you (or specified player) n live items (def 1)
- **/lives set [Player] [n]** - sets players lives to n
- **/lives add [Player] [n]** - adds player n lives
- **/lives remove [Player] [n]** - removes player n lives
- **/lives addeveryone [n]** (Alias: /l addev) - adds everyone n lives
- **/lives admin_revive [Player]** (Alias: /l arev) - revives a player "for free"
- **/lives [start | stop]** - stops/starts lives counting
- **/lives [save | load]** - saves/loads lives to/from file
- **/lives scoreboard [show | hide]** (Alias: /l score) - shows/hides the lives scoreboard
- **/lives reset_config** - resets config to default values


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
  - **lives.config.reset**:
      Allows you to reset config file from command.
      default: op
  - **lives.save**:
      Allows you to save/load lives to/from file.
      default: op
  - **lives.moveItem**:
      Allows you to move item.
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
