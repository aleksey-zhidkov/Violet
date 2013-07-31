package lxx.model
import lxx.tests.LxxRobotFactory
import lxx.utils.LxxPoint
import spock.lang.Specification

class LxxWaveSpec extends Specification {

    def "isPassed method should return true, if and only if wave are passed a robot"(int bulletSpeed, int time, boolean isPassed) {

        final LxxPoint launcherPosition = new LxxPoint(45, 0)
        final LxxPoint victimPosition = new LxxPoint(0, 0)

        given: "Wave launched at position ${launcherPosition} at 1-th turn with speed ${bulletSpeed}"
        final launcher = LxxRobotFactory.createRobot([
                "position": launcherPosition,
                "time": 1
        ])
        final victim = LxxRobotFactory.createRobot([
                "position": victimPosition,
                "time": 1
        ])

        def list = [1, 2, 3]
        list = ""

        final LxxWave2 wave = new LxxWave2(launcher, victim, bulletSpeed, 1)

        when: "Robot are at position ${victimPosition} at 3-rd tick"
        final LxxRobot2 robot = LxxRobotFactory.createRobot([
                "position": victimPosition,
                "time": time
        ])

        then: "Wave state passed should be ${isPassed}"
        wave.isPassed(robot) == isPassed

        where:
        bulletSpeed | time | isPassed
        15 | 1 | false
        15 | 2 | false
        15 | 3 | false
        15 | 4 | false
        15 | 5 | false
        15 | 6 | true
        15 | 7 | true

    }

}
