package lxx.movement;

import lxx.Violet;
import lxx.model.BattleState;
import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.movement.orbital.AvoidEnemyOrbitalMovement;
import lxx.movement.orbital.OrbitDirection;
import lxx.paint.Canvas;
import lxx.paint.Circle;
import lxx.services.DangerService;
import lxx.services.MonitoringService;
import lxx.services.WaveDangerInfo;
import lxx.utils.func.F1;
import lxx.utils.func.F3;
import robocode.Rules;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.signum;
import static lxx.utils.LxxUtils.List;

public class WaveSurfingMovement {

    public static final MovementOptionDangerComparator optionsComparator = new MovementOptionDangerComparator();

    private final DangerService dangerService;
    private final AvoidEnemyOrbitalMovement orbitalMovement;

    private OrbitDirection lastOrbitDirection;
    private Color pathColor;

    public WaveSurfingMovement(DangerService dangerService, AvoidEnemyOrbitalMovement orbitalMovement) {
        this.dangerService = dangerService;
        this.orbitalMovement = orbitalMovement;
    }

    public MovementDecision getMovementDecision(BattleState bs, List<LxxWave> waves) {
        final TreeSet<LxxWave> enemyBullets = new TreeSet<LxxWave>(new FlightTimeComparator(bs.me));
        enemyBullets.addAll(waves);
        assert enemyBullets.size() > 0;

        pathColor = Violet.primaryColor155;
        lastOrbitDirection = selectOrbitDirection(bs.me, bs.me, bs.opponent, new ArrayList<LxxWave>(enemyBullets), lastOrbitDirection).orbitDirection;
        MonitoringService.setOrbitDirection(lastOrbitDirection);

        return orbitalMovement.getMovementDecision(bs.me, enemyBullets.iterator().next(), lastOrbitDirection, bs.opponent);
    }

    private MovementOption selectOrbitDirection(LxxRobot myRealState, LxxRobot me, LxxRobot enemy, List<LxxWave> waves,
                                                OrbitDirection lastOrbitDirection) {
        assert waves != null && waves.size() > 0 : waves;

        final LxxWave firstWave;
        final LxxWave secondWave;
        final int firstWaveFlightTimeLimit;
        if (waves.size() == 1) {
            firstWave = waves.get(0);
            secondWave = null;
            firstWaveFlightTimeLimit = 0;
        } else {
            if (waves.get(0).getFlightTime(me) < waves.get(1).getFlightTime(me)) {
                firstWave = waves.get(0);
                secondWave = waves.get(1);
            } else {
                firstWave = waves.get(1);
                secondWave = waves.get(0);
            }
            firstWaveFlightTimeLimit = 2;
        }

        final WaveDangerInfo waveDangerInfo = dangerService.getWaveDangerInfo(firstWave);
        waveDangerInfo.draw(Canvas.WS, myRealState.time);
        final DangerFunction firstWaveSameDirDF = new DangerFunction(waveDangerInfo, myRealState, 0.98, pathColor != null);
        final DangerFunction firstWaveAnotherDirDF = new DangerFunction(waveDangerInfo, myRealState, 1, pathColor != null);
        final MovementOption[] options = new MovementOption[]{
                predict(firstWave, me, enemy, firstWaveFlightTimeLimit, OrbitDirection.CLOCKWISE,
                        lastOrbitDirection == OrbitDirection.CLOCKWISE ? firstWaveSameDirDF : firstWaveAnotherDirDF),

                predict(firstWave, me, enemy, firstWaveFlightTimeLimit, OrbitDirection.STOP,
                        lastOrbitDirection == OrbitDirection.STOP ? firstWaveSameDirDF : firstWaveAnotherDirDF),

                predict(firstWave, me, enemy, firstWaveFlightTimeLimit, OrbitDirection.COUNTER_CLOCKWISE,
                        lastOrbitDirection == OrbitDirection.COUNTER_CLOCKWISE ? firstWaveSameDirDF : firstWaveAnotherDirDF)
        };

        pathColor = null;

        return selectBestOption(myRealState, enemy, secondWave, options);
    }

    private MovementOption selectBestOption(LxxRobot myRealState, LxxRobot enemy, LxxWave secondWave, MovementOption[] options) {
        Arrays.sort(options, optionsComparator);

        if (secondWave == null) {
            return options[0];
        }

        final MovementOption[] secondWaveOptions = new MovementOption[3];
        secondWaveOptions[0] = selectOrbitDirection(myRealState, options[0].me, enemy, List(secondWave), null);

        if (options[0].danger + secondWaveOptions[0].danger < options[1].danger) {
            return options[0];
        }

        int bestOptIdx = 0;
        for (int i = 1; i < secondWaveOptions.length; i++) {
            secondWaveOptions[i] = selectOrbitDirection(myRealState, options[i].me, enemy, List(secondWave), null);
            if (options[i].danger + secondWaveOptions[i].danger < options[bestOptIdx].danger + secondWaveOptions[bestOptIdx].danger) {
                bestOptIdx = i;
            }
        }

        return options[bestOptIdx];
    }

    private MovementOption predict(LxxWave wave, LxxRobot me, LxxRobot enemy, int flightLimit,
                                   OrbitDirection orbitDirection, DangerFunction dangerFunction) {
        final MovementDecision enemyMd = new MovementDecision(
                enemy != null ? Rules.MAX_VELOCITY * signum(enemy.velocity) : 0, 0);

        double minDist = enemy != null ? me.distance(enemy) : Integer.MAX_VALUE;

        do {
            final MovementDecision md = orbitalMovement.getMovementDecision(me, wave, orbitDirection, enemy);
            me = new LxxRobot(me, md.turnRate, md.desiredVelocity);
            if (enemy != null && enemy.alive) {
                enemy = new LxxRobot(enemy, enemyMd.turnRate, enemyMd.desiredVelocity);
                assert enemy.alive;
                minDist = min(minDist, me.distance(enemy));
            }

            if (Canvas.WS.enabled() && pathColor != null) {
                Canvas.WS.draw(new Circle(me, 3, true), pathColor);
                Canvas.WS.draw(new Circle(enemy, 3, true), pathColor);
            }
        } while ((wave.distance(me) - (me.time - wave.time) * wave.speed) / wave.speed > flightLimit);

        return new MovementOption(orbitDirection, dangerFunction.f(me, wave, minDist, orbitDirection), me);
    }

    private static double getDistDanger(double distBetween) {
        if (distBetween < 50) {
            return 500 / distBetween;
        }
        if (distBetween < 400) {
            return 400 / (400 + Math.pow(Math.E, distBetween / 40)) + 0.01;
        } else if (distBetween < 1000) {
            return (1000 - distBetween) / 600 * 0.01;
        } else {
            return 0;
        }
    }

    private static class MovementOption {

        public final OrbitDirection orbitDirection;
        public final double danger;
        public final LxxRobot me;

        private MovementOption(OrbitDirection orbitDirection, double danger, LxxRobot me) {
            this.orbitDirection = orbitDirection;
            this.danger = danger;
            this.me = me;
        }
    }

    private static class DangerFunction implements F3<LxxRobot, LxxWave, Double, Double> {

        private final WaveDangerInfo waveDangerInfo;
        private final LxxRobot myRealState;
        private final double mult;
        private final boolean drawDangers;

        private DangerFunction(WaveDangerInfo waveDangerInfo, LxxRobot myRealState, double mult, boolean drawDangers) {
            this.waveDangerInfo = waveDangerInfo;
            this.myRealState = myRealState;
            this.mult = mult;
            this.drawDangers = drawDangers;
        }

        @Override
        public Double f(LxxRobot me, LxxWave wave, Double minDist, OrbitDirection dir) {

            final double distDng = getDistDanger(minDist);
            final double pointDanger = waveDangerInfo.getPointDanger(me);
            final double flightTime = wave.getFlightTime(myRealState.position, myRealState.time);

            final double danger = (pointDanger / flightTime + 4 * distDng) * mult;

            MonitoringService.setDangerComponents(dir, 4 * distDng, pointDanger, flightTime, danger);

            return danger;
        }
    }

    private static class MovementOptionDangerComparator implements Comparator<MovementOption> {

        @Override
        public int compare(MovementOption o1, MovementOption o2) {
            return (int) signum(o1.danger - o2.danger);
        }
    }

    private static final class comingBullets implements F1<LxxWave, Boolean> {

        private final LxxRobot victim;
        private final int flightTimeThreshold;

        private comingBullets(LxxRobot victim, int flightTimeThreshold) {
            this.victim = victim;
            this.flightTimeThreshold = flightTimeThreshold;
        }

        @Override
        public Boolean f(LxxWave bullet) {
            return (bullet.aDistance(victim) - (victim.time - bullet.time) * bullet.speed) / bullet.speed > flightTimeThreshold;
        }
    }

    private static final class FlightTimeComparator implements Comparator<LxxWave> {

        private final LxxRobot victim;

        private FlightTimeComparator(LxxRobot victim) {
            this.victim = victim;
        }

        @Override
        public int compare(LxxWave o1, LxxWave o2) {
            final double o1FlightTime = (o1.aDistance(victim) - (victim.time - o1.time) * o1.speed) / o1.speed;
            final double o2FlightTime = (o2.aDistance(victim) - (victim.time - o2.time) * o2.speed) / o2.speed;
            return (int) signum(o1FlightTime - o2FlightTime);
        }
    }

}