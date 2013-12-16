/*  1:   */ package enchantit;
/*  2:   */ 
/*  3:   */ import org.bukkit.command.CommandSender;
/*  4:   */ import org.bukkit.entity.Player;
/*  5:   */ 
/*  6:   */ public class MockPermissionHandler
/*  7:   */   implements IPermissionHandler
/*  8:   */ {
/*  9:   */   public boolean has(Player player, String permission)
/* 10:   */   {
/* 11:18 */     return player.hasPermission(permission);
/* 12:   */   }
/* 13:   */   
/* 14:   */   public boolean has(CommandSender sender, String permission)
/* 15:   */   {
/* 16:26 */     return sender.hasPermission(permission);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\ADoby\Downloads\EnchantIt.jar
 * Qualified Name:     enchantit.MockPermissionHandler
 * JD-Core Version:    0.7.0.1
 */