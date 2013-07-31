package lxx.movement;

import java.io.Serializable;

import static java.lang.Math.toDegrees;

public class MovementDecision implements Serializable {

    public final double desiredVelocity;
    public final double turnRate;

    public MovementDecision(double desiredVelocity, double turnRateRadians) {
        this.desiredVelocity = desiredVelocity;
        this.turnRate = turnRateRadians;
    }

    public String toString() {
        return String.format("(desired speed = %3.2f, turnRate %3.2f)", desiredVelocity, toDegrees(turnRate));
    }

}
