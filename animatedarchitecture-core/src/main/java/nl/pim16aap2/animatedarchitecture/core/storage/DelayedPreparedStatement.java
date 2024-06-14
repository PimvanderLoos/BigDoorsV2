package nl.pim16aap2.animatedarchitecture.core.storage;

import nl.pim16aap2.animatedarchitecture.core.util.Util;
import nl.pim16aap2.animatedarchitecture.core.util.functional.CheckedTriConsumer;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.regex.Pattern;

/**
 * Represents a wrapper around {@link PreparedStatement}. It can be used to set variables before obtaining a
 * connection.
 * <p>
 * It also allows you to easily create a String containing the full statement with all the variable substituted, which
 * can be useful for debugging purposes.
 */
@SuppressWarnings("unused")
public class DelayedPreparedStatement
{
    private final Action<?>[] actions;
    private String statement;
    public static final Pattern QUESTION_MARK = Pattern.compile("\\?");
    private int currentIDX = 1;

    /**
     * Keeps track of the number of variables that should be skipped.
     * <p>
     * This number is almost always going to 0, but in case {@link #setRawString(int, String)} has been invoked, it will
     * be higher.
     */
    private int skipCount = 0;

    /**
     * Constructs a new {@link DelayedPreparedStatement}.
     * <p>
     * It counts the number of variables ('?' characters) in the statement.
     *
     * @param statement
     *     The SQL statement.
     */
    public DelayedPreparedStatement(String statement)
    {
        actions = new Action[Util.countPatternOccurrences(DelayedPreparedStatement.QUESTION_MARK, statement)];
        this.statement = statement;
    }

    /**
     * Constructs a new {@link DelayedPreparedStatement}.
     *
     * @param variableCount
     *     The number of variables ('?' characters) in the statement.
     * @param statement
     *     The SQL statement.
     */
    public DelayedPreparedStatement(int variableCount, String statement)
    {
        actions = new Action[variableCount];
        this.statement = statement;
    }

    @Override
    public String toString()
    {
        String result = statement;
        for (int idx = 0; idx < (actions.length - skipCount); ++idx)
        {
            final Action<?> action = actions[idx];
            if (action == null || action.obj == null)
                result = result.replaceFirst("[?]", "NULL");
            else if (action.obj instanceof Number)
                result = result.replaceFirst("[?]", action.obj.toString());
            else
                result = result.replaceFirst("[?]", "\"" + action.obj + "\"");
        }
        return result;
    }

    /**
     * Constructs a {@link PreparedStatement} from this {@link DelayedPreparedStatement}, applying all variables as
     * defined up to this point.
     *
     * @param conn
     *     A connection to a database.
     * @return The constructed {@link PreparedStatement}.
     *
     * @throws SQLException
     */
    public PreparedStatement construct(Connection conn)
        throws SQLException
    {
        final PreparedStatement ps = conn.prepareStatement(statement);
        for (int idx = 0; idx < (actions.length - skipCount); ++idx)
            actions[idx].applyOn(ps);
        return ps;
    }

    /**
     * Constructs a {@link PreparedStatement} from this {@link DelayedPreparedStatement}, applying all variables as
     * defined up to this point.
     *
     * @param conn
     *     A connection to a database.
     * @param autoGeneratedKeys
     *     A flag indicating whether auto-generated keys should be returned; one of Statement.RETURN_GENERATED_KEYS or
     *     Statement.NO_GENERATED_KEYS
     * @return The constructed {@link PreparedStatement}.
     *
     * @throws SQLException
     */
    public PreparedStatement construct(Connection conn, int autoGeneratedKeys)
        throws SQLException
    {
        final PreparedStatement ps = conn.prepareStatement(statement, autoGeneratedKeys);
        for (int idx = 0; idx < actions.length - skipCount; ++idx)
            actions[idx].applyOn(ps);
        return ps;
    }

    /**
     * Gets the real index based on the received index value.
     *
     * @param idx
     *     The received index value.
     * @return The actual index value.
     */
    private int getRealIndex(int idx)
    {
        return idx - 1 - skipCount;
    }

