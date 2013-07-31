package lxx.movement.orbital;

import lxx.model.LxxRobot;
import lxx.movement.MovementDecision;
import lxx.utils.APoint;
import lxx.utils.LxxPoint;
import lxx.utils.LxxUtils;
import robocode.util.Utils;

public class AvoidEnemyOrbitalMovement {

    private final OrbitalMovement orbitalMovement;

    public AvoidEnemyOrbitalMovement(OrbitalMovement orbitalMovement) {
        this.orbitalMovement = orbitalMovement;
    }

    public MovementDecision getMovementDecision(LxxRobot me, APoint center, OrbitDirection direction, LxxRobot enemy) {
        MovementDecision movementDecision = orbitalMovement.getMovementDecision(me, center, direction);

        if (enemy != null) {
            final LxxPoint oppPos = enemy.position;
            final double distToOpponent = me.distance(oppPos);
            if (distToOpponent < 100) {
                final double angleToOpponent = LxxUtils.angle(me.position.x, me.position.y, oppPos.x, oppPos.y);
                final double desiredHeading = (movementDecision.desiredVelocity >= 0
                        ? me.heading
                        : Utils.normalAbsoluteAngle(me.heading + Math.PI)) + movementDecision.turnRate;
                if (((LxxUtils.anglesDiff(desiredHeading, angleToOpponent) < LxxUtils.getRobotWidthInRadians(angleToOpponent, distToOpponent) * 1.01))) {
                    movementDecision = orbitalMovement.getMovementDecision(me, center, OrbitDirection.STOP);
                }
            }

        }

        return movementDecision;
    }

}