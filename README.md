**Lives plugin v1.5.1**
=================

This is a repository of my 1.15 Minecraft plugin.


## What does this plugin do? ##
This plugin adds every player lives. When someone looses his last live, his gamemode is changed to spectator until the lives counter is reset. You loose a live if you die and you can get it by using a certain item.

## Commands ##
You can see all commands in-game by using /lives help

**Default commands:**
- **/lives** - tells you how many lives you have
- **/lives extract** - extracts one of your lives to an item
- **/lives status** - tells the status of lives counting
- **/lives get [Player]** - tells you how many lives the player has

**Administrator commands:**
- **/lives reset [n]** - resets lives counter for everyone to n lives (def 3)
- **/lives give [n]** - gives you n live items (def 1)
- **/lives [start | stop]** - stops/starts lives counting
- **/lives reset_config** - resets config to default values
- **/lives [save | load]** - saves/loads lives to/from file

## Config file ##
Structure of config file:

    version: 1.5.2
    autoSave: true
    autoLoad: true
    resetLives: 3
    onJoinLives: 3
    defStarted: true
	alwaysExtract: false
    itemName: Live

- **autoSave** - plugin saves lives automatically to file
- **autoLoad** - plugin loads lives automatically from file on enable
- **resetLives** - number of lives to give on reset by default
- **onJoinLives** - number of lives player gets when joining for the first time
- **defStarted** - if lives counting should be on or off by default
- **alwaysExtract** - if you can always extract lives, if this is off you only can extract them when lives counting is on
- **itemName** - name of live item

## Permissions ##
Plugins Permissions:

  - **lives.reset**:
    Allows you to reset the lives counter
    Default: op
  - **lives.get**:
    Allows you to see other peoples lives.
    Default: everyone
  - **lives.give**:
    Allows you to give you the live item.
    Default: op
  - **lives.control**:
    Allows you to turn counting lives on and off.
    Default: op
  -**lives.config.reset**:
    Allows you to reset config file from command.
    Default: op
  -**lives.save**:
    Allows you to save/load lives to/from file.
    Default: op
