package nl.pim16aap2.bigdoors.text;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Represents a piece of text with styled sections.
 *
 * @author Pim
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class Text
{
    /**
     * The {@link ColorScheme} used to add styles to sections of this text.
     */
    @Getter
    private final ColorScheme colorScheme;

    /**
     * The {@link StringBuilder} backing this {@link Text} object. All strings appended to this {@link Text} will be
     * stored here.
     */
    private final StringBuilder stringBuilder = new StringBuilder();

    /**
     * The list of {@link StyledSection}s.
     */
    private List<StyledSection> styledSections = new ArrayList<>();

    /**
     * The total size of the string held by this object. This is used by {@link #toString()} to instantiate a
     * {@link StringBuilder} of the right size.
     * <p>
     * This value includes both the size of {@link #stringBuilder} as well as the total size of all the
     * {@link #styledSections}.
     */
    private int styledSize = 0;

    // CopyConstructor
    public Text(Text other)
    {
        colorScheme = other.colorScheme;
        stringBuilder.append(other.stringBuilder);
        other.styledSections.forEach(section -> styledSections.add(new StyledSection(section)));
        styledSize = other.styledSize;
    }

    /**
     * Gets the length of the raw text without any styles applied to it.
     *
     * @return The length of the raw text without any styles applied to it.
     */
    public int getLength()
    {
        return stringBuilder.length();
    }

    /**
     * The length of the text including all the styles.
     *
     * @return The length of the text including all the styles.
     */
    public int getStyledLength()
    {
        return styledSize;
    }

    /**
     * Gets a subsection from this {@link Text}.
     * <p>
     * See {@link StringBuilder#substring(int, int)}.
     * <p>
     * Any styles/decorators that have at least 1 character inside this range will be copied.
     *
     * @param start
     *     The beginning index, inclusive.
     * @param end
     *     The ending index, exclusive.
     * @return The {@link Text} in the given range.
     */
    @Contract("_, _ -> this")
    public Text subsection(int start, int end)
    {
        if (start == 0 && end == stringBuilder.length())
            return this;

        if (end <= start)
            throw new RuntimeException(String.format("The end (%d) of a substring cannot be before it (%d)!",
                                                     end, start));

        if (start < 0 || end > stringBuilder.length())
            throw new RuntimeException(String.format("Range [%d %d] out of bounds for range: [0 %d]!",
                                                     start, end, stringBuilder.length()));

        final String string = stringBuilder.substring(start, end);
        final Text newText = new Text(colorScheme);
        newText.add(string);

        for (final StyledSection section : styledSections)
        {
            if (section.getStartIndex() >= end)
                break;

            if (section.getEnd() < start)
                continue;

            int length = section.getLength();

            int startIdx = section.getStartIndex() - start;
            if (startIdx < 0)
            {
                length += startIdx;
                startIdx = 0;
            }

            if (section.getEnd() > end)
                length -= (section.getEnd() - end);

            if (length <= 0)
                continue;

            newText.styledSections.add(new StyledSection(startIdx, length, section.getStyle()));
        }

        return newText;
    }

    /**
     * Appends some unstyled text to the current text.
     *
     * @param text
     *     The unstyled text to add.
     * @return The current {@link Text} instance.
     */
    @Contract("_ -> this")
    public Text add(String text)
    {
        stringBuilder.append(text);
        styledSize += text.length();
        return this;
    }

    /**
     * Appends some styled text to the current text.
     *
     * @param text
     *     The text to add.
     * @param type
     *     The {@link TextType} of the text to add. The {@link #colorScheme} will be used to look up the style
     *     associated with the type. See {@link ColorScheme#getStyle(TextType)}.
     * @return The current {@link Text} instance.
     */
    @Contract("_, _ -> this")
    public Text add(String text, @Nullable TextType type)
    {
        if (type != null)
        {
            final TextComponent style = colorScheme.getStyle(type);
            styledSections.add(new StyledSection(stringBuilder.length(), text.length(), style));
            styledSize += style.getOn().length() + style.getOff().length();
        }
        return add(text);
    }

    /**
     * Prepends another {@link Text} object to this object, so the other text is placed before the current one.
     * <p>
     * The other {@link Text} instance is not modified.
     *
     * @param other
     *     The {@link Text} to insert before the current {@link Text}.
     * @return The current {@link Text} instance.
     */
    @Contract("_ -> this")
    public Text prepend(Text other)
    {
        styledSections = appendSections(other.getLength(), other.styledSections, styledSections,
                                        (section, offset) -> new StyledSection(section.startIndex + offset,
                                                                               section.length, section.style));
        stringBuilder.insert(0, other.stringBuilder);
        return this;
    }

    /**
     * Appends the (copied) values of a list to the values of another list into a list.
     *
     * @param offset
     *     The offset of the second set of values.
     * @param first
     *     The first set of values. All values in this list will maintain their index in the new list.
     * @param last
     *     The last set of values. These values will be placed after the first set of values.
     * @param copier
     *     The function that creates a new list entry from the current entry and the offset value (this value is 0 for
     *     the first list).
     * @param <T>
     *     The type of the entries in the list.
     * @return The new list with all the values copied from the first and the last list (in that order).
     */
    private static <T> List<T> appendSections(
        final int offset,
        final List<T> first,
        final List<T> last,
        final BiFunction<T, Integer, T> copier)
    {
        final List<T> ret = new ArrayList<>(first.size() + last.size());
        first.forEach(entry -> ret.add(copier.apply(entry, 0)));
        last.forEach(entry -> ret.add(copier.apply(entry, offset)));
        return ret;
    }

    /**
     * Appends another {@link Text} object to this one.
     * <p>
     * The other {@link Text} object will not be modified.
     *
     * @param other
     *     The other {@link Text} instance to append to the current one.
     * @return The current {@link Text} instance.
     */
    @Contract("_ -> this")
    public Text add(Text other)
    {
        if (other.stringBuilder.length() == 0)
            return this;

        styledSections = appendSections(getLength(), styledSections, other.styledSections,
                                        (section, offset) -> new StyledSection(section.startIndex + offset,
                                                                               section.length, section.style));
        stringBuilder.append(other.stringBuilder);
        styledSize += other.styledSize;
        return this;
    }

    @Override
    public String toString()
    {
        if (stringBuilder.length() == 0)
            return "";

        final StringBuilder sb = new StringBuilder(styledSize);
        int lastIdx = 0;
        for (final StyledSection section : styledSections)
        {
            // If there are any parts without any styles.
            if (section.startIndex > lastIdx)
                sb.append(stringBuilder.substring(lastIdx, section.startIndex));
            final int end = section.getEnd();
            sb.append(section.getStyle().getOn())
              .append(stringBuilder.substring(section.getStartIndex(), end))
              .append(section.getStyle().getOff());
            lastIdx = end;
        }

        // Add any trailing text that doesn't have any styles.
        if (lastIdx < stringBuilder.length())
            sb.append(stringBuilder.substring(lastIdx, stringBuilder.length()));

        return sb.toString();
    }

    /**
     * Gets the plain String without any styles.
     *
     * @return The plain String without any styles.
     */
    public String toPlainString()
    {
        return stringBuilder.toString();
    }

    /**
     * Represents a section in a text that is associated with a certain style.
     *
     * @author Pim
     */
    @AllArgsConstructor
    @Getter
    private static class StyledSection
    {
        private final int startIndex;
        private final int length;
        private final TextComponent style;

        // Copy constructor
        public StyledSection(final StyledSection other)
        {
            startIndex = other.startIndex;
            length = other.length;
            style = other.style;
        }

        int getEnd()
        {
            return startIndex + length;
        }
    }
}
