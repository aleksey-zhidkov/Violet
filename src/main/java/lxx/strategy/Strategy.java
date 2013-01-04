package lxx.strategy;

import lxx.model.BattleState;

public interface Strategy {

    TurnDecision getTurnDecision(BattleState state);

}
