package se.hertzole.mchertzlib.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.Conversable;
import se.hertzole.mchertzlib.HertzPlugin;
import se.hertzole.mchertzlib.exceptions.ConfigError;
import se.hertzole.mchertzlib.messages.Messenger;

import java.util.*;

public abstract class BaseCommandHandler implements CommandExecutor, TabCompleter {

    protected HertzPlugin plugin;
    private Messenger fallbackMessenger;

    private Map<String, Command> commands;

    public BaseCommandHandler(HertzPlugin plugin) {
        this.plugin = plugin;

        fallbackMessenger = new Messenger(plugin, "&f[&6Fallback&f]");

        commands = new LinkedHashMap<>();
        registerCommands();
    }

    public abstract void registerCommands();

    protected void register(Class<? extends Command> c) {
        CommandInfo info = c.getAnnotation(CommandInfo.class);
        if (info == null)
            return;

        try {
            commands.put(info.pattern(), c.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Command> getMatchingCommands(String arg) {
        List<Command> result = new ArrayList<>();

        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            if (arg.matches(entry.getKey())) {
                result.add(entry.getValue());
            }
        }

        return result;
    }

    private String[] trimFirstArg(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission(getReloadPermission())) {
            return safeTell(sender, getNoPermissionMessage());
        }
        try {
            plugin.reload();
            plugin.getGlobalMessenger().tell(sender, "&aPlugin reloaded!");
        } catch (ConfigError e) {
            fallbackMessenger.tell(sender, "Reload failed due to config file error:\n" + ChatColor.RED + e.getMessage());
        } catch (Exception e) {
            fallbackMessenger.tell(sender, "Reload failed:\n" + ChatColor.RED + e.getMessage());
        }

        return true;
    }

    private boolean safeTell(CommandSender sender, String msg) {
        if (plugin != null) {
            plugin.getGlobalMessenger().tell(sender, msg);
        } else {
            fallbackMessenger.tell(sender, msg);
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        StringBuilder user = new StringBuilder();

        for (Command cmd : commands.values()) {
            CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
            if (!sender.hasPermission(info.permission()))
                continue;

            user.append("\n").append(ChatColor.RESET).append(info.usage()).append(" ").append(ChatColor.YELLOW).append(info.desc());
        }
        plugin.getGlobalMessenger().tell(sender, "Available commands: " + user.toString());
    }

    private void showUsage(Command cmd, CommandSender sender, boolean prefix) {
        CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
        if (!sender.hasPermission(info.permission()))
            return;

        sender.sendMessage((prefix ? "Usage: " : "") + info.usage() + " " + ChatColor.YELLOW + info.desc());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command bukkitCommand, String label, String[] args) {
        String base = (args.length > 0 ? args[0] : "").toLowerCase();
        String last = (args.length > 0 ? args[args.length - 1] : "").toLowerCase();

        if (sender instanceof Conversable && ((Conversable) sender).isConversing()) {
            return true;
        }

        if (base.equals("")) {
            return safeTell(sender, getHelpMessage());
        }

        if (base.equals("reload") || (base.equals("config") && args.length > 1 && args[1].equals("reload"))) {
            return reload(sender);
        }

        if (base.equals("?") || base.equals("help")) {
            showHelp(sender);
            return true;
        }

        List<Command> matches = getMatchingCommands(base);

        if (matches.size() > 1) {
            plugin.getGlobalMessenger().tell(sender, getMultipleMatchesMessage());
            for (Command cmd : matches) {
                showUsage(cmd, sender, false);
            }

            return true;
        }

        if (matches.size() == 0) {
            plugin.getGlobalMessenger().tell(sender, getUnknownCommandMessage());
            return true;
        }

        Command command = matches.get(0);
        CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);

        if (sender instanceof ConsoleCommandSender && !info.console()) {
            plugin.getGlobalMessenger().tell(sender, getNoConsoleMessage());
            return true;
        }

        if (!sender.hasPermission(info.permission())) {
            plugin.getGlobalMessenger().tell(sender, getNoPermissionMessage());
            return true;
        }

        if (last.equals("?") || last.equals("help")) {
            showUsage(command, sender, true);
            return true;
        }

        String[] params = trimFirstArg(args);
        if (!command.execute(plugin, sender, params)) {
            showUsage(command, sender, true);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command bukkitCommand, String alias, String[] args) {
        String base = (args.length > 0 ? args[0] : "").toLowerCase();

        List<String> result = new ArrayList<>();

        //if (sender.hasPermission(getReloadPermission()))

        if (sender instanceof Conversable && ((Conversable) sender).isConversing()) {
            return result;
        }

        if (args.length == 1) {
            result.addAll(commands.keySet());
            return result;
        }

        List<Command> matches = getMatchingCommands(base);

        if (matches.size() > 0) {
            Command command = matches.get(0);

            String[] params = trimFirstArg(args);
            result = command.onTabComplete(sender, bukkitCommand, alias, params.length - 1);
        }

        return result;
    }

    protected abstract String getReloadPermission();

    protected abstract String getHelpMessage();

    protected abstract String getMultipleMatchesMessage();

    protected abstract String getUnknownCommandMessage();

    protected abstract String getNoPermissionMessage();

    protected abstract String getNoConsoleMessage();
}
