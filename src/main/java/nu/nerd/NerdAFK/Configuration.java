package nu.nerd.NerdAFK;

import org.bukkit.plugin.Plugin;

public class Configuration {
    
    /** Time, in milliseconds, before a person is considered afk */
    public long AFK_DELAY = 600000;
    
    /**
     * Load the config from disk
     */
    public void Load(Plugin plugin) {
        AFK_DELAY = plugin.getConfig().getLong("afk_delay", 600000);
    }

}
