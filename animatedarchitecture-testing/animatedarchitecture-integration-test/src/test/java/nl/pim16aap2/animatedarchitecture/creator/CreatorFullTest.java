package nl.pim16aap2.animatedarchitecture.creator;

import nl.pim16aap2.animatedarchitecture.core.UnitTestUtil;
import nl.pim16aap2.animatedarchitecture.core.api.IPlayer;
import nl.pim16aap2.animatedarchitecture.core.structures.Structure;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureType;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.Property;
import nl.pim16aap2.animatedarchitecture.core.tooluser.Step;
import nl.pim16aap2.animatedarchitecture.core.tooluser.ToolUser;
import nl.pim16aap2.animatedarchitecture.core.tooluser.creator.Creator;
import nl.pim16aap2.animatedarchitecture.core.tooluser.creator.CreatorTest;
import nl.pim16aap2.animatedarchitecture.core.util.MovementDirection;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Di;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * This class tests the general creation flow of the creator process.
 * <p>
 * The specific methods are test in {@link CreatorTest}.
 */
@Timeout(1)
class CreatorFullTest extends CreatorTestsUtil
{
    private final List<Property<?>> properties = List.of(Property.OPEN_STATUS, Property.ROTATION_POINT);

    private Structure structure;

    @Override
    @BeforeEach
    public void beforeEach()
    {
        super.beforeEach();

        final var structureType = Mockito.mock(StructureType.class);
        Mockito
            .when(structureType.getValidMovementDirections())
            .thenReturn(EnumSet.of(MovementDirection.NORTH, MovementDirection.SOUTH));

        Mockito
            .when(structureType.getProperties())
            .thenReturn(properties);

        structure = Mockito.mock(Structure.class);

        Mockito
            .when(structure.getType())
            .thenReturn(structureType);

        UnitTestUtil.setPropertyContainerInMockedStructure(structure, properties);
    }

    @Test
    void runThroughProcess()
    {
        openDirection = MovementDirection.NORTH;

        final var creator = new CreatorTestImpl(context, player, structure);

        setEconomyEnabled(true);
        setEconomyPrice(12.34);
        setBuyStructure(true);

        final Vector3Di rotationPoint = cuboid.getCenterBlock();
        final boolean isOpen = false;

        testCreation(
            creator,
            structure,
            structureName,
            UnitTestUtil.getLocation(min, world),
            UnitTestUtil.getLocation(max, world),
            UnitTestUtil.getLocation(rotationPoint, world),
            UnitTestUtil.getLocation(powerblock, world),
            isOpen,
            openDirection,
            true
        );
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void delayedOpenDirectionInput()
    {
        openDirection = MovementDirection.NORTH;

        final var creator = new CreatorTestImpl(context, player, structure);

        setEconomyEnabled(true);
        setEconomyPrice(12.34);
        setBuyStructure(true);

        applySteps(
            creator,
            structureName,
            UnitTestUtil.getLocation(min, world),
            UnitTestUtil.getLocation(max, world),
            UnitTestUtil.getLocation(cuboid.getCenterBlock(), world),
            UnitTestUtil.getLocation(powerblock, world)
        );

        Assertions.assertDoesNotThrow(() ->
            delayedCommandInputManager.getInputRequest(player).get().provide(false).join());

        Assertions.assertDoesNotThrow(() ->
            delayedCommandInputManager.getInputRequest(player).get().provide(MovementDirection.EAST).join());

        Assertions.assertDoesNotThrow(() ->
            delayedCommandInputManager.getInputRequest(player).get().provide(openDirection).join());

        testCreation(creator, structure, true);
    }

    private static class CreatorTestImpl extends Creator
    {
        private final Structure structure;

        protected CreatorTestImpl(ToolUser.Context context, IPlayer player, Structure structure)
        {
            super(context, structure.getType(), player, null);
            this.structure = structure;
            init();
        }

        @Override
        protected synchronized @NotNull List<Step> generateSteps()
            throws InstantiationException
        {
            return Arrays.asList(
                factoryProvideName.messageKey("CREATOR_BASE_GIVE_NAME").construct(),
                factoryProvideFirstPos.messageKey("CREATOR_BIG_DOOR_STEP1").construct(),
                factoryProvideSecondPos.messageKey("CREATOR_BIG_DOOR_STEP2").construct(),
                factoryProvideRotationPointPos.messageKey("CREATOR_BIG_DOOR_STEP3").construct(),
                factoryProvidePowerBlockPos.messageKey("CREATOR_BASE_SET_POWER_BLOCK").construct(),
                factoryProvideOpenStatus.messageKey("CREATOR_BASE_SET_OPEN_DIR").construct(),
                factoryProvideOpenDir.messageKey("CREATOR_BASE_SET_OPEN_DIR").construct(),
                factoryConfirmPrice.messageKey("CREATOR_BASE_CONFIRM_PRICE").construct(),
                factoryCompleteProcess.messageKey("CREATOR_BIG_DOOR_SUCCESS").construct());
        }

        @Override
        protected void giveTool()
        {
        }

        @Override
        protected @NotNull Structure constructStructure()
        {
            return structure;
        }
    }
}
