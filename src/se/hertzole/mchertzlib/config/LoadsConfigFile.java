package se.hertzole.mchertzlib.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import se.hertzole.mchertzlib.HertzPlugin;

import java.io.File;
import java.io.IOException;

public class LoadsConfigFile {

    private final HertzPlugin plugin;

    public LoadsConfigFile(HertzPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration load() {
        try {
            return loadConfiguration();
        } catch (IOException | InvalidConfigurationException e) {
            throw new IllegalStateException("Failed to load config file.", e);
        }
    }

    private FileConfiguration loadConfiguration() throws IOException, InvalidConfigurationException {
        File folder = createDataFolder();
        File file = new File(folder, "config.yml");
        if (!file.exists()) {
            plugin.getGlobalMessenger().tellConsole("No config file found. Creating default...");
            plugin.saveDefaultConfig();
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(file);
        return yaml;
    }

    private File createDataFolder() {
        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IllegalStateException("Failed to create data folder");
            }
        }

        return folder;
    }
}
