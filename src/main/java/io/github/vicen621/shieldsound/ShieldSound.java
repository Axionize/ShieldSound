/*
 * Copyright (c) 2022. Vicen621
 * All rights reserved.
 */

package io.github.vicen621.shieldsound;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import io.github.vicen621.shieldsound.commands.ShieldSoundCommand;
import io.github.vicen621.shieldsound.config.Config;
import io.github.vicen621.shieldsound.config.ConfigManager;
import io.github.vicen621.shieldsound.listeners.ShieldBreakListener;
import io.github.vicen621.shieldsound.nms.SSEntityLiving;
import io.github.vicen621.shieldsound.nms.SSEntityLivingCraftBukkit;
import io.github.vicen621.shieldsound.nms.SSEntityLivingSpigot;
import io.github.vicen621.shieldsound.utils.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShieldSound extends JavaPlugin {

    private ConfigManager<Config> configManager;
    private VersionChecker versionChecker;
    private SSEntityLiving entityLiving;
    private double version;

    @Override
    public void onEnable() {
        configManager = new ConfigManager<>(this, "config.yml", Config.class);
        getLogger().info("[ShieldSound] > Loaded configuration file: config.yml!");

        versionChecker = new VersionChecker(this);
        versionChecker.versionCheck(null);

        commands();

        if (!setupEntityLiving()) {
            getLogger().severe("Failed to setup ShieldSound!");
            getLogger().severe("Your server version is not compatible with this plugin!");

            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        new ShieldBreakListener(this);

        getLogger().info("[ShieldSound] > ShieldSound has been enabled!");
    }

    @Override
    public void onDisable() {
        configManager.saveConfig();
    }

    @SuppressWarnings("deprecation")
    private void commands() {
        BukkitCommandManager cmdManager = new BukkitCommandManager(this);
        cmdManager.enableUnstableAPI("help");
        cmdManager.registerDependency(ShieldSound.class, "plugin", this);

        cmdManager.setFormat(MessageType.HELP, ChatColor.DARK_AQUA, ChatColor.AQUA, ChatColor.GRAY, ChatColor.DARK_GRAY);
        cmdManager.registerCommand(new ShieldSoundCommand());
    }

    private boolean setupEntityLiving() {
        String serverVersion = Bukkit.getBukkitVersion().split("-")[0];

        // Bukkit 1.x versions used a two-component version after the leading
        // "1." (for example, 1.21.1 -> 21.1). Newer Paper releases use the
        // calendar version scheme (for example, 26.2.build.135), which is not
        // a valid double. Keep the numeric value only for the legacy branch;
        // modern Paper always has the public absorption API.
        if (serverVersion.startsWith("1.")) {
            String legacyVersion = serverVersion.substring(2);
            int secondDot = legacyVersion.indexOf('.');
            if (secondDot >= 0) {
                legacyVersion = legacyVersion.substring(0, secondDot + 2);
            }
            version = Double.parseDouble(legacyVersion);
        } else {
            version = Double.POSITIVE_INFINITY;
        }

        if (version >= 14.4) // >= 1.14.4
            entityLiving = new SSEntityLivingSpigot();
        else if (version > 9.0) // >= 1.9
            entityLiving = new SSEntityLivingCraftBukkit();

        return entityLiving != null;
    }

    public Config getConfiguration() {
        return configManager.getConfig();
    }

    public ConfigManager<Config> getConfigManager() {
        return configManager;
    }

    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

    public SSEntityLiving getEntityLiving() {
        return entityLiving;
    }

    public double getVersion() {
        return version;
    }
}
