package me.bewu.lives;

import me.bewu.lives.commands.LivesAdminCommand;
import me.bewu.lives.commands.LivesAdminTabCompleter;
import me.bewu.lives.commands.LivesCommand;
import me.bewu.lives.commands.LivesTabCompleter;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
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

    public static JavaPlugin plugin;
    public static Configuration config;
    public static boolean started;
    public static boolean scoreboardShown;
    public static Map<UUID, Integer> playersLives = new HashMap<>();
    private static File customConfigFile;
    private static FileConfiguration customConfig;
    private static long lastSave;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        config = plugin.getConfig();
        started = config.getConfigurationSection("generalOptions").getBoolean("defStarted");
        scoreboardShown = config.getConfigurationSection("scoreboard").getBoolean("defShown");

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("lives").setExecutor(new LivesCommand());
        getCommand("lives").setTabCompleter(new LivesTabCompleter());

        getCommand("lives_admin").setExecutor(new LivesAdminCommand());
        getCommand("lives_admin").setTabCompleter(new LivesAdminTabCompleter());

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

    public static void saveLives() {
        //saving lives to file
        for(Map.Entry me : playersLives.entrySet()) {
            customConfig.set(String.valueOf(me.getKey()), me.getValue());
        }

        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Main.plugin.getLogger().info("[Lives]: Saved lives to file");
    }

    public static void loadLives() {
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
                Main.plugin.getLogger().warning("[Lives]: Check lives.yml file, one of the entries is not a valid UUID and may cause plugin to 'crash'");
            }
        }
        Main.plugin.getLogger().info("[Lives]: Loaded lives from file");
        updateScores();
    }

    public static void giveItem(Player player, int amount) {
        ItemStack life = new ItemStack(Material.getMaterial(Main.plugin.getConfig().getConfigurationSection("generalOptions").getString("item")));
        life.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta lifeMeta = life.getItemMeta();
        lifeMeta.setDisplayName(Main.plugin.getConfig().getConfigurationSection("generalOptions").getString("itemName"));
        lifeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        NamespacedKey key = new NamespacedKey(plugin, "lives");
        lifeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "item");

        life.setItemMeta(lifeMeta);
        life.setAmount(amount);
        player.getInventory().addItem(life);
    }

    public static void revivePlayer(String name, boolean admin, CommandSender sender, boolean quiet) {
        Integer cost = Main.plugin.getConfig().getConfigurationSection("reviving").getInt("cost");
        if (admin || playersLives.get(((Player) sender).getUniqueId()) > cost) {
            String pen = Main.plugin.getConfig().getConfigurationSection("penalty").getString("type");

            if (pen.equalsIgnoreCase("BAN") || pen.equalsIgnoreCase("TEMPBAN")) {
                if (Main.plugin.getServer().getBanList(BanList.Type.NAME).getBanEntry(name) != null) {
                    if (Main.plugin.getServer().getBanList(BanList.Type.NAME).getBanEntry(name).getReason().equalsIgnoreCase(Main.plugin.getConfig().getConfigurationSection("penalty").getString("banMessage"))) {
                        if (!admin) {
                            playersLives.put(((Player) sender).getUniqueId(), playersLives.get(((Player) sender).getUniqueId()) - cost);
                            if (!quiet) sender.sendMessage(ChatColor.GREEN + "Revived " + name + " and took " + cost + " of your lives!");
                        } else {
                            if (!quiet) sender.sendMessage(ChatColor.GREEN + "Revived " + name + "!");
                        }

                        Main.plugin.getServer().getBanList(BanList.Type.NAME).pardon(name);
                    } else {
                        if (!quiet) sender.sendMessage(ChatColor.RED + "This player hasn't been banned for losing his last life! If you believe this is a mistake, please contact the server administrators.");
                    }
                } else {
                    if (!quiet) sender.sendMessage(ChatColor.RED + "This player hasn't been banned!");
                }
            } else if (pen.equalsIgnoreCase("GM3")) {
                if (Main.plugin.getServer().getPlayer(name) != null) {
                    if (Main.plugin.getServer().getPlayer(name).getGameMode() == GameMode.SPECTATOR) {
                        Main.plugin.getServer().getPlayer(name).setGameMode(GameMode.SURVIVAL);
                        Main.plugin.getServer().getPlayer(name).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 350, 200, true, false));

                        if (!admin) {
                            playersLives.put(((Player) sender).getUniqueId(), playersLives.get(((Player) sender).getUniqueId()) - cost);
                            if (!quiet) sender.sendMessage(ChatColor.GREEN + "Revived " + name + " and took " + cost + " of your lives!");
                        } else {
                            if (!quiet) sender.sendMessage(ChatColor.GREEN + "Revived " + name + "!");
                        }
                        playersLives.put(Main.plugin.getServer().getPlayer(name).getUniqueId(), Main.plugin.getConfig().getConfigurationSection("reviving").getInt("lives"));
                    } else {
                        if (!quiet) sender.sendMessage(ChatColor.RED + "This player is not dead!");
                    }
                }  else {
                    if (!quiet) sender.sendMessage(ChatColor.RED + "This player is not online!");
                }
            }
            autoSave();
        } else {
            if (!quiet) sender.sendMessage(ChatColor.RED + "You don't have enough lives to revive a player! (It costs: " + cost + " live/s)");
        }
    }

    private boolean checkItem(ItemStack item) {
        NamespacedKey key = new NamespacedKey(this, "lives");
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING) && meta.getPersistentDataContainer().get(key, PersistentDataType.STRING).equals("item");
        } else {
            return false;
        }
    }

    public static void updateScores() {
        if (scoreboardShown) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard livesBoard = manager.getNewScoreboard();
            Objective objective = livesBoard.registerNewObjective("lives", "dummy", ChatColor.RED + Main.plugin.getConfig().getConfigurationSection("scoreboard").getString("name"));

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.setScoreboard(livesBoard);

                objective.getScore(online.getName()).setScore(playersLives.get(online.getUniqueId()));
            }

            if (Main.plugin.getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("TAB")) {
                objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            } else if (Main.plugin.getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("SIDE")) {
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else if (Main.plugin.getConfig().getConfigurationSection("scoreboard").getString("type").equalsIgnoreCase("UNDER_NAME")) {
                objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            } else {
                Main.plugin.getLogger().warning("[Lives]: The option \"type\" in config file under section \"scoreboard\" was set to an incorrect value! Using TAB by default!");
                objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            }
        }
    }

    public static void autoSave() {
        if (Main.plugin.getConfig().getConfigurationSection("livesManagement").getBoolean("autoSave")) {
            if((System.currentTimeMillis() - lastSave) > (Main.plugin.getConfig().getConfigurationSection("livesManagement").getInt("saveInterval") * 1000)) {
                saveLives();
                lastSave = System.currentTimeMillis();
            }
        }

        if(scoreboardShown) {
            updateScores();
        }
    }
}
