package lxx.model

import lxx.tests.LxxRobotFactory;
import lxx.tests.RobotStatusFactory;
import lxx.tests.TestConstants
import lxx.tests.TestUtils;
import lxx.utils.BattleRules
import lxx.utils.LxxConstants;
import lxx.utils.LxxPoint
import lxx.utils.LxxUtils
import org.junit.Test
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.Event
import robocode.Rules;

import static org.junit.Assert.assertEquals;

public class BattleStateTest {

    @Test
    public void testBulletHitBulletHandling() {
        BattleRules rules = TestConstants.stdDuelBattleRules;
        LxxRobot me = new LxxRobot(rules, "me");
        LxxRobotInfo newState = new LxxRobotInfo();
        newState.position = new LxxPoint(10, 10);
        newState.time = 30;
        newState.energy = me.energy;
        newState.name = "enemy";
        newState.alive = true
        me = new LxxRobot(me, newState);
        LxxRobot enemy = new LxxRobot(TestConstants.stdDuelBattleRules, "enemy");
        newState.position = new LxxPoint(100, 100);
        newState.alive = true
        enemy = new LxxRobot(enemy, newState);

        BattleState bs = new BattleState(rules, me, enemy);

        final double bulletPower = 1;
        final double bulletSpeed = Rules.getBulletSpeed(bulletPower);
        newState.energy = me.energy - bulletPower;
        final LxxPoint firePos = new LxxPoint(10, 10);
        newState.position = firePos;
        newState.time = 31;
        newState.alive = true;
        bs = new BattleState(bs, new LxxRobot(me, newState), new LxxRobot(enemy, enemy.time + 1));

        assertEquals(1, bs.getMyBulletsInAir().size());

        LinkedList<Event> allEvents = new LinkedList<Event>();
        Bullet myBullet = new Bullet(0, firePos.x + bulletSpeed, 10, 1, "", "", false, 0);
        Bullet enemyBullet = new Bullet(0d, 0d, 0d, 0.1, "enemy", "me", false, 0);
        allEvents.add(new BulletHitBulletEvent(myBullet, enemyBullet));
        TestUtils.setFinal(bs.enemyBullets, BattleState.WavesState.getField('inAir'), [new LxxWave(enemy, me, Rules.getBulletSpeed(0.1), me.time - 7)])
        bs = BattleStateFactory.updateState(rules, bs, RobotStatusFactory.createRobotStatus([
                time: 32L,
                energy: 100
        ]), allEvents, null);

        assertEquals(0, bs.getMyBulletsInAir().size());
    }

    @Test
    public void testHitByDeathEnemy() {
        long timeToCoolGun = LxxConstants.INITIAL_GUN_HEAT / TestConstants.stdDuelBattleRules.gunCoolingRate + 1
        LxxRobot me = LxxRobotFactory.createMockRobot(timeToCoolGun,
                ['energy': 100, 'position': new LxxPoint(100, 100), 'alive': true])
        LxxRobot enemy = LxxRobotFactory.createMockRobot(timeToCoolGun,
                ['energy': 100, 'position': new LxxPoint(10, 10), 'alive': true])
        def battleState1 = new BattleState(TestConstants.stdDuelBattleRules, me, enemy)
        me = LxxRobotFactory.createMockRobot(me, ['alive': true, 'energy': 99, 'position': new LxxPoint(100, 100)])
        def battleState2 = new BattleState(battleState1, me, enemy)
        TestUtils.setFinal(battleState2.myBullets, BattleState.WavesState.getField('inAir'), [new LxxWave(me, enemy, Rules.getBulletSpeed(0.1), me.time - 7)])
        assertEquals(1, battleState2.myBulletsInAir.size())
        me = LxxRobotFactory.createMockRobot(me, [
                'energy': 99,
                'hitBullets': [new Bullet(0, 0, 0, 0, "", "", true, -1)],
                'position': new LxxPoint(100, 100)
        ])
        enemy = LxxRobotFactory.createMockRobot(timeToCoolGun,
                ['energy': 100, 'position': new LxxPoint(10, 10)])
        final battleState3 = new BattleState(battleState2, me, enemy)
    }

}
