package com.jabyftw.customserver.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Created by Rafael on 03/03/2015.
 */
public class CommandExecutor implements org.bukkit.command.CommandExecutor {

    private final JavaPlugin plugin;

    protected CommandExecutor(final JavaPlugin plugin, final String name, final String description, final String usageMessage) {
        this.plugin = plugin;
        final org.bukkit.command.CommandExecutor executor = this;
        Bukkit.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                {
                    ((PluginCommand) plugin.getCommand(name)
                            .setDescription(description)
                            .setUsage(usageMessage)
                                    //.setPermissionMessage(Main.getMessage("no-permission"))) // You can change this message if you want
                            .setExecutor(executor);
                }
            }
        }, 2);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] stringArguments) {
        if(command.getPermission() != null && command.getPermission().length() > 0 && !commandSender.hasPermission(command.getPermission())) {
            commandSender.sendMessage(command.getPermissionMessage());
            return true;
        }

        Argument[] objectArguments = new Argument[stringArguments.length];
        {
            for(int i = 0; i < stringArguments.length; i++) {
                objectArguments[i] = ArgumentType.handleArgument(commandSender, stringArguments[i]);
                //main.getLogger().info("* i=" + i + "/" + stringArguments.length + " " + objectArguments[i].toString());
            }
        }

        Method mostNear = null;
        int nearValue = Integer.MAX_VALUE;
        LinkedList<Object> objects = new LinkedList<>();

        for(Method currentMethod : getClass().getDeclaredMethods()) {
            if(currentMethod.isAnnotationPresent(CommandHandler.class)) {

                CommandHandler declaredAnnotation = currentMethod.getDeclaredAnnotation(CommandHandler.class);
                String additionalPermission = declaredAnnotation.additionalPermission();

                if(additionalPermission.length() == 0 || commandSender.hasPermission(additionalPermission)) {
                    Class<?>[] requiredArguments = currentMethod.getParameterTypes();

                    boolean isPlayerNeeded = false;

                    if(requiredArguments.length > 0 && currentMethod.getReturnType().isAssignableFrom(HandleResponse.class) &&
                            ((isPlayerNeeded = requiredArguments[0].isAssignableFrom(Player.class)) || requiredArguments[0].isAssignableFrom(CommandSender.class))) {

                        {
                            LinkedList<Object> objectList = new LinkedList<>();

                            if(objectArguments.length < (requiredArguments.length - 1)) {
                                continue; // Not enough arguments
                            }

                            Player player;
                            if(isPlayerNeeded && !(commandSender instanceof Player)) {
                                continue; // Can't handle this command on console
                            } else {
                                player = (Player) commandSender;
                            }

                            {
                                objectList.add(isPlayerNeeded ? player : commandSender);
                            }

                            Integer integer = doLoop(objectList, requiredArguments, objectArguments);

                            if(integer != null && integer <= nearValue) {
                                mostNear = currentMethod;
                                nearValue = integer;

                                objects.clear();
                                objects.addAll(objectList);

                                if(integer == 0) break; // Break method-iterator
                            } else {
                                objectList.clear();
                            }
                        }
                    }
                }
            }
        }

        if(mostNear == null) {
            commandSender.sendMessage(command.getUsage());
        } else {
            try {
                HandleResponse handleResponse = (HandleResponse) mostNear.invoke(this, objects.toArray());

                switch(handleResponse) {
                    case RETURN_HELP:
                        commandSender.sendMessage(command.getUsage());
                        break;
                    case RETURN_NO_PERMISSION:
                        commandSender.sendMessage(command.getPermissionMessage());
                        break;
                }

            } catch(IllegalAccessException | InvocationTargetException | ClassCastException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private Integer doLoop(LinkedList<Object> objectList, Class<?>[] requiredArguments, Argument[] objectArguments) {
        int objectIndex = 0;
        int argumentIndex = 1;

        for(; argumentIndex < requiredArguments.length; argumentIndex++) {
            Class<?> currentArgument = requiredArguments[argumentIndex];

            if(currentArgument.isArray()) { // If is array
                LinkedList<Object> objectArray = new LinkedList<>();

                while(objectIndex < objectArguments.length) { // While you have unprocessed objects
                    Object currentObject;

                    if((currentObject = objectArguments[objectIndex].getArgument(currentArgument)) != null) { // Add them until its over, or they're not compatible
                        objectArray.add(currentObject);
                        objectIndex++;
                    } else { // Not compatible, break
                        break;
                    }
                }

                objectList.add(objectArray.toArray());

                if(objectIndex >= objectArguments.length && argumentIndex < requiredArguments.length) // don't have more arguments
                    return null; // Return not enough arguments
            } else {
                Object currentObject;

                if(objectIndex < objectArguments.length) {
                    if((currentObject = objectArguments[objectIndex].getArgument(currentArgument)) != null) {
                        objectList.add(currentObject);
                        objectIndex++;
                    } else {
                        return null; // Incompatible
                    }
                } else {
                    return null; // Not enough arguments
                }
            }
        }

        return objectArguments.length - objectIndex; // 0 when all arguments were used, > 0 when there are remaining arguments
    }
}
