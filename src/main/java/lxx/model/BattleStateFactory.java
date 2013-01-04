package lxx.model;

import lxx.strategy.TurnDecision;
import lxx.utils.BattleRules;
import lxx.utils.LxxPoint;
import lxx.utils.LxxUtils;
import robocode.*;

import java.util.Collection;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class BattleStateFactory {

    private BattleStateFactory() {}

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
                myInfo.bullets.add(bhe.getBullet());
                enemyInfo.name = bhe.getBullet().getVictim();
                enemyInfo.alive = enemyInfo.energy > 0;
                enemyInfo.position = new LxxPoint(bhe.getBullet().getX(), bhe.getBullet().getY());
            } else if (e instanceof DeathEvent) {
                myInfo.alive = false;
            } else if (e instanceof HitByBulletEvent) {
                final HitByBulletEvent hbe = (HitByBulletEvent) e;
                // TODO (azhidkov): LxxUtils.getReturnedEnergy
                final double dmg = Rules.getBulletDamage(hbe.getPower());
                final double returnedEnergy = LxxUtils.getReturnedEnergy(hbe.getPower());
                enemyInfo.returnedEnergy += returnedEnergy;
                myInfo.receivedDmg += dmg;
                enemyInfo.bullets.add(hbe.getBullet());
                enemyInfo.name = hbe.getBullet().getName();
            } else if (e instanceof HitRobotEvent) {
                myInfo.hitRobot = true;
                enemyInfo.hitRobot = true;
            } else if (e instanceof HitWallEvent) {
                final double expectedSpeed = getNewVelocity(battleState.me.velocity, lastTurnDecision.desiredVelocity);
                myInfo.wallDmg += Rules.getWallHitDamage(expectedSpeed);
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
                myInfo.bullets.add(bhbe.getBullet());
                enemyInfo.bullets.add(bhbe.getHitBullet());
            }
        }

        final LxxRobot me = new LxxRobot(battleState.me, myInfo);
        final LxxRobot enemy = new LxxRobot(battleState.enemy, enemyInfo);

        return new BattleState(battleState, battleState.rules, me, enemy);
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
