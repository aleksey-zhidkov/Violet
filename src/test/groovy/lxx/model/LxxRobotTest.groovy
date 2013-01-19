package lxx.model;

import lxx.tests.TestConstants;
import org.junit.Test
import robocode.util.Utils;

import static org.junit.Assert.assertEquals;

public class LxxRobotTest {

    @Test
    public void testGunCooling() {
        LxxRobot me = new LxxRobot(TestConstants.stdDuelBattleRules, "");
        assertEquals(3.0, me.gunHeat, Utils.NEAR_DELTA);

        LxxRobot enemy = new LxxRobot(TestConstants.stdDuelBattleRules, "");
        enemy = new LxxRobot(enemy, 1);
        assertEquals(3.0 - TestConstants.stdDuelBattleRules.gunCoolingRate, enemy.gunHeat, Utils.NEAR_DELTA);

        final LxxRobotInfo currentState = new LxxRobotInfo();
        currentState.time = me.time + 1;
        currentState.alive = true;
        currentState.energy = me.energy;
        me = new LxxRobot(me, currentState);
        assertEquals(3.0 - TestConstants.stdDuelBattleRules.gunCoolingRate, me.gunHeat, Utils.NEAR_DELTA);
    }

    @Test
    public void testGunHeatConstraint() {
        final LxxRobot state1 = new LxxRobot(TestConstants.stdDuelBattleRules, "");

        final LxxRobotInfo currentState2 = new LxxRobotInfo();
        currentState2.energy = 100;
        currentState2.alive = true;
        currentState2.time = state1.time + 1

        final LxxRobot state2 = new LxxRobot(state1, currentState2);

        final LxxRobotInfo currentState3 = new LxxRobotInfo();
        currentState3.energy = 98;
        currentState3.alive = true;
        currentState3.time = state2.time + 1

        final LxxRobot state3 = new LxxRobot(state2, currentState3);

        // todo: assertEquals(0.0, state3.firePower, Utils.NEAR_DELTA);
    }

}
