package lxx.strategy;

import lxx.model.BattleState2;

public interface Strategy {

    TurnDecision getTurnDecision(BattleState2 state);

}
