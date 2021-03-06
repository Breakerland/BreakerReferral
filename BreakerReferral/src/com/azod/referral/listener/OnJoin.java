package com.azod.referral.listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.azod.referral.main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class OnJoin implements Listener {
	main plugin = main.getPlugin(main.class);
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		String s = e.getPlayer().getUniqueId().toString();
		if(plugin.DataExist(s)) {
			if(haveDemande(s)) {
				AllDmd(s, e.getPlayer());
			}
		}
		if(plugin.isRef(s)) {
			plugin.updateCol(s);
		}
		return;
		
	}
	
	public boolean haveDemande(String uuid) {
		try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referral`='"+plugin.getId(uuid)+"' AND `accepted`=FALSE");		
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.GOLD + Bukkit.getPlayer(UUID.fromString(uuid)).getName() + " have "+results.getRow()+" request(s)");
				return true;
			}			
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void AllDmd(String uuid, Player p) {
		try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referral`=? AND `accepted`=FALSE");
			statement.setInt(1, plugin.getId(uuid));
			
			ResultSet results = statement.executeQuery();
			while(results.next()) {
				TextComponent acceptText = new TextComponent();
				acceptText.setText(ChatColor.RED+"["+plugin.getConfig().getString("data.bouton_refuser")+"]");
				acceptText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/parrain decline " + plugin.getUnique(results.getInt("referred"))));
				acceptText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + plugin.getConfig().getString("data.HOVER_decline")).create()));
				
				///creation du bouton accept�
				TextComponent declineText = new TextComponent();
				declineText.setText(ChatColor.GREEN+"["+plugin.getConfig().getString("data.bouton_accepter")+"] ");
				declineText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/parrain accept " + plugin.getUnique(results.getInt("referred"))));
				declineText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + plugin.getConfig().getString("data.HOVER_accept")).create()));
				
				///creation text quand demande
				TextComponent broadText = new TextComponent();
				broadText.setText(ChatColor.GOLD + Bukkit.getPlayer(UUID.fromString(plugin.getUnique(results.getInt("referred")))).getName() + " "+plugin.getConfig().getString("data.demande"));
				///ajout du choice text au broad
				broadText.addExtra(declineText);
				broadText.addExtra(acceptText);
				
				p.spigot().sendMessage(broadText);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
	
}
