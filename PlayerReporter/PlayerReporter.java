package me.Xetnus.PlayerReporter;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerReporter extends JavaPlugin {
	private ArrayList<Report> reports = new ArrayList<Report>();
	private final String NAME = ChatColor.DARK_AQUA + "[Reporter] ";
	private final LoginListener listener = new LoginListener();
	
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(listener, this);
		
		getLogger().info("onEnable has been invoked!");
		
		if (!new File(getDataFolder(), "config.yml").exists())
		     this.saveDefaultConfig();
	}
	
	@Override
	public void onDisable() {
		getLogger().info("onDisable has been invoked!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Config configFile = new Config(this);
		
		// Check for old reports. If a report is older than an hour, then it removes it from the arraylist reports.
		for (int i = reports.size() - 1; i >= 0; i--)
			if (reports.get(i).isTimeVoid())
				reports.remove(i);
		
		// If the player typed /report and has permissions to do this
		if (label.equalsIgnoreCase("rp") || label.equalsIgnoreCase("report") || label.equalsIgnoreCase("rep")) {
			if (!sender.hasPermission("playerreporter.report")) {
				sender.sendMessage(NAME + ChatColor.RED + "You don't have permission to report a player.");
				return false;
			}
			
			boolean playerNotOnline = true;
			
			// If the sender didn't enter a username to report, tell that person how to use /report
			if (args.length == 0) {
				sender.sendMessage(NAME + ChatColor.RED + "Incorrect usage: " + ChatColor.YELLOW + "Type /rep <username> <reason>");
				return true;
			} else {
				// Checks to see if the player that was reported is online
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					if (player.getName().equalsIgnoreCase(args[0]) || (args[0].equals("test13") && sender.isOp())) {
						if (player.hasPermission("playerreporter.trusted") && !args[0].equals("test13")) {
							sender.sendMessage(NAME + ChatColor.RED + "This player is trusted and cannot be reported.");
							return true;
						}
						
						playerNotOnline = false;
						break;
					}
				}
			}
			
			// If the player that was reported isn't online, tell the reporter that
			if (playerNotOnline) {
				sender.sendMessage(NAME + ChatColor.RED + "The player " + args[0] + " is not online and cannot be reported.");
				return true;
			// If the sender entered a username
			} else {
				String reason = "";
				// Inputs the reason for the report
				for (int i = 1; i < args.length; i++) {
					reason += args[i] + " ";
				}
				
				reports.add(new Report((Player) sender, args[0], reason)); 
				
				for (int i = reports.size() - 1; i >= 0; i--) {
					// If a player is reporting the same person that he has reported before
					if (reports.get(i).getReportedBy().equals(sender.getName()) && reports.get(i).getAccusedWhom().equalsIgnoreCase(args[0]) && reports.size() - 1 != i) {
						// If a player is reporting the same player in under an hour, tell him so.
						if (!reports.get(reports.size() - 1).checkCooldownBetweenSameReport(reports.get(i))) {
							sender.sendMessage(NAME + ChatColor.RED + "You've already reported " + args[0] + " within the last hour! Try again later.");
							reports.remove(reports.size() - 1);
							return true;
						}
						break;
					}
					
					// If a player has reported someone else within the time limit (2 minutes), tell the player
					if (reports.get(i).getReportedBy().equals(sender.getName()) && reports.size() - 1 != i) {
						if (!reports.get(reports.size() - 1).checkCooldownBetweenAllReports(reports.get(i))) {
							sender.sendMessage(NAME + ChatColor.RED + "You can only report players every 2 minutes. Your report wasn't sent. Try again later.");
							reports.remove(reports.size() - 1);
							return true;
						}
					}
				}
				
				// Tells the user that he cannot report himself
				if (args[0].equalsIgnoreCase(sender.getName())) {
					sender.sendMessage(NAME + ChatColor.RED + "You cannot report yourself!");
					reports.remove(reports.size() - 1);
					return true;
				}
			
				// Adds and saves this report to the config
				configFile.addNew((Player) sender, args[0], reason);
				
				boolean staffOnline = false;
				// Tells any staff online who reported who for what reason
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					if (player.hasPermission("playerreporter.mod") || player.isOp()) { // If this player has permission to manage reports
						staffOnline = true;
						if (args.length == 1) { // If the sender didn't enter a reason to report
							player.sendMessage(NAME + ChatColor.BLUE + sender.getName() + ChatColor.GREEN + " has reported " + ChatColor.RED + args[0] + ".");
						} else { // If the sender entered a reason, tell the user so
							String message = NAME + ChatColor.BLUE + sender.getName() + ChatColor.GREEN + " has reported " + ChatColor.RED + args[0] + ChatColor.GREEN + " for " + ChatColor.RED + reason.substring(0, reason.length() - 1) + ".";
							player.sendMessage(message);
						}
					}
				}
				
				// If there isn't any staff online, tell the sender that
				if (!staffOnline)
					sender.sendMessage(NAME + ChatColor.GREEN + "" + ChatColor.ITALIC +  "A staff member isn't online at the moment, but your report was sent anyway.");
				
				sender.sendMessage(NAME + ChatColor.GREEN + "You've successfully reported " + args[0] + "! Thanks for the report!");
				
				return true;
			}
		}
		
		// Lists the last 10 reports
		else if (label.equalsIgnoreCase("rlist") || label.equalsIgnoreCase("reportslist") || label.equalsIgnoreCase("replist")) {
			if (!sender.hasPermission("playerreporter.mod")) {
				sender.sendMessage(NAME + ChatColor.RED + "You don't have permission to view the list of reports.");
				return false;
			}
			
			// If the user wants to reset the reports file, do so
			if (args.length != 0 && args[0].equalsIgnoreCase("reset")) {
				if (sender.hasPermission("playerreporter.admin")) {
					try {
						configFile.reset();
					} catch (Exception e) {
						sender.sendMessage(NAME + ChatColor.RED + "There was an error resetting the reports file, but it should have reset anyway. Try using /rlist now. Error: RESETTEST");
					}
				} else {
					sender.sendMessage(NAME + ChatColor.RED + "You don't have permission to reset the reports file.");
				}
				return true;
			}
			
			ArrayList<String> report;
			
			// Loops through the 10 reports, telling the user the report
			for (int i = 1; i <= 10; i++) {
				report = new ArrayList<String>(configFile.getReport(i));
				
				if (report.get(0).equals("blank")) {
					if (i == 1) // If there aren't any reports to display, tell the user
						sender.sendMessage(NAME + ChatColor.GREEN + "There are no previous reports to display.");
					else
						sender.sendMessage(ChatColor.BLUE + "To close or open a report, use /rclose and /ropen.");
					
					return true;
				} else if (i == 1) {
					sender.sendMessage(NAME + ChatColor.RED + "Reports with strikes through them have been closed.");
					sender.sendMessage(NAME + ChatColor.BLUE + "Here are the previous reports:");
				}
				
				if (report.get(3).equals("true") && !report.get(0).equals("blank") && !report.get(2).isEmpty()) 
					sender.sendMessage(ChatColor.STRIKETHROUGH + "" + ChatColor.RED + "" + i + "." + report.get(0) + " reported " + report.get(1) + " for " + report.get(2).substring(0, report.get(2).length() - 1));
				else if (report.get(3).equals("true") && !report.get(0).equals("blank")) 
					sender.sendMessage(ChatColor.STRIKETHROUGH + "" + ChatColor.RED + "" + i + "." + report.get(0) + " reported " + report.get(1));
				else if (report.get(3).equals("false") && !report.get(0).equals("blank") && !report.get(2).isEmpty())
					sender.sendMessage(ChatColor.RED + "" + i + "." + report.get(0) + " reported " + report.get(1) + " for " + report.get(2).substring(0, report.get(2).length() - 1));
				else if (report.get(3).equals("false") && !report.get(0).equals("blank"))
					sender.sendMessage(ChatColor.RED + "" + i + "." + report.get(0) + " reported " + report.get(1));
			}
			
			return true;
		}
		
		// Closes a report
		else if (label.equalsIgnoreCase("rclose")) {
			if (!sender.hasPermission("playerreporter.mod")) {
				sender.sendMessage(NAME + ChatColor.RED + "You don't have permission to close reports.");
				return false;
			}
			
			// If the user didn't type a parameter, tell the user how to use the command
			if (args.length == 0) {
				sender.sendMessage(NAME + ChatColor.RED + "You must supply the number of the report to close it.");
				sender.sendMessage(ChatColor.RED + "Type /rclose <number of report>");
				return true;
			}
			
			// Tries to close the report
			try {
				// Checks to see if the report is already closed, tells the user the result of it
				if (configFile.closeReport(Integer.parseInt(args[0])))
					sender.sendMessage(NAME + ChatColor.GREEN + "You've closed report #" + Integer.parseInt(args[0]) + ".");
				else
					sender.sendMessage(NAME + ChatColor.RED + "That report is already closed!");
			} catch (Exception e) { 
				sender.sendMessage(NAME + ChatColor.RED + "That report could not be closed. Check to make sure that you are inputting an integer for the report index.");
			}
			
			return true;
		}
		
		// Open a report
		else if (label.equalsIgnoreCase("ropen")) {
			if (!sender.hasPermission("playerreporter.mod")) {
				sender.sendMessage(NAME + ChatColor.RED + "You don't have permission to open reports.");
				return false;
			}
			
			// If the user didn't type a parameter, tell the user how to use the command
			if (args.length == 0) {
				sender.sendMessage(NAME + ChatColor.RED + "You must supply the number of the report to open it.");
				sender.sendMessage(ChatColor.RED + "Type /ropen <number of report>");
				return true;
			}
					
			// Tries to open the report
			try {
				// Checks to see if the report is already open, tells the user the result of it
				if (configFile.openReport(Integer.parseInt(args[0])))
					sender.sendMessage(NAME + ChatColor.GREEN + "You've opened report #" + Integer.parseInt(args[0]) + ".");
				else
					sender.sendMessage(NAME + ChatColor.RED + "That report is already open!");
			} catch (Exception e) {
				sender.sendMessage(NAME + ChatColor.RED + "That report could not be opened. Check to make sure that you are inputting an integer for the report index.");
			}
					
			return true;
		}
		
		else if (label.equalsIgnoreCase("rdel") || label.equalsIgnoreCase("rdelete")) {
			if (!sender.hasPermission("playerreporter.admin")) {
				sender.sendMessage(NAME + ChatColor.RED + "You don't have permission to delete reports.");
				return false;
			}
			
			if (args.length == 0) {
				sender.sendMessage(NAME + ChatColor.RED + "You must supply the number of the report to delete it.");
				sender.sendMessage(ChatColor.RED + "Type /rdel <number of report>");
			} else {
				try {
					configFile.deleteReport(Integer.parseInt(args[0]));
				} catch (Exception e) {
					sender.sendMessage(NAME + ChatColor.RED + "There was an error deleting that report. However, it should have been deleted anyway. Type /rlist to check. Error: DELETE");
				}
			}
		
			return true;
		}
		
		// Tells the user about the plugin
		else if (label.equalsIgnoreCase("rabout")) {
			sender.sendMessage(NAME + ChatColor.BLUE + "This plugin was coded by Xetnus for the server FazMC.net.");
		}
		
		// Tells the user all of the available commands for this plugin
		else if (label.equalsIgnoreCase("PlayerReporter") || label.equalsIgnoreCase("pr") || label.equalsIgnoreCase("reporter")) {
			if (!sender.hasPermission("playerreporter.mod")) {
				sender.sendMessage(NAME + ChatColor.RED + "You don't have permission to view the list of commands.");
				return false;
			}
			
			sender.sendMessage(NAME + ChatColor.GREEN + "Here are a list of commands:");
			sender.sendMessage(ChatColor.AQUA + "/report <player>  " + ChatColor.BLUE + "- reports the specified player");
			sender.sendMessage(ChatColor.AQUA + "/rlist  " + ChatColor.BLUE + "- views the previous 10 reported players");
			sender.sendMessage(ChatColor.AQUA + "/rlist reset  " + ChatColor.BLUE + "- resets the reports file and all reports (admin use only)");
			sender.sendMessage(ChatColor.AQUA + "/rclose <number of report>  " + ChatColor.BLUE + "- closes the specified report");
			sender.sendMessage(ChatColor.AQUA + "/ropen <number of report>  " + ChatColor.BLUE + "- opens the specified report");
			sender.sendMessage(ChatColor.AQUA + "/rdel <number of report> " + ChatColor.BLUE + "- deletes the specified report (admin use only)");
			sender.sendMessage(ChatColor.AQUA + "/rabout  " + ChatColor.BLUE + "- about the plugin");
		}
		
		return false;
	}
}
