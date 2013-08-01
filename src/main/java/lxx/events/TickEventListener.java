package lxx.events;

import lxx.model.BattleState;

public interface TickEventListener {

    void onTick(BattleState battleState);

}
