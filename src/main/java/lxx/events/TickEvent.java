package lxx.events;

import lxx.model.BattleState;

public class TickEvent implements LxxEvent {

    public final BattleState battleState;

    public TickEvent(BattleState battleState) {
        this.battleState = battleState;
    }
}
