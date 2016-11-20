package me.Xetnus.MysteryCrates;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class MysteryCrates extends JavaPlugin
{
	
	@Override
	public void onEnable()
	{
		// Creates the default config file if it doesn't exist already
		File configFile = new File("plugins/MysteryCrates/config.yml");	
		if (!configFile.exists())
		{
		     this.saveDefaultConfig();
		}
		
		// If the backup file doesn't already exist, create it, and copy everything over from config.yml
		File backup = new File("plugins/MysteryCrates/backup.yml");	
		if (!backup.exists())
		{
			// Creates the file
		     try {
				backup.createNewFile();
			} catch (IOException e) {
				getLogger().info("Error: Backup file could not be created.");
			}
		     
		    FileChannel source = null;
		    FileChannel destination = null;
		     
		    // Copies everything from config to backup file
		    try {
		        source = new FileInputStream(configFile).getChannel();
		        destination = new FileOutputStream(backup).getChannel();
		        destination.transferFrom(source, 0, source.size());
		    } catch (Exception e) {
		    	getLogger().info("Error: Backup file could not be updated (on load).");
		    }
		    finally {
		        if(source != null) {
		            try {
						source.close();
					} catch (IOException e) {
						getLogger().info("Error: Config file could not be closed (on load).");
					}
		        }
		        if(destination != null) {
		            try {
						destination.close();
					} catch (IOException e) {
						getLogger().info("Error: Backup file could not be closed (on load).");
					}
		        }
		    }
		}
		
		getLogger().info("onEnable has been invoked!");
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("onDisable has been invoked!");
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{		
		File backup = new File("plugins/MysteryCrates/backup.yml");	
		File configFile = new File("plugins/MysteryCrates/config.yml");		
		
		FileChannel source = null;
	    FileChannel destination = null;
	     
	    // Updates the backup file every time someone types /crate. This copies everything from config to the backup file
	    try {
	        source = new FileInputStream(configFile).getChannel();
	        destination = new FileOutputStream(backup).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    } catch (Exception e) {
	    	getLogger().info("Error: Backup file could not be updated.");
	    }
	    finally {
	        if(source != null) {
	            try {
					source.close();
				} catch (IOException e) {
					getLogger().info("Error: Config file could not be closed.");
				}
	        }
	        if(destination != null) {
	            try {
					destination.close();
				} catch (IOException e) {
					getLogger().info("Error: Backup file could not be closed.");
				}
	        }
	    }
		
		int currentBal;
		// Shows the player available commands for them
		if (args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			// Admin commands, only admins can see this
			if (sender.hasPermission("crates.admin"))
			{
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.AQUA + "Here are the commands to use MysteryCrates:");
				sender.sendMessage(ChatColor.GREEN + "/crate open  " + ChatColor.BLUE + "- opens one of your crates");
				sender.sendMessage(ChatColor.GREEN + "/crate balance <username>  " + ChatColor.BLUE + "- see the current balance of a player");
				sender.sendMessage(ChatColor.GREEN + "/crate pay <username> <amount of crates>  " + ChatColor.BLUE + "- pays someone some of your crates");
				sender.sendMessage(ChatColor.GREEN + "/crate give <username> <amount of crates>  " + ChatColor.BLUE + "- gives someone an amount of crates (admin use)");
				sender.sendMessage(ChatColor.GREEN + "/crate remove <username> <amount of crates>  " + ChatColor.BLUE + "- removes an amount of crates from a player (admin use)");
				sender.sendMessage(ChatColor.GREEN + "/crate reload  " + ChatColor.BLUE + "- reloads the config file (admin use)");
			// The regular user sees these commands
			} else {
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.AQUA + "Here are the commands to use MysteryCrates:");
				sender.sendMessage(ChatColor.GREEN + "/crate open  " + ChatColor.BLUE + "- opens one of your crates");
				sender.sendMessage(ChatColor.GREEN + "/crate balance  " + ChatColor.BLUE + "- see your current crate balance");
				sender.sendMessage(ChatColor.GREEN + "/crate pay <username> <amount of crates> " + ChatColor.BLUE + "- pays someone some of your crates");
			}
			
			return true;
			
		// Gives a player a certain amount of crates
		} else if (args[0].equalsIgnoreCase("give")) {
			if (sender.hasPermission("crates.admin"))
			{
				// If they didn't give all the parameters needed
				if (args.length <= 1)
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "Incorrect usage. Type /crate give <username> <amount of crates>");
				} else {
					int amount;
					
					// If the player didn't enter an amount, assume 1 crate
					if (args.length == 2)
						amount = 1;
					else
						amount = Integer.parseInt(args[2]);
					
					// Updates the player's balance of crates
					currentBal = this.getConfig().getInt("balances." + args[1].toLowerCase());
					this.getConfig().set("balances." + args[1].toLowerCase(), currentBal + amount);
					
					// Attempts the save the file
					try {
						this.getConfig().save(configFile);
						sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.GREEN + args[1] + " now has " + ChatColor.AQUA + this.getConfig().getInt("balances." + args[1].toLowerCase()) + ChatColor.GREEN + " crates.");
					} catch (IOException e) {
						sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "There was an error saving the config file. Contact the owner about this. Error code: GIVE_SAVE");
					}
				}
			} else {
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "You don't have permission to give players crates!" + ChatColor.GREEN + "Did you mean to type /crate pay <username> <amount of crates>?");
			}
			
			return true;
			
		// Removes a certain amount of crates from a player
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (sender.hasPermission("crates.admin"))
			{
				// If the player didn't enter all of the parameters, tell him how to use the command
				if (args.length <= 2)
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "Incorrect usage. Type /crate remove <username> <amount of crates>");
				} else {
					// Removes the specified amount from the player's balance
					currentBal = this.getConfig().getInt("balances." + args[1].toLowerCase());
					this.getConfig().set("balances." + args[1].toLowerCase(), currentBal - Integer.parseInt(args[2]));
					
					// Attempts to save the config file
					try {
						this.getConfig().save(configFile);
						sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.GREEN + sender.getName() + " now has " + ChatColor.AQUA + this.getConfig().getInt("balances." + args[1].toLowerCase()) + ChatColor.GREEN + " crates.");
					} catch (IOException e) {
						sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "There was an error saving the config file. Contact the owner about this. Error code: REMOVE_SAVE");
					}
				}
			} else {
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "You don't have permission to remove crates from players!");
			}
			
			return true;
			
		// Pays someone an amount of crates from someone's balance
		} else if (args[0].equalsIgnoreCase("pay")) {
			// If the player didn't enter the correct amount of parameters, tell the player
			if (!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "Only players on the server may use this command! You may not use this command using the console.");
			} else if (args.length <= 1) {
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "Incorrect usage. Type /crate pay <username> <amount of crates>");
			} else {
				int amount;
				// If the player didn't enter an amount, assume 1 crate
				if (args.length == 2)
					amount = 1;
				else
					amount = Integer.parseInt(args[2]);
				
				// Gets the player's current balance
				currentBal = this.getConfig().getInt("balances." + sender.getName().toLowerCase());
				
				// Make sure the player has enough crates to give away
				if (currentBal >= amount)
				{
					// Updates both of the players' balances
					this.getConfig().set("balances." + sender.getName().toLowerCase(), currentBal - amount);
				
					currentBal = this.getConfig().getInt("balances." + args[1].toLowerCase());
					this.getConfig().set("balances." + args[1].toLowerCase(), currentBal + amount);
				
					// Attempts to save the file
					try {
						this.getConfig().save(configFile);
						sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.GREEN + "You have given " + ChatColor.AQUA + amount + ChatColor.GREEN + " crate(s) to " + args[1] + ".");
					} catch (IOException e) {
						sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "There was an error saving the config file. Contact a staff member about this. Error code: PAY_SAVE");
					}
				} else {
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "You don't have enough crates to give away.");
				}
			}
			
			return true;
		
		// Opens a crate, giving that player the random crate
		} else if (args[0].equalsIgnoreCase("open")) {
			// Gets the player's current balance
			currentBal = this.getConfig().getInt("balances." + sender.getName().toLowerCase());
			
			// Checks to make sure that the player has enough crates to open
			if (!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "Only players currently on the server may use this command! You may not use this command using the console.");
			} else if (currentBal >= 1) {
				// Takes a crate away from the player's balance
				this.getConfig().set("balances." + sender.getName().toLowerCase(), currentBal - 1);
				
				// Attempts to save the file, as well as gives the player a random crate with the items inside
				try {
					// Saves the config file
					this.getConfig().save(configFile);
					
					// Creates a random number for the random crate
					Random rand = new Random();
					int randNum = rand.nextInt(this.getConfig().getInt("crates.numberOfCrates")) + 1;
					List<String> list = this.getConfig().getStringList("crates." + randNum + ".items");
					boolean flag = false;
					
					// Loops through the crate, giving all of the items inside of the crate
					for (int i = 0; i < list.size() - 2; i += 3)
					{
						int amount = Integer.parseInt(list.get(i + 1)); // Gets the amount of the item
						
						String itemString = list.get(i);
						int itemID = 0;
						int data = 0;
						flag = false;
						
						// Checks to see if it's a special item ID, with a colon (:) in the ID
						for (int j = 0; j < itemString.length(); j++)
						{
							if (itemString.charAt(j) == ':')
							{
								// Separates the item ID and data
								itemID = Integer.parseInt(itemString.substring(0, j));
								data = Integer.parseInt(itemString.substring(j + 1));
								flag = true;
								break;
							}
						}
						
						ItemStack item = new ItemStack(0); // Defaults to air if something goes wrong
						
						// If there wasn't a colon in the item ID, then the item ID is the whole string
						if (flag == false)
						{
							itemID = Integer.parseInt(itemString);
							item = new ItemStack(itemID, amount); // Creates the item stack of the item without any data
						} else {
							item = new ItemStack(itemID, amount, (byte)data); // Creates the item stack of the item with data
						}
						flag = false;
						
						String line = list.get(i + 2);
						int enchantment = -1;
						int level = -1;
						int temp = 0;
						
						// Checks for any enchantments on the item
						for (int j = 0; j < line.length(); j++)
						{
							if (line.charAt(j) == ':')
							{
								// Gets the enchantment ID, the number before the colon
								enchantment = Integer.parseInt(line.substring(0, j));
								temp = j + 1;
							} else if (line.charAt(j) == ',') {
								// Gets multiple enchantments on an item, separated by a comma
								level = Integer.parseInt(line.substring(temp, j));
								line = line.substring(j + 1);
								j = 0;
								temp = 0;
								
								// If the enchantment ID does not equal -1 and the level isn't 0, then add the unsafe enchantment
								if (enchantment != -1 && level != 0)
									item.addUnsafeEnchantment(Enchantment.getById(enchantment), level);
							}
						}
						
						// After all but one of the enchantments have been added, add the last enchantment
						level = Integer.parseInt(line.substring(temp));
						
						if (enchantment != -1 && level != 0)
							item.addUnsafeEnchantment(Enchantment.getById(enchantment), level);
						
						// Add the item to player's inventory. If there isn't enough room, drops the item on the ground
						flag = false;
						for (ItemStack it : ((Player) sender).getInventory().addItem(item).values())
						{
							flag = true;
							((Player) sender).getWorld().dropItemNaturally(((Player) sender).getLocation(), it);
						}
					}
					
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.GREEN + "You have opened a crate. You have " + ChatColor.AQUA + this.getConfig().getInt("balances." + sender.getName().toLowerCase()) + ChatColor.GREEN + " crates left.");
					if (flag)
						sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.GREEN + "Some items from the crate were dropped near you, because you didn't have enough inventory space.");
					
				} catch (IOException e) {
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "There was an error saving the config file. Contact a staff member about this. Error code: OPEN_SAVE");
				}
			} else {
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "You don't have any crates to open!");
			}
			
			return true;
			 
		// Check your own balance, or someone else's balance
		} else if (args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal")) {
			// If the player didn't enter a username, tell the player his own balance
			if (args.length == 1)
			{
				if (sender instanceof Player)
				{
					currentBal = this.getConfig().getInt("balances." + sender.getName().toLowerCase());
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.GREEN + "You have " + ChatColor.AQUA + currentBal + ChatColor.GREEN + " crates.");
				} else {
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "Only players currently on the server may use this command.");
				}
			// If the player entered a username, check to make sure that the player has permission, and tell him that person's balance
			} else if (args.length == 2) {
				if (sender.hasPermission("crates.admin"))
				{
					currentBal = this.getConfig().getInt("balances." + args[1].toLowerCase());
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.GREEN + args[1] + " has " + ChatColor.AQUA + currentBal + ChatColor.GREEN + " crates.");
				} else {
					sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "You don't have permission to view other players' crate balances!");
				}
			}
			
			return true;
			
		// Reloads the config file
		} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
			if (sender.hasPermission("crates.admin"))
			{
				this.reloadConfig();
				sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.GREEN + "The config file was reloaded.");
			}
			
			return true;
		
		// Unknown command, tell the user that
		} else {
			sender.sendMessage(ChatColor.DARK_AQUA + "[CoalitionCrates] " + ChatColor.RED + "Unknown command. Check to make sure that you spelled it correctly.");
		}
		
		return false;
	}
}
