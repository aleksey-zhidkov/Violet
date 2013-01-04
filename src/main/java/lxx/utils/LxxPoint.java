package lxx.utils;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Math.abs;

public class LxxPoint extends Point2D.Double implements APoint, Serializable {

    private static final NumberFormat format = new DecimalFormat();

    static {
        format.setMaximumFractionDigits(2);
    }

    public LxxPoint(double x, double y) {
        super(x, y);
    }

    public LxxPoint(LxxPoint point) {
        super(point.x, point.y);
    }

    public LxxPoint() {
        super();
    }

    public LxxPoint(APoint original) {
        this(original.x(), original.y());
    }

    // TODO (azhidkov): fix me
    public double aDistance(APoint p) {
        return distance(p.x(), p.y());
    }

    public double distance(APoint pnt) {
        return distance(pnt.x(), pnt.y());
    }

    public double aDistance(LxxPoint p) {
        return distance(p.x, p.y);
    }

    public double aDistanceSq(LxxPoint p) {
        return distanceSq(p.x, p.y);
    }

    public String toString() {
        return "[" + format.format(x) + ", " + format.format(y) + "]";
    }

    public LxxPoint project(double alpha, double dist) {
        return new LxxPoint(x + QuickMath.sin(alpha) * dist, y + QuickMath.cos(alpha) * dist);
    }

    public double angleTo(APoint another) {
        return LxxUtils.angle(this, another);
    }

    public APoint project(Vector2D result) {
        return project(result.getAlphaRadians(), result.getLength());
    }

    public double distanceToWall(BattleField battleField, double direction) {
        return distanceToWall(this, battleField, direction);
    }

    public static double distanceToWall(LxxPoint pnt, BattleField battleField, double direction) {
        final BattleField.Wall w = battleField.getWall(pnt, direction);
        return battleField.getDistanceToWall(w, pnt) / abs(QuickMath.cos(direction - w.wallType.fromCenterAngle));
    }

    public double x() {
        return super.getX();
    }

    public double y() {
        return super.getY();
    }
}