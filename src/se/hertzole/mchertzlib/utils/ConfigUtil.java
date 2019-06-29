package se.hertzole.mchertzlib.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public final class ConfigUtil {

    private static Map<String, YamlConfiguration> resourceCache = new HashMap<>();

    public static void addIfEmpty(Plugin plugin, String resource, ConfigurationSection section) {
        process(plugin, resource, section, true, false);
    }

    public static void addMissingRemoveObsolete(Plugin plugin, String resource, ConfigurationSection section) {
        process(plugin, resource, section, false, true);
    }

    public static void addMissingRemoveObsolete(File file, YamlConfiguration defaults, FileConfiguration config) {
        try {
            process(defaults, config, false, true);
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void process(Plugin plugin, String resource, ConfigurationSection section, boolean addOnlyIfEmpty, boolean removeObsolete) {
        YamlConfiguration defaults = resourceCache.computeIfAbsent(resource, res -> {
            InputStream is = plugin.getResource("res/" + res);
            if (is == null) {
                throw new IllegalStateException("Couldn't read " + res + " from jar, please re-install Jumps");
            }
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            if (!scanner.hasNext()) {
                throw new IllegalStateException("No content in " + res + " in jar, please re-install Jumps");
            }
            String contents = scanner.next();
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.loadFromString(contents);
                return yaml;
            } catch (InvalidConfigurationException e) {
                throw new IllegalStateException("Invalid contents in " + res + " in jar, please re-install Jumps.");
            }
        });

        boolean modified = process(defaults, section, addOnlyIfEmpty, removeObsolete);
        if (modified) {
            plugin.saveConfig();
        }
    }

    private static boolean process(YamlConfiguration defaults, ConfigurationSection section, boolean addOnlyIfEmpty, boolean removeObsolete) {
        boolean modified = false;
        Set<String> present = section.getKeys(true);
        Set<String> required = defaults.getKeys(true);
        if (!addOnlyIfEmpty || present.isEmpty()) {
            for (String req : required) {
                if (!present.remove(req)) {
                    section.set(req, defaults.get(req));
                    modified = true;
                }
            }
        }
        if (removeObsolete) {
            for (String obs : present) {
                section.set(obs, null);
                modified = true;
            }
        }

        return modified;
    }
}
