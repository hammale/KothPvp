package me.hammale.koth;

import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.entity.Player;

public class kPlayer {
	
	Player player;
	koth plugin;
	public int kills;
	
	public kPlayer(koth plugin, Player p){
		this.plugin = plugin;
		this.player = p;
	}
	public Player getPlayer(){
		return player;
	}

	public void writeKills(){
		try{
			Statement s = plugin.conn.createStatement ();
			s.executeQuery ("SELECT * FROM `board` WHERE `ign` = '" + player.getName() + "'");
			ResultSet rs = s.getResultSet();
			
			if(rs.next()){
				s.executeUpdate("UPDATE `board` SET `kills` = '" + kills + "' WHERE `board`.`ign`='" + player.getName() + "'");
			}else{
				s.executeUpdate("INSERT INTO `mc2179`.`board` (`id`, `ign`, `kills`) VALUES (NULL, '" + player.getName() + "', '" + kills + "')");
			}
			rs.close();
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}