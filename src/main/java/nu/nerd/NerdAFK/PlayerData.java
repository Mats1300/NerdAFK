package nu.nerd.NerdAFK;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import org.bukkit.entity.Player;

/**
 * Tracks and manages AFK status for a specific player.
 * Determines if the player is AFK based on head movement
 * and updates their tablist name via the TAB plugin accordingly.
 * <p>
 * This class uses the TAB API (v5.2.5) to dynamically modify the player's
 * tablist name using TabListFormatManager, without relying on placeholders.
 */
public class PlayerData {

    private final AFKPlugin plugin;

    /**
     * The Bukkit Player instance this data refers to.
     */
    private final Player player;

    /**
     * The TabListFormatManager for dynamic tablist modifications.
     */
    private final TabListFormatManager formatManager;

    /**
     * Configuration instance containing AFK detection parameters.
     */
    private final Configuration config;

    /**
     * True if the player is currently marked as AFK.
     */
    private boolean isAfk;

    /**
     * The last recorded pitch value of the player's head direction.
     */
    private float pitch;

    /**
     * The last recorded yaw value of the player's head direction.
     */
    private float yaw;

    /**
     * The system time (in ms) when the player last moved their head.
     */
    private long lastMoveTime;

    /**
     * The actual max movement threshold used, loaded from config or default.
     */
    private final float maxMovement;

    /**
     * The AFK suffix to append to player names when AFK
     */
    private final String afkSuffix;


    /**
     * Constructs a new PlayerData instance for tracking AFK status.
     *
     * @param player The Bukkit player.
     * @param config The plugin configuration instance.
     */
    public PlayerData(Player player, Configuration config, AFKPlugin plugin) {
        this.plugin = plugin;
        this.player = player;
        this.formatManager = TabAPI.getInstance().getTabListFormatManager();
        this.config = config;
        this.isAfk = false;
        this.pitch = player.getLocation().getPitch();
        this.yaw = player.getLocation().getYaw();
        this.lastMoveTime = System.currentTimeMillis();
        this.maxMovement = config.MAX_MOVEMENT;
        this.afkSuffix = config.AFK_SUFFIX;
    }

    /**
     * Returns the current TAB player for this Bukkit player.
     *
     * @return The TabPlayer instance or null if not available.
     */
    private TabPlayer getTabPlayer() {
        return TabAPI.getInstance().getPlayer(player.getUniqueId());
    }

    /**
     * Polls the player's head movement and updates AFK status if needed.
     * Should be called periodically (e.g., every few seconds) by the plugin's scheduler.
     */
    public void pollMovement() {
        // Players with this permission should never go AFK
        if (player.hasPermission("nerdafk.noautoafk")) {
            lastMoveTime = System.currentTimeMillis();
        }

        float newPitch = player.getLocation().getPitch();
        float newYaw = player.getLocation().getYaw();

        boolean hasMoved = Math.abs(newPitch - pitch) >= maxMovement ||
                Math.abs(newYaw - yaw) >= maxMovement;

        if (hasMoved) {
            lastMoveTime = System.currentTimeMillis();
            if (isAfk) {
                clearAFK();
            }
        } else {
            if ((System.currentTimeMillis() - lastMoveTime) > config.AFK_DELAY) {
                setAFK();
            }
        }

        pitch = newPitch;
        yaw = newYaw;
    }

    /**
     * Marks the player as AFK and updates their tablist name via TAB using TabListFormatManager.
     */
    public void setAFK() {
        if (isAfk) return;

        isAfk = true;
        TabPlayer tabPlayer = getTabPlayer();

        if (tabPlayer == null) {
            return;
        }

        if (formatManager != null) {
            formatManager.setName(tabPlayer, player.getName()); // keeps real name
            formatManager.setSuffix(tabPlayer, afkSuffix);
        } else {
            plugin.getLogger().warning("TAB: formatManager is null in setAFK()");
        }
    }

    /**
     * Clears the player's AFK status and resets their tablist name via TAB to config default.
     */
    public void clearAFK() {
        lastMoveTime = System.currentTimeMillis();
        if (!isAfk) return;

        isAfk = false;
        TabPlayer tabPlayer = getTabPlayer();
        if (tabPlayer != null && formatManager != null) {
            // Reset to config/default values
            formatManager.setName(tabPlayer, null);
            formatManager.setSuffix(tabPlayer, null);
        }
    }
}