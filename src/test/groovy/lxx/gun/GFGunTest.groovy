package lxx.gun

import lxx.model.BattleState
import lxx.model.LxxRobot
import lxx.services.GFMovementLogService
import lxx.tests.LxxRobotFactory
import lxx.tests.TestConstants
import lxx.utils.LxxConstants
import lxx.utils.LxxPoint
import lxx.utils.ScoredBearingOffset
import org.junit.Test
import robocode.Rules
import robocode.util.Utils

import static junit.framework.Assert.assertEquals

class GFGunTest {

    @Test
    def void testGFGun() {
        GFGun gfGun = new GFGun(new GFMovementLogService() {
            @Override
            List<ScoredBearingOffset> getVisits(BattleState state, double bulletSpeed) {
                return [new ScoredBearingOffset(LxxConstants.RADIANS_45, 1),
                        new ScoredBearingOffset(LxxConstants.RADIANS_0, 0.1)]
            }
        })

        final enemyPos = new LxxPoint(10, 10)
        LxxRobot enemy = LxxRobotFactory.createMockLxxRobot(0,
                ['energy': 100, 'position': enemyPos, 'alive': true])
        final myPos = new LxxPoint(100, 100)
        LxxRobot me = LxxRobotFactory.createMockLxxRobot(0,
                ['energy': 100,
                        'position': myPos,
                        'alive': true,
                'time': 40,
                'gunHeading': myPos.angleTo(enemyPos)])
        def battleState = new BattleState(TestConstants.stdDuelBattleRules, me, enemy)

        final gunTurnAngle = gfGun.getGunTurnAngle(battleState, Rules.getBulletSpeed(1))
        assertEquals(LxxConstants.RADIANS_45, gunTurnAngle, Utils.NEAR_DELTA)
    }

}
