package nl.pim16aap2.bigdoors.doors;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.moveblocks.BridgeMover;
import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Vector2D;
import nl.pim16aap2.bigdoors.util.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a DrawBrige doorType.
 *
 * @author Pim
 * @see HorizontalAxisAlignedBase
 */
public class Drawbridge extends HorizontalAxisAlignedBase
{
    private RotateDirection currentToggleDir = null;

    Drawbridge(final @NotNull PLogger pLogger, final long doorUID, final @NotNull DoorType type)
    {
        super(pLogger, doorUID, type);
    }

    Drawbridge(final @NotNull PLogger pLogger, final long doorUID)
    {
        this(pLogger, doorUID, DoorType.DRAWBRIDGE);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Vector2D[] calculateChunkRange()
    {
        int xLen = dimensions.getX();
        int yLen = dimensions.getY();
        int zLen = dimensions.getZ();

        int radius = 0;

        if (dimensions.getY() != 1)
            radius = yLen / 16 + 1;
        else
            radius = Math.max(xLen, zLen) / 16 + 1;

        return new Vector2D[]{new Vector2D(getChunk().getX() - radius, getChunk().getZ() - radius),
                              new Vector2D(getChunk().getX() + radius, getChunk().getZ() + radius)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpenable()
    {
        return !isOpen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCloseable()
    {
        return isOpen;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public PBlockFace calculateCurrentDirection()
    {
        if (!isOpen)
            return PBlockFace.UP;
        // TODO: Ewww
        return PBlockFace.valueOf(RotateDirection.getOpposite(getCurrentToggleDir()).name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultOpenDirection()
    {
        if (onNorthSouthAxis())
            openDir = RotateDirection.EAST;
        else
            openDir = RotateDirection.NORTH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getNewLocations(final @Nullable PBlockFace openDirection,
                                final @Nullable RotateDirection rotateDirection, final @NotNull Location newMin,
                                final @NotNull Location newMax, final int blocksMoved)
    {
        throw new IllegalStateException("THIS SHOULD NOT HAVE BEEN REACHED");
    }

    @NotNull
    private RotateDirection calculateCurrentToggleDir()
    {
        return isOpen ? getOpenDir() : RotateDirection.getOpposite(getOpenDir());
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public RotateDirection getCurrentToggleDir()
    {
        if (currentToggleDir == null)
            currentToggleDir = calculateCurrentToggleDir();
        return currentToggleDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean getPotentialNewCoordinates(final @NotNull Location min, final @NotNull Location max)
    {
        Vector3D vec = PBlockFace.getDirection(getCurrentDirection());
        Bukkit.broadcastMessage(
            "DOORBASE: on NS axis: " + onNorthSouthAxis() + ", vector: " + vec.toString() + ", currentDirection: " +
                getCurrentDirection().name());
        RotateDirection currentToggleDir = getCurrentToggleDir();
        if (isOpen)
        {
            if (onNorthSouthAxis())
            {
                max.setY(min.getBlockY() + dimensions.getX());
                int newX = vec.getX() > 0 ? min.getBlockX() : max.getBlockX();
                min.setX(newX);
                max.setX(newX);
            }
            else
            {
                max.setY(min.getBlockY() + dimensions.getZ());
                int newZ = vec.getZ() > 0 ? min.getBlockZ() : max.getBlockZ();
                min.setZ(newZ);
                max.setZ(newZ);
            }
        }
        else
        {
            if (onNorthSouthAxis()) // On Z-axis, i.e. Z doesn't change
            {
                max.setY(min.getBlockY());
                min.add(currentToggleDir.equals(RotateDirection.WEST) ? -dimensions.getY() : 0, 0, 0);
                max.add(currentToggleDir.equals(RotateDirection.EAST) ? dimensions.getY() : 0, 0, 0);
            }
            else
            {
                max.setY(min.getBlockY());
                min.add(0, 0, currentToggleDir.equals(RotateDirection.NORTH) ? -dimensions.getY() : 0);
                max.add(0, 0, currentToggleDir.equals(RotateDirection.SOUTH) ? dimensions.getY() : 0);
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerBlockMover(final @NotNull DoorActionCause cause, final double time,
                                      final boolean instantOpen, final @NotNull Location newMin,
                                      final @NotNull Location newMax, final @NotNull BigDoors plugin)
    {
        PBlockFace upDown =
            Math.abs(getMinimum().getBlockY() - getMaximum().getBlockY()) > 0 ? PBlockFace.DOWN : PBlockFace.UP;

        Bukkit.broadcastMessage(
            "DRAWBRIDGE: IsOpen: " + isOpen + ", upDown: " + upDown.name() + ", currentToggleDir: " +
                getCurrentToggleDir().name() +
                ", currentDirection = " + getCurrentDirection());

        doorOpener.registerBlockMover(
            new BridgeMover(plugin, getWorld(), time, this, upDown, getCurrentToggleDir(), instantOpen,
                            plugin.getConfigLoader().getMultiplier(DoorType.DRAWBRIDGE),
                            cause == DoorActionCause.PLAYER ? getPlayerUUID() : null, newMin, newMax));
    }
}
