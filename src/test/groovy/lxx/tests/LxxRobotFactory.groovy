package lxx.tests

import lxx.model.LxxRobot
import lxx.model.LxxRobotInfo
import lxx.utils.LxxPoint
import robocode.Bullet
import robocode.Rules

import static java.lang.Math.random
import static lxx.tests.TestConstants.stdDuelBattleRules

class LxxRobotFactory {

    private static LxxRobot original = new LxxRobot(stdDuelBattleRules, "")

    def static createMockRobot(long time, Map<String, Object> params) {
        def prevState = new LxxRobot(original, time)
        def info = new LxxRobotInfo()
        info.energy = params.get('energy', 0d) as Double
        info.position = params.get('position', null) as LxxPoint
        info.alive = params.get('alive', false) as Boolean
        info.gunHeading = params.get('gunHeading', null) as Double
        info.time = params.get('time', time + 1) as Long
        new LxxRobot(prevState, info)
    }

    def static createMockRobot(LxxRobot original, Map<String, Object> params) {
        def info = new LxxRobotInfo()
        info.energy = params.get('energy', 0d) as Double
        info.time = original.time + 1
        info.position = params.get('position', null) as LxxPoint
        if (params['hitBullets'] != null) {
            info.hitBullets.addAll(params['hitBullets'] as List<Bullet>)
        }
        info.alive = params.get('alive', false) as Boolean
        new LxxRobot(original, info)
    }

    static LxxRobot generateRandomMockRobot() {
        def prevState = new LxxRobot(original, original.time + 1)
        def info = new LxxRobotInfo()
        info.energy = random() * 100
        info.position = new LxxPoint(random() * stdDuelBattleRules.field.width, random() * stdDuelBattleRules.field.height)
        info.velocity = random() * Rules.MAX_VELOCITY - Rules.MAX_VELOCITY
        TestUtils.setFinal(prevState, LxxRobot.getField('velocity'), random() * Rules.MAX_VELOCITY)
        def acceleration = (Rules.ACCELERATION + Rules.DECELERATION) * random() - Rules.DECELERATION
        info.velocity = prevState.velocity + acceleration
        new LxxRobot(prevState, info)
    }

}
