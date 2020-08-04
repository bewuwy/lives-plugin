package me.bewu.lives;

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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public final class Main extends JavaPlugin implements Listener {

    boolean started = getConfig().getConfigurationSection("generalOptions").getBoolean("defStarted");
    boolean scoreboardShown = getConfig().getConfigurationSection("scoreboard").getBoolean("defShown");
    Map<UUID, Integer> playersLives = new HashMap<>();
    private File customConfigFile;
    private FileConfiguration customConfig;
    long lastSave;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);

        //bstats
        int pluginId = 8287;
        MetricsLite metrics = new MetricsLite(this, pluginId);

        //ad
        getLogger().info("-------------------------------------------------");
        getLogger().info("Lives plugin was made by bewu.");
        getLogger().info("Please consider donating on https://ko-fi.com/bewuwy");
        getLogger().info("-------------------------------------------------");

        lastSave = System.currentTimeMillis();

        //Creates lives.yml if not existing
        createCustomConfig();

        if(getConfig().getConfigurationSection("livesManagement").getBoolean("autoLoad")) {
            loadLives();
        }

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

                    getLogger().warning("-------------------------------------------------");
                    getLogger().warning("[Lives]: Config.yml outdated (v" + oldVer + ")");
                    getLogger().warning("[Lives]: Updated it to the latest version and reset values.");
                    getLogger().warning("[Lives]: You can see the old config in file old_config_v" + oldVer + ".yml !");
                    getLogger().warning("-------------------------------------------------");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() { autoSave(); }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (command.getName().equalsIgnoreCase("lives") || command.getName().equalsIgnoreCase("l")) {
                //command /lives (/l)
                if (args.length == 0) {
                    if(sender instanceof Player) {
                        sender.sendMessage(ChatColor.GREEN + "You have got: " + playersLives.get(((Player) sender).getUniqueId()) + " live/s.");
                    } else {
                        sender.sendMessage("You can't use that command from the console!");
                    }
                }

                //command /lives reset (n) (revive)
                else if (args[0].equalsIgnoreCase("reset")) {
                    if (sender.hasPermission("lives.reset") || sender.hasPermission("lives.*")) {
                        for (Player i : Bukkit.getOnlinePlayers()) {
                            if (args.length > 1) {
                                if (StringUtils.isNumeric(args[1])) {
                                    playersLives.put(i.getUniqueId(), Integer.valueOf(args[1]));
                                    sender.sendMessage(ChatColor.GREEN + "Reset " + i.getName() + " lives to " + args[1]);
                                } else if (args[1].equalsIgnoreCase("revive")) {
                                    playersLives.put(i.getUniqueId(), getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                                    sender.sendMessage(ChatColor.GREEN + "Reset " + i.getName() + " lives to " + getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                                    revivePlayer(i.getName(), true, sender);
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives reset (number) (revive)");
                                }
                            } else {
                                playersLives.put(i.getUniqueId(), getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                                sender.sendMessage(ChatColor.GREEN + "Reset " + i.getName() + " lives to " + getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                            }
                            if (args.length > 2 && args[2].equalsIgnoreCase("revive")) {
                                revivePlayer(i.getName(), true, sender);
                            }
                        }
                        autoSave();
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /lives get [Player]
                else if (args[0].equalsIgnoreCase("get")) {
                    if(args.length > 1) {
                        if (sender.hasPermission("lives.get") || sender.hasPermission("lives.*")) {
                            if (getServer().getPlayer(args[1]) != null) {
                                UUID id = getServer().getPlayer(args[1]).getUniqueId();
                                if (playersLives.containsKey(id)) {
                                    sender.sendMessage(args[1] + " has got " + playersLives.get(id) + " live/s.");
                                } else {
                                    sender.sendMessage(ChatColor.RED + "This player has not joined the server!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Player offline!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Too few arguments! Use: /lives get [Player]");
                    }
                }

                //command /lives give (Player) [n]
                else if (args[0].equalsIgnoreCase("give")) {
                    if (sender instanceof Player) {
                        if (sender.hasPermission("lives.give") || sender.hasPermission("lives.*")) {
                            if (args.length > 1) {
                                if (StringUtils.isNumeric(args[1])) {
                                    giveItem((Player) sender, Integer.parseInt(args[1]));
                                    sender.sendMessage(ChatColor.GREEN + "Gave you " + args[1] + " live item/s");
                                } else if (getServer().getPlayer(args[1]) != null) {
                                    if (args.length > 2) {
                                        if (StringUtils.isNumeric(args[2])) {
                                            giveItem(getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
                                            sender.sendMessage(ChatColor.GREEN + "Gave " + args[1] + " " + args[2] + " live item/s");
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives give (Player) [number]");
                                        }
                                    } else {
                                        giveItem(getServer().getPlayer(args[1]), 1);
                                        sender.sendMessage(ChatColor.GREEN + "Gave " + args[1] + " 1 live item");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives give (Player) [number]");
                                }
                            } else {
                                giveItem((Player) sender, 1);
                                sender.sendMessage(ChatColor.GREEN + "Gave you 1 live item");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        }
                    } else {
                        if(args.length > 1) {
                            if(getServer().getPlayer(args[1]) != null) {
                                if (args.length > 2) {
                                    if (StringUtils.isNumeric(args[2])) {
                                        giveItem(getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
                                        sender.sendMessage("Gave "  + args[1] + " " + args[2] + " live item/s");
                                    } else {
                                        sender.sendMessage("Invalid syntax! From console use: /lives give [Player] [number]");
                                    }
                                } else {
                                    giveItem(getServer().getPlayer(args[1]), 1);
                                    sender.sendMessage("Gave " + args[1] +" 1 live item");
                                }
                            } else  {
                                sender.sendMessage("Player must be online!");
                            }
                        } else {
                            sender.sendMessage("Invalid syntax! From console use: /lives give [Player] (number)");
                        }
                    }
                }

                //command /lives stop (quiet)
                else if (args[0].equalsIgnoreCase("stop")) {
                    if (sender.hasPermission("lives.control") || sender.hasPermission("lives.*")) {
                        if (args.length > 1 && args[1].equalsIgnoreCase("quiet")) {
                            getServer().broadcastMessage(ChatColor.RED + "Stopped counting lives!");
                        }
                        else { sender.sendMessage(ChatColor.RED + "Stopped counting lives!"); }
                        started = false;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /lives start (quiet)
                else if (args[0].equalsIgnoreCase("start")) {
                    if (sender.hasPermission("lives.control") || sender.hasPermission("lives.*")) {
                        if (args.length > 1 && args[1].equalsIgnoreCase("quiet")) {
                            getServer().broadcastMessage(ChatColor.GREEN + "Started counting lives!");
                        }
                        else { sender.sendMessage(ChatColor.GREEN + "Started counting lives!"); }
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
                    if (sender.hasPermission("lives.config.reset") || sender.hasPermission("lives.*")) {
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
                    if (sender.hasPermission("lives.save") || sender.hasPermission("lives.*")) {

                        saveLives();
                        sender.sendMessage(ChatColor.GREEN + "Successfully saved lives to file!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /lives load
                else if (args[0].equalsIgnoreCase("load")) {
                    if (sender.hasPermission("lives.save") || sender.hasPermission("lives.*")) {

                        loadLives();
                        sender.sendMessage(ChatColor.GREEN + "Successfully loaded lives from file!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /lives help (admin)
                else if (args[0].equalsIgnoreCase("help")) {

                    sender.sendMessage(ChatColor.YELLOW + " Default commands:\n" + ChatColor.RESET +
                            " - /lives (Player) (Alias: /l) - tells you how many lives you (or specified player) have\n" +
                            " - /lives get [Player] - tells you how many lives the player has\n" +
                            " - /lives extract (n) (Alias: /l ex) - extracts n of your lives to an item (def 1)\n" +
                            " - /lives revive [Player] (Alias: /l rev) - revives a player for some lives\n" +
                            " - /lives status - tells if lives counting is on or off\n" +
                            " - /lives help (admin) - shows list of commands in-game (def without admin commands)");

                    if(args.length > 1 && args[1].equalsIgnoreCase("admin")) {
                        sender.sendMessage(ChatColor.YELLOW + "\n Administrator commands:\n" + ChatColor.RESET +
                                " - /lives reset (n) (revive) - resets lives counter for everyone to n lives (def 3), if run with revive options, revives everyone\n" +
                                " - /lives give (Player) (n) - gives you (or specified player) n live items (def 1)\n" +
                                " - /lives set [Player] [n] - sets players lives to n\n" +
                                " - /lives add [Player] [n] - adds player n lives\n" +
                                " - /lives remove [Player] [n] - removes player n lives\n" +
                                " - /lives addeveryone [n] (Alias: /l addev) - adds everyone n lives\n" +
                                " - /lives admin_revive [Player] (Alias: /l arev) - revives a player \"for free\"\n" +
                                " - /lives [start | stop] - stops/starts lives counting\n" +
                                " - /lives [save | load] - saves/loads lives to/from file\n" +
                                " - /lives scoreboard [show | hide] (Alias: /l score) - shows/hides the lives scoreboard\n" +
                                " - /lives reset_config - resets config to default values");
                    }
                }

                //command /lives extract (number) Alias: /l ex
                else if(args[0].equalsIgnoreCase("extract") || args[0].equalsIgnoreCase("ex")) {
                    if(sender instanceof Player) {
                        UUID id = ((Player) sender).getUniqueId();
                        if (getConfig().getConfigurationSection("generalOptions").getBoolean("alwaysExtract") || started) {
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
                                    sender.sendMessage(ChatColor.GREEN + "You have extracted " + args[1] +  " of your lives! You now have " + playersLives.get(id) + " live/s.");

                                    autoSave();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "You don't have enough lives!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /l extract (number)!");
                            }
                        } else {
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
                    if (sender.hasPermission("lives.scoreboard") || sender.hasPermission("lives.*")) {
                        if (args.length > 1) {
                            if (args[1].equalsIgnoreCase("show")) {

                                sender.sendMessage(ChatColor.GREEN + "Showing the scoreboard");
                                scoreboardShown = true;

                                updateScores();
                            } else if (args[1].equalsIgnoreCase("hide")) {

                                sender.sendMessage(ChatColor.RED + "Hid the scoreboard");
                                scoreboardShown = false;

                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    online.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                                }
                            } else if (args[1].equalsIgnoreCase("update")) {
                                updateScores();
                                sender.sendMessage(ChatColor.GREEN + "Updated the scoreboard!");
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives help admin to see usage");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Use: /lives help admin to see usage");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /l set [Player] [n]
                else if(args[0].equalsIgnoreCase("set")) {
                    if(sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
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
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Usage: /l set [Player] [number]!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /l set [Player] [number]!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /l add [Player] [n]
                else if(args[0].equalsIgnoreCase("add")) {
                    if(sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
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
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Usage: /l add [Player] [number]!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /l add [Player] [number]!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /l remove [Player] [n]
                else if(args[0].equalsIgnoreCase("remove")) {
                    if(sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
                        if(args.length > 2) {
                            if(StringUtils.isNumeric(args[2])) {
                                if(getServer().getPlayer(args[1]) != null) {
                                    UUID id = getServer().getPlayer(args[1]).getUniqueId();
                                    playersLives.put(id, playersLives.get(id) - Integer.parseInt(args[2]));
                                    sender.sendMessage(ChatColor.GREEN + "Removed " + args[1] + " " + args[2] + " live/s!");

                                    autoSave();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Player offline!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Usage: /l remove [Player] [number]!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /l remove [Player] [number]!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /l addeveryone [n] (/l addev [n])
                else if(args[0].equalsIgnoreCase("addeveryone") || args[0].equalsIgnoreCase("addev")) {
                    if(sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
                        if(args.length > 1) {
                            if(StringUtils.isNumeric(args[2])) {
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    playersLives.put(online.getUniqueId(), playersLives.get(online.getUniqueId()) + Integer.valueOf(args[1]));
                                    sender.sendMessage(ChatColor.GREEN + "Added everyone " + args[2] + " lives!");

                                    autoSave();
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Usage: /l addev [number]!");
                            }
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /l addev [number]!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                }

                //command /lives revive [Player] (/l rev) and /l admin_revive (/l arev)
                else if (args[0].equalsIgnoreCase("revive") || args[0].equalsIgnoreCase("rev") || args[0].equalsIgnoreCase("admin_revive") || args[0].equalsIgnoreCase("arev")) {
                    boolean revive = getConfig().getConfigurationSection("reviving").getBoolean("allowed");
                    boolean admin = false;
                    if (args[0].equalsIgnoreCase("admin_revive") || args[0].equalsIgnoreCase("arev") || !(sender instanceof Player)) {
                        if (sender.hasPermission("lives.revive.admin") || sender.hasPermission("lives.*")) {
                            admin = true;
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        }
                    }
                    if (revive || (sender.hasPermission("lives.revive") || sender.hasPermission("lives.*"))) {
                        if (args.length > 1) {
                            autoSave();
                            if (!sender.getName().equalsIgnoreCase(args[1]) || admin) {
                                revivePlayer(args[1], admin, sender);
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

                //command /l [Player] - shorter /l get
                else if (getServer().getPlayer(args[0]) != null) {
                    if (sender.hasPermission("lives.get") || sender.hasPermission("lives.*")) {
                        UUID id = getServer().getPlayer(args[0]).getUniqueId();
                        if (playersLives.containsKey(id)) {
                            sender.sendMessage(ChatColor.GREEN + args[0] + " has got " + playersLives.get(id) + " live/s.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "This is a bug!");
                        }
                    } else {
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
        if(started) {

            Player player = e.getEntity();
            playersLives.put(player.getUniqueId(), playersLives.get(player.getUniqueId()) - 1);
            player.sendMessage(ChatColor.RED + "You lost one life. You now have " + playersLives.get(player.getUniqueId()) + " live/s.");

            autoSave();
            if (playersLives.get(player.getUniqueId()) < 1) {
                getServer().broadcastMessage(ChatColor.DARK_RED + player.getName() + " lost his last life.");
                e.getEntity().getLocation().getWorld().strikeLightningEffect(e.getEntity().getLocation());

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
    public void addLife(PlayerInteractEvent e) {
        //using the life item
        Player player = e.getPlayer();
        if(e.getItem() != null) {
            if(checkItem(e.getItem())) {
                if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {

                    e.getItem().setAmount(e.getItem().getAmount() - 1);
                    playersLives.put(player.getUniqueId(), playersLives.get(player.getUniqueId()) + 1);
                    player.sendMessage(ChatColor.GREEN + "Added a life. You now have " + playersLives.get(player.getUniqueId()) + " live/s.");
                } else {
                    playersLives.put(player.getUniqueId(), playersLives.get(player.getUniqueId()) + e.getItem().getAmount());
                    player.sendMessage(ChatColor.GREEN + "Added " + e.getItem().getAmount() + " live/s. You now have " + playersLives.get(player.getUniqueId()) + " live/s.");
                    e.getItem().setAmount(0);
                }
                autoSave();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        //adding player if not in the database
        if(!playersLives.containsKey(e.getPlayer().getUniqueId())) {
            playersLives.put(e.getPlayer().getUniqueId(), getConfig().getConfigurationSection("generalOptions").getInt("onJoinLives"));
        } else if(playersLives.get(e.getPlayer().getUniqueId()) < 1) {
            if(getConfig().getConfigurationSection("penalty").getString("type").equalsIgnoreCase("GM3")) {
                e.getPlayer().setGameMode(GameMode.SPECTATOR);
            } else {
                e.getPlayer().sendMessage(ChatColor.GREEN + "You have been revived!");
                playersLives.put(e.getPlayer().getUniqueId(), getConfig().getConfigurationSection("reviving").getInt("lives"));
            }
        }

        autoSave();
    }

    @EventHandler
    public void moveItem(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (player.getGameMode() != GameMode.CREATIVE && e.getCurrentItem() != null && checkItem(e.getCurrentItem()) && (!player.hasPermission("lives.moveItem") && !player.hasPermission("lives.*"))) {
            e.setCancelled(true);

            playersLives.put(player.getUniqueId(), playersLives.get(player.getUniqueId()) + e.getCurrentItem().getAmount());
            e.getCurrentItem().setAmount(0);
            player.sendMessage(ChatColor.GREEN + "Added " + e.getCurrentItem().getAmount() +" live/s. You now have " + playersLives.get(player.getUniqueId()) + " live/s.");

            autoSave();
        }
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
        getLogger().info("[Lives]: Saved lives to file");
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
                getLogger().warning("[Lives]: Check lives.yml file, one of the entries is not a valid UUID and may cause plugin to 'crash'");
            }
        }
        getLogger().info("[Lives]: Loaded lives from file");
        updateScores();
    }

    public void giveItem(Player player, int amount) {
        ItemStack life = new ItemStack(Material.getMaterial(getConfig().getConfigurationSection("generalOptions").getString("item")));
        life.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta lifeMeta = life.getItemMeta();
        lifeMeta.setDisplayName(getConfig().getConfigurationSection("generalOptions").getString("itemName"));
        lifeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        NamespacedKey key = new NamespacedKey(this, "lives");
        lifeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "item");

        life.setItemMeta(lifeMeta);
        life.setAmount(amount);
        player.getInventory().addItem(life);
    }

    public void revivePlayer(String name, boolean admin, CommandSender sender) {
        Integer cost = getConfig().getConfigurationSection("reviving").getInt("cost");
        if (admin || playersLives.get(((Player) sender).getUniqueId()) > cost) {
            String pen = getConfig().getConfigurationSection("penalty").getString("type");

            if (pen.equalsIgnoreCase("BAN") || pen.equalsIgnoreCase("TEMPBAN")) {
                if (getServer().getBanList(BanList.Type.NAME).getBanEntry(name) != null) {
                    if (getServer().getBanList(BanList.Type.NAME).getBanEntry(name).getReason().equalsIgnoreCase(getConfig().getConfigurationSection("penalty").getString("banMessage"))) {
                        if (!admin) {
                            playersLives.put(((Player) sender).getUniqueId(), playersLives.get(((Player) sender).getUniqueId()) - cost);
                            sender.sendMessage(ChatColor.GREEN + "Revived " + name + " and took " + cost + " of your lives!");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Revived " + name + "!");
                        }

                        getServer().getBanList(BanList.Type.NAME).pardon(name);
                    } else {
                        sender.sendMessage(ChatColor.RED + "This player hasn't been banned for losing his last life! If you believe this is a mistake, please contact the server administrators.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "This player hasn't been banned!");
                }
            } else if (pen.equalsIgnoreCase("GM3")) {
                if (getServer().getPlayer(name) != null) {
                    if (getServer().getPlayer(name).getGameMode() == GameMode.SPECTATOR) {
                        getServer().getPlayer(name).setGameMode(GameMode.SURVIVAL);
                        getServer().getPlayer(name).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 350, 200, true, false));

                        if (!admin) {
                            playersLives.put(((Player) sender).getUniqueId(), playersLives.get(((Player) sender).getUniqueId()) - cost);
                            sender.sendMessage(ChatColor.GREEN + "Revived " + name + " and took " + cost + " of your lives!");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Revived " + name + "!");
                        }
                        playersLives.put(getServer().getPlayer(name).getUniqueId(), getConfig().getConfigurationSection("reviving").getInt("lives"));
                    } else {
                        sender.sendMessage(ChatColor.RED + "This player is not dead!");
                    }
                }  else {
                    sender.sendMessage(ChatColor.RED + "This player is not online!");
                }
            }
            autoSave();
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have enough lives to revive a player! (It costs: " + cost + " live/s)");
        }
    }

    public boolean checkItem(ItemStack item) {
        NamespacedKey key = new NamespacedKey(this, "lives");
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING) && meta.getPersistentDataContainer().get(key, PersistentDataType.STRING).equals("item");
        } else {
            return false;
        }
    }

    public void updateScores() {
        if (scoreboardShown) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard livesBoard = manager.getNewScoreboard();
            Objective objective = livesBoard.registerNewObjective("lives", "dummy", ChatColor.RED + getConfig().getConfigurationSection("scoreboard").getString("name"));

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.setScoreboard(livesBoard);

                objective.getScore(online.getName()).setScore(playersLives.get(online.getUniqueId()));
            }

            if (getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("TAB")) {
                objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            } else if (getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("SIDE")) {
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else if (getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("UNDER_NAME")) {
                objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            } else {
                getLogger().warning("[Lives]: The option \"type\" in config file under section \"scoreboard\" was set to an incorrect value! Using TAB by default!");
                objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            }
        }
    }

    public void autoSave() {
        if (getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
            if((System.currentTimeMillis() - lastSave) > (getConfig().getConfigurationSection("livesManagement").getInt("saveInterval") * 1000)) {
                saveLives();
                lastSave = System.currentTimeMillis();
            }
        }

        if(scoreboardShown) {
            updateScores();
        }
    }
}
