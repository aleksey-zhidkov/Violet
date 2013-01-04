package lxx.utils;

public interface APoint {

    double x();

    double y();

    double aDistance(APoint p);

    double angleTo(APoint pnt);

    APoint project(double alpha, double distance);

    APoint project(Vector2D dv);

    double distance(APoint to);

    double distance(double x, double y);
}
