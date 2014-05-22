package enchantit;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionHandlerWrapper implements IPermissionHandler {
	private Permission permissionBase;

	public PermissionHandlerWrapper(Permission permission) {
		this.permissionBase = permission;
	}

	public boolean has(Player player, String permissionLevel) {
		return this.permissionBase.has(player, permissionLevel);
	}

	public boolean has(CommandSender sender, String permissionLevel) {
		return this.permissionBase.has(sender, permissionLevel);
	}
}