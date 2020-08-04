package me.bewu.lives.commands;

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

            if (args[0].length() > 0) {
                arg1.removeIf(i -> !i.startsWith(args[0]));
            }

            return arg1;
        }

        else if (args.length == 2) {

        }

        return null;
    }
}
