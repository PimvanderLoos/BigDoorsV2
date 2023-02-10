package nl.pim16aap2.bigdoors.spigot.core.factories.locationfactory;

import nl.pim16aap2.bigdoors.core.api.IWorld;
import nl.pim16aap2.bigdoors.core.api.factories.ILocationFactory;
import nl.pim16aap2.bigdoors.core.api.factories.IWorldFactory;
import nl.pim16aap2.bigdoors.core.util.vector.IVector3D;
import nl.pim16aap2.bigdoors.spigot.util.implementations.LocationSpigot;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Represents an implementation of {@link ILocationFactory} for the Spigot platform.
 *
 * @author Pim
 */
@Singleton
public class LocationFactorySpigot implements ILocationFactory
{
    private final IWorldFactory worldFactory;

    @Inject
    public LocationFactorySpigot(IWorldFactory worldFactory)
    {
        this.worldFactory = worldFactory;
    }

    @Override
    public LocationSpigot create(IWorld world, double x, double y, double z)
    {
        return new LocationSpigot(world, x, y, z);
    }

    @Override
    public LocationSpigot create(IWorld world, IVector3D position)
    {
        return create(world, position.xD(), position.yD(), position.zD());
    }

    @Override
    public LocationSpigot create(String worldName, double x, double y, double z)
    {
        return create(worldFactory.create(worldName), x, y, z);
    }

    @Override
    public LocationSpigot create(String worldName, IVector3D position)
    {
        return create(worldName, position.xD(), position.yD(), position.zD());
    }
}
