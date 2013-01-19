package lxx.strategy;

import lxx.model.BattleState;
import lxx.utils.LxxConstants;
import robocode.util.Utils;

import static java.lang.Math.*;

public class WinStrategy implements Strategy {

    private Long winTime = -1L;
    private static final double PARADE_HEADING = LxxConstants.RADIANS_90;

    public double getTurnRemaining(BattleState bs) {
        double turnRemaining = Utils.normalRelativeAngle(PARADE_HEADING - bs.me.heading);
        if (abs(turnRemaining) > LxxConstants.RADIANS_90) {
            turnRemaining = Utils.normalRelativeAngle(PARADE_HEADING - Utils.normalAbsoluteAngle(bs.me.heading + Math.PI));
        }
        return turnRemaining;
    }

    @Override
    public TurnDecision getTurnDecision(BattleState bs) {
        boolean match = bs.time > 10 && bs.getEnemyBulletsInAir().size() == 0 && !bs.enemy.alive;

        if (match && winTime == -1L) {
            winTime = bs.time;
        }

        return new TurnDecision(0, getTurnRemaining(bs),
                Utils.normalRelativeAngle(-bs.me.gunHeading), 0.1,
                Utils.normalRelativeAngle(-bs.me.radarHeading));
    }
}
