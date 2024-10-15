package nl.pim16aap2.animatedarchitecture.core.structures.properties;

import java.util.Iterator;
import java.util.Map;

/**
 * Represents a read-only property container.
 * <p>
 * This interface is used to get the values of properties from a property container.
 * <p>
 * This is a specialization of {@link IPropertyHolderConst} that is used specifically for property containers.
 */
public sealed interface IPropertyContainerConst
    extends IPropertyHolderConst, Iterable<Map.Entry<String, IPropertyValue<?>>>
    permits PropertyContainer, PropertyContainerSnapshot
{
    /**
     * Returns a read-only iterator over the properties in this container.
     * <p>
     * The iterator contains the properties as key-value pairs.
     * <p>
     * Each key is the fully qualified key of the property. See {@link Property#getFullKey()}.
     *
     * @return A read-only iterator over the properties in this container.
     */
    @Override
    Iterator<Map.Entry<String, IPropertyValue<?>>> iterator();
}
