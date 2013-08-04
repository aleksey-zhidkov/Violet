package lxx.utils;

import lxx.model.LxxRobot;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;

public final class LxxUtils {

    private static final double ROBOT_SQUARE_DIAGONAL = LxxConstants.ROBOT_SIDE_SIZE * sqrt(2);
    private static final double HALF_PI = Math.PI / 2;
    private static final double DOUBLE_PI = Math.PI * 2;

    private LxxUtils() {
    }

    public static double angle(double baseX, double baseY, double x, double y) {
        double theta = QuickMath.asin((y - baseY) / LxxPoint.distance(x, y, baseX, baseY)) - HALF_PI;
        if (x >= baseX && theta < 0) {
            theta = -theta;
        }
        theta = theta % DOUBLE_PI;
        return (theta) >= 0 ? theta : (theta + DOUBLE_PI);
    }

    public static double angle(APoint p1, APoint p2) {
        return angle(p1.x(), p1.y(), p2.x(), p2.y());
    }

    public static double anglesDiff(double alpha1, double alpha2) {
        return abs(Utils.normalRelativeAngle(alpha1 - alpha2));
    }

    public static double limit(double minValue, double value, double maxValue) {
        if (value < minValue) {
            return minValue;
        }

        if (value > maxValue) {
            return maxValue;
        }

        return value;
    }

    public static double lateralDirection(APoint center, LxxRobot robot) {
        return lateralDirection(center, robot, robot.velocity, robot.heading);
    }

    private static double lateralDirection(APoint center, APoint pos, double velocity, double heading) {
        assert !Double.isNaN(heading);
        if (Utils.isNear(0, velocity)) {
            return 1;
        }
        return signum(lateralVelocity(center, pos, velocity, heading));
    }

    public static double lateralVelocity(APoint center, LxxRobot robot) {
        return lateralVelocity(center, robot, robot.velocity, robot.heading);
    }

    public static double lateralVelocity(APoint center, APoint pos, double velocity, double heading) {
        assert !Double.isNaN(heading);
        assert heading >= 0 && heading <= LxxConstants.RADIANS_360;
        return velocity * QuickMath.sin(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
    }

    public static double advancingVelocity(APoint center, LxxRobot robot) {
        return advancingVelocity(center, robot, robot.velocity, robot.heading);
    }

    public static double advancingVelocity(APoint center, APoint pos, double velocity, double heading) {
        assert !Double.isNaN(heading);
        assert heading >= 0 && heading <= LxxConstants.RADIANS_360;
        return velocity * QuickMath.cos(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
    }

    public static double getReturnedEnergy(double bulletPower) {
        return 3 * bulletPower;
    }

    public static Rectangle2D getBoundingRectangleAt(APoint point) {
        return getBoundingRectangleAt(point, LxxConstants.ROBOT_SIDE_HALF_SIZE);
    }

    public static Rectangle2D getBoundingRectangleAt(APoint point, final int sideHalfSize) {
        return new Rectangle.Double(point.x() - sideHalfSize, point.y() - sideHalfSize,
                sideHalfSize * 2, sideHalfSize * 2);
    }

    public static double getRobotWidthInRadians(APoint center, APoint robotPos) {
        return getRobotWidthInRadians(angle(center, robotPos), center.distance(robotPos));
    }

    public static double getRobotWidthInRadians(double angle, double distance) {
        final double alpha = abs(LxxConstants.RADIANS_45 - (angle % LxxConstants.RADIANS_90));
        final double validDistance = distance < ROBOT_SQUARE_DIAGONAL ? ROBOT_SQUARE_DIAGONAL : distance;
        return QuickMath.asin(QuickMath.cos(alpha) * ROBOT_SQUARE_DIAGONAL / validDistance);
    }

    public static double getMaxEscapeAngle(double bulletSpeed) {
        return QuickMath.asin(Rules.MAX_VELOCITY / bulletSpeed) * 1.3;
    }

    public static <T> List<T> List(T ... items) {
        return Arrays.asList(items);
    }

    public static <T> List<T> add(List<T> lst, T item) {
        lst.add(item);
        return lst;
    }

    public static double getNewVelocity(double currentVelocity, double desiredVelocity) {
        if (currentVelocity == 0 || signum(currentVelocity) == signum(desiredVelocity)) {
            final double desiredAcceleration = abs(desiredVelocity) - abs(currentVelocity);
            return limit(-Rules.MAX_VELOCITY,
                    currentVelocity + limit(-Rules.DECELERATION, desiredAcceleration, Rules.ACCELERATION) * signum(desiredVelocity),
                    Rules.MAX_VELOCITY);
        } else if (abs(currentVelocity) >= Rules.DECELERATION) {
            return (currentVelocity - Rules.DECELERATION * (signum(currentVelocity)));
        } else {
            final double acceleration = 1 - abs(currentVelocity) / Rules.DECELERATION;
            return acceleration * signum(desiredVelocity);
        }
    }

    public static boolean isNear(double value1, double value2) {
        assert !Double.isNaN(value1) && !Double.isNaN(value2);
        return abs(value1 - value1) < 0.01;
    }

    public static boolean isValidAcceleration(double acceleration) {
        return acceleration >= -Rules.MAX_VELOCITY - LxxConstants.EPSILON &&
                acceleration <= Rules.ACCELERATION + LxxConstants.EPSILON;
    }

    // turnRate = 10 - 0.75 * speed
    // turnRate - 10 = - 0.75 * speed
    // speed = (10 - turnRate) / 0.75
    public static double getRequiredSpeed(double turnRate) {
        return max(0, (10 - toDegrees(turnRate) / 0.75));
    }
}
