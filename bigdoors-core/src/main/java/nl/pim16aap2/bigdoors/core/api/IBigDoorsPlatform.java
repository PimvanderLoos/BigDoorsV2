package nl.pim16aap2.bigdoors.core.api;

import nl.pim16aap2.bigdoors.core.api.factories.IAnimatedBlockFactory;
import nl.pim16aap2.bigdoors.core.api.factories.ILocationFactory;
import nl.pim16aap2.bigdoors.core.api.factories.IPlayerFactory;
import nl.pim16aap2.bigdoors.core.api.factories.IWorldFactory;
import nl.pim16aap2.bigdoors.core.audio.IAudioPlayer;
import nl.pim16aap2.bigdoors.core.commands.DelayedCommandInputRequest;
import nl.pim16aap2.bigdoors.core.commands.IServer;
import nl.pim16aap2.bigdoors.core.localization.ILocalizer;
import nl.pim16aap2.bigdoors.core.managers.DatabaseManager;
import nl.pim16aap2.bigdoors.core.managers.DelayedCommandInputManager;
import nl.pim16aap2.bigdoors.core.managers.LimitsManager;
import nl.pim16aap2.bigdoors.core.managers.PowerBlockManager;
import nl.pim16aap2.bigdoors.core.managers.StructureSpecificationManager;
import nl.pim16aap2.bigdoors.core.managers.StructureTypeManager;
import nl.pim16aap2.bigdoors.core.managers.ToolUserManager;
import nl.pim16aap2.bigdoors.core.moveblocks.StructureActivityManager;
import nl.pim16aap2.bigdoors.core.structures.StructureRegistry;

/**
 * Represents a set of getter methods to get access to the internals of BigDoors.
 *
 * @author Pim
 */
@SuppressWarnings("unused")
public interface IBigDoorsPlatform
{
    /**
     * Restarts the plugin.
     */
    void restartPlugin();

    /**
     * Shuts the plugin down.
     */
    void shutDownPlugin();

    /**
     * Gets the build id of BigDoors that is currently running.
     *
     * @return The build id of BigDoors that is currently running.
     */
    String getVersion();

    /**
     * Gets the instance of the {@link IBigDoorsToolUtil} for this platform.
     *
     * @return The instance of the {@link IBigDoorsToolUtil} for this platform.
     */
    IBigDoorsToolUtil getBigDoorsToolUtil();

    /**
     * Gets the instance of the {@link IWorldFactory} for this platform.
     *
     * @return The instance of the {@link IWorldFactory} for this platform.
     */
    IWorldFactory getWorldFactory();

    /**
     * Gets the instance of the {@link ILocationFactory} for this platform.
     *
     * @return The instance of the {@link ILocationFactory} for this platform.
     */
    ILocationFactory getLocationFactory();

    /**
     * Gets the instance of the {@link IAnimatedBlockFactory} for this platform.
     *
     * @return The instance of the {@link IAnimatedBlockFactory} for this platform.
     */
    IAnimatedBlockFactory getAnimatedBlockFactory();

    /**
     * Gets the instance of the {@link IPlayerFactory} for this platform.
     *
     * @return The instance of the {@link IPlayerFactory} for this platform.
     */
    IPlayerFactory getPlayerFactory();

    /**
     * Gets the instance of the {@link IConfig} for this platform.
     *
     * @return The instance of the {@link IConfig} for this platform.
     */
    IConfig getBigDoorsConfig();

    /**
     * Gets the instance of the {@link IAudioPlayer} for this platform.
     *
     * @return The instance of the {@link IAudioPlayer} for this platform.
     */
    IAudioPlayer getAudioPlayer();

    /**
     * Gets the instance of the {@link IBlockAnalyzer} for this platform.
     *
     * @return The instance of the {@link IBlockAnalyzer} for this platform.
     */
    IBlockAnalyzer getBlockAnalyzer();

    /**
     * Constructs a new {@link IExecutor}.
     *
     * @return A new {@link IExecutor}.
     */
    IExecutor getExecutor();

    /**
     * Gets the {@link GlowingBlockSpawner} for the current platform.
     *
     * @return The {@link GlowingBlockSpawner} for the current platform.
     */
    GlowingBlockSpawner getGlowingBlockSpawner();

    /**
     * Gets the {@link ILocalizer} used to localize strings.
     *
     * @return The {@link ILocalizer} registered for this platform.
     */
    ILocalizer getLocalizer();

    /**
     * Gets the instance of the {@link IMessagingInterface} for this platform.
     *
     * @return The instance of the {@link IMessagingInterface} for this platform.
     */
    IMessagingInterface getMessagingInterface();

    /**
     * Gets the implementation of a {@link IMessageable} for the server.
     *
     * @return The implementation of a {@link IMessageable} for the server.
     */
    IMessageable getMessageableServer();

    /**
     * Gets the {@link IServer} instance.
     *
     * @return The {@link IServer} instance.
     */
    IServer getServer();

    /**
     * Gets the {@link StructureRegistry}.
     *
     * @return The {@link StructureRegistry}.
     */
    StructureRegistry getDoorRegistry();

    /**
     * @return The instance of the {@link IConfig} for this platform.
     */
    IChunkLoader getChunkLoader();

    /**
     * Gets the {@link DatabaseManager}.
     *
     * @return The {@link DatabaseManager}.
     */
    DatabaseManager getDatabaseManager();

    /**
     * Gets the {@link StructureActivityManager} instance.
     *
     * @return The {@link StructureActivityManager} instance.
     */
    StructureActivityManager getDoorActivityManager();

    /**
     * Gets the {@link StructureSpecificationManager} instance.
     *
     * @return The {@link StructureSpecificationManager} instance.
     */
    StructureSpecificationManager getDoorSpecificationManager();

    /**
     * Gets the {@link StructureTypeManager} instance.
     *
     * @return The {@link StructureTypeManager} instance.
     */
    StructureTypeManager getDoorTypeManager();

    /**
     * Gets the {@link ToolUserManager} instance.
     *
     * @return The {@link ToolUserManager} instance.
     */
    ToolUserManager getToolUserManager();

    /**
     * Gets the {@link DelayedCommandInputManager} to manage {@link DelayedCommandInputRequest}s.
     *
     * @return The {@link DelayedCommandInputManager} registered by the platform.
     */
    DelayedCommandInputManager getDelayedCommandInputManager();

    /**
     * Gets the {@link PowerBlockManager} instance.
     *
     * @return The {@link PowerBlockManager} instance.
     */
    PowerBlockManager getPowerBlockManager();

    /**
     * Gets the instance of the {@link IEconomyManager} for this platform.
     *
     * @return The instance of the {@link IEconomyManager} for this platform.
     */
    IEconomyManager getEconomyManager();

    /**
     * Gets the instance of the {@link IPermissionsManager} for this platform.
     *
     * @return The instance of the {@link IPermissionsManager} for this platform.
     */
    IPermissionsManager getPermissionsManager();

    /**
     * Gets the instance of the {@link IProtectionCompatManager} for this platform.
     *
     * @return The instance of the {@link IProtectionCompatManager} for this platform.
     */
    IProtectionCompatManager getProtectionCompatManager();

    /**
     * Gets the {@link LimitsManager}.
     *
     * @return The {@link LimitsManager}.
     */
    LimitsManager getLimitsManager();
}
