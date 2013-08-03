package lxx.strategy;

import lxx.gun.Gun;
import lxx.model.BattleState;
import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.movement.MovementDecision;
import lxx.movement.WaveSurfingMovement;
import lxx.utils.LxxConstants;
import robocode.Rules;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.signum;

public class DuelStrategy implements Strategy {

    private final WaveSurfingMovement waveSurfingMovement;
    private final Gun gun;

    public DuelStrategy(WaveSurfingMovement waveSurfingMovement, Gun gun) {
        this.waveSurfingMovement = waveSurfingMovement;
        this.gun = gun;
    }

    @Override
    public TurnDecision getTurnDecision(BattleState battleState) {
        final List<LxxWave> bulletsInAir = new ArrayList<LxxWave>(battleState.opponent.bulletsInAir);
        if (!battleState.opponent.alive && bulletsInAir.isEmpty() ||
                LxxRobot.UNKNOWN.equals(battleState.opponent.name)) {
            return null;
        }

        if (bulletsInAir.size() < 2) {
            bulletsInAir.add(new LxxWave(battleState.opponent, battleState.me, Rules.getBulletSpeed(3), battleState.time));
        }
        final MovementDecision md = waveSurfingMovement.getMovementDecision(battleState, bulletsInAir);

        final double bulletPower = 1.95;
        return new TurnDecision(md.desiredVelocity, md.turnRate,
                gun.getGunTurnAngle(battleState, Rules.getBulletSpeed(bulletPower)),
                bulletPower, getRadarTurnAngleRadians(battleState));
    }

    public double getRadarTurnAngleRadians(BattleState battleState) {
        final double angleToTarget = battleState.me.angleTo(battleState.opponent);
        final double sign = (angleToTarget != battleState.me.radarHeading)
                ? signum(Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading))
                : 1;

        return Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading + LxxConstants.RADIANS_10 * sign);
    }


}
