package me.xetnus.checkplayer;

import java.util.Calendar;

import org.bukkit.entity.Player;

/**
 * This class represents a player who has gone into stealth mode.
 * @author Grant Grubbs
 */
public class StealthyPlayer {
	private long mostRecentAttack;
	private Player player;
	
	/**
	 * Constructor to create a new StealthyPlayer object.
	 * @param player The player who is going into stealth mode.
	 */
	public StealthyPlayer(Player player) {
		this.player = player;
		mostRecentAttack = 0;
	}
	
	/**
	 * While a player is in stealth mode, that player can only hit another player every 3 seconds.
	 * This method sets the most recent attack done by the player in stealth mode.
	 */
	public void setMostRecentAttack() {
		mostRecentAttack = Calendar.getInstance().getTimeInMillis();
	}
	
	/**
	 * Determines whether or not 3 seconds has passed since the player's last attack on another player.
	 * @return true if the player is able to attack again, false otherwise.
	 */
	public boolean canAttackAgain() {
		if (Calendar.getInstance().getTimeInMillis() - mostRecentAttack >= 3000)
			return true;
		
		return false;
	}
	
	/**
	 * Gets the Player representation of this StealthyPlayer
	 * @return the Player representation of this StealthyPlayer
	 */
	public Player getPlayer() {
		return player;
	}
}
