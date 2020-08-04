package me.bewu.lives.commands;

import me.bewu.lives.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LivesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("lives") || command.getName().equalsIgnoreCase("l")) {
            //command /lives (/l)
            if (args.length == 0) {
                if(sender instanceof Player) {
                    sender.sendMessage(ChatColor.GREEN + "You have got: " + Main.playersLives.get(((Player) sender).getUniqueId()) + " live/s.");
                } else {
                    sender.sendMessage("You can't use that command from the console!");
                }
            }

            //command /lives get [Player]
            else if (args[0].equalsIgnoreCase("get")) {
                if(args.length > 1) {
                    if (sender.hasPermission("lives.get") || sender.hasPermission("lives.*")) {
                        if (Main.plugin.getServer().getPlayer(args[1]) != null) {
                            UUID id = Main.plugin.getServer().getPlayer(args[1]).getUniqueId();
                            if (Main.playersLives.containsKey(id)) {
                                sender.sendMessage(args[1] + " has got " + Main.playersLives.get(id) + " live/s.");
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

            //command /lives status
            else if (args[0].equalsIgnoreCase("status")) {
                if (Main.started) {
                    sender.sendMessage(ChatColor.GREEN + "Lives counting is on.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Lives counting is off.");
                }
            }

            //command /lives help
            else if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.YELLOW + " Default commands:\n" + ChatColor.RESET +
                        " - /lives (Player) (Alias: /l) - tells you how many lives you (or specified player) have\n" +
                        " - /lives get [Player] - tells you how many lives the player has\n" +
                        " - /lives extract (n) (Alias: /l ex) - extracts n of your lives to an item (def 1)\n" +
                        " - /lives revive [Player] (Alias: /l rev) - revives a player for some lives\n" +
                        " - /lives status - tells if lives counting is on or off\n" +
                        " - /lives help - displays this list");
            }

            //command /lives extract (number) Alias: /l ex
            else if(args[0].equalsIgnoreCase("extract") || args[0].equalsIgnoreCase("ex")) {
                if(sender instanceof Player) {
                    UUID id = ((Player) sender).getUniqueId();
                    if (Main.plugin.getConfig().getConfigurationSection("generalOptions").getBoolean("alwaysExtract") || Main.started) {
                        if(args.length == 1) {
                            if (Main.playersLives.get(id) > 1) {

                                Main.playersLives.put(id, Main.playersLives.get(id) - 1);
                                Main.giveItem((Player) sender, 1);
                                sender.sendMessage(ChatColor.GREEN + "You have extracted one of your lives! You now have " + Main.playersLives.get(id) + " live/s.");

                                Main.autoSave();
                            } else {
                                sender.sendMessage(ChatColor.RED + "You can't extract lives when you have only one life.");
                            }
                        } else if(StringUtils.isNumeric(args[1])) {
                            if (Main.playersLives.get(id) > Integer.parseInt(args[1])) {

                                Main.playersLives.put(id, Main.playersLives.get(id) - Integer.parseInt(args[1]));
                                Main.giveItem((Player) sender, Integer.parseInt(args[1]));
                                sender.sendMessage(ChatColor.GREEN + "You have extracted " + args[1] +  " of your lives! You now have " + Main.playersLives.get(id) + " live/s.");

                                Main.autoSave();
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

            //command /lives revive [Player] (/l rev) and /l admin_revive (/l arev)
            else if (args[0].equalsIgnoreCase("revive") || args[0].equalsIgnoreCase("rev") || args[0].equalsIgnoreCase("admin_revive") || args[0].equalsIgnoreCase("arev")) {
                boolean revive = Main.plugin.getConfig().getConfigurationSection("reviving").getBoolean("allowed");
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
                        Main.autoSave();
                        if (!sender.getName().equalsIgnoreCase(args[1]) || admin) {
                            Main.revivePlayer(args[1], admin, sender, false);
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

            //command /lives version
            else if(args[0].equalsIgnoreCase("version")) {
                sender.sendMessage("The plugin version is " + Main.plugin.getDescription().getVersion());
            }

            //command /l [Player] - shorter /l get
            else if (Main.plugin.getServer().getPlayer(args[0]) != null) {
                if (sender.hasPermission("lives.get") || sender.hasPermission("lives.*")) {
                    UUID id = Main.plugin.getServer().getPlayer(args[0]).getUniqueId();
                    if (Main.playersLives.containsKey(id)) {
                        sender.sendMessage(ChatColor.GREEN + args[0] + " has got " + Main.playersLives.get(id) + " live/s.");
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
}
