package nl.pim16aap2.animatedarchitecture.structures.bigdoor;

import lombok.extern.flogger.Flogger;
import nl.pim16aap2.animatedarchitecture.core.animation.AnimationRequestData;
import nl.pim16aap2.animatedarchitecture.core.animation.AnimationUtil;
import nl.pim16aap2.animatedarchitecture.core.animation.IAnimationComponent;
import nl.pim16aap2.animatedarchitecture.core.animation.IAnimator;
import nl.pim16aap2.animatedarchitecture.core.animation.RotatedPosition;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimatedBlockData;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureSnapshot;
import nl.pim16aap2.animatedarchitecture.core.util.MathUtil;
import nl.pim16aap2.animatedarchitecture.core.util.MovementDirection;
import nl.pim16aap2.animatedarchitecture.core.util.Util;
import nl.pim16aap2.animatedarchitecture.core.util.vector.IVector3D;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Dd;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Flogger
public class BigDoorAnimationComponent implements IAnimationComponent
{
    private final MovementDirection movementDirection;
    private final StructureSnapshot snapshot;
    private final Vector3Dd rotationCenter;
    private final double resultAngle;
    private final double step;
    private final int quarterCircles;

    public BigDoorAnimationComponent(AnimationRequestData data, MovementDirection movementDirection, int quarterCircles)
    {
        this.snapshot = data.getStructureSnapshot();
        this.movementDirection = movementDirection;
        this.quarterCircles = quarterCircles;

        resultAngle = movementDirection == MovementDirection.CLOCKWISE ? quarterCircles * MathUtil.HALF_PI :
                      movementDirection == MovementDirection.COUNTERCLOCKWISE ? quarterCircles * -MathUtil.HALF_PI :
                      0.0D;

        if (resultAngle == 0.0D)
            log.atSevere()
               .log("Invalid open direction '%s' for structure: %d", movementDirection.name(), snapshot.getUid());

        rotationCenter = new Vector3Dd(
            snapshot.getRotationPoint().x(),
            snapshot.getCuboid().getMin().y(),
            snapshot.getRotationPoint().z());

        final int animationDuration =
            AnimationUtil.getAnimationTicks(data.getAnimationTime(), data.getServerTickTime());

        this.step = this.resultAngle / animationDuration;
    }

    @Override
    public RotatedPosition getFinalPosition(int xAxis, int yAxis, int zAxis)
    {
        return getGoalPos(Util.clampAngleRad(resultAngle), xAxis, yAxis, zAxis);
    }

    @Override
    public RotatedPosition getStartPosition(int xAxis, int yAxis, int zAxis)
    {
        return getGoalPos(0, xAxis, yAxis, zAxis);
    }

    @Override
    public void executeAnimationStep(IAnimator animator, Iterable<IAnimatedBlock> animatedBlocks, int ticks)
    {
        final double angle = Util.clampAngleRad(step * ticks);
        final double cos = Math.cos(angle);
        final double sin = Math.sin(angle);
        final Vector3Dd localRotation = getGoalRotation(angle);

        for (final IAnimatedBlock animatedBlock : animatedBlocks)
            animator.applyMovement(animatedBlock, getGoalPos(animatedBlock, localRotation, angle, cos, sin));
    }

    private RotatedPosition getGoalPos(
        @Nullable Vector3Dd localRotation,
        double angle, double cos, double sin,
        double startX, double startY, double startZ)
    {
        final double translatedX = startX - rotationCenter.x();
        final double translatedZ = startZ - rotationCenter.z();

        final double changeX = translatedX * cos - translatedZ * sin;
        final double changeZ = translatedX * sin + translatedZ * cos;

        final Vector3Dd goalPos = new Vector3Dd(rotationCenter.x() + changeX, startY, rotationCenter.z() + changeZ);
        final Vector3Dd goalRot = localRotation != null ? localRotation : getGoalRotation(angle);

        return new RotatedPosition(goalPos, goalRot);
    }

    private Vector3Dd getGoalRotation(double angle)
    {
        final double yaw = -Math.toDegrees(angle + MathUtil.HALF_PI);
        return new Vector3Dd(0, 0, yaw);
    }

    private RotatedPosition getGoalPos(double angle, double startX, double startY, double startZ)
    {
        return getGoalPos(null, angle, Math.cos(angle), Math.sin(angle), startX, startY, startZ);
    }

    private RotatedPosition getGoalPos(
        IAnimatedBlock animatedBlock, @Nullable Vector3Dd localRotation, double angle, double cos, double sin)
    {
        return getGoalPos(
            localRotation, angle, cos, sin,
            animatedBlock.getStartX(), animatedBlock.getStartY(), animatedBlock.getStartZ());
    }

    static float getRadius(IVector3D rotationPoint, int xAxis, int zAxis)
    {
        final double deltaA = rotationPoint.xD() - xAxis;
        final double deltaB = rotationPoint.zD() - zAxis;
        return (float) Math.sqrt(Math.pow(deltaA, 2) + Math.pow(deltaB, 2));
    }

    @Override
    public float getRadius(int xAxis, int yAxis, int zAxis)
    {
        return getRadius(snapshot.getRotationPoint(), xAxis, zAxis);
    }

    @Override
    public @Nullable Consumer<IAnimatedBlockData> getBlockDataRotator()
    {
        return blockData -> blockData.rotateBlock(movementDirection, quarterCircles);
    }
}
