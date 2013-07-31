package lxx.logs;

import lxx.model.LxxRobot;

public interface LocationFactory {

    int getDimensionCount();

    double[] getLocation(LxxRobot observer, LxxRobot observable);

}
