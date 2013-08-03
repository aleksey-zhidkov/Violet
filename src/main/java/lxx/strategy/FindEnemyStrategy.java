package lxx.strategy;

import lxx.model.BattleState;
import robocode.Rules;

import static java.lang.Math.signum;
import static robocode.util.Utils.normalRelativeAngle;

public class FindEnemyStrategy implements Strategy {

    @Override
    public TurnDecision getTurnDecision(BattleState bs) {
        if (!bs.opponent.alive || bs.time - bs.opponent.lastScanTime < 3) {
            return null;
        }

        final double turnDirection = (int) signum(normalRelativeAngle(bs.me.angleTo(bs.opponent.position) - bs.me.radarHeading));

        return new TurnDecision(
                0, Rules.MAX_TURN_RATE_RADIANS * turnDirection,
                Rules.GUN_TURN_RATE_RADIANS * turnDirection, 0D,
                Rules.RADAR_TURN_RATE_RADIANS * turnDirection);
    }
}
