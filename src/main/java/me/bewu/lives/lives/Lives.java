package me.bewu.lives.lives;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public final class Lives extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic

        //ad
        getLogger().info("-------------------------------------------------");
        getLogger().info("This plugin was made by bewu.");
        getLogger().info("Please consider donating on https://ko-fi.com/bewuwy");
        getLogger().info("-------------------------------------------------");

        //Creates lives.yml if not existing
        createCustomConfig();

        if(getConfig().getConfigurationSection("livesManagement").getBoolean("autoLoad")) {
            loadLives();
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("config.yml found, loading!");

                //Checking config version
                String oldVer = getConfig().getString("configVersion");

                InputStream customClassStream= getClass().getResourceAsStream("/config.yml");
                InputStreamReader strR = new InputStreamReader(customClassStream);
                FileConfiguration defaults = YamlConfiguration.loadConfiguration(strR);

                if(!oldVer.equals(defaults.getString("configVersion"))) {

                    File oldConf = new File(getDataFolder(), "old_config_v" + oldVer +".yml");
                    file.renameTo(oldConf);
                    saveDefaultConfig();

                    getLogger().info("-------------------------------------------------");
                    getLogger().info("Config.yml outdated (v" + oldVer + ")");
                    getLogger().info("Updated it to the latest version and reset values.");
                    getLogger().warning("You can see the old config in file old_config_v" + oldVer + ".yml !");
                    getLogger().info("-------------------------------------------------");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    Map<String, Integer> playersLives = new HashMap<>();
    boolean started = getConfig().getConfigurationSection("generalOptions").getBoolean("defStarted");
    private File customConfigFile;
    private FileConfiguration customConfig;
    boolean scoreboardShown = getConfig().getConfigurationSection("scoreboard").getBoolean("defShown");


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (command.getName().equalsIgnoreCase("lives") || command.getName().equalsIgnoreCase("l")) {
                //command /lives
                if (args.length == 0) {
                    if(sender instanceof Player) {

                        sender.sendMessage(ChatColor.GREEN + "You have got: " + String.valueOf(playersLives.get(sender.getName())) + " live/s.");

                    } else {
                        sender.sendMessage("You can't use that command from the console!");
                    }

                }
                //command /lives reset
                else if (args[0].equalsIgnoreCase("reset")) {
                    if (sender.hasPermission("lives.reset")) {

                        List<String> players = new ArrayList<String>();
                        for (Player i : Bukkit.getOnlinePlayers()) {
                            players.add(i.getName());
                            if (i.getGameMode() == GameMode.SPECTATOR) {
                                if (i.getBedSpawnLocation() != null) {
                                    i.teleport(i.getBedSpawnLocation());
                                }
                                i.setGameMode(GameMode.SURVIVAL);
                            }
                        }
                        for (String i : players) {
                            if (args.length > 1) {
                                if (StringUtils.isNumeric(args[1])) {
                                    playersLives.put(i, Integer.valueOf(args[1]));
                                    sender.sendMessage(ChatColor.GREEN + "Reset " + i + " lives to " + args[1]);
                                    if(getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
                                        saveLives();
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives reset [number]");
                                }
                            } else {
                                playersLives.put(i, getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                                sender.sendMessage(ChatColor.GREEN + "Reset lives to " + getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                                if(getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
                                    saveLives();
                                }
                            }
                        }

                        updateScores();
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /lives get
                else if (args[0].equalsIgnoreCase("get")) {
                    if(args.length > 1) {
                        if (sender.hasPermission("lives.get")) {

                            if (playersLives.containsKey(args[1])) {
                                sender.sendMessage(args[1] + " has got " + String.valueOf(playersLives.get(args[1])) + " live/s.");
                            } else {
                                sender.sendMessage(ChatColor.RED + "This player is not in the database. If this player is online and you see this error, use /lives reset");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        }
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "Too few arguments! Use: /lives help to see usage");
                    }
                }
                //command /lives give
                else if (args[0].equalsIgnoreCase("give")) {
                    if(sender instanceof Player) {
                        if (sender.hasPermission("lives.give")) {

                            if (args.length > 1) {
                                if (StringUtils.isNumeric(args[1])) {
                                    giveItem((Player) sender, Integer.parseInt(args[1]));
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives give [number]");
                                }
                            } else {
                                giveItem((Player) sender, 1);
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        }
                    } else {
                        if(args.length > 1) {
                            if(getServer().getPlayerExact(args[1]) != null) {
                                if (args.length > 2) {
                                    if(StringUtils.isNumeric(args[2])) {
                                        giveItem(getServer().getPlayerExact(args[1]), Integer.parseInt(args[2]));
                                        sender.sendMessage("Gave 1 live item to " + args[1]);
                                    } else {
                                        sender.sendMessage("Invalid syntax! From console use: /lives give [Player] [number]");
                                    }
                                }
                                else {
                                    giveItem(getServer().getPlayerExact(args[1]), 1);
                                    sender.sendMessage("Gave 1 live item to " + args[1]);
                                }
                            } else  {
                                sender.sendMessage("Player must be online!");
                            }
                        }
                        else {
                            sender.sendMessage("Invalid syntax! From console use: /lives give [Player] {number}");
                        }
                    }
                }
                //command /lives stop
                else if (args[0].equalsIgnoreCase("stop")) {
                    if (sender.hasPermission("lives.control")) {

                        sender.sendMessage(ChatColor.RED + "Stopped counting lives.");
                        started = false;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /lives start
                else if (args[0].equalsIgnoreCase("start")) {
                    if (sender.hasPermission("lives.control")) {

                        sender.sendMessage(ChatColor.GREEN + "Started counting lives.");
                        started = true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /lives status
                else if (args[0].equalsIgnoreCase("status")) {
                    if (started) {
                        sender.sendMessage(ChatColor.GREEN + "Lives counting is on.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Lives counting is off.");
                    }
                }
                //command /lives reset_config
                else if (args[0].equalsIgnoreCase("reset_config")) {
                    if (sender.hasPermission("lives.config.reset")) {

                        File file = new File(getDataFolder(), "config.yml");
                        file.delete();
                        saveDefaultConfig();
                        sender.sendMessage(ChatColor.RED + "Reset config to default values.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /lives save
                else if (args[0].equalsIgnoreCase("save")) {
                    if (sender.hasPermission("lives.save")) {

                        saveLives();
                        sender.sendMessage(ChatColor.GREEN + "Successfully saved lives to file!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /lives load
                else if (args[0].equalsIgnoreCase("load")) {
                    if (sender.hasPermission("lives.save")) {

                        loadLives();
                        sender.sendMessage(ChatColor.GREEN + "Successfully loaded lives from file!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /lives help
                else if (args[0].equalsIgnoreCase("help")) {

                    sender.sendMessage(ChatColor.YELLOW + " Default commands:\n" + ChatColor.RESET +
                            " - /lives (Alias: /l) - tells you how many lives you have\n" +
                            " - /lives get [Player] - tells you how many lives the player has\n" +
                            " - /lives extract (n) (Alias: /l ex) - extracts n of your lives to an item (def 1)\n" +
                            " - /lives status - tells if lives counting is on or off\n" +
                            " - /lives help (admin) - shows list of commands in-game (def without admin commands)");

                    if(args.length > 1 && args[1].equalsIgnoreCase("admin")) {
                        sender.sendMessage(ChatColor.YELLOW + "\n Administrator commands:\n" + ChatColor.RESET +
                                " - /lives reset (n) - resets lives counter for everyone to n lives (def 3)\n" +
                                " - /lives give (n) - gives you n live items (def 1)\n" +
                                " - /lives [start | stop] - stops/starts lives counting\n" +
                                " - /lives [save | load] - saves/loads lives to/from file\n" +
                                " - /lives scoreboard [show | hide] (Alias: /l score) - shows/hides the lives scoreboard\n" +
                                " - /lives reset_config - resets config to default values");
                    }
                }
                //command /lives extract Alias: /l ex
                else if(args[0].equalsIgnoreCase("extract") || args[0].equalsIgnoreCase("ex")) {
                    if(sender instanceof Player) {

                        if ((!getConfig().getConfigurationSection("generalOptions").getBoolean("alwaysExtract") && started) || getConfig().getConfigurationSection("generalOptions").getBoolean("alwaysExtract")) {

                            if(args.length == 1) {
                                if (playersLives.get(sender.getName()) > 1) {
                                    playersLives.put(sender.getName(), playersLives.get(sender.getName()) - 1);

                                    giveItem((Player) sender, 1);
                                    sender.sendMessage(ChatColor.GREEN + "You have extracted one of your lives! You now have " + playersLives.get(sender.getName()) + " live/s.");

                                    updateScores();

                                } else {
                                    sender.sendMessage(ChatColor.RED + "You can't extract lives when you have only one life.");
                                }
                            } else if(StringUtils.isNumeric(args[1])){
                                if (playersLives.get(sender.getName()) > Integer.parseInt(args[1])) {
                                    playersLives.put(sender.getName(), playersLives.get(sender.getName()) - Integer.parseInt(args[1]));

                                    giveItem((Player) sender, Integer.parseInt(args[1]));
                                    sender.sendMessage(ChatColor.GREEN + "You have extracted " + args[1] +  " lives! You now have " + playersLives.get(sender.getName()) + " live/s.");

                                    updateScores();

                                } else {
                                    sender.sendMessage(ChatColor.RED + "You don't have enough lives!");
                                }
                            }
                            else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Second argument must be a number!");
                            }
                            if (getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
                                saveLives();
                            }
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "Lives counting must be on to extract lives!");
                        }
                    } else {
                        sender.sendMessage("You can't use that command from the console!");
                    }
                }
                //command /lives version
                else if(args[0].equalsIgnoreCase("version")) {
                    sender.sendMessage("The plugin version is " + getDescription().getVersion());
                }
                //command /l scoreboard
                else if(args[0].equalsIgnoreCase("scoreboard") || args[0].equalsIgnoreCase("score")) {
                    if(sender.hasPermission("lives.scoreboard")) {

                        if (args.length > 1) {

                            if (args[1].equalsIgnoreCase("show")) {
                                sender.sendMessage(ChatColor.GREEN + "Showing the scoreboard");
                                scoreboardShown = true;

                                updateScores();

                            } else if (args[1].equalsIgnoreCase("hide")) {
                                sender.sendMessage(ChatColor.RED + "Hidden the scoreboard");
                                scoreboardShown = false;

                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    online.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                                }

                            } else if (args[1].equalsIgnoreCase("update")) {
                                updateScores();
                            } else {
                                sender.sendMessage(ChatColor.RED + "Wrong argument! Use: /lives help to see usage");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Use: /lives help to see usage");
                        }
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //wrong command
                else {
                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives help to see commands");
                }
            }
        return false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        //Someone died - removing life
        if(started) {

            Player player = e.getEntity();
            playersLives.put(player.getName(), playersLives.get(player.getName()) - 1);
            player.sendMessage(ChatColor.RED + "You lost one life. You now have " + playersLives.get(player.getName()) + " live/s.");

            updateScores();

            if(getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
                saveLives();
            }
            if (playersLives.get(player.getName()) < 1) {
                getServer().broadcastMessage(ChatColor.DARK_RED + player.getName() + " lost his last life.");
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
    }

    @EventHandler
    public void add_life(PlayerInteractEvent e) {
        //using the emerald life item
        Player player = e.getPlayer();
        if(e.hasItem()) {
            if (Objects.requireNonNull(e.getItem()).getType() == Material.GHAST_TEAR && e.getItem().containsEnchantment(Enchantment.DURABILITY)) {

                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                playersLives.put(player.getName(), playersLives.get(player.getName()) + 1);
                player.sendMessage(ChatColor.GREEN + "Added a life. You now have " + playersLives.get(player.getName()) + " live/s.");

                updateScores();

                //auto-save
                if(getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
                    saveLives();
                }
            }
        }
    }

    @EventHandler
    public void newPlayer(PlayerJoinEvent e) {
        //adding player if not in the database
        if(!playersLives.containsKey(e.getPlayer().getName())) {
            playersLives.put(e.getPlayer().getName(), getConfig().getConfigurationSection("generalOptions").getInt("onJoinLives"));
            if(getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
                saveLives();
            }
        }

        if(scoreboardShown) {
            updateScores();
        }
    }

    @EventHandler
    public void moveBlock(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if(e.getCurrentItem() != null) {
            if (e.getCurrentItem().getType().equals(Material.GHAST_TEAR) && e.getCurrentItem().containsEnchantment(Enchantment.DURABILITY)) {
                if (!player.hasPermission("lives.moveItem")) {
                    e.setCancelled(true);

                    playersLives.put(player.getName(), playersLives.get(player.getName()) + e.getCurrentItem().getAmount());
                    e.getCurrentItem().setAmount(0);
                    player.sendMessage(ChatColor.GREEN + "Added a live/s. You now have " + playersLives.get(player.getName()) + " live/s.");

                    updateScores();

                    //auto-save
                    if (getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
                        saveLives();
                    }
                }
            }
        }
    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "lives.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("lives.yml", false);
        }

        customConfig= new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveLives() {
        //saving lives to file
        for(Map.Entry me : playersLives.entrySet()) {
            customConfig.set((String) me.getKey(), me.getValue());
        }

        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLogger().info("Saved lives to file");
    }

    public void loadLives() {
        //loading lives from file
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        for(String i : customConfig.getKeys(false)) {
            playersLives.put(i, customConfig.getInt(i));
        }
        getLogger().info("Loaded lives from file");
    }

    public void giveItem(Player player, int amount) {
        //just to make code shorter - give item method
        ItemStack life = new ItemStack(Material.GHAST_TEAR);
        life.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta lifeMeta = life.getItemMeta();
        lifeMeta.setDisplayName(getConfig().getConfigurationSection("generalOptions").getString("itemName"));
        life.setItemMeta(lifeMeta);
        life.setAmount(amount);
        player.getInventory().addItem(life);
    }

    public void updateScores() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard livesBoard = manager.getNewScoreboard();
        Objective objective = livesBoard.registerNewObjective("lives", "dummy", ChatColor.RED + getConfig().getConfigurationSection("scoreboard").getString("name"));

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.setScoreboard(livesBoard);

            objective.getScore(online.getName()).setScore(playersLives.get(online.getName()));
        }

        if(getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("TAB")) {
            objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }
        else if(getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("SIDE")) {
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        else if(getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("UNDER_NAME")) {
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
        else {
            getLogger().warning("The option \"type\" in config file under section \"scoreboard\" was set to an incorrect value! Using TAB by default!");
            objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }
    }
}
