package lxx.utils;

import static java.lang.StrictMath.abs;

public class GuessFactor {

    public final double guessFactor;

    public GuessFactor(double bearingOffset, double mae, double lateralDirection) {
        assert !Double.isNaN(lateralDirection);

        guessFactor = bearingOffset / mae * lateralDirection;

        assert !Double.isNaN(guessFactor);
        assert abs(guessFactor) <= 1;
    }

    public double getBearingOffset(double mae, double lateralDirection) {
        assert !Double.isNaN(lateralDirection);

        return guessFactor * mae * lateralDirection;
    }

}
