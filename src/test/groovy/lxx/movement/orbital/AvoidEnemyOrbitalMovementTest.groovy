package lxx.movement.orbital;

import lxx.model.LxxRobot;
import lxx.model.LxxRobotInfo;
import lxx.movement.MovementDecision;
import lxx.tests.TestConstants;
import lxx.utils.LxxConstants;
import lxx.utils.LxxPoint
import org.junit.Test
import robocode.util.Utils;

import static org.junit.Assert.assertEquals;

public class AvoidEnemyOrbitalMovementTest {

    @Test
    public void testAvoidEnemyOrbitalMovement() {
        final OrbitalMovement om = new OrbitalMovement(TestConstants.stdDuelBattleRules.field, 100);
        final AvoidEnemyOrbitalMovement aeom = new AvoidEnemyOrbitalMovement(om);

        LxxRobot me = new LxxRobot(TestConstants.stdDuelBattleRules, "me");

        LxxRobotInfo myInfo = new LxxRobotInfo();
        myInfo.position = new LxxPoint(200, 200);
        myInfo.heading = LxxConstants.RADIANS_90;
        me = new LxxRobot(me, myInfo);

        LxxRobot enemy = new LxxRobot(TestConstants.stdDuelBattleRules, "enemy");
        LxxRobotInfo enemyInfo = new LxxRobotInfo();
        enemyInfo.position = new LxxPoint(280, 200);
        enemy = new LxxRobot(enemy, enemyInfo);

        MovementDecision movementDecision = aeom.getMovementDecision(me, new LxxPoint(200, 300), OrbitDirection.COUNTER_CLOCKWISE, enemy);
        assertEquals(-0.0D, movementDecision.getDesiredVelocity(), Utils.NEAR_DELTA);
        movementDecision = aeom.getMovementDecision(me, new LxxPoint(200, 300), OrbitDirection.CLOCKWISE, enemy);
        assertEquals(-8.0D, movementDecision.getDesiredVelocity(), Utils.NEAR_DELTA);

        myInfo.heading = LxxConstants.RADIANS_270;
        myInfo.velocity = -1;
        me = new LxxRobot(me, myInfo);

        movementDecision = aeom.getMovementDecision(me, new LxxPoint(200, 300), OrbitDirection.COUNTER_CLOCKWISE, enemy);
        assertEquals(0.0, movementDecision.getDesiredVelocity(), Utils.NEAR_DELTA);
    }

}
