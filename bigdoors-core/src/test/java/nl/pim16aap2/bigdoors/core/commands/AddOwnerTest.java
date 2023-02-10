package nl.pim16aap2.bigdoors.core.commands;

import nl.pim16aap2.bigdoors.core.UnitTestUtil;
import nl.pim16aap2.bigdoors.core.api.IPlayer;
import nl.pim16aap2.bigdoors.core.api.factories.ITextFactory;
import nl.pim16aap2.bigdoors.core.localization.ILocalizer;
import nl.pim16aap2.bigdoors.core.managers.DatabaseManager;
import nl.pim16aap2.bigdoors.core.structures.AbstractStructure;
import nl.pim16aap2.bigdoors.core.structures.PermissionLevel;
import nl.pim16aap2.bigdoors.core.structuretypes.StructureType;
import nl.pim16aap2.bigdoors.core.util.structureretriever.StructureRetriever;
import nl.pim16aap2.bigdoors.core.util.structureretriever.StructureRetrieverFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Timeout(1)
class AddOwnerTest
{
    private ILocalizer localizer;

    @Mock
    private DatabaseManager databaseManager;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AddOwner.IFactory factory;

    private StructureRetriever doorRetriever;

    @Mock
    private AbstractStructure door;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IPlayer commandSender;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IPlayer target;

    private AddOwner addOwnerCreator;
    private AddOwner addOwnerAdmin;
    private AddOwner addOwnerUser;

    @BeforeEach
    void init()
    {
        MockitoAnnotations.openMocks(this);

        final StructureType doorType = Mockito.mock(StructureType.class);
        Mockito.when(doorType.getLocalizationKey()).thenReturn("DoorType");
        Mockito.when(door.getType()).thenReturn(doorType);

        localizer = UnitTestUtil.initLocalizer();

        Mockito.when(databaseManager.addOwner(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
               .thenReturn(CompletableFuture.completedFuture(DatabaseManager.ActionResult.SUCCESS));

        Mockito.when(factory.newAddOwner(Mockito.any(ICommandSender.class),
                                         Mockito.any(StructureRetriever.class),
                                         Mockito.any(IPlayer.class),
                                         Mockito.any(PermissionLevel.class)))
               .thenAnswer((Answer<AddOwner>) invoc ->
                   new AddOwner(invoc.getArgument(0, ICommandSender.class), localizer,
                                ITextFactory.getSimpleTextFactory(),
                                invoc.getArgument(1, StructureRetriever.class),
                                invoc.getArgument(2, IPlayer.class), invoc.getArgument(3, PermissionLevel.class),
                                databaseManager));

        CommandTestingUtil.initCommandSenderPermissions(commandSender, true, true);
        doorRetriever = StructureRetrieverFactory.ofStructure(door);

        addOwnerCreator = factory.newAddOwner(commandSender, doorRetriever, target, PermissionLevel.CREATOR);
        addOwnerAdmin = factory.newAddOwner(commandSender, doorRetriever, target, PermissionLevel.ADMIN);
        addOwnerUser = factory.newAddOwner(commandSender, doorRetriever, target, PermissionLevel.USER);
    }

    @Test
    void testInputValidity()
    {
        Assertions.assertFalse(addOwnerCreator.validInput());
        Assertions.assertTrue(addOwnerAdmin.validInput());
        Assertions.assertTrue(addOwnerUser.validInput());
        Assertions.assertFalse(
            factory.newAddOwner(commandSender, doorRetriever, target, PermissionLevel.NO_PERMISSION).validInput());
    }

    @Test
    void testIsAllowed()
    {
        Assertions.assertTrue(addOwnerAdmin.isAllowed(door, true));
        Assertions.assertFalse(addOwnerAdmin.isAllowed(door, false));

        Mockito.when(door.getOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerCreator));
        Assertions.assertFalse(addOwnerCreator.isAllowed(door, false));
        Assertions.assertTrue(addOwnerAdmin.isAllowed(door, false));
        Assertions.assertTrue(addOwnerUser.isAllowed(door, false));

        Mockito.when(door.getOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerAdmin));
        Assertions.assertFalse(addOwnerCreator.isAllowed(door, false));
        Assertions.assertFalse(addOwnerAdmin.isAllowed(door, false));
        Assertions.assertTrue(addOwnerUser.isAllowed(door, false));

        Mockito.when(door.getOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerUser));
        Assertions.assertFalse(addOwnerCreator.isAllowed(door, false));
        Assertions.assertFalse(addOwnerAdmin.isAllowed(door, false));
        Assertions.assertFalse(addOwnerUser.isAllowed(door, false));
    }

    @Test
    void nonPlayer()
    {
        final ICommandSender server = Mockito.mock(ICommandSender.class, Answers.CALLS_REAL_METHODS);
        final AddOwner addOwner = factory.newAddOwner(server, doorRetriever, target, PermissionLevel.CREATOR);

        Mockito.when(door.getOwner(target)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerCreator));
        Assertions.assertFalse(addOwner.isAllowed(door, false));

        Mockito.when(door.getOwner(target)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerAdmin));
        Assertions.assertTrue(addOwner.isAllowed(door, false));
    }

    @Test
    void testIsAllowedExistingTarget()
    {
        Mockito.when(door.getOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerCreator));
        Mockito.when(door.getOwner(target)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerAdmin));
        Assertions.assertFalse(addOwnerAdmin.isAllowed(door, false));
        Assertions.assertTrue(addOwnerUser.isAllowed(door, false));

        Mockito.when(door.getOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerAdmin));
        Mockito.when(door.getOwner(target)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerAdmin));
        Assertions.assertFalse(addOwnerAdmin.isAllowed(door, false));
        Assertions.assertFalse(addOwnerUser.isAllowed(door, false));

        Mockito.when(door.getOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerCreator));
        Mockito.when(door.getOwner(target)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerUser));
        Assertions.assertTrue(addOwnerAdmin.isAllowed(door, false));
        Assertions.assertFalse(addOwnerUser.isAllowed(door, false));

        Mockito.when(door.getOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerAdmin));
        Mockito.when(door.getOwner(target)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerCreator));
        Assertions.assertFalse(addOwnerAdmin.isAllowed(door, false));
        Assertions.assertFalse(addOwnerUser.isAllowed(door, false));

        // It should never be possible to re-assign level 0 ownership, even with bypass enabled.
        Mockito.when(door.getOwner(target)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerCreator));
        Assertions.assertFalse(addOwnerAdmin.isAllowed(door, true));
        Assertions.assertFalse(addOwnerUser.isAllowed(door, true));
    }

    @Test
    void testDatabaseInteraction()
    {
        Mockito.when(door.getOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerCreator));
        Mockito.when(door.getOwner(target)).thenReturn(Optional.of(CommandTestingUtil.structureOwnerAdmin));
        Mockito.when(door.isOwner(Mockito.any(UUID.class))).thenReturn(true);
        Mockito.when(door.isOwner(Mockito.any(IPlayer.class))).thenReturn(true);

        final CompletableFuture<?> result =
            factory.newAddOwner(commandSender, doorRetriever, target, AddOwner.DEFAULT_PERMISSION_LEVEL).run();

        Assertions.assertDoesNotThrow(() -> result.get(1, TimeUnit.SECONDS));
        Mockito.verify(databaseManager, Mockito.times(1)).addOwner(door, target, AddOwner.DEFAULT_PERMISSION_LEVEL,
                                                                   commandSender.getPlayer().orElse(null));
    }
}
