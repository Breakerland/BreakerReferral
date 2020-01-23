package com.azod.referral.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.azod.referral.main;

public class OnInte implements Listener {
	main plugin = main.getPlugin(main.class);
	
	public OnInte(main main) {
		this.plugin = main;
	}
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInterract(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(!plugin.players.contains(p.getUniqueId())) {
			return;
		}
		e.setCancelled(true);
	}
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onClose(InventoryCloseEvent e) {
		plugin.players.remove(e.getPlayer().getUniqueId());
	}
}
