/*  1:   */ package enchantit;
/*  2:   */ 
/*  3:   */ import net.milkbowl.vault.permission.Permission;
/*  4:   */ import org.bukkit.command.CommandSender;
/*  5:   */ import org.bukkit.entity.Player;
/*  6:   */ 
/*  7:   */ public class PermissionHandlerWrapper
/*  8:   */   implements IPermissionHandler
/*  9:   */ {
/* 10:   */   private Permission permissionBase;
/* 11:   */   
/* 12:   */   public PermissionHandlerWrapper(Permission permission)
/* 13:   */   {
/* 14:19 */     this.permissionBase = permission;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public boolean has(Player player, String permissionLevel)
/* 18:   */   {
/* 19:24 */     return this.permissionBase.has(player, permissionLevel);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public boolean has(CommandSender sender, String permissionLevel)
/* 23:   */   {
/* 24:29 */     return this.permissionBase.has(sender, permissionLevel);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\ADoby\Downloads\EnchantIt.jar
 * Qualified Name:     enchantit.PermissionHandlerWrapper
 * JD-Core Version:    0.7.0.1
 */