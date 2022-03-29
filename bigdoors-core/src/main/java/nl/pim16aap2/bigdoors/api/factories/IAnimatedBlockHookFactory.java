package nl.pim16aap2.bigdoors.api.factories;

import nl.pim16aap2.bigdoors.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.bigdoors.api.animatedblockhook.IAnimatedBlockHook;

/**
 * Represents a factory for {@link IAnimatedBlockHook}s.
 *
 * @author Pim
 */
public interface IAnimatedBlockHookFactory<T extends IAnimatedBlock>
{
    /**
     * Creates a new {@link IAnimatedBlockHook}.
     *
     * @param animatedBlock
     *     The {@link IAnimatedBlock} that is being hooked into.
     * @return The new hook.
     */
    IAnimatedBlockHook<T> newInstance(T animatedBlock);
}
