package org.lucagoc.wakecommander;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.io.*;
import java.net.*;

@Plugin(
        id = "wakecommander",
        name = "WakeCommander",
        version = "1.0-SNAPSHOT"
)
public class WakeCommander {


    private final List<UUID> connectedPlayers = new ArrayList<>();

    @Getter
    private static Logger logger;

    @Getter
    private final ProxyServer proxy;

    @Getter
    private static YamlDocument config;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("wake").aliases("wk").plugin(this).build();

        BrigadierCommand wakeCommand = WakeCommand.createBrigadierCommand(proxy);

        commandManager.register(commandMeta, wakeCommand);
    }

    public static void wake() {

        String adresseDestinataire = "127.0.0.1";
        int portDestinataire = 12345;

        try {
            // Creating the socket
            Socket socket = new Socket(adresseDestinataire, portDestinataire);

            // Stream to send the data
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);

            // Sending data
            writer.println("THE MAGIC KEY"); // C'est la qu'il faut insérer la clé !

            // Closing socket and writer
            writer.close();
            socket.close();
        } catch (UnknownHostException e) {
            logger.info("!!UnknownHostException!! : " + e.getMessage());
        } catch (IOException e) {
            logger.info("!!IOException!! : " + e.getMessage());
        }
    }

    @Inject
    public WakeCommander(Logger logger, ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.proxy = proxy;

        try {
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    getClass().getResourceAsStream("/config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Could not create/load plugin config ! This plugin will shutdown");
            Optional<PluginContainer> container = proxy.getPluginManager().getPlugin("wakecommander");
            container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }

        logger.info("WakeCommander plugin initialized !");
    }

    @Subscribe
    public void onServerPreConnectionEvent(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        if (!connectedPlayers.contains(player.getUniqueId())) {
            connectedPlayers.add(player.getUniqueId());
            this.logger.info("Player has not joined the session, adding to the array");
        }
    }

    @Subscribe
    public void onPlayerLeaveEvent(DisconnectEvent event) {
        Player player = event.getPlayer();
        connectedPlayers.remove(player.getUniqueId());
        this.logger.info("Player has left the session, removing from the array");
    }
}
