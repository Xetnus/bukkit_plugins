package me.Xetnus.CommandCooldown;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import java.util.Date;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Listeners implements Listener
{
	private CommandCooldown plugin;
	
	public Listeners(CommandCooldown plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{	
		// Gets the first word from the command entered
		String[] commands = event.getMessage().split(" ");
		String command = commands[0];
		
		// If the player does not have permission to bypass CommandCooldown
		if (!event.getPlayer().hasPermission("cooldown.bypass"))
		{
			String message = "NoCooldown";
			
			// Checks if the command has a cooldown, if it doesn't, then don't do anything
			message = checkForCooldown(command);
			
			if (!message.equals("NoCooldown"))
			{
				Date curDate = new Date();
				
				// Gets the previous time that the player typed the command
				int previousTime = plugin.getConfig().getInt("players." + message + "." + event.getPlayer().getName());
				// Gets the cooldown specified for the command
				int cooldown = Integer.parseInt(plugin.getConfig().getString("commands." + message + ".cooldown"));
				
				// If the cooldown time is -2, only allow ops to use the command
				if (cooldown == -2)
				{
					if (!event.getPlayer().isOp())
					{
						event.getPlayer().sendMessage(ChatColor.RED + "Sorry, but you do not have permission to perform this command.");
						event.setCancelled(true);
					}
					
				// If the cooldown time is -1, then the player can't use the command after the first time they use it
				} else if (cooldown == -1) {
					// If it's the first time that the player used this command, allow them to use it
					if (previousTime == 0)
					{
						plugin.getConfig().set("players." + message + "." + event.getPlayer().getName(), (int) (curDate.getTime() * 0.001));
						plugin.saveConfig();
						// If they've used this command before, disallow them from using it again
					} else {
						event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.RED + "You cannot use this command again.");
						event.setCancelled(true);
						return;
					}
				
				// If the player is able to use this command, let them to, updating the previous time in the config
				} else if ((((int) (curDate.getTime() * 0.001)) - previousTime >= cooldown)) {
				
					plugin.getConfig().set("players." + message + "." + event.getPlayer().getName(), (int) (curDate.getTime() * 0.001));
					plugin.saveConfig();
				
				// Calculates how long the user must wait to use the command again
				} else {
					int secsLeft = (int) (((curDate.getTime() * 0.001) - previousTime) - cooldown);
					int hours = Math.abs(secsLeft / 3600);
					int remainder = secsLeft % 3600;
					int minutes = Math.abs(remainder / 60);
					int seconds = Math.abs(remainder % 60); 
				
					event.setCancelled(true);
					if (hours > 0)
					{
						event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.RED + "You must wait " + hours + " hour(s), " + minutes + " minute(s) and " + seconds + " second(s) to use this command again.");
					}
					else if (minutes > 0)
					{
						event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.RED + "You must wait " + minutes + " minute(s) and " + seconds + " second(s) to use this command again.");
					}
					else
					{
						event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.RED + "You must wait " + seconds + " second(s) to use this command again.");
					}
				
				}
			}
		}
	}
	
	// Returns the base command for the command entered if it has a cooldown (gets rid of the alias)
	public String checkForCooldown(String message)
	{		
		// If the command itself is a base command, return the command
		if (plugin.getConfig().contains("commands." + message.toLowerCase() + ".cooldown"))
			return message.toLowerCase();			
		
		// If the command is an alias, find its base command and return it
		if (plugin.getConfig().contains("commands.Aliases." + message.toLowerCase()))
			return plugin.getConfig().get("commands.Aliases." + message.toLowerCase()).toString();
		
		return "NoCooldown";
	}
}
