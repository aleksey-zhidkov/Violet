package lxx.strategy;

import lxx.gun.Gun;
import lxx.model.BattleState;
import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.movement.MovementDecision;
import lxx.movement.WaveSurfingMovement;
import lxx.paint.Canvas;
import lxx.utils.LxxConstants;
import lxx.utils.LxxPoint;
import lxx.utils.LxxUtils;
import lxx.utils.func.F1;
import lxx.utils.func.LxxCollections;
import robocode.Rules;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.signum;
import static lxx.utils.LxxUtils.anglesDiff;
import static robocode.util.Utils.normalRelativeAngle;

public class DuelStrategy implements Strategy {

    public static final int FLIGHT_TIME_THRESHOLD = 2;
    private final Map<LxxWave, LxxPoint> wavesDestinations = new HashMap<LxxWave, LxxPoint>();

    private final WaveSurfingMovement waveSurfingMovement;
    private final Gun gun;

    public DuelStrategy(WaveSurfingMovement waveSurfingMovement, Gun gun) {
        this.waveSurfingMovement = waveSurfingMovement;
        this.gun = gun;
    }



    @Override
    public TurnDecision getTurnDecision(BattleState battleState) {
        final MovementDecision md = getMovementDecision(battleState);
        if (md == null) return null;

        final double bulletPower = 1.95;
        return new TurnDecision(md.desiredVelocity, md.turnRate,
                gun.getGunTurnAngle(battleState, Rules.getBulletSpeed(bulletPower)),
                bulletPower, getRadarTurnAngleRadians(battleState));
    }

    private MovementDecision getMovementDecision(BattleState battleState) {
        final List<LxxWave> bulletsInAir = getWaves(battleState);
        if (bulletsInAir == null) return null;

        final LxxWave firstWave = bulletsInAir.get(0);
        if (!wavesDestinations.containsKey(firstWave)) {
            wavesDestinations.put(firstWave, waveSurfingMovement.getMovementDecision(battleState, bulletsInAir, FLIGHT_TIME_THRESHOLD));
        }

        final LxxPoint destination = wavesDestinations.get(firstWave);

        final double desiredHeading = battleState.me.angleTo(destination);
        final double distanceRemaining = battleState.me.distance(destination);
        if (distanceRemaining < LxxUtils.getStopDistance(battleState.me.speed) || distanceRemaining < Rules.MAX_VELOCITY) {
            return new MovementDecision(0, 0);
        } else {
            final boolean wantToGoFront = anglesDiff(battleState.me.heading, desiredHeading) < LxxConstants.RADIANS_90;
            final double normalizedDesiredHeading = wantToGoFront ? desiredHeading : Utils.normalAbsoluteAngle(desiredHeading + LxxConstants.RADIANS_180);

            final double turnRemaining = normalRelativeAngle(normalizedDesiredHeading - battleState.me.heading);
            return new MovementDecision(Rules.MAX_VELOCITY * (wantToGoFront ? 1 : -1), turnRemaining);
        }
    }

    private List<LxxWave> getWaves(BattleState battleState) {
        List<LxxWave> bulletsInAir = new ArrayList<LxxWave>(battleState.opponent.bulletsInAir);
        if (!battleState.opponent.alive && bulletsInAir.isEmpty() ||
                LxxRobot.UNKNOWN.equals(battleState.opponent.name)) {
            return null;
        }

        if (bulletsInAir.size() > 1) {
            bulletsInAir = LxxCollections.filter(bulletsInAir, flightTimeLessThan(battleState.me, FLIGHT_TIME_THRESHOLD));
        }

        final boolean isFirstWaveImaginary = bulletsInAir.isEmpty();
        if (bulletsInAir.size() < 2) {
            bulletsInAir.add(new LxxWave(battleState.opponent, battleState.me, Rules.getBulletSpeed(3),
                    (long) Math.ceil(battleState.time + battleState.opponent.gunHeat / battleState.rules.gunCoolingRate), isFirstWaveImaginary));
        }

        waveSurfingMovement.getDangerService().getWaveDangerInfo(bulletsInAir.get(0), isFirstWaveImaginary).draw(Canvas.WS_WAVES, battleState.time);

        return bulletsInAir;
    }

    public double getRadarTurnAngleRadians(BattleState battleState) {
        final double angleToTarget = battleState.me.angleTo(battleState.opponent);
        final double sign = (angleToTarget != battleState.me.radarHeading)
                ? signum(Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading))
                : 1;

        return Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading + LxxConstants.RADIANS_10 * sign);
    }

    private static F1<LxxWave, Boolean> flightTimeLessThan(final LxxRobot robot, final int flightTimeThreshold) {
        return new F1<LxxWave, Boolean>() {
            @Override
            public Boolean f(LxxWave wave) {
                return wave.getFlightTime(robot) > flightTimeThreshold;
            }
        };
    }

}
