package lxx.strategy;

import lxx.gun.Gun;
import lxx.model.BattleState2;
import lxx.model.LxxRobot;
import lxx.movement.MovementDecision;
import lxx.movement.WaveSurfingMovement;
import lxx.utils.LxxConstants;
import robocode.Rules;
import robocode.util.Utils;

import static java.lang.Math.signum;

public class DuelStrategy implements Strategy {

    private final WaveSurfingMovement waveSurfingMovement;
    private final Gun gun;

    public DuelStrategy(WaveSurfingMovement waveSurfingMovement, Gun gun) {
        this.waveSurfingMovement = waveSurfingMovement;
        this.gun = gun;
    }

    @Override
    public TurnDecision getTurnDecision(BattleState2 battleState) {
        if (!battleState.opponent.alive && battleState.opponentBulletsInAir.size() == 0 ||
                LxxRobot.UNKNOWN_ENEMY.equals(battleState.opponent.name)) {
            return null;
        }

        final MovementDecision md = waveSurfingMovement.getMovementDecision(battleState);

        final double bulletPower = 1.95;
        return new TurnDecision(md.desiredVelocity, md.turnRate,
                gun.getGunTurnAngle(battleState, Rules.getBulletSpeed(bulletPower)),
                bulletPower, getRadarTurnAngleRadians(battleState));
    }

    public double getRadarTurnAngleRadians(BattleState2 battleState) {
        final double angleToTarget = battleState.me.angleTo(battleState.opponent);
        final double sign = (angleToTarget != battleState.me.radarHeading)
                ? signum(Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading))
                : 1;

        return Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading + LxxConstants.RADIANS_10 * sign);
    }


}
