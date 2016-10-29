package nu.nerd.NerdAFK;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class PlayerData {
    
    public static final float MAX_MOVEMENT = (float)1.0; // 1 degree
    
    // Do not put any command chars at the end of the message here.
    // MC will remove them and the search/replace scheme of removing
    // this suffix will fail.
    public static final String AFK = ChatColor.GRAY + "(afk)";

    private Player _player;
    private Configuration _config;
    private boolean _isafk;
    private float _pitch;
    private float _yaw;
    private long _lastMoveTime;
    
    /**
     * Create new player data
     */
    public PlayerData(Player p, Configuration c) {
        _player = p;
        _config = c;
        _isafk = false;
        _pitch = p.getLocation().getPitch();
        _yaw   = p.getLocation().getYaw();
        _lastMoveTime = System.currentTimeMillis();
    }
    
    /**
     * Poll for AFK updates. This will set/clear the player AFK
     * status
     */
    public void PollMovement() {
        float newPitch = _player.getLocation().getPitch();
        float newYaw = _player.getLocation().getYaw();
        
        if ( Math.abs(newPitch - _pitch) < MAX_MOVEMENT &&
             Math.abs(newYaw - _yaw) < MAX_MOVEMENT) {

            if ((System.currentTimeMillis() - _lastMoveTime) > _config.AFK_DELAY) {
                setAFK();
            }
        } else {
            clearAFK();
        }

        _pitch = newPitch;
        _yaw = newYaw;
    }
    
    /**
     * Make a player afk
     */
    public void setAFK() {
        if (_isafk) {
            return;
        }

        _isafk = true;
        
        String newName = _player.getPlayerListName() + AFK;
        _player.setPlayerListName(newName);
        
        _player.getServer().broadcastMessage(
                String.format("%s%s* %s is afk", ChatColor.ITALIC, ChatColor.GRAY, _player.getName()));
    }
    
    /**
     * Clear the AFK status from a player
     */
    public void clearAFK() {
        _lastMoveTime = System.currentTimeMillis();
        if(!_isafk) {
            return;
        }
        _isafk = false;
        
        String newName = _player.getPlayerListName().replace(AFK, "");
        _player.setPlayerListName(newName);
        
        /*
        String name = _player.getPlayerListName();
        _player.getServer().getLogger().info("Name: " + name);
        int i = name.indexOf(AFK);
        _player.getServer().getLogger().info("i: " + i);
        String front = name.substring(0, i);
        String end = name.substring(i + AFK.length());
        _player.setPlayerListName(front + end);
        _player.getServer().getLogger().info("Front: " + front);
        _player.getServer().getLogger().info("End: " + end);
        */

        String msg;
        int r = (int)(Math.random()*100.0);
        if (r < 10) {
            msg = "" + ChatColor.ITALIC + ChatColor.GRAY + "* " + _player.getName() + " is back from the land of afk";
        } else {
            msg = "" + ChatColor.ITALIC + ChatColor.GRAY + "* " + _player.getName() + " is back from afk";
        }
        
        _player.getServer().broadcastMessage(msg);
    }
    
    /**
     * See if a player is AFK
     * @return true for AFK
     */
    public boolean isAFK() {
        return _isafk;
    }
    
    /**
     * Get the enclosed player
     * @return
     */
    public Player getPlayer() {
        return _player;
    }
    
}
