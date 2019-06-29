package se.hertzole.mchertzlib;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import se.hertzole.mchertzlib.commands.BaseCommandHandler;
import se.hertzole.mchertzlib.config.LoadsConfigFile;
import se.hertzole.mchertzlib.exceptions.ConfigError;
import se.hertzole.mchertzlib.messages.Messenger;

import java.io.File;
import java.util.logging.Level;

public abstract class HertzPlugin extends JavaPlugin {

    protected Messenger messenger;

    private Throwable lastFailureCause;

    private LoadsConfigFile loadsConfigFile;
    protected FileConfiguration config;

    @Override
    public void onEnable() {
        preOnEnable();

        try {
            setup();
            reload();
        } catch (ConfigError e) {
            getLogger().log(Level.SEVERE, "You have an error in your config file!\n\n" + e.getMessage() + "\n");
        }

        onEnabled();
    }

    protected void preOnEnable() {

    }

    protected abstract void onEnabled();

    @Override
    public void onDisable() {
        loadsConfigFile = null;
        onDisabled();
    }

    protected void onDisabled() {
    }

    private void setup() {
        try {
            createDataFolder();
            setupCommandHandler();

            onSetup();
        } catch (RuntimeException e) {
            setLastFailureCauseAndRethrow(e);
        }
        lastFailureCause = null;
    }

    private void createDataFolder() {
        File dir = getDataFolder();
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private void setupCommandHandler() {
        BaseCommandHandler handler = getCommandHandler();

        this.getCommand(getCommandPrefix()).setExecutor(handler);
        this.getCommand(getCommandPrefix()).setTabCompleter(handler);
    }

    protected abstract BaseCommandHandler getCommandHandler();

    protected abstract String getCommandPrefix();

    protected abstract void onSetup();

    public void reload() {
        try {

            reloadConfig();
            reloadGlobalMessenger();

            onReload();
        } catch (RuntimeException e) {
            setLastFailureCauseAndRethrow(e);
        }
        lastFailureCause = null;
    }

    @Override
    public void reloadConfig() {
        if (loadsConfigFile == null) {
            loadsConfigFile = new LoadsConfigFile(this);
        }

        config = loadsConfigFile.load();

        onReloadConfig(config);
    }

    protected abstract void onReloadConfig(FileConfiguration config);

    protected void onReload() {
    }

    protected abstract void reloadGlobalMessenger();

    private void setLastFailureCauseAndRethrow(RuntimeException exception) {
        lastFailureCause = exception;
        throw exception;
    }

    @Override
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public Throwable getLastFailureCause() {
        return lastFailureCause;
    }

    public Messenger getGlobalMessenger() {
        if (messenger == null) {
            messenger = new Messenger(this, "&f[&9HertzLib&f]");
        }

        return messenger;
    }
}
