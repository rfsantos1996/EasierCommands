package com.jabyftw.easiercommands;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rafael on 20/02/2015.
 */
public enum ArgumentType {

    LOCATION(Location.class),
    WORLD(World.class),

    TIME_DIFFERENCE(Long.class),
    NUMBER(Number.class), // Because of this, you need to use Double.class, Integer.class, etc instead of int, double, float

    ENTITY_TYPE(EntityType.class),
    POTION_TYPE(PotionType.class),
    MATERIAL(Material.class),

    PLAYER_NAME(Player.class),

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
            Location location = parseToLocation(
                    commandSender instanceof Player ?
                            ((Player) commandSender).getWorld() :
                            null,
                    string
            );
            if(location != null)
                argument.addArgumentType(ArgumentType.LOCATION, location);
        }

        { // Check for world
            World world = parseToWorld(string);
            if(world != null)
                argument.addArgumentType(ArgumentType.WORLD, world);
        }

        { // Check for materials
            Material material = parseToMaterial(string);
            if(material != null)
                argument.addArgumentType(MATERIAL, material);
        }

        { // Check for entity types
            EntityType entityType = parseToEntityType(string);
            if(entityType != null)
                argument.addArgumentType(ENTITY_TYPE, entityType);
        }

        { // Check for potion types
            PotionType potionType = parseToPotionType(string);
            if(potionType != null)
                argument.addArgumentType(POTION_TYPE, potionType);
        }

        { // Check for player names
            Player playerThatMatches = getPlayerThatMatches(string);
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
                long timeDifference = parseTimeDifference(string);
                argument.addArgumentType(TIME_DIFFERENCE, timeDifference);
            } catch(IllegalArgumentException ignored) {
            }
        }

        return argument;
    }

    /*
     * Some util to parse material, entity type, etc
     */

    public static Location parseToLocation(World world, String string) {
        String[] strings = string.split(";");
        double[] position = new double[3];

        if(strings.length < 3 || strings.length > 4)
            return null;

        if(strings.length == 4) {
            world = Bukkit.getWorld(strings[0]);
            System.arraycopy(strings, 1, strings, 0, strings.length - 1);
        }

        if(world == null)
            return null;

        for(int index = 0; index < strings.length; index++) {
            if(!NumberUtils.isNumber(strings[index]))
                return null;
            else
                position[index] = Double.parseDouble(strings[index]);
        }

        return new Location(world, position[0], position[1], position[2]);
    }

    public static World parseToWorld(String string) {
        World mostEqual = null;
        int equality = 2;

        for(World world : Bukkit.getServer().getWorlds()) {
            int equalityOfWords = equalityOfWords(world.getName(), string);

            if(equalityOfWords >= equality) {
                mostEqual = world;
                equality = equalityOfWords;
            }
        }

        return mostEqual;
    }

    public static Material parseToMaterial(String string) {
        Material mostEqual = null;
        int equality = 2;

        for(Material material : Material.values()) {
            boolean useUnderline = string.contains("_");

            int equalityOfWords = equalityOfWords(material.name().replaceAll("_", (useUnderline ? "" : "_")), string);
            if(equalityOfWords >= equality) {
                mostEqual = material;
                equality = equalityOfWords;
            }
        }

        return mostEqual;
    }

    public static EntityType parseToEntityType(String string) {
        EntityType mostEqual = null;
        int equality = 2;

        for(EntityType material : EntityType.values()) {
            boolean useUnderline = string.contains("_");

            int equalityOfWords = equalityOfWords(material.name().replaceAll("_", (useUnderline ? "" : "_")), string);
            if(equalityOfWords >= equality) {
                mostEqual = material;
                equality = equalityOfWords;
            }
        }

        return mostEqual;
    }

    public static PotionType parseToPotionType(String string) {
        PotionType mostEqual = null;
        int equality = 2;

        for(PotionType material : PotionType.values()) {
            boolean useUnderline = string.contains("_");

            int equalityOfWords = equalityOfWords(material.name().replaceAll("_", (useUnderline ? "" : "_")), string);
            if(equalityOfWords >= equality) {
                mostEqual = material;
                equality = equalityOfWords;
            }
        }

        return mostEqual;
    }

    public static Player getPlayerThatMatches(String string) {
        if(string == null || string.length() < 3) {
            return null;
        }

        Player mostEqual = null;
        int equalSize = 3;

        for(Player online : Bukkit.getServer().getOnlinePlayers()) {
            int thisSize = getEqualityOfNames(string.toCharArray(), online.getName().toCharArray());

            if(thisSize >= equalSize) {
                mostEqual = online;
                equalSize = thisSize;
            }
        }

        return mostEqual != null ? mostEqual : null;
    }

    /**
     * Source: Essentials (found through Ban-Management)
     * https://github.com/BanManagement/BanManager/ @ Util.java
     *
     * @param time string with the time, eg: "3w4h" - three weeks and four hours
     * @return the time in milliseconds
     */
    public static long parseTimeDifference(String time) {
        Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = timePattern.matcher(time);

        int years = 0, months = 0, weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0;
        boolean found = false;

        while(matcher.find()) {
            if(matcher.group() == null || matcher.group().isEmpty()) {
                continue;
            }

            for(int i = 0; i < matcher.groupCount(); i++) {
                if(matcher.group(i) != null && !matcher.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }

            if(found) {
                if(matcher.group(1) != null && !matcher.group(1).isEmpty())
                    years = Integer.parseInt(matcher.group(1));
                if(matcher.group(2) != null && !matcher.group(2).isEmpty())
                    months = Integer.parseInt(matcher.group(2));
                if(matcher.group(3) != null && !matcher.group(3).isEmpty())
                    weeks = Integer.parseInt(matcher.group(3));
                if(matcher.group(4) != null && !matcher.group(4).isEmpty())
                    days = Integer.parseInt(matcher.group(4));
                if(matcher.group(5) != null && !matcher.group(5).isEmpty())
                    hours = Integer.parseInt(matcher.group(5));
                if(matcher.group(6) != null && !matcher.group(6).isEmpty())
                    minutes = Integer.parseInt(matcher.group(6));
                if(matcher.group(7) != null && !matcher.group(7).isEmpty())
                    seconds = Integer.parseInt(matcher.group(7));
                break;
            }
        }

        if(!found)
            throw new IllegalArgumentException("Date can't be parsed");
        if(years > 20)
            throw new IllegalArgumentException("Date is too big");

        Calendar calendar = new GregorianCalendar();

        if(years > 0)
            calendar.add(Calendar.YEAR, years);
        if(months > 0)
            calendar.add(Calendar.MONTH, months);
        if(weeks > 0)
            calendar.add(Calendar.WEEK_OF_YEAR, weeks);
        if(days > 0)
            calendar.add(Calendar.DAY_OF_MONTH, days);
        if(hours > 0)
            calendar.add(Calendar.HOUR_OF_DAY, hours);
        if(minutes > 0)
            calendar.add(Calendar.MINUTE, minutes);
        if(seconds > 0)
            calendar.add(Calendar.SECOND, seconds);

        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }

    /*
     * Some util for word equality
     */

    public static int getEqualityOfNames(char[] firstWord, char[] secondWord) {
        if(firstWord.length > secondWord.length)  // do not accept search being bigger than player name. Jaby (4) < (5) Jaby2
            return 0;

        int equality = equalityOfChars(firstWord, secondWord);
        return secondWord.length > firstWord.length ? equality - (secondWord.length - firstWord.length) : equality;
    }

    public static int equalityOfWordsIgnoringLength(String sentence1, String sentence2) {
        String[] words1 = sentence1.split(" "),
                words2 = sentence2.split(" ");
        int equality = 0;

        for(String wordsSentence1 : words1) {
            int equalityOfWord = 0;

            for(String worldSentence2 : words2) { // for all words
                int thisEq = equalityOfChars(wordsSentence1.toCharArray(), worldSentence2.toCharArray()); // compare each word

                if(thisEq > equalityOfWord) { // if world is equal than other word
                    equalityOfWord = thisEq;
                }
            }

            equality += equalityOfWord; // add most equal word to the equality
        }

        return equality;
    }

    public static int equalityOfWords(String sentence1, String sentence2) {
        int equality = equalityOfWordsIgnoringLength(sentence1, sentence2),
                max = Math.max(sentence1.length(), sentence2.length()),
                min = Math.min(sentence1.length(), sentence2.length());

        return max > min ? equality - (max - min) : equality;
    }

    public static int equalityOfChars(char[] string1, char[] string2) {
        int length = Math.min(string1.length, string2.length),
                equality = 0;
        for(int i = 0; i < length; i++) {
            if(Character.toLowerCase(string1[i]) == Character.toLowerCase(string2[i])) {
                equality++;
            } else {
                equality--;
            }
        }
        return equality;
    }
}
