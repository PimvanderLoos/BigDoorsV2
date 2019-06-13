package nl.pim16aap2.bigdoors.doors;

import org.bukkit.Chunk;
import org.bukkit.Location;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.util.Mutable;
import nl.pim16aap2.bigdoors.util.MyBlockFace;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Vector2D;

/**
 * Represents a Portcullis doorType.
 *
 * @author pim
 * @see DoorBase
 */
public class Portcullis extends DoorBase
{
    Portcullis(BigDoors plugin, long doorUID, DoorType type)
    {
        super(plugin, doorUID, type);
    }

    Portcullis(BigDoors plugin, long doorUID)
    {
        this(plugin, doorUID, DoorType.PORTCULLIS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MyBlockFace calculateCurrentDirection()
    {
        return isOpen ? MyBlockFace.DOWN : MyBlockFace.UP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2D[] calculateChunkRange()
    {
        Chunk minChunk = min.getChunk();
        Chunk maxChunk = max.getChunk();

        return new Vector2D[] { new Vector2D(minChunk.getX(), minChunk.getZ()),
                                new Vector2D(maxChunk.getX(), maxChunk.getZ()) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultOpenDirection()
    {
        openDir = RotateDirection.UP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getNewLocations(MyBlockFace openDirection, RotateDirection rotateDirection, Location newMin,
                                Location newMax, int blocksMoved, Mutable<MyBlockFace> newEngineSide)
    {
        newMin.setX(min.getBlockX());
        newMin.setY(min.getBlockY() + blocksMoved);
        newMin.setZ(min.getBlockZ());

        newMax.setX(max.getBlockX());
        newMax.setY(max.getBlockY() + blocksMoved);
        newMax.setZ(max.getBlockZ());
    }
}
