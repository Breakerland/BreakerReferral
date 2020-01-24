package com.azod.referral.cmd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

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
	
	private List<UUID> child = new ArrayList<UUID>();
	
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
			Player p = (Player) sender;
			if(!plugin.haveChild(p.getUniqueId().toString())) {
				p.sendMessage(ChatColor.RED+plugin.getConfig().getString("data.nof"));
				return false;
			}
			child.clear();
			getChild(p.getUniqueId().toString());
			Inventory inventory = Bukkit.createInventory(p, Math.min(6, (int) Math.ceil(child.size() / 9D)) * 9, "§9Vos filleuls");
			for(UUID owner : child) {
				ItemStack skull = new ItemStack(Material.PLAYER_HEAD,1);
				SkullMeta meta = (SkullMeta) skull.getItemMeta();
				meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
				String name = Bukkit.getOfflinePlayer(owner).getName();
				meta.setDisplayName(name);
				skull.setItemMeta(meta);
				inventory.addItem(skull);
			}
			Integer i = child.size();
			ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE,1);
			ItemMeta gmeta = glass.getItemMeta();
			gmeta.setDisplayName(ChatColor.RED+"Rien à voir ici !");
			glass.setItemMeta(gmeta);
			while(i < inventory.getSize()) {
				inventory.setItem(i, glass);
				i++;
			}
			child.clear();
			plugin.players.add(p.getUniqueId());
			
			p.openInventory(inventory);
			return false;
		}
		else if(args[0].equalsIgnoreCase("accept")) {
			if(args.length < 2) {
				return false;
			}
			Player p = (Player) sender;
			
			if(args[1].length()<=16) {	
				OfflinePlayer f = Bukkit.getOfflinePlayer(args[1]);
				if(!plugin.hasAsk(f.getUniqueId().toString(), p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.RED+plugin.getConfig().getString("data.noask"));
					return false;
				}
				else if(plugin.isRef(f.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.RED+plugin.getConfig().getString("data.fhasp"));
					plugin.delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					return false;
				}
				else if(plugin.haveMax(p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.RED+plugin.getConfig().getString("data.msgmax"));
					plugin.delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					return false;
				}
				else {
					plugin.accRef(f.getUniqueId().toString(), p.getUniqueId().toString());
					
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.GREEN+plugin.getConfig().getString("data.accf"));
					if(Bukkit.getOnlinePlayers().contains(f)) {
						Bukkit.getPlayer(f.getUniqueId()).sendMessage(ChatColor.GOLD+p.getName()+" "+ChatColor.GREEN+plugin.getConfig().getString("data.accp"));
						
					}
					return false;
				}
						
			}
			else {
				OfflinePlayer f = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
				if(!plugin.hasAsk(f.getUniqueId().toString(), p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+ f.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.noask"));
					return false;
				}
				else if(plugin.isRef(f.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+f.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.fhasp"));
					plugin.delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					return false;
				}
				else if(plugin.haveMax(p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+f.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.msgmax"));
					plugin.delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					return false;
				}
				else {
					plugin.accRef(f.getUniqueId().toString(), p.getUniqueId().toString());
					
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
				if(!plugin.hasAsk(f.getUniqueId().toString(), p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+args[1]+" "+ChatColor.RED+plugin.getConfig().getString("data.noask"));
					return false;
				}
				else {
					plugin.delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
					p.sendMessage(ChatColor.RED+plugin.getConfig().getString("data.reff")+" "+ChatColor.GOLD+f.getName());
					if(Bukkit.getOnlinePlayers().contains(f)) {
						Bukkit.getPlayer(f.getUniqueId()).sendMessage(ChatColor.GOLD+p.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.refp"));
					}
					return false;
				}
			}
			else {
				OfflinePlayer f = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
				if(!plugin.hasAsk(f.getUniqueId().toString(), p.getUniqueId().toString())) {
					p.sendMessage(ChatColor.GOLD+f.getName()+" "+ChatColor.RED+plugin.getConfig().getString("data.noask"));
					return false;
				}
				else {
					plugin.delRef(f.getUniqueId().toString(),p.getUniqueId().toString());
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
				if(!plugin.DataExist(player.getUniqueId().toString())) {
					plugin.addData(Bukkit.getOfflinePlayer(player.getUniqueId()));
				}
				if(!plugin.DataExist(receiver.getUniqueId().toString())) {
					plugin.addData(receiver);
				}
				if(plugin.isRef(player.getUniqueId().toString())) {
					player.sendMessage(ChatColor.RED + plugin.getConfig().getString("data.isref"));
					return false;
				}
				else if(plugin.haveMax(receiver.getUniqueId().toString())) {
					player.sendMessage(ChatColor.GOLD+ receiver.getName()+" " +ChatColor.RED +plugin.getConfig().getString("data.msgmax"));
					return false;
				}
				else if(plugin.hasAsk(player.getUniqueId().toString(), receiver.getUniqueId().toString())) {
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
				Bukkit.getScheduler ().runTaskLater(plugin, () -> plugin.addRef(player.getUniqueId().toString(),receiver.getUniqueId().toString()), 20);
				
			}
		
		return false;
	}
	
	public void getChild(String r) {
	try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referral`='"+plugin.getId(r)+"' AND `accepted`='1'");
			
			
			ResultSet results = statement.executeQuery();
			while(results.next()) {
				UUID player = UUID.fromString(plugin.getUnique(results.getInt("referred")));
				child.add(player);
				return;
			}
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN +"Player don't have child");
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		return;
	
	}

	
	
}
