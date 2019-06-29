package se.hertzole.mchertzlib.commands;

import org.bukkit.command.CommandSender;
import se.hertzole.mchertzlib.HertzPlugin;

import java.util.List;

public interface Command {

    boolean execute(HertzPlugin plugin, CommandSender sender, String... args);

    List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, int argIndex);
}
