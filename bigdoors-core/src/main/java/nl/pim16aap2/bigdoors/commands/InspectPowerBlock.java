package nl.pim16aap2.bigdoors.commands;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import lombok.ToString;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.factories.ITextFactory;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.managers.ToolUserManager;
import nl.pim16aap2.bigdoors.movable.MovableBase;
import nl.pim16aap2.bigdoors.tooluser.PowerBlockInspector;
import nl.pim16aap2.bigdoors.util.Constants;

import java.util.concurrent.CompletableFuture;

/**
 * Represents the command to inspect a location to check if there are any powerblocks registered there.
 *
 * @author Pim
 */
@ToString
public class InspectPowerBlock extends BaseCommand
{
    private final ToolUserManager toolUserManager;
    private final PowerBlockInspector.IFactory inspectPowerBlockFactory;

    @AssistedInject //
    InspectPowerBlock(
        @Assisted ICommandSender commandSender, ILocalizer localizer, ITextFactory textFactory,
        ToolUserManager toolUserManager, PowerBlockInspector.IFactory inspectPowerBlockFactory)
    {
        super(commandSender, localizer, textFactory);
        this.toolUserManager = toolUserManager;
        this.inspectPowerBlockFactory = inspectPowerBlockFactory;
    }

    @Override
    public CommandDefinition getCommand()
    {
        return CommandDefinition.INSPECT_POWER_BLOCK;
    }

    @Override
    protected boolean availableForNonPlayers()
    {
        return false;
    }

    @Override
    protected CompletableFuture<?> executeCommand(PermissionsStatus permissions)
    {
        toolUserManager.startToolUser(inspectPowerBlockFactory.create((IPPlayer) getCommandSender(),
                                                                      permissions.hasAdminPermission()),
                                      Constants.MOVABLE_CREATOR_TIME_LIMIT);
        return CompletableFuture.completedFuture(null);
    }

    @AssistedFactory
    interface IFactory
    {
        /**
         * Creates (but does not execute!) a new {@link InspectPowerBlock} command.
         *
         * @param commandSender
         *     The {@link ICommandSender} responsible for inspecting the powerblocks.
         *     <p>
         *     They can only discover {@link MovableBase}s attached to specific locations if they both have access to
         *     the specific location and access to the specific movable(s).
         * @return See {@link BaseCommand#run()}.
         */
        InspectPowerBlock newInspectPowerBlock(ICommandSender commandSender);
    }
}
