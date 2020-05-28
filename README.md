**Lives plugin v1.4**
=================

This is a repository of my 1.15 Minecraft plugin.


## What does this plugin do? ##
This plugin adds every player lives. When someone looses his last live, his gamemode is changed to spectator until the lives counter is reseted. You loose a live if you die and you can get it by using a certain item.

## Commands ##
You can see all commands in-game by using /lives help

- **/lives reset [n]** - resets lives counter for everyone to n lives (def 3)
- **/lives get [Player]** - tells you how many lives the player has
- **/lives give [n]** - gives you n live items (def 1)
- **/lives [start | stop]** - stops/starts lives counting
- **/lives status** - tells the status of lives counting
- **/lives reset_config** - resets config to default values
- **/lives [save | load]** - saves/loads lives to/from file

## Config file ##
Structure of config file:

    version: '1.4'
    autoSave: true
    autoLoad: true
    resetLives: 3
    onJoinLives: 3
    defStarted: true
    itemName: Live

- **autoSave** - plugin saves lives automatically to file
- **autoLoad** - plugin loads lives automatically from file on enable
- **resetLives** - number of lives to give on reset by default
- **onJoinLives** - number of lives player gets when joining for the first time
- **defStarted** - if lives counting should be on or off by default
- **itemName** - name of live item
