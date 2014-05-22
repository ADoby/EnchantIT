package enchantit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MockPermissionHandler implements IPermissionHandler {
	public boolean has(Player player, String permission) {
		return player.hasPermission(permission);
	}

	public boolean has(CommandSender sender, String permission) {
		return sender.hasPermission(permission);
	}
}