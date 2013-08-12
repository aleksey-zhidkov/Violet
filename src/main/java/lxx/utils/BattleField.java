package lxx.utils;

import robocode.util.Utils;

import static java.lang.Math.max;

public class BattleField {

    public static final double WALL_STICK = 140;

    public final APoint availableLeftBottom;
    public final APoint availableLeftTop;
    public final APoint availableRightTop;
    public final APoint availableRightBottom;

    public final Wall bottom;
    public final Wall left;
    public final Wall top;
    public final Wall right;

    public final int availableBottomY;
    public final int availableTopY;
    public final int availableLeftX;
    public final int availableRightX;

    public final LxxPoint center;
    public final int fieldDiagonal;

    public final IntervalDouble noSmoothX;
    public final IntervalDouble noSmoothY;

    public final int width;
    public final int height;

    private final LxxPoint leftTop;
    private final LxxPoint rightTop;
    private final LxxPoint rightBottom;

    public BattleField(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        availableBottomY = y;
        availableTopY = y + height;
        availableLeftX = x;
        availableRightX = x + width;

        availableLeftBottom = new LxxPoint(availableLeftX, availableBottomY);
        availableLeftTop = new LxxPoint(availableLeftX, availableTopY);
        availableRightTop = new LxxPoint(availableRightX, availableTopY);
        availableRightBottom = new LxxPoint(availableRightX, availableBottomY);

        final int bottomY = 0;
        final int topY = y * 2 + height;
        final int leftX = 0;
        final int rightX = x * 2 + width;

        leftTop = new LxxPoint(leftX, topY);
        rightTop = new LxxPoint(rightX, topY);
        rightBottom = new LxxPoint(rightX, bottomY);

        bottom = new Wall(WallType.BOTTOM, availableRightBottom, availableLeftBottom);
        left = new Wall(WallType.LEFT, availableLeftBottom, availableLeftTop);
        top = new Wall(WallType.TOP, availableLeftTop, availableRightTop);
        right = new Wall(WallType.RIGHT, availableRightTop, availableRightBottom);
        bottom.clockwiseWall = left;
        bottom.counterClockwiseWall = right;
        left.clockwiseWall = top;
        left.counterClockwiseWall = bottom;
        top.clockwiseWall = right;
        top.counterClockwiseWall = left;
        right.clockwiseWall = bottom;
        right.counterClockwiseWall = top;

        center = new LxxPoint(rightX / 2D, topY / 2D);
        fieldDiagonal = (int) new LxxPoint(x, y).distance(width, height);

        noSmoothX = new IntervalDouble(WALL_STICK, width - WALL_STICK);
        noSmoothY = new IntervalDouble(WALL_STICK, height - WALL_STICK);
    }

    // this method is called very often, so keep it optimal
    public Wall getWall(LxxPoint pos, double heading) {
        final double normalHeadingTg = QuickMath.tan(heading % LxxConstants.RADIANS_90);
        if (heading < LxxConstants.RADIANS_90) {
            final double rightTopTg = (rightTop.x - pos.x) / (rightTop.y - pos.y);
            if (normalHeadingTg < rightTopTg) {
                return top;
            } else {
                return right;
            }
        } else if (heading < LxxConstants.RADIANS_180) {
            final double rightBottomTg = pos.y / (rightBottom.x - pos.x);
            if (normalHeadingTg < rightBottomTg) {
                return right;
            } else {
                return bottom;
            }
        } else if (heading < LxxConstants.RADIANS_270) {
            final double leftBottomTg = pos.x / pos.y;
            if (normalHeadingTg < leftBottomTg) {
                return bottom;
            } else {
                return left;
            }
        } else if (heading < LxxConstants.RADIANS_360) {
            final double leftTopTg = (leftTop.y - pos.y) / pos.x;
            if (normalHeadingTg < leftTopTg) {
                return left;
            } else {
                return top;
            }
        }
        throw new IllegalArgumentException("Invalid heading: " + heading);
    }

    public double getDistanceToWall(Wall wall, LxxPoint pnt) {
        switch (wall.wallType) {
            case TOP:
                return availableTopY - pnt.y;
            case RIGHT:
                return availableRightX - pnt.x;
            case BOTTOM:
                return pnt.y - availableBottomY;
            case LEFT:
                return pnt.x - availableLeftX;
            default:
                throw new IllegalArgumentException("Unknown wallType: " + wall.wallType);
        }
    }

    public double smoothWalls(LxxPoint pnt, double desiredHeading, boolean isClockwise) {
        if (noSmoothX.contains(pnt.x) && noSmoothY.contains(pnt.y)) {
            return desiredHeading;
        }
        return smoothWall(getWall(pnt, desiredHeading), pnt, desiredHeading, isClockwise);
    }

    private double smoothWall(Wall wall, LxxPoint pnt, double desiredHeading, boolean isClockwise) {
        final double adjacentLeg = max(0, getDistanceToWall(wall, pnt) - 4);
        if (WALL_STICK < adjacentLeg) {
            return desiredHeading;
        }
        double smoothAngle;
        smoothAngle = (QuickMath.acos(adjacentLeg / WALL_STICK) + LxxConstants.RADIANS_4) * (isClockwise ? 1 : -1);
        final double baseAngle = wall.wallType.fromCenterAngle;
        final double smoothedAngle = Utils.normalAbsoluteAngle(baseAngle + smoothAngle);
        final Wall secondWall = isClockwise ? wall.clockwiseWall : wall.counterClockwiseWall;
        return smoothWall(secondWall, pnt, smoothedAngle, isClockwise);
    }

    public static enum WallType {

        TOP(LxxConstants.RADIANS_0, LxxConstants.RADIANS_90, LxxConstants.RADIANS_270),
        RIGHT(LxxConstants.RADIANS_90, LxxConstants.RADIANS_180, LxxConstants.RADIANS_0),
        BOTTOM(LxxConstants.RADIANS_180, LxxConstants.RADIANS_270, LxxConstants.RADIANS_90),
        LEFT(LxxConstants.RADIANS_270, LxxConstants.RADIANS_0, LxxConstants.RADIANS_180);
        public final double fromCenterAngle;
        public final double clockwiseAngle;
        public final double counterClockwiseAngle;

        private WallType(double fromCenterAngle, double clockwiseAngle, double counterClockwiseAngle) {
            this.fromCenterAngle = fromCenterAngle;


            this.clockwiseAngle = clockwiseAngle;
            this.counterClockwiseAngle = counterClockwiseAngle;
        }

    }

    public static final class Wall {

        public final WallType wallType;
        public final APoint ccw;
        public final APoint cw;
        private Wall clockwiseWall;
        private Wall counterClockwiseWall;

        private Wall(WallType wallType, APoint ccw, APoint cw) {
            this.wallType = wallType;
            this.ccw = ccw;
            this.cw = cw;
        }
    }
}
