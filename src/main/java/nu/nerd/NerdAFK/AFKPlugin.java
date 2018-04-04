package nu.nerd.NerdAFK;

import java.util.Hashtable;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;


public class AFKPlugin extends JavaPlugin implements Listener, Runnable {
    
    private Hashtable<UUID, PlayerData> _playerData;
    private Configuration config = new Configuration();

    /**
     * Called when the plugin is first enabled
     */
    @Override
    public void onEnable() {
        getLogger().info("Starting...");
        _playerData = new Hashtable<UUID, PlayerData>();
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
     * @param command The command to be performed. Includes all yml meta data.
     * @param label The exact name of the command the user typed in
     * @param args Command arguments
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
        if (sender instanceof Player) {
            Player p = (Player)sender;
            _playerData.get(p.getUniqueId()).setAFK();

            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.ITALIC);
            builder.append(ChatColor.GRAY);
            builder.append("* ");
            builder.append(p.getName());

            if (Math.random() < 0.05 && args.length == 0) {
                builder.append(" went to afk land");
            } else {
                builder.append(" is afk");
                for (String arg : args) {
                    builder.append(" ");
                    builder.append(arg);
                }
            }
            p.getServer().broadcastMessage(builder.toString());
        } else {
            // The sender is the console
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
                new PlayerData(e.getPlayer(), config));
    }
    
    /**
     * Remove players as they leave
     * @param e
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        _playerData.remove(e.getPlayer().getUniqueId());
    }
    
    /**
     * The player typed something. Reset the AFK timer
     * @param e
     */
    @EventHandler
    public void onPlayerAsyncChat(AsyncPlayerChatEvent e) {
        final AsyncPlayerChatEvent e2 = e;

        // Run this on the main thread if it is async
        if (e.isAsynchronous()) {
            new BukkitRunnable() {
                
                @Override
                public void run() {
                    _playerData.get(e2.getPlayer().getUniqueId()).clearAFK();
                }
    
            }.runTaskLater(this, 0);
        } else {
            _playerData.get(e2.getPlayer().getUniqueId()).clearAFK();
        }
    }
    
    /**
     * Clear afk when a player enters a command
     * @param e
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
            p.PollMovement();
        }

    }
    
    /**
     * Get a list of all the player data for external plugins
     * @return
     */
    public Hashtable<UUID, PlayerData> getPlayerData() {
        return _playerData;
    }


}
