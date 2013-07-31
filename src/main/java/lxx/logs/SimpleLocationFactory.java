package lxx.logs;

import lxx.model.LxxRobot2;
import lxx.utils.LxxConstants;
import lxx.utils.LxxUtils;
import robocode.Rules;
import robocode.util.Utils;

public class SimpleLocationFactory implements LocationFactory {
    @Override
    public int getDimensionCount() {
        return 6;
    }

    @Override
    public double[] getLocation(LxxRobot2 observer, LxxRobot2 observable) {
        return new double[]{
                observer.distance(observable) / observer.rules.field.fieldDiagonal,
                LxxUtils.lateralVelocity(observer, observable) / Rules.MAX_VELOCITY,
                LxxUtils.advancingVelocity(observer, observable) / Rules.MAX_VELOCITY,
                observable.acceleration / 3,
                (Double.isNaN(observable.movementDirection)
                        ? observable.position.distanceToWall(observable.rules.field, observable.heading)
                        : observable.position.distanceToWall(observable.rules.field, observable.movementDirection)) / observer.rules.field.fieldDiagonal,
                (Double.isNaN(observable.movementDirection)
                        ? observable.position.distanceToWall(observable.rules.field, Utils.normalAbsoluteAngle(observable.heading + LxxConstants.RADIANS_180))
                        : observable.position.distanceToWall(observable.rules.field, Utils.normalAbsoluteAngle(observable.movementDirection + LxxConstants.RADIANS_180))) /
                        observer.rules.field.fieldDiagonal
        };
    }
}
