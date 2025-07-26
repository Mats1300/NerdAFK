package nu.nerd.NerdAFK;

import java.util.Hashtable;
import java.util.UUID;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class AFKPlugin extends JavaPlugin implements Listener, Runnable {
    
    private Hashtable<UUID, PlayerData> _playerData;
    private final Configuration config = new Configuration();

    /**
     * Called when the plugin is first enabled
     */
    @Override
    public void onEnable() {
        getLogger().info("Starting...");
        _playerData = new Hashtable<>();
        saveDefaultConfig();
        config.Load(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this, 200, 20);
    }
    
    /**
     * Called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        getLogger().info("Stopping...");
        _playerData = null;
        // The events and scheduled task should stop themselves. (maybe)
    }
    
    /**
     * Process user commands.
     * 
     * @param sender Command origin
     * @param command The command to be performed. Includes all yml meta-data.
     * @param label The exact name of the command the user typed in
     * @param args Command arguments
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Check Command
        if (command.getName().equalsIgnoreCase("afk")) {
            return cmdAFK(sender, args);
        } else {
            return false;
        }
    }

    /**
     * Perform the /afk [ARGS] action
     * @param sender Command sender
     * @param args command arguments
     * @return Returns true if the command was handled
     */
    public boolean cmdAFK(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            PlayerData data = _playerData.get(p.getUniqueId());

            data.setAFK(); // This sets the AFK state

            Component baseMessage = Component.text("* " + p.getName())
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC);

            final Component fullMessage;

            if (Math.random() < 0.05 && args.length == 0) {
                fullMessage = baseMessage.append(Component.text(" went to afk land"));
            } else {
                Component temp = baseMessage.append(Component.text(" is afk"));
                for (String arg : args) {
                    temp = temp.append(Component.text(" " + arg));
                }
                fullMessage = temp;
            }

            // Broadcast using Adventure's audience API
            p.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(fullMessage));
        } else {
            sender.sendMessage("You are always afk!");
        }
        return true;
    }

    /**
     * Handle player join event
     * @param e Event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        _playerData.put(
                e.getPlayer().getUniqueId(), 
                new PlayerData(e.getPlayer(), config, this));
    }
    
    /**
     * Remove players as they leave
     * @param e The PlayerQuitEvent triggered when a player disconnects
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        _playerData.remove(e.getPlayer().getUniqueId());
    }
    
    /**
     * The player typed something. Reset the AFK timer
     * @param e The AsyncChatEvent fired when a player sends a chat message
     */
    @EventHandler
    public void onPlayerAsyncChat(AsyncChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        PlayerData data = _playerData.get(uuid);

        if (data == null) return;

        // If async, run clearAFK() on the main thread
        if (e.isAsynchronous()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerData d = _playerData.get(uuid);
                    if (d != null) d.clearAFK();
                }
            }.runTaskLater(this, 0);
        } else {
            data.clearAFK();
        }
    }
    
    /**
     * Clear afk when a player enters a command
     * @param e The PlayerCommandPreprocessEvent triggered before a player command is processed
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        _playerData.get(e.getPlayer().getUniqueId()).clearAFK();
    }
    
    /**
     * This is called every 20 ticks to find AFK players
     */
    @Override
    public void run() {
        for (PlayerData p : _playerData.values()) {
            p.pollMovement();
        }

    }

    /**
     * Gets a hashtable containing all player data entries.
     * Each entry maps a player's UUID to their corresponding PlayerData instance.
     * This can be used by external plugins or other parts of the plugin to access
     * the AFK status and related information of players.
     *
     * @return a hashtable mapping player UUIDs to PlayerData objects
     */
    @SuppressWarnings("unused")
    public Hashtable<UUID, PlayerData> getPlayerData() {
        return _playerData;
    }
}
