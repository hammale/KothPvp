package me.hammale.koth;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class listener implements Listener {
	
	koth plugin;
	
	public listener(koth plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent e){
		kPlayer tp = plugin.getPlayer(e.getPlayer());
		tp.stop();
		plugin.players.remove(tp);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		plugin.players.add(new kPlayer(plugin, p));
		try{
			Statement s = plugin.conn.createStatement ();
			s.executeQuery ("SELECT * FROM `board` WHERE `ign` = '" + p.getName() + "'");
			ResultSet rs = s.getResultSet();
			if(rs.next()){
				plugin.getPlayer(p).kills = rs.getInt("kills");		    
				String[] results = rs.getString("time").split( ":\\s*" );
				plugin.getPlayer(p).time = ((Integer.parseInt(Arrays.asList(results).get(0))*60*60)) + ((Integer.parseInt(Arrays.asList(results).get(1))*60)) + ((Integer.parseInt(Arrays.asList(results).get(2))));			
			}			
			double score = (plugin.getPlayer(p).kills/plugin.totalKills) + (plugin.getPlayer(p).time/plugin.totalTime);
			plugin.getPlayer(p).score = score;
			if(!plugin.scores.containsValue(p.getName())){
				plugin.scores.put(p.getName(),score);
			}else{
				while(plugin.scores.values().remove(p.getName()));
				plugin.scores.put(p.getName(),score);
			}
			plugin.getPlayer(p).rank = plugin.getRank(p);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		e.getDrops().clear();
		Player p = e.getEntity();
		if (p.getKiller() instanceof Player){
			Player killer = p.getKiller();
			plugin.getServer().dispatchCommand(
					plugin.getServer().getConsoleSender(),
					"eco give " + killer.getName() + " 50");
			plugin.getPlayer(killer).kills += 1;
			plugin.totalKills += 1;
			plugin.getPlayer(killer).writeKills();
			killer.sendMessage(ChatColor.GREEN + "[KothPVP] You earned $50!");		
		}	
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e){
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if(plugin.blockLoc == null){
			return;
		}
		if(e.getBlock().getX() == plugin.blockLoc.getBlockX()
				&& e.getBlock().getY() == plugin.blockLoc.getBlockY()
				&& e.getBlock().getZ() == plugin.blockLoc.getBlockZ()
				&& e.getPlayer().isOp()){
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "[KothPVP] You can't break this block. Use '/pvp set' instead.");
			return;
		}
		if(!e.getPlayer().isOp()){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if(!e.getPlayer().isOp()){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if(plugin.blockLoc == null){
			return;
		}
		if(plugin.getServer().getOnlinePlayers().length >= 2 
				&& e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getX() == plugin.blockLoc.getBlockX()
				&& e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getY() == plugin.blockLoc.getBlockY()
				&& e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getZ() == plugin.blockLoc.getBlockZ()){
			if(!plugin.active.contains(e.getPlayer().getName())){
				plugin.active.add(e.getPlayer().getName());
				plugin.getPlayer(e.getPlayer()).start();
			}
		}
	}
	
}
