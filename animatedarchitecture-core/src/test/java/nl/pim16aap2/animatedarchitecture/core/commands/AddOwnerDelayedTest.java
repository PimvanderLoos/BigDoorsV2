package nl.pim16aap2.animatedarchitecture.core.commands;

import nl.pim16aap2.animatedarchitecture.core.UnitTestUtil;
import nl.pim16aap2.animatedarchitecture.core.api.IPlayer;
import nl.pim16aap2.animatedarchitecture.core.api.debugging.DebuggableRegistry;
import nl.pim16aap2.animatedarchitecture.core.localization.ILocalizer;
import nl.pim16aap2.animatedarchitecture.core.managers.DelayedCommandInputManager;
import nl.pim16aap2.animatedarchitecture.core.structures.Structure;
import nl.pim16aap2.animatedarchitecture.core.structures.PermissionLevel;
import nl.pim16aap2.animatedarchitecture.core.structures.retriever.StructureRetriever;
import nl.pim16aap2.animatedarchitecture.core.structures.retriever.StructureRetrieverFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.inject.Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.AdditionalAnswers.delegatesTo;

@Timeout(1)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unused")
class AddOwnerDelayedTest
{
    @Spy
    DelayedCommandInputManager delayedCommandInputManager =
        new DelayedCommandInputManager(Mockito.mock(DebuggableRegistry.class));

    ILocalizer localizer = UnitTestUtil.initLocalizer();

    @Mock
    DelayedCommandInputRequest.IFactory<AddOwnerDelayed.DelayedInput> inputRequestFactory;

    @InjectMocks
    DelayedCommand.Context context;

    @Mock
    CommandFactory commandFactory;

    @SuppressWarnings("unchecked")
    Provider<CommandFactory> commandFactoryProvider =
        Mockito.mock(Provider.class, delegatesTo((Provider<CommandFactory>) () -> commandFactory));

    @Mock
    ICommandSender commandSender;

    StructureRetriever structureRetriever;

    @Mock
    Structure structure;

    @InjectMocks
    StructureRetrieverFactory structureRetrieverFactory;

    @Mock
    AddOwner addOwner;

    @Mock
    IPlayer targetPlayer;

    @BeforeEach
    void init()
    {
        DelayedCommandTest.initInputRequestFactory(inputRequestFactory, localizer, delayedCommandInputManager);

        structureRetriever = structureRetrieverFactory.of(structure);

        Mockito.when(addOwner.run()).thenReturn(CompletableFuture.completedFuture(null));

        Mockito.when(commandFactory.newAddOwner(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(addOwner);
    }

    @Test
    void normal()
    {
        final AddOwnerDelayed addOwnerDelayed = new AddOwnerDelayed(context, inputRequestFactory);

        final CompletableFuture<?> result0 = addOwnerDelayed.runDelayed(commandSender, structureRetriever);
        final AddOwnerDelayed.DelayedInput input = new AddOwnerDelayed.DelayedInput(targetPlayer);
        final CompletableFuture<?> result1 = addOwnerDelayed.provideDelayedInput(commandSender, input);

        Assertions.assertDoesNotThrow(() -> result0.get(1, TimeUnit.SECONDS));
        Assertions.assertDoesNotThrow(() -> result1.get(1, TimeUnit.SECONDS));

        Mockito.verify(commandFactory, Mockito.times(1))
            .newAddOwner(commandSender, structureRetriever, input.getTargetPlayer(), PermissionLevel.USER);
    }
}
