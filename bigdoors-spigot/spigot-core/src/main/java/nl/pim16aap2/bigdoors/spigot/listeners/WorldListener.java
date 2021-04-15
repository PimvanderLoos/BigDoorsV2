package nl.pim16aap2.bigdoors.spigot.listeners;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.managers.PowerBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Process world (un)load events.
 *
 * @author Pim
 */
public class WorldListener implements Listener
{
    private static @Nullable WorldListener INSTANCE;

    private final @NonNull PowerBlockManager powerBlockManager;

    private WorldListener(final @NonNull PowerBlockManager powerBlockManager)
    {
        this.powerBlockManager = powerBlockManager;
        for (final @NonNull World world : Bukkit.getWorlds())
            powerBlockManager.loadWorld(world.getName());
    }

    /**
     * Initializes the {@link WorldListener}. If it has already been initialized, it'll return that instance instead.
     *
     * @param powerBlockManager The {@link PowerBlockManager}.
     * @return The instance of this {@link WorldListener}.
     */
    public static WorldListener init(final @NonNull PowerBlockManager powerBlockManager)
    {
        return INSTANCE == null ? INSTANCE = new WorldListener(powerBlockManager) : INSTANCE;
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(final WorldLoadEvent event)
    {
        powerBlockManager.loadWorld(event.getWorld().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldUnload(final WorldUnloadEvent event)
    {
        powerBlockManager.unloadWorld(event.getWorld().getName());
    }

}
