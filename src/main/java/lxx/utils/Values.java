package lxx.utils;

import static java.lang.Math.max;
import static java.lang.StrictMath.min;

public class Values {

    private final AvgValue avgValue;
    private double maxValue = Long.MIN_VALUE;
    private double minValue = Long.MAX_VALUE;
    private double total;

    public Values(int deph) {
        avgValue = new AvgValue(deph);
    }

    public void addValue(double value) {
        maxValue = max(maxValue, value);
        minValue = min(minValue, value);
        avgValue.addValue(value);
        total += value;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public AvgValue getAvgValue() {
        return avgValue;
    }

    @Override
    public String toString() {
        if (maxValue == Long.MIN_VALUE) {
            return "[ No Data ]";
        } else if (maxValue == minValue) {
            return String.format("[ %,14.0f ]", minValue);
        } else {
            return String.format("[ %,9.0f | %,9.0f | %,14.0f | %,20.0f]", minValue, avgValue.getCurrentValue(), maxValue, total);
        }
    }
}
