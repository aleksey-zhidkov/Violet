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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class BattleStateService {

    private final Map<Class<? extends Event>, EventProcessor<? extends Event>> eventProcessors = new HashMap<Class<? extends Event>, EventProcessor<? extends Event>>(){{
        put(BulletHitEvent.class, new BulletHitEventProcessor());
        put(DeathEvent.class, new DeathEventProcessor());
        put(HitByBulletEvent.class, new HitByBulletEventProcessor());
        put(HitRobotEvent.class, new HitRobotEventProcessor());
        put(HitWallEvent.class, new HitWallEventProcessor());
        put(RobotDeathEvent.class, new RobotDeathEventProcessor());
        put(ScannedRobotEvent.class, new ScannedRobotEventProcessor());
        put(BulletHitBulletEvent.class, new BulletHitBulletEventProcessor());
        put(CustomEvent.class, new CustomEventProcessor());
    }};

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
            //noinspection unchecked
            final EventProcessor<Event> ep = (EventProcessor<Event>) eventProcessors.get(e.getClass());
            if (ep != null) {
                ep.process(e, battleState, meBuilder, opponentBuilder, lastTurnDecision);
            }
        }

        final Double opponentFirePower = opponentBuilder.getFirePower();
        if (opponentFirePower > 0) {
            opponentBuilder.bulletFired(new LxxWave(battleState.opponent, battleState.me, Rules.getBulletSpeed(opponentFirePower), battleState.time, false));
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

    private Option<LxxBullet> bulletGone(LxxRobotBuilder launcherBulder, List<LxxWave> launcherBullets, Bullet bullet, long time, LxxBulletState state) {
        final Option<LxxWave> wave = findWave(launcherBullets, bullet, time);
        if (wave.defined()) {
            launcherBulder.bulletGone(wave.get());
            final LxxBullet detectedBullet = new LxxBullet(wave.get(), bullet, state);
            context.getBulletsEventsChannel(launcherBulder.getName()).fireEvent(new BulletDetectedEvent(detectedBullet));
            return Option.of(detectedBullet);
        }
        return Option.none();
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

    private interface EventProcessor<T extends Event> {

        void process(T event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision);

    }

    private class BulletHitEventProcessor implements EventProcessor<BulletHitEvent> {
        @Override
        public void process(BulletHitEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            final double dmg = Rules.getBulletDamage(event.getBullet().getPower());
            final double returnedEnergy = LxxUtils.getReturnedEnergy(event.getBullet().getPower());

            myBuilder.returnedEnergy(returnedEnergy);
            opponentBuilder.receivedDmg(dmg);
            opponentBuilder.energy(event.getEnergy());
            opponentBuilder.name(event.getBullet().getVictim());
            opponentBuilder.alive(event.getEnergy() > 0);

            bulletGone(myBuilder, battleState.me.bulletsInAir, event.getBullet(), event.getTime(), LxxBulletState.HIT_ROBOT);
        }
    }

    private static class DeathEventProcessor implements EventProcessor<DeathEvent> {
        @Override
        public void process(DeathEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            myBuilder.died();
        }
    }

    private class HitByBulletEventProcessor implements EventProcessor<HitByBulletEvent> {
        @Override
        public void process(HitByBulletEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            final double dmg = Rules.getBulletDamage(event.getPower());
            final double returnedEnergy = LxxUtils.getReturnedEnergy(event.getPower());

            opponentBuilder.returnedEnergy(returnedEnergy);
            myBuilder.receivedDmg(dmg);
            final Bullet bullet = event.getBullet();
            opponentBuilder.name(bullet.getName());

            bulletGone(opponentBuilder, battleState.opponent.bulletsInAir, event.getBullet(), event.getTime(), LxxBulletState.HIT_ROBOT);
        }
    }

    private static class HitRobotEventProcessor implements EventProcessor<HitRobotEvent> {
        @Override
        public void process(HitRobotEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            myBuilder.hitRobot();
            opponentBuilder.hitRobot();
            MonitoringService.robotHitted();
        }
    }

    private static class HitWallEventProcessor implements EventProcessor<HitWallEvent> {
        @Override
        public void process(HitWallEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            final double expectedSpeed = getNewVelocity(battleState.me.velocity, lastTurnDecision.desiredVelocity);
            myBuilder.hitWall(Rules.getWallHitDamage(expectedSpeed));
            MonitoringService.wallHitted();
        }
    }

    private static class RobotDeathEventProcessor implements EventProcessor<RobotDeathEvent> {
        @Override
        public void process(RobotDeathEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            opponentBuilder.died();
        }
    }

    private static class ScannedRobotEventProcessor implements EventProcessor<ScannedRobotEvent> {
        @Override
        public void process(ScannedRobotEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            opponentBuilder.position(myBuilder.getPosition().project(myBuilder.getHeading() + event.getBearingRadians(), event.getDistance()));
            opponentBuilder.velocity(event.getVelocity());
            opponentBuilder.heading(event.getHeadingRadians());
            opponentBuilder.energy(event.getEnergy());
            opponentBuilder.name(event.getName());
            opponentBuilder.alive();
            opponentBuilder.lastScanTime(event.getTime());
        }
    }

    private class BulletHitBulletEventProcessor implements EventProcessor<BulletHitBulletEvent> {
        @Override
        public void process(BulletHitBulletEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            bulletGone(myBuilder, battleState.me.bulletsInAir, event.getBullet(), event.getTime(), LxxBulletState.HIT_BULLET);
            bulletGone(opponentBuilder, battleState.opponent.bulletsInAir, event.getHitBullet(), event.getTime(), LxxBulletState.HIT_BULLET);
        }
    }

    private class CustomEventProcessor implements EventProcessor<CustomEvent> {
        @Override
        public void process(CustomEvent event, BattleState battleState, LxxRobotBuilder myBuilder, LxxRobotBuilder opponentBuilder, TurnDecision lastTurnDecision) {
            final Condition condition = event.getCondition();
            if (condition instanceof Violet.FireCondition) {
                final Bullet bullet = ((Violet.FireCondition) condition).bullet;
                final double power = bullet.getPower();
                final LxxWave wave = new LxxWave(battleState.me, battleState.opponent, Rules.getBulletSpeed(power), battleState.time, false);

                myBuilder.fire(power);
                myBuilder.bulletFired(wave);

                context.getMyBulletsEventsChannel().fireEvent(new BulletFiredEvent(new LxxBullet(wave, bullet, LxxBulletState.COMING)));
            }
        }
    }
}
