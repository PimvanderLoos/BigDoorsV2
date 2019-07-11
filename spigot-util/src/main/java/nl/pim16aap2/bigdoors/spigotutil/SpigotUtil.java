package nl.pim16aap2.bigdoors.spigotutil;

import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents various small and Spigot-specific utility functions.
 *
 * @author Pim
 */
public final class SpigotUtil
{
    /**
     * Send a colored message to a specific player.
     *
     * @param player The player that will receive the message.
     * @param color  Color of the message
     * @param msg    The message to be sent.
     */
    public static void messagePlayer(Player player, ChatColor color, String msg)
    {
        player.sendMessage(color + msg);
    }

    /**
     * Convert a command and its explanation to the help format.
     *
     * @param command     Name of the command.
     * @param explanation Explanation of how to use the command.
     * @return String in the helperformat.
     */
    public static String helpFormat(String command, String explanation)
    {
        return String.format(ChatColor.GREEN + "/%s: " + ChatColor.BLUE + "%s\n", command, explanation);
    }

    private static final Map<PBlockFace, BlockFace> toBlockFace = new HashMap<>();
    private static final Map<BlockFace, PBlockFace> toPBlockFace = new HashMap<>();

    static
    {
        for (PBlockFace mbf : PBlockFace.values())
        {
            BlockFace mappedBlockFace = BlockFace.valueOf(mbf.toString());
            toBlockFace.put(mbf, mappedBlockFace);
            toPBlockFace.put(mappedBlockFace, mbf);
        }
    }

    /**
     * Get the {@link PBlockFace} parallel to the given {@link org.bukkit.block.BlockFace}.
     *
     * @param mbf {@link PBlockFace} that will be converted.
     * @return The parallel {@link org.bukkit.block.BlockFace}.
     */
    public static BlockFace getBukkitFace(PBlockFace mbf)
    {
        return toBlockFace.get(mbf);
    }

    /**
     * Get the {@link org.bukkit.block.BlockFace} parallel to the given {@link PBlockFace}.
     *
     * @param bf {@link org.bukkit.block.BlockFace} that will be converted.
     * @return The parallel {@link PBlockFace}.
     */
    public static PBlockFace getPBlockFace(BlockFace bf)
    {
        return toPBlockFace.get(bf);
    }

    public static boolean printDebugMessages = false;

    /**
     * Broadcast a message if debugging is enabled in the config.
     *
     * @param message The message to broadcast.
     */
    public static void broadcastMessage(String message)
    {
        if (printDebugMessages)
            Bukkit.broadcastMessage(message);
    }

