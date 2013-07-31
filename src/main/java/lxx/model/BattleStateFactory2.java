package lxx.model;

import lxx.Violet;
import lxx.strategy.TurnDecision;
import lxx.utils.BattleRules;
import lxx.utils.LxxPoint;
import lxx.utils.LxxUtils;
import lxx.utils.func.Option;
import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.signum;
import static lxx.model.LxxWave2.inAir;
import static lxx.utils.func.LxxCollections.split;

public class BattleStateFactory2 {

    public static BattleState2 updateState(BattleRules rules, BattleState2 battleState,
                                           RobotStatus status, Collection<Event> allEvents, TurnDecision lastTurnDecision) {
        final LxxRobotBuilder meBuilder = new LxxRobotBuilder(rules, Option.of(battleState.me), status.getTime(), status.getRoundNum());
        final LxxRobotBuilder opponentBuilder = new LxxRobotBuilder(rules, Option.of(battleState.opponent), status.getTime(), status.getRoundNum());

        final LxxPoint myPos = new LxxPoint(status.getX(), status.getY());
        final double myHeading = status.getHeadingRadians();

        meBuilder.alive();
        meBuilder.energy(status.getEnergy());
        meBuilder.position(myPos);
        meBuilder.heading(myHeading);
        meBuilder.gunHeading(status.getGunHeadingRadians());
        meBuilder.radarHeading(status.getRadarHeadingRadians());
        meBuilder.velocity(status.getVelocity());
        meBuilder.gunHeat(status.getGunHeat());
        meBuilder.lastScanTime(status.getTime());
        meBuilder.name(rules.myName);

        List<LxxWave2> myBulletsInAir = new ArrayList<LxxWave2>(battleState.myBulletsInAir);
        List<LxxWave2> opponentBulletsInAir = new ArrayList<LxxWave2>(battleState.opponentBulletsInAir);
        final List<LxxBullet2> myHitBullets = new ArrayList<LxxBullet2>();
        final List<LxxBullet2> opponentHitBullets = new ArrayList<LxxBullet2>();
        final List<LxxBullet2> myInterceptedBullets = new ArrayList<LxxBullet2>();
        final List<LxxBullet2> opponentInterceptedBullets = new ArrayList<LxxBullet2>();

        for (Event e : allEvents) {
            if (e instanceof BulletHitEvent) {
                final BulletHitEvent bhe = (BulletHitEvent) e;
                final double dmg = Rules.getBulletDamage(bhe.getBullet().getPower());
                final double returnedEnergy = LxxUtils.getReturnedEnergy(bhe.getBullet().getPower());

                meBuilder.returnedEnergy(returnedEnergy);
                opponentBuilder.receivedDmg(dmg);
                opponentBuilder.energy(bhe.getEnergy());
                opponentBuilder.name(bhe.getBullet().getVictim());
                opponentBuilder.alive(bhe.getEnergy() > 0);

                final LxxBullet2 bullet = getMyBullet(myBulletsInAir, e.getTime(), bhe.getBullet());
                myHitBullets.add(bullet);
                myBulletsInAir.remove(bullet.wave);
            } else if (e instanceof DeathEvent) {
                meBuilder.dead();
            } else if (e instanceof HitByBulletEvent) {
                final HitByBulletEvent hbe = (HitByBulletEvent) e;
                final double dmg = Rules.getBulletDamage(hbe.getPower());
                final double returnedEnergy = LxxUtils.getReturnedEnergy(hbe.getPower());

                opponentBuilder.returnedEnergy(returnedEnergy);
                meBuilder.receivedDmg(dmg);
                final Bullet bullet = hbe.getBullet();
                opponentBuilder.name(bullet.getName());

                final Option<LxxBullet2> bulletOpt = getOpponentBullet(battleState, opponentBulletsInAir, e.getTime(), bullet);
                if (bulletOpt.defined()) {
                    opponentHitBullets.add(bulletOpt.get());
                    opponentBulletsInAir.remove(bulletOpt.get().wave);
                } else {
                    System.out.println("Missed bullet!");
                }
            } else if (e instanceof HitRobotEvent) {
                meBuilder.hitRobot();
                opponentBuilder.hitRobot();
            } else if (e instanceof HitWallEvent) {
                final double expectedSpeed = getNewVelocity(battleState.me.velocity, lastTurnDecision.desiredVelocity);
                meBuilder.hitWall(Rules.getWallHitDamage(expectedSpeed));
            } else if (e instanceof RobotDeathEvent) {
                opponentBuilder.dead();
            } else if (e instanceof ScannedRobotEvent) {
                final ScannedRobotEvent sre = (ScannedRobotEvent) e;
                opponentBuilder.position(myPos.project(myHeading + sre.getBearingRadians(), sre.getDistance()));
                opponentBuilder.velocity(sre.getVelocity());
                opponentBuilder.heading(sre.getHeadingRadians());
                opponentBuilder.energy(sre.getEnergy());
                opponentBuilder.name(sre.getName());
                opponentBuilder.alive();
                opponentBuilder.lastScanTime(e.getTime());
            } else if (e instanceof BulletHitBulletEvent) {
                final BulletHitBulletEvent bhbe = (BulletHitBulletEvent) e;
                final LxxBullet2 bullet = getMyBullet(myBulletsInAir, e.getTime(), bhbe.getBullet());
                myInterceptedBullets.add(bullet);
                myBulletsInAir.remove(bullet.wave);

                final Option<LxxBullet2> bulletOpt = getOpponentBullet(battleState, opponentBulletsInAir, e.getTime(), bhbe.getHitBullet());
                if (bulletOpt.defined()) {
                    opponentInterceptedBullets.add(bulletOpt.get());
                    opponentBulletsInAir.remove(bulletOpt.get().wave);
                } else {
                    System.out.println("Missed bullet!");
                }
            } else if (e instanceof CustomEvent) {
                final Condition condition = ((CustomEvent) e).getCondition();
                if (condition instanceof Violet.FireCondition) {
                    meBuilder.fire(((Violet.FireCondition) condition).bullet.getPower());
                }
            } else if (e instanceof SkippedTurnEvent) {
                System.out.println("Skipped turn");
            }
        }

        final LxxRobot2 me = meBuilder.build();
        final LxxRobot2 opponent = opponentBuilder.build();

        List<LxxWave2>[] splittedWaves = split(myBulletsInAir, new inAir(opponent));
        myBulletsInAir = splittedWaves[0];
        final List<LxxWave2> myGoneBullets = splittedWaves[1];

        splittedWaves = split(opponentBulletsInAir, new inAir(opponent));
        opponentBulletsInAir = splittedWaves[0];
        final List<LxxWave2> opponentGoneBullets = splittedWaves[1];

        final Option<LxxWave2> myFiredBullet = me.firePower == 0 ? Option.<LxxWave2>none() :
                Option.of(new LxxWave2(battleState.me, battleState.opponent, Rules.getBulletSpeed(me.firePower), battleState.time));

        if (myFiredBullet.defined()) {
            myBulletsInAir.add(myFiredBullet.get());
        }

        final Option<LxxWave2> opponentFiredBullet = opponent.firePower == 0 ? Option.<LxxWave2>none() :
                Option.of(new LxxWave2(battleState.opponent, battleState.me, Rules.getBulletSpeed(opponent.firePower), battleState.time));

        if (opponentFiredBullet.defined()) {
            opponentBulletsInAir.add(opponentFiredBullet.get());
        }

        assert myBulletsInAir.size() == battleState.myBulletsInAir.size() - myInterceptedBullets.size() - myHitBullets.size() + (myFiredBullet.defined() ? 1 : 0);

        return new BattleState2(rules, status.getTime(), battleState, me, opponent,
                myFiredBullet, opponentFiredBullet,
                myBulletsInAir, opponentBulletsInAir,
                myHitBullets, opponentHitBullets,
                myInterceptedBullets, opponentInterceptedBullets,
                myGoneBullets, opponentGoneBullets);
    }

