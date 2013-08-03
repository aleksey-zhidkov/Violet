package lxx.utils;

import static java.lang.Math.signum;

public class ScoredBearingOffset implements Comparable<ScoredBearingOffset> {

    public final double bearingOffset;
    public final double score;

    public ScoredBearingOffset(double bearingOffset, double score) {
        this.bearingOffset = bearingOffset;
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ScoredBearingOffset that = (ScoredBearingOffset) o;

        if (Double.compare(that.bearingOffset, bearingOffset) != 0) return false;
        if (Double.compare(that.score, score) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(bearingOffset);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public int compareTo(ScoredBearingOffset o) {
        if (equals(o)) {
            return 0;
        }
        return (int) signum(bearingOffset - o.bearingOffset);
    }

}
