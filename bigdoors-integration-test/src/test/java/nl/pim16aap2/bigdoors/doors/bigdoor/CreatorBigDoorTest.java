package nl.pim16aap2.bigdoors.doors.bigdoor;

import nl.pim16aap2.bigdoors.tooluser.creator.CreatorTestsUtil;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreatorBigDoorTest extends CreatorTestsUtil
{
    @BeforeEach
    public void setup()
    {
        super.beforeEach();
    }

    @Test
    public void createBigDoor()
        throws InterruptedException
    {
        openDirection = RotateDirection.CLOCKWISE;
        String openDirectionName = "0";

        final @NotNull BigDoor actualDoor = new BigDoor(constructDoorData());
        final @NotNull CreatorBigDoor creator = new CreatorBigDoor(PLAYER);
        testCreation(creator, actualDoor,
                     doorName,
                     min.toLocation(world),
                     max.toLocation(world),
                     engine.toLocation(world),
                     powerblock.toLocation(world),
                     openDirectionName);
    }
}
