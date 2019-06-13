package nl.pim16aap2.bigdoors.moveblocks;

import org.bukkit.Location;
import org.bukkit.World;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.doors.DoorType;
import nl.pim16aap2.bigdoors.util.DoorOpenResult;
import nl.pim16aap2.bigdoors.util.MyBlockFace;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Util;

public class BridgeOpener extends Opener
{
    public BridgeOpener(BigDoors plugin)
    {
        super(plugin);
    }

    // Check if the new position is free.
    private boolean isNewPosFree(DoorBase door, MyBlockFace upDown, MyBlockFace cardinal, Location newMin, Location newMax)
    {
        int startX = 0, startY = 0, startZ = 0;
        int endX = 0, endY = 0, endZ = 0;
        World world = door.getWorld();

        if (upDown.equals(MyBlockFace.UP))
            switch (cardinal)
            {
            // North West = Min X, Min Z
            // South West = Min X, Max Z
            // North East = Max X, Min Z
            // South East = Max X, Max X
            case NORTH:
                startX = door.getMinimum().getBlockX();
                endX = door.getMaximum().getBlockX();

                startY = door.getMinimum().getBlockY() + 1;
                endY = door.getMinimum().getBlockY() + door.getMaximum().getBlockZ() - door.getMinimum().getBlockZ();

                startZ = door.getMinimum().getBlockZ();
                endZ = door.getMinimum().getBlockZ();
                break;

            case SOUTH:
                startX = door.getMinimum().getBlockX();
                endX = door.getMaximum().getBlockX();

                startY = door.getMinimum().getBlockY() + 1;
                endY = door.getMinimum().getBlockY() + door.getMaximum().getBlockZ() - door.getMinimum().getBlockZ();

                startZ = door.getMaximum().getBlockZ();
                endZ = door.getMaximum().getBlockZ();
                break;

            case EAST:
                startX = door.getMaximum().getBlockX();
                endX = door.getMaximum().getBlockX();

                startY = door.getMinimum().getBlockY() + 1;
                endY = door.getMinimum().getBlockY() + door.getMaximum().getBlockX() - door.getMinimum().getBlockX();

                startZ = door.getMinimum().getBlockZ();
                endZ = door.getMaximum().getBlockZ();
                break;

            case WEST:
                startX = door.getMinimum().getBlockX();
                endX = door.getMinimum().getBlockX();

                startY = door.getMinimum().getBlockY() + 1;
                endY = door.getMinimum().getBlockY() + door.getMaximum().getBlockX() - door.getMinimum().getBlockX();

                startZ = door.getMinimum().getBlockZ();
                endZ = door.getMaximum().getBlockZ();
                break;
            default:
                plugin.getMyLogger().dumpStackTrace("Invalid rotation for bridge opener: " + cardinal.toString());
                break;
            }
        else
            switch (cardinal)
            {
            // North West = Min X, Min Z
            // South West = Min X, Max Z
            // North East = Max X, Min Z
            // South East = Max X, Max X
            case NORTH:
                startX = door.getMinimum().getBlockX();
                endX = door.getMaximum().getBlockX();

                startY = door.getMinimum().getBlockY();
                endY = door.getMinimum().getBlockY();

                startZ = door.getMinimum().getBlockZ() - door.getMaximum().getBlockY() + door.getMinimum().getBlockY();
                endZ = door.getMinimum().getBlockZ() - 1;
                break;

            case SOUTH:
                startX = door.getMinimum().getBlockX();
                endX = door.getMaximum().getBlockX();

                startY = door.getMinimum().getBlockY();
                endY = door.getMinimum().getBlockY();

                startZ = door.getMinimum().getBlockZ() + 1;
                endZ = door.getMinimum().getBlockZ() + door.getMaximum().getBlockY() - door.getMinimum().getBlockY();
                break;

            case EAST:
                startX = door.getMinimum().getBlockX() + 1;
                endX = door.getMaximum().getBlockX() + door.getMaximum().getBlockY() - door.getMinimum().getBlockY();

                startY = door.getMinimum().getBlockY();
                endY = door.getMinimum().getBlockY();

                startZ = door.getMinimum().getBlockZ();
                endZ = door.getMaximum().getBlockZ();
                break;

            case WEST:
                startX = door.getMinimum().getBlockX() - door.getMaximum().getBlockY() + door.getMinimum().getBlockY();
                endX = door.getMinimum().getBlockX() - 1;

                startY = door.getMinimum().getBlockY();
                endY = door.getMinimum().getBlockY();

                startZ = door.getMinimum().getBlockZ();
                endZ = door.getMaximum().getBlockZ();
                break;
            default:
                plugin.getMyLogger().dumpStackTrace("Invalid rotation for bridge opener: " + cardinal.toString());
                break;
            }

        for (int xAxis = startX; xAxis <= endX; ++xAxis)
            for (int yAxis = startY; yAxis <= endY; ++yAxis)
                for (int zAxis = startZ; zAxis <= endZ; ++zAxis)
                    if (!Util.isAirOrLiquid(world.getBlockAt(xAxis, yAxis, zAxis)))
                        return false;

        newMin.setX(startX);
        newMin.setY(startZ);
        newMin.setZ(startZ);
        newMax.setX(endX);
        newMax.setY(endZ);
        newMax.setZ(endZ);

        return true;
    }

