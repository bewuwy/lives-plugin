**Lives plugin**
=================

This is a repository of my 1.15.2 Minecraft plugin.


## What does this plugin do? ##
This plugin adds every player lives. When someone looses his last live, his gamemode is changed to spectator until the lives counter is reset. You loose a live if you die and you can get it by using a certain item.

## Commands ##
You can see all commands in-game by using /lives help admin

**Default commands:**
- **/lives** (Alias: /l) - tells you how many lives you have
- **/lives get [Player]** - tells you how many lives the player has
- **/lives extract (n)** (Alias: /l ex) - extracts n of your lives to an item (def 1)
- **/lives status** - tells if lives counting is on or off
- **/lives help (admin)** - shows list of commands in-game (def without admin commands)


**Administrator commands:**
- **/lives reset (n)** - resets lives counter for everyone to n lives (def 3)
- **/lives give (n)** - gives you n live items (def 1)
- **/lives set [Player] [n]** - sets players lives to n
- **/lives add [Player] [n]** - adds player n lives
- **/lives addeveryone [n]** (Alias: /l addev) - adds everyone n lives
- **/lives [start | stop]** - stops/starts lives counting
- **/lives [save | load]** - saves/loads lives to/from file
- **/lives scoreboard [show | hide]** (Alias: /l score) - shows/hides the lives scoreboard
- **/lives reset_config** - resets config to default values

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
      saveInterval: 30      #How often autosave can run (in seconds!)
      autoLoad: true        #If lives should be loaded from file on plugin start

    scoreboard:
      defShown: false       #If scoreboard should be shown when plugins starts
      type: TAB             #Type of scoreboard (supported: TAB, SIDE, UNDER_NAME)
      name: Lives           #Scoreboard name, only shown when scoreboard type is set to SIDE

    penalty:
      type: GM3             #Penalty for loosing the last life (GM3, BAN, TEMPBAN)
      tempbanTime: 60       #Tempban time (in minutes!)
      banMessage: You lost your last life!


    #If you change this variable, your config will be cleared!
    configVersion: 2.2

## Permissions ##
Plugins Permissions:


  - **lives.get**:
    Allows you to see other peoples lives.
    Default: everyone
  - **lives.set**:
    Allows you to reset, set and add players lives.
    Default: op
  - **lives.give**:
    Allows you to give you the live item.
    Default: op
  - **lives.control**:
    Allows you to turn counting lives on and off.
    Default: op
  - **lives.save**:
    Allows you to save/load lives to/from file.
    Default: op
  - **lives.move**:
    Allows you to move the live item.
    Default: op
  - **lives.scoreboard**:
    Allows you to show/hide scoreboard.
    Default: op
  - **lives.config.reset**:
    Allows you to reset config file from command.
    Default: op
