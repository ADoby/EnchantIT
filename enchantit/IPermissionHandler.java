package enchantit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract interface IPermissionHandler
{
  public abstract boolean has(Player paramPlayer, String paramString);
  
  public abstract boolean has(CommandSender paramCommandSender, String paramString);
}


/* Location:           C:\Users\ADoby\Downloads\EnchantIt.jar
 * Qualified Name:     enchantit.IPermissionHandler
 * JD-Core Version:    0.7.0.1
 */