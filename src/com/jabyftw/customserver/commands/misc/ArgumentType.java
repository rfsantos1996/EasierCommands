package com.jabyftw.customserver.commands.misc;

import com.jabyftw.customserver.jogador.Jogador;
import com.jabyftw.customserver.util.Util;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;

/**
 * Created by Rafael on 20/02/2015.
 */
public enum ArgumentType {

    LOCATION(Location.class),
    WORLD(World.class),

    TIME_DIFFERENCE(Long.class),
    NUMBER(Number.class),

    ENTITY_TYPE(EntityType.class),
    POTION_TYPE(PotionType.class),
    MATERIAL(Material.class),

    PLAYER_NAME(Jogador.class),

    PLAYER_IP(String.class),
    STRING(String.class);

    private final Class<?> clazz;

    private ArgumentType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public static Argument handleArgument(CommandSender commandSender, String string) {
        final Argument argument = new Argument(string);

        { // Check for location
            Location location = Util.parseToLocation(
                    commandSender instanceof Player ?
                            ((Player) commandSender).getWorld() :
                            null,
                    string
            );
            if(location != null)
                argument.addArgumentType(ArgumentType.LOCATION, location);
        }

        { // Check for world
            World world = Util.parseToWorld(string);
            if(world != null)
                argument.addArgumentType(ArgumentType.WORLD, world);
        }

        { // Check for materials
            Material material = Util.parseToMaterial(string);
            if(material != null)
                argument.addArgumentType(MATERIAL, material);
        }

        { // Check for entity types
            EntityType entityType = Util.parseToEntityType(string);
            if(entityType != null)
                argument.addArgumentType(ENTITY_TYPE, entityType);
        }

        { // Check for potion types
            PotionType potionType = Util.parseToPotionType(string);
            if(potionType != null)
                argument.addArgumentType(POTION_TYPE, potionType);
        }

        { // Check for player names
            Jogador playerThatMatches = Util.getPlayerThatMatches(commandSender, string);
            if(playerThatMatches != null)
                argument.addArgumentType(PLAYER_NAME, playerThatMatches);
        }

        { // Check for player IP
            for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                if(player.getAddress().getAddress().getHostName().equalsIgnoreCase(string)) {
                    argument.addArgumentType(PLAYER_IP, argument);
                    break; // Don't repeat
                }
            }
        }

        { // Check for numbers
            if(NumberUtils.isNumber(string))
                argument.addArgumentType(NUMBER, Double.parseDouble(string));
        }

        { // Check for date difference
            try {
                long timeDifference = Util.parseTimeDifference(string);
                argument.addArgumentType(TIME_DIFFERENCE, timeDifference);
            } catch(IllegalArgumentException ignored) {
            }
        }

        return argument;
    }
}
