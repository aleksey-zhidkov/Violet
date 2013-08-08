package lxx.movement.orbital;

import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.movement.MovementDecision;
import lxx.utils.LxxPoint;
import lxx.utils.LxxUtils;
import robocode.util.Utils;

import static java.lang.Math.abs;
import static lxx.utils.LxxConstants.*;
import static lxx.utils.LxxUtils.limit;

public class AvoidEnemyOrbitalMovement {

    private final OrbitalMovement orbitalMovement;

    public AvoidEnemyOrbitalMovement(OrbitalMovement orbitalMovement) {
        this.orbitalMovement = orbitalMovement;
    }

    public MovementDecision getMovementDecision(LxxRobot me, LxxWave wave, OrbitDirection direction, LxxRobot enemy) {
        final double maxAttackAngle = getMaxAttackAngle(me, enemy, wave);
        MovementDecision movementDecision = orbitalMovement.getMovementDecision(me, wave, direction, maxAttackAngle);

        if (enemy == null) {
            return movementDecision;
        }

        final LxxPoint oppPos = enemy.position;
        final double distToOpponent = me.distance(oppPos);
        if (distToOpponent < 100) {
            final double angleToOpponent = LxxUtils.angle(me.position.x, me.position.y, oppPos.x, oppPos.y);
            final double desiredHeading = (movementDecision.desiredVelocity >= 0
                    ? me.heading
                    : Utils.normalAbsoluteAngle(me.heading + Math.PI)) + movementDecision.turnRate;
            if (((LxxUtils.anglesDiff(desiredHeading, angleToOpponent) < LxxUtils.getRobotWidthInRadians(angleToOpponent, distToOpponent) * 1.01))) {
                movementDecision = orbitalMovement.getMovementDecision(me, wave, OrbitDirection.STOP, maxAttackAngle);
            }
        }

        return movementDecision;
    }

    private double getMaxAttackAngle(LxxRobot me, LxxRobot opponent, LxxWave wave) {

        if (opponent == null) {
            return RADIANS_90;
        }

        final double distancePart = RADIANS_30 * (1 - limit(0, me.distance(opponent) - 50, orbitalMovement.getDesiredDistance()) / orbitalMovement.getDesiredDistance());

        final double opponentDirectionPart;
        if (Double.isNaN(opponent.movementDirection)) {
            opponentDirectionPart = 0;
        } else {
            opponentDirectionPart = RADIANS_30 * (1 - abs(Utils.normalRelativeAngle(opponent.movementDirection - opponent.angleTo(me)))) / RADIANS_180;
        }

        final double wavePart = RADIANS_30 * limit(0, wave.getFlightTime(me) - 10, 30) / 30;

        return distancePart + opponentDirectionPart + wavePart;

    }

}
