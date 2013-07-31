package lxx.model

import lxx.strategy.TurnDecision
import lxx.tests.LxxRobotFactory
import lxx.tests.RobotStatusFactory
import lxx.tests.ScannedRobotEventFactory
import lxx.tests.TestConstants
import lxx.utils.LxxPoint
import lxx.utils.func.Option
import robocode.Event
import robocode.ScannedRobotEvent
import spock.lang.Specification

class BattleStateFactorySpec extends Specification {

    def "Passed and only passed bullets should be removed from myBulletsInAir"(int time, int myBulletsInAirSize) {

        given: "Battle state with one my bullet in air"
        final LxxRobot2 launcher = LxxRobotFactory.createRobot([
                'position': new LxxPoint(0, 0),
                'heading': 0,
                'time': 0
        ])
        final LxxRobot2 victim = LxxRobotFactory.createRobot([
                'position': new LxxPoint(0, 60),
                'time': 0
        ])
        final BattleState2 bs = new BattleState2(TestConstants.stdDuelBattleRules, 1L, null as BattleState2,
                null as LxxRobot2, null as LxxRobot2,
                null as Option<LxxWave2>, null as Option<LxxWave2>,
                [new LxxWave2(launcher, victim, 15, 0)], [],
                [], [],
                [], [],
                [], [])

        when: "Next battle state are build at time=${time}"
        final ScannedRobotEvent e = ScannedRobotEventFactory.createScannedRobotEvent(launcher, ["position": victim.position])
        final BattleState2 nextState = BattleStateFactory2.updateState(TestConstants.stdDuelBattleRules, bs, RobotStatusFactory.createRobotStatus(["time":time]), [e] as List<Event>, null as TurnDecision)

        then: "Next my bullets in air size should be ${myBulletsInAirSize}"
        nextState.myBulletsInAir.size() == myBulletsInAirSize

        where:
        time | myBulletsInAirSize
        1 | 1
        2 | 1
        3 | 1
        4 | 1
        5 | 0
    }

}
