package nl.pim16aap2.bigdoors.commands;

import lombok.SneakyThrows;
import nl.pim16aap2.bigdoors.UnitTestUtil;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.logging.BasicPLogger;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.managers.DelayedCommandInputManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class DelayedCommandInputRequestTest
{
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private CommandDefinition commandDefinition;

    @Mock
    private ICommandSender commandSender;

    private IPLogger logger;


    private ILocalizer localizer;

    private DelayedCommandInputManager delayedCommandInputManager;

    @BeforeEach
    void init()
    {
        MockitoAnnotations.openMocks(this);

        logger = new BasicPLogger();
        localizer = UnitTestUtil.initLocalizer();
        delayedCommandInputManager = new DelayedCommandInputManager();
    }

    @Test
    @SneakyThrows
    void test()
    {
        final DelayedInput delayedInput = new DelayedInput(UUID.randomUUID(), "Some string");
        final DelayedCommandInputRequest<?> inputRequest =
            new DelayedCommandInputRequest<>(100, commandSender, commandDefinition,
                                             input -> verifyInput(delayedInput, input), () -> "", DelayedInput.class,
                                             logger, localizer, delayedCommandInputManager);

        final CompletableFuture<Boolean> first = inputRequest.getCommandOutput();
        final CompletableFuture<Boolean> second = inputRequest.provide(delayedInput);

        Assertions.assertTrue(second.get(1, TimeUnit.SECONDS));
        Assertions.assertEquals(first, second);
    }

    @Test
    @SneakyThrows
    void testInvalidInput()
    {
        final DelayedInput delayedInput = new DelayedInput(UUID.randomUUID(), "Some string");
        final DelayedCommandInputRequest<?> inputRequest =
            new DelayedCommandInputRequest<>(100, commandSender, commandDefinition,
                                             input -> verifyInput(delayedInput, input), () -> "", DelayedInput.class,
                                             logger, localizer, delayedCommandInputManager);

        final CompletableFuture<Boolean> first = inputRequest.getCommandOutput();
        final CompletableFuture<Boolean> second = inputRequest.provide("Invalid!");

        Assertions.assertFalse(second.get(1, TimeUnit.SECONDS));
        Assertions.assertNotEquals(first, second);
    }

    @Test
    @SneakyThrows
    void testException()
    {
        // Ensure that exceptions are properly propagated.
        final DelayedCommandInputRequest<?> inputRequest =
            new DelayedCommandInputRequest<>(100, commandSender, commandDefinition,
                                             input ->
                                             {
                                                 throw new RuntimeException(input.toString());
                                             }, () -> "", DelayedInput.class, logger, localizer,
                                             delayedCommandInputManager);
        Assertions.assertThrows(ExecutionException.class,
                                () -> inputRequest.provide(new DelayedInput(UUID.randomUUID(), ""))
                                                  .get(1, TimeUnit.SECONDS));
    }

    private CompletableFuture<Boolean> verifyInput(DelayedInput actualInput, DelayedInput delayedInput)
    {
        Assertions.assertEquals(actualInput, delayedInput);
        return CompletableFuture.completedFuture(true);
    }

    private record DelayedInput(UUID uuid, String string)
    {
    }
}
