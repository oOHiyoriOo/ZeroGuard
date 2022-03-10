package tv.twitch.gravatonplay.zerowachen;

import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Logger;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Jails;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public final class PlayerLeash implements Listener {
    private final Main plugin;
    private final Logger log;
    private FileConfiguration conf;
    private String GaurdPerm;
    private String JailName;
    private String JailTime;
    private com.earth2me.essentials.Essentials Essentials;
    private PlayerParticlesAPI ppAPI;

    public PlayerLeash(Main plugin, Logger log,PlayerParticlesAPI ppAPI){
        this.plugin = plugin;
        this.log = log;
        this.conf = this.plugin.getConfig();
        this.Essentials = (Essentials) this.plugin.getServer().getPluginManager().getPlugin("Essentials");

        GaurdPerm = this.conf.getString("wachen_permissions");
        JailName = this.conf.getString("jail_name");
        JailTime = this.conf.getString("jail_time");

        this.ppAPI = ppAPI;
        if(ppAPI == null){
            log.warning("Particles failed, continue without!");
        }

    }

    @EventHandler
    public void onLeash(PlayerInteractAtEntityEvent e) {
        final Player source = e.getPlayer();
        Entity targetEntity = e.getRightClicked();

        if(targetEntity instanceof Player && source.hasPermission(GaurdPerm) &&
                source.getInventory().getItemInMainHand().getType() == Material.LEAD &&
                e.getHand().equals(EquipmentSlot.HAND)){

            final Player target = (Player) targetEntity;

            PersistentDataContainer targetData = target.getPersistentDataContainer();
            PersistentDataContainer sourceData = source.getPersistentDataContainer();

            if(targetData.get(new NamespacedKey(this.plugin, "ko"), PersistentDataType.INTEGER) == 1 &&
                    targetData.get(new NamespacedKey(this.plugin, "chainable"), PersistentDataType.INTEGER) == 1){

                if(!Essentials.getUser(target).isJailed()){
                    target.sendMessage(target.getName()+" du bist nun festgenommen!");
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

                    String JailCommand = "jail "+target.getName()+" "+JailName+" "+ ( JailTime == "false" ? "" : JailTime);

                    target.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,560,1));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,100,255));

                    Bukkit.dispatchCommand(console,JailCommand);
                    log.info(ChatColor.YELLOW+"Spieler "+ChatColor.RED+target.getName()+ChatColor.YELLOW+" Jailed by: "+ChatColor.GREEN+source.getName()+ChatColor.RESET);
                }else{
                    source.sendMessage("Dieser Spieler ist bereits festgenommen!");
                }

            }else if(targetData.get(new NamespacedKey(this.plugin, "ko"), PersistentDataType.INTEGER) == 1 &&
                    targetData.get(new NamespacedKey(this.plugin, "chainable"), PersistentDataType.INTEGER) == 0){
                source.sendMessage("Du musst 5s warten bevor du jemanden einsperren kannst!");

            }else if(targetData.get(new NamespacedKey(this.plugin, "ko"), PersistentDataType.INTEGER) == 0){
                source.sendMessage("Du musst Spieler betäuben bevor du sie einsperren kannst!");
            }
        }
    }
}