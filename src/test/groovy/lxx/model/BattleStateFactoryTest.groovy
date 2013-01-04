package lxx.model

import lxx.tests.LxxRobotFactory
import lxx.tests.RobotStatusFactory
import lxx.tests.TestConstants
import lxx.utils.BattleRules
import lxx.utils.LxxConstants
import lxx.utils.LxxPoint
import lxx.utils.LxxUtils
import org.junit.Test
import robocode.Bullet
import robocode.BulletHitEvent
import robocode.DeathEvent
import robocode.Event
import robocode.HitByBulletEvent
import robocode.RobotDeathEvent
import robocode.RobotStatus
import robocode.Rules
import robocode.ScannedRobotEvent
import robocode.util.Utils

import static org.junit.Assert.assertEquals;

class BattleStateFactoryTest {

    private static final double[] bulletPowers = [0.1, 0.5, 0.99, 1, 1.01, 1.55, 1.99, 2, 2.01, 2.5, 2.99, 3];

    @Test
    public void testMyHits() throws InstantiationException, IllegalAccessException {
        for (Double bulletPower : bulletPowers) {
            testMyHit(bulletPower);
        }
    }

    @Test
    public void testEnemyHits() throws InstantiationException, IllegalAccessException {
        for (Double bulletPower : bulletPowers) {
            testEnemyHit(bulletPower);
        }
    }

    @Test
    public void testDeath() {
        LxxRobot enemy = LxxRobotFactory.createMockLxxRobot(0,
                ['energy': 100, 'position': new LxxPoint(10, 10), 'alive': true])
        LxxRobot me = LxxRobotFactory.createMockLxxRobot(0,
                ['energy': 100, 'position': new LxxPoint(100, 100), 'alive': true])
        def battleState1 = new BattleState(TestConstants.stdDuelBattleRules, me, enemy)
        RobotStatus status = RobotStatusFactory.createRobotStatus(['energy': me.energy, 'time': me.time + 1]);
        def state = BattleStateFactory.updateState(TestConstants.stdDuelBattleRules, battleState1, status,
                [new DeathEvent(), new HitByBulletEvent(0, new Bullet(0, 0, 0, 1, 'enemy', 'me', true, 1))],
                null)
        assertEquals(0.0d, state.enemy.firePower, Utils.NEAR_DELTA)
    }

    @Test
    public void testMyAndEnemyHit() {
        long timeToCoolGun = LxxConstants.INITIAL_GUN_HEAT / TestConstants.stdDuelBattleRules.gunCoolingRate + 1
        LxxRobot enemy = LxxRobotFactory.createMockLxxRobot(timeToCoolGun,
                ['energy': 100, 'position': new LxxPoint(10, 10), 'alive': true])
        LxxRobot me = LxxRobotFactory.createMockLxxRobot(timeToCoolGun,
                ['energy': 100, 'position': new LxxPoint(100, 100), 'alive': true])
        def battleState1 = new BattleState(TestConstants.stdDuelBattleRules, me, enemy)
        RobotStatus status = RobotStatusFactory.createRobotStatus(['energy': me.energy, 'time': me.time + 1]);
        def state = BattleStateFactory.updateState(TestConstants.stdDuelBattleRules, battleState1, status,
                [new RobotDeathEvent("enemy"),
                        new HitByBulletEvent(0, new Bullet(0, 0, 0, 1, 'enemy', 'me', true, 1)),
                        new BulletHitEvent('enemy', 0, new Bullet(0, 0, 0, 1, 'me', 'enemy', true, 1))],
                null)
        assertEquals(0.0d, state.enemy.firePower, Utils.NEAR_DELTA)
    }


    public static void testMyHit(final double bulletPower) throws IllegalAccessException, InstantiationException {

        final double myReturnedEnergy = LxxUtils.getReturnedEnergy(bulletPower);
        final double enemyEnergyLoss = Rules.getBulletDamage(bulletPower);

        final BattleRules rules = TestConstants.stdDuelBattleRules;

        final LxxRobot myState = new LxxRobot(rules, "");
        final LxxRobotInfo myCurrentState2 = new LxxRobotInfo();
        myCurrentState2.time = 35;
        myCurrentState2.position = new LxxPoint(0, 0);
        myCurrentState2.energy = 100;
        final LxxRobot me = new LxxRobot(myState, myCurrentState2);

        final LxxRobot enemyState = new LxxRobot(rules, "");
        final LxxRobotInfo enemyCurrentState2 = new LxxRobotInfo();
        enemyCurrentState2.time = 35;
        enemyCurrentState2.energy = 100;
        enemyCurrentState2.position = new LxxPoint(0, 0);
        final LxxRobot enemy = new LxxRobot(enemyState, enemyCurrentState2);

        final BattleState initialState = new BattleState(rules, me, enemy);

        RobotStatus status = RobotStatusFactory.createRobotStatus(['energy': me.energy + myReturnedEnergy, 'time': me.time + 1]);

        final LinkedList<Event> allEvents = new LinkedList<Event>();
        allEvents.add(new ScannedRobotEvent() {

            @Override
            public double getEnergy() {
                return enemy.energy - enemyEnergyLoss;
            }

            @Override
            public long getTime() {
                return enemy.time + 1;
            }
        });
        allEvents.add(new BulletHitEvent(null, enemy.energy - Rules.getBulletDamage(bulletPower), new Bullet(0, 0, 0, bulletPower, null, null, false, -1)));

        final BattleState newState = BattleStateFactory.updateState(rules, initialState, status, allEvents, null);

        assertEquals(0.0, newState.me.firePower, Utils.NEAR_DELTA);
        assertEquals(0.0, newState.enemy.firePower, Utils.NEAR_DELTA);

    }

    public static void testEnemyHit(double bulletPower) throws IllegalAccessException, InstantiationException {

        final double enemyReturnedEnergy = LxxUtils.getReturnedEnergy(bulletPower);
        final double myEnergyLoss = Rules.getBulletDamage(bulletPower);

        final BattleRules rules = TestConstants.stdDuelBattleRules;

        final LxxRobot myState = new LxxRobot(rules, "");
        final LxxRobotInfo myCurrentState2 = new LxxRobotInfo();
        myCurrentState2.time = 35;
        myCurrentState2.energy = 100;
        myCurrentState2.position = new LxxPoint(0, 0);
        final LxxRobot me = new LxxRobot(myState, myCurrentState2);

        final LxxRobot enemyState = new LxxRobot(rules, "");
        final LxxRobotInfo enemyCurrentState2 = new LxxRobotInfo();
        enemyCurrentState2.time = 35;
        enemyCurrentState2.energy = 100;
        enemyCurrentState2.position = new LxxPoint(0, 0);
        final LxxRobot enemy = new LxxRobot(enemyState, enemyCurrentState2);

        final BattleState initialState = new BattleState(rules, me, enemy);

        RobotStatus status = RobotStatusFactory.createRobotStatus(['energy': me.energy + myEnergyLoss, 'time': me.time + 1]);

        final LinkedList<Event> allEvents = new LinkedList<Event>();
        allEvents.add(new ScannedRobotEvent() {

            @Override
            public long getTime() {
                return enemy.time + 1;
            }

            @Override
            public double getEnergy() {
                return enemy.energy + enemyReturnedEnergy;
            }
        });
        allEvents.add(new HitByBulletEvent(0, new Bullet(0, 0, 0, bulletPower, null, null, false, -1)));

        final BattleState newState = BattleStateFactory.updateState(rules, initialState, status, allEvents, null);

        assertEquals(0.0, newState.me.firePower, Utils.NEAR_DELTA);
        assertEquals(0.0, newState.enemy.firePower, Utils.NEAR_DELTA);

    }

}
