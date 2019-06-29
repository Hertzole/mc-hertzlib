package se.hertzole.mchertzlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class CommandsUtil {

    public static Player unwrap(CommandSender sender) {
        Player proxy = (Player) sender;
        UUID id = proxy.getUniqueId();
        return Bukkit.getPlayer(id);
    }

    public static boolean isPlayer(CommandSender sender) {
        return (sender instanceof Player);
    }
}
