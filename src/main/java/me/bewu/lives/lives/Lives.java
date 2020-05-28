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
                    file.renameTo(new File(getDataFolder(), "config" + getConfig().getString("version") + ".yml"));
                    saveDefaultConfig();

                    getLogger().info("-------------------------------------------------");
                    getLogger().info("Config.yml outdated (" + oldVer + ")");
                    getLogger().info("New config file was created, you can see old options in config" + oldVer +".yml");
                    getLogger().info("-------------------------------------------------");

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

        if(sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equals("lives")) {
                if (args.length == 0) {
                    player.sendMessage("You have got: " + String.valueOf(playersLives.get(player.getName())) + " live/s.");


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
                        player.spigot().sendMessage(message);
                    }

                } else if (args[0].equals("reset")) {
                    if (player.hasPermission("lives.reset")) {

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
                                    player.sendMessage(ChatColor.GREEN + "Reset lives to " + args[1]);
                                } else {
                                    player.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives reset [number]");
                                }
                            } else {
                                playersLives.put(i, getConfig().getInt("resetLives"));
                                player.sendMessage(ChatColor.GREEN + "Reset lives to " + getConfig().getInt("resetLives"));
                            }
                            // player.sendMessage(String.valueOf(i) + ": " + playersLives.get(i));
                            // player.sendMessage(String.valueOf(playersLives));
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("get")) {
                    if (player.hasPermission("lives.get")) {
                        // player.sendMessage(String.valueOf(playersLives));
                        if (playersLives.containsKey(args[1])) {
                            player.sendMessage(args[1] + " has got " + String.valueOf(playersLives.get(args[1])) + " live/s.");
                        } else {
                            player.sendMessage(ChatColor.RED + "This player is not in the database. If this player is online and you see this error, use /lives reset");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("give")) {
                    if (player.hasPermission("lives.give")) {
                        if (args.length > 1) {
                            if (StringUtils.isNumeric(args[1])) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "give " + player.getName() + " minecraft:emerald{Enchantments:[{id:\"minecraft:unbreaking\",lvl:1}],display:{Name:\"{\\\"text\\\":\\\"" + getConfig().getString("itemName") + "\\\"}\"}} " + args[1]);
                            } else {
                                player.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives get [number]");
                            }
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "give " + player.getName() + " minecraft:emerald{Enchantments:[{id:\"minecraft:unbreaking\",lvl:1}],display:{Name:\"{\\\"text\\\":\\\"" + getConfig().getString("itemName") + "\\\"}\"}} 1");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("stop")) {
                    if (player.hasPermission("lives.control")) {
                        player.sendMessage(ChatColor.RED + "Stopped counting lives.");
                        started = false;
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("start")) {
                    if (player.hasPermission("lives.control")) {
                        player.sendMessage(ChatColor.GREEN + "Started counting lives.");
                        started = true;
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("status")) {
                    if (player.hasPermission("lives.status")) {
                        if (started) {
                            player.sendMessage(ChatColor.GREEN + "Lives counting is on.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Lives counting is off.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("reset_config")) {

                    if (player.hasPermission("lives.config.reset")) {
                        File file = new File(getDataFolder(), "config.yml");
                        file.delete();
                        saveDefaultConfig();
                        player.sendMessage(ChatColor.RED + "Reset config to default values.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("save")) {

                    if (player.hasPermission("lives.save")) {
                        saveLives();
                        player.sendMessage(ChatColor.GREEN + "Successfully saved lives to file!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("load")) {

                    if (player.hasPermission("lives.save")) {
                        loadLives();
                        player.sendMessage(ChatColor.GREEN + "Successfully loaded lives from file!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else if (args[0].equals("help")) {
                    player.sendMessage(" /lives - tells you how many lives you have \n /lives extract - extracts one of your lives to an item \n /lives get [Player] - tells you how many lives the player has \n /lives reset [n] - resets lives counter for everyone to n lives (def 3) \n /lives give [n] - gives you n live items (def 1) \n /lives [start | stop] - stops/starts lives counting \n /lives status - tells the status of lives counting \n /lives reset_config - resets config to default values \n /lives [save | load] - saves/loads lives to/from file");
                } else if(args[0].equals("extract")) {
                    if(playersLives.get(player.getName()) > 1) {
                        playersLives.put(player.getName(), playersLives.get(player.getName()) - 1);
                        ItemStack life = new ItemStack(Material.EMERALD);
                        life.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                        player.getInventory().addItem(life);
                        player.sendMessage(ChatColor.GREEN + "You have extracted one of your lives! You now have " + playersLives.get(player.getName()) + " live/s.");
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "You can't extract lives when you have only one life.");
                    }
                    if(getConfig().getBoolean("autoSave")) {
                        saveLives();
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives help to see commands");
                }
            }
        }
        else {
            ConsoleCommandSender console = getServer().getConsoleSender();
            console.sendMessage("Currently commands can only be send using in game console.");
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
