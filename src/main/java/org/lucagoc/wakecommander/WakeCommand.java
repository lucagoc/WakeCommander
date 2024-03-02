package org.lucagoc.wakecommander;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Collection;

public class WakeCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy){
        LiteralCommandNode<CommandSource> wakeCommandsNode = LiteralArgumentBuilder.<CommandSource>literal("wake")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("server", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            Collection<RegisteredServer> servers = proxy.getAllServers();

                            servers.forEach(server ->{
                                try{

                                    String argument = ctx.getArgument("server", String.class);

                                    if(server.getServerInfo().getName().startsWith(argument)){
                                        builder.suggest(server.getServerInfo().getName(),
                                                VelocityBrigadierMessage.tooltip(
                                                        MiniMessage.miniMessage().deserialize("<rainbow>" + server.getServerInfo().getName()))
                                                );
                                    }
                                } catch (IllegalArgumentException e){
                                    builder.suggest(server.getServerInfo().getName(),
                                            VelocityBrigadierMessage.tooltip(
                                                    MiniMessage.miniMessage().deserialize("<rainbow>" + server.getServerInfo().getName())
                                            )
                                    );
                                }
                            });

                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String argumentProvided = context.getArgument("server", String.class);
                            WakeCommander.wake(argumentProvided); // argumentProvided

                            context.getSource().sendMessage(Component.text("Waking Server " + argumentProvided));
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(commandContext -> {

                    return Command.SINGLE_SUCCESS;
                })
        .build();

        return new BrigadierCommand(wakeCommandsNode);
    }
}
