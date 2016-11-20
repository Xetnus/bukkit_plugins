package me.xetnus.checkplayer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.faris.kingkits.hooks.PvPKits;

/**
 * This plugin allows staff members on a server to check other players who might be hacking or breaking the rules of the server. 
 * @author Grant Grubbs
 */
@SuppressWarnings("deprecation")
public class CheckPlayer extends JavaPlugin {
	private final String NAME = ChatColor.RED + "FazCheck: ";
	private ArrayList<StealthyPlayer> currentlyStealthy = new ArrayList<StealthyPlayer>();
	private GhostManager gManager;
	
	@Override
	public void onEnable() {
		getLogger().info("onEnable has been invoked!");
		
		gManager = new GhostManager(this);
		Listeners listeners = new Listeners(this);
		PluginManager pm = getServer().getPluginManager();
		
		// Adds the listeners
		pm.registerEvents(listeners, this);
	}
	
	@Override
	public void onDisable() {
		getLogger().info("onDisable has been invoked!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// If the console is attempting to execute a command
		if (!(sender instanceof Player)) {
			sender.sendMessage(NAME + ChatColor.YELLOW + "Only players in the game may use this command.");
			return false;
		}
		
		// If the command entered is /check
		if (label.equalsIgnoreCase("check")) {
			// Ensures that the player executing the command has permission.
			if (!sender.hasPermission("checkplayer.check")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				return false;
			// Ensures that the player entered in a username to be checked.
			} else if (args.length == 0) {
				sender.sendMessage(NAME + ChatColor.YELLOW + "Correct usage: /check <player's name>");
				return false;
			}
			
			// Ensures that the staff member entered a correct username.
			Player suspect = null;
			for (Player p : Bukkit.getServer().getOnlinePlayers())
				if (p.getName().equals(args[0]))
					suspect = p;
			
			if (suspect == null) {
				sender.sendMessage(NAME + ChatColor.YELLOW + "The player you entered is not online.");
				return false;
			} else if (suspect.getName().equals(sender.getName())) {
				sender.sendMessage(NAME + ChatColor.LIGHT_PURPLE + "Why would you check yourself? Oh, whatever...");
			}
			
			// Puts the player into stealth mode
			initiateStealthMode((Player) sender, suspect);
		} else if(label.equalsIgnoreCase("uncheck")) {
			if (!sender.hasPermission("checkplayer.check")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				return false;
			}
			
			endStealthMode((Player) sender);
		}
		
		return true;
	}
	
	/**
	 * Gets the player who are currently in stealth mode.
	 * @return an ArrayList containing all of the StealthyPlayers who are in stealth mode.
	 */
	public ArrayList<StealthyPlayer> getStealthyPlayers() {
		return currentlyStealthy;
	}
	
	/**
	 * Gets the GhostManager responsible for making players appear see-through.
	 * @return the GhostManager being used that will make players appear see-through.
	 */
	public GhostManager getGhostManager() {
		return gManager;
	}
	
	/**
	 * Removes a specified StealthyPlayer out of ArrayList
	 * @param player The StealthyPlayer who will be removed from the ArrayList of players currently in stealth mode.
	 */
	public void removeStealthyPlayer(StealthyPlayer player) {
		currentlyStealthy.remove(player);
	}
	
	/**
	 * Puts the player into stealth mode by making him/her invisible, teleporting him/her to the suspect and etc.
	 * @param staff The staff member who wishes to go into stealth mode
	 * @param suspect The suspect that the staff member wishes to be teleported to and to watch over
	 */
	private void initiateStealthMode(Player staff, Player suspect) {
		// Adds the staff member to the ArrayList
		currentlyStealthy.add(new StealthyPlayer(staff));
		
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.hasPermission("checkplayer.op"))
				gManager.addPlayer(p);
			else
				p.hidePlayer(staff);
		}
		
		gManager.setGhost(staff, true);
		
		// Clears all of his/her inventory
		staff.getInventory().clear();
		staff.getInventory().setHelmet(null);
		staff.getInventory().setChestplate(null);
		staff.getInventory().setLeggings(null);
		staff.getInventory().setBoots(null);
		
		// Enables flying
		staff.setAllowFlight(true);
		staff.setFlying(true);
		
		// Teleports the player to the given player (suspect)
		staff.teleport(suspect);
		
		// Tells the staff member that he/she is in stealth mode.
		staff.sendMessage(NAME + ChatColor.GREEN + "You are now in stealth mode. Type /uncheck to exit.");
		 
		// Announces to all staff that a staff member has entered stealth mode.
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.hasPermission("checkplayer.check")) {
				if (p.hasPermission("checkplayer.op"))
					p.sendMessage(NAME + ChatColor.DARK_GRAY + staff.getName() + ChatColor.GRAY + " is checking " + suspect.getName() + ".");
				else
					p.sendMessage(NAME + ChatColor.DARK_GRAY + staff.getName() + " has gone into stealth mode.");
			}
		}
	}
	
	/**
	 * Takes a staff member out of stealth mode.
	 * @param staff The staff member who will be taken out of stealth mode.
	 */
	private void endStealthMode(Player staff) {
		StealthyPlayer player = null;
		
		// Ensures that the player is currently in stealth mode
		for (StealthyPlayer p : currentlyStealthy)
			if(p.getPlayer() == staff)
				player = p;
		
		if (player == null) {
			staff.sendMessage(NAME + ChatColor.YELLOW + "You are not in stealth mode!");
			return;
		}
		
		// If the player is in creative mode, the player can keep their ability to fly
		if (staff.getGameMode() != GameMode.CREATIVE) {
			staff.setFlying(false);
			staff.setAllowFlight(false);
		}
		
		// Teleports the player back to spawn
		staff.teleport(staff.getWorld().getSpawnLocation());
		// Allows the player to get another kit
		PvPKits.removePlayer(staff);
		
		// Turns the player's translucent ability off.
		gManager.setGhost(staff, false);
		
		// Loops through everyone on the server, allowing everyone to see staff
		for (Player p : Bukkit.getServer().getOnlinePlayers())
			p.showPlayer(staff);
		
		// Removes the player from the ArrayList
		currentlyStealthy.remove(player);
		
		staff.sendMessage(NAME + ChatColor.GREEN + "You have exited stealth mode. You are now visible to everyone.");
		
		// Tells other staff members that the player has left stealth mode.
		for (Player p : Bukkit.getServer().getOnlinePlayers())
			if (p.hasPermission("checkplayer.check"))
				p.sendMessage(NAME + ChatColor.DARK_GRAY + staff.getName() + ChatColor.GRAY + " has exited stealth mode.");
	}
}
