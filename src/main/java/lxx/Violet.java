package lxx;

import lxx.gun.GFGun;
import lxx.model.BattleState;
import lxx.services.*;
import lxx.movement.WaveSurfingMovement;
import lxx.movement.orbital.AvoidEnemyOrbitalMovement;
import lxx.movement.orbital.OrbitalMovement;
import lxx.paint.Canvas;
import lxx.paint.LxxGraphics;
import lxx.strategy.*;
import lxx.utils.BattleRules;
import lxx.utils.Logger;
import lxx.utils.LxxConstants;
import lxx.utils.func.F1;
import lxx.utils.func.LxxCollections;
import lxx.utils.func.Option;
import robocode.*;
import robocode.Event;
import robocode.util.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.signum;

public class Violet extends AdvancedRobot {

    public static final Color primaryColor = new Color(40, 6, 78);
    public static final Color secondaryColor = new Color(218, 177, 40);
    public static final Color primaryColor155 = new Color(40, 6, 78, 155);

    private static final StaticDataStorage staticDataStorage = new StaticDataStorage();

    private final EventFilter scannedRobotEvent = new EventFilter(ScannedRobotEvent.class);
    private final EventFilter battleEndedEvent = new EventFilter(BattleEndedEvent.class);

    private BattleState battleState;
    private BattleRules rules;
    private Strategy[] strategies;
    private TurnDecision turnDecision;
    private BattleStateService battleStateService;

    public void run() {
        if (getOthers() > 1) {
            System.out.println("Violet isn't support battles with more than 1 opponents");
            return;
        }

        setColors(primaryColor, new Color(28, 4, 52), secondaryColor,
                new Color(141, 0, 207), new Color(141, 0, 207));
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (battleStateService == null) {
            setTurnRightRadians(Double.POSITIVE_INFINITY);
            setTurnGunRightRadians(Double.POSITIVE_INFINITY);
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            execute();
        }

        while (battleState.me.alive) {
            Logger.setTurn(getTime());
            doTurn();

            setDebugProperty("", MonitoringService.formatData());

            paint();
            execute();
        }
    }

    private void paint() {
        for (Canvas c : Canvas.values()) {
            c.exec(new LxxGraphics(getGraphics()));
        }
        Canvas.setPaintEnabled(false);
    }

    private void initContext(String opponentName, BattleRules rules) {
        final Context ctx = new Context(staticDataStorage, getName(), opponentName);
        battleStateService = new BattleStateService(ctx);

        final WaveSurfingMovement waveSurfingMovement =
                new WaveSurfingMovement(ctx.dangerService, new AvoidEnemyOrbitalMovement(new OrbitalMovement(rules.field, max(rules.field.width, rules.field.height))));

        strategies = new Strategy[]{
                new FindEnemyStrategy(),
                new DuelStrategy(waveSurfingMovement, new GFGun(ctx.enemyLogService)),
                new WinStrategy()
        };
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

        if (turnDecision.firePower == null || getGunHeat() > 0 || abs(getGunTurnRemaining()) > 1) {
            aimGun(turnDecision);
            return;
        }

        final Bullet firedBullet = setFireBullet(turnDecision.firePower);
        if (firedBullet != null) {
            addCustomEvent(new FireCondition(firedBullet));
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
        final Vector<Event> allEvents = getAllEvents();
        if (rules == null) {
            rules = new BattleRules(getBattleFieldWidth(), getBattleFieldHeight(), LxxConstants.ROBOT_SIDE_SIZE,
                    getGunCoolingRate(), getEnergy(), getName());
        }
        if (battleStateService == null) {
            final Option<Event> eventOption = LxxCollections.find(allEvents, scannedRobotEvent);
            if (eventOption.defined()) {
                initContext(((ScannedRobotEvent) eventOption.get()).getName(), rules);
                // todo: replace nulls with null objects
                battleState = new BattleState(rules, se.getTime(), null, null, null);
            } else {
                return;
            }
        }
        try {
            battleState = battleStateService.updateState(rules, battleState, se.getStatus(), allEvents, turnDecision);

            assert se.getTime() >= BattleRules.initialGunHeat / rules.gunCoolingRate || Utils.isNear(battleState.opponent.gunHeat, battleState.me.gunHeat)
                    : se.getTime() + ": " + battleState.me.gunHeat + ", " + battleState.opponent.gunHeat;

            MonitoringService.setRobot(battleState.me);
            MonitoringService.setRobot(battleState.opponent);

            if (LxxCollections.find(allEvents, battleEndedEvent).defined()) {
                System.out.println(MonitoringService.formatData());
            }
        } catch (RuntimeException t) {
            t.printStackTrace();
            throw t;
        }
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_5) {
            Logger.setLevel(Logger.DEBUG_LEVEL);
        } else if (e.getKeyChar() == KeyEvent.VK_4) {
            Logger.setLevel(Logger.INFO_LEVEL);
        } else if (e.getKeyChar() == KeyEvent.VK_3) {
            Logger.setLevel(Logger.WARN_LEVEL);
        } else if (e.getKeyChar() == KeyEvent.VK_2) {
            Logger.setLevel(Logger.ERR_LEVEL);
        } else if (e.getKeyChar() == KeyEvent.VK_1) {
            Logger.setLevel(Logger.OFF_LEVEL);
        } else {
            for (Canvas c : Canvas.values()) {
                if (c.getEnableSwitchKey() == e.getKeyChar()) {
                    c.switchEnabled();
                }
            }
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        Canvas.setPaintEnabled(true);
    }

    public class FireCondition extends Condition {

        public final Bullet bullet;

        public FireCondition(Bullet firedBullet) {
            this.bullet = firedBullet;
        }

        @Override
        public boolean test() {
            removeCustomEvent(this);
            return true;
        }
    }

    private static final class EventFilter<T extends Event> implements F1<Event, Boolean> {

        private final Class<T> clazz;

        private EventFilter(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Boolean f(Event event) {
            return clazz.isAssignableFrom(event.getClass());
        }
    }

}
