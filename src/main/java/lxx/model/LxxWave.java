package lxx.model;

import lxx.utils.APoint;
import lxx.utils.LxxPoint;
import lxx.utils.LxxUtils;
import lxx.utils.Vector2D;
import robocode.util.Utils;

import static java.lang.Math.abs;

public class LxxWave implements APoint {

    public final LxxRobot launcher;
    public final LxxRobot victim;
    public final double noBearingOffset;
    public final double speed;
    public final long time;
    public final boolean imaginary;

    public LxxWave(LxxRobot launcher, LxxRobot victim, double speed, long time, boolean imaginary) {
        this.launcher = launcher;
        this.victim = victim;
        this.imaginary = imaginary;
        this.noBearingOffset = launcher.angleTo(victim);
        this.speed = speed;
        this.time = time;
    }

    public double getTraveledDistance(long time) {
        return speed * (time - this.time);
    }

    @Override
    public double x() {
        return launcher.x();
    }

    @Override
    public double y() {
        return launcher.y();
    }

    @Override
    public double angleTo(APoint pnt) {
        return launcher.angleTo(pnt);
    }

    @Override
    public APoint project(double alpha, double distance) {
        return launcher.project(alpha, distance);
    }

    @Override
    public APoint project(Vector2D dv) {
        return launcher.project(dv);
    }

    @Override
    public double distance(APoint to) {
        return launcher.distance(to);
    }

    @Override
    public double distance(double x, double y) {
        return launcher.distance(x, y);
    }

    public double getFlightTime(LxxPoint pnt, long time) {
        return (distance(pnt) - getTraveledDistance(time)) / speed;
    }

    public double getFlightTime(LxxRobot robot) {
        return getFlightTime(robot.position, robot.time);
    }

    public double getBearingOffset(APoint pnt) {
        final double bo = Utils.normalRelativeAngle(launcher.angleTo(pnt) - noBearingOffset);
        if (abs(bo) > LxxUtils.getMaxEscapeAngle(speed)) {
            assert abs(bo) <= LxxUtils.getMaxEscapeAngle(speed);
        }
        return bo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LxxWave wave = (LxxWave) o;

        if (imaginary != wave.imaginary) return false;
        if (time != wave.time) return false;
        if (!launcher.equals(wave.launcher)) return false;
        if (!victim.equals(wave.victim)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = launcher.hashCode();
        result = 31 * result + victim.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (imaginary ? 1 : 0);
        return result;
    }

    public boolean isPassed(LxxRobot robot) {
        final double traveledDistance = getTraveledDistance(robot.time);
        return traveledDistance > launcher.distance(robot) &&
                !LxxUtils.getBoundingRectangleAt(robot).contains((LxxPoint) launcher.project(launcher.angleTo(robot), traveledDistance));
    }

    public boolean isPassed(LxxPoint pos, long time) {
        final double traveledDistance = getTraveledDistance(time);
        return traveledDistance > launcher.distance(pos) &&
                !LxxUtils.getBoundingRectangleAt(pos).contains((LxxPoint) launcher.project(launcher.angleTo(pos), traveledDistance));
    }

}
