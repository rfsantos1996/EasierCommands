package com.jabyftw.easiercommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Rafael on 29/03/2015.
 */
public enum SenderType {

    PLAYER,
    CONSOLE,
    BOTH;

    public boolean canHandleCommandSender(CommandSender commandSender) {
        return this == BOTH || (commandSender instanceof Player && this == PLAYER) || (!(commandSender instanceof Player) && this == CONSOLE);
    }
}
