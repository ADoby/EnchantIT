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

	public AutomaticItemRefilling(EnchantIt plugin) {
		this.plugin = plugin;

		Reload();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public void Reload() {
		plugin.log("Trying to start Refiller");
		
		stringToSetting = new HashMap<String, Boolean>();
		
		Player[] players = plugin.getServer().getOnlinePlayers();
		
		for(int i = 0; i < players.length; i++){
			LoadPlayerToList(getPlayerIdentifier(players[i]));
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
			TryRefilling(event.getPlayer(), event.getItemInHand());
		}
	}
	
	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		if(event.getBrokenItem().equals(event.getPlayer().getInventory().getItemInHand())){
			TryRefilling(event.getPlayer(), event.getBrokenItem());
		}else{
			//Should be one of players armor
			//Dont know how yet.
			return;
			/*
			int slotID = getItemSlotID(event.getPlayer(), event.getBrokenItem());
			
			if(slotID != -1){
				TryRefilling(event.getPlayer(), event.getBrokenItem(), slotID);
				return;
			}			
			plugin.msg(event.getPlayer(), "&aCould not find a nother amor");
			*/
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
	
	
	
	@SuppressWarnings("deprecation")
	private boolean TryRefilling(Player player, ItemStack brokenItem, int brokenItemSlot){
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
	
	private boolean TryRefilling(Player player, ItemStack replacingItemStack) {
		if(replacingItemStack == null){
			return false;
		}
		
		return TryRefilling(player, replacingItemStack, player.getInventory().getHeldItemSlot());
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
				if (!args[0].equalsIgnoreCase("refill")) {
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
