package lxx.strategy;

import lxx.model.BattleState;
import lxx.utils.LxxConstants;
import robocode.util.Utils;

import static java.lang.Math.abs;

public class WinStrategy implements Strategy {

    private static final double PARADE_HEADING = LxxConstants.RADIANS_90;

    @Override
    public TurnDecision getTurnDecision(BattleState bs) {
        if (bs.opponent.bulletsInAir.size() > 0 || bs.opponent.alive) {
            return null;
        }

        return new TurnDecision(0, getTurnRemaining(bs),
                Utils.normalRelativeAngle(-bs.me.gunHeading), 0.1,
                Utils.normalRelativeAngle(-bs.me.radarHeading));
    }

    public double getTurnRemaining(BattleState bs) {
        double turnRemaining = Utils.normalRelativeAngle(PARADE_HEADING - bs.me.heading);
        if (abs(turnRemaining) > LxxConstants.RADIANS_90) {
            turnRemaining = Utils.normalRelativeAngle(PARADE_HEADING - Utils.normalAbsoluteAngle(bs.me.heading + Math.PI));
        }
        return turnRemaining;
    }

}
