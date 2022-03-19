package nl.pim16aap2.bigdoors.api;

import nl.pim16aap2.bigdoors.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;

/**
 * Represents a hook for {@link IAnimatedBlock}s.
 *
 * @author Pim
 */
public interface IAnimatedBlockHook
{
    /**
     * Fires at the end of the animated block's constructor.
     *
     * @param animatedBlock
     *     The animated block that was constructed.
     */
    void onCreate(IAnimatedBlock animatedBlock);

    /**
     * Fires after an animated block has been spawned.
     *
     * @param animatedBlock
     *     The animated block that was spawned into a world.
     */
    void onSpawn(IAnimatedBlock animatedBlock);

    /**
     * Fires after an animated block has died.
     *
     * @param animatedBlock
     *     The animated block that died.
     */
    void onDie(IAnimatedBlock animatedBlock);

    /**
     * Fires after an animated block has been teleported.
     *
     * @param animatedBlock
     *     The animated block that was teleported.
     * @param newPosition
     *     The position the animated block was teleported to.
     */
    void onTeleport(IAnimatedBlock animatedBlock, Vector3Dd newPosition);

    /**
     * Fires after this animated block has been moved.
     * <p>
     * This may happen either because of a teleport or because of tick-based movement.
     *
     * @param animatedBlock
     *     The animated block that was moved.
     * @param newPosition
     *     The position the animated block was moved to.
     */
    void onMoved(IAnimatedBlock animatedBlock, Vector3Dd newPosition);

    /**
     * Fires right before a tick is processed.
     *
     * @param animatedBlock
     *     The animated block that is processing a tick.
     */
    void preTick(IAnimatedBlock animatedBlock);

    /**
     * Fires right after a tick is processed.
     *
     * @param animatedBlock
     *     The animated block that is processing a tick.
     */
    void postTick(IAnimatedBlock animatedBlock);

    /**
     * Represents an instantiation function for hooks.
     */
    @FunctionalInterface
    interface Factory
    {
        IAnimatedBlockHook newInstance();
    }
}
