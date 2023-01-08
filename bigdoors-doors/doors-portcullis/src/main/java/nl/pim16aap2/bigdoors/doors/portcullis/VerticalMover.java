package nl.pim16aap2.bigdoors.doors.portcullis;

import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.vector.IVector3D;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;

/**
 * Represents a {@link BlockMover} for {@link Portcullis}'s.
 *
 * @author Pim
 */
public class VerticalMover extends BlockMover
{
    protected final int blocksToMove;
    private final double step;

    public VerticalMover(
        Context context, AbstractDoor door, double time, boolean skipAnimation, int blocksToMove, double multiplier,
        IPPlayer player, Cuboid newCuboid, DoorActionCause cause, DoorActionType actionType)
        throws Exception
    {
        super(context, door, time, skipAnimation, RotateDirection.NONE, player, newCuboid, cause, actionType);
        this.blocksToMove = blocksToMove;

        double speed = 1;
        double pcMultiplier = multiplier;
        pcMultiplier = pcMultiplier == 0.0 ? 1.0 : pcMultiplier;
        final int maxSpeed = 6;

        // If the time isn't default, calculate speed.
        if (time != 0.0)
        {
            speed = Math.abs(blocksToMove) / time;
            super.time = time;
        }

        // If the non-default exceeds the max-speed or isn't set, calculate default speed.
        if (time == 0.0 || speed > maxSpeed)
        {
            speed = (blocksToMove < 0 ? 1.7 : 0.8) * pcMultiplier;
            speed = speed > maxSpeed ? maxSpeed : speed;
            super.time = Math.abs(blocksToMove) / speed;
        }

        super.animationDuration = (int) (20 * super.time);
        step = ((double) blocksToMove) / ((double) super.animationDuration);
    }

    @Override
    protected Vector3Dd getFinalPosition(IVector3D startLocation, float radius)
    {
        return Vector3Dd.of(startLocation).add(0, blocksToMove, 0);
    }

    protected Vector3Dd getGoalPos(IAnimatedBlock animatedBlock, double stepSum)
    {
        return animatedBlock.getStartPosition().add(0, stepSum, 0);
    }

    @Override
    protected void executeAnimationStep(int ticks)
    {
        final double stepSum = step * ticks;
        for (final IAnimatedBlock animatedBlock : getAnimatedBlocks())
            applyMovement(animatedBlock, getGoalPos(animatedBlock, stepSum));
    }
}