    // Check if the bridge should go up or down.
    public MyBlockFace getUpDown(DoorBase door)
    {
        int height = Math.abs(door.getMinimum().getBlockY() - door.getMaximum().getBlockY());
        if (height > 0)
            return MyBlockFace.DOWN;
        return MyBlockFace.UP;
    }

    // Figure out which way the bridge should go.
    private RotateDirection getOpenDirection(DoorBase door, Location newMin, Location newMax)
    {
        MyBlockFace upDown = getUpDown(door);
        MyBlockFace cDir = getCurrentDirection(door);
        boolean NS = cDir == MyBlockFace.NORTH || cDir == MyBlockFace.SOUTH;

        if (upDown.equals(MyBlockFace.UP))
            return isNewPosFree(door, upDown, door.getEngineSide(), newMin, newMax) ?
                RotateDirection.valueOf(door.getEngineSide().toString()) : null;

        if (door.getOpenDir().equals(RotateDirection.CLOCKWISE) && !door.isOpen() ||
            door.getOpenDir().equals(RotateDirection.COUNTERCLOCKWISE) && door.isOpen())
        {
            return NS && isNewPosFree(door, upDown, MyBlockFace.SOUTH, newMin, newMax) ? RotateDirection.SOUTH :
                !NS && isNewPosFree(door, upDown, MyBlockFace.EAST, newMin, newMax) ? RotateDirection.EAST : null;
        }
        if (door.getOpenDir().equals(RotateDirection.CLOCKWISE) && door.isOpen() ||
            door.getOpenDir().equals(RotateDirection.COUNTERCLOCKWISE) && !door.isOpen())
        {
            return NS && isNewPosFree(door, upDown, MyBlockFace.NORTH, newMin, newMax) ? RotateDirection.NORTH :
                !NS && isNewPosFree(door, upDown, MyBlockFace.WEST, newMin, newMax) ? RotateDirection.WEST : null;
        }

        return NS && isNewPosFree(door, upDown, MyBlockFace.NORTH, newMin, newMax) ? RotateDirection.NORTH :
            !NS && isNewPosFree(door, upDown, MyBlockFace.EAST, newMin, newMax) ? RotateDirection.EAST :
            NS && isNewPosFree(door, upDown, MyBlockFace.SOUTH, newMin, newMax) ? RotateDirection.SOUTH :
            !NS && isNewPosFree(door, upDown, MyBlockFace.WEST, newMin, newMax) ? RotateDirection.WEST : null;
    }

    // Get the "current direction". In this context this means on which side of the
    // drawbridge the engine is.
    private MyBlockFace getCurrentDirection(DoorBase door)
    {
        return door.getEngineSide();
    }

    @Override
    public DoorOpenResult openDoor(DoorBase door, double time, boolean instantOpen, boolean silent)
    {
        DoorOpenResult isOpenable = super.isOpenable(door, silent);
        if (isOpenable != DoorOpenResult.SUCCESS)
            return abort(door, isOpenable);
        super.setBusy(door);

        if (super.isTooBig(door))
            instantOpen = true;

        MyBlockFace currentDirection = getCurrentDirection(door);
        if (currentDirection == null)
        {
            plugin.getMyLogger()
                .warn("Current direction is null for bridge " + door.getName() + " (" + door.getDoorUID() + ")!");
            return abort(door, DoorOpenResult.ERROR);
        }

        MyBlockFace upDown = getUpDown(door);
        if (upDown == null)
        {
            plugin.getMyLogger()
                .warn("UpDown direction is null for bridge " + door.getName() + " (" + door.getDoorUID() + ")!");
            return abort(door, DoorOpenResult.ERROR);
        }

        Location newMin = new Location(door.getWorld(), 0, 0, 0);
        Location newMax = new Location(door.getWorld(), 0, 0, 0);
        RotateDirection openDirection = getOpenDirection(door, newMin, newMax);
        if (openDirection == null)
        {
            plugin.getMyLogger().warn("OpenDirection direction is null for bridge " + door.getName() + " ("
                + door.getDoorUID() + ")!");
            return abort(door, DoorOpenResult.NODIRECTION);
        }

        // The door's owner does not have permission to move the door into the new
        // position (e.g. worldguard doens't allow it.
        if (plugin.canBreakBlocksBetweenLocs(door.getPlayerUUID(), newMin, newMax) != null)
            return abort(door, DoorOpenResult.NOPERMISSION);

        plugin.addBlockMover(new BridgeMover(plugin, door.getWorld(), time, door, upDown, openDirection, instantOpen,
                                             plugin.getConfigLoader().getMultiplier(DoorType.DRAWBRIDGE)));

        return DoorOpenResult.SUCCESS;
    }
}
