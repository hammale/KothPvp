package me.hammale.koth;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class listener implements Listener {
	
	koth plugin;
	Random ran = new Random();
	
	public listener(koth plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent e){
		String command = e.getMessage().replace("/", "");
		if(command.contains(" ")){
			return;
		}
		if(InvHandler.kits.get(command) != null){
			if(InvHandler.kits.get(command).hasPerm()){
				if(e.getPlayer().hasPermission(InvHandler.kits.get(command).getPerm())){
					e.setCancelled(true);
					InvHandler.giveKit(e.getPlayer(), command);
					e.getPlayer().sendMessage(ChatColor.GREEN + "You have been given the " + command + " kit!");
				}
			}else{
				e.setCancelled(true);
				InvHandler.giveKit(e.getPlayer(), command);
				e.getPlayer().sendMessage(ChatColor.GREEN + "You have been given the " + command + " kit!");
			}
		}
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent e){
		kPlayer tp = plugin.getPlayer(e.getPlayer());
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
			}
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
			int amnt = ran.nextInt(170)+30;
			plugin.getServer().dispatchCommand(
					plugin.getServer().getConsoleSender(),
					"eco give " + killer.getName() + " " + amnt);
			plugin.getPlayer(killer).kills += 1;
			plugin.getPlayer(killer).writeKills();
			killer.sendMessage(ChatColor.AQUA + "You have earned " + amnt + " credits for killing player: " + p.getName());
		}	
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e){
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
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
	
}
