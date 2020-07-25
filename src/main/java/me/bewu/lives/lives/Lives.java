package me.bewu.lives.lives;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
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

        //bstats
        int pluginId = 8287;
        MetricsLite metrics = new MetricsLite(this, pluginId);

        lastSave = System.currentTimeMillis();

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

    @Override
    public void onDisable() {
        autoSave();
    }

    Map<UUID, Integer> playersLives = new HashMap<>();
    boolean started = getConfig().getConfigurationSection("generalOptions").getBoolean("defStarted");
    private File customConfigFile;
    private FileConfiguration customConfig;
    boolean scoreboardShown = getConfig().getConfigurationSection("scoreboard").getBoolean("defShown");
    long lastSave;


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (command.getName().equalsIgnoreCase("lives") || command.getName().equalsIgnoreCase("l")) {
                //command /lives
                if (args.length == 0) {
                    if(sender instanceof Player) {

                        sender.sendMessage(ChatColor.GREEN + "You have got: " + playersLives.get(((Player) sender).getUniqueId()) + " live/s.");

                    } else {
                        sender.sendMessage("You can't use that command from the console!");
                    }

                }
                //command /lives reset
                else if (args[0].equalsIgnoreCase("reset")) {
                    if (sender.hasPermission("lives.set")) {
                        for (Player i : Bukkit.getOnlinePlayers()) {
                            if (i.getGameMode() == GameMode.SPECTATOR) {
                                if (i.getBedSpawnLocation() != null) {
                                    i.teleport(i.getBedSpawnLocation());
                                }
                                i.setGameMode(GameMode.SURVIVAL);
                            }
                            if (args.length > 1) {
                                if (StringUtils.isNumeric(args[1])) {
                                    playersLives.put(i.getUniqueId(), Integer.valueOf(args[1]));
                                    sender.sendMessage(ChatColor.GREEN + "Reset " + i.getName() + " lives to " + args[1]);
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives reset [number]");
                                }
                            } else {
                                playersLives.put(i.getUniqueId(), getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                                sender.sendMessage(ChatColor.GREEN + "Reset" + i.getName() + "lives to " + getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                            }
                        }

                        autoSave();
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /lives get
                else if (args[0].equalsIgnoreCase("get")) {
                    if(args.length > 1) {
                        if (sender.hasPermission("lives.get")) {
                            if (getServer().getPlayer(args[1]) != null) {
                                UUID id = getServer().getPlayer(args[1]).getUniqueId();
                                if (playersLives.containsKey(id)) {
                                    sender.sendMessage(args[1] + " has got " + playersLives.get(id) + " live/s.");
                                }
                                else {
                                    sender.sendMessage(ChatColor.RED + "This player is not in the database. If this player is online and you see this error, use /lives reset");
                                }
                            }
                            else {
                                sender.sendMessage(ChatColor.RED + "Player offline!");
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
                            " - /lives revive [Player] (Alias: /l rev) - revives a player for some lives\n" +
                            " - /lives status - tells if lives counting is on or off\n" +
                            " - /lives help (admin) - shows list of commands in-game (def without admin commands)");

                    if(args.length > 1 && args[1].equalsIgnoreCase("admin")) {
                        sender.sendMessage(ChatColor.YELLOW + "\n Administrator commands:\n" + ChatColor.RESET +
                                " - /lives reset (n) - resets lives counter for everyone to n lives (def 3)\n" +
                                " - /lives give (n) - gives you n live items (def 1)\n" +
                                " - /lives set [Player] [n] - sets players lives to n\n" +
                                " - /lives add [Player] [n] - adds player n lives\n" +
                                " - /lives addeveryone [n] (Alias: /l addev) - adds everyone n lives\n" +
                                " - /lives admin_revive [Player] (Alias: /l arev) - revives a player \"for free\"\n" +
                                " - /lives [start | stop] - stops/starts lives counting\n" +
                                " - /lives [save | load] - saves/loads lives to/from file\n" +
                                " - /lives scoreboard [show | hide] (Alias: /l score) - shows/hides the lives scoreboard\n" +
                                " - /lives reset_config - resets config to default values");
                    }
                }
                //command /lives extract Alias: /l ex
                else if(args[0].equalsIgnoreCase("extract") || args[0].equalsIgnoreCase("ex")) {
                    if(sender instanceof Player) {
                        UUID id = ((Player) sender).getUniqueId();
                        if ((!getConfig().getConfigurationSection("generalOptions").getBoolean("alwaysExtract") && started) || getConfig().getConfigurationSection("generalOptions").getBoolean("alwaysExtract")) {

                            if(args.length == 1) {
                                if (playersLives.get(id) > 1) {
                                    playersLives.put(id, playersLives.get(id) - 1);

                                    giveItem((Player) sender, 1);
                                    sender.sendMessage(ChatColor.GREEN + "You have extracted one of your lives! You now have " + playersLives.get(id) + " live/s.");

                                    autoSave();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "You can't extract lives when you have only one life.");
                                }
                            } else if(StringUtils.isNumeric(args[1])) {
                                if (playersLives.get(id) > Integer.parseInt(args[1])) {
                                    playersLives.put(id, playersLives.get(id) - Integer.parseInt(args[1]));

                                    giveItem((Player) sender, Integer.parseInt(args[1]));
                                    sender.sendMessage(ChatColor.GREEN + "You have extracted " + args[1] +  " lives! You now have " + playersLives.get(id) + " live/s.");

                                    autoSave();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "You don't have enough lives!");
                                }
                            }
                            else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Second argument must be a number!");
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
                    if (sender.hasPermission("lives.scoreboard")) {

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
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /l set [Player] [n]
                else if(args[0].equalsIgnoreCase("set")) {
                    if(sender.hasPermission("lives.set")) {
                        if(args.length > 2) {
                            if(StringUtils.isNumeric(args[2])) {
                                if(getServer().getPlayer(args[1]) != null) {
                                    playersLives.put(getServer().getPlayer(args[1]).getUniqueId(), Integer.valueOf(args[2]));
                                    sender.sendMessage(ChatColor.GREEN + "Set " + args[1] + " lives to " + args[2] + '!');

                                    autoSave();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Player offline!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Third argument must be a number!");
                            }
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Use: /lives help to see usage");
                        }
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /l add [Player] [n]
                else if(args[0].equalsIgnoreCase("add")) {
                    if(sender.hasPermission("lives.set")) {
                        if(args.length > 2) {
                            if(StringUtils.isNumeric(args[2])) {
                                if(getServer().getPlayer(args[1]) != null) {
                                    UUID id = getServer().getPlayer(args[1]).getUniqueId();
                                    playersLives.put(id, playersLives.get(id) + Integer.parseInt(args[2]));
                                    sender.sendMessage(ChatColor.GREEN + "Added " + args[1] + " " + args[2] + " live/s!");

                                    autoSave();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Player offline!");
                                }
                            }
                            else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Third argument must be a number!");
                            }
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Use: /lives help to see usage");
                        }
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                //command /l addeveryone [n] (/l addev [n])
                else if(args[0].equalsIgnoreCase("addeveryone") || args[0].equalsIgnoreCase("addev")) {
                    if(sender.hasPermission("lives.set")) {
                        if(args.length > 1) {
                            if(StringUtils.isNumeric(args[2])) {
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    playersLives.put(online.getUniqueId(), Integer.valueOf(args[1]));
                                    sender.sendMessage(ChatColor.GREEN + "Set everyone's lives to " + args[2] + '!');

                                    autoSave();
                                }
                            }
                            else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Second argument must be a number!");
                            }
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Use: /lives help to see usage");
                        }
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }
                // /lives revive [Player] (/l rev) and /l admin_revive (/l arev)
                else if (args[0].equalsIgnoreCase("revive") || args[0].equalsIgnoreCase("rev")  || args[0].equalsIgnoreCase("admin_revive") || args[0].equalsIgnoreCase("arev")) {
                    Boolean revive = getConfig().getConfigurationSection("reviving").getBoolean("allowed");
                    Boolean admin = false;
                    if (args[0].equalsIgnoreCase("admin_revive") || args[0].equalsIgnoreCase("arev") || !(sender instanceof Player)) {
                        if (sender.hasPermission("lives.revive.admin")) {
                            admin = true;
                        }
                    }
                    if (revive || sender.hasPermission("lives.revive")) {
                        if (args.length > 1) {
                            autoSave();
                            if (!sender.getName().equalsIgnoreCase(args[1]) || admin) {

                                Integer cost = getConfig().getConfigurationSection("reviving").getInt("cost");
                                if (admin || playersLives.get(((Player) sender).getUniqueId()) > cost) {
                                    String pen = getConfig().getConfigurationSection("penalty").getString("type");

                                    if (pen.equalsIgnoreCase("BAN") || pen.equalsIgnoreCase("TEMPBAN")) {
                                        if (getServer().getBanList(BanList.Type.NAME).getBanEntry(args[1]) != null) {

                                            if (getServer().getBanList(BanList.Type.NAME).getBanEntry(args[1]).getReason().equalsIgnoreCase(getConfig().getConfigurationSection("penalty").getString("banMessage"))) {
                                                if (!admin) {
                                                    playersLives.put(((Player) sender).getUniqueId(), playersLives.get(((Player) sender).getUniqueId()) - cost);
                                                    sender.sendMessage(ChatColor.GREEN + "Revived " + args[1] + " and took " + cost + " of your lives!");
                                                } else {
                                                    sender.sendMessage(ChatColor.GREEN + "Revived " + args[1] + "!");
                                                }

                                                getServer().getBanList(BanList.Type.NAME).pardon(args[1]);
                                            }
                                            else {
                                                sender.sendMessage(ChatColor.RED + "This player wasn't banned for loosing his last life! If you believe this is a mistake, please contact the server administrators.");
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "This player wasn't banned for losing his last life!");
                                        }
                                    } else if (pen.equalsIgnoreCase("GM3")) {
                                        if (getServer().getPlayer(args[1]) != null) {
                                            getServer().getPlayer(args[1]).setGameMode(GameMode.SURVIVAL);

                                            if(!admin) {
                                                playersLives.put(((Player) sender).getUniqueId(), playersLives.get(((Player) sender).getUniqueId()) - cost);
                                                sender.sendMessage(ChatColor.GREEN + "Revived " + args[1] + " and took " + cost + " of your lives!");
                                            }
                                            else {
                                                sender.sendMessage(ChatColor.GREEN + "Revived " + args[1] + "!");
                                            }

                                            playersLives.put(getServer().getPlayer(args[1]).getUniqueId(), getConfig().getConfigurationSection("reviving").getInt("lives"));
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "This player is not online!");
                                        }
                                    }
                                        autoSave();
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "You don't have enough lives to revive a player! (" + (cost + 1) + " required)");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "You can't revive yourself!");
                                }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! See /lives help for usage.");
                        }
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to use that command!");
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
            playersLives.put(player.getUniqueId(), playersLives.get(player.getUniqueId()) - 1);
            player.sendMessage(ChatColor.RED + "You lost one life. You now have " + playersLives.get(player.getUniqueId()) + " live/s.");

            autoSave();

            if (playersLives.get(player.getUniqueId()) < 1) {
                getServer().broadcastMessage(ChatColor.DARK_RED + player.getName() + " lost his last life.");

                if(getConfig().getConfigurationSection("penalty").getString("type").equalsIgnoreCase("GM3")) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
                else if(getConfig().getConfigurationSection("penalty").getString("type").equalsIgnoreCase("BAN")) {
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), getConfig().getConfigurationSection("penalty").getString("banMessage"), null, null);
                    player.kickPlayer(getConfig().getConfigurationSection("penalty").getString("banMessage"));
                }
                else if(getConfig().getConfigurationSection("penalty").getString("type").equalsIgnoreCase("TEMPBAN")) {
                    Date date = new Date(System.currentTimeMillis()+ getConfig().getConfigurationSection("penalty").getInt("tempbanTime") *60*1000);
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), getConfig().getConfigurationSection("penalty").getString("banMessage"), date, null);
                    player.kickPlayer(getConfig().getConfigurationSection("penalty").getString("banMessage"));
                }
            }
        }
    }

    @EventHandler
    public void add_life(PlayerInteractEvent e) {
        //using the life item
        Player player = e.getPlayer();
        if(e.hasItem()) {
            if (Objects.requireNonNull(e.getItem()).getType() == Material.GHAST_TEAR && e.getItem().containsEnchantment(Enchantment.DURABILITY)) {

                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                playersLives.put(player.getUniqueId(), playersLives.get(player.getUniqueId()) + 1);
                player.sendMessage(ChatColor.GREEN + "Added a life. You now have " + playersLives.get(player.getUniqueId()) + " live/s.");

                autoSave();
            }
        }
    }

    @EventHandler
    public void newPlayer(PlayerJoinEvent e) {
        //adding player if not in the database
        if(!playersLives.containsKey(e.getPlayer().getUniqueId())) {
            playersLives.put(e.getPlayer().getUniqueId(), getConfig().getConfigurationSection("generalOptions").getInt("onJoinLives"));
            autoSave();
        }
        else if(playersLives.get(e.getPlayer().getUniqueId()) < 1) {
            if(getConfig().getConfigurationSection("penalty").getString("type").equalsIgnoreCase("GM3")) {
                e.getPlayer().setGameMode(GameMode.SPECTATOR);
            }
            else {
                e.getPlayer().sendMessage(ChatColor.GREEN + "You have been revived!");
                playersLives.put(e.getPlayer().getUniqueId(), getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));

                if(getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
                    saveLives();
                }
                updateScores();
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

                    playersLives.put(player.getUniqueId(), playersLives.get(player.getUniqueId()) + e.getCurrentItem().getAmount());
                    e.getCurrentItem().setAmount(0);
                    player.sendMessage(ChatColor.GREEN + "Added a live/s. You now have " + playersLives.get(player.getUniqueId()) + " live/s.");

                    autoSave();
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
            customConfig.set(String.valueOf(me.getKey()), me.getValue());
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
            try {
                playersLives.put(UUID.fromString(i), customConfig.getInt(i));
            } catch (IllegalArgumentException ignored) {
                getLogger().warning("Check lives.yml file, one of the entries is not an UUID and may cause plugin to 'crash'");
            }
        }
        getLogger().info("Loaded lives from file");
        updateScores();
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

            objective.getScore(online.getName()).setScore(playersLives.get(online.getUniqueId()));
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

    public void autoSave() {
        if (getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
            if((System.currentTimeMillis() - lastSave) > (getConfig().getConfigurationSection("livesManagement").getInt("saveInterval") * 1000)) {
                saveLives();
                lastSave = System.currentTimeMillis();
            }
        }

        updateScores();
    }

    public void revive(Player player, Boolean admin) {

    }
}
