package tv.twitch.gravatonplay.zerowachen;

import com.earth2me.essentials.BalanceTopImpl;
import net.ess3.api.MaxMoneyException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.logging.Logger;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

public final class GuardHit implements Listener {
    private final Main plugin;
    private final Logger log;
    private FileConfiguration conf;
    private String GaurdPerm;
    private int KOHits;

    public GuardHit(Main plugin, Logger log){
        this.plugin = plugin;
        this.log = log;

        this.conf = this.plugin.getConfig();

        GaurdPerm = this.conf.getString("wachen_permissions");
        KOHits = this.conf.getInt("ko_hits");
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player &&
                e.getDamager() instanceof Player &&
                e.getCause() != EntityDamageEvent.DamageCause.THORNS){

            Player target = (Player) e.getEntity();
            Player source = (Player) e.getDamager();

            PersistentDataContainer targetPlayer = target.getPersistentDataContainer();

            if(source.getInventory().getItemInMainHand().getType() == Material.STICK &&
                    source.hasPermission(GaurdPerm) &&
                    targetPlayer.get(new NamespacedKey(plugin,"ko"),PersistentDataType.INTEGER) == 0){
                // Stick, Player, Permission Match
                double HealthPart = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / KOHits;
                double ActualHealth = target.getHealth();

                if(ActualHealth > (HealthPart+1)) {
                    target.setHealth(ActualHealth - HealthPart);
                }else{
                    log.info(source.getName()+" hat "+ target.getName()+" K.O. geschlagen!");
                    target.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,620,255),true);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,600,255),true);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,600,1),true);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,600,-127),true);

                    targetPlayer.set(new NamespacedKey(this.plugin, "ko"), PersistentDataType.INTEGER, 1);
                    targetPlayer.set(new NamespacedKey(this.plugin, "chainable"), PersistentDataType.INTEGER, 0);

                    // not Ko after 30 sec.
                    new BukkitRunnable(){
                        public void run(){
                            if( targetPlayer.get(new NamespacedKey(plugin,"ko"),PersistentDataType.INTEGER) == 1) {
                                targetPlayer.set(new NamespacedKey(plugin, "ko"), PersistentDataType.INTEGER, 0);
                                targetPlayer.set(new NamespacedKey(plugin, "chainable"), PersistentDataType.INTEGER, 0);
                            }
                        }
                    }.runTaskLater(this.plugin,20l*30);

                    // Chainable after 5 seconds
                    new BukkitRunnable(){
                        public void run(){
                            if( targetPlayer.get(new NamespacedKey(plugin,"ko"),PersistentDataType.INTEGER) == 1 &&
                                    targetPlayer.get(new NamespacedKey(plugin,"chainable"),PersistentDataType.INTEGER) == 0) {
                                targetPlayer.set(new NamespacedKey(plugin, "chainable"), PersistentDataType.INTEGER, 1);
                            }
                        }
                    }.runTaskLater(this.plugin,20l*5);

                }
            }else if(source.getInventory().getItemInMainHand().getType() == Material.AIR &&
                    (target.getHealth() - e.getDamage()) <= 1){
                                e.setDamage(0);
            }
        }
    }
}
