package lxx.model;

import lxx.utils.*;
import robocode.Bullet;
import robocode.Rules;
import robocode.util.Utils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static java.lang.Double.NaN;
import static java.lang.Math.*;

public class LxxRobot implements APoint {

    public static final String UNKNOWN_ENEMY = "Unknown";

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

    public final Collection<Bullet> hitBullets;
    public final Collection<Bullet> interceptedBullets;
    public final BattleRules rules;
    public final String name;

    public LxxRobot(BattleRules rules, String name) {
        this.rules = rules;

        energy = rules.initialEnergy;
        gunHeat = rules.initialGunHeat;
        alive = true;
        this.name = name;

        position = null;
        velocity = 0;
        heading = 0;
        lastScanTime = 0;
        time = 0;
        round = 0;
        radarHeading = null;
        gunHeading = null;
        firePower = 0;

        speed = 0;

        acceleration = 0;
        movementDirection = 0;

        hitBullets = new LinkedList<Bullet>();
        interceptedBullets = new LinkedList<Bullet>();

        assert gunHeat >= 0 && gunHeat <= rules.initialGunHeat : gunHeat;
    }

    public LxxRobot(LxxRobot prevState, long time) {
        this.time = time;

        round = prevState.round;
        position = prevState.position;
        velocity = prevState.velocity;
        heading = prevState.heading;
        energy = prevState.energy;
        lastScanTime = prevState.lastScanTime;
        radarHeading = prevState.radarHeading;
        gunHeading = prevState.gunHeading;
        alive = prevState.alive;
        firePower = 0;
        gunHeat = max(0, prevState.gunHeat - prevState.rules.gunCoolingRate * time);

        speed = prevState.speed;

        acceleration = prevState.acceleration;
        movementDirection = prevState.movementDirection;

        hitBullets = new LinkedList<Bullet>();
        interceptedBullets = new LinkedList<Bullet>();
        rules = prevState.rules;
        name = prevState.name;

        assert gunHeat >= 0 && gunHeat <= rules.initialGunHeat : gunHeat;
    }

    public LxxRobot(LxxRobot prevState, LxxRobotInfo currentState) {
        rules = prevState.rules;
        position = currentState.position;
        velocity = currentState.velocity;
        heading = currentState.heading;
        energy = currentState.energy;
        lastScanTime = currentState.time;
        time = currentState.time;
        round = currentState.round;
        radarHeading = currentState.radarHeading;
        gunHeading = currentState.gunHeading;
        alive = currentState.alive;

        speed = abs(currentState.velocity);

        acceleration = calculateAcceleration(prevState, currentState);
        if (currentState.velocity == 0) {
            movementDirection = NaN;
        } else if (currentState.velocity > 0) {
            movementDirection = currentState.heading;
        } else {
            movementDirection = Utils.normalAbsoluteAngle(currentState.heading + LxxConstants.RADIANS_180);
        }

        double expectedEnergy = prevState.energy;
        final boolean isHitWall = isHitWall(prevState, currentState);
        if (currentState.energy != prevState.energy) {
            expectedEnergy += currentState.returnedEnergy - currentState.receivedDmg;
            if (isHitWall) {
                expectedEnergy -= currentState.wallDmg != null
                        ? currentState.wallDmg
                        : Rules.getWallHitDamage(LxxUtils.limit(0, prevState.speed + prevState.acceleration, Rules.MAX_VELOCITY));
            }
            if (currentState.hitRobot) {
                expectedEnergy -= LxxConstants.ROBOT_HIT_DAMAGE;
            }
        }

        final boolean canFire = prevState.gunHeat - rules.gunCoolingRate * (currentState.time - prevState.time) <= 0 && prevState.alive;
        final boolean firedForSure = energy < prevState.energy && currentState.time == prevState.time + 1 &&
                currentState.receivedDmg == 0 && currentState.returnedEnergy == 0 && !isHitWall && !currentState.hitRobot;
        if (firedForSure || (canFire && energy < expectedEnergy)) {
            firePower = LxxUtils.limit(0.1, expectedEnergy - energy, Rules.MAX_BULLET_POWER);
            assert isHitWall || firePower > 0 && firePower <= 3 : firePower;
            gunHeat = Rules.getGunHeat(firePower) - rules.gunCoolingRate;
        } else {
            firePower = 0;
            if (alive) {
                gunHeat = max(0, prevState.gunHeat - rules.gunCoolingRate * (currentState.time - prevState.time)); // TODO (azhidkov): add tests
            } else {
                gunHeat = prevState.gunHeat;
            }
        }

        hitBullets = Collections.unmodifiableList(currentState.hitBullets);
        interceptedBullets = Collections.unmodifiableList(currentState.interceptedBullets);
        name = currentState.name;

        assert gunHeat >= 0 && gunHeat <= rules.initialGunHeat : gunHeat;
        assert gunHeat < prevState.gunHeat || prevState.gunHeat < rules.gunCoolingRate || !alive || prevState.time != currentState.time - 1;
    }

    public LxxRobot(LxxRobot original, double turnRate, double desiredVelocity) {
        assert turnRate >= -Rules.getTurnRateRadians(original.speed) &&
                turnRate <= Rules.getTurnRateRadians(original.speed)
                : turnRate + ":" + original.speed;

        velocity = getNewVelocity(original.velocity, desiredVelocity);
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
        hitBullets = null;
        interceptedBullets = null;
        rules = null;
        name = null;
    }

    private static double getNewVelocity(double currentVelocity, double desiredVelocity) {
        if (currentVelocity == 0 || signum(currentVelocity) == signum(desiredVelocity)) {
            final double desiredAcceleration = abs(desiredVelocity) - abs(currentVelocity);
            return LxxUtils.limit(-Rules.MAX_VELOCITY,
                    currentVelocity + LxxUtils.limit(-Rules.DECELERATION, desiredAcceleration, Rules.ACCELERATION) * signum(desiredVelocity),
                    Rules.MAX_VELOCITY);
        } else if (abs(currentVelocity) >= Rules.DECELERATION) {
            return (currentVelocity - Rules.DECELERATION * (signum(currentVelocity)));
        } else {
            final double acceleration = 1 - abs(currentVelocity) / Rules.DECELERATION;
            return acceleration * signum(desiredVelocity);
        }
    }

    @Override
    public double x() {
        return position.x;
    }

    @Override
    public double y() {
        return position.y;
    }

    @Override
    public double aDistance(APoint p) {
        return position.aDistance(p);
    }

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

    @Override
    public String toString() {
        return "LxxRobot " + name + "{" +
                "\n    position=" + position +
                "\n    velocity=" + velocity +
                "\n    heading=" + heading +
                "\n    energy=" + energy +
                "\n    lastScanTime=" + lastScanTime +
                "\n    time=" + time +
                "\n    radarHeading=" + radarHeading +
                "\n    gunHeading=" + gunHeading +
                "\n    alive=" + alive +
                "\n    firePower=" + firePower +
                "\n    gunHeat=" + gunHeat +
                "\n    speed=" + speed +
                "\n    acceleration=" + acceleration +
                "\n    movementDirection=" + movementDirection +
                "\n    hitBullets=" + hitBullets +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LxxRobot lxxRobot = (LxxRobot) o;

        if (round != lxxRobot.round) return false;
        if (time != lxxRobot.time) return false;
        if (name != null ? !name.equals(lxxRobot.name) : lxxRobot.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + round;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    private static double calculateAcceleration(LxxRobot prevState, LxxRobotInfo curState) {
        if (prevState == null) {
            return 0;
        }

        double acceleration;
        if (signum(curState.velocity) == signum(prevState.velocity) || abs(curState.velocity) < 0.001) {
            acceleration = abs(curState.velocity) - abs(prevState.velocity);
        } else {
            acceleration = abs(curState.velocity);
        }

        if (acceleration < -Rules.DECELERATION || acceleration > Rules.ACCELERATION) {
            if (!isHitWall(prevState, curState)) {
                acceleration = LxxUtils.limit(Rules.DECELERATION, acceleration, Rules.ACCELERATION);
                if (prevState.lastScanTime + 1 == curState.time) {
                    assert acceleration >= -Rules.DECELERATION && acceleration <= Rules.ACCELERATION;
                }
            }
        }

        return acceleration;
    }

    private static boolean isHitWall(LxxRobot prevState, LxxRobotInfo currentState) {
        if (prevState.position == null) {
            return false;
        }

        if (currentState.time - prevState.time > 1) {
            return false;
        }

        if (abs(prevState.velocity) - abs(currentState.velocity) > Rules.DECELERATION) {
            return true;
        }

        return prevState.position.aDistance(currentState.position) - currentState.velocity < -1.1 &&
                !prevState.rules.field.contains(currentState.position);
    }

    public double getTurnsToGunCool() {
        return gunHeat / rules.gunCoolingRate;
    }
}
