package lxx.movement;

import lxx.model.LxxRobot;
import lxx.utils.LxxConstants;
import lxx.utils.LxxUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.io.Serializable;

import static java.lang.Math.toDegrees;

public class MovementDecision implements Serializable {

    public final double desiredVelocity;
    public final double turnRate;

    public MovementDecision(double desiredVelocity, double turnRateRadians) {
        this.desiredVelocity = desiredVelocity;
        this.turnRate = turnRateRadians;
    }

    public double getTurnRate() {
        return turnRate;
    }

    public double getDesiredVelocity() {
        return desiredVelocity;
    }

    public static MovementDecision toMovementDecision(LxxRobot robot, double desiredSpeed, double desiredHeading) {
        if (desiredSpeed > Rules.MAX_VELOCITY) {
            desiredSpeed = Rules.MAX_VELOCITY;
        }

        final double headingRadians = robot.heading;
        final boolean wantToGoFront = LxxUtils.anglesDiff(headingRadians, desiredHeading) < LxxConstants.RADIANS_90;
        final double normalizedDesiredHeading = wantToGoFront ? desiredHeading : Utils.normalAbsoluteAngle(desiredHeading + LxxConstants.RADIANS_180);

        final double turnRemaining = Utils.normalRelativeAngle(normalizedDesiredHeading - headingRadians);
        final double speed = robot.speed;
        final double turnRateRadiansLimit = Rules.getTurnRateRadians(speed);
        final double turnRateRadians =
                LxxUtils.limit(-turnRateRadiansLimit,
                        turnRemaining,
                        turnRateRadiansLimit);

        return new MovementDecision(desiredSpeed * (wantToGoFront ? 1 : -1), turnRateRadians);
    }

    public String toString() {
        return String.format("(desired speed = %3.2f, turnRate %3.2f)", desiredVelocity, toDegrees(turnRate));
    }

}
