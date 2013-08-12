package lxx.movement.orbital;

import lxx.model.LxxRobot;
import lxx.movement.MovementDecision;
import lxx.utils.APoint;
import lxx.utils.BattleField;
import lxx.utils.LxxConstants;
import robocode.util.Utils;

import static java.lang.Math.*;
import static lxx.utils.LxxUtils.*;
import static robocode.Rules.*;
import static robocode.util.Utils.normalRelativeAngle;

public class OrbitalMovement {

    private final BattleField battleField;
    private final double desiredDistance;

    public OrbitalMovement(BattleField battleField, double desiredDistance) {
        this.battleField = battleField;
        this.desiredDistance = desiredDistance;
    }

    public MovementDecision getMovementDecision(LxxRobot me, APoint center, OrbitDirection direction, double minAttackAngle) {
        final double desiredHeading;
        final double smoothedHeading;
        if (direction.direction != 0) {
            desiredHeading = getDesiredHeading(me, center, direction, minAttackAngle);
            smoothedHeading = battleField.smoothWalls(me.position, desiredHeading, direction.direction == 1);
        } else {
            desiredHeading = Utils.normalAbsoluteAngle(center.angleTo(me.position) + LxxConstants.RADIANS_90);
            smoothedHeading = desiredHeading;
        }

        return toMovementDecision(me, direction.speed, smoothedHeading, !Utils.isNear(desiredHeading, smoothedHeading));
    }

    private MovementDecision toMovementDecision(LxxRobot robot, double desiredSpeed, double desiredHeading, boolean forceTurn) {
        final boolean wantToGoFront = anglesDiff(robot.heading, desiredHeading) < LxxConstants.RADIANS_90;
        final double normalizedDesiredHeading = wantToGoFront ? desiredHeading : Utils.normalAbsoluteAngle(desiredHeading + LxxConstants.RADIANS_180);

        final double turnRemaining = normalRelativeAngle(normalizedDesiredHeading - robot.heading);
        final double turnRateRadiansLimit = getTurnRateRadians(robot.speed);
        final double turnRate = limit(-turnRateRadiansLimit, turnRemaining, turnRateRadiansLimit);

        final double speed = forceTurn ? min(desiredSpeed, getRequiredSpeed(abs(turnRemaining - abs(turnRate)))) : desiredSpeed;
        return new MovementDecision(speed * (wantToGoFront ? 1 : -1), turnRate);
    }

    private double getDesiredHeading(LxxRobot me, APoint center, OrbitDirection direction, double minAttackAngle) {
        final double distanceBetween = me.position.distance(center);

        final double distanceDiff = distanceBetween - desiredDistance;
        final double attackAngleKoeff = distanceDiff / desiredDistance;

        final double attackAngle = LxxConstants.RADIANS_90 + (LxxConstants.RADIANS_90 * attackAngleKoeff);

        final double angleToMe = angle(center.x(), center.y(), me.position.x, me.position.y);

        return Utils.normalAbsoluteAngle(angleToMe +
                limit(minAttackAngle, attackAngle, LxxConstants.RADIANS_100) * direction.direction);
    }

    public double getDesiredDistance() {
        return desiredDistance;
    }
}
