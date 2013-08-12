package lxx.model;

import lxx.utils.*;
import lxx.utils.func.F1;
import lxx.utils.func.Option;

import java.util.List;

import static java.lang.Double.NaN;
import static java.lang.Math.abs;

public class LxxRobot implements APoint {

    public static final String UNKNOWN = "Unknown";
    public static final F1<LxxRobot, String> toName = new F1<LxxRobot, String>() {
        @Override
        public String f(LxxRobot lxxRobot) {
            return lxxRobot.name;
        }
    };
    public static final F1<LxxRobot, Double> toHeading = new F1<LxxRobot, Double>() {
        @Override
        public Double f(LxxRobot lxxRobot) {
            return lxxRobot.heading;
        }
    };
    public static final F1<LxxRobot, Double> toVelocity = new F1<LxxRobot, Double>() {
        @Override
        public Double f(LxxRobot lxxRobot) {
            return lxxRobot.velocity;
        }
    };
    public static final F1<LxxRobot, Long> toLastScanTime = new F1<LxxRobot, Long>() {
        @Override
        public Long f(LxxRobot lxxRobot) {
            return lxxRobot.lastScanTime;
        }
    };
    public static final F1<LxxRobot, Boolean> toAlive = new F1<LxxRobot, Boolean>() {
        @Override
        public Boolean f(LxxRobot lxxRobot) {
            return lxxRobot.alive;
        }
    };
    public final Option<LxxRobot> prevState;
    public final BattleRules rules;
    public final String name;
    public final LxxPoint position;
    public final double velocity;
    public final double heading;
    public final double energy;
    public final long lastScanTime;
    public final long time;
    public final int round;
    public final Double radarHeading;
    public final Double gunHeading;
    public final boolean alive;
    public final double firePower;
    public final double gunHeat;
    public final double speed;
    public final double acceleration;
    public final double movementDirection;
    public final List<LxxWave> bulletsInAir;

    public LxxRobot(Option<LxxRobot> prevState, BattleRules rules, String name, LxxPoint position, double velocity, double heading,
                    double energy, long lastScanTime, long time, int round, Double radarHeading, Double gunHeading, boolean alive,
                    double firePower, double gunHeat, double speed, double acceleration, double movementDirection, List<LxxWave> bulletsInAir) {
        this.prevState = prevState;
        this.rules = rules;
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.heading = heading;
        this.energy = energy;
        this.lastScanTime = lastScanTime;
        this.time = time;
        this.round = round;
        this.radarHeading = radarHeading;
        this.gunHeading = gunHeading;
        this.alive = alive;
        this.firePower = firePower;
        this.gunHeat = gunHeat;
        this.speed = speed;
        this.acceleration = acceleration;
        this.movementDirection = movementDirection;
        this.bulletsInAir = bulletsInAir;
    }

    public LxxRobot(LxxRobot original, double turnRate, double desiredVelocity) {
        prevState = Option.of(original);
        velocity = LxxUtils.getNewVelocity(original.velocity, desiredVelocity);
        speed = abs(velocity);
        heading = original.heading + turnRate;
        position = (LxxPoint) original.project(heading, velocity);
        time = original.time + 1;
        round = original.round;
        alive = original.alive;

        energy = NaN;
        lastScanTime = -1;
        radarHeading = null;
        gunHeading = null;
        firePower = NaN;
        gunHeat = NaN;
        acceleration = NaN;
        movementDirection = NaN;
        rules = null;
        name = null;
        bulletsInAir = null;
    }

    @Override
    public double x() {
        return position.getX();
    }

    @Override
    public double y() {
        return position.getY();
    }

    @Override
    public double angleTo(APoint pnt) {
        return position.angleTo(pnt);
    }

    @Override
    public APoint project(double alpha, double distance) {
        return position.project(alpha, distance);
    }

    @Override
    public APoint project(Vector2D dv) {
        return position.project(dv);
    }

    @Override
    public double distance(APoint to) {
        return position.distance(to);
    }

    @Override
    public double distance(double x, double y) {
        return position.distance(x, y);
    }

    public double getTurnsToGunCool() {
        return gunHeat / rules.gunCoolingRate;
    }

    public boolean known() {
        return !UNKNOWN.equals(name);
    }

    public boolean outDated() {
        return time != lastScanTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LxxRobot lxxRobot = (LxxRobot) o;

        if (round != lxxRobot.round) return false;
        if (time != lxxRobot.time) return false;
        if (!name.equals(lxxRobot.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + round;
        return result;
    }

}
