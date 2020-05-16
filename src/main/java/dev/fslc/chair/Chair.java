package dev.fslc.chair;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Chair extends JavaPlugin {

    @Override
    public void onEnable() {
        registerListeners(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void registerListeners(Plugin plugin) {
        PluginManager m = plugin.getServer().getPluginManager();
        m.registerEvents(new ChairListener(plugin), plugin);
    }
}
