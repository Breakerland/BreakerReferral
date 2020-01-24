package com.azod.referral.listener;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.azod.referral.main;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;



public class OnInte implements Listener {
	main plugin = main.getPlugin(main.class);
	private HashMap<String, String>plcl = new HashMap<String,String>();
	private HashMap<Integer, String>madv = new HashMap<Integer,String>();
	public OnInte(main main) {
		this.plugin = main;
	}
	@SuppressWarnings({ "static-access", "deprecation" })
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInterract(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(plugin.players.contains(p.getUniqueId())) {
			if(e.getCurrentItem() == null) {
				e.setCancelled(true);
				return;
			}
			ItemStack i = e.getCurrentItem();			
			if(!(i.getType().toString().equalsIgnoreCase("PLAYER_HEAD"))) {
				e.setCancelled(true);
				return;
			}
			ItemMeta im = i.getItemMeta();
			String[] one = im.toString().split("display-name=");
			String[] two = one[1].split(",");
			String pname = two[0];
			e.setCancelled(true);
			plcl.put(p.getUniqueId().toString(), Bukkit.getOfflinePlayer(pname).getUniqueId().toString());
			Inventory rew = Bukkit.createInventory(p, Math.min(6, (int) Math.ceil(plugin.advlist.size() / 9D)) * 9, "§9Récompenses");
			Integer it = 0;
			for(String s : plugin.advlist) {
				Advancement a = plugin.getAdvancement(s);				
				if(CheckRec(Bukkit.getOfflinePlayer(pname).getUniqueId().toString(), p.getUniqueId().toString(), s)) {
					rew.setItem(it, getGSkull("http://textures.minecraft.net/texture/cb3c17b2ddec2b726d717a8b5a7d68b7772fbd9c09a3ff4bd2a412d24ccd491c", a,s));
				}
				else if(CheckAdv(p.getUniqueId().toString(), s)) {
					rew.setItem(it, getESkull("http://textures.minecraft.net/texture/c0e9e66de3631f412b0298c4f0b6ed5c41468c158641210f5c850fa4ed02b2c4", a,s));
				}
				
				else{
					rew.setItem(it, getSkull("http://textures.minecraft.net/texture/6fd452870d493718eb63647ad80e00f50b774601cb067775f90fc1eaada8fcef", a,s));
				}
				madv.put(it, s);
				it++;
			}
			Integer iter = plugin.advlist.size();
			ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE,1);
			ItemMeta gmeta = glass.getItemMeta();
			gmeta.setDisplayName(ChatColor.RED+"Rien à voir ici !");
			glass.setItemMeta(gmeta);
			while(iter < rew.getSize()) {
				rew.setItem(iter, glass);
				iter++;
			}
			p.openInventory(rew);
			plugin.rewplayer.add(p.getUniqueId());
		}
		else if(plugin.rewplayer.contains(p.getUniqueId())) {
			if(e.getCurrentItem() == null) {
				e.setCancelled(true);
				return;
			}
			ItemStack i = e.getCurrentItem();
			if(!(i.getType().toString().equalsIgnoreCase("PLAYER_HEAD"))) {
				e.setCancelled(true);
				return;
			}
			if(i.containsEnchantment(Enchantment.MENDING)) {
				//Peut etre récupérer
				e.setCancelled(true);
				String f = plcl.get(p.getUniqueId().toString());
				Double d = new Double(plugin.getConfig().getInt("rewards."+madv.get(e.getSlot())));
				plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()),d);
				updateRew(p.getUniqueId().toString(), f, madv.get(e.getSlot()).replace("minecraft:", ""));
				p.sendMessage("Félicitation tu as reçu "+d+plugin.getConfig().getString("data.moneyb"));
				e.setCancelled(true);
				plcl.remove(p.getUniqueId().toString(), Bukkit.getOfflinePlayer(f).getUniqueId().toString());
				p.closeInventory();
				p.performCommand("parrain advancement");
				return;
			}
			else if(i.containsEnchantment(Enchantment.DURABILITY)){
				//Déjà récupéré
				e.setCancelled(true);
				p.sendMessage("deja recup");
				return;
			}
			else if(i.containsEnchantment(Enchantment.FIRE_ASPECT)) {
				//Pas finis
				e.setCancelled(true);
				p.sendMessage("peut pas recup");
				return;
			}
		}
		else return;
	
	}
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onClose(InventoryCloseEvent e) {
		if(plugin.players.contains(e.getPlayer().getUniqueId())) {
			plugin.players.remove(e.getPlayer().getUniqueId());
			return;
		}
		else if(plugin.rewplayer.contains(e.getPlayer().getUniqueId())) {
			plugin.rewplayer.remove(e.getPlayer().getUniqueId());
			return;
		}
	}
    public ItemStack getSkull(String url, Advancement a, String s) {
        @SuppressWarnings("deprecation")
		ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
        if(url.isEmpty())return head;
       
       
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        String[] name = s.split("/");
        String nem = name[1].replace("_", " ");
        headMeta.setDisplayName(nem);
        List<String> lore = new ArrayList<String>();
        Integer r = plugin.getConfig().getInt("rewards."+s);
        lore.add("§6Récompense: "+r.toString()+plugin.getConfig().getString("data.moneyb"));
        headMeta.setLore(lore);
        headMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        head.setItemMeta(headMeta);
        head.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        return head;
    }
    public ItemStack getESkull(String url, Advancement a, String s) {
        @SuppressWarnings("deprecation")
		ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
        if(url.isEmpty())return head;
       
       
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        String[] name = s.split("/");
        String nem = name[1].replace("_", " ");
        headMeta.setDisplayName(nem);
        headMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<String>();
        Integer r = plugin.getConfig().getInt("rewards."+s);
        lore.add("§6Récompense: "+r.toString()+plugin.getConfig().getString("data.moneyb"));
        lore.add("§bA récupérer !");
        headMeta.setLore(lore);
        head.setItemMeta(headMeta);
        head.addUnsafeEnchantment(Enchantment.MENDING, 1);
        
        return head;
    }
    public ItemStack getGSkull(String url, Advancement a, String s) {
        @SuppressWarnings("deprecation")
		ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
        if(url.isEmpty())return head;
       
       
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        String[] name = s.split("/");
        String nem = name[1].replace("_", " ");
        headMeta.setDisplayName(nem);
        headMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<String>();
        lore.add("§4Déjà récupérer !");
        headMeta.setLore(lore);        
        head.setItemMeta(headMeta);
        ItemMeta sim = head.getItemMeta();
        sim.addEnchant(Enchantment.DURABILITY, 1, true);
        head.setItemMeta(sim);
        
        return head;
    }
    public boolean CheckAdv(String uuid, String adv) {
    	adv = adv.replace("minecraft:", "");
    	try {
    		PreparedStatement st = plugin.getConnection()
    				.prepareStatement("SELECT * FROM `achievements` WHERE `id`='"+plugin.getId(uuid)+"' AND `"+adv+"`='1'");
    		ResultSet rs = st.executeQuery();
    		if(rs.next()) {
    			return true;
    		}
    	}catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    public boolean CheckRec(String d,String r, String adv) {
    	adv = adv.replace("minecraft:", "");
    	try {
    		PreparedStatement st = plugin.getConnection()
    				.prepareStatement("SELECT * FROM `rewards` WHERE `referral`='"+plugin.getId(r)+"' AND `referred`='"+plugin.getId(d)+"' AND `"+adv+"`='1'");
    		ResultSet rs = st.executeQuery();
    		if(rs.next()) {
    			return true;
    		}
    	}catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    public void updateRew(String d,String r, String adv) {
    	try (Statement statement = plugin.getConnection().createStatement()){
    		statement.executeUpdate("UPDATE `rewards` SET `"+adv+"`='1' WHERE `referral`='"+plugin.getId(r)+"' AND `referred`='"+plugin.getId(d)+"'");
    	}catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
}
