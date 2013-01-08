package lxx.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class IntervalDouble implements Comparable<IntervalDouble> {

    public final double a;
    public final double b;

    public IntervalDouble() {
        this.a = Long.MAX_VALUE;
        this.b = Long.MIN_VALUE;
    }

    public IntervalDouble(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public IntervalDouble(IntervalDouble ival) {
        this.a = ival.a;
        this.b = ival.b;
    }

    public double getLength() {
        return b - a;
    }

    public String toString() {
        return "[" + a + ", " + b + "]";
    }

    public double center() {
        return (a + b) / 2;
    }

    public boolean contains(double x) {
        return a <= x && b >= x;
    }

    public IntervalDouble extend(double x) {
        double a = this.a;
        double b = this.b;
        if (a > x) {
            a = x;
        }
        if (b < x) {
            b = x;
        }

        return new IntervalDouble(a, b);
    }

    public boolean intersects(IntervalDouble another) {
        return (a <= another.a && b >= another.a) ||
                (another.a <= a && another.b >= a);
    }

    public double intersection(IntervalDouble another) {
        return min(b, another.b) - max(a, another.a);
    }

    public IntervalDouble merge(double a, double b) {
        return new IntervalDouble(min(a, this.a), max(b, this.b));
    }

    public int compareTo(IntervalDouble another) {
        return a < another.a ? -1 : a == another.a ? 0 : 1;
    }

}
