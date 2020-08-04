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

public class LivesAdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("lives_admin") || command.getName().equalsIgnoreCase("la")) {

            //command /la help
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.YELLOW + "\n Administrator commands:\n" + ChatColor.RESET +
                        " - /la reset (n) (revive) - resets lives counter for everyone to n lives (def 3), if run with revive options, revives everyone\n" +
                        " - /la give (Player) (n) - gives you (or specified player) n live items (def 1)\n" +
                        " - /la set [Player] [n] - sets players lives to n\n" +
                        " - /la add [Player] [n] - adds player n lives\n" +
                        " - /la remove [Player] [n] - removes player n lives\n" +
                        " - /la addeveryone [n] (Alias: /l addev) - adds everyone n lives\n" +
                        " - /la admin_revive [Player] (Alias: /l arev) - revives a player \"for free\"\n" +
                        " - /la [start | stop] - stops/starts lives counting\n" +
                        " - /la [save | load] - saves/loads lives to/from file\n" +
                        " - /la scoreboard [show | hide] (Alias: /l score) - shows/hides the lives scoreboard\n");
            }

            //command /la reset (n) (revive)
            else if (args[0].equalsIgnoreCase("reset")) {
                if (sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {

                    if (args.length > 1) {
                        if (StringUtils.isNumeric(args[1])) {
                            for (Player i : Bukkit.getOnlinePlayers()) {
                                Main.playersLives.put(i.getUniqueId(), Integer.valueOf(args[1]));
                            }
                            sender.sendMessage(ChatColor.GREEN + "Reset " + Bukkit.getOnlinePlayers().size() + " player/s lives to " + args[1]);
                        } else if (args[1].equalsIgnoreCase("revive")) {
                            for (Player i : Bukkit.getOnlinePlayers()) {
                                Main.playersLives.put(i.getUniqueId(), Main.plugin.getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                                Main.revivePlayer(i.getName(), true, sender, true);
                            }
                            sender.sendMessage(ChatColor.GREEN + "Reset " + Bukkit.getOnlinePlayers().size() + " player/s lives to " + Main.plugin.getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                            sender.sendMessage(ChatColor.GREEN + "Tried to revive " + Bukkit.getOnlinePlayers().size() + " player/s");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives reset (number) (revive)");
                        }
                    } else {
                        for (Player i : Bukkit.getOnlinePlayers()) {
                            Main.playersLives.put(i.getUniqueId(), Main.plugin.getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                        }
                        sender.sendMessage(ChatColor.GREEN + "Reset " + Bukkit.getOnlinePlayers().size() + " player/s lives to " + Main.plugin.getConfig().getConfigurationSection("generalOptions").getInt("resetLives"));
                    }
                    if (args.length > 2 && args[2].equalsIgnoreCase("revive")) {
                        for (Player i : Bukkit.getOnlinePlayers()) {
                            Main.revivePlayer(i.getName(), true, sender, true);
                        }
                        sender.sendMessage(ChatColor.GREEN + "Tried to revive " + Bukkit.getOnlinePlayers().size() + " player/s");
                    }

                    Main.autoSave();
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }

            //command /la give (Player) [n]
            else if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    if (sender.hasPermission("lives.give") || sender.hasPermission("lives.*")) {
                        if (args.length > 1) {
                            if (StringUtils.isNumeric(args[1])) {
                                Main.giveItem((Player) sender, Integer.parseInt(args[1]));
                                sender.sendMessage(ChatColor.GREEN + "Gave you " + args[1] + " live item/s");
                            } else if (Main.plugin.getServer().getPlayer(args[1]) != null) {
                                if (args.length > 2) {
                                    if (StringUtils.isNumeric(args[2])) {
                                        Main.giveItem(Main.plugin.getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
                                        sender.sendMessage(ChatColor.GREEN + "Gave " + args[1] + " " + args[2] + " live item/s");
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives give (Player) [number]");
                                    }
                                } else {
                                    Main.giveItem(Main.plugin.getServer().getPlayer(args[1]), 1);
                                    sender.sendMessage(ChatColor.GREEN + "Gave " + args[1] + " 1 live item");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /lives give (Player) [number]");
                            }
                        } else {
                            Main.giveItem((Player) sender, 1);
                            sender.sendMessage(ChatColor.GREEN + "Gave you 1 live item");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                    }
                } else {
                    if(args.length > 1) {
                        if(Main.plugin.getServer().getPlayer(args[1]) != null) {
                            if (args.length > 2) {
                                if (StringUtils.isNumeric(args[2])) {
                                    Main.giveItem(Main.plugin.getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
                                    sender.sendMessage("Gave "  + args[1] + " " + args[2] + " live item/s");
                                } else {
                                    sender.sendMessage("Invalid syntax! From console use: /lives give [Player] [number]");
                                }
                            } else {
                                Main.giveItem(Main.plugin.getServer().getPlayer(args[1]), 1);
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

            //command /la stop (quiet)
            else if (args[0].equalsIgnoreCase("stop")) {
                if (sender.hasPermission("lives.control") || sender.hasPermission("lives.*")) {
                    if (args.length > 1 && args[1].equalsIgnoreCase("quiet")) {
                        Main.plugin.getServer().broadcastMessage(ChatColor.RED + "Stopped counting lives!");
                    }
                    else { sender.sendMessage(ChatColor.RED + "Stopped counting lives!"); }
                    Main.started = false;
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }

            //command /la start (quiet)
            else if (args[0].equalsIgnoreCase("start")) {
                if (sender.hasPermission("lives.control") || sender.hasPermission("lives.*")) {
                    if (args.length > 1 && args[1].equalsIgnoreCase("quiet")) {
                        Main.plugin.getServer().broadcastMessage(ChatColor.GREEN + "Started counting lives!");
                    }
                    else { sender.sendMessage(ChatColor.GREEN + "Started counting lives!"); }
                    Main.started = true;
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }

            //command /la save
            else if (args[0].equalsIgnoreCase("save")) {
                if (sender.hasPermission("lives.save") || sender.hasPermission("lives.*")) {

                    Main.saveLives();
                    sender.sendMessage(ChatColor.GREEN + "Successfully saved lives to file!");
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }

            //command /la load
            else if (args[0].equalsIgnoreCase("load")) {
                if (sender.hasPermission("lives.save") || sender.hasPermission("lives.*")) {

                    Main.loadLives();
                    sender.sendMessage(ChatColor.GREEN + "Successfully loaded lives from file!");
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }

            //command /la scoreboard
            else if(args[0].equalsIgnoreCase("scoreboard") || args[0].equalsIgnoreCase("score")) {
                if (sender.hasPermission("lives.scoreboard") || sender.hasPermission("lives.*")) {
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("show")) {

                            sender.sendMessage(ChatColor.GREEN + "Showing the scoreboard");
                            Main.scoreboardShown = true;

                            Main.updateScores();
                        } else if (args[1].equalsIgnoreCase("hide")) {

                            sender.sendMessage(ChatColor.RED + "Hid the scoreboard");
                            Main.scoreboardShown = false;

                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                            }
                        } else if (args[1].equalsIgnoreCase("update")) {
                            Main.updateScores();
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

            //command /la set [Player] [n]
            else if(args[0].equalsIgnoreCase("set")) {
                if(sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
                    if(args.length > 2) {
                        if(StringUtils.isNumeric(args[2])) {
                            if(Main.plugin.getServer().getPlayer(args[1]) != null) {
                                Main.playersLives.put(Main.plugin.getServer().getPlayer(args[1]).getUniqueId(), Integer.valueOf(args[2]));
                                sender.sendMessage(ChatColor.GREEN + "Set " + args[1] + " lives to " + args[2] + '!');

                                Main.autoSave();
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

            //command /la add [Player] [n]
            else if(args[0].equalsIgnoreCase("add")) {
                if(sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
                    if(args.length > 2) {
                        if(StringUtils.isNumeric(args[2])) {
                            if(Main.plugin.getServer().getPlayer(args[1]) != null) {
                                UUID id = Main.plugin.getServer().getPlayer(args[1]).getUniqueId();
                                Main.playersLives.put(id, Main.playersLives.get(id) + Integer.parseInt(args[2]));
                                sender.sendMessage(ChatColor.GREEN + "Added " + args[1] + " " + args[2] + " live/s!");

                                Main.autoSave();
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

            //command /la remove [Player] [n]
            else if(args[0].equalsIgnoreCase("remove")) {
                if(sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
                    if(args.length > 2) {
                        if(StringUtils.isNumeric(args[2])) {
                            if(Main.plugin.getServer().getPlayer(args[1]) != null) {
                                UUID id = Main.plugin.getServer().getPlayer(args[1]).getUniqueId();
                                Main.playersLives.put(id, Main.playersLives.get(id) - Integer.parseInt(args[2]));
                                sender.sendMessage(ChatColor.GREEN + "Removed " + args[1] + " " + args[2] + " live/s!");

                                Main.autoSave();
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

            //command /la addeveryone [n] (/l addev [n])
            else if(args[0].equalsIgnoreCase("addeveryone") || args[0].equalsIgnoreCase("addev")) {
                if(sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
                    if(args.length > 1) {
                        if(StringUtils.isNumeric(args[2])) {
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                Main.playersLives.put(online.getUniqueId(), Main.playersLives.get(online.getUniqueId()) + Integer.valueOf(args[1]));
                                sender.sendMessage(ChatColor.GREEN + "Added everyone " + args[2] + " lives!");

                                Main.autoSave();
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

            //command /la admin_revive (/l arev)
            else if (args[0].equalsIgnoreCase("admin_revive") || args[0].equalsIgnoreCase("arev")) {
                if (sender.hasPermission("lives.revive.admin") || sender.hasPermission("lives.*")) {
                    if (args.length > 1) {
                        Main.autoSave();

                        Main.revivePlayer(args[1], true, sender, false);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Too few arguments! See /la help for usage.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }

            //wrong command
            else {
                sender.sendMessage(ChatColor.RED + "Invalid syntax! Use: /la help to see commands");
            }
        }

        return false;
    }
}
