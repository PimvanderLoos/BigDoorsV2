package nl.pim16aap2.animatedarchitecture.spigot.core.animation.recovery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Type;

/**
 * Represents the data type for the recovery data of an animated block.
 * <p>
 * This class is used to store the recovery data of an animated block. These data are used to recover the state of an
 * animated block that has been 'orphaned' (e.g. during a server crash).
 * <p>
 * This class can be used through {@link #INSTANCE}.
 */
public final class AnimatedBlockRecoveryDataType implements PersistentDataType<String, IAnimatedBlockRecoveryData>
{
    /**
     * The instance of this class.
     */
    public static final AnimatedBlockRecoveryDataType INSTANCE = new AnimatedBlockRecoveryDataType();

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(BlockData.class, new BlockDataSerializer())
        .registerTypeAdapter(BlockData.class, new BlockDataDeserializer())
        .create();

    private AnimatedBlockRecoveryDataType()
    {
    }

    @Override
    public Class<String> getPrimitiveType()
    {
        return String.class;
    }

    @Override
    public Class<IAnimatedBlockRecoveryData> getComplexType()
    {
        return IAnimatedBlockRecoveryData.class;
    }

    @Override
    public String toPrimitive(IAnimatedBlockRecoveryData complex, PersistentDataAdapterContext context)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", complex.getClass().getName());
        jsonObject.add("data", gson.toJsonTree(complex));
        return gson.toJson(jsonObject);
    }

    @Override
    public IAnimatedBlockRecoveryData fromPrimitive(String primitive, PersistentDataAdapterContext context)
    {
        final JsonObject jsonObject = gson.fromJson(primitive, JsonObject.class);
        final String type = jsonObject.get("type").getAsString();
        final JsonElement element = jsonObject.get("data");

        try
        {
            Class<?> clazz = Class.forName(type);
            return (IAnimatedBlockRecoveryData) gson.fromJson(element, clazz);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("Unknown RecoveryData subclass: " + type);
        }
    }

    private static class BlockDataSerializer implements JsonSerializer<BlockData>
    {
        @Override
        public JsonElement serialize(BlockData src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(src.getAsString());
        }
    }

    private static class BlockDataDeserializer implements JsonDeserializer<BlockData>
    {
        @Override
        public BlockData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
        {
            return Bukkit.createBlockData(json.getAsString());
        }
    }
}
