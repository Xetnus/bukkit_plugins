package me.Xetnus.PlayerReporter;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Config {
    private static PlayerReporter plugin;
    private Player player;
    private File reportsFile;
    
    public Config(PlayerReporter p) {
    	plugin = p;
    	reportsFile = new File("plugins/PlayerReporter/config.yml");
    }
    
    // Checks to see if the reports file exists
    public boolean exists() { 
    	if(!reportsFile.exists()) {
		    try {
		    	reportsFile.createNewFile(); 
		    } catch(Exception e) { 
		    	player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "A new reports file could not be created. Tell an admin about this so that it can be fixed. Error: CREATECONFIG");
		    	return false;
		    }
		}
    	return true;
    }
    
    // Adds a new report to the reports file
    public boolean addNew(Player reportedBy, String accusedWhom, String reason) {    	
    	try {
    		for (int i = 10; i > 1; i--) {
    			plugin.getConfig().set("reports." + i + ".reportedBy", plugin.getConfig().getString("reports." + (i - 1) + ".reportedBy"));
    			plugin.getConfig().set("reports." + i + ".accusedWhom", plugin.getConfig().getString("reports." + (i - 1) + ".accusedWhom"));
    			plugin.getConfig().set("reports." + i + ".reason", plugin.getConfig().getString("reports." + (i - 1) + ".reason"));
    			plugin.getConfig().set("reports." + i + ".closed", plugin.getConfig().getBoolean("reports." + (i - 1) + ".closed"));
    		}
    	
    		plugin.getConfig().set("reports.1.reportedBy", reportedBy.getName());
    		plugin.getConfig().set("reports.1.accusedWhom", accusedWhom);
    		plugin.getConfig().set("reports.1.reason", reason);
    		plugin.getConfig().set("reports.1.closed", false);
    	} catch (Exception e) {
    		player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "The player could not be added to the reports file. Tell an admin about this so it can be fixed. Error: ADDNEWPLAYER");
    		return false;
    	}
    	
    	try {
    		plugin.getConfig().save(reportsFile); 
		} catch (Exception e) {
			player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "The reports file could not be saved. Tell an admin about this so it can be fixed. Error: ADDNEWSAVE");
			return false;
		}
    	
    	return true;
    }    
    
    public static int getNumberOfOpenReports() {
    	int index = 1, numOpen = 0;
    	
    	while (!plugin.getConfig().get("reports." + index + ".reportedBy").toString().equals("blank")) {
    		if (plugin.getConfig().get("reports." + index + ".closed").toString().equals("false"))
    			numOpen++;
    		
    		index++;
    	}
    	
    	return numOpen;
    }
    
    // Returns the report at the specified location
    public ArrayList<String> getReport(int index) {
    	ArrayList<String> report = new ArrayList<String>();
    	
    	try {
    		report.add(plugin.getConfig().get("reports." + index + ".reportedBy").toString());
    		report.add(plugin.getConfig().get("reports." + index + ".accusedWhom").toString());
    		report.add(plugin.getConfig().get("reports." + index + ".reason").toString());
    		report.add(plugin.getConfig().get("reports." + index + ".closed").toString());
    	} catch (Exception e) {
    		player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "There was an error receiving the reports. Tell an admin about this so it can be fixed. Error: GETREPORT");
    	}
    	
    	return report;
    }
    
    // Closes a report
    public boolean closeReport(int index) {
    	// If the report was already closed, return false, otherwise, close the report
    	if (!plugin.getConfig().getBoolean("reports." + index + ".closed"))
    			plugin.getConfig().set("reports." + index + ".closed", true);
    	else
    		return false;
    	
    	// Try to save the config file
    	try {
    		plugin.getConfig().save(reportsFile); 
		} catch (Exception e) {
			player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "The reports file could not be saved. Tell an admin about this so it can be fixed. Error: CLOSESAVE");
		}
    	
    	return true;
    }
    
    // Opens a report
    public boolean openReport(int index) {
    	// If the report was already open, return false, otherwise, open the report
    	if (!plugin.getConfig().getBoolean("reports." + index + ".closed"))
    		plugin.getConfig().set("reports." + index + ".closed", false);
    	else
    		return false;
    	
    	// Try to save the config file
    	try {
    		plugin.getConfig().save(reportsFile);
		} catch (Exception e) {
			player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "The reports file could not be saved. Tell an admin about this so it can be fixed. Error: OPENSAVE");
		}
    	
    	return true;
    }
    
    // Resets the entire reports file
    public void reset() {
    	try {
    		for (int i = 1; i <= 10; i++) {
    			plugin.getConfig().set("reports." + i + ".reportedBy", "blank");
    			plugin.getConfig().set("reports." + i + ".accusedWhom", "blank");
    			plugin.getConfig().set("reports." + i + ".reason", "blank");
    			plugin.getConfig().set("reports." + i + ".closed", false);
			}
    	} catch (Exception e) {
			player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "The reports file could not be reset. Tell an admin about this so it can be fixed. Error: RESET");
		}
    	
    	try {
    		plugin.getConfig().save(reportsFile); 
    		player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.GREEN + "The reports file has been entirely reset.");
		} catch (Exception e) {
			player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "The reports file could not be saved. Tell an admin about this so it can be fixed. Error: RESETSAVE");
		}
    }
    
    // Deletes the specified report
    public void deleteReport(int index) {
    	for (int i = index; i < 10; i++) {
    		plugin.getConfig().set("reports." + i + ".reportedBy", plugin.getConfig().getString("reports." + (i + 1) + ".reportedBy"));
    		plugin.getConfig().set("reports." + i + ".accusedWhom", plugin.getConfig().getString("reports." + (i + 1) + ".accusedWhom"));
    		plugin.getConfig().set("reports." + i + ".reason", plugin.getConfig().getString("reports." + (i + 1) + ".reason"));
			plugin.getConfig().set("reports." + i + ".closed", plugin.getConfig().getBoolean("reports." + (i + 1) + ".closed"));
    	}
		
    	plugin.getConfig().set("reports." + 10 + ".reportedBy", "blank");
    	plugin.getConfig().set("reports." + 10 + ".accusedWhom", "blank");
    	plugin.getConfig().set("reports." + 10 + ".reason", "blank");
    	plugin.getConfig().set("reports." + 10 + ".closed", false);
		
		try {
    		plugin.getConfig().save(reportsFile); 
    		player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.GREEN + "Report #" + index + " has been deleted.");
		} catch (Exception e) {
			player.sendMessage(ChatColor.DARK_AQUA + "[Reporter] " + ChatColor.RED + "The reports file could not be saved. Tell an admin about this so it can be fixed. Error: DELETESAVE");
		}
    }
}