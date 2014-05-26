package enchantit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.permission.Permission;

public class EnchantIt extends JavaPlugin implements CommandExecutor, Listener {

	// Wont Change
	public String CONFIG_SPLIT_STRING = " &f: &6";
	private String ID_MAXLEVEL_SPLIT_STRING = "-";

	// Set by this plugin
	private static EnchantIt plugin;
	IPermissionHandler permissions;
	
	private AutomaticItemRefilling refilling = null;
	private List<String> enchantStrings = null;
	private Map<Integer, Enchantment> enchantsByID = null;
	private Map<String, Enchantment> enchantsByName = null;
	private Map<Enchantment, Integer> enchantMaxLevels = null;

	// Config Settings
	private int MAX_ENCHANT_LEVEL_DEFAULT = 10;
	private int MAX_ENCHANT_LEVEL_PERM = 30;
	private double LEVEL_BACK = 0.5D;
	private int LEVEL_COST_PER_SKILL_LEVEL = 4;
	private double MULT_PER_SKILL_LEVEL = 2.0D;
	private int MAX_LEVEL_COST = 40;

	private double LEVEL_BACK_MULT = 1.0D;
	
	private boolean REFILLING_ENABLED = true;

	public void onEnable() {

		// Default setup
		plugin = this;
		
		getServer().getPluginManager().registerEvents(this, this);

		// First add callback thingy for my command
		getCommand("enchantit").setExecutor(plugin);

		// if some settings are missing, copy defaults
		getConfig().options().copyDefaults(true);

		// Save Config (if e.g. defaults where copied)
		saveConfig();

		
		
		LoadPlugin();
		
	}

	private void LoadPlugin() {
		// create persmission Handling
		RegisteredServiceProvider<Permission> permissionsPlugin = null;
		if (getServer().getPluginManager().isPluginEnabled("Vault")) {
			log("Vault detected. hooked.");

			permissionsPlugin = getServer().getServicesManager()
					.getRegistration(Permission.class);

			permissions = new PermissionHandlerWrapper(
					(Permission) permissionsPlugin.getProvider());
		} else {
			log("Vault not detected for permissions, defaulting to Bukkit Permissions");
			permissions = new MockPermissionHandler();
		}

		// Load config into variables
		this.MAX_ENCHANT_LEVEL_DEFAULT = getConfig().getInt(
				"defaults.max_enchant_level_default");
		this.MAX_ENCHANT_LEVEL_PERM = getConfig().getInt(
				"defaults.max_enchant_level_perm");
		this.LEVEL_BACK = getConfig().getDouble("defaults.level_back");

		this.LEVEL_COST_PER_SKILL_LEVEL = getConfig().getInt(
				"defaults.level_cost_per_skill_level");
		this.MULT_PER_SKILL_LEVEL = getConfig().getDouble(
				"defaults.mult_per_skill_level");
		this.MAX_LEVEL_COST = getConfig().getInt("defaults.max_level_cost");

		this.LEVEL_BACK_MULT = getConfig()
				.getDouble("defaults.level_back_mult");

		this.REFILLING_ENABLED = getConfig().getBoolean("defaults.enable_refiller");
		
		ConfigurationSection CS = getConfig().getConfigurationSection(
				"enchants");

		enchantStrings = new ArrayList<String>();
		enchantsByID = new HashMap<Integer, Enchantment>();
		enchantsByName = new HashMap<String, Enchantment>();
		enchantMaxLevels = new HashMap<Enchantment, Integer>();

		// First load all enchantments
		if (CS != null) {
			Set<String> enchants = CS.getKeys(false);
			Iterator<String> ita = enchants.iterator();
			while (ita.hasNext()) {
				AddEnchantmentFromConfig((String) ita.next());
			}
		} else {
			log("Error loading config, disabling !");
			getPluginLoader().disablePlugin(this);
		}
		
		if(REFILLING_ENABLED){
			refilling = new AutomaticItemRefilling(this);
		}
	}

