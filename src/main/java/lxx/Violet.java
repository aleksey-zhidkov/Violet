package lxx;

import lxx.gun.GFGun;
import lxx.model.BattleState;
import lxx.model.BattleStateFactory;
import lxx.movement.WaveSurfingMovement;
import lxx.movement.orbital.AvoidEnemyOrbitalMovement;
import lxx.movement.orbital.OrbitalMovement;
import lxx.paint.Canvas;
import lxx.paint.LxxGraphics;
import lxx.services.DangerService;
import lxx.services.DataService;
import lxx.services.GFEnemyMovementLogService;
import lxx.strategy.*;
import lxx.utils.BattleRules;
import lxx.utils.LxxUtils;
import robocode.*;
import robocode.Event;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class Violet extends AdvancedRobot {

    public static final Color primaryColor = new Color(40, 6, 78);
    public static final Color secondaryColor = new Color(218, 177, 40);

    public static final Color primaryColor155 = new Color(40, 6, 78, 155);
    public static final Color secondaryColor155 = new Color(218, 177, 40, 155);

    private static final Map<String, Object> staticData = new HashMap<String, Object>();
    private final GFEnemyMovementLogService enemyLogService = new GFEnemyMovementLogService(staticData);

    private final List<DataService> services;

    private BattleState battleState;
    private BattleRules rules;
    private Strategy[] strategies;
    private TurnDecision turnDecision;

    public Violet() {
        this.services = LxxUtils.<DataService>asModifiableList(
                enemyLogService
        );
    }

    public void run() {
        if (getBattleFieldWidth() > 800 || getBattleFieldHeight() > 600) {
            System.out.println("Violet isn't support battle fields greater than 800x600");
            return;
        }
        if (getOthers() > 1) {
            System.out.println("Violet isn't support battles with more than 1 opponents");
            return;
        }

        setColors(primaryColor, new Color(28, 4, 52), secondaryColor,
                new Color(141, 0, 207), new Color(141, 0, 207));
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        final WaveSurfingMovement waveSurfingMovement =
                new WaveSurfingMovement(new DangerService(), new AvoidEnemyOrbitalMovement(new OrbitalMovement(battleState.rules.field, 800)));
        strategies = new Strategy[]{
                new FindEnemyStrategy(battleState),
                new DuelStrategy(waveSurfingMovement, new GFGun(enemyLogService)),
                new WinStrategy()
        };

        while (battleState.me.alive) {

            for (DataService service : services) {
                service.updateData(battleState);
            }

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
        final Vector<Event> allEvents = getAllEvents();
        battleState = BattleStateFactory.updateState(rules, battleState, se.getStatus(), allEvents, turnDecision);
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        if (e.getKeyChar() == 'w') {
            Canvas.WS.switchEnabled();
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        final LxxGraphics lg = new LxxGraphics(g);

        for (Canvas c : Canvas.values()) {
            c.exec(lg);
        }
    }
}
