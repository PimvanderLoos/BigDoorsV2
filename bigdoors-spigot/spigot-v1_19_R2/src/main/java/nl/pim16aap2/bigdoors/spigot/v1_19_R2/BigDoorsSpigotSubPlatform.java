package nl.pim16aap2.bigdoors.spigot.v1_19_R2;

import lombok.Getter;
import nl.pim16aap2.bigdoors.core.annotations.Initializer;
import nl.pim16aap2.bigdoors.core.api.IBlockAnalyzer;
import nl.pim16aap2.bigdoors.core.api.IExecutor;
import nl.pim16aap2.bigdoors.core.api.factories.IAnimatedBlockFactory;
import nl.pim16aap2.bigdoors.core.managers.AnimatedBlockHookManager;
import nl.pim16aap2.bigdoors.spigot.util.api.IBigDoorsSpigotSubPlatform;
import nl.pim16aap2.bigdoors.spigot.util.api.IGlowingBlockFactory;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class BigDoorsSpigotSubPlatform implements IBigDoorsSpigotSubPlatform
{
    private static final String VERSION = "v1_19_R2";

    @Getter
    private IAnimatedBlockFactory animatedBlockFactory;

    @Getter
    private IBlockAnalyzer blockAnalyzer;

    @Getter
    private IGlowingBlockFactory glowingBlockFactory;

    private final AnimatedBlockHookManager animatedBlockHookManager;
    private final IExecutor executor;

    @Inject
    public BigDoorsSpigotSubPlatform(AnimatedBlockHookManager animatedBlockHookManager, IExecutor executor)
    {
        this.animatedBlockHookManager = animatedBlockHookManager;
        this.executor = executor;
    }

    @Override
    public String getVersion()
    {
        return VERSION;
    }

    @Override
    @Initializer
    public void init(JavaPlugin plugin)
    {
        animatedBlockFactory = new AnimatedBlockFactory(animatedBlockHookManager, executor);
        blockAnalyzer = new BlockAnalyzer();
        glowingBlockFactory = new GlowingBlock.Factory();
    }
}
