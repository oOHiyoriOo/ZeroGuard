package tv.twitch.gravatonplay.zerowachen;

import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    private Logger log;
    private PlayerParticlesAPI ppAPI;

    @Override
    public void onEnable() {
        log = getLogger(); // get logger to spam the console o.O
        log.info("Logger loaded.");

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerParticles")) {
            this.ppAPI = PlayerParticlesAPI.getInstance();
        }

        getServer().getPluginManager().registerEvents(new GuardHit(this,log), this);
        log.info("Loaded GuardHit.");

        getServer().getPluginManager().registerEvents(new PlayerLeash(this,log,this.ppAPI), this);
        log.info("Loaded PlayerLeash.");


        // When you want to access the API, check if the instance is null
        getServer().getPluginManager().registerEvents(new NewLife(this,log,this.ppAPI), this);
        log.info("Loaded NewLife.");

        this.saveDefaultConfig(); // only saved provided plugin conf is old doesn't exist.

        log.info("Zero Guard is now running!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.info("Zero Guard is now disabled!");
    }
}
