package nl.pim16aap2.bigdoors.movable.movablearchetypes;

import nl.pim16aap2.bigdoors.movable.IMovable;

/**
 * Represents movables that can move perpetually. For example, windmills and flags.
 *
 * @author Pim
 */
public interface IPerpetualMover extends IMovable
{
    /**
     * Checks if this specific perpetual mover should move perpetually.
     * <p>
     * Not all perpetual movers make use of this ability.
     *
     * @return True if this perpetual mover should move perpetually.
     */
    default boolean isPerpetual()
    {
        return true;
    }
}
