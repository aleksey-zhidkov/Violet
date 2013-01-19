package lxx;

import ags.utils.KdTree;
import lxx.gun.GFGun;
import lxx.logs.KdTreeMovementLog;
import lxx.logs.SimpleLocationFactory;
import lxx.model.BattleState;
import lxx.model.BattleStateFactory;
import lxx.model.LxxRobot;
import lxx.movement.WaveSurfingMovement;
import lxx.movement.orbital.AvoidEnemyOrbitalMovement;
import lxx.movement.orbital.OrbitalMovement;
import lxx.paint.Canvas;
import lxx.paint.LxxGraphics;
import lxx.services.DangerService;
import lxx.services.DataService;
import lxx.services.GFMovementLogServiceImpl;
import lxx.services.MonitoringService;
import lxx.strategy.*;
import lxx.utils.BattleRules;
import lxx.utils.GuessFactor;
import lxx.utils.LxxUtils;
import robocode.*;
import robocode.Event;
import robocode.util.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class Violet extends AdvancedRobot {

    private final List<Vector<Event>> allEventsList = new ArrayList<Vector<Event>>();

    public static final Color primaryColor = new Color(40, 6, 78);
    public static final Color secondaryColor = new Color(218, 177, 40);

    public static final Color primaryColor155 = new Color(40, 6, 78, 155);
    public static final Color secondaryColor155 = new Color(218, 177, 40, 155);

    public static final String ENEMY_MOVEMENT_SIMPLE = "enemy.movement.simple";
    public static final String MY_MOVEMENT_SIMPLE = "my.movement.simple";

    private static final SimpleLocationFactory simpleLocFactory = new SimpleLocationFactory();

    private static final Map<String, Object> staticData = new HashMap<String, Object>() {{
        put(ENEMY_MOVEMENT_SIMPLE, new KdTree.SqrEuclid<GuessFactor>(simpleLocFactory.getDimensionCount(), Integer.MAX_VALUE));
        put(MY_MOVEMENT_SIMPLE, new KdTree.SqrEuclid<GuessFactor>(simpleLocFactory.getDimensionCount(), Integer.MAX_VALUE));
    }};

    private List<DataService> services;

    private BattleState battleState;
    private BattleRules rules;
    private Strategy[] strategies;
    private TurnDecision turnDecision;
    private Bullet firedBullet;

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

        strategies = new Strategy[]{
                new FindEnemyStrategy(battleState)
        };
        services = new ArrayList<DataService>(0);

        while (battleState.me.alive) {

            if (!LxxRobot.UNKNOWN_ENEMY.equals(battleState.enemy.name) && services.size() == 0) {
                initStrategies();
            }

            for (DataService service : services) {
                service.updateData(battleState);
            }

            doTurn();

            setDebugProperty("", MonitoringService.formatData());

            Canvas.setPaintEnabled(false);
            execute();
        }
    }

    private void initStrategies() {
        final KdTreeMovementLog<GuessFactor> enemySimpleMovementLog =
                new KdTreeMovementLog<GuessFactor>((KdTree<GuessFactor>) staticData.get(ENEMY_MOVEMENT_SIMPLE), simpleLocFactory);
        final KdTreeMovementLog<GuessFactor> mySimpleMovementLog =
                new KdTreeMovementLog<GuessFactor>((KdTree<GuessFactor>) staticData.get(MY_MOVEMENT_SIMPLE), simpleLocFactory);

        final GFMovementLogServiceImpl enemyLogService = new GFMovementLogServiceImpl(enemySimpleMovementLog, battleState.me.name, battleState.enemy.name);
        final DangerService dangerService = new DangerService(mySimpleMovementLog);

        final WaveSurfingMovement waveSurfingMovement =
                new WaveSurfingMovement(dangerService, new AvoidEnemyOrbitalMovement(new OrbitalMovement(battleState.rules.field, 800)));

        strategies = new Strategy[]{
                new FindEnemyStrategy(battleState),
                new DuelStrategy(waveSurfingMovement, new GFGun(enemyLogService)),
                new WinStrategy()
        };

        services = LxxUtils.asModifiableList(
                enemyLogService,
                dangerService
        );
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
        firedBullet = null;
        if (getGunHeat() == 0) {
            if (abs(getGunTurnRemaining()) > 1) {
            } else if (turnDecision.firePower != null) {
                firedBullet = setFireBullet(turnDecision.firePower);
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
                    se.getStatus().getGunHeat(), getGunCoolingRate(), se.getStatus().getEnergy(), getName());
        }
        final Vector<Event> allEvents = getAllEvents();
        final Vector<Event> allEventsCp = new Vector<Event>(allEvents);
        allEventsCp.add(se);
        allEventsList.add(allEventsCp);
        final BattleState newBattleState = BattleStateFactory.updateState(rules, battleState, se.getStatus(), allEvents, turnDecision);

        if (newBattleState.time != se.getTime()) {
            BattleStateFactory.updateState(rules, battleState, se.getStatus(), allEvents, turnDecision);
        }

        if (firedBullet == null ^ newBattleState.getRobotFiredBullet(newBattleState.me.name) == null ||
                firedBullet != null ^ newBattleState.getRobotFiredBullet(newBattleState.me.name) != null) {
            BattleStateFactory.updateState(rules, battleState, se.getStatus(), allEvents, turnDecision);
        }

        if (!Utils.isNear(se.getStatus().getGunHeat(), newBattleState.me.gunHeat)) {
            BattleStateFactory.updateState(rules, battleState, se.getStatus(), allEvents, turnDecision);
        }

        assert newBattleState.time == se.getTime();
        assert newBattleState.me.time == se.getTime();
        assert !newBattleState.me.alive || Utils.isNear(se.getStatus().getGunHeat(), newBattleState.me.gunHeat);
        assert !newBattleState.me.alive || (firedBullet == null && newBattleState.getRobotFiredBullet(newBattleState.me.name) == null ||
                firedBullet != null && newBattleState.getRobotFiredBullet(newBattleState.me.name) != null);

        battleState = newBattleState;
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        if (e.getKeyChar() == 'w') {
            Canvas.WS.switchEnabled();
        } else if (e.getKeyChar() == 'b') {
            Canvas.BATTLE_STATE.switchEnabled();
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        Canvas.setPaintEnabled(true);
        final LxxGraphics lg = new LxxGraphics(g);

        for (Canvas c : Canvas.values()) {
            c.exec(lg);
        }
    }
}
