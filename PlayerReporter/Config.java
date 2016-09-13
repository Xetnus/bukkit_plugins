package me.Xetnus.PlayerReporter;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Config
{
    private static PlayerReporter plugin;
    private String accusedWhom, reportedBy, reason;
    private Player player;
    //new File("plugins/PlayerReporter").mkdir();
    File reportsFile;
    
    public Config()
    {
    	reportsFile = new File("plugins/PlayerReporter/config.yml");
    }
    
    public boolean exists()
    { 
    	if(!reportsFile.exists()) 
    	{
		    try 
		    {
		    	reportsFile.createNewFile(); 
		    }
		    catch(Exception e)
		    { 
		    	player.sendMessage("The reports file could not be created! Tell a staff member about this so that it can be fixed.");
		    	return false;
		    }
		}
    	return true;
    }
    
    public boolean addNew(Player reportedBy, String accusedWhom, String reason)
    {
    	this.accusedWhom = accusedWhom;
    	this.reportedBy = reportedBy.getName();
    	this.reason = reason;
    	player = reportedBy;
    	
    	//FileConfiguration data = YamlConfiguration.loadConfiguration(reportsFile);
    	
    	for (int i = 2; i > 1; i--)
    	{
    		plugin.getConfig().set("reports." + i + ".reportedBy", plugin.getConfig().get("reports." + (i - 1) + ".reportedBy"));
    		plugin.getConfig().set("reports." + i + ".accusedWhom", plugin.getConfig().get("reports." + (i - 1) + ".accusedWhom"));
    		plugin.getConfig().set("reports." + i + ".reason", plugin.getConfig().get("reports." + (i - 1) + ".reason"));
    		plugin.getConfig().set("reports." + i + ".closed", plugin.getConfig().get("reports." + (i - 1) + ".closed"));
    	}
    	
    	plugin.getConfig().set("reports.1.reportedBy", reportedBy);
    	plugin.getConfig().set("reports.1.accusedWhom", accusedWhom);
    	plugin.getConfig().set("reports.1.reason", reason);
    	plugin.getConfig().set("reports.1.closed", false);
    	
    	try
		{
    		plugin.getConfig().save(reportsFile); 
		}
		catch(Exception e)
		{
			player.sendMessage("The reports file could not be saved! Tell a staff member about this so that it can be fixed.");
			return false;
		}
    	
    	return true;
    }    
    
    public ArrayList<String> getReport(int index)
    {
    	ArrayList<String> report = new ArrayList<String>();
    	//FileConfiguration data = YamlConfiguration.loadConfiguration(reportsFile);
    	
    	try
    	{
    		report.add(plugin.getConfig().get("reports." + index + ".reportedBy").toString());
    		report.add(plugin.getConfig().get("reports." + index + ".accusedWhom").toString());
    		report.add(plugin.getConfig().get("reports." + index + ".reason").toString());
    		report.add(plugin.getConfig().get("reports." + index + ".closed").toString());
    	}
    	catch (Exception e)
    	{
    		player.sendMessage("There was an error receiving the reports.");
    	}
    	
    	return report;
    }
    
    public boolean closeReport(int index)
    {
    	FileConfiguration data = YamlConfiguration.loadConfiguration(reportsFile);
    	plugin.getConfig().set("reports." + index + ".closed", true);
    	return true;
    }
}