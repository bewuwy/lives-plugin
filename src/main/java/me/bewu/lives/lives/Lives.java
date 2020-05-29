package me.bewu.lives.lives;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Lives extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        // getLogger().info("Plugin started");
        createCustomConfig();

        if(getConfig().getBoolean("autoLoad")) {
            loadLives();
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        List<String> players = new ArrayList<String>();

        for(Player i: Bukkit.getOnlinePlayers()) {
            players.add(i.getName());
        }

        for(String i: players) {
            playersLives.put(i, 3);
        }

        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");

                if(!getConfig().getString("version").equals(getDescription().getVersion())) {
                    String oldVer = getConfig().getString("version");

                    HashMap<String, Object> oldConf = new HashMap<String, Object>();
                    for(String i : getConfig().getKeys(false)) {
                        oldConf.put(i, getConfig().get(i));
                    }

                    file.delete();
                    saveDefaultConfig();

                    getLogger().info("-------------------------------------------------");
                    getLogger().info("Config.yml outdated (v" + oldVer + ")");
                    getLogger().info("Updated it to the latest version.");
                    getLogger().info("-------------------------------------------------");

                    for(String i : oldConf.keySet()) {
                        getConfig().set(i, oldConf.get(i));
                    }

                    getConfig().set("version", getDescription().getVersion());

                    saveConfig();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    Map<String, Integer> playersLives = new HashMap<>();
    boolean started = getConfig().getBoolean("defStarted");
    private File customConfigFile;
    private FileConfiguration customConfig;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> players = new ArrayList<String>();


            if (command.getName().equalsIgnoreCase("lives") || command.getName().equalsIgnoreCase("l")) {
                if (args.length == 0) {
                    if(sender instanceof Player) {
                        sender.sendMessage("You have got: " + String.valueOf(playersLives.get(sender.getName())) + " live/s.");

                        Random rand = new Random();
                        int rand_int1 = rand.nextInt(15);

                        if (rand_int1 == 0) {
                            //ad
                            TextComponent message = new TextComponent("This plugin was made by ");
                            TextComponent bewu = new TextComponent(ChatColor.AQUA + "bewu");
                            message.addExtra(bewu);
                            message.addExtra(ChatColor.WHITE + ". Please consider donating ");
                            TextComponent messageLink = new TextComponent(ChatColor.BLUE + "here");
                            messageLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.ko-fi.com/bewuwy"));
                            message.addExtra(messageLink);
                            message.addExtra(".");
                            sender.spigot().sendMessage(message);
                        }
                    }
                    else {
                        sender.sendMessage("You can't use that command from the console!");
                    }

                } else if (args[0].equalsIgnoreCase("reset")) {
                    if (sender.hasPermission("lives.reset")) {

                        // List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
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
                                    sender.sendMessage(ChatColor.GREEN + "Reset lives to " + args[1]);
                                    if(getConfig().getBoolean("autoSave")) {
                                        saveLives();
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives reset [number]");
                                }
                            } else {
                                playersLives.put(i, getConfig().getInt("resetLives"));
                                sender.sendMessage(ChatColor.GREEN + "Reset lives to " + getConfig().getInt("resetLives"));
                                if(getConfig().getBoolean("autoSave")) {
                                    saveLives();
                                }
                            }
                            // player.sendMessage(String.valueOf(i) + ": " + playersLives.get(i));
                            // player.sendMessage(String.valueOf(playersLives));
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("get")) {
                    if (sender.hasPermission("lives.get")) {
                        // player.sendMessage(String.valueOf(playersLives));
                        if (playersLives.containsKey(args[1])) {
                            sender.sendMessage(args[1] + " has got " + String.valueOf(playersLives.get(args[1])) + " live/s.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "This player is not in the database. If this player is online and you see this error, use /lives reset");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("give")) {
                    if(sender instanceof Player) {
                        if (sender.hasPermission("lives.give")) {
                            if (args.length > 1) {
                                if (StringUtils.isNumeric(args[1])) {
                                    ItemStack life = new ItemStack(Material.EMERALD);
                                    life.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                    ItemMeta lifeMeta = life.getItemMeta();
                                    lifeMeta.setDisplayName(getConfig().getString("itemName"));
                                    life.setItemMeta(lifeMeta);
                                    life.setAmount(Integer.parseInt(args[1]));
                                    Player player = (Player) sender;
                                    player.getInventory().addItem(life);
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives get [number]");
                                }
                            } else {
                                ItemStack life = new ItemStack(Material.EMERALD);
                                life.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                ItemMeta lifeMeta = life.getItemMeta();
                                lifeMeta.setDisplayName(getConfig().getString("itemName"));
                                life.setItemMeta(lifeMeta);
                                Player player = (Player) sender;
                                player.getInventory().addItem(life);
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        }
                    }
                    else {
                        sender.sendMessage("You can't use that command from the console!");
                    }
                } else if (args[0].equalsIgnoreCase("stop")) {
                    if (sender.hasPermission("lives.control")) {
                        sender.sendMessage(ChatColor.RED + "Stopped counting lives.");
                        started = false;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("start")) {
                    if (sender.hasPermission("lives.control")) {
                        sender.sendMessage(ChatColor.GREEN + "Started counting lives.");
                        started = true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("status")) {
                    if (sender.hasPermission("lives.status")) {
                        if (started) {
                            sender.sendMessage(ChatColor.GREEN + "Lives counting is on.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Lives counting is off.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("reset_config")) {

                    if (sender.hasPermission("lives.config.reset")) {
                        File file = new File(getDataFolder(), "config.yml");
                        file.delete();
                        saveDefaultConfig();
                        sender.sendMessage(ChatColor.RED + "Reset config to default values.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("save")) {

                    if (sender.hasPermission("lives.save")) {
                        saveLives();
                        sender.sendMessage(ChatColor.GREEN + "Successfully saved lives to file!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("load")) {

                    if (sender.hasPermission("lives.save")) {
                        loadLives();
                        sender.sendMessage(ChatColor.GREEN + "Successfully loaded lives from file!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(" /lives - tells you how many lives you have \n /lives extract - extracts one of your lives to an item \n /lives get [Player] - tells you how many lives the player has \n /lives reset [n] - resets lives counter for everyone to n lives (def 3) \n /lives give [n] - gives you n live items (def 1) \n /lives [start | stop] - stops/starts lives counting \n /lives status - tells the status of lives counting \n /lives reset_config - resets config to default values \n /lives [save | load] - saves/loads lives to/from file");
                } else if(args[0].equalsIgnoreCase("extract")) {
                    if(sender instanceof Player) {
                        if (playersLives.get(sender.getName()) > 1) {
                            playersLives.put(sender.getName(), playersLives.get(sender.getName()) - 1);
                            ItemStack life = new ItemStack(Material.EMERALD);
                            life.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                            ItemMeta lifeMeta = life.getItemMeta();
                            lifeMeta.setDisplayName(getConfig().getString("itemName"));
                            life.setItemMeta(lifeMeta);
                            Player player = (Player) sender;
                            player.getInventory().addItem(life);
                            sender.sendMessage(ChatColor.GREEN + "You have extracted one of your lives! You now have " + playersLives.get(player.getName()) + " live/s.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You can't extract lives when you have only one life.");
                        }
                        if (getConfig().getBoolean("autoSave")) {
                            saveLives();
                        }
                    }
                    else {
                        sender.sendMessage("You can't use that command from the console!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives help to see commands");
                }
            }
        return false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        // getLogger().info("Someone died");
        if(started) {
            Player player = (Player) e.getEntity();
            playersLives.put(player.getName(), playersLives.get(player.getName()) - 1);
            player.sendMessage(ChatColor.RED + "You lost one life. You now have " + playersLives.get(player.getName()) + " live/s.");
            if(getConfig().getBoolean("autoSave")) {
                saveLives();
            }

            if (playersLives.get(player.getName()) < 1) {
                getServer().broadcastMessage(ChatColor.DARK_RED + player.getName() + " lost his last life.");
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
//        else {
//            e.getEntity().sendMessage("You got lucky and didn't lose a life.");
//        }
    }

    @EventHandler
    public void add_life(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(e.hasItem()) {
            if (Objects.requireNonNull(e.getItem()).getType() == Material.EMERALD && e.getItem().containsEnchantment(Enchantment.DURABILITY)) {
                if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                    player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                } else {
                    player.getInventory().getItemInMainHand().setAmount(0);
                }
                playersLives.put(player.getName(), playersLives.get(player.getName()) + 1);
                player.sendMessage(ChatColor.GREEN + "Added a life. You now have " + playersLives.get(player.getName()) + " live/s.");
                if(getConfig().getBoolean("autoSave")) {
                    saveLives();
                }
            }
        }
    }

    @EventHandler
    public void nowy_gracz(PlayerJoinEvent e) {
        if(!playersLives.containsKey(e.getPlayer().getName())) {
            playersLives.put(e.getPlayer().getName(), getConfig().getInt("onJoinLives"));
            if(getConfig().getBoolean("autoSave")) {
                saveLives();
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

}
