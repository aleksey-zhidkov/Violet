package lxx.strategy;

import lxx.model.BattleState2;
import lxx.model.LxxRobot;
import robocode.Rules;

import static java.lang.Math.signum;
import static robocode.util.Utils.normalRelativeAngle;

public class FindEnemyStrategy implements Strategy {

    private final int turnDirection;

    public FindEnemyStrategy(BattleState2 bs) {
        turnDirection = (int) signum(normalRelativeAngle(bs.me.angleTo(bs.rules.field.center) - bs.me.radarHeading));
    }

    @Override
    public TurnDecision getTurnDecision(BattleState2 state) {
        if (!LxxRobot.UNKNOWN_ENEMY.equals(state.opponent.name) && state.time -state.opponent.lastScanTime < 3) {
            return null;
        }

        return new TurnDecision(
                0, Rules.MAX_TURN_RATE_RADIANS * turnDirection,
                Rules.GUN_TURN_RATE_RADIANS * turnDirection, 0D,
                Rules.RADAR_TURN_RATE_RADIANS * turnDirection);
    }
}
