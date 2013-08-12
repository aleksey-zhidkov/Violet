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
import lxx.utils.func.F3;
import robocode.Rules;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.pow;
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

        final WaveDangerInfo waveDangerInfo = dangerService.getWaveDangerInfo(firstWave, firstWave.time >= myRealState.time);
        waveDangerInfo.draw(Canvas.WS, myRealState.time);
        final DangerFunction firstWaveSameDirDF = new DangerFunction(waveDangerInfo, myRealState, 0.98);
        final DangerFunction firstWaveAnotherDirDF = new DangerFunction(waveDangerInfo, myRealState, 1);
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

    private MovementOption predict(LxxWave wave, LxxRobot me, LxxRobot opponent, int flightLimit,
                                   OrbitDirection orbitDirection, DangerFunction dangerFunction) {
        final MovementDecision enemyMd = new MovementDecision(
                opponent != null ? Rules.MAX_VELOCITY * signum(opponent.velocity) : 0, 0);

        double minDist = opponent != null ? me.distance(opponent) : Integer.MAX_VALUE;

        LxxRobot meImage = me;
        LxxRobot opponentImage = opponent;
        do {
            final MovementDecision md = orbitalMovement.getMovementDecision(meImage, wave, orbitDirection, opponentImage);
            meImage = new LxxRobot(meImage, md.turnRate, md.desiredVelocity);
            if (opponentImage != null && opponentImage.alive) {
                opponentImage = new LxxRobot(opponentImage, enemyMd.turnRate, enemyMd.desiredVelocity);
                assert opponentImage.alive;
                minDist = min(minDist, meImage.distance(opponentImage));
            }

            if (Canvas.WS.enabled() && pathColor != null) {
                Canvas.WS.draw(new Circle(meImage, 3, true), pathColor);
                Canvas.WS.draw(new Circle(opponentImage, 3, true), pathColor);
            }
        } while ((wave.distance(meImage) - (meImage.time - wave.time) * wave.speed) / wave.speed > flightLimit);

        return new MovementOption(orbitDirection, dangerFunction.f(meImage, wave, minDist, orbitDirection), meImage);
    }

    private static double getDistDanger(double distBetween) {
        if (distBetween <= 50) {
            return 1;
        } else if (distBetween < 1000) {
            return 1 - (pow(distBetween / 1000, 2));
        } else {
            return 0;
        }
    }

    private static final class MovementOption {

        public final OrbitDirection orbitDirection;
        public final double danger;
        public final LxxRobot me;

        private MovementOption(OrbitDirection orbitDirection, double danger, LxxRobot me) {
            this.orbitDirection = orbitDirection;
            this.danger = danger;
            this.me = me;
        }
    }

    private static final class DangerFunction implements F3<LxxRobot, LxxWave, Double, Double> {

        private final WaveDangerInfo waveDangerInfo;
        private final LxxRobot myRealState;
        private final double mult;

        private DangerFunction(WaveDangerInfo waveDangerInfo, LxxRobot myRealState, double mult) {
            this.waveDangerInfo = waveDangerInfo;
            this.myRealState = myRealState;
            this.mult = mult;
        }

        @Override
        public Double f(LxxRobot me, LxxWave wave, Double minDist, OrbitDirection dir) {

            final double distDng = getDistDanger(minDist);
            final double pointDanger = waveDangerInfo.getPointDanger(me);
            final double flightTime = wave.getFlightTime(myRealState.position, myRealState.time);

            // todo: use first bullet flight time instead of 30
            final double danger = (pointDanger * 3 + distDng) * mult * (30 / flightTime);

            MonitoringService.setDangerComponents(dir, distDng, pointDanger, flightTime, danger);

            return danger;
        }
    }

    private static class MovementOptionDangerComparator implements Comparator<MovementOption>, Serializable {

        @Override
        public int compare(MovementOption o1, MovementOption o2) {
            return (int) signum(o1.danger - o2.danger);
        }
    }

    private static final class FlightTimeComparator implements Comparator<LxxWave> {

        private final LxxRobot victim;

        private FlightTimeComparator(LxxRobot victim) {
            this.victim = victim;
        }

        @Override
        public int compare(LxxWave o1, LxxWave o2) {
            final double o1FlightTime = (o1.distance(victim) - (victim.time - o1.time) * o1.speed) / o1.speed;
            final double o2FlightTime = (o2.distance(victim) - (victim.time - o2.time) * o2.speed) / o2.speed;
            return (int) signum(o1FlightTime - o2FlightTime);
        }
    }

}
