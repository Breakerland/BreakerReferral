package com.azod.referral.cmd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import com.azod.referral.main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandParrain implements CommandExecutor, Listener {
	main plugin = main.getPlugin(main.class);
	public CommandParrain(main main) {
		this.plugin = main;
	}
	


	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( !(sender instanceof Player) || ! (args.length > 0)) {
			
			return false;
			
		}
		else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("sponsor.admin")) {
			plugin.reloadConfig();
			Player p = (Player) sender;
			p.sendMessage(ChatColor.GREEN+"[BreakerReferral] >>> Config Reloaded !");
			return false;
		}
		else if(args[0].equalsIgnoreCase("advancement") || cmd.getName().equalsIgnoreCase("padv")) {
			
		}
		else if(args[0].equalsIgnoreCase("accept")) {
			if(args.length < 2) {
				return false;
			}
			Player p = (Player) sender;
			
			if(args[1].length()<=16) {	
				OfflinePlayer f = Bukkit.getOfflinePlayer(args[1]);
				if(!hasAsk(f.getUniqueId().toString(), p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.RED+plugin.getConfig().getString("data.noask"));
					return false;
				}
				else if(isRef(f.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.RED+plugin.getConfig().getString("data.fhasp"));
					delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					return false;
				}
				else if(haveMax(p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.RED+plugin.getConfig().getString("data.msgmax"));
					delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					return false;
				}
				else {
					accRef(f.getUniqueId().toString(), p.getUniqueId().toString());
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.GREEN+plugin.getConfig().getString("data.accf"));
					if(Bukkit.getOnlinePlayers().contains(f)) {
						Bukkit.getPlayer(f.getUniqueId()).sendMessage(ChatColor.GOLD+p.getName()+" "+ChatColor.GREEN+plugin.getConfig().getString("data.accp"));
					}
					return false;
				}
						
			}
			else {
				OfflinePlayer f = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
				if(!hasAsk(f.getUniqueId().toString(), p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+ f.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.noask"));
					return false;
				}
				else if(isRef(f.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+f.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.fhasp"));
					delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					return false;
				}
				else if(haveMax(p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+f.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.msgmax"));
					delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					return false;
				}
				else {
					accRef(f.getUniqueId().toString(), p.getUniqueId().toString());
					p.sendMessage(ChatColor.GOLD+f.getName()+" "+ChatColor.GREEN+plugin.getConfig().getString("data.accf"));
					if(Bukkit.getOnlinePlayers().contains(f)) {
						Bukkit.getPlayer(f.getUniqueId()).sendMessage(ChatColor.GOLD+p.getName()+" "+ChatColor.GREEN+plugin.getConfig().getString("data.accp"));
					}
					return false;
				}
			}
		}
		else if(args[0].equalsIgnoreCase("decline")) {
			if(args.length < 2) {
				return false;
			}
			Player p = (Player) sender;
			if(args[1].length() <=16) {
				OfflinePlayer f = Bukkit.getOfflinePlayer(args[1]);
				if(!hasAsk(f.getUniqueId().toString(), p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.RED+plugin.getConfig().getString("data.noask"));
					return false;
				}
				else {
					delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					p.sendMessage(ChatColor.RED+plugin.getConfig().getString("data.reff")+" "+ChatColor.GOLD+f.getName());
					if(Bukkit.getOnlinePlayers().contains(f)) {
						Bukkit.getPlayer(f.getUniqueId()).sendMessage(ChatColor.GOLD+p.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.refp"));
					}
					return false;
				}
			}
			else {
				OfflinePlayer f = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
				if(!hasAsk(f.getUniqueId().toString(), p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+f.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.noask"));
					return false;
				}
				else {
					delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					p.sendMessage(ChatColor.RED+plugin.getConfig().getString("data.reff")+" "+ChatColor.GOLD+f.getName());
					if(Bukkit.getOnlinePlayers().contains(f)) {
						Bukkit.getPlayer(f.getUniqueId()).sendMessage(ChatColor.GOLD+p.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.refp"));
					}
					return false;
				}
			}
		}
		
		OfflinePlayer receiver = Bukkit.getOfflinePlayer(args[0]);
		Player player = (Player) sender;
			if (receiver == null) {
				player.sendMessage("§cLe joueur §6"+ args[0] + "§c n'existe pas !");
			}
			else {
				if(!DataExist(player.getUniqueId().toString())) {
					addData(Bukkit.getOfflinePlayer(player.getUniqueId()));
				}
				if(!DataExist(receiver.getUniqueId().toString())) {
					addData(receiver);
				}
				if(isRef(player.getUniqueId().toString())) {
					player.sendMessage(ChatColor.RED + plugin.getConfig().getString("data.isref"));
					return false;
				}
				else if(haveMax(receiver.getUniqueId().toString())) {
					player.sendMessage(ChatColor.GOLD+ receiver.getName()+" " +ChatColor.RED +plugin.getConfig().getString("data.msgmax"));
					return false;
				}
				else if(hasAsk(player.getUniqueId().toString(), receiver.getUniqueId().toString())) {
					player.sendMessage(ChatColor.GOLD+ receiver.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.hasask"));
					return false;
				}
				if(receiver.isOnline()) {
					Player p = (Player) receiver;
					///creation du bouton accepté
					TextComponent acceptText = new TextComponent();
					acceptText.setText("§4[REFUSER]");
					acceptText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/parrain decline " + player.getUniqueId()));
					acceptText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Refuser la demander").create()));
					
					///creation du bouton accepté
					TextComponent declineText = new TextComponent();
					declineText.setText("§a [ACCEPTER] ");
					declineText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/parrain accept " + player.getUniqueId()));
					declineText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Accepter la demande").create()));
					
					///creation text quand demande
					TextComponent broadText = new TextComponent();
					broadText.setText(ChatColor.GOLD + player.getName() + " souhaite devenir votre filleul(e)");
					///ajout du choice text au broad
					broadText.addExtra(declineText);
					broadText.addExtra(acceptText);
					
					p.spigot().sendMessage(broadText);
				}
				addRef(player.getUniqueId().toString(),receiver.getUniqueId().toString());
			}
		
		return false;
	}
	
	public boolean hasAsk(String d, String r) {
		try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referral`='"+plugin.getId(r)+"' AND `referred`='"+plugin.getId(d)+"' AND `accepted`=FALSE");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return true;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean haveMax(String s) {
		try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT COUNT(*) as rowcount FROM `referral` WHERE `referral`='"+plugin.getId(s)+"' AND `accepted`=TRUE");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				if(rs.getInt("rowcount") == plugin.getConfig().getInt("data.maxref")) {
					return true;
				}				
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean isRef(String s) {
		try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referred`='"+plugin.getId(s)+"' AND `accepted`=TRUE");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return true;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean DataExist(String s) {
		try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `playerdata` WHERE `uuid`='"+s+"'");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return true;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void addData(OfflinePlayer p) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Statement statement = plugin.getConnection().createStatement()){
				statement.executeUpdate("INSERT INTO `playerdata` (uuid, username) VALUES ('"+p.getUniqueId().toString()+"', '"+p.getName()+"')");
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	public void addRef(String d, String r) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Statement statement = plugin.getConnection().createStatement()){
				statement.executeUpdate("INSERT INTO `referral` (referral,referred,accepted) VALUES ('"+plugin.getId(r)+"','"+plugin.getId(d)+"', '0')");
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	public void delRef(String d, String r) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Statement statement = plugin.getConnection().createStatement()){
				statement.executeUpdate("DELETE FROM `referral` WHERE `referral`='"+plugin.getId(r)+"' AND `referred`='"+plugin.getId(d)+"' AND `accepted`=FALSE");
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	public void accRef(String d, String r) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Statement statement = plugin.getConnection().createStatement()){
				statement.executeUpdate("UPDATE `referral` SET `accepted`=TRUE WHERE `referral`='"+plugin.getId(r)+"' AND `referred`='"+plugin.getId(d)+"' AND `accepted`=FALSE");
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
	
}
