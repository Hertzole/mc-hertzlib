package se.hertzole.mchertzlib.messages;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import se.hertzole.mchertzlib.HertzPlugin;

public class Messenger {

    public final String prefix;

    private HertzPlugin plugin;

    private ConsoleCommandSender console;

    public Messenger(HertzPlugin plugin, String prefix) {
        this.plugin = plugin;

        console = plugin.getServer().getConsoleSender();

        if (prefix.contains("&")) {
            prefix = ChatColor.translateAlternateColorCodes('&', prefix) + " ";
        }
        this.prefix = prefix;
    }

    public boolean tell(CommandSender p, String msg) {
        if (p == null || msg == null || msg.equals("")) {
            return false;
        }

        p.sendMessage(prefix + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', msg));
        return true;
    }

    public boolean tell(CommandSender p, Message msg, String s) {
        return tell(p, msg.format(s));
    }

    public boolean tell(CommandSender p, Message msg) {
        return tell(p, msg.toString());
    }

    public boolean tellConsole(String msg) {
        console.sendMessage(ChatColor.stripColor(prefix) + ChatColor.translateAlternateColorCodes('&', msg));
        return true;
    }

    public boolean tellConsole(Message msg) {
        return tellConsole(msg.toString());
    }

    public boolean tellConsole(Message msg, String s) {
        return tellConsole(msg.format(s));
    }
}
