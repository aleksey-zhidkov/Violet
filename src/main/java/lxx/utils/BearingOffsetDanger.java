package lxx.utils;

import static java.lang.Math.signum;

public class BearingOffsetDanger implements Comparable<BearingOffsetDanger> {

    public final double bearingOffset;
    public final double danger;

    public BearingOffsetDanger(double bearingOffset, double danger) {
        this.bearingOffset = bearingOffset;
        this.danger = danger;
    }

    public int compareTo(BearingOffsetDanger o) {
        return (int) signum(bearingOffset - o.bearingOffset);
    }

}