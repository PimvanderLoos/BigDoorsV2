package nl.pim16aap2.bigdoors.commands.subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.commands.CommandActionNotAllowedException;
import nl.pim16aap2.bigdoors.commands.CommandData;
import nl.pim16aap2.bigdoors.commands.CommandInvalidVariableException;
import nl.pim16aap2.bigdoors.commands.CommandPermissionException;
import nl.pim16aap2.bigdoors.commands.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.managers.CommandManager;
import nl.pim16aap2.bigdoors.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.RotateDirection;

public class SubCommandSetRotation extends SubCommand
{
    protected static final String help = "Change the rotation direction of a door";
    protected static final String argsHelp = "<doorUID/Name> <CLOCK || COUNTER || ANY>";
    protected static final int minArgCount = 3;
    protected static final CommandData command = CommandData.SETROTATION;

    public SubCommandSetRotation(final BigDoors plugin, final CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    public void execute(CommandSender sender, DoorBase door, RotateDirection openDir)
    {
        plugin.getDatabaseManager().updateDoorOpenDirection(door.getDoorUID(), openDir);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException, CommandInvalidVariableException,
        CommandActionNotAllowedException
    {
        DoorBase door = plugin.getDatabaseManager().getDoor(args[1], sender instanceof Player ? (Player) sender : null);
        if (door == null)
            throw new CommandInvalidVariableException(args[1], "door");

        if (sender instanceof Player && !plugin.getDatabaseManager()
            .hasPermissionForAction(((Player) sender), door.getDoorUID(),
                                    DoorAttribute.DIRECTION_STRAIGHT_VERTICAL))
            throw new CommandActionNotAllowedException();

        RotateDirection openDir = RotateDirection.valueOf(args[2].toUpperCase());
        if (openDir != RotateDirection.NONE &&
            openDir != RotateDirection.CLOCKWISE &&
            openDir != RotateDirection.COUNTERCLOCKWISE)
            return false;

        return true;
    }
}
