**Lives plugin**
=================

This is a repository of my 1.15.2 Minecraft plugin.


## What does this plugin do? ##
This plugin adds every player lives. When someone looses his last live, his gamemode is changed to spectator until the lives counter is reset. You loose a live if you die and you can get it by using a certain item.

## Commands ##
You can see all commands in-game by using /lives help

**Default commands:**
- **/lives** - tells you how many lives you have
- **/lives extract [n]** - extracts one of your lives to an item (Alias: /lives ex [n])
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

    #Lives plugin config file (version 2)

    generalOptions:
      itemName: Live        #Changes the name of the live item
      resetLives: 3         #Changes the default amount of lives for /l reset
      onJoinLives: 3        #Changes how much lives a player has when joining for the first time
      defStarted: true      #If lives counting should be on when plugins starts
      alwaysExtract: false  #If player can extract lives when lives counting is off

    livesManagement:
      autoSave: true        #If lives should be saved automatically to file
      autoLoad: true        #If lives should be loaded from file on plugin start



    #If you change this variable, your config will be cleared!
    configVersion: 2

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
  -**lives.move**:
    Allows you to move the live item.
    Default: op
