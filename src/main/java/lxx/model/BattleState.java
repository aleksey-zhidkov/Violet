package lxx.model;

import lxx.utils.BattleRules;

public class BattleState {

    public final long time;

    public final BattleRules rules;

    public final BattleState prevState;

    public final LxxRobot me;
    public final LxxRobot opponent;

    public BattleState(BattleRules rules, long time, BattleState prevState, LxxRobot me, LxxRobot opponent) {
        this.rules = rules;
        this.time = time;
        this.prevState = prevState;

        this.me = me;
        this.opponent = opponent;
    }

    public LxxRobot getRobot(String robotName) {
        if (robotName.equals(me.name)) {
            return me;
        } else if (robotName.equals(opponent.name)) {
            return opponent;
        }

        throw new IllegalArgumentException("Unknown robot: " + robotName);
    }

}