    private static Option<LxxBullet2> getOpponentBullet(BattleState2 battleState, List<LxxWave2> opponentBulletsInAir, long time, Bullet bullet) {
        Option<LxxBullet2> bulletOpt = Option.none();
        final Option<LxxWave2> wave = findWave(opponentBulletsInAir, bullet, time);
        if (wave.defined()) {
            bulletOpt = Option.of(new LxxBullet2(wave.get(), bullet));
        } else {
            final Option<BattleState2> fireTimeOpt = findOpponentFireTime(bullet, battleState);
            if (fireTimeOpt.defined()) {
                final BattleState2 fireTime = fireTimeOpt.get();
                final LxxWave2 missedWave = new LxxWave2(fireTime.opponent, fireTime.me, bullet.getVelocity(), fireTime.time);
                bulletOpt = Option.of(new LxxBullet2(missedWave, bullet));
            }
        }
        return bulletOpt;
    }

    private static LxxBullet2 getMyBullet(List<LxxWave2> myBulletsInAir, long time, Bullet bullet) {
        final Option<LxxWave2> wave = findWave(myBulletsInAir, bullet, time);
        if (wave.defined()) {
            return new LxxBullet2(wave.get(), bullet);
        } else {
            throw new IllegalStateException("Miss own bullet!");
        }
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

    private static Option<LxxWave2> findWave(List<LxxWave2> waves, Bullet bullet, long time) {
        for (LxxWave2 w : waves) {
            if (Utils.isNear(w.speed, bullet.getVelocity()) && abs(w.launcher.distance(bullet.getX(), bullet.getY()) -
                    w.getTraveledDistance(time)) < w.speed) {
                return Option.of(w);
            }
        }

        return Option.none();
    }

    private static Option<BattleState2> findOpponentFireTime(Bullet bullet, BattleState2 current) {
        BattleState2 bs = current;
        LxxPoint bltPos = new LxxPoint(bullet.getX(), bullet.getY());
        double dist = bltPos.aDistance(bs.opponent);
        while (true) {
            bs = bs.prevState;
            bltPos = bltPos.project(bullet.getHeading(), -bullet.getVelocity());
            final double newDist = bltPos.aDistance(bs.opponent);
            if (newDist > dist) {
                break;
            }
            dist = newDist;
        }

        return LxxUtils.getBoundingRectangleAt(bs.opponent).contains(bltPos.getX(), bltPos.getY())
                ? Option.of(bs)
                : Option.<BattleState2>none();
    }

}