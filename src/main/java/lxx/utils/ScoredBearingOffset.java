package lxx.utils;

import static java.lang.Math.signum;

public class ScoredBearingOffset implements Comparable<ScoredBearingOffset> {

    public final double bearingOffset;
    public final double score;

    public ScoredBearingOffset(double bearingOffset, double score) {
        this.bearingOffset = bearingOffset;
        this.score = score;
    }

    public int compareTo(ScoredBearingOffset o) {
        return (int) signum(bearingOffset - o.bearingOffset);
    }

}