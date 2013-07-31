package lxx.movement.orbital;

import lxx.model.LxxRobot2;
import lxx.movement.MovementDecision;
import lxx.utils.APoint;
import lxx.utils.BattleField;
import lxx.utils.LxxConstants;
import lxx.utils.LxxUtils;
import robocode.Rules;
import robocode.util.Utils;

public class OrbitalMovement {

    private final BattleField battleField;
    private final double desiredDistance;

    public OrbitalMovement(BattleField battleField, double desiredDistance) {
        this.battleField = battleField;
        this.desiredDistance = desiredDistance;
    }

    public MovementDecision getMovementDecision(LxxRobot2 me, APoint center, OrbitDirection direction) {
        double desiredHeading;

        if (direction.direction != 0) {
            desiredHeading = battleField.smoothWalls(me.position, getDesiredHeading(me, center, direction), direction.direction == 1);
        } else {
            desiredHeading = Utils.normalAbsoluteAngle(center.angleTo(me.position) + LxxConstants.RADIANS_90);
        }

        return toMovementDecision(me, direction.speed, desiredHeading);
    }

    private MovementDecision toMovementDecision(LxxRobot2 robot, double desiredSpeed, double desiredHeading) {
        final boolean wantToGoFront = LxxUtils.anglesDiff(robot.heading, desiredHeading) < LxxConstants.RADIANS_90;
        final double normalizedDesiredHeading = wantToGoFront ? desiredHeading : Utils.normalAbsoluteAngle(desiredHeading + LxxConstants.RADIANS_180);

        final double turnRemaining = Utils.normalRelativeAngle(normalizedDesiredHeading - robot.heading);
        final double turnRateRadiansLimit = Rules.getTurnRateRadians(robot.speed);
        final double turnRate =
                LxxUtils.limit(-turnRateRadiansLimit,
                        turnRemaining,
                        turnRateRadiansLimit);

        return new MovementDecision(desiredSpeed * (wantToGoFront ? 1 : -1), turnRate);
    }

    private double getDesiredHeading(LxxRobot2 me, APoint center, OrbitDirection direction) {
        final double distanceBetween = me.position.distance(center);

        final double distanceDiff = distanceBetween - desiredDistance;
        final double attackAngleKoeff = distanceDiff / desiredDistance;

        final double maxAttackAngle = LxxConstants.RADIANS_100;
        final double minAttackAngle = LxxConstants.RADIANS_80;
        final double attackAngle = LxxConstants.RADIANS_90 + (LxxConstants.RADIANS_30 * attackAngleKoeff);

        final double angleToMe = LxxUtils.angle(center.x(), center.y(), me.position.x, me.position.y);

        return Utils.normalAbsoluteAngle(angleToMe +
                LxxUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * direction.direction);
    }


}
