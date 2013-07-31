package lxx.logs;

import lxx.model.LxxRobot2;

public interface LocationFactory {

    int getDimensionCount();

    double[] getLocation(LxxRobot2 observer, LxxRobot2 observable);

}
