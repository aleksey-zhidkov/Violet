package lxx.strategy;

import lxx.model.BattleState;
import lxx.model.LxxRobot;
import lxx.movement.MovementDecision;
import lxx.movement.WaveSurfingMovement;
import lxx.utils.LxxConstants;
import robocode.util.Utils;

import static java.lang.Math.signum;

public class DuelStrategy implements Strategy {

    private final WaveSurfingMovement waveSurfingMovement;

    public DuelStrategy(WaveSurfingMovement waveSurfingMovement) {
        this.waveSurfingMovement = waveSurfingMovement;
    }

    @Override
    public TurnDecision getTurnDecision(BattleState battleState) {
        if (!battleState.enemy.alive && battleState.getEnemyBullets().size() == 0 ||
                LxxRobot.UNKNOWN_ENEMY.equals(battleState.enemy.name)) {
            return null;
        }

        final MovementDecision md = waveSurfingMovement.getMovementDecision(battleState);

        return new TurnDecision(md.desiredVelocity, md.turnRate,
                Utils.normalRelativeAngle(battleState.me.angleTo(battleState.enemy) - battleState.me.gunHeading),
                1.95, getRadarTurnAngleRadians(battleState));
    }

    public double getRadarTurnAngleRadians(BattleState battleState) {
        final double angleToTarget = battleState.me.angleTo(battleState.enemy);
        final double sign = (angleToTarget != battleState.me.radarHeading)
                ? signum(Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading))
                : 1;

        return Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading + LxxConstants.RADIANS_5 * sign);
    }


}
