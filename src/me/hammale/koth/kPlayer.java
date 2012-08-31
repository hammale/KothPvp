package me.hammale.koth;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class kPlayer {
	
	Player player;
	koth plugin;
	int id, time;
	boolean active;
	public int kills, rank;
	public double score;
	
	public kPlayer(koth plugin, Player p){
		this.plugin = plugin;
		this.player = p;
	}
	
	public void start(){
		active = true;
		run();
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public void stop(){
		active = false;
		plugin.getServer().getScheduler().cancelTask(id);
		plugin.active.remove(player.getName());
	}
	
	public void writeKills(){
		score = (kills/(float)plugin.totalKills) + (time/(float)plugin.totalTime);
		plugin.scores.put(player.getName(), score);
		try{
			Statement s = plugin.conn.createStatement ();
			s.executeQuery ("SELECT * FROM `board` WHERE `ign` = '" + player.getName() + "'");
			ResultSet rs = s.getResultSet();
			
			DecimalFormat formatter = new DecimalFormat("#00.###");
			String tmptime = formatter.format(time / 3600) + ":" + formatter.format((time % 3600) / 60) + ":" + formatter.format(time % 60);
			if(rs.next()){
				if(plugin.getRank(score) != rs.getInt("rank")){
					plugin.syncRank();
				}
				s.executeUpdate("UPDATE `board` SET `kills` = '" + kills + "' WHERE `board`.`ign`='" + player.getName() + "'");
				s.executeUpdate("UPDATE `board` SET `rank` = '" + plugin.getRank(player) + "' WHERE `board`.`ign`='" + player.getName() + "'");
			}else{
				s.executeUpdate("INSERT INTO `koth`.`board` (`id`, `ign`, `rank`, `kills`, `time`) VALUES (NULL, '" + player.getName() + "', '" + plugin.getRank(player) + "', '" + kills + "', '" + tmptime + "')");
			}
			rs.close();
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		rank = plugin.getRank(player);
	}
	
	public void writeTime(){
		score = (kills/(float)plugin.totalKills) + (time/(float)plugin.totalTime);
		plugin.scores.put(player.getName(), score);
		try{
			Statement s = plugin.conn.createStatement ();
			s.executeQuery ("SELECT * FROM `board` WHERE `ign` = '" + player.getName() + "'");
			ResultSet rs = s.getResultSet();
			
			DecimalFormat formatter = new DecimalFormat("#00.###");			
			String tmptime = formatter.format(time / 3600) + ":" + formatter.format((time % 3600) / 60) + ":" + formatter.format(time % 60);
			if(rs.next()){
				System.out.println(plugin.getRank(player) + ", " + rs.getInt("rank"));
				if(plugin.getRank(score) != rs.getInt("rank")){
					plugin.syncRank();
				}
				s.executeUpdate("UPDATE `board` SET `time` = '" + tmptime + "' WHERE `board`.`ign`='" + player.getName() + "'");
				s.executeUpdate("UPDATE `board` SET `rank` = '" + plugin.getRank(player) + "' WHERE `board`.`ign`='" + player.getName() + "'");
			}else{
				s.executeUpdate("INSERT INTO `koth`.`board` (`id`, `ign`, `rank`, `kills`, `time`) VALUES (NULL, '" + player.getName() + "', '" + plugin.getRank(player) + "', '" + kills + "', '" + tmptime + "')");
			}
			rs.close();
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		rank = plugin.getRank(player);
	}
	
	private void run(){
		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			   public void run() {
			       if(active
			    		    && player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getX() == plugin.blockLoc.getBlockX()
							&& player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getY() == plugin.blockLoc.getBlockY()
							&& player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getZ() == plugin.blockLoc.getBlockZ()){
			    	   plugin.getServer().dispatchCommand(
								plugin.getServer().getConsoleSender(),
								"eco give " + player.getName() + " 2000");
			    	   player.sendMessage(ChatColor.GREEN + "[KothPVP] You earned $2,000!");
			    	   time += 15;
			    	   plugin.totalTime += 15;
			    	   writeTime();
			       }else{
			    	   stop();
			       }
			   }
		}, 300L, 300L);
	}	
}