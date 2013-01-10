package lxx.tests

import lxx.model.LxxRobot
import lxx.model.LxxRobotInfo
import lxx.utils.LxxPoint
import robocode.Bullet

class LxxRobotFactory {

    private static LxxRobot original = new LxxRobot(TestConstants.stdDuelBattleRules, "")

    def static createMockLxxRobot(long time, Map<String, Object> params) {
        def prevState = new LxxRobot(original, time)
        def info = new LxxRobotInfo()
        info.energy = params.get('energy', 0d) as Double
        info.time = time + 1
        info.position = params.get('position', null) as LxxPoint
        info.alive = params.get('alive', false) as Boolean
        info.gunHeading = params.get('gunHeading', null) as Double
        info.time = params.get('time', 0) as Long
        new LxxRobot(prevState, info)
    }

    def static createMockLxxRobot(LxxRobot original, Map<String, Object> params) {
        def info = new LxxRobotInfo()
        info.energy = params.get('energy', 0d) as Double
        info.time = original.time + 1
        info.position = params.get('position', null) as LxxPoint
        if (params['bullets'] != null) {
            info.bullets.addAll(params['bullets'] as List<Bullet>)
        }
        info.alive = params.get('alive', false) as Boolean
        new LxxRobot(original, info)
    }

}
