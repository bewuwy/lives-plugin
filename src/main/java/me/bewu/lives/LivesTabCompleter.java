package me.bewu.lives;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LivesTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            List<String> arg1 = new ArrayList<>();
            arg1.add("extract");
            arg1.add("revive");
            arg1.add("status");
            arg1.add("help");

            if (sender.hasPermission("lives.get") || sender.hasPermission("lives.*")) {
                arg1.add("get");
            }
            if (sender.hasPermission("lives.revive.admin") || sender.hasPermission("lives.*")) {
                arg1.add("arev");
            }
            if (sender.hasPermission("lives.set") || sender.hasPermission("lives.*")) {
                arg1.add("reset");
                arg1.add("set");
                arg1.add("add");
                arg1.add("remove");
                arg1.add("addev");
            }
            if (sender.hasPermission("lives.give") || sender.hasPermission("lives.*")) {
                arg1.add("give");
            }
            if (sender.hasPermission("lives.control") || sender.hasPermission("lives.*")) {
                arg1.add("start");
                arg1.add("stop");
            }
            if (sender.hasPermission("lives.save") || sender.hasPermission("lives.*")) {
                arg1.add("save");
                arg1.add("load");
            }
            if (sender.hasPermission("lives.scoreboard") || sender.hasPermission("lives.*")) {
                arg1.add("scoreboard");
            }
            if (sender.hasPermission("lives.config.reset") || sender.hasPermission("lives.*")) {
                arg1.add("reset_config");
            }

            return arg1;
        }
        else if (args.length == 2) {

        }

        return null;
    }
}
