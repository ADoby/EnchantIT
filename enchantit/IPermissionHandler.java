package enchantit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract interface IPermissionHandler {
	public abstract boolean has(Player paramPlayer, String paramString);

	public abstract boolean has(CommandSender paramCommandSender,
			String paramString);
}