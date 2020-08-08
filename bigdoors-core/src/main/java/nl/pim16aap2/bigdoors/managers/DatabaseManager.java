package nl.pim16aap2.bigdoors.managers;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IConfigLoader;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.IRestartableHolder;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.storage.IStorage;
import nl.pim16aap2.bigdoors.storage.sqlite.SQLiteJDBCDriverConnection;
import nl.pim16aap2.bigdoors.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.DoorOwner;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.Pair;
import nl.pim16aap2.bigdoors.util.Restartable;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.vector.IVector3DiConst;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages all database interactions.
 *
 * @author Pim
 */
public final class DatabaseManager extends Restartable
{
    @Nullable
    private static DatabaseManager instance;

    /**
     * The thread pool to use for storage access.
     */
    private final ExecutorService threadPool;

    /**
     * The number of threads to use for storage access if the storage allows multithreaded access as determined by
     * {@link IStorage#isSingleThreaded()}.
     */
    private static final int THREADCOUNT = 10;

    private final IStorage db;

    /**
     * Constructs a new {@link DatabaseManager}.
     *
     * @param restartableHolder The object managing restarts for this object.
     * @param config            The configuration for the plugin.
     * @param dbFile            The name of the database file.
     */
    private DatabaseManager(final @NotNull IRestartableHolder restartableHolder, final @NotNull IConfigLoader config,
                            final @NotNull File dbFile)
    {
        super(restartableHolder);
        db = new SQLiteJDBCDriverConnection(dbFile, config);
        if (db.isSingleThreaded())
            threadPool = Executors.newSingleThreadExecutor();
        else
            threadPool = Executors.newFixedThreadPool(THREADCOUNT);
    }

    /**
     * Initializes the {@link DatabaseManager}. If it has already been initialized, it'll return that instance instead.
     *
     * @param restartableHolder The object managing restarts for this object.
     * @param config            The configuration for the plugin.
     * @param dbFile            The name of the database file.
     * @return The instance of this {@link DatabaseManager}.
     */
    @NotNull
    public static DatabaseManager init(final @NotNull IRestartableHolder restartableHolder,
                                       final @NotNull IConfigLoader config, final @NotNull File dbFile)
    {
        return (instance == null) ? instance = new DatabaseManager(restartableHolder, config, dbFile) : instance;
    }

    /**
     * Registeres an {@link DoorType} in the database.
     *
     * @param doorType The {@link DoorType}.
     * @return The identifier value assigned to the {@link DoorType} during registration. A value less than 1 means that
     * registration was not successful. If the {@link DoorType} already exists in the database, it will return the
     * existing identifier value. As long as the type does not change,
     */
    public CompletableFuture<Long> registerDoorType(final @NotNull DoorType doorType)
    {
        return CompletableFuture.supplyAsync(() -> db.registerDoorType(doorType));
    }

    /**
     * Gets the instance of the {@link DatabaseManager} if it exists.
     *
     * @return The instance of the {@link DatabaseManager}.
     */
    @NotNull
    public static DatabaseManager get()
    {
//        Preconditions.checkState(instance != null,
//                                 "Instance has not yet been initialized. Be sure #init() has been invoked");
        return instance;
    }

    /**
     * Obtains {@link IStorage.DatabaseState} the database is in.
     *
     * @return The {@link IStorage.DatabaseState} the database is in.
     */
    @NotNull
    public IStorage.DatabaseState getDatabaseState()
    {
        return db.getDatabaseState();
    }

    @Override
    public void restart()
    {
    }

    @Override
    public void shutdown()
    {
    }

    /**
     * Enables or disables logging of statements sent to the database.
     *
     * @param enabled True to enable statement logging, false to disable.
     */
    public void setStatementLogging(final boolean enabled)
    {
        db.setStatementLogging(enabled);
    }

    /**
     * Inserts a {@link AbstractDoorBase} into the database.
     *
     * @param newDoor The new {@link AbstractDoorBase}.
     * @return The future result of the operation. If the operation was successful this will be true.
     */
    public CompletableFuture<Boolean> addDoorBase(final @NotNull AbstractDoorBase newDoor)
    {
        return CompletableFuture.supplyAsync(
            () ->
            {
                boolean result = db.insert(newDoor);
                if (result)
                    BigDoors.get().getPowerBlockManager().onDoorAddOrRemove(newDoor.getWorld().getUID(), new Vector3Di(
                        newDoor.getPowerBlock().getX(),
                        newDoor.getPowerBlock().getY(),
                        newDoor.getPowerBlock().getZ()));
                return result;
            }, threadPool);
    }

    /**
     * Removes a {@link AbstractDoorBase} from the database.
     *
     * @param door The door.
     * @return The future result of the operation. If the operation was successful this will be true.
     */
    public CompletableFuture<Boolean> removeDoor(final @NotNull AbstractDoorBase door)
    {
        return CompletableFuture.supplyAsync(
            () ->
            {
                boolean result = db.removeDoor(door.getDoorUID());
                if (result)
                    BigDoors.get().getPowerBlockManager().onDoorAddOrRemove(door.getWorld().getUID(), new Vector3Di(
                        door.getPowerBlock().getX(),
                        door.getPowerBlock().getY(),
                        door.getPowerBlock().getZ()));
                return result;
            }, threadPool);
    }

    /**
     * Gets a list of door UIDs that have their engine in a given chunk.
     *
     * @param chunkHash The hash of the chunk the doors are in.
     * @return A list of door UIDs that have their engine in a given chunk.
     */
    @NotNull
    public CompletableFuture<List<Long>> getDoorsInChunk(final long chunkHash)
    {
        return CompletableFuture.supplyAsync(() -> db.getDoorsInChunk(chunkHash), threadPool);
    }

    /**
     * Gets all {@link AbstractDoorBase} owned by a player. Only searches for {@link AbstractDoorBase} with a given name
     * if one was provided.
     *
     * @param playerUUID The {@link UUID} of the payer.
     * @param name       The name or the UID of the {@link AbstractDoorBase} to search for. Can be null.
     * @return All {@link AbstractDoorBase} owned by a player with a specific name.
     */
    @NotNull
    public CompletableFuture<Optional<List<AbstractDoorBase>>> getDoors(final @NotNull UUID playerUUID,
                                                                        final @Nullable String name)
    {
        // Check if the name is actually the UID of the door.
        final @NotNull Pair<Boolean, Long> doorID = Util.longFromString(name);
        if (doorID.key())
            return CompletableFuture
                .supplyAsync(() -> db.getDoor(playerUUID, doorID.value()).map(Collections::singletonList),
                             threadPool);

        return name == null ? getDoors(playerUUID) :
               CompletableFuture.supplyAsync(() -> db.getDoors(playerUUID, name), threadPool);

    }

    /**
     * Gets the prime {@link DoorOwner}. I.e. the owner with permission level 0. In most cases, this will just be the
     * original creator of the door. Every valid door has a prime owner.
     *
     * @param doorUID The UID of the door.
     * @return The Owner of the door, is possible.
     */
    @NotNull
    public CompletableFuture<Optional<DoorOwner>> getPrimeOwner(final long doorUID)
    {
        return CompletableFuture.supplyAsync(() -> db.getPrimeOwner(doorUID), threadPool);
    }

    /**
     * Gets all {@link AbstractDoorBase} owned by a player.
     *
     * @param playerUUID The {@link UUID} of the payer.
     * @return All {@link AbstractDoorBase} owned by a player.
     */
    @NotNull
    public CompletableFuture<Optional<List<AbstractDoorBase>>> getDoors(final @NotNull UUID playerUUID)
    {
        return CompletableFuture.supplyAsync(() -> db.getDoors(playerUUID), threadPool);
    }

    /**
     * Gets all {@link AbstractDoorBase} owned by a player with a specific name.
     *
     * @param playerUUID    The {@link UUID} of the payer.
     * @param name          The name of the {@link AbstractDoorBase} to search for.
     * @param maxPermission The maximum level of ownership (inclusive) this player has over the {@link
     *                      AbstractDoorBase}s.
     * @return All {@link AbstractDoorBase} owned by a player with a specific name.
     */
    @NotNull
    public CompletableFuture<Optional<List<AbstractDoorBase>>> getDoors(final @NotNull String playerUUID,
                                                                        final @NotNull String name,
                                                                        final int maxPermission)
    {
        return CompletableFuture.supplyAsync(() -> db.getDoors(playerUUID, name, maxPermission), threadPool);
    }

    /**
     * Gets all {@link AbstractDoorBase}s with a specific name, regardless over ownership.
     *
     * @param name The name of the {@link AbstractDoorBase}s.
     * @return All {@link AbstractDoorBase}s with a specific name.
     */
    @NotNull
    public CompletableFuture<Optional<List<AbstractDoorBase>>> getDoors(final @NotNull String name)
    {
        return CompletableFuture.supplyAsync(() -> db.getDoors(name), threadPool);
    }

    /**
     * Updates the name of a player in the database, to make sure the player's name and UUID don't go out of sync.
     *
     * @param player The Player.
     * @return The future result of the operation. If the operation was successful this will be true.
     */
    public CompletableFuture<Boolean> updatePlayer(final @NotNull IPPlayer player)
    {
        return CompletableFuture
            .supplyAsync(() -> db.updatePlayerName(player.getUUID().toString(), player.getName()), threadPool);
    }

    /**
     * Gets the {@link AbstractDoorBase} with a specific UID.
     *
     * @param doorUID The UID of the {@link AbstractDoorBase}.
     * @return The {@link AbstractDoorBase} if it exists.
     */
    @NotNull
    public CompletableFuture<Optional<AbstractDoorBase>> getDoor(final long doorUID)
    {
        return CompletableFuture.supplyAsync(() -> db.getDoor(doorUID), threadPool);
    }

    /**
     * Gets the {@link AbstractDoorBase} with the given UID owned by the player, if provided. Otherwise, the original
     * creator is used as {@link DoorOwner}.
     *
     * @param player  The player. Null will default to the original creator.
     * @param doorUID The UID of the {@link AbstractDoorBase}.
     * @return The {@link AbstractDoorBase} with the given UID owned by the player, if provided.
     */
    @NotNull
    public CompletableFuture<Optional<AbstractDoorBase>> getDoor(final @Nullable IPPlayer player,
                                                                 final long doorUID)
    {
        return player == null ?
               CompletableFuture.supplyAsync(() -> db.getDoor(doorUID), threadPool) :
               CompletableFuture.supplyAsync(() -> db.getDoor(player.getUUID(), doorUID),
                                             threadPool);
    }

    /**
     * Gets the number of {@link AbstractDoorBase}s owned by a player.
     *
     * @param playerUUID The {@link UUID} of the player.
     * @return The number of {@link AbstractDoorBase}s this player owns.
     */
    public CompletableFuture<Integer> countDoorsOwnedByPlayer(final @NotNull UUID playerUUID)
    {
        return CompletableFuture.supplyAsync(() -> db.getDoorCountForPlayer(playerUUID), threadPool);
    }

    /**
     * Counts the number of {@link AbstractDoorBase}s with a specific name owned by a player.
     *
     * @param playerUUID The {@link UUID} of the player.
     * @param doorName   The name of the door.
     * @return The number of {@link AbstractDoorBase}s with a specific name owned by a player.
     */
    public CompletableFuture<Integer> countDoorsOwnedByPlayer(final @NotNull UUID playerUUID,
                                                              final @NotNull String doorName)
    {
        return CompletableFuture.supplyAsync(() -> db.getDoorCountForPlayer(playerUUID, doorName), threadPool);
    }

    public CompletableFuture<Integer> countOwnersOfDoor(final long doorUID)
    {
        return CompletableFuture.supplyAsync(() -> db.getOwnerCountOfDoor(doorUID), threadPool);
    }

    /**
     * The number of {@link AbstractDoorBase}s in the database with a specific name.
     *
     * @param doorName The name of the {@link AbstractDoorBase}.
     * @return The number of {@link AbstractDoorBase}s with a specific name.
     */
    public CompletableFuture<Integer> countDoorsByName(final @NotNull String doorName)
    {
        return CompletableFuture.supplyAsync(() -> db.getDoorCountByName(doorName), threadPool);
    }

    /**
     * Checks if a player has a high enough lever of ownership over a {@link AbstractDoorBase} to interact with a
     * specific {@link DoorAttribute}.
     *
     * @param player  The {@link IPPlayer}.
     * @param doorUID The UID of the {@link AbstractDoorBase}.
     * @param atr     The {@link DoorAttribute}.
     * @return True if the player has a high enough lever of ownership over a {@link AbstractDoorBase} to interact with
     * a specific {@link DoorAttribute}.
     */
    public CompletableFuture<Boolean> hasPermissionForAction(final @NotNull IPPlayer player, final long doorUID,
                                                             final @NotNull DoorAttribute atr)
    {
        return hasPermissionForAction(player.getUUID(), doorUID, atr);
    }

    /**
     * Checks if a player has a high enough lever of ownership over a {@link AbstractDoorBase} to interact with a
     * specific {@link DoorAttribute}.
     *
     * @param playerUUID The {@link UUID} of the {@link IPPlayer}.
     * @param doorUID    The UID of the {@link AbstractDoorBase}.
     * @param atr        The {@link DoorAttribute}.
     * @return True if the player has a high enough lever of ownership over a {@link AbstractDoorBase} to interact with
     * a specific {@link DoorAttribute}.
     */
    public CompletableFuture<Boolean> hasPermissionForAction(final @NotNull UUID playerUUID, final long doorUID,
                                                             final @NotNull DoorAttribute atr)
    {
        return CompletableFuture.supplyAsync(
            () ->
            {
                int playerPermission;
                try
                {
                    playerPermission = getPermission(playerUUID, doorUID).get();
                }
                catch (InterruptedException | ExecutionException e)
                {
                    PLogger.get().logException(e);
                    playerPermission = Integer.MAX_VALUE;
                }
                return playerPermission >= 0 && playerPermission <= DoorAttribute.getPermissionLevel(atr);
            });
    }

    /**
     * Gets the level of ownership a player has over a {@link AbstractDoorBase}.
     *
     * @param player  The player.
     * @param doorUID The UID of the {@link AbstractDoorBase}.
     * @return The level of ownership a player has over a {@link AbstractDoorBase}.
     */
    public CompletableFuture<Integer> getPermission(final @NotNull IPPlayer player, final long doorUID)
    {
        return getPermission(player.getUUID(), doorUID);
    }

    /**
     * Gets the level of ownership a player has over a {@link AbstractDoorBase}.
     *
     * @param playerUUID The {@link UUID} of the player.
     * @param doorUID    The UID of the {@link AbstractDoorBase}.
     * @return The level of ownership a player has over a {@link AbstractDoorBase}.
     */
    public CompletableFuture<Integer> getPermission(final @NotNull UUID playerUUID, final long doorUID)
    {
        return CompletableFuture.supplyAsync(() -> db.getPermission(playerUUID.toString(), doorUID), threadPool);
    }

    /**
     * Updates the coordinates of a {@link AbstractDoorBase} in the database.
     *
     * @param doorUID   The UID of the {@link AbstractDoorBase}.
     * @param isOpen    Whether the {@link AbstractDoorBase} is now open or not.
     * @param blockXMin The lower bound x coordinates.
     * @param blockYMin The lower bound y coordinates.
     * @param blockZMin The lower bound z coordinates.
     * @param blockXMax The upper bound x coordinates.
     * @param blockYMax The upper bound y coordinates.
     * @param blockZMax The upper bound z coordinates.
     * @return The future result of the operation. If the operation was successful this will be true.
     */
    public CompletableFuture<Boolean> updateDoorCoords(final long doorUID, final boolean isOpen, final int blockXMin,
                                                       final int blockYMin, final int blockZMin, final int blockXMax,
                                                       final int blockYMax, final int blockZMax)
    {
        return CompletableFuture.supplyAsync(() -> db.updateDoorCoords(doorUID, isOpen,
                                                                       blockXMin, blockYMin, blockZMin,
                                                                       blockXMax, blockYMax, blockZMax), threadPool);
    }

    /**
     * Adds a player as owner to a {@link AbstractDoorBase} at a given level of ownership.
     *
     * @param door       The {@link AbstractDoorBase}.
     * @param player     The {@link IPPlayer}.
     * @param permission The level of ownership.
     * @return True if owner addition was successful.
     */
    public CompletableFuture<Boolean> addOwner(final @NotNull AbstractDoorBase door, final @NotNull IPPlayer player,
                                               final int permission)
    {
        if (permission < 1 || permission > 2 || door.getPermission() != 0 ||
            door.getPlayerUUID().equals(player.getUUID()))
            return CompletableFuture.completedFuture(false);

        return CompletableFuture.supplyAsync(() -> db.addOwner(door.getDoorUID(), player, permission), threadPool);
    }

    /**
     * Remove a {@link IPPlayer} as owner of a {@link AbstractDoorBase}.
     *
     * @param door       The {@link AbstractDoorBase}.
     * @param playerUUID The {@link UUID} of the {@link IPPlayer}.
     * @return True if owner removal was successful.
     */
    public CompletableFuture<Boolean> removeOwner(final @NotNull AbstractDoorBase door, final @NotNull UUID playerUUID)
    {
        return removeOwner(door.getDoorUID(), playerUUID);
    }

    /**
     * Remove a {@link IPPlayer} as owner of a {@link AbstractDoorBase}.
     *
     * @param doorUID    The UID of the {@link AbstractDoorBase}.
     * @param playerUUID The {@link UUID} of the {@link IPPlayer}.
     * @return True if owner removal was successful.
     */
    public CompletableFuture<Boolean> removeOwner(final long doorUID, final @NotNull UUID playerUUID)
    {
        return CompletableFuture.supplyAsync(
            () ->
            {
                if (db.getPermission(playerUUID.toString(), doorUID) == 0)
                    return false;
                return db.removeOwner(doorUID, playerUUID.toString());
            }, threadPool);
    }

    /**
     * Gets all owners of a {@link AbstractDoorBase}.
     *
     * @param doorUID The UID of the {@link AbstractDoorBase}.
     * @return All owners of a {@link AbstractDoorBase}.
     */
    public CompletableFuture<List<DoorOwner>> getDoorOwners(final long doorUID)
    {
        return CompletableFuture.supplyAsync(() -> db.getOwnersOfDoor(doorUID), threadPool);
    }

    /**
     * Updates the opening direction of a {@link AbstractDoorBase}.
     *
     * @param doorUID The UID of the {@link AbstractDoorBase}.
     * @param openDir The new opening direction.
     * @return The future result of the operation. If the operation was successful this will be true.
     */
    public CompletableFuture<Boolean> updateDoorOpenDirection(final long doorUID,
                                                              final @NotNull RotateDirection openDir)
    {
        return CompletableFuture.supplyAsync(() -> db.updateDoorOpenDirection(doorUID, openDir), threadPool);
    }

    /**
     * Updates the type-specific data of an {@link AbstractDoorBase}. The data will be provided by {@link
     * DoorType#getTypeData(AbstractDoorBase)}.
     *
     * @param door The {@link AbstractDoorBase} whose type-specific data will be updated.
     * @return The future result of the operation. If the operation was successful this will be true.
     */
    public CompletableFuture<Boolean> updateDoorTypeData(final @NotNull AbstractDoorBase door)
    {
        return CompletableFuture.supplyAsync(() -> db.updateTypeData(door), threadPool);
    }

    /**
     * Changes the locked status of a {@link AbstractDoorBase}.
     *
     * @param doorUID       The UID of the {@link AbstractDoorBase}.
     * @param newLockStatus The new locked status.
     * @return The future result of the operation. If the operation was successful this will be true.
     */
    public CompletableFuture<Boolean> setLock(final long doorUID, final boolean newLockStatus)
    {
        return CompletableFuture.supplyAsync(() -> db.setLock(doorUID, newLockStatus), threadPool);
    }

    /**
     * Updates the location of a power block of a door. If you want to move the powerblock, it's recommended to use
     * {@link PowerBlockManager#updatePowerBlockLoc} instead, as that will properly invalidate the cache.
     *
     * @param doorUID The UID of the door.
     * @param newLoc  The new location.
     * @return The future result of the operation. If the operation was successful this will be true.
     */
    CompletableFuture<Boolean> updatePowerBlockLoc(final long doorUID, final @NotNull IVector3DiConst newLoc)
    {
        return CompletableFuture.supplyAsync(() -> db.updateDoorPowerBlockLoc(doorUID, newLoc.getX(), newLoc.getY(),
                                                                              newLoc.getZ()), threadPool);
    }

    /**
     * Checks if a world contains any big doors.
     *
     * @param world The world.
     * @return True if at least 1 door exists in the world.
     */
    CompletableFuture<Boolean> isBigDoorsWorld(final @NotNull UUID world)
    {
        return CompletableFuture.supplyAsync(() -> db.isBigDoorsWorld(world), threadPool);
    }

    /**
     * Gets a map of location hashes and their connected powerblocks for all doors in a chunk.
     * <p>
     * The key is the hashed location in chunk space, the value is the list of UIDs of the doors whose powerblocks
     * occupies that location.
     *
     * @param chunkHash The hash of the chunk the doors are in.
     * @return A map of location hashes and their connected powerblocks for all doors in a chunk.
     */
    @NotNull
    CompletableFuture<ConcurrentHashMap<Integer, List<Long>>> getPowerBlockData(final long chunkHash)
    {
        return CompletableFuture.supplyAsync(() -> db.getPowerBlockData(chunkHash), threadPool);
    }
}
