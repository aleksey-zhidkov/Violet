package lxx.tests

import lxx.model.LxxRobot2
import lxx.utils.LxxPoint
import robocode.ScannedRobotEvent
import robocode.util.Utils

class ScannedRobotEventFactory {

    static createScannedRobotEvent(LxxRobot2 owner, Map params) {
        return new ScannedRobotEvent() {

            @Override
            double getBearingRadians() {
                return Utils.normalRelativeAngle(owner.angleTo(params.get('position', new LxxPoint()) as LxxPoint) - owner.heading)
            }

            @Override
            double getDistance() {
                return owner.aDistance(params.get('position', new LxxPoint()) as LxxPoint)
            }
        }
    }

}
