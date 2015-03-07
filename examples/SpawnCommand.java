/*
 * EasierCommand's default
 *
 * 5 lines of code
 */
public class SpawnCommand extends CommandExecutor {

    public SpawnCommand() {
        super(Main.getInstance(), "spawn", "Voltar a origem do servidor", "&cUso: &e/spawn");
    }

    // Note: EasierCommand support custom classes ('Jogador' means 'Player' in PT-BR), just add the support at ArgumentType

    /*
     * This command MUST be executed by a Player (that on my server is represented by 'Jogador') and there is no additional permission for this command
     * (except the one in plugin.yml)
     */
    @CommandHandler
    public HandleResponse handleDefaultCommand(Jogador jogador) {
        jogador.teleport(jogador.getPlayer().getWorld().getSpawnLocation(), true);
        return HandleResponse.RETURN;
    }

    /*
     * This command can be executed by any CommandSender (Player or console) that have this permission, and the single argument that is required is: Jogador
     *
     * Example of usage: /spawn player123
     */
    @CommandHandler(additionalPermission = "customserver.teleportation.spawn.others")
    public HandleResponse handleAdminCommand(CommandSender commandSender, Jogador player) {
        player.instantTeleport(player.getPlayer().getWorld().getSpawnLocation(), true);
        return HandleResponse.RETURN;
    }
}

/*
 * Bukkit's default
 *
 * 24 lines of code to do the same task
 */
public class SpawnCommandBukkitDefault implements org.bukkit.command.CommandExecutor {

    public SpawnCommandBukkitDefault() {
        Bukkit.getServer().getPluginCommand("spawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if(arguments.length > 0) {

            if(commandSender.hasPermission("customserver.teleportation.spawn.others")) {
                Jogador player = Main.getInstance().getJogador(arguments[0], false);
                if(player != null) {
                    player.instantTeleport(player.getPlayer().getWorld().getSpawnLocation(), true);
                    return true;
                } else {
                    commandSender.sendMessage(Main.getMessage("spawn.home-usage"));
                    return true;
                }
            } else {
                commandSender.sendMessage(Main.getMessage("no-permission"));
                return true;
            }

        } else if(commandSender instanceof Player) {
            Jogador jogador = Main.getInstance().getJogador(commandSender);
            jogador.teleport(jogador.getPlayer().getWorld().getSpawnLocation(), true);
            return true;

        } else {
            commandSender.sendMessage(Main.getMessage("not-on-console"));
            return true;
        }
    }
}
