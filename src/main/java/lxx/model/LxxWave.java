package lxx.model;

import lxx.utils.APoint;
import lxx.utils.Vector2D;
import lxx.utils.LxxPoint;

public class LxxWave implements APoint {

    public final LxxRobot launcher;
    public final LxxRobot victim;
    public final double noBearingOffset;
    public final double speed;
    public final long time;

    public LxxWave(LxxRobot launcher, LxxRobot victim, double speed, long time) {
        this.launcher = launcher;
        this.victim = victim;
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
    public double aDistance(APoint p) {
        return launcher.aDistance(p);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LxxWave lxxWave = (LxxWave) o;

        if (time != lxxWave.time) return false;
        if (launcher != null ? !launcher.equals(lxxWave.launcher) : lxxWave.launcher != null) return false;
        if (victim != null ? !victim.equals(lxxWave.victim) : lxxWave.victim != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = launcher != null ? launcher.hashCode() : 0;
        result = 31 * result + (victim != null ? victim.hashCode() : 0);
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }
}
