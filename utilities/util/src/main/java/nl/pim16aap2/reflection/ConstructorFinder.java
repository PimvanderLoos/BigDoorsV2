package nl.pim16aap2.reflection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Represents a {@link ReflectionFinder} that can find {@link Constructor}s.
 *
 * @author Pim
 */
@SuppressWarnings("unused")
public class ConstructorFinder
{
    /**
     * Sets the class this constructor finder will search in for constructors.
     *
     * @param source
     *     The class to analyze.
     * @return The next step in the constructor finding process.
     */
    @Contract("_ -> new")
    public ConstructorFinderInSource inClass(Class<?> source)
    {
        return new ConstructorFinderInSource(Objects.requireNonNull(source, "Source class cannot be null!"));
    }

    /**
     * Represents an implementation of {@link ReflectionFinder}
     */
    public static final class ConstructorFinderInSource
        extends ReflectionFinder.ReflectionFinderWithParameters<Constructor<?>, ConstructorFinderInSource>
    {
        private final Class<?> source;

        private ConstructorFinderInSource(Class<?> source)
        {
            this.source = source;
        }

        @Override
        public Constructor<?> getRequired()
        {
            return Objects.requireNonNull(get(), String.format("Failed to find constructor [%s %s(%s)].",
                                                               ReflectionBackend.optionalModifiersToString(modifiers),
                                                               source.getName(),
                                                               ReflectionBackend.formatOptionalValue(parameters)));
        }

        @Override
        public @Nullable Constructor<?> get()
        {
            return ReflectionBackend.findCTor(source, modifiers, parameters);
        }
    }
}