    /**
     * Replaces a '?' at the given index with a raw String. Unlike all other types, this is executed before constructing
     * the actual {@link PreparedStatement} and modifies {@link #statement}.
     * <p>
     * Note that you'll have to make sure that the String is sanitized and valid. For example, when using it to specify
     * the name of a table, the name must comply with {@link IStorage#isValidTableName(String)}. This means that only
     * alphanumerics characters and the underscore character are allowed.
     * <p>
     * Most importantly: When using this method, it <b><u>MUST BE USED in the correct order</u></b>. Variables that
     * should be set before should be added before and those that should be set after should be added after.
     *
     * @param idx
     *     The index of the variable character (= "?") in the statement that should be replaced by the table name.
     * @param obj
     *     The name of the table.
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setRawString(int idx, String obj)
    {
        if (!IStorage.isValidTableName(obj))
            throw new IllegalArgumentException("Trying to set table name using an invalid string: " + obj);

        final String tmp = statement;
        int questionMarkPos = tmp.indexOf('?');
        if (questionMarkPos == -1)
            throw new IllegalArgumentException("Trying to set table name in a statement without any variables!");

        // TODO: Precompute the indices of the question marks.
        int questionMarkIDX = 1;

        while (questionMarkPos > -1 && questionMarkIDX < idx)
        {
            ++questionMarkIDX;
            questionMarkPos = tmp.indexOf('?', questionMarkPos + 1);
        }

        statement = statement.substring(0, questionMarkPos) + obj + statement.substring(questionMarkPos + 1);

        ++skipCount;

        return this;
    }

    /**
     * See {@link PreparedStatement#setBoolean(int, boolean)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextBoolean(Boolean obj)
    {
        return setBoolean(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setBoolean(int, boolean)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setBoolean(int idx, Boolean obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setBoolean, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setByte(int, byte)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextByte(Byte obj)
    {
        return setByte(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setByte(int, byte)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setByte(int idx, Byte obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setByte, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setShort(int, short)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextShort(Short obj)
    {
        return setShort(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setShort(int, short)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setShort(int idx, Short obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setShort, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setInt(int, int)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextInt(Integer obj)
    {
        return setInt(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setInt(int, int)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setInt(int idx, Integer obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setInt, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setLong(int, long)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextLong(Long obj)
    {
        return setLong(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setLong(int, long)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setLong(int idx, Long obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setLong, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setFloat(int, float)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextFloat(Float obj)
    {
        return setFloat(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setFloat(int, float)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setFloat(int idx, Float obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setFloat, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setDouble(int, double)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextDouble(Double obj)
    {
        return setDouble(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setDouble(int, double)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setDouble(int idx, Double obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setDouble, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setBigDecimal(int, BigDecimal)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextBigDecimal(BigDecimal obj)
    {
        return setBigDecimal(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setBigDecimal(int, BigDecimal)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setBigDecimal(int idx, BigDecimal obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setBigDecimal, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setString(int, String)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextString(@Nullable String obj)
    {
        return setString(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setString(int, String)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setString(int idx, @Nullable String obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setString, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setBytes(int, byte[])}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextBytes(byte[] obj)
    {
        return setBytes(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setBytes(int, byte[])}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setBytes(int idx, byte[] obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setBytes, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setDate(int, Date)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextDate(@Nullable Date obj)
    {
        return setDate(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setDate(int, Date)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setDate(int idx, @Nullable Date obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setDate, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setTime(int, Time)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextTime(@Nullable Time obj)
    {
        return setTime(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setTime(int, Time)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setTime(int idx, @Nullable Time obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setTime, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setTimestamp(int, Timestamp)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextTimestamp(@Nullable Timestamp obj)
    {
        return setTimestamp(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setTimestamp(int, Timestamp)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setTimestamp(int idx, @Nullable Timestamp obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setTimestamp, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setObject(int, Object)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextObject(@Nullable Object obj)
    {
        return setObject(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setObject(int, Object)}.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setObject(int idx, @Nullable Object obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setObject, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setNull(int, int)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNextNull(int sqlType)
    {
        return setNull(currentIDX, sqlType);
    }

    /**
     * See {@link PreparedStatement#setNull(int, int)}
     *
     * @return This {@link DelayedPreparedStatement}.
     */
    public DelayedPreparedStatement setNull(int idx, int sqlType)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action<>(PreparedStatement::setNull, idx - skipCount, sqlType);
        return this;
    }

    /**
     * Represents an action that will set a typed value in a {@link PreparedStatement}. E.g.
     * {@link PreparedStatement#setBoolean(int, boolean)}. This class allows delaying the actual action until a
     * PreparedStatement is created.
     *
     * @param <T>
     *     The type of the data to set.
     */
    private record Action<T>(CheckedTriConsumer<PreparedStatement, Integer, @Nullable T, SQLException> fun, int idx,
        @Nullable T obj)
    {
        /**
         * Applies the {@link #obj} at the given {@link #idx} in the provided {@link PreparedStatement}.
         *
         * @param ps
         *     The PreparedStatement to use for setting the
         * @throws SQLException
         */
        @SuppressWarnings({"ConstantConditions", "NullAway"})
        private void applyOn(PreparedStatement ps)
            throws SQLException
        {
            fun.accept(ps, idx, obj);
        }
    }
}
