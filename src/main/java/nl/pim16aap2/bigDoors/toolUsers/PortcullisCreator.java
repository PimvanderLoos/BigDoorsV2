package nl.pim16aap2.bigDoors.toolUsers;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.util.DoorType;
import nl.pim16aap2.bigDoors.util.Util;

public class PortcullisCreator extends ToolUser
{	
	public PortcullisCreator(BigDoors plugin, Player player, String name)
	{
		super(plugin, player, name, DoorType.PORTCULLIS);
		Util.messagePlayer(player, messages.getString("CREATOR.PORTCULLIS.Init"));
		if (name == null)
			Util.messagePlayer(player, ChatColor.GREEN, messages.getString("CREATOR.GENERAL.GiveNameInstruc"));
		else
			triggerGiveTool();
	}
	
	@Override
	protected void triggerGiveTool()
	{
		giveToolToPlayer(messages.getString("CREATOR.PORTCULLIS.StickLore"    ).split("\n"), 
		                 messages.getString("CREATOR.PORTCULLIS.StickReceived").split("\n"));
	}
	
	@Override
	protected boolean isReadyToCreateDoor()
	{
		return one != null && two != null && engine != null;
	}
	
	@Override
	protected void triggerFinishUp()
	{
		finishUp(messages.getString("CREATOR.PORTCULLIS.Success"));
	}
	
	// Make sure the power point is in the middle.
	private void setEngine()
	{
		int xMid = one.getBlockX() + (two.getBlockX() - one.getBlockX()) / 2;
		int zMid = one.getBlockZ() + (two.getBlockZ() - one.getBlockZ()) / 2;
		int yMin = one.getBlockY();
		this.engine = new Location(one.getWorld(), xMid, yMin, zMid);
	}
	
	// Make sure the second position is not the same as the first position
	// And that the portcullis is only 1 block deep.
	private boolean isPositionValid(Location loc)
	{
		if (one == null && two == null)
			return true;	
		if (one.equals(loc))
			return false;
		
		int xDepth, zDepth;
		xDepth = Math.abs(loc.getBlockX() - one.getBlockX());
		zDepth = Math.abs(loc.getBlockZ() - one.getBlockZ());
		
		return xDepth == 0 ^ zDepth == 0;
	}
	
	// Take care of the selection points.
	@Override
	public void selector(Location loc)
	{
		if (name == null)
			return;
		if (!isPositionValid(loc))
			return;
		if (this.one == null)
		{
			this.one = loc;
			Util.messagePlayer(player, messages.getString("CREATOR.PORTCULLIS.Step1"));
		}
		else
			this.two = loc;
		
		if (this.one != null && this.two != null)
		{
			minMaxFix();
			setEngine();
			setIsDone(true);
		}
	}
	
	@Override
	public void abort()
	{
		if (!this.done)
		{
			this.takeToolFromPlayer();
			plugin.removeToolUser(this);
			plugin.getMyLogger().returnToSender((CommandSender) player, Level.INFO, ChatColor.RED, messages.getString("CREATOR.GENERAL.TimeUp"));
		}
	}
}
