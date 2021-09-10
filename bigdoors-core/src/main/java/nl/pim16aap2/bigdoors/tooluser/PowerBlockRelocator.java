package nl.pim16aap2.bigdoors.tooluser;

import lombok.ToString;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.managers.ToolUserManager;
import nl.pim16aap2.bigdoors.tooluser.step.IStep;
import nl.pim16aap2.bigdoors.tooluser.step.Step;
import nl.pim16aap2.bigdoors.tooluser.stepexecutor.StepExecutorPLocation;
import nl.pim16aap2.bigdoors.tooluser.stepexecutor.StepExecutorVoid;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a tool user that relocates a powerblock to a new position.
 *
 * @author Pim
 */
@ToString
public class PowerBlockRelocator extends ToolUser
{
    private final AbstractDoor door;
    private @Nullable IPLocation newLoc;

    public PowerBlockRelocator(IPPlayer player, AbstractDoor door, IPLogger logger, ILocalizer localizer,
                               ToolUserManager toolUserManager)
    {
        super(player, logger, localizer, toolUserManager);
        this.door = door;
    }

    @Override
    protected void init()
    {
        giveTool("tool_user.base.stick_name", "tool_user.powerblock_relocator.stick_lore",
                 "tool_user.powerblock_relocator.init");
    }

    protected boolean moveToLoc(IPLocation loc)
    {
        if (!loc.getWorld().equals(door.getWorld()))
        {
            getPlayer().sendMessage(localizer.getMessage("tool_user.powerblock_relocator.error.world_mismatch"));
            return false;
        }

        if (loc.getPosition().equals(door.getPowerBlock()))
        {
            newLoc = loc;
            return true;
        }

        if (!playerHasAccessToLocation(loc))
            return false;

        newLoc = loc;
        return true;
    }

    private boolean completeProcess()
    {
        if (newLoc == null)
        {
            logger.logThrowable(
                new NullPointerException("newLoc is null, which should not be possible at this point!"));
            getPlayer().sendMessage(localizer.getMessage("constants.error.generic"));
        }
        else if (door.getPowerBlock().equals(newLoc.getPosition()))
            getPlayer().sendMessage(localizer.getMessage("tool_user.powerblock_relocator.error.location_unchanged"));
        else
        {
            door.setPowerBlockPosition(newLoc.getPosition());
            door.syncData();
            getPlayer().sendMessage(localizer.getMessage("tool_user.powerblock_relocator.success"));
        }
        return true;
    }

    @Override
    protected List<IStep> generateSteps()
        throws InstantiationException
    {
        final Step stepPowerblockRelocatorInit = new Step.Factory(localizer, "RELOCATE_POWER_BLOCK_INIT")
            .messageKey("tool_user.powerblock_relocator.init")
            .stepExecutor(new StepExecutorPLocation(logger, this::moveToLoc))
            .waitForUserInput(true).construct();

        final Step stepPowerblockRelocatorCompleted = new Step.Factory(localizer, "RELOCATE_POWER_BLOCK_COMPLETED")
            .messageKey("tool_user.powerblock_relocator.success")
            .stepExecutor(new StepExecutorVoid(logger, this::completeProcess))
            .waitForUserInput(false).construct();

        return Arrays.asList(stepPowerblockRelocatorInit, stepPowerblockRelocatorCompleted);
    }
}
