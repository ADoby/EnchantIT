package enchantit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AutomaticItemRefilling implements Listener {

	EnchantIt plugin;

	private String REFILL_SECTION_NAME = "autorefillsettings";

	private Map<String, Object> stringToSetting = null;

	public AutomaticItemRefilling(EnchantIt plugin) {
		this.plugin = plugin;

		Reload();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	ConfigurationSection configSection;
	
	public void Reload() {
		stringToSetting = new HashMap<String, Object>();

		plugin.log("Trying to start Refiller");
		
		FileConfiguration FileCOnfig = plugin.getConfig();
		
		if(FileCOnfig == null){
			plugin.log("Error, Config null");
			return;
		}
		
		//configSection = plugin.getConfig().getConfigurationSection(REFILL_SECTION_NAME);

		//if(configSection == null){
			//plugin.log("Error, ConfigSection null");
			//return;
		//}
		
		/*Set<String> settings = configSection.getKeys(false);
		if(settings != null){
			Iterator<String> ita = settings.iterator();
			if(ita != null){
				while (ita.hasNext()) {
					if(ita.next() != null){
						AddPlayerToList((String) ita.next());
					}
				}
			}
		}*/
		
		//plugin.getConfig().createSection(REFILL_SECTION_NAME, stringToSetting);
		
		plugin.log("Refiller Loaded");
	}

	private void LoadPlayerToList(String key) {
		boolean refilling = plugin.getConfig().getBoolean(
				REFILL_SECTION_NAME + "." + key + ".refill", true);

		stringToSetting.put(key, refilling);
		
		plugin.saveConfig();
	}
	

	public void AddNewPlayerToList(String key, boolean refilling) {
		stringToSetting.put(key, refilling);
		
		plugin.getConfig().set(
				REFILL_SECTION_NAME + "." + key + ".refill", refilling);
		
		plugin.saveConfig();
		
		//configSection.set(player, refilling);
		
		/*
		 * Position = new Vector3(sin(i), position.x, cos(i));
		 * i++;
		 * if(i==359)
		 * i = 0;
		 */
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(true)
			return;
		
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
			if (plugin.permissions.has(e.getPlayer(), "enchantit.refill")) {
				// We only do it when, we player wants to refill
				if ((boolean) stringToSetting.get(getPlayerIdentifier(e.getPlayer()))) {
					ItemStack item = null;
					PlayerInventory inv = e.getPlayer().getInventory();
					
					ItemStack itemInHand = inv.getItemInHand();
					if(itemInHand != null){
						if(itemInHand.getAmount() == 0 || itemInHand.getDurability() == 0){
							item = itemInHand;
						}
					}
					
					if(item != null){
						TryRefilling(e.getPlayer(), item);
					}
				}
			}
		}
	}
	
	@EventHandler
    public void onItemDrop (PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        
    }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		String identifier = getPlayerIdentifier(e.getPlayer());
		
		LoadPlayerToList(identifier);
		
		//boolean refilling = (boolean) configSection.get(identifier, true);
		
		//AddNewPlayerToList(identifier, refilling);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (plugin.permissions.has(e.getPlayer(), "enchantit.refill")) {
			// We only do it when, we player wants to refill
			if ((boolean) stringToSetting.get(getPlayerIdentifier(e.getPlayer()))) {
				if(e.getItemInHand().getAmount() == 1){
					TryRefilling(e.getPlayer(), e.getItemInHand());
				}else{
					plugin.msg(e.getPlayer(), "&aItem Not empty");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		if (plugin.permissions.has(event.getPlayer(), "enchantit.refill")) {
			// We only do it when, we player wants to refill
			if ((boolean) stringToSetting.get(getPlayerIdentifier(event
					.getPlayer()))) {
				TryRefilling(event.getPlayer(), event.getBrokenItem());
			}
		}
	}

	private void TryRefilling(Player player, ItemStack replacingItem) {
		if(replacingItem == null){
			plugin.msg(player, "&aItemis null");
			return;
		}
		
		Integer InvSize = player.getInventory().getSize();
		Integer i = 0;

		//Material wantedMat = replacingItem.getType();

		for (i = 0; i < InvSize; i++) {
			ItemStack item = player.getInventory().getItem(i);
			if (item != null && i != player.getInventory().getHeldItemSlot()) {
				if (item.getType() == replacingItem.getType()) {
					if(item.getData().getData() == replacingItem.getData().getData()){
						
						ItemStack newItem = item.clone();
						
						player.getInventory().setItem(player.getInventory().getHeldItemSlot(), newItem);
						
						
						//player.getInventory().						
						//player.getInventory().setItemInHand(item.clone());
						
						player.getInventory().setItem(i, null);
						
						//player.getInventory().clear(i);
						
						//player.getInventory().
						
						
						plugin.msg(player, "&aReplaced");
						return;
					}
				}
			}
		}
		plugin.msg(player, "&aNo item found to replace");
	}

	public String getPlayerIdentifier(Player player) {
		if (player.getServer().getOnlineMode()) {
			try {
				// Save online mode with uuid (MC 1.7+)
				return player.getUniqueId().toString();
			} catch (Exception e) {
				// Probably old minecraft version
				// just use offline modus
				return player.getName();
			}
		} else {
			// Unsave offline mode
			return player.getName();
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		// TODO Auto-generated method stub
		if ((label.equalsIgnoreCase("enchantit"))
				|| (label.equalsIgnoreCase("eit"))) {
			if ((args.length == 2) && (sender instanceof Player)) {
				Player player = (Player) sender;
				if (!args[0].equalsIgnoreCase("refill")) {
					return false;
				}
				if (!args[1].equalsIgnoreCase("true") && !args[1].equalsIgnoreCase("false")) {
					plugin.msg(sender, "&aSecond attribute has to be true or false was: &6"+ args[1]);
					return false;
				}

				// Set Settings for player
				boolean refilling = false;
				if (args[1].equalsIgnoreCase("true"))
					refilling = true;

				AddNewPlayerToList(getPlayerIdentifier(player), refilling);

				plugin.msg(player, "&aRefill settings updated");
				return true;
			}
		}

		return false;
	}

}
