package lxx;

import lxx.model.BattleState;
import lxx.model.BattleStateFactory;
import lxx.movement.WaveSurfingMovement;
import lxx.movement.orbital.AvoidEnemyOrbitalMovement;
import lxx.movement.orbital.OrbitalMovement;
import lxx.services.DangerService;
import lxx.strategy.*;
import lxx.utils.BattleRules;
import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.Condition;
import robocode.StatusEvent;

import java.awt.*;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class Violet extends AdvancedRobot {

    private BattleState battleState;
    private BattleRules rules;
    private Strategy[] strategies;
    private TurnDecision turnDecision;

    public void run() {
        if (getBattleFieldWidth() > 800 || getBattleFieldHeight() > 600) {
            System.out.println("Violet isn't support battle fields greater than 800x600");
            return;
        }
        if (getOthers() > 1) {
            System.out.println("Violet isn't support battles with more than 1 opponents");
            return;
        }

        setColors(new Color(40, 6, 78), new Color(28, 4, 52), new Color(218, 177, 40),
                new Color(141, 0, 207), new Color(141, 0, 207));
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        final WaveSurfingMovement waveSurfingMovement =
                new WaveSurfingMovement(new DangerService(), new AvoidEnemyOrbitalMovement(new OrbitalMovement(battleState.rules.field, 800)));
        strategies = new Strategy[]{
                new FindEnemyStrategy(battleState),
                new DuelStrategy(waveSurfingMovement),
                new WinStrategy()
        };

        while (battleState.me.alive) {

            doTurn();

            execute();
        }
    }

    private void doTurn() {
        for (Strategy s : strategies) {
            turnDecision = s.getTurnDecision(battleState);
            if (turnDecision == null) {
                continue;
            }

            if (turnDecision.gunTurnRate != null) {
                handleGun(turnDecision);
            }
            move(turnDecision);
            if (turnDecision.radarTurnRate != null) {
                turnRadar(turnDecision);
            }
            break;
        }
    }

    private void turnRadar(TurnDecision turnDecision) {
        setTurnRadarRightRadians(turnDecision.radarTurnRate);
    }

    private void handleGun(TurnDecision turnDecision) {

        if (getGunHeat() == 0) {
            if (abs(getGunTurnRemaining()) > 1) {
                System.out.printf("[WARN] gun turn remaining is %3.2f when gun is cool\n", getGunTurnRemaining());
            } else if (turnDecision.firePower != null) {
                setFireBullet(turnDecision.firePower);
            } else {
                aimGun(turnDecision);
            }
        } else {
            aimGun(turnDecision);
        }
    }

    private void aimGun(TurnDecision turnDecision) {
        setTurnGunRightRadians(turnDecision.gunTurnRate);
    }

    private void move(TurnDecision turnDecision) {
        setTurnRightRadians(turnDecision.turnRate);
        setAhead(100 * signum(turnDecision.desiredVelocity));
    }

    @Override
    public void onStatus(StatusEvent se) {
        if (rules == null) {
            rules = new BattleRules(getBattleFieldWidth(), getBattleFieldHeight(), getWidth(),
                    getGunHeat(), getGunCoolingRate(), getEnergy(), getName());
        }
        battleState = BattleStateFactory.updateState(rules, battleState, se.getStatus(), getAllEvents(), turnDecision);
    }

}
