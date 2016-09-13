package me.Xetnus.PlayerReporter;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerReporter extends JavaPlugin
{
	private ArrayList<Report> reports = new ArrayList<Report>();
	
	@Override
	public void onEnable()
	{
		//this.getConfig().addDefault("reports", ) 
		getLogger().info("onEnable has been invoked!");
		
		if (!new File(getDataFolder(), "config.yml").exists())
		{
		     saveDefaultConfig();
		}
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("onDisable has been invoked!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Config configFile = new Config();
		
		// If the player typed /report and has permissions to do this
		if ((label.equalsIgnoreCase("rp") || label.equalsIgnoreCase("report") || label.equalsIgnoreCase("rep")) && sender.hasPermission("playerreporter.report"))
		{
			boolean flag = true;
			
			// If the sender didn't enter a username to report, tell that person how to use /report
			if (args.length == 0)
			{
				sender.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "Type /rep <username> <reason>");
				return true;
			}
			else
			{
				
				// Checks to see if the player that was reported is online
				for (Player player : Bukkit.getServer().getOnlinePlayers())
				{
					if (player.getName().equalsIgnoreCase(args[0]))
					{
						flag = false;
						break;
					}
				}
			}
			
			if (flag) // If the player that was reported isn't online, tell the reporter that
			{
				sender.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "The player " + args[0] + " is not online, so that player cannot be reported.");
				return true;
			}
			else // If the sender entered a username
			{
				String reason = "";
				// Inputs the reason for the report
				for (int i = 1; i < args.length; i++)
				{
					reason += args[i] + " ";
				}
				
				reports.add(new Report((Player) sender, args[0], reason)); 
				
				for (int i = reports.size() - 1; i >= 0; i--)
				{
					// If a player is reporting the same person that he has reported before
					if (reports.get(i).getReportedBy().equals(sender.getName()) && reports.get(i).getAccusedWhom().equalsIgnoreCase(args[0]) && reports.size() - 1 != i)
					{
						// If a player is reporting the same player in under an hour, tell him so.
						if (!reports.get(reports.size() - 1).checkCooldownBetweenSameReport(reports.get(i)) && !sender.isOp())
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "You've already reported " + args[0] + " within the last hour! Try again later.");
							reports.remove(reports.size() - 1);
							return true;
						}
						break;
					}
					
					// If a player has reported someone else within the time limit (2 minutes), tell the player
					if (reports.get(i).getReportedBy().equals(sender.getName()) && !sender.isOp() && reports.size() - 1 != i)
					{
						if (!reports.get(reports.size() - 1).checkCooldownBetweenAllReports(reports.get(i)))
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "You can only report players every 2 minutes. Your report wasn't sent. Try again later.");
							reports.remove(reports.size() - 1);
							return true;
						}
					}
				}
				
				// Saves this report to the config
				/*try
				{
					configFile.addNew((Player) sender, args[0], reason);
					/*if (configFile.exists())
						configFile.addNew((Player) sender, args[0], reason);
				}
				catch(Exception e)
				{
					sender.sendMessage("There was an error adding your report.");
				}*/
				
				int counter = 0;
				// Tells any staff online who reported who for what reason
				for (Player player : Bukkit.getServer().getOnlinePlayers())
				{
					
					if (player.hasPermission("playerreporter.mod") || player.hasPermission("playerreporter.*") || player.isOp()) // If this player has permission to manage reports
					{
						counter++;
						if (args.length == 1) // If the sender didn't enter a reason to report
						{
							player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.BLUE + sender.getName() + ChatColor.GREEN + " has reported " + ChatColor.RED + args[0] + ".");
						}
						else // If the sender entered a reason, tell the user so
						{
							String message = ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.BLUE + sender.getName() + ChatColor.GREEN + " has reported " + ChatColor.RED + args[0] + ChatColor.GREEN + " for " + ChatColor.RED + reason.substring(0, reason.length() - 1) + ".";
							player.sendMessage(message);
						}
					}							
				}
				
				// If there isn't any staff online, tell the sender that
				if (counter == 0)
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.GREEN + "" + ChatColor.ITALIC +  "A staff member isn't online at the moment, but your report was sent anyway.");
				}	
				
				// If the person reported himself, tell him so
				if (sender.getName().equalsIgnoreCase(args[0]))
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.GREEN + "You've successfully reported yourself! Nice going...");
				}
				else // Otherwise, tell the reporter that you've successfully reported the player
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.GREEN + "You've successfully reported " + args[0] + "! Thanks for the report!");
				}
				
				return true;
			}
		}
		else if ((label.equalsIgnoreCase("rlist") || label.equalsIgnoreCase("reportslist") || label.equalsIgnoreCase("replist")) && sender.hasPermission("playerreporter.mod"))
		{
			ArrayList<String> report;
			
			try
			{
				for (int i = 1; i <= 2; i++)
				{
					report = new ArrayList<String>(configFile.getReport(i));
					
					if (report.get(3).equals("true"))
						sender.sendMessage(ChatColor.RED + "" + i + ". " + report.get(0) + " reported " + report.get(1) + " for " + report.get(2) + ".");
					else if (report.get(3).equals("false"))
						sender.sendMessage(ChatColor.GREEN + "" + i + ". " + report.get(0) + " reported " + report.get(1) + " for " + report.get(2) + ".");
				}
			}
			catch (Exception e)
			{
				sender.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.GREEN + "There was a problem receiving the reports.");
			}
		}
		
		return false;
	}
}