	private void AddEnchantmentFromConfig(String enchant) {
		String enchantString = enchant
				+ CONFIG_SPLIT_STRING
				+ getConfig().getString(
						new StringBuilder("enchants.").append(enchant)
								.toString());

		String enchantValue = getConfig().getString("enchants." + enchant);
		String[] enchantParts = enchantValue.split(ID_MAXLEVEL_SPLIT_STRING);

		if (enchantParts.length < 2 || !isInt(enchantParts[0])
				|| !isInt(enchantParts[1])) {
			log("Error: Enchantment: " + enchant + " wrong config settings");
			return;
		}

		int enchantmentID = Integer.parseInt(enchantParts[0]);
		int enchantMaxLevel = Integer.parseInt(enchantParts[1]);

		@SuppressWarnings("deprecation")
		Enchantment ench = Enchantment.getById(enchantmentID);
		/*
		 * Probably has to be replaced by Enchantment.getByName(string name); In
		 * some time. (Will break plugin)
		 */

		if (ench != null) {
			if (!enchantStrings.contains(enchantString)) {
				enchantStrings.add(enchantString);
			}

			if (!enchantsByName.containsKey(enchant)) {
				enchantsByName.put(enchant, ench);
			}

			if (!enchantsByID.containsKey(enchantmentID)) {
				enchantsByID.put(enchantmentID, ench);
			}
			if (!enchantMaxLevels.containsKey(ench)) {
				enchantMaxLevels.put(ench, enchantMaxLevel);
			}
		} else {
			log("Enchantment with ID: " + enchantmentID + " not found.");
		}
	}

	public void onDisable() {
		// We should clean up things here
		enchantStrings.clear();
		enchantsByName.clear();
		enchantsByID.clear();
		enchantMaxLevels.clear();
	}

	private void Reload() {
		log("Reloading...");
		onDisable();
		LoadPlugin();
		log("Reloaded!");
	}

