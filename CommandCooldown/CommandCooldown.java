package me.Xetnus.CommandCooldown;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.Xetnus.CommandCooldown.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandCooldown extends JavaPlugin
{
	private final Listeners listeners = new Listeners(this);
	
	@Override
	public void onEnable()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(listeners, this);
		
		getLogger().info("Enabled CommandCooldown");
		
		// Creates the directory if it doesn't already exist
		if (!(getDataFolder().exists())) 
	    {
	        getDataFolder().mkdir();
	    }
		
		// Saves the config file if it doesn't exist
		if (!new File(getDataFolder(), "config.yml").exists())
		{
		     this.saveDefaultConfig();
		}
		
		// If readme.txt doesn't exist, create it
	    copyReadMeIfNotPresent();
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("Disabled CommandCooldown");
	}
	
	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender.hasPermission("cooldown.admin"))
		{
			// Displays the help screen, showing all commands for the plugin
			if (args.length == 0 || args[0].equalsIgnoreCase("help"))
			{
				sender.sendMessage(ChatColor.BLUE + "Here are the commands for CommandCooldown:");
				sender.sendMessage(ChatColor.GREEN + "/cool add <cooldown time> <command> <alias #1> <alias etc>  " + ChatColor.AQUA + "~ adds a command with cooldown");
				sender.sendMessage(ChatColor.AQUA + "Example: /cool add 1h /tnt /etnt");
				sender.sendMessage(ChatColor.GREEN + "/cool add op <command> <alias #1> <alias etc>  " + ChatColor.AQUA + "~ adds a command that only ops can use");
				sender.sendMessage(ChatColor.AQUA + "Example: /cool add op /?");
				sender.sendMessage(ChatColor.GREEN + "/cool reload  " + ChatColor.AQUA + "~ reloads the config file");
				sender.sendMessage(ChatColor.GREEN + "/cool help  " + ChatColor.AQUA + "~ shows this help screen");
				
				return true;
			}
			// Reloads the config file
			else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl"))
			{
				this.reloadConfig();
				sender.sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.GREEN + "The config was reloaded!");
				
				return true;
			}
			// Attempts to add a command to the config file
			else if (args[0].equalsIgnoreCase("add"))
			{
				// If the command wasn't typed properly, tell the user that
				if (args.length <= 2)
				{
					sender.sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.RED + "Please type the command like this: /cool add <cooldown time> <command> <alias #1> <alias etc>");
				} else {
					int time = 0;
					int len = args[1].length();
					
					// If the user didn't enter a time of -1, convert the time that they entered into seconds
					if (args[1].equalsIgnoreCase("op"))
					{
						time = -2;
					} else if (args[1].charAt(0) != '-') {
						switch(args[1].charAt(len - 1))
						{
						case 's': // Converts time to seconds
							time = Integer.parseInt(args[1].substring(0, len - 1));
							break;
						case 'm': // Converts minutes to seconds
							time = Integer.parseInt(args[1].substring(0, len - 1));
							time *= 60;
							break;
						case 'h': // Converts hours to seconds
							time = Integer.parseInt(args[1].substring(0, len - 1));
							time *= 3600;
							break;
						case 'd': // Converts days to seconds
							time = Integer.parseInt(args[1].substring(0, len - 1));
							time *= 86400;
							break;
						default: // If the user entered an incorrect time, tell the user that.
							sender.sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.RED + "Check the time that you entered, something went wrong when converting it to seconds.");
							return false;
						}
					} else if (args[1].charAt(0) == '-'){ // If they entered -1 for the time, set time equals to -1
						time = -1;
					}
					
					// Obtains any aliases that the user may have input
					String[] aliases = new String[args.length - 3];
					for (int i = 3; i < args.length; i++)
					{
						aliases[i - 3] = args[i];
					}
					
					// Creates aliases for the base command. Saved in the config file.
					createAlias(aliases, args[2]);
					
					this.getConfig().set("commands." + args[2] + ".cooldown", Integer.toString(time));
					this.saveConfig();
					sender.sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.GREEN + "This command has been added to the config!");
				}
			
				return true;
			}
			else
			{
				sender.sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.RED + "Incorrect usage. Please check to make sure that you have typed the command correctly.");
			
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.DARK_PURPLE + "[Cooldown] " + ChatColor.RED + "You don't have permission.");
		}
		return false;
	}
	
	// Creates aliases for the specified base command in the config
	public void createAlias(String[] aliases, String baseCommand)
	{
		for (int i = 0; i < aliases.length; i++)
		{
			this.getConfig().set("commands.Aliases." + aliases[i].toLowerCase(), baseCommand.toLowerCase());
		}
	}
	
	// If readme.txt doesn't exist, create it and fill it with all of the text
	public void copyReadMeIfNotPresent()
	{
	    try {
	    	File file = new File(getDataFolder(), "readme.txt");
	    	if (!file.exists())
	    	{
	    		getDataFolder().mkdirs();
				file.createNewFile();
			}
	    	
	    	InputStream in = this.getClass().getResourceAsStream("/readme.txt");
	    	OutputStream out;
			out = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int have;
			
			while((have = in.read(buffer)) > -1)
			    out.write(buffer, 0, have);
			
			out.close();
			in.close();
	    } catch (Exception e) {
	    	getLogger().info("readme.txt could not be generated.");
			e.printStackTrace();
	    }
	}
}
