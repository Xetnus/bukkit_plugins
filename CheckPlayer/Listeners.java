package me.xetnus.checkplayer;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.faris.kingkits.hooks.PvPKits;

@SuppressWarnings("deprecation")
public class Listeners implements Listener {
	private final String NAME = ChatColor.RED + "FazCheck: ";
	private CheckPlayer check;
	
	public Listeners(CheckPlayer checkPlayer) {
		check = checkPlayer;
	}
	
	/**
	 * Handles an event where a player joins the server.
	 * This listener will ensure that no new players joining the server will be able to see any staff
	 * members currently in stealth mode.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		// If the player is op, allow that player to see ghosts
		if (e.getPlayer().hasPermission("checkplayer.op")) {
			check.getGhostManager().addPlayer(e.getPlayer());
			return;
		}
		
		// Loops through all players currently in stealth mode and ensures that the player joining the server won't be able to see them.
		for (StealthyPlayer stealthP : check.getStealthyPlayers())
			e.getPlayer().hidePlayer(stealthP.getPlayer());
	}
	
	/**
	 * Handles an event where a player leaves the server.
	 * This listener checks to see if the player leaving is currently in stealth mode. IF they are,
	 * they will be removed from stealth mode.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		
		// Loops through all players currently in stealth mode
		for (StealthyPlayer stealthP : check.getStealthyPlayers()) {
			if (stealthP.getPlayer() == player) {
				// Teleports the player back to spawn
				player.teleport(player.getWorld().getSpawnLocation());
				
				// If the player is in creative mode, there's no reason to reset his ability to fly.
				if (player.getGameMode() != GameMode.CREATIVE) {
					player.setFlying(false);
					player.setAllowFlight(false);
				}
				// Allows the player to get another kit
				PvPKits.removePlayer(player);
				// Removes the player from being a ghost
				check.getGhostManager().removePlayer(player);
				// Removes the player from the ArrayList
				check.removeStealthyPlayer(stealthP);
				
				break;
			}
		}
	}
	
	/**
	 * Handles an event where an entity damages another entity.
	 * This listener prevents a staff member from abusing their stealth mode by limiting how often they're able to
	 * hit another player while in stealth mode.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		// If both entities involved are players
		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			Player player = (Player) e.getDamager();
			
			// Loops through all StealthyPlayers, looking to see if the player is in stealth mode.
			for (StealthyPlayer p : check.getStealthyPlayers()) {
				if(p.getPlayer() == player) {
					// If the player is not able to attack again at this moment, tell him/her so and cancel the event.
					if (!p.canAttackAgain()) {
						e.setCancelled(true);
						player.sendMessage(NAME + ChatColor.RED + "You can only attack a player every 3 seconds while in stealth mode.");
					} else {
						p.setMostRecentAttack();
					}
					break;
				}
			}
		}
	}
}
