package enchantit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

	private Map<String, Boolean> stringToEnchantedRefill = null;
	
	public AutomaticItemRefilling(EnchantIt plugin) {
		this.plugin = plugin;

		Reload();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public void Reload() {
		plugin.log("Trying to start Refiller");
		
		stringToSetting = new HashMap<String, Boolean>();
		stringToEnchantedRefill = new HashMap<String, Boolean>();
		
		Player[] players = plugin.getServer().getOnlinePlayers();
		
		for(int i = 0; i < players.length; i++){
			LoadPlayerToList(getPlayerIdentifier(players[i]));
		}
		
		plugin.log("Refiller Loaded");
	}

	private void LoadPlayerToList(String key) {
		boolean refilling = plugin.getConfig().getBoolean(
				REFILL_SECTION_NAME + "." + key + ".refill", true);
		
		boolean enchantedRefill = plugin.getConfig().getBoolean(
				REFILL_SECTION_NAME + "." + key + ".enchantedRefill", true);
		
		AddNewPlayerToList(key, refilling, enchantedRefill);
	}
	

	public boolean PlayerWantsEnchantedRefill(Player player){
		return stringToEnchantedRefill.get(getPlayerIdentifier(player));
	}
	
	public void AddNewPlayerToList(String key, boolean refilling, boolean enchantedRefill) {
		stringToSetting.put(key, refilling);
		stringToEnchantedRefill.put(key, enchantedRefill);
		
		plugin.getConfig().set(
				REFILL_SECTION_NAME + "." + key + ".refill", refilling);
		
		plugin.getConfig().set(
				REFILL_SECTION_NAME + "." + key + ".enchantedRefill", enchantedRefill);
		
		plugin.saveConfig();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		String identifier = getPlayerIdentifier(e.getPlayer());
		
		LoadPlayerToList(identifier);
	}
	
	
	//Refill events
	
	@EventHandler
    public void onItemDrop (PlayerDropItemEvent event) {
		/*
		if(event.getItemDrop().getItemStack().equals(event.getPlayer().getItemInHand()) && event.getPlayer().getItemInHand().getAmount() == 1){
			TryRefilling(event.getPlayer(), event.getItemDrop().getItemStack());
		}
        */
    }
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getItemInHand().getAmount() == 1 && event.getBlockPlaced().getType() != Material.SOIL){
			//Only refill if the last item was placed
			TryRefillingItem(event.getPlayer(), event.getItemInHand());
		}
	}
	
	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		plugin.GetLevelBack(event.getPlayer(), event.getBrokenItem());
		
		if(!event.getBrokenItem().equals(event.getPlayer().getInventory().getItemInHand())){
			//Armor
			return;
		}
		
		if(plugin.ItemHasEnchantments(event.getBrokenItem())){
			
			final ItemStack oldItem = event.getBrokenItem().clone();
			final Player player = event.getPlayer();
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                	if(TryRefillingTool(player, oldItem)){
                		plugin.EnchantItemWithSameEnchantments(player, oldItem, player.getItemInHand());
                    	plugin.msg(player, "&aYour enchanted item broke, i found another non enchanted item and enchanted it");
                	}
                }
            }, 5L);

		}else{
			final ItemStack oldItem = event.getBrokenItem().clone();
			final Player player = event.getPlayer();
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                	if(TryRefillingTool(player, oldItem)){
                		plugin.msg(player, "&aYour item broke, i replaced it");
                	}
                }
            }, 5L);
		}
	}
	
	@SuppressWarnings("unused")
	private int getItemSlotID(Player player, ItemStack item){
		for (int InventorySlotID = 0; InventorySlotID < player.getInventory().getSize(); InventorySlotID++) {
			if(player.getInventory().getItem(InventorySlotID).equals(item)){
				return InventorySlotID;
			}
		}
		return -1;
	}
	
	@SuppressWarnings("deprecation")
	private void ReplaceItem(Player player, int brokenItemSlot, int newItemSlot, ItemStack newItem){
		player.getInventory().setItem(brokenItemSlot, newItem.clone());
		
		player.getInventory().setItem(newItemSlot, null);
		
		player.updateInventory();
		
		plugin.msg(player, "&aItem Replaced");
	}
	
	
	private boolean TryRefillingItem(Player player, ItemStack brokenItem, int brokenItemSlot){
		if (!stringToSetting.get(getPlayerIdentifier(player))) {
			//Player doesn't want to refill
			return false;
		}
		if (!plugin.permissions.has(player, "enchantit.refill")) {
			return false;
		}

		for (int InventorySlotID = 0; InventorySlotID < player.getInventory().getSize(); InventorySlotID++) {
			ItemStack item = player.getInventory().getItem(InventorySlotID);
			
			//First is to except empty slots, second so we don't swap with the same item we are holding
			if (item != null && InventorySlotID != brokenItemSlot) {
				if (item.isSimilar(brokenItem)) {
					ReplaceItem(player, brokenItemSlot, InventorySlotID, item);
					return true;
				}
			}
		}
		
		//If we get here we did not find any item
		return false;
	}

	private boolean TryRefillingTool(Player player, ItemStack brokenItem, int brokenItemSlot){
		if (!stringToSetting.get(getPlayerIdentifier(player))) {
			//Player doesn't want to refill
			return false;
		}
		if (!plugin.permissions.has(player, "enchantit.refill")) {
			return false;
		}

		for (int InventorySlotID = 0; InventorySlotID < player.getInventory().getSize(); InventorySlotID++) {
			ItemStack item = player.getInventory().getItem(InventorySlotID);
			
			//First is to except empty slots, second so we don't swap with the same item we are holding
			if (item != null && InventorySlotID != brokenItemSlot) {
				if (item.getType() == brokenItem.getType()) {
					//Check type like dirt, log, wool, axe etc.
					
					ReplaceItem(player, brokenItemSlot, InventorySlotID, item);
					return true;
				}
			}
		}
		
		//If we get here we did not find any item
		return false;
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	private boolean TryRefilling2(Player player, ItemStack brokenItem, int brokenItemSlot){
		if (!stringToSetting.get(getPlayerIdentifier(player))) {
			//Player doesn't want to refill
			return false;
		}
		if (!plugin.permissions.has(player, "enchantit.refill")) {
			return false;
		}

		for (int InventorySlotID = 0; InventorySlotID < player.getInventory().getSize(); InventorySlotID++) {
			ItemStack item = player.getInventory().getItem(InventorySlotID);
			
			//First is to except empty slots, second so we don't swap with the same item we are holding
			if (item != null && InventorySlotID != brokenItemSlot) {
				if (item.getType() == brokenItem.getType()) {
					//Check type like dirt, log, wool, axe etc.
					
					//First is for tools, second for blocks etc.
					if(brokenItem.getDurability() != 0 || item.getData().getData() == brokenItem.getData().getData()){
						ReplaceItem(player, brokenItemSlot, InventorySlotID, item);
						return true;
					}
				}
			}
		}
		
		//If we get here we did not find any item
		return false;
	}
	
	private boolean TryRefillingTool(Player player, ItemStack replacingItemStack) {
		if(replacingItemStack == null){
			return false;
		}
		
		return TryRefillingTool(player, replacingItemStack, player.getInventory().getHeldItemSlot());
	}
	
	private boolean TryRefillingItem(Player player, ItemStack replacingItemStack) {
		if(replacingItemStack == null){
			return false;
		}
		
		return TryRefillingItem(player, replacingItemStack, player.getInventory().getHeldItemSlot());
	}

	public String getPlayerIdentifier(Player player) {
		if (player.getServer().getOnlineMode()) {
			try {
				// Save online mode with uuid (MC 1.7+)
				return GetPlayerUID(player);
			} catch (Exception e) {
				// Probably old minecraft version
				// just use offline modus
				return GetPlayerName(player);
			}
		} else {
			// Unsave offline mode
			return GetPlayerName(player);
		}
	}
	
	private String GetPlayerName(Player player){
		return player.getName();
	}
	
	private String GetPlayerUID(Player player){
		return player.getUniqueId().toString();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if ((label.equalsIgnoreCase("enchantit"))
				|| (label.equalsIgnoreCase("eit"))) {
			if ((args.length == 2) && (sender instanceof Player)) {
				Player player = (Player) sender;
				
				if(!plugin.permissions.has(player, "enchantit")){
					plugin.msg(player, "&aYou don't have the permission to use enchantit.");
					return true;
				}
				
				if (!args[0].equalsIgnoreCase("refill")) {
					return false;
				}
				if (!args[0].equalsIgnoreCase("enchantedRefill")) {
					return false;
				}
				
				if (!plugin.permissions.has(player, "enchantit.refill")) {
					plugin.msg(player, "&aYou don't have permissions to refill items");
					return false;
				}
				
				if (!args[1].equalsIgnoreCase("true") && !args[1].equalsIgnoreCase("false")) {
					plugin.msg(sender, "&aSecond attribute has to be true or false was: &6"+ args[1]);
					return false;
				}

				if (args[0].equalsIgnoreCase("refill")) {
					SetRefillSetting(player, args[1]);
				}else if (args[0].equalsIgnoreCase("enchantedRefill")) {
					SetEnchantedRefillSetting(player, args[1]);
				}
				return true;
			}
		}

		return false;
	}

	private void SetRefillSetting(Player player, String arg){
		// Set Settings for player
		boolean refilling = false;
		if (arg.equalsIgnoreCase("true"))
			refilling = true;

		AddNewPlayerToList(getPlayerIdentifier(player), refilling, stringToEnchantedRefill.get(getPlayerIdentifier(player)));

		plugin.msg(player, "&aRefill settings updated");
	}
	
	private void SetEnchantedRefillSetting(Player player, String arg){
		boolean refilling = false;
		if (arg.equalsIgnoreCase("true"))
			refilling = true;

		AddNewPlayerToList(getPlayerIdentifier(player), stringToSetting.get(getPlayerIdentifier(player)), refilling);

		plugin.msg(player, "&aEnchanted refill settings updated");
	}
	
}