	private void GetLevelBack(Player player, ItemStack item) {
		int level = 0;
		for (Map.Entry<Integer, Enchantment> entry : enchantsByID.entrySet()) {
			level += getEnchantmentLevel(item, entry.getValue())
					* LEVEL_BACK_MULT * LEVEL_COST_PER_SKILL_LEVEL;
		}

		msg(player, "&aYou should get " + level + " level back.");

		player.setLevel(player.getLevel() + level);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		
		if(refilling.onCommand(sender, cmd, label, args)){
			return true;
		}
		if ((label.equalsIgnoreCase("enchantit"))
				|| (label.equalsIgnoreCase("eit"))) {
			if((sender instanceof Player)){
				Player p = (Player) sender;
				if(!permissions.has(p, "enchantit")){
					msg(p, "&aYou don't have the permission to use enchantit.");
					return true;
				}
			}
			
			
			if ((args.length >= 2) && ((sender instanceof Player))
					&& (((Player) sender).getItemInHand() != null)) {
				
				
				Player p = (Player) sender;

				Enchantment ench = null;

				if (isInt(args[1])) {
					ench = (Enchantment) enchantsByID.get(Integer
							.parseInt(args[1]));
				} else {
					ench = (Enchantment) enchantsByName.get(args[1]);
				}

				if (ench == null) {
					msg(p, "&6" + args[1] + "&a is no enchantment");
					return true;
				}

				if ((args[0].equalsIgnoreCase("increase"))
						|| (args[0].equalsIgnoreCase("i"))) {
					
					//Increase Enchantment level by 1
					int enchantmentLevel = getEnchantmentLevel(
							p.getItemInHand(), ench);
					SetEnchantmentLevel(p, ench, enchantmentLevel+1);
					
				} else if ((args[0].equalsIgnoreCase("decrease"))
						|| (args[0].equalsIgnoreCase("d"))) {
					
					//Decrease enchantment level by 1
					int enchantmentLevel = getEnchantmentLevel(
							p.getItemInHand(), ench);
					SetEnchantmentLevel(p, ench, enchantmentLevel-1);
					
				} else if ((args[0].equalsIgnoreCase("max"))
						|| (args[0].equalsIgnoreCase("m"))) {
					
					
					//Set Enchantment level to either maxlevel or enchantmentMaxLevel
					int maxlevel = this.MAX_ENCHANT_LEVEL_DEFAULT;
					if (permissions.has(p, "enchantit.enchantmore")) {
						maxlevel = this.MAX_ENCHANT_LEVEL_PERM;
					}
					
					if(maxlevel > enchantMaxLevels.get(ench)){
						SetEnchantmentLevel(p, ench, enchantMaxLevels.get(ench));
					}else{
						SetEnchantmentLevel(p, ench, maxlevel);
					}
					
				} else if ((args[0].equalsIgnoreCase("set"))
						|| (args[0].equalsIgnoreCase("s"))) {

					if (isInt(args[2])) {
						SetEnchantmentLevel(p, ench, Integer.parseInt(args[2]));
					} else {
						msg(p, "&aThird argument: &6" + args[2]
								+ "&a must be a number");
						return true;
					}

				} else if ((args[0].equalsIgnoreCase("remove"))
						|| (args[0].equalsIgnoreCase("r"))) {
					
					SetEnchantmentLevel(p, ench, 0);
					
				}
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("list")
						|| args[0].equalsIgnoreCase("l")) {
					sender.sendMessage("§6Enchantment List:");
					for (int i = 0; i < this.enchantStrings.size(); i++) {
						msg(sender,
								" &a"
										+ ChatColor
												.translateAlternateColorCodes(
														'&',
														(String) this.enchantStrings
																.get(i)));
					}
				} else if (args[0].equalsIgnoreCase("reload")
						|| args[0].equalsIgnoreCase("r")) {
					if ((sender instanceof Player)) {
						Player p = (Player) sender;
						if (permissions.has(p, "enchantit.reload")) {
							Reload();
						}
					} else {
						Reload();
					}
				}
			} else if (args.length == 0) {
				SendEnchantInfo(sender);
			}
			return true;
		}
		return false;
	}

	private void SetEnchantmentLevel(Player p, Enchantment ench, int newLevel) {
		if (newLevel < 0) {
			newLevel = 0;
		}
		
		int currentLevel = getEnchantmentLevel(p.getItemInHand(), ench);

		int maxlevel = this.MAX_ENCHANT_LEVEL_DEFAULT;
		if (permissions.has(p, "enchantit.enchantmore")) {
			maxlevel = this.MAX_ENCHANT_LEVEL_PERM;
		}

		if (currentLevel >= enchantMaxLevels.get(ench).intValue()) {
			msg(p, "&aEnchantment is on max enchantment level");
			return;
		}
		if (currentLevel >= maxlevel) {
			msg(p, "&aEnchantment is on max global level:&6" + maxlevel);
			return;
		}
		
		if(currentLevel <= 0 && newLevel == 0){
			msg(p, "&aEnchantment not on this tool, can't remove");
			return;
		}

		
		if (newLevel > enchantMaxLevels.get(ench).intValue()) {
			msg(p,
					"&aThird argument: &6" + newLevel
							+ "&a must be smaller or equal then max enchant level: &6"
							+ String.valueOf(enchantMaxLevels.get(ench)));
			return;
		}
		if (newLevel > maxlevel) {
			msg(p,
					"&aThird argument: &6" + newLevel
							+ "&a must be smaller or equal then global max enchant level: &6"
							+ maxlevel);
			return;
		}

		if (currentLevel == newLevel) {
			msg(p, "&aEnchantment level is the same");
			return;
		}

		if (currentLevel > newLevel) {
			if(!permissions.has(p, "enchantit.decrease")){
				msg(p, "&aYou don't have the permission to decrease any enchantment level.");
				return;
			}
			
			int levelcost = (int) (calcLevel(newLevel, currentLevel) * this.LEVEL_BACK);
			int dif = currentLevel - newLevel;
			
			if(setEnchantment(p.getItemInHand(), ench, newLevel)){
				msg(p,
						"&aDecreased &6" + ench.getName() + "&a by &6"
								+ String.valueOf(dif) + "&a level, now: &6"
								+ String.valueOf(newLevel) + "&a giving &6"
								+ String.valueOf(levelcost) + "&a level/s");
				
				p.setLevel(p.getLevel() + levelcost);
			}else{
				msg(p, "&aSome error, don't know");
			}
		} else if (currentLevel < newLevel) {
			if(!permissions.has(p, "enchantit.increase")){
				msg(p, "&aYou don't have the permission to increase any enchantment level.");
				return;
			}
			
			int levelcost = calcLevel(currentLevel, newLevel);
			int dif = newLevel - currentLevel;

			if ((p.getLevel() >= levelcost)
					&& setEnchantment(p.getItemInHand(), ench, newLevel)) {
				msg(p,
						"&aIncreased &6" + ench.getName() + "&a by &6"
								+ String.valueOf(dif) + "&a level, now: &6"
								+ String.valueOf(newLevel)
								+ "&a taking &6" + String.valueOf(levelcost)
								+ "&a level/s");
				p.setLevel(p.getLevel() - levelcost);

			} else {
				msg(p, "&aYou need &6" + String.valueOf(levelcost)
						+ "&a level/s");
			}
		}
	}

	private void SendEnchantInfo(CommandSender sender) {
		int maxlevel = this.MAX_ENCHANT_LEVEL_DEFAULT;
		if (((sender instanceof Player))
				&& (permissions.has((Player) sender, "enchantit.enchantmore"))) {
			maxlevel = this.MAX_ENCHANT_LEVEL_PERM;
		}
		msg(sender, "&eEnchantIt v" + plugin.getDescription().getVersion()
				+ " help");
		msg(sender, "&6/eit i &b<name/id>");
		msg(sender, "  &aAdds 1 level to the enchantment");
		msg(sender, "&6/eit d &b<name/id>");
		msg(sender, "  &aRemoves 1 level of the enchantment, "
				+ (LEVEL_BACK * LEVEL_COST_PER_SKILL_LEVEL)
				+ " level per enchantment level back");
		msg(sender, "&6/eit m &b<name/id>");
		msg(sender,
				"  &aMaximize the enchantment, max level &c"
						+ String.valueOf(maxlevel));
		msg(sender, "&6/eit r &b<name/id>");
		msg(sender, "  &aRemoves the enchantment completely, "
				+ (LEVEL_BACK * LEVEL_COST_PER_SKILL_LEVEL)
				+ " level per enchantment level back");
		msg(sender, "&6/eit s &b<name/id> <level>");
		msg(sender, "  &aSets enchantment to &b<level>&a, "
				+ (LEVEL_BACK * LEVEL_COST_PER_SKILL_LEVEL)
				+ " level per enchantment level back if smaller then current");
		msg(sender, "&6/eIt list/l");
		msg(sender, "  &aLists all enchantments with IDs");
	}

	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		if (permissions.has(event.getPlayer(), "enchantit.levelback")) {
			// We only do it when, we want to give levels back
			if (LEVEL_BACK_MULT > 0) {
				GetLevelBack(event.getPlayer(), event.getBrokenItem());
			}
		}
	}

	public void msg(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	public void msg(Player sender, String msg) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	private int calcLevel(int from, int to) {
		int levelsneeded = 0;
		for (int i = from + 1; i <= to; i++) {
			int currentLevelCost = (int) (LEVEL_COST_PER_SKILL_LEVEL + LEVEL_COST_PER_SKILL_LEVEL
					* (i * MULT_PER_SKILL_LEVEL));

			if (currentLevelCost > MAX_LEVEL_COST) {
				levelsneeded += MAX_LEVEL_COST;
			} else {
				levelsneeded += currentLevelCost;
			}
		}

		return levelsneeded;
	}

	private boolean isInt(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException nFE) {
		}
		return false;
	}

	private int getEnchantmentLevel(ItemStack item, Enchantment enchantment) {
		try {
			int level = item.getEnchantmentLevel(enchantment);
			if (level < 0) {
				return 0;
			}
			return level;
		} catch (Exception ex) {
		}
		return 0;
	}

	private boolean setEnchantment(ItemStack item, Enchantment enchantment, int level) {
		try {
			item.removeEnchantment(enchantment);
			if (level > 0) {
				item.addUnsafeEnchantment(enchantment, level);
			}
			return true;
		} catch (Exception ex) {
		}
		return false;
	}

	public void log(String msg) {
		getServer().getLogger().info("[EnchantIt] " + msg);
	}
}