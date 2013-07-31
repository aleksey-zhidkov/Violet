package lxx.model;

import lxx.Violet;
import lxx.strategy.TurnDecision;
import lxx.utils.BattleRules;
import lxx.utils.LxxPoint;
import lxx.utils.LxxUtils;
import lxx.utils.func.Option;
import robocode.*;

import java.util.Collection;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class BattleStateFactory {

    private BattleStateFactory() {
    }

    public static BattleState updateState(BattleRules rules, BattleState battleState,
                                          RobotStatus status, Collection<Event> allEvents,
                                          TurnDecision lastTurnDecision) {
        if (battleState == null) {
            battleState = new BattleState(rules, new LxxRobot(rules, rules.myName), new LxxRobot(rules, LxxRobot.UNKNOWN_ENEMY));
        }

        final LxxRobotInfo myInfo = getMyInfo(status);
        myInfo.name = rules.myName;
        final LxxRobotInfo enemyInfo = getEnemyInfo(battleState.enemy);
        enemyInfo.time = status.getTime();
        enemyInfo.round = status.getRoundNum();

        for (Event e : allEvents) {
            if (e instanceof BulletHitEvent) {
                final BulletHitEvent bhe = (BulletHitEvent) e;
                final double dmg = Rules.getBulletDamage(bhe.getBullet().getPower());
                final double returnedEnergy = LxxUtils.getReturnedEnergy(bhe.getBullet().getPower());
                myInfo.returnedEnergy += returnedEnergy;
                enemyInfo.receivedDmg += dmg;
                enemyInfo.energy = bhe.getEnergy();
                myInfo.hitBullets.add(bhe.getBullet());
                enemyInfo.name = bhe.getBullet().getVictim();
                enemyInfo.alive = enemyInfo.energy > 0;
                enemyInfo.position = new LxxPoint(bhe.getBullet().getX(), bhe.getBullet().getY());
            } else if (e instanceof DeathEvent) {
                myInfo.alive = false;
            } else if (e instanceof HitByBulletEvent) {
                final HitByBulletEvent hbe = (HitByBulletEvent) e;
                final double dmg = Rules.getBulletDamage(hbe.getPower());
                final double returnedEnergy = LxxUtils.getReturnedEnergy(hbe.getPower());
                enemyInfo.returnedEnergy += returnedEnergy;
                myInfo.receivedDmg += dmg;
                enemyInfo.hitBullets.add(hbe.getBullet());
                enemyInfo.name = hbe.getBullet().getName();
            } else if (e instanceof HitRobotEvent) {
                myInfo.hitRobot = true;
                enemyInfo.hitRobot = true;
            } else if (e instanceof HitWallEvent) {
                final double expectedSpeed = getNewVelocity(battleState.me.velocity, lastTurnDecision.desiredVelocity);
                myInfo.wallDmg = Rules.getWallHitDamage(expectedSpeed);
            } else if (e instanceof RobotDeathEvent) {
                enemyInfo.alive = false;
                enemyInfo.energy = 0;
            } else if (e instanceof ScannedRobotEvent) {
                final ScannedRobotEvent sre = (ScannedRobotEvent) e;
                enemyInfo.position = myInfo.position.project(myInfo.heading + sre.getBearingRadians(), sre.getDistance());
                enemyInfo.velocity = sre.getVelocity();
                enemyInfo.heading = sre.getHeadingRadians();
                enemyInfo.energy = sre.getEnergy();
                enemyInfo.name = sre.getName();
                enemyInfo.alive = true;
            } else if (e instanceof BulletHitBulletEvent) {
                final BulletHitBulletEvent bhbe = (BulletHitBulletEvent) e;
                myInfo.interceptedBullets.add(bhbe.getBullet());
                enemyInfo.interceptedBullets.add(bhbe.getHitBullet());
            } else if (e instanceof CustomEvent) {
                final Condition condition = ((CustomEvent) e).getCondition();
                if (condition instanceof Violet.FireCondition) {
                    myInfo.firePower = Option.of(((Violet.FireCondition)condition).bullet.getPower());
                }
            }
        }

        final LxxRobot me = new LxxRobot(battleState.me, myInfo);
        final LxxRobot enemy = new LxxRobot(battleState.enemy, enemyInfo);

        final BattleState newState = new BattleState(battleState, me, enemy);

        if (newState.getEnemyHitBullets().size() != enemy.hitBullets.size()) {
            new BattleState(battleState, me, enemy);
            new BattleState(battleState, me, enemy);
            assert !newState.enemy.alive || newState.getEnemyHitBullets().size() == enemy.hitBullets.size();
        }
        if (newState.getMyHitBullets().size() != me.hitBullets.size()) {
            new BattleState(battleState, me, enemy);
            new BattleState(battleState, me, enemy);
            assert newState.getMyHitBullets().size() == me.hitBullets.size();
        }

        if (newState.getEnemyInterceptedBullets().size() != enemy.interceptedBullets.size()) {
            assert newState.getEnemyInterceptedBullets().size() == enemy.interceptedBullets.size();
        }
        if (newState.getMyInterceptedBullets().size() != me.interceptedBullets.size()) {
            new BattleState(battleState, me, enemy);
            assert newState.getMyInterceptedBullets().size() == me.interceptedBullets.size();
        }

        return newState;
    }

    private static LxxRobotInfo getMyInfo(RobotStatus status) {
        final LxxRobotInfo myInfo = new LxxRobotInfo();
        myInfo.alive = true;
        myInfo.energy = status.getEnergy();
        myInfo.gunHeading = status.getGunHeadingRadians();
        myInfo.heading = status.getHeadingRadians();
        myInfo.position = new LxxPoint(status.getX(), status.getY());
        myInfo.radarHeading = status.getRadarHeadingRadians();
        myInfo.time = status.getTime();
        myInfo.round = status.getRoundNum();
        myInfo.velocity = status.getVelocity();
        myInfo.gunHeat = Option.of(status.getGunHeat());

        return myInfo;
    }

    private static LxxRobotInfo getEnemyInfo(LxxRobot prevState) {
        final LxxRobotInfo enemyInfo = new LxxRobotInfo();
        enemyInfo.alive = prevState.alive;
        enemyInfo.energy = prevState.energy;
        enemyInfo.heading = prevState.heading;
        enemyInfo.position = prevState.position;
        enemyInfo.round = prevState.round;
        enemyInfo.velocity = prevState.velocity;
        enemyInfo.name = prevState.name;

        return enemyInfo;
    }

    private static double getNewVelocity(double currentVelocity, double desiredVelocity) {
        if (currentVelocity == 0 || signum(currentVelocity) == signum(desiredVelocity)) {
            final double desiredAcceleration = abs(desiredVelocity) - abs(currentVelocity);
            return LxxUtils.limit(-Rules.MAX_VELOCITY,
                    currentVelocity + LxxUtils.limit(-Rules.DECELERATION, desiredAcceleration, Rules.ACCELERATION) * signum(desiredVelocity),
                    Rules.MAX_VELOCITY);
        } else if (abs(currentVelocity) >= Rules.DECELERATION) {
            return (currentVelocity - Rules.DECELERATION * (signum(currentVelocity)));
        } else {
            final double acceleration = 1 - abs(currentVelocity) / Rules.DECELERATION;
            return acceleration * signum(desiredVelocity);
        }
    }

}
