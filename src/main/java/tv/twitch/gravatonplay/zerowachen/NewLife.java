package tv.twitch.gravatonplay.zerowachen;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.particles.data.OrdinaryColor;
import dev.esophose.playerparticles.styles.DefaultStyles;
import net.ess3.api.MaxMoneyException;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.logging.Logger;

// public final class PlayerLeash implements Listener {

public final class NewLife implements Listener {
    private final Main plugin;
    private final Logger log;
    private FileConfiguration conf;
    private com.earth2me.essentials.Essentials Essentials;
    private PlayerParticlesAPI ppAPI;

    public NewLife(Main plugin, Logger log,PlayerParticlesAPI ppAPI){
        this.plugin = plugin;
        this.log = log;
        this.conf = this.plugin.getConfig();
        this.Essentials = (com.earth2me.essentials.Essentials) this.plugin.getServer().getPluginManager().getPlugin("Essentials");

        // this is ugly i know. KeK
        this.ppAPI = ppAPI;
        if(ppAPI == null){
            log.warning("Particles failed, continue without!");
        }

    }

    @EventHandler
    public void onPlayerKilledByPlayer(PlayerDeathEvent event){
        if( event.getEntity().getKiller() != null && event.getEntity().getKiller() instanceof Player){
            Player target = event.getPlayer();

            // get the killer and his data.
            Player source = target.getKiller();
            PersistentDataContainer sourceData = source.getPersistentDataContainer();

            // let the target player keep his stuff and prevent drops.
            event.setKeepLevel(true);
            event.setKeepInventory(true);
            event.setShouldDropExperience(false);

            // delete the drops.
            for(ItemStack i : event.getDrops()) {
                i.setType(Material.AIR);
            }

            double x = BigDecimal.valueOf(event.getEntity().getLocation().getX()).setScale(0, RoundingMode.HALF_UP).doubleValue();
            double z = BigDecimal.valueOf(event.getEntity().getLocation().getZ()).setScale(0, RoundingMode.HALF_UP).doubleValue();


            BigDecimal balance = new BigDecimal(0);

            //////////////////////////////////////////////////////////////////
            Location KillLocation = event.getEntity().getLocation();

            if( true ){ // disabled rn to avoid some bugs, reanbling later when code is working like intended.
                if (!event.getPlayer().getWorld().getName().contains("nether")){
                    event.setDeathMessage("§4§l┇ §cIm Königreich bei den Koordinaten §eX: "+x+" §cund §eZ: "+z+" §cist ein Mord geschehen!");
                    log.info(""+ChatColor.RED+event.getPlayer().getKiller().getName()+" hat "+event.getPlayer().getName()+" in "+event.getPlayer().getWorld().getName()+" getötet.");

                    Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new DRunnable(this.conf.getString("Discord_Webhook_url")){
                        @Override
                        public void run() {
                            DHook webhook = new DHook(this.webhook_url);
                            webhook.setContent(""+event.getPlayer().getKiller().getName()+" hat "+event.getPlayer().getName()+" in "+event.getPlayer().getWorld().getName()+"bei: X:"+x+" Z:"+z+" getötet. [ /minecraft:tp "+x+" 100 "+z+" ]");
                            webhook.setTts(false);
                            try {
                                webhook.execute();
                            } catch (IOException e) {
                                log.warning("Failed to Post to Discord!");
                            }
                        }
                    });

                } else {
                    event.setDeathMessage("§4§l┇ §cIm Nether bei den Koordinaten §eX: "+x+" §cund §eZ: "+z+" §cist ein Mord geschehen!");
                    log.info(""+ChatColor.RED+event.getPlayer().getKiller().getName()+" hat "+event.getPlayer().getName()+" in "+event.getPlayer().getWorld().getName()+" getötet.");
                }
            }else{
                event.setDeathMessage("§4§l┇ §c"+event.getPlayer().getName()+" ist gestorben.");
                log.info(""+ChatColor.RED+event.getPlayer().getKiller().getName()+" hat "+event.getPlayer().getName()+" in "+event.getPlayer().getWorld().getName()+" getötet.");

            }



            sourceData.set(new NamespacedKey(plugin, "killer"), PersistentDataType.INTEGER, 1);

            if(ppAPI != null){
                ppAPI.addActivePlayerParticle(source,ParticleEffect.DUST, DefaultStyles.TRAIL, new OrdinaryColor(255,0,0));
            }
            source.sendMessage(ChatColor.RED+"An dir klebt nun das Blut unschuldiger!");

            try {
                balance = com.earth2me.essentials.api.Economy.getMoneyExact(target.getUniqueId());

                if(balance.compareTo(new BigDecimal(0)) == 1){

                    BigDecimal theftAmount = balance.multiply(new BigDecimal("0.01"));

                    try{
                        com.earth2me.essentials.api.Economy.subtract(target.getUniqueId(),theftAmount); //setMoney(target.getUniqueId(),targetAmountLeft);
                        com.earth2me.essentials.api.Economy.add(Objects.requireNonNull(target.getKiller()).getUniqueId(),theftAmount);
                        Objects.requireNonNull(target.getKiller()).sendMessage("Du hast "+target.getName()+" "+theftAmount.setScale(2, RoundingMode.HALF_UP)+" geklaut!");
                        target.sendMessage("§c§l┇ §7Dir wurden §e" + theftAmount.setScale(2, RoundingMode.HALF_UP) + "⛃ §7geklaut.");
                    }catch(NoLoanPermittedException e){
                        Objects.requireNonNull(target.getKiller()).sendMessage("§c§l┇ §7Diese Person hat kein Geld, welches du klauen könntest.");
                    }catch(MaxMoneyException err){
                        Objects.requireNonNull(target.getKiller()).sendMessage("Dein Konto ist voll, du kannst kein Geld mehr nehmen.");
                    }
                }else{
                    Objects.requireNonNull(target.getKiller()).sendMessage("§c§l┇ §7Diese Person hat kein Geld, welches du klauen könntest.");
                }
            } catch(UserDoesNotExistException e){
                if(!com.earth2me.essentials.api.Economy.playerExists(target.getUniqueId())){
                    com.earth2me.essentials.api.Economy.createNPC(target.getName());
                    balance = new BigDecimal(0);
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // used to notify only in distance.
    private boolean inRange(Location KLocation) {

        double NRange = this.plugin.getConfig().getDouble("kill_notify_range"); // 600

        double centerX = this.plugin.getConfig().getDouble("kill_notify_range"); // 0
        double centerZ = this.plugin.getConfig().getDouble("kill_notify_range"); // 0

        double KX = KLocation.getX();
        double KZ = KLocation.getZ();

        log.info("ist es das?!?!??!:  "+( -620 > 600 ));
        if(
                ( ( KX <= ( centerX + NRange ) && KX >= 0 ) || ( KX >= ( centerX - NRange ) && KX <= 0 ) ) &&
                ( ( KZ <= ( centerZ + NRange ) && KZ >= 0 ) || ( KZ >= ( centerZ - NRange ) && KZ <= 0 ) )
        ){
            return true;
        }else{
            return false;
        }
    }
    //////////////////////////////////////////////////////////////////////////

    @EventHandler
    public void NewLife(PlayerRespawnEvent e){
        Player player = e.getPlayer();
        PersistentDataContainer playerData = player.getPersistentDataContainer();

        // && player.getKiller() instanceof Player
        if( !Essentials.getUser(player).isJailed() ){

            if(playerData.get(new NamespacedKey(plugin, "killer"), PersistentDataType.INTEGER) == 1){
                playerData.set(new NamespacedKey(plugin, "killer"), PersistentDataType.INTEGER, 0);
                if(ppAPI != null){
                    ppAPI.resetActivePlayerParticles(player);
                }
            }

            playerData.set(new NamespacedKey(this.plugin, "new_life"), PersistentDataType.INTEGER, 1);

            double x = this.conf.getDouble("respawn.x");
            double y = this.conf.getDouble("respawn.y");
            double z = this.conf.getDouble("respawn.z");

            e.setRespawnLocation(new Location(Bukkit.getWorld(Objects.requireNonNull(this.conf.getString("respawn_world"))), x,y,z));

            Bukkit.getWorld(Objects.requireNonNull(this.conf.getString("respawn_world"))).strikeLightningEffect(new Location(Bukkit.getWorld(Objects.requireNonNull(this.conf.getString("respawn_world"))), x,y,z));

            new BukkitRunnable(){
                public void run(){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,100,2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,100,1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,560,1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,620,255),true);
                }
            }.runTaskLater(this.plugin,2l);

            player.sendMessage(ChatColor.YELLOW+"=====================================================");
            player.sendMessage(ChatColor.GREEN+"Willkommen in deinem neuen Leben!");
            player.sendMessage("Du wurdest "+ ChatColor.RED +"getötet"+ChatColor.RESET+" und deine Seele "+ ChatColor.RED +"beschädigt"+ ChatColor.RESET +".");
            player.sendMessage(ChatColor.RED+"Du erinnerst dich nicht daran, was passiert ist oder wer es war!");
            player.sendMessage(ChatColor.YELLOW+"=====================================================");

            new BukkitRunnable(){
                public void run(){
                    if( playerData.get(new NamespacedKey(plugin,"new_life"),PersistentDataType.INTEGER) == 1) {
                        playerData.set(new NamespacedKey(plugin, "new_life"), PersistentDataType.INTEGER, 0);
                    }
                }
            }.runTaskLater(this.plugin,20L*30);

        }
    }

    @EventHandler
    public void NewLifeCommand(PlayerCommandPreprocessEvent e){
        Player player = e.getPlayer();
        PersistentDataContainer playerData = player.getPersistentDataContainer();

        if( playerData.get(new NamespacedKey(plugin,"new_life"),PersistentDataType.INTEGER) == 1 &&
                !(player.hasPermission("zeroguard.newlife.cblockbypass"))) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED+"Du erinnerst dich noch nicht ganz, wie das funktioniert...");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        PersistentDataContainer playerData = player.getPersistentDataContainer();
        if(playerData.get(new NamespacedKey(this.plugin, "ko"), PersistentDataType.INTEGER) == null){
            playerData.set(new NamespacedKey(this.plugin, "ko"), PersistentDataType.INTEGER, 0);
        }

        if(playerData.get(new NamespacedKey(this.plugin, "chainable"), PersistentDataType.INTEGER) == null){
            playerData.set(new NamespacedKey(this.plugin, "chainable"), PersistentDataType.INTEGER, 0);
        }

        if(playerData.get(new NamespacedKey(this.plugin, "killer"), PersistentDataType.INTEGER) == null){
            playerData.set(new NamespacedKey(this.plugin, "killer"), PersistentDataType.INTEGER, 0);
        }

        if(playerData.get(new NamespacedKey(this.plugin, "new_life"), PersistentDataType.INTEGER) == null){
            playerData.set(new NamespacedKey(this.plugin, "new_life"), PersistentDataType.INTEGER, 0);

        }else if(playerData.get(new NamespacedKey(this.plugin, "new_life"), PersistentDataType.INTEGER) == 1){
            new BukkitRunnable(){
                public void run(){
                    if( playerData.get(new NamespacedKey(plugin,"new_life"),PersistentDataType.INTEGER) == 1) {
                        playerData.set(new NamespacedKey(plugin, "new_life"), PersistentDataType.INTEGER, 0);
                    }
                }
            }.runTaskLater(this.plugin,20L*30);
        }
    }

    @EventHandler
    public void onPlayerAirChangeEvent(EntityAirChangeEvent e){
        if( e.getEntity() instanceof Player ){
            Player target = (Player)e.getEntity();
            PersistentDataContainer targetData = target.getPersistentDataContainer();

            if(target.getRemainingAir() <= 200){
                if(targetData.get(new NamespacedKey(plugin, "killer"), PersistentDataType.INTEGER) != null &&
                    targetData.get(new NamespacedKey(plugin, "killer"), PersistentDataType.INTEGER) == 1){

                    targetData.set(new NamespacedKey(plugin, "killer"), PersistentDataType.INTEGER, 0);
                    if(ppAPI != null){
                        ppAPI.resetActivePlayerParticles(target);
                    }
                    target.sendMessage(ChatColor.GREEN+"Du fühlst dich gereinigt.");
                }
            }
        }
    }
}
