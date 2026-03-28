package io.github.bakedlibs.dough.protection.modules;

import io.github.bakedlibs.dough.protection.Interaction;
import io.github.bakedlibs.dough.protection.ProtectionModule;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public class WorldGuardProtectionModule implements ProtectionModule {

    private final Plugin plugin;

    public WorldGuardProtectionModule(@Nonnull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void load() {
    }

    @Override
    public boolean hasPermission(OfflinePlayer p, Location l, Interaction action) {
        return true;
    }
}
