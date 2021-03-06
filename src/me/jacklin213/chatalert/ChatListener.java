package me.jacklin213.chatalert;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

public class ChatListener implements Listener{
	
	private ChatAlert plugin;
	
	public ChatListener(ChatAlert instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onChatTab(PlayerChatTabCompleteEvent event){
		final Collection<String> names = event.getTabCompletions();
		
		if (event.getLastToken().equals("@")){
			return;
		}
		
		if (event.getLastToken().startsWith("@")){
			String prefix = event.getLastToken().substring(1).toLowerCase();
			
			for(Player player : Bukkit.getOnlinePlayers()){
				if (player.getName().toLowerCase().startsWith(prefix)){
					names.add("@" + player.getName());
				} 
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
				public void run(){
					names.clear();
				}
			}, 40);
		} else {
			return;
		}
	}
	
	@EventHandler
	public void onTag(AsyncPlayerChatEvent event){
		final Player player = event.getPlayer();
		String msg = event.getMessage();
		String suffix = plugin.getMsgColor();
		if (plugin.getConfig().getBoolean("Advanced.UseVault") && plugin.getConfig().getBoolean("Advanced.UseSuffix")){
			suffix = ChatAlert.chat.getPlayerSuffix(player);
		}
		if(player.hasPermission("chatalert.alert")){
			if (plugin.onCooldown.contains(player.getName())){
				player.sendMessage(plugin.chatPluginPrefix + "Tagging is on cooldown");
				event.setCancelled(true);
			} else {
				for (Player p : Bukkit.getOnlinePlayers()){
					if (msg.contains("@" + p.getName()) || msg.contains("@" + p.getName().toLowerCase()) || msg.contains("@" + p.getName().toUpperCase())){
						this.ping(p);
						msg = ChatColor.translateAlternateColorCodes('&', msg.replace("@" + p.getName(), plugin.getTagColor() + "@" + p.getName() + suffix));
					}/* else {
						event.setCancelled(true);
						player.sendMessage(plugin.chatPluginPrefix + ChatColor.RED + "Player not online or does not exist.");
					}*/
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
					public void run(){
						plugin.onCooldown.remove(player.getName());
					}
				}, plugin.getCooldownTime());
				event.setMessage(msg);
			}
			
			/*for (String phrase : args){
				if (phrase.startsWith("@")){
					String name = phrase.substring(1);
					Player targetPlayer = Bukkit.getPlayer(name);
					if(targetPlayer != null){
						plugin.log.info("So far so good");
					} else {
						event.setCancelled(true);
						player.sendMessage(plugin.chatPluginPrefix + ChatColor.RED + "Player not online or does not exist.");
					}
				}
			}*/
		}
		
	}
	
	/*@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		final String playerName = player.getName();
		String pMsg = event.getMessage();
		String msg = plugin.getMsgColor() + "~ " + pMsg;
		if (player.hasPermission("chatalert.alert")){
			if (plugin.onCooldown.contains(playerName)){
				player.sendMessage(plugin.chatPluginPrefix + "Tagging is on cooldown");
				event.setCancelled(true);
			} else {
				Boolean tagged = Boolean.valueOf(false);
				Boolean invalid = Boolean.valueOf(false);
				String[] sentence = msg.split(" ");
				
				for (String word : sentence){
					if (word.startsWith("@")){
						String result = word.replaceAll("[-+.^:,!*%$�|/@]", "");
						Player p = Bukkit.getPlayerExact(result);
						if (p !=null){
							int index = Arrays.asList(sentence).indexOf(word);
							sentence[index] = plugin.getTagColor() + "" +  ChatColor.UNDERLINE + "@" + p.getName() + plugin.getMsgColor();
							Player pinged = Bukkit.getPlayer(p.getName());
							this.ping(pinged);
							tagged = Boolean.valueOf(true);
						} else {
							invalid = Boolean.valueOf(true);
						}
						StringBuilder builder = new StringBuilder();
						for (String s : sentence){
							builder.append(new StringBuilder().append(s).append(" ").toString());
						}
						event.setMessage(builder.toString());
					}
				}
				if (tagged.booleanValue() == true){
					if (player.hasPermission("chatalert.nocooldown")){
						return;
					} else {
						plugin.onCooldown.add(playerName);
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
							public void run(){
								plugin.onCooldown.remove(playerName);
							}
						}, plugin.getCooldownTime());
					}
				}
				if (invalid.booleanValue() == true){
					event.setCancelled(true);
					player.sendMessage(plugin.chatPluginPrefix + ChatColor.RED + "Invalid username");
				}
			}
		}
	}*/
	
	/*@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		Player p = event.getPlayer();
		final String playerName = p.getName();
		if (p.hasPermission("chatalert.alert")){
			String rawMessage = event.getMessage();
			String[] message = rawMessage.split(" ");
			for(int i = 0; i < message.length; i++){
				String txt = message[i];
				if (txt.startsWith("@")){
					String[] pName = txt.split("@");
					try {
						final Player player = Bukkit.getServer().getPlayerExact(pName[1]);
						if (player.isOnline()){
							if (plugin.onCooldown.contains(playerName)){
								p.sendMessage(plugin.chatPluginPrefix + "Tagging is on cooldown");
								event.setCancelled(true);
							} else {
								final Location location = player.getLocation();
								player.getWorld().playSound(location, Sound.NOTE_PIANO, 1.0F, pitch(13));
								// Note played after one sec
								Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
									public void run(){
										player.getWorld().playSound(location, Sound.NOTE_PIANO, 1.0F, pitch(18));
									}
								}, (long) 4);
								String formatMessage = ChatColor.WHITE + rawMessage;
								String cusMessage = formatMessage.replace(txt, ChatColor.GOLD + "" + ChatColor.UNDERLINE + txt + ChatColor.WHITE);
								event.setMessage(cusMessage);
								if (p.hasPermission("chatalert.nocooldown")){
									return;
								}
								plugin.onCooldown.add(playerName);
								Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
									public void run(){
										plugin.onCooldown.remove(playerName);
									}
								}, 200);
							}
						}	
					} catch (Exception ex){
						event.getPlayer().sendMessage(plugin.chatPluginPrefix + ChatColor.RED + "Invalid username");
						event.setCancelled(true);
					}
				}
			}
		} 
	}*/
	
	
	private void ping(final Player player){
		final Location location = player.getLocation();
		player.getWorld().playSound(location, Sound.NOTE_PIANO, 1.0F, pitch(13));
		// Note played after one sec
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			public void run(){
				player.getWorld().playSound(location, Sound.NOTE_PIANO, 1.0F, pitch(18));
			}
		}, (long) 4);
	}
	
	private Float pitch(int numberOfClicks){
		switch (numberOfClicks){
		case 0:
			return 0.5F;
		case 1:
		    return 0.53F;
		case 2:
		    return 0.56F;
		case 3:
		    return 0.6F;
		case 4:
		    return 0.63F;
		case 5:
			return 0.67F;
		case 6:
			return 0.7F;
		case 7:
			return 0.76F;
		case 8:
			return 0.8F;
		case 9:
			return 0.84F;
		case 10:
			return 0.9F;
		case 11:
			return 0.94F;
		case 12:
			return 1.0F;
		case 13:
			return 1.06F;
		case 14:
			return 1.12F;
		case 15:
			return 1.18F;
		case 16:
			return 1.26F;
		case 17:
			return 1.34F;
		case 18:
			return 1.42F;
		case 19:
			return 1.5F;
		case 20:
			return 1.6F;
		case 21:
			return 1.68F;
		case 22:
			return 1.78F;
		case 23:
			return 1.88F;
		case 24:
	    	return 2.0F;
		}
		return 0.0F;
	}
}
