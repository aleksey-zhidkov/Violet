package lxx.model

import lxx.tests.LxxRobotFactory
import lxx.tests.TestConstants
import lxx.utils.LxxPoint
import lxx.utils.func.Option
import robocode.Rules
import spock.lang.Specification

class LxxRobotBuilderSpec extends Specification {

    def "Skipped scans handling"(int initialVelocity, int nextVelocity) {

        given: "LxxRobot with skipped scan and speed ${initialVelocity}"
        final LxxRobot2 prevState = LxxRobotFactory.createRobot([
                "velocity": initialVelocity,
                "time": 2,
                "lastScanTime": 1
        ])

        when: "New robot are builded with velocity ${nextVelocity}"
        final currentTime = 3
        final LxxRobotBuilder builder = new LxxRobotBuilder(TestConstants.stdDuelBattleRules, Option.of(prevState), currentTime, 0)
        builder.lastScanTime(currentTime)
        builder.velocity(nextVelocity)

        then: "New robot acceleration should be ${-Rules.DECELERATION}"
        builder.build().acceleration == -Rules.DECELERATION

        where:
        initialVelocity | nextVelocity
        8               | 4
        -8              | -4
    }

    def "Hit wall acceleration handling"(int initialX, int initialY, int afterHitX, int afterHitY) {

        given: "LxxRobot with speed 8 at (${initialX}, ${initialY})"
        final LxxRobot2 prevState = LxxRobotFactory.createRobot([
                "velocity": 8D,
                "time": 2,
                "lastScanTime": 2,
                "position": new LxxPoint(initialX, initialY)
        ])

        when: "New robot are builded after hit wall with position at (${afterHitX}, ${afterHitY})"
        final LxxRobotBuilder builder = new LxxRobotBuilder(TestConstants.stdDuelBattleRules, Option.of(prevState), 3, 0)
        builder.velocity(0)
        builder.position(new LxxPoint(afterHitX, afterHitY))

        then: "Accelearion should be ${-Rules.MAX_VELOCITY}"
        builder.build().acceleration == -Rules.MAX_VELOCITY

        where:
        initialX | initialY | afterHitX | afterHitY
        534      | 20       | 534       | 18
        534      | 580      | 534       | 582
        20       | 534      | 18        | 534
        780      | 534      | 782       | 534
    }

}
