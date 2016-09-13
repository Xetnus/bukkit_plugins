package me.Xetnus.PlayerReporter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LoginListener implements Listener {
	private final String NAME = ChatColor.DARK_AQUA + "[Reporter] ";
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = (Player) event.getPlayer();
			
		if (player.hasPermission("playerreporter.mod")) {
			int numOpenReports = Config.getNumberOfOpenReports();
			
			if (numOpenReports > 0)
				player.sendMessage(NAME + ChatColor.GREEN + "There are currently " + ChatColor.YELLOW + numOpenReports + ChatColor.GREEN + " open reports that need to be investigated.");
			
			player.sendMessage(ChatColor.GREEN + "Remember to check out some of the recent reports using /rlist!");
		}
	}
}
