package nl.pim16aap2.bigdoors.commands;

import nl.pim16aap2.bigdoors.api.IConfigLoader;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.managers.DelayedCommandInputManager;
import nl.pim16aap2.bigdoors.util.Constants;
import nl.pim16aap2.bigdoors.util.delayedinput.DelayedInputRequest;
import nl.pim16aap2.bigdoors.util.doorretriever.DoorRetriever;
import nl.pim16aap2.bigdoors.util.doorretriever.DoorRetrieverFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a delayed command.
 * <p>
 * A delayed command allows "starting" the command while only knowing the target door. This is useful when the door is
 * already known (e.g. after having been selected in a GUI), but the other specific data is not.
 * <p>
 * An example usage of this system could be changing the name of a door via a GUI. Using the GUI, the user has to select
 * a door to apply the change to, but we'll need to wait for user input to update the name. For a user, having to use
 * specify the door they would need to change in a command or something would be rather awkward, so this way we can
 * remember that information and not require the user to input duplicate data.
 *
 * @author Pim
 */
public abstract class DelayedCommand<T>
{
    protected final DelayedCommandInputManager delayedCommandInputManager;
    protected final ILocalizer localizer;
    private final IConfigLoader configLoader;
    protected final Provider<CommandFactory> commandFactory;
    protected final DelayedCommandInputRequest.IFactory<T> inputRequestFactory;
    private final Class<T> delayedInputClz;

    protected DelayedCommand(
        Context context,
        DelayedCommandInputRequest.IFactory<T> inputRequestFactory,
        Class<T> delayedInputClz)
    {
        this.delayedCommandInputManager = context.delayedCommandInputManager;
        this.localizer = context.localizer;
        this.configLoader = context.configLoader;
        this.commandFactory = context.commandFactory;
        this.inputRequestFactory = inputRequestFactory;
        this.delayedInputClz = delayedInputClz;
    }

    /**
     * Starts the (new) {@link DelayedInputRequest} for this delayed command.
     * <p>
     * The {@link DelayedCommandInputRequest} will be used to retrieve the values that are required to execute the
     * command. The player will be asked to use the command (again, if needed) to supply the missing data.
     * <p>
     * These missing data can be supplied using {@link #provideDelayedInput(ICommandSender, T)}. Once the data are
     * supplied, the command will be executed.
     *
     * @param commandSender
     *     The entity that sent the command and is held responsible (i.e. permissions, communication) for its
     *     execution.
     * @param doorRetriever
     *     A {@link DoorRetrieverFactory} that references the target door.
     * @return See {@link BaseCommand#run()}.
     */
    public CompletableFuture<Boolean> runDelayed(ICommandSender commandSender, DoorRetriever doorRetriever)
    {
        final int commandTimeout = Constants.COMMAND_WAITER_TIMEOUT;
        return inputRequestFactory.create(commandTimeout, commandSender, getCommandDefinition(),
                                          delayedInput -> delayedInputExecutor(commandSender, doorRetriever,
                                                                               delayedInput),
                                          () -> inputRequestMessage(commandSender, doorRetriever), delayedInputClz)
                                  .getCommandOutput();
    }

    /**
     * Provides the delayed input if there is currently an active {@link DelayedCommandInputRequest} for the {@link
     * ICommandSender}. After processing the input, the new command will be executed immediately.
     * <p>
     * If no active {@link DelayedCommandInputRequest} can be found for the command sender, the command sender will be
     * informed about it (e.g. "We are not waiting for a command!").
     *
     * @param commandSender
     *     The {@link ICommandSender} for which to look for an active {@link DelayedCommandInputRequest} that can be
     *     fulfilled.
     * @param data
     *     The data specified by the user.
     * @return See {@link BaseCommand#run()}.
     */
    public CompletableFuture<Boolean> provideDelayedInput(ICommandSender commandSender, T data)
    {
        return delayedCommandInputManager
            .getInputRequest(commandSender)
            .map(request -> request.provide(data))
            .orElseGet(
                () ->
                {
                    commandSender.sendMessage(getNotWaitingMessage());
                    return CompletableFuture.completedFuture(false);
                });
    }

    protected abstract CommandDefinition getCommandDefinition();

    /**
     * @return The message to send to the user when they are trying to provide input while we are not waiting for any.
     */
    protected abstract String getNotWaitingMessage();

    /**
     * The method that is run once delayed input is received.
     * <p>
     * It processes the new input and executes the command using the previously-provided data (see {@link
     * #runDelayed(ICommandSender, DoorRetriever)}).
     *
     * @param commandSender
     *     The entity that sent the command and is held responsible (i.e. permissions, communication) for its
     *     execution.
     * @param doorRetriever
     *     A {@link DoorRetrieverFactory} that references the target door.
     * @param delayedInput
     *     The delayed input that was retrieved.
     * @return See {@link BaseCommand#run()}.
     */
    protected abstract CompletableFuture<Boolean> delayedInputExecutor(
        ICommandSender commandSender, DoorRetriever doorRetriever, T delayedInput);

    /**
     * Retrieves the message that will be sent to the command sender after initialization of a delayed input request.
     *
     * @param commandSender
     *     The user responsible for the delayed command.
     * @param doorRetriever
     *     The door retriever as currently specified.
     * @return The init message for the delayed input request.
     */
    protected abstract String inputRequestMessage(ICommandSender commandSender, DoorRetriever doorRetriever);

    static final class Context
    {
        final DelayedCommandInputManager delayedCommandInputManager;
        final ILocalizer localizer;
        final IConfigLoader configLoader;
        final Provider<CommandFactory> commandFactory;

        @Inject
        public Context(
            DelayedCommandInputManager delayedCommandInputManager,
            ILocalizer localizer,
            IConfigLoader configLoader,
            Provider<CommandFactory> commandFactory)
        {
            this.delayedCommandInputManager = delayedCommandInputManager;
            this.localizer = localizer;
            this.configLoader = configLoader;
            this.commandFactory = commandFactory;
        }
    }
}
