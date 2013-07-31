package lxx.strategy;

import lxx.model.BattleState2;
import lxx.utils.LxxConstants;
import robocode.util.Utils;

import static java.lang.Math.abs;

public class WinStrategy implements Strategy {

    private Long winTime = -1L;
    private static final double PARADE_HEADING = LxxConstants.RADIANS_90;

    @Override
    public TurnDecision getTurnDecision(BattleState2 bs) {
        boolean match = bs.opponentBulletsInAir.size() == 0 && !bs.opponent.alive;

        if (match && winTime == -1L) {
            winTime = bs.time;
        }

        return new TurnDecision(0, getTurnRemaining(bs),
                Utils.normalRelativeAngle(-bs.me.gunHeading), 0.1,
                Utils.normalRelativeAngle(-bs.me.radarHeading));
    }

    public double getTurnRemaining(BattleState2 bs) {
        double turnRemaining = Utils.normalRelativeAngle(PARADE_HEADING - bs.me.heading);
        if (abs(turnRemaining) > LxxConstants.RADIANS_90) {
            turnRemaining = Utils.normalRelativeAngle(PARADE_HEADING - Utils.normalAbsoluteAngle(bs.me.heading + Math.PI));
        }
        return turnRemaining;
    }

}
