package nl.pim16aap2.animatedarchitecture.spigot.core.listeners;

import org.bukkit.event.Listener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Represents a class that ensures listeners are instantiated.
 * <p>
 * This class has no other purpose than to have listeners injected into it to make sure they are instantiated.
 */
@Singleton
public final class ListenerHolder
{
    /**
     * A list of all listeners that are injected into this class.
     * <p>
     * Kept to prevent garbage collection of the listeners.
     */
    @SuppressWarnings({"FieldCanBeLocal", "unused", "PMD.SingularField"})
    private final List<Listener> listeners;

    @Inject ListenerHolder(
        ChunkListener chunkListener,
        PlayerLoginListener playerLoginListener,
        PlayerLoginListenerLoginMessage playerLoginListenerLoginMessage,
        PlayerLoginListenerResourcePack playerLoginListenerResourcePack,
        PlayerLogoutListener playerLogoutListener,
        RedstoneListener redstoneListener,
        ToolUserListener toolUserListener,
        WorldListener worldListener
    )
    {
        listeners = List.of(
            chunkListener,
            playerLoginListener,
            playerLoginListenerLoginMessage,
            playerLoginListenerResourcePack,
            playerLogoutListener,
            redstoneListener,
            toolUserListener,
            worldListener
        );
    }
}
