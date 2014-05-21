/*   1:    */ package enchantit;
/*   2:    */ 
/*   3:    */ import java.util.ArrayList;
/*   4:    */ import java.util.HashMap;
/*   5:    */ import java.util.Iterator;
/*   6:    */ import java.util.List;
/*   7:    */ import java.util.Map;
/*   8:    */ import java.util.Set;
/*  11:    */ import org.bukkit.ChatColor;
/*  13:    */ import org.bukkit.command.Command;
/*  14:    */ import org.bukkit.command.CommandExecutor;
/*  15:    */ import org.bukkit.command.CommandSender;
/*  17:    */ import org.bukkit.configuration.ConfigurationSection;
/*  20:    */ import org.bukkit.enchantments.Enchantment;
/*  21:    */ import org.bukkit.entity.Player;
/*  22:    */ import org.bukkit.inventory.ItemStack;
/*  28:    */ import org.bukkit.plugin.java.JavaPlugin;
/*  29:    */ 
/*  30:    */ public class EnchantIt
/*  31:    */   extends JavaPlugin
/*  32:    */   implements CommandExecutor
/*  33:    */ {
/*  34:    */   public static EnchantIt plugin;
/*  35:    */   public static IPermissionHandler permissions;
/*  36: 30 */   private List<String> enchantStrings = new ArrayList<String>();
/*  37: 33 */   private String splitString = " &f: &6";
/*  38: 36 */   private Map<String, Enchantment> enchants = new HashMap<String, Enchantment>();
/*  39: 39 */   private Map<Enchantment, Integer> enchantMaxLevels = new HashMap<Enchantment, Integer>();
/*  40: 41 */   private int MAX_ENCHANT_LEVEL_DEFAULT = 10;
/*  41: 42 */   private int MAX_ENCHANT_LEVEL_PERM = 30;
/*  42: 43 */   private double LEVEL_BACK = 0.5D;
private int LEVEL_COST_PER_SKILL_LEVEL = 4;
private double MULT_PER_SKILL_LEVEL = 2.0D;
private int MAX_LEVEL_COST = 40;
/*  43:    */   
/*  44:    */   public void onEnable()
/*  45:    */   {
/*  46: 47 */     plugin = this;
/*  47:    */     
/*  48: 49 */     getCommand("enchantit").setExecutor(this);
/*  49:    */     
/*  50:    */ 
/*  51: 52 */     permissions = new MockPermissionHandler();

/*  65: 65 */     getConfig().options().copyDefaults(true);
/*  66: 66 */     saveConfig();
/*  67:    */     
/*  68:    */ 
/*  69:    */ 
/*  70: 70 */     this.MAX_ENCHANT_LEVEL_DEFAULT = getConfig().getInt("defaults.max_enchant_level_default");
/*  71: 71 */     this.MAX_ENCHANT_LEVEL_PERM = getConfig().getInt("defaults.max_enchant_level_perm");
/*  72: 72 */     this.LEVEL_BACK = getConfig().getDouble("defaults.level_back");

				this.LEVEL_COST_PER_SKILL_LEVEL = getConfig().getInt("defaults.level_cost_per_skill_level");
				this.MULT_PER_SKILL_LEVEL = getConfig().getDouble("defaults.mult_per_skill_level");
				this.MAX_LEVEL_COST = getConfig().getInt("defaults.max_level_cost");
/*  73:    */     
/*  74: 74 */     ConfigurationSection CS = getConfig().getConfigurationSection("enchants");
/*  75: 75 */     ConfigurationSection CS2 = getConfig().getConfigurationSection("enchantsmaxlevels");
/*  76: 76 */     if (CS != null)
/*  77:    */     {
/*  78: 78 */       Set<String> enchants = CS.getKeys(false);
/*  79: 79 */       Iterator<String> ita = enchants.iterator();
/*  80: 80 */       while (ita.hasNext())
/*  81:    */       {
						try{
							
							String enchant = (String)ita.next();
							String enchantString = enchant + this.splitString + getConfig().getString(new StringBuilder("enchants.").append(enchant).toString());
							this.enchantStrings.add(enchantString);
							//this.enchants.put(enchant, Enchantment.values()[getConfig().getInt("enchants." + enchant)]);
							this.enchants.put(enchant, Enchantment.getById(getConfig().getInt("enchants." + enchant)));
							
						}catch(Exception e){
							
						}
/*  82: 81 */         
/*  86:    */       }
/*  87: 87 */       Set<String> enchantmaxlevels = CS2.getKeys(false);
/*  88: 88 */       Iterator<String> ita2 = enchantmaxlevels.iterator();
/*  89: 89 */       while (ita2.hasNext())
/*  90:    */       {
/*  91: 90 */         String enchantId = (String)ita2.next();
/*  92: 92 */         if (isInt(enchantId))
/*  93:    */         {
/*  94: 93 */           int enchID = Integer.parseInt(enchantId);
/*  95: 94 */           if (isInt(getConfig().getString("enchantsmaxlevels." + enchantId)))
/*  96:    */           {
	try{
							//Enchantment ench = Enchantment.values()[enchID];
/*  97: 95 */             Enchantment ench = Enchantment.getById(enchID);
/*  98: 96 */             this.enchantMaxLevels.put(ench, Integer.valueOf(getConfig().getInt("enchantsmaxlevels." + enchantId)));
/*  99:    */           
	}catch(Exception e){
		
	}
	}
/* 100:    */         }
/* 101:    */       }
/* 102:    */     }
/* 103:    */     else
/* 104:    */     {
/* 105:103 */       log("Error loading config, disabling !");
/* 106:104 */       getPluginLoader().disablePlugin(this);
/* 107:    */     }
/* 108:    */   }
/* 109:    */   
/* 110:    */   public void onDisable() {}
/* 111:    */   
/* 112:    */   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
/* 113:    */   {
/* 114:114 */     if ((label.equalsIgnoreCase("enchantit")) || (label.equalsIgnoreCase("eit")))
/* 115:    */     {
/* 116:115 */       if ((args.length >= 2) && ((sender instanceof Player)) && (((Player)sender).getItemInHand() != null))
/* 117:    */       {
/* 118:117 */         Player p = (Player)sender;
/* 119:118 */         Enchantment ench = null;
/* 120:119 */         if (isInt(args[1])) {
						//ench = Enchantment.values()[Integer.parseInt(args[1])];
/* 121:120 */           ench = Enchantment.getById(Integer.parseInt(args[1]));
/* 122:    */         } else {
/* 123:122 */           ench = (Enchantment)this.enchants.get(args[1]);
/* 124:    */         }
/* 125:124 */         if (ench == null)
/* 126:    */         {
/* 127:125 */           msg(p, "&6" + args[1] + "&a is no enchantment");
/* 128:126 */           return true;
/* 129:    */         }
/* 130:128 */         if ((args[0].equalsIgnoreCase("increase")) || (args[0].equalsIgnoreCase("i")))
/* 131:    */         {
/* 132:129 */           int currentLevel = getEnchantmentLevel(p.getItemInHand(), ench);
/* 133:130 */           int maxlevel = this.MAX_ENCHANT_LEVEL_DEFAULT;
/* 134:131 */           if (permissions.has(p, "enchantit.enchantmore")) {
/* 135:132 */             maxlevel = this.MAX_ENCHANT_LEVEL_PERM;
/* 136:    */           }
/* 137:134 */           if ((currentLevel < ((Integer)this.enchantMaxLevels.get(ench)).intValue()) && (currentLevel < maxlevel))
/* 138:    */           {
/* 139:135 */             int newLevel = currentLevel + 1;
/* 140:136 */             int levelcost = calcLevel(currentLevel, newLevel);
/* 141:137 */             if ((p.getLevel() >= levelcost) && (addEnchantment(p.getItemInHand(), ench, newLevel)))
/* 142:    */             {
/* 143:138 */               msg(p, "&aIncreased &6" + ench.getName() + "&a by &61&a level, now: &6" + String.valueOf(newLevel) + "&a taking &6" + String.valueOf(levelcost) + "&a level/s");
/* 144:139 */               p.setLevel(p.getLevel() - levelcost);
/* 145:    */             }
/* 146:140 */             else if (p.getLevel() < levelcost)
/* 147:    */             {
/* 148:141 */               msg(p, "&aYou need &6" + String.valueOf(levelcost) + "&a level/s");
/* 149:    */             }
/* 150:    */           }
/* 151:    */           else
/* 152:    */           {
/* 153:144 */             msg(p, "&aMax enchantment level");
/* 154:    */           }
/* 155:    */         }
/* 156:146 */         else if ((args[0].equalsIgnoreCase("decrease")) || (args[0].equalsIgnoreCase("d")))
/* 157:    */         {
/* 158:147 */           int enchantmentLevel = getEnchantmentLevel(p.getItemInHand(), ench);
/* 159:148 */           if (enchantmentLevel > 0)
/* 160:    */           {
/* 161:149 */             int levelcost = (int)(calcLevel(enchantmentLevel - 1, enchantmentLevel) * this.LEVEL_BACK);
/* 162:150 */             enchantmentLevel--;
/* 163:151 */             if (setEnchantment(p.getItemInHand(), ench, enchantmentLevel))
/* 164:    */             {
/* 165:152 */               msg(p, "&aDecreased &6" + ench.getName() + "&a by &61&a level, now: &6" + String.valueOf(enchantmentLevel) + "&a giving &6" + String.valueOf(levelcost) + "&a level/s");
/* 166:153 */               p.setLevel(p.getLevel() + levelcost);
/* 167:    */             }
/* 168:    */           }
/* 169:    */           else
/* 170:    */           {
/* 171:156 */             msg(p, "&aYou can't decrease beneath zero");
/* 172:    */           }
/* 173:    */         }
/* 174:158 */         else if ((args[0].equalsIgnoreCase("max")) || (args[0].equalsIgnoreCase("m")))
/* 175:    */         {
/* 176:159 */           int enchantmentLevel = getEnchantmentLevel(p.getItemInHand(), ench);
/* 177:160 */           int maxlevel = this.MAX_ENCHANT_LEVEL_DEFAULT;
/* 178:161 */           if (permissions.has(p, "enchantit.enchantmore")) {
/* 179:162 */             maxlevel = this.MAX_ENCHANT_LEVEL_PERM;
/* 180:    */           }
/* 181:164 */           if ((enchantmentLevel < ((Integer)this.enchantMaxLevels.get(ench)).intValue()) && (enchantmentLevel < maxlevel))
/* 182:    */           {
/* 183:165 */             int levelcost = calcLevel(enchantmentLevel, ((Integer)this.enchantMaxLevels.get(ench)).intValue());
/* 184:166 */             enchantmentLevel = ((Integer)this.enchantMaxLevels.get(ench)).intValue();
/* 185:167 */             if (enchantmentLevel > maxlevel) {
/* 186:168 */               enchantmentLevel = maxlevel;
/* 187:    */             }
/* 188:170 */             if ((p.getLevel() >= levelcost) && (addEnchantment(p.getItemInHand(), ench, enchantmentLevel)))
/* 189:    */             {
/* 190:171 */               msg(p, "&6" + ench.getName() + "&a set to max level: &6" + String.valueOf(this.enchantMaxLevels.get(ench)) + "&a taking &6" + String.valueOf(levelcost) + "&a level/s");
/* 191:172 */               p.setLevel(p.getLevel() - levelcost);
/* 192:    */             }
/* 193:173 */             else if (p.getLevel() < levelcost)
/* 194:    */             {
/* 195:174 */               msg(p, "&aYou need &6" + String.valueOf(levelcost) + "&a level/s");
/* 196:175 */               msg(p, "&aTry &6increase &aor &6set &ato set a lower amount of levels at once");
/* 197:    */             }
/* 198:    */           }
/* 199:    */           else
/* 200:    */           {
/* 201:178 */             msg(p, "&aMax enchantment level");
/* 202:    */           }
/* 203:    */         }
/* 204:180 */         else if ((args[0].equalsIgnoreCase("set")) || (args[0].equalsIgnoreCase("s")))
/* 205:    */         {
/* 206:181 */           int enchantmentLevel = getEnchantmentLevel(p.getItemInHand(), ench);
/* 207:182 */           int maxlevel = this.MAX_ENCHANT_LEVEL_DEFAULT;
/* 208:183 */           if (permissions.has(p, "enchantit.enchantmore")) {
/* 209:184 */             maxlevel = this.MAX_ENCHANT_LEVEL_PERM;
/* 210:    */           }
/* 211:186 */           int setto = -1;
/* 212:187 */           if (isInt(args[2]))
/* 213:    */           {
/* 214:188 */             setto = Integer.parseInt(args[2]);
/* 215:    */           }
/* 216:    */           else
/* 217:    */           {
/* 218:190 */             msg(p, "&aThird argument: &6" + args[2] + "&a must be a number");
/* 219:191 */             return true;
/* 220:    */           }
/* 221:193 */           if ((enchantmentLevel > 0) && (enchantmentLevel < ((Integer)this.enchantMaxLevels.get(ench)).intValue()) && (enchantmentLevel < maxlevel))
/* 222:    */           {
/* 223:194 */             if ((setto <= ((Integer)this.enchantMaxLevels.get(ench)).intValue()) && (setto <= maxlevel) && (setto >= 0))
/* 224:    */             {
/* 225:195 */               if (enchantmentLevel > setto)
/* 226:    */               {
/* 227:197 */                 int levelcost = (int)(calcLevel(setto, enchantmentLevel) * this.LEVEL_BACK);
/* 228:198 */                 int dif = enchantmentLevel - setto;
/* 229:199 */                 enchantmentLevel = setto;
/* 230:200 */                 if (setEnchantment(p.getItemInHand(), ench, enchantmentLevel))
/* 231:    */                 {
/* 232:201 */                   msg(p, "&aDecreased &6" + ench.getName() + "&a by &6" + String.valueOf(dif) + "&a level, now: &6" + String.valueOf(enchantmentLevel) + "&a giving &6" + String.valueOf(levelcost) + "&a level/s");
/* 233:202 */                   p.setLevel(p.getLevel() + levelcost);
/* 234:    */                 }
/* 235:    */               }
/* 236:204 */               else if (enchantmentLevel < setto)
/* 237:    */               {
/* 238:207 */                 int levelcost = calcLevel(enchantmentLevel, setto);
/* 239:208 */                 int dif = setto - enchantmentLevel;
/* 240:209 */                 enchantmentLevel = setto;
/* 241:210 */                 if ((p.getLevel() >= levelcost) && (addEnchantment(p.getItemInHand(), ench, enchantmentLevel)))
/* 242:    */                 {
/* 243:211 */                   msg(p, "&aIncreased &6" + ench.getName() + "&a by &6" + String.valueOf(dif) + "&a level, now: &6" + String.valueOf(enchantmentLevel) + "&a taking &6" + String.valueOf(levelcost) + "&a level/s");
/* 244:212 */                   p.setLevel(p.getLevel() - levelcost);
/* 245:    */                 }
/* 246:213 */                 else if (p.getLevel() < levelcost)
/* 247:    */                 {
/* 248:214 */                   msg(p, "&aYou need &6" + String.valueOf(levelcost) + "&a level/s");
/* 249:    */                 }
/* 250:    */               }
/* 251:    */             }
/* 252:    */             else {
/* 253:218 */               msg(p, "&aThird argument: &6" + args[2] + "&a must be smaller then max enchant level: &6" + String.valueOf(this.enchantMaxLevels.get(ench)));
/* 254:    */             }
/* 255:    */           }
/* 256:    */           else {
/* 257:221 */             msg(p, "&aMax enchantment level");
/* 258:    */           }
/* 259:    */         }
/* 260:223 */         else if ((args[0].equalsIgnoreCase("remove")) || (args[0].equalsIgnoreCase("r")))
/* 261:    */         {
/* 262:224 */           int enchantmentLevel = getEnchantmentLevel(p.getItemInHand(), ench);
/* 263:225 */           if ((enchantmentLevel > 0) && (removeEnchantment(p.getItemInHand(), ench)))
/* 264:    */           {
/* 265:226 */             int levelcost = calcLevel(0, enchantmentLevel);
/* 266:227 */             msg(p, "&aEnchantment removed: &6" + ench.getName() + "&a gave you &6" + String.valueOf(levelcost) + "&a level/s");
/* 267:228 */             p.setLevel((int)(p.getLevel() + levelcost * this.LEVEL_BACK));
/* 268:    */           }
/* 269:    */           else
/* 270:    */           {
/* 271:230 */             msg(p, "&aNo &6" + ench.getName() + " &aon your item");
/* 272:    */           }
/* 273:    */         }
/* 274:    */       }
/* 275:233 */       else if (args.length == 1)
/* 276:    */       {
/* 277:235 */         sender.sendMessage("§6Enchantment List:");
/* 278:236 */         for (int i = 0; i < this.enchantStrings.size(); i++) {
/* 279:237 */           msg(sender, "  §a" + ChatColor.translateAlternateColorCodes('&', (String)this.enchantStrings.get(i)));
/* 280:    */         }
/* 281:    */       }
/* 282:239 */       else if (args.length == 0)
/* 283:    */       {
/* 284:241 */         int maxlevel = this.MAX_ENCHANT_LEVEL_DEFAULT;
/* 285:242 */         if (((sender instanceof Player)) && (permissions.has((Player)sender, "enchantit.enchantmore"))) {
/* 286:243 */           maxlevel = this.MAX_ENCHANT_LEVEL_PERM;
/* 287:    */         }
/* 288:245 */         msg(sender, "§eEnchantIt v" + plugin.getDescription().getVersion() + " help");
/* 289:246 */         msg(sender, "§6/eIt increase&a/&6i &b<name/id>");
/* 290:247 */         msg(sender, "  §aAdds 1 level to the enchantment");
/* 291:248 */         msg(sender, "§6/eIt decrease&a/&6d &b<name/id>");
/* 292:249 */         msg(sender, "  §aRemoves 1 level of the enchantment, 50% exp back");
/* 293:250 */         msg(sender, "§6/eIt max&a/&6m &b<name/id>");
/* 294:251 */         msg(sender, "  §aMaximize the enchantment, max level &c" + String.valueOf(maxlevel));
/* 295:252 */         msg(sender, "§6/eIt remove&a/&6r &b<name/id>");
/* 296:253 */         msg(sender, "  §aRemoves the enchantment completely, 50% exp back");
/* 297:254 */         msg(sender, "§6/eIt set&a/&6s &b<name/id> <level>");
/* 298:255 */         msg(sender, "  §aSets enchantment to &b<level>&a, 50% exp if smaller then current");
/* 299:256 */         msg(sender, "§6/eIt list");
/* 300:257 */         msg(sender, "  §aLists all enchantments with IDs");
/* 301:    */       }
/* 302:259 */       return true;
/* 303:    */     }
/* 304:261 */     return false;
/* 305:    */   }
/* 306:    */   
/* 307:    */   private void msg(CommandSender sender, String msg)
/* 308:    */   {
/* 309:265 */     sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
/* 310:    */   }
/* 311:    */   
/* 312:    */   private void msg(Player sender, String msg)
/* 313:    */   {
/* 314:269 */     sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
/* 315:    */   }
/* 316:    */   
/* 317:    */   private int calcLevel(int from, int to)
/* 318:    */   {
/* 319:273 */     int levelsneeded = 0;
/* 320:274 */     for (int i = from + 1; i <= to; i++) {
/* 324:278 */        levelsneeded += LEVEL_COST_PER_SKILL_LEVEL + LEVEL_COST_PER_SKILL_LEVEL * (i * MULT_PER_SKILL_LEVEL);
/* 326:    */     }
					if(levelsneeded > MAX_LEVEL_COST){
						levelsneeded = MAX_LEVEL_COST;
					
					}
/* 327:282 */     return levelsneeded;
/* 328:    */   }
/* 329:    */   
/* 330:    */   private boolean isInt(String input)
/* 331:    */   {
/* 332:    */     try
/* 333:    */     {
/* 334:287 */       Integer.parseInt(input);
/* 335:288 */       return true;
/* 336:    */     }
/* 337:    */     catch (NumberFormatException nFE) {}
/* 338:290 */     return false;
/* 339:    */   }
/* 340:    */   
/* 341:    */   private boolean addEnchantment(ItemStack item, Enchantment enchantment, int level)
/* 342:    */   {
/* 343:    */     try
/* 344:    */     {
/* 345:296 */       item.addUnsafeEnchantment(enchantment, level);
/* 346:297 */       return true;
/* 347:    */     }
/* 348:    */     catch (Exception ex) {}
/* 349:299 */     return false;
/* 350:    */   }
/* 351:    */   
/* 352:    */   private int getEnchantmentLevel(ItemStack item, Enchantment enchantment)
/* 353:    */   {
/* 354:    */     try
/* 355:    */     {
/* 356:305 */       int level = item.getEnchantmentLevel(enchantment);
/* 357:306 */       if (level < 0) {
/* 358:307 */         return 0;
/* 359:    */       }
/* 360:309 */       return level;
/* 361:    */     }
/* 362:    */     catch (Exception ex) {}
/* 363:311 */     return 0;
/* 364:    */   }
/* 365:    */   
/* 366:    */   private boolean removeEnchantment(ItemStack item, Enchantment enchantment)
/* 367:    */   {
/* 368:    */     try
/* 369:    */     {
/* 370:317 */       item.removeEnchantment(enchantment);
/* 371:318 */       return true;
/* 372:    */     }
/* 373:    */     catch (Exception ex) {}
/* 374:320 */     return false;
/* 375:    */   }
/* 376:    */   
/* 377:    */   private boolean setEnchantment(ItemStack item, Enchantment enchantment, int level)
/* 378:    */   {
/* 379:    */     try
/* 380:    */     {
/* 381:326 */       item.removeEnchantment(enchantment);
/* 382:327 */       if (level > 0) {
/* 383:328 */         item.addUnsafeEnchantment(enchantment, level);
/* 384:    */       }
/* 385:330 */       return true;
/* 386:    */     }
/* 387:    */     catch (Exception ex) {}
/* 388:332 */     return false;
/* 389:    */   }
/* 390:    */   
/* 391:    */   private void log(String msg)
/* 392:    */   {
/* 393:337 */     getServer().getLogger().info("[EnchantIt] " + msg);
/* 394:    */   }
/* 395:    */ }


/* Location:           C:\Users\ADoby\Downloads\EnchantIt.jar
 * Qualified Name:     enchantit.EnchantIt
 * JD-Core Version:    0.7.0.1
 */