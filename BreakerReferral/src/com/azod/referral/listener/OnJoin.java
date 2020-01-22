package com.azod.referral.listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.azod.referral.main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class OnJoin implements Listener {
	main plugin = main.getPlugin(main.class);
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if(!haveDemande(e.getPlayer().getUniqueId().toString())) {
			return;
		}
		AllDmd(e.getPlayer().getUniqueId().toString(), e.getPlayer());
	}
	
	public boolean haveDemande(String uuid) {
		try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referral`=? AND `accepted`=FALSE");
			statement.setInt(1, plugin.getId(uuid));			
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
				acceptText.setText("§4[REFUSER]");
				acceptText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/parrain decline " + plugin.getUnique(results.getInt("id"))));
				acceptText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Refuser la demander").create()));
				
				///creation du bouton accepté
				TextComponent declineText = new TextComponent();
				declineText.setText("§a [ACCEPTER] ");
				declineText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/parrain accept " + plugin.getUnique(results.getInt("id"))));
				declineText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Accepter la demande").create()));
				
				///creation text quand demande
				TextComponent broadText = new TextComponent();
				broadText.setText(ChatColor.GOLD + Bukkit.getPlayer(UUID.fromString(plugin.getUnique(results.getInt("id")))).getName() + " souhaite devenir votre filleul(e)");
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
