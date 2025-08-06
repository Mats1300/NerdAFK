package nu.nerd.NerdAFK;

import org.bukkit.plugin.Plugin;

public class Configuration {

    /**
     * Time, in milliseconds, before a person is considered afk
     */
    public long AFK_DELAY = 300000;

    /**
     * The Maximum allowed head movement in degrees before resetting AFK timer.
     */
    public float MAX_MOVEMENT = 1.0f;

    /**
     * AFK suffix to display next to the player's name.
     */
    public String AFK_SUFFIX = "§7 (AFK)";

    /**
     * Load the config from disk.
     * If values are not specified in config.yml, defaults are used.
     */
    public void Load(Plugin plugin) {
        AFK_DELAY = plugin.getConfig().getLong("afk_delay", 300000);
        MAX_MOVEMENT = (float) plugin.getConfig().getDouble("max_movement", 1.0);
        AFK_SUFFIX = plugin.getConfig().getString("afk_suffix", "§7 (AFK)");
    }
}