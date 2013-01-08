package lxx.model

import lxx.tests.LxxRobotFactory;
import lxx.tests.RobotStatusFactory;
import lxx.tests.TestConstants;
import lxx.utils.BattleRules
import lxx.utils.LxxConstants;
import lxx.utils.LxxPoint
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

        assertEquals(1, bs.getMyBullets().size());

        LinkedList<Event> allEvents = new LinkedList<Event>();
        Bullet myBullet = new Bullet(0, firePos.x + bulletSpeed, 10, 0, "", "", false, 0);
        Bullet enemyBullet = new Bullet(0d, 0d, 0d, 0d, "enemy", "me", false, 0);
        allEvents.add(new BulletHitBulletEvent(myBullet, enemyBullet));
        bs = BattleStateFactory.updateState(rules, bs, RobotStatusFactory.createRobotStatus(new HashMap<String, Object>() {
            {
                put("time", 31L);
            }
        }), allEvents, null);

        assertEquals(0, bs.getMyBullets().size());
    }

    @Test
    public void testHitByDeathEnemy() {
        long timeToCoolGun = LxxConstants.INITIAL_GUN_HEAT / TestConstants.stdDuelBattleRules.gunCoolingRate + 1
        LxxRobot me = LxxRobotFactory.createMockLxxRobot(timeToCoolGun,
                ['energy': 100, 'position': new LxxPoint(100, 100), 'alive': true])
        LxxRobot enemy = LxxRobotFactory.createMockLxxRobot(timeToCoolGun,
                ['energy': 100, 'position': new LxxPoint(10, 10), 'alive': true])
        def battleState1 = new BattleState(TestConstants.stdDuelBattleRules, me, enemy)
        me = LxxRobotFactory.createMockLxxRobot(me, ['alive': true, 'energy': 99, 'position': new LxxPoint(100, 100)])
        def battleState2 = new BattleState(battleState1, me, enemy)
        assertEquals(1, battleState2.myBullets.size())
        me = LxxRobotFactory.createMockLxxRobot(me,
                ['energy': 99, 'bullets': [new Bullet(0, 0, 0, 0, "", "", true, -1)]])
        enemy = LxxRobotFactory.createMockLxxRobot(timeToCoolGun,
                ['energy': 100])
        final battleState3 = new BattleState(battleState2, me, enemy)
    }

}
