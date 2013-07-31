package lxx.services;

import lxx.Violet;
import lxx.events.BulletDetectedEvent;
import lxx.events.BulletFiredEvent;
import lxx.events.BulletGoneEvent;
import lxx.events.TickEvent;
import lxx.model.*;
import lxx.strategy.TurnDecision;
import lxx.utils.BattleRules;
import lxx.utils.LxxPoint;
import lxx.utils.LxxUtils;
import lxx.utils.func.Option;
import robocode.*;
import robocode.util.Utils;

import java.util.Collection;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class BattleStateService {

    private final Context context;

    public BattleStateService(Context context) {
        this.context = context;
    }

    public BattleState updateState(BattleRules rules, BattleState battleState,
                                   RobotStatus status, Collection<Event> allEvents, TurnDecision lastTurnDecision) {
        // todo: make me and opponent not nullable
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

                bulletGone(meBuilder, battleState.me.bulletsInAir, bhe.getBullet(), e.getTime());
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

                bulletGone(opponentBuilder, battleState.opponent.bulletsInAir, hbe.getBullet(), e.getTime());
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
                bulletGone(meBuilder, battleState.me.bulletsInAir, bhbe.getBullet(), e.getTime());
                bulletGone(opponentBuilder, battleState.opponent.bulletsInAir, bhbe.getHitBullet(), e.getTime());
            } else if (e instanceof CustomEvent) {
                final Condition condition = ((CustomEvent) e).getCondition();
                if (condition instanceof Violet.FireCondition) {
                    final Bullet bullet = ((Violet.FireCondition) condition).bullet;
                    final double power = bullet.getPower();
                    final LxxWave wave = new LxxWave(battleState.me, battleState.opponent, Rules.getBulletSpeed(power), battleState.time);

                    meBuilder.fire(power);
                    meBuilder.bulletFired(wave);

                    context.getMyBulletsEventsChannel().fireEvent(new BulletFiredEvent(new LxxBullet(wave, bullet)));
                }
            } else if (e instanceof SkippedTurnEvent) {
                System.out.println("Skipped turn");
            }
        }

        final Double opponentFirePower = opponentBuilder.getFirePower();
        if (opponentFirePower > 0) {
            opponentBuilder.bulletFired(new LxxWave(battleState.opponent, battleState.me, Rules.getBulletSpeed(opponentFirePower), battleState.time));
        }

        if (battleState.me != null) {
            processPassedBullets(battleState.me.bulletsInAir, meBuilder, opponentBuilder, status.getTime());
        }
        if (battleState.opponent != null) {
            processPassedBullets(battleState.opponent.bulletsInAir, opponentBuilder, meBuilder, status.getTime());
        }

        final LxxRobot me = meBuilder.build();
        final LxxRobot opponent = opponentBuilder.build();

        final BattleState newState = new BattleState(rules, status.getTime(), battleState, me, opponent);
        context.getBattleEventsChannel().fireEvent(new TickEvent(newState));
        return newState;
    }

    private void processPassedBullets(List<LxxWave> bulletsInAir, LxxRobotBuilder launcherBuilder, LxxRobotBuilder victimBuilder, long time) {
        for (LxxWave wave : bulletsInAir) {
            if (wave.isPassed(victimBuilder.getPosition(), time)) {
                launcherBuilder.bulletGone(wave);
                context.getBulletsEventsChannel(launcherBuilder.getName()).fireEvent(new BulletGoneEvent(wave));
            }
        }
    }

    private Option<LxxBullet> bulletGone(LxxRobotBuilder launcherBulder, List<LxxWave> launcherBullets, Bullet bullet, long time) {
        final Option<LxxWave> wave = findWave(launcherBullets, bullet, time);
        if (wave.defined()) {
            launcherBulder.bulletGone(wave.get());
            final LxxBullet detectedBullet = new LxxBullet(wave.get(), bullet);
            context.getBulletsEventsChannel(launcherBulder.getName()).fireEvent(new BulletDetectedEvent(detectedBullet));
            return Option.of(detectedBullet);
        }
        return Option.NONE;
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

    private static Option<LxxWave> findWave(List<LxxWave> waves, Bullet bullet, long time) {
        for (LxxWave w : waves) {
            if (Utils.isNear(w.speed, bullet.getVelocity()) && abs(w.launcher.distance(bullet.getX(), bullet.getY()) -
                    w.getTraveledDistance(time)) < w.speed) {
                return Option.of(w);
            }
        }

        return Option.none();
    }

}