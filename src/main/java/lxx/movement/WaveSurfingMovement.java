package lxx.movement;

import lxx.Violet;
import lxx.model.BattleState;
import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.movement.orbital.AvoidEnemyOrbitalMovement;
import lxx.movement.orbital.OrbitDirection;
import lxx.paint.Canvas;
import lxx.paint.Circle;
import lxx.paint.Square;
import lxx.services.DangerService;
import lxx.services.MonitoringService;
import lxx.services.WaveDangerInfo;
import lxx.utils.APoint;
import lxx.utils.LxxPoint;
import lxx.utils.func.F3;
import robocode.Rules;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;

public class WaveSurfingMovement {

    private final DangerService dangerService;
    private final AvoidEnemyOrbitalMovement orbitalMovement;
    private Color pathColor;

    public WaveSurfingMovement(DangerService dangerService, AvoidEnemyOrbitalMovement orbitalMovement) {
        this.dangerService = dangerService;
        this.orbitalMovement = orbitalMovement;
    }

    public DangerService getDangerService() {
        return dangerService;
    }

    public LxxPoint getMovementDecision(BattleState bs, List<LxxWave> waves, int flightTimeThreshold) {
        assert waves.size() > 0;

        pathColor = Violet.primaryColor155;
        Canvas.WS_MOVEMENT.reset();
        final LxxPoint pnt = selectDestination(bs.me, bs.opponent, waves, flightTimeThreshold);
        Canvas.WS_MOVEMENT.draw(new Square(pnt, bs.rules.robotWidth), Color.RED);

        return pnt;
    }

    private FuturePoint selectDestination(LxxRobot me, LxxRobot enemy, List<LxxWave> waves, int flightTimeThreshold) {
        assert waves != null && waves.size() > 0 : waves;

        final LxxWave firstWave;
        final int firstWaveFlightTimeLimit;
        if (waves.size() == 1) {
            firstWave = waves.get(0);
            firstWaveFlightTimeLimit = 0;
        } else {
            if (waves.get(0).getFlightTime(me) < waves.get(1).getFlightTime(me)) {
                firstWave = waves.get(0);
            } else {
                firstWave = waves.get(1);
            }
            firstWaveFlightTimeLimit = flightTimeThreshold;
        }

        final WaveDangerInfo waveDangerInfo = dangerService.getWaveDangerInfo(firstWave, firstWave.imaginary);
        MonitoringService.setSurfingDangerInfo(waveDangerInfo);
        final DangerFunction dangerFunction = new DangerFunction(waveDangerInfo);


        final List<FuturePoint> futurePoints = new ArrayList<FuturePoint>();
        futurePoints.addAll(predict(firstWave, me, enemy, firstWaveFlightTimeLimit, OrbitDirection.CLOCKWISE, dangerFunction));
        futurePoints.addAll(predict(firstWave, me, enemy, firstWaveFlightTimeLimit, OrbitDirection.COUNTER_CLOCKWISE, dangerFunction));

        for (FuturePoint fp : futurePoints) {
            fp.calculateTotalDanger(dangerFunction);
        }

        Collections.sort(futurePoints);

        return futurePoints.get(0);
    }

    private List<FuturePoint> predict(LxxWave wave, LxxRobot me, LxxRobot opponent, int flightLimit,
                                      OrbitDirection orbitDirection, DangerFunction dangerFunction) {
        final MovementDecision enemyMd = new MovementDecision(
                Rules.MAX_VELOCITY * signum(opponent.velocity), 0);

        LxxRobot meImage = me;
        LxxRobot opponentImage = opponent;

        final List<FuturePoint> points = new ArrayList<FuturePoint>();
        final FuturePoint firstPoint = new FuturePoint(me.position, dangerFunction.waveDangerInfo.getPointDanger(me.position));
        firstPoint.minDistToOpponent = opponent.alive ? me.distance(opponent) : Integer.MAX_VALUE;
        points.add(firstPoint);

        do {
            final APoint surfPoint = opponentImage.alive ? opponentImage : wave;
            final MovementDecision md = orbitalMovement.getMovementDecision(meImage, surfPoint, wave, orbitDirection, opponentImage);
            meImage = new LxxRobot(meImage, md.turnRate, md.desiredVelocity);
            final FuturePoint futurePoint = new FuturePoint(meImage.position, dangerFunction.waveDangerInfo.getPointDanger(meImage.position));
            points.add(futurePoint);
            if (opponentImage.alive) {
                opponentImage = new LxxRobot(opponentImage, enemyMd.turnRate, enemyMd.desiredVelocity);
                assert opponentImage.alive;
                futurePoint.distanceToOpponent = meImage.distance(opponentImage);
                futurePoint.minDistToOpponent = futurePoint.distanceToOpponent;
                for (FuturePoint point : points) {
                    point.minDistToOpponent = min(point.minDistToOpponent, opponent.alive ? me.distance(opponent) : Integer.MAX_VALUE);
                }
            }

            if (Canvas.WS_MOVEMENT.enabled() && pathColor != null) {
                Canvas.WS_MOVEMENT.draw(new Circle(meImage, 3, true), pathColor);
                Canvas.WS_MOVEMENT.draw(new Circle(opponentImage, 3, true), pathColor);
            }
        } while ((wave.distance(meImage) - (meImage.time - wave.time) * wave.speed) / wave.speed > flightLimit);

        return points;
    }

    private static final class DangerFunction implements F3<Double, Double, Double, Double> {

        private final WaveDangerInfo waveDangerInfo;

        private DangerFunction(WaveDangerInfo waveDangerInfo) {
            this.waveDangerInfo = waveDangerInfo;
        }

        @Override
        public Double f(Double distanceToEnemy, Double minDistToEnemy, Double danger) {
            final double distDng = getDistDanger(distanceToEnemy);
            final double minDististDng = getDistDanger(minDistToEnemy);
            return danger * 3 + distDng + minDististDng;
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

    }

    private static final class FuturePoint extends LxxPoint implements Comparable<FuturePoint> {

        public final double danger;

        public double minDistToOpponent;
        public double distanceToOpponent;

        public double totalDanger;

        public FuturePoint(LxxPoint pnt, double danger) {
            super(pnt);
            this.danger = danger;
        }

        public void calculateTotalDanger(DangerFunction dangerFunction) {
            totalDanger = dangerFunction.f(distanceToOpponent, minDistToOpponent, danger);
        }

        @Override
        public int compareTo(FuturePoint o) {
            return java.lang.Double.compare(totalDanger, o.totalDanger);
        }
    }

}
