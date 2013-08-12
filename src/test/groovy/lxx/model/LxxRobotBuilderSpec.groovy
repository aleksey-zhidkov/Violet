package lxx.model

import lxx.TestUtils
import lxx.utils.LxxConstants
import lxx.utils.LxxPoint
import lxx.utils.func.Option
import spock.lang.Specification

import static lxx.TestConstants.stdRules

class LxxRobotBuilderSpec extends Specification {

    def "Just found opponent should have correct gun heat"() {

        given:
        int elapsedTime = 5
        def builder = new LxxRobotBuilder(stdRules, Option.none(), elapsedTime, 1)

        when:
        def robot = builder.build()

        then:
        robot.gunHeat == stdRules.initialGunHeat - stdRules.gunCoolingRate * elapsedTime

    }

    def "Opponent wall collisions should be correctly processed"() {

        final int velocity = 8
        given:
        LxxRobot prev = TestUtils.createRobot([
                'position': new LxxPoint(21.39, 213.28),
                'heading': LxxConstants.RADIANS_180,
                'velocity': velocity
        ])

        def builder = new LxxRobotBuilder(stdRules, Option.of(prev), 1, 1)

        when:
        builder.position(new LxxPoint(18, 213.28))
        builder.velocity(0)
        def robot = builder.build()

        then:
        robot.acceleration == -velocity

    }

}