    /**
     * Convert a location to a nicely formatted string of x:y:z using integers.
     *
     * @param loc The location to convert to a string.
     * @return A string of the coordinates of the location.
     */
    public static String locIntToString(Location loc)
    {
        return String.format("(%d;%d;%d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Convert a location to a nicely formatted string of x:y:z using doubles rounded to 2 decimals.
     *
     * @param loc The location to convert to a string.
     * @return A string of the coordinates of the location.
     */
    public static String locDoubleToString(Location loc)
    {
        return String.format("(%.2f;%.2f;%.2f)", loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Get the hash of the location of the chunk that the provided location lies in.
     *
     * @param loc The location to get the chunk Hash of.
     * @return The hash of the chunk
     */
    public static long chunkHashFromLocation(Location loc)
    {
        return chunkHashFromLocation(loc.getBlockX(), loc.getBlockZ(), loc.getWorld().getUID());
    }

    /**
     * Get the hash of a Chunk location.
     *
     * @param x         The X-coordinate of the position in the world (NOT the chunk coordinate!).
     * @param z         The Z-coordinate of the position in the world (NOT the chunk coordinate!).
     * @param worldUUID The UUID of the world.
     * @return The hash of the Chunk location.
     */
    public static long chunkHashFromLocation(int x, int z, UUID worldUUID)
    {
        int chunk_X = x >> 4;
        int chunk_Z = z >> 4;
        long hash = 3;
        hash = 19 * hash + worldUUID.hashCode();
        hash = 19 * hash + (int) (Double.doubleToLongBits(chunk_X) ^ (Double.doubleToLongBits(chunk_X) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(chunk_Z) ^ (Double.doubleToLongBits(chunk_Z) >>> 32));
        return hash;
    }

    /**
     * Generate the hash of a location.
     *
     * @param x         X-coordinate.
     * @param y         Y-coordinate.
     * @param z         Z-coordinate.
     * @param worldUUID UUID of the world.
     * @return Hash of the location.
     */
    public static long locationHash(int x, int y, int z, UUID worldUUID)
    {
        return new Location(Bukkit.getWorld(worldUUID), x, y, z).hashCode();
    }

    public static String nameFromUUID(UUID playerUUID)
    {
        if (playerUUID == null)
            return null;
        String output = null;
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null)
            output = player.getName();
        else
            output = Bukkit.getOfflinePlayer(playerUUID).getName();
        return output;
    }

    /**
     * Try to get a player's UUID from a given name.
     *
     * @param playerName Name of the player.
     * @return UUID of the player if one was found, otherwise null.
     */
    /*
     * First try to get the UUID from an online player, then try an offline player;
     * the first option is faster.
     */
    public static UUID playerUUIDFromString(String playerName)
    {
        Player player = null;
        player = Bukkit.getPlayer(playerName);
        if (player == null)
            try
            {
                player = Bukkit.getPlayer(UUID.fromString(playerName));
            }
            catch (Exception dontcare)
            {
            }
        if (player != null)
            /*
             * Check if the resulting player's name is a match to the provided playerName,
             * because player retrieval from a name is not exact. "pim" would match
             * "pim16aap2", for example.
             */
            return player.getName().equals(playerName) ? player.getUniqueId() : null;

        OfflinePlayer offPlayer = null;
        try
        {
            offPlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerName));
        }
        catch (Exception dontcare)
        {
        }
        if (offPlayer != null)
            return offPlayer.getName().equals(playerName) ? offPlayer.getUniqueId() : null;
        return null;
    }

    /**
     * Play a sound for all players in a range of 15 blocks around the provided location.
     *
     * @param loc    The location of the sound.
     * @param sound  The name of the sound.
     * @param volume The volume
     * @param pitch  The pitch
     */
    public static void playSound(Location loc, String sound, float volume, float pitch)
    {
        for (Entity ent : loc.getWorld().getNearbyEntities(loc, 15, 15, 15))
            if (ent instanceof Player)
                ((Player) ent).playSound(loc, sound, volume, pitch);
    }

    /**
     * Retrieve the number of doors a given player is allowed to won.
     *
     * @param player The player for whom to retrieve the limit.
     * @return The limit if one was found, or -1 if unlimited.
     */
    public static int getMaxDoorsForPlayer(Player player)
    {
        if (player.isOp())
            return -1;
        return getHighestPermissionSuffix(player, "bigdoors.own.");
    }

    /**
     * Retrieve the limit of the door size (measured in blocks) a given player can own.
     *
     * @param player The player for whom to retrieve the limit.
     * @return The limit if one was found, or -1 if unlimited.
     */
    public static int getMaxDoorSizeForPlayer(Player player)
    {
        if (player.isOp())
            return -1;
        return getHighestPermissionSuffix(player, "bigdoors.maxsize.");
    }

    /**
     * Get the highest value of a variable in a permission node of a player.
     * <p>
     * For example, retrieve '8' from 'permission.node.8'.
     *
     * @param player         The player whose permissions to check.
     * @param permissionNode The base permission node.
     * @return The highest value of the variable suffix of the permission node or -1 if none was found.
     */
    private static int getHighestPermissionSuffix(Player player, String permissionNode)
    {
        int ret = -1;
        for (PermissionAttachmentInfo perms : player.getEffectivePermissions())
            if (perms.getPermission().startsWith(permissionNode))
                try
                {
                    ret = Math.max(ret, Integer.valueOf(perms.getPermission().split(permissionNode)[1]));
                }
                catch (Exception e)
                {
                }
        return ret;
    }

    /**
     * Send a white message to a player.
     *
     * @param player Player to receive the message.
     * @param msg    The message.
     */
    public static void messagePlayer(Player player, String msg)
    {
        messagePlayer(player, ChatColor.WHITE, msg);
    }

    /**
     * Send a number message to a player.
     *
     * @param player The player that will receive the message
     * @param msg    The messages
     */
    public static void messagePlayer(Player player, String[] msg)
    {
        messagePlayer(player, Util.stringFromArray(msg));
    }

    /**
     * Send a number of messages to a player.
     *
     * @param player The player that will receive the message
     * @param color  The color of the message
     * @param msg    The messages
     */
    public static void messagePlayer(Player player, ChatColor color, String[] msg)
    {
        messagePlayer(player, color, Util.stringFromArray(msg));
    }

    /**
     * Check if a block if air or liquid (water, lava).
     *
     * @param block The block to be checked.
     * @return True if it is air or liquid.
     */
    public static boolean isAirOrLiquid(Block block)
    {
        // Empty means it's air.
        return block.isLiquid() || block.isEmpty();
    }

    /**
     * Certain material types need to be refreshed when being placed down.
     *
     * @param mat Material to be checked.
     * @return True if it needs to be refreshed.
     *
     * @deprecated I'm pretty sure this is no longer needed.
     */
    @Deprecated
    public static boolean needsRefresh(Material mat)
    {
        switch (mat)
        {
            case ACACIA_FENCE:
            case ACACIA_FENCE_GATE:
            case BIRCH_FENCE:
            case BIRCH_FENCE_GATE:
            case DARK_OAK_FENCE:
            case DARK_OAK_FENCE_GATE:
            case JUNGLE_FENCE:
            case JUNGLE_FENCE_GATE:
            case OAK_FENCE:
            case OAK_FENCE_GATE:
            case SPRUCE_FENCE:
            case SPRUCE_FENCE_GATE:
            case NETHER_BRICK_FENCE:

            case COBBLESTONE_WALL:
            case IRON_BARS:

            case WHITE_STAINED_GLASS_PANE:
            case YELLOW_STAINED_GLASS_PANE:
            case PURPLE_STAINED_GLASS_PANE:
            case LIGHT_BLUE_STAINED_GLASS_PANE:
            case MAGENTA_STAINED_GLASS_PANE:
            case GRAY_STAINED_GLASS_PANE:
            case GREEN_STAINED_GLASS_PANE:
            case BLACK_STAINED_GLASS_PANE:
            case LIME_STAINED_GLASS_PANE:
            case BLUE_STAINED_GLASS_PANE:
            case BROWN_STAINED_GLASS_PANE:
            case CYAN_STAINED_GLASS_PANE:
            case RED_STAINED_GLASS_PANE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if a block is on the blacklist of types/materials that is not allowed for animations.
     *
     * @param block The block to be checked
     * @return True if the block can be used for animations.
     */
    public static boolean isAllowedBlock(Block block)
    {
        if (block == null || isAirOrLiquid(block))
            return false;

        Material mat = block.getType();
        if (mat == null)
            return false;

        BlockData blockData = block.getBlockData();
        BlockState blockState = block.getState();

        if (blockData instanceof org.bukkit.block.data.type.Stairs ||
                blockData instanceof org.bukkit.block.data.type.Gate)
            return true;

        if (blockState instanceof org.bukkit.inventory.InventoryHolder
                // Door, Stairs, TrapDoor, sunflower, tall grass, tall seagrass, large fern,
                // peony, rose bush, lilac,
                || blockData instanceof org.bukkit.block.data.Bisected ||
                blockData instanceof org.bukkit.block.data.Rail
                // Cauldron, Composter, Water, Lava
                || blockData instanceof org.bukkit.block.data.Levelled

                || blockData instanceof org.bukkit.block.data.type.Bed ||
                blockData instanceof org.bukkit.block.data.type.BrewingStand ||
                blockData instanceof org.bukkit.block.data.type.Cake ||
                blockData instanceof org.bukkit.block.data.type.CommandBlock ||
                blockData instanceof org.bukkit.block.data.type.EnderChest ||
                blockData instanceof org.bukkit.block.data.type.Ladder ||
                blockData instanceof org.bukkit.block.data.type.Sapling ||
                blockData instanceof org.bukkit.block.data.type.Sign ||
                blockData instanceof org.bukkit.block.data.type.TechnicalPiston ||
                blockData instanceof org.bukkit.block.data.type.WallSign ||
                blockData instanceof org.bukkit.block.data.type.RedstoneWire ||
                blockData instanceof org.bukkit.block.data.type.RedstoneWallTorch ||
                blockData instanceof org.bukkit.block.data.type.Tripwire ||
                blockData instanceof org.bukkit.block.data.type.TripwireHook ||
                blockData instanceof org.bukkit.block.data.type.Repeater ||
                blockData instanceof org.bukkit.block.data.type.Switch ||
                blockData instanceof org.bukkit.block.data.type.Comparator)
            return false;

        switch (mat)
        {
            case WALL_TORCH:

            case PAINTING:

            case ATTACHED_MELON_STEM:
            case ATTACHED_PUMPKIN_STEM:
            case WHITE_TULIP:
            case DANDELION:
            case SUGAR_CANE:
            case NETHER_WART:
            case CHORUS_FLOWER:
            case CHORUS_FRUIT:
            case SEAGRASS:
            case POPPY:
            case OXEYE_DAISY:
            case LILY_OF_THE_VALLEY:
            case LILY_PAD:
            case VINE:
                return false;
            default:
                break;
        }

        String matName = mat.toString();
        // Potted stuff will always work.
        if (matName.startsWith("POTTED"))
            return true;
        if (matName.endsWith("TULIP") || matName.endsWith("BANNER") || matName.endsWith("CARPET") ||
                matName.endsWith("HEAD"))
            return false;
        return true;
    }

    @Deprecated
    public static int tickRateFromSpeed(double speed)
    {
        int tickRate;
        if (speed > 9)
            tickRate = 1;
        else if (speed > 7)
            tickRate = 2;
        else if (speed > 6)
            tickRate = 3;
        else
            tickRate = 4;
        return tickRate;
    }

    // Return {time, tickRate, distanceMultiplier} for a given door size.
    @Deprecated
    public static double[] calculateTimeAndTickRate(int doorSize, double time, double speedMultiplier, double baseSpeed)
    {
        double ret[] = new double[3];
        double distance = Math.PI * doorSize / 2;
        if (time == 0.0)
            time = baseSpeed + doorSize / 3.5;
        double speed = distance / time;
        if (speedMultiplier != 1.0 && speedMultiplier != 0.0)
        {
            speed *= speedMultiplier;
            time = distance / speed;
        }

        // Too fast or too slow!
        double maxSpeed = 11;
        if (speed > maxSpeed || speed <= 0)
            time = distance / maxSpeed;

        double distanceMultiplier = speed > 4 ? 1.01 : speed > 3.918 ? 1.08 : speed > 3.916 ? 1.10 :
                                                                              speed > 2.812 ? 1.12 :
                                                                              speed > 2.537 ? 1.19 :
                                                                              speed > 2.2 ? 1.22 : speed > 2.0 ? 1.23 :
                                                                                                   speed > 1.770 ?
                                                                                                   1.25 :
                                                                                                   speed > 1.570 ?
                                                                                                   1.28 : 1.30;
        ret[0] = time;
        ret[1] = tickRateFromSpeed(speed);
        ret[2] = distanceMultiplier;
        return ret;
    }
}
