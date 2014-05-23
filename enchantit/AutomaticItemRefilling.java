package enchantit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class AutomaticItemRefilling implements Listener {

	EnchantIt plugin;

	private String REFILL_SECTION_NAME = "autorefillsettings";

	private Map<String, Boolean> stringToSetting = null;

	public AutomaticItemRefilling(EnchantIt plugin) {
		this.plugin = plugin;

		Reload();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	ConfigurationSection configSection;
	
	public void Reload() {
		stringToSetting = new HashMap<String, Boolean>();

		plugin.log("Trying to start Refiller");
		
		FileConfiguration FileCOnfig = plugin.getConfig();
		
		if(FileCOnfig == null){
			plugin.log("Error, Config null");
			return;
		}
		
		plugin.log("Refiller Loaded");
	}

	private void LoadPlayerToList(String key) {
		boolean refilling = plugin.getConfig().getBoolean(
				REFILL_SECTION_NAME + "." + key + ".refill", true);
		
		AddNewPlayerToList(key, refilling);
	}
	

	public void AddNewPlayerToList(String key, boolean refilling) {
		stringToSetting.put(key, refilling);
		
		plugin.getConfig().set(
				REFILL_SECTION_NAME + "." + key + ".refill", refilling);
		
		plugin.saveConfig();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		String identifier = getPlayerIdentifier(e.getPlayer());
		
		LoadPlayerToList(identifier);
	}
	
	
	//Refill events
	
	@EventHandler
    public void onItemDrop (PlayerDropItemEvent e) {
        TryRefilling(e.getPlayer(), e.getItemDrop().getItemStack(), true);
    }
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getItemInHand().getAmount() == 1 && event.getBlockPlaced().getType() != Material.SOIL){
			//Only refill if the last item was placed
			TryRefilling(event.getPlayer(), event.getItemInHand(), true);
		}
	}
	
	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		TryRefilling(event.getPlayer(), event.getBrokenItem(), false);
	}
	
	@SuppressWarnings("deprecation")
	private boolean TryRefilling(Player player, ItemStack itemStack){
		
		if (!stringToSetting.get(getPlayerIdentifier(player))) {
			//Player doesn't want to refill
			return false;
		}
		if (!plugin.permissions.has(player, "enchantit.refill")) {
			plugin.msg(player, "&aYou don't have permissions to refill items");
			return false;
		}
		
		if(itemStack == null){
			return false;
		}
		
		for (int InventorySlotID = 0; InventorySlotID < player.getInventory().getSize(); InventorySlotID++) {
			ItemStack item = player.getInventory().getItem(InventorySlotID);
			if (item != null && InventorySlotID != player.getInventory().getHeldItemSlot()) {

				if(itemStack.getTypeId() != item.getTypeId()){
					return false;
				}
				
				if((itemStack.getData().getData() < 30 && itemStack.getData().getData() != 0)){
					if(itemStack.getData().getData() != item.getData().getData()){
						return false;
					}
				}
				
				player.getInventory().setItem(player.getInventory().getHeldItemSlot(), item.clone());
				
				player.getInventory().setItem(InventorySlotID, null);
				
				player.updateInventory();
				
				plugin.msg(player, "&aItem Replaced");
				return true;
			}
		}
		plugin.msg(player, "&aDid not find another item");
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private boolean TryRefilling(Player player, Material type, int data, boolean checkData){		
		if (!stringToSetting.get(getPlayerIdentifier(player))) {
			//Player doesn't want to refill
			return false;
		}
		if (!plugin.permissions.has(player, "enchantit.refill")) {
			plugin.msg(player, "&aYou don't have permissions to refill items");
			return false;
		}

		for (int InventorySlotID = 0; InventorySlotID < player.getInventory().getSize(); InventorySlotID++) {
			ItemStack item = player.getInventory().getItem(InventorySlotID);
			
			if (item != null && InventorySlotID != player.getInventory().getHeldItemSlot()) {
				if (item.getType() == type) {
					//Check type like dirt, log, wool, axe etc.
					
					if(item.getDurability() != 0){
						player.getInventory().setItem(player.getInventory().getHeldItemSlot(), item.clone());
						
						player.getInventory().setItem(InventorySlotID, null);
						
						player.updateInventory();
						
						plugin.msg(player, "&aItem Replaced");
						return true;
					}
					
					if(item.getData().getData() == data){
						//check data like red wool, cracked brick etc.
						
						player.getInventory().setItem(player.getInventory().getHeldItemSlot(), item.clone());
						
						player.getInventory().setItem(InventorySlotID, null);
						
						player.updateInventory();
						
						plugin.msg(player, "&aItem Replaced");
						return true;
					} 
				}
			}
		}
		plugin.msg(player, "&aDid not find another item");
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean TryRefilling(Player player, Item replacingItem, boolean checkData) {
		if(replacingItem == null){
			return false;
		}
		
		return TryRefilling(player, replacingItem.getItemStack(), checkData);
	}
	
	@SuppressWarnings("deprecation")
	private boolean TryRefilling(Player player, ItemStack replacingItemStack, boolean checkData) {
		if(replacingItemStack == null){
			return false;
		}
		
		plugin.log("Data: " + replacingItemStack.getData().getData() + " Durability: " + replacingItemStack.getDurability());
		
		return TryRefilling(player, replacingItemStack.getType(), replacingItemStack.getData().getData(), checkData);
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
