package nl.pim16aap2.bigDoors.toolUsers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.util.Util;

public class PowerBlockRelocator extends ToolUser
{
	public PowerBlockRelocator(BigDoors plugin, Player player, long doorUID)
	{
		super(plugin, player, null, null);
		this.doorUID = doorUID;
        Util.messagePlayer(player, messages.getString("CREATOR.PBRELOCATOR.Init"));
        triggerGiveTool();
	}

	@Override
	protected void triggerGiveTool()
	{
		giveToolToPlayer(messages.getString("CREATOR.PBRELOCATOR.StickLore"    ).split("\n"),
		                 messages.getString("CREATOR.PBRELOCATOR.StickReceived").split("\n"));
	}

	@Override
	protected void triggerFinishUp()
	{
		if (one != null)
		{
			plugin.getCommander().updatePowerBlockLoc(doorUID, one);
			Util.messagePlayer(player, messages.getString("CREATOR.PBRELOCATOR.Success"));
		}
		takeToolFromPlayer();
	}

	// Take care of the selection points.
	@Override
	public void selector(Location loc)
	{
		if (plugin.getCommander().isPowerBlockLocationValid(loc))
		{
			done = true;
			one  = loc;
			setIsDone(true);
		}
		else
			Util.messagePlayer(player, messages.getString("CREATOR.PBRELOCATOR.LocationInUse"));
	}

	@Override
	protected boolean isReadyToCreateDoor()
	{
		return false;
	}
}
