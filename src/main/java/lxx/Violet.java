package lxx;

import ags.utils.KdTree;
import lxx.gun.GFGun;
import lxx.logs.KdTreeMovementLog;
import lxx.logs.SimpleLocationFactory;
import lxx.model.BattleState2;
import lxx.model.BattleStateFactory2;
import lxx.model.LxxBullet2;
import lxx.model.LxxWave2;
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
import lxx.utils.LxxConstants;
import lxx.utils.LxxUtils;
import lxx.utils.func.Option;
import robocode.*;
import robocode.Event;

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

    private BattleState2 battleState2;
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
                new FindEnemyStrategy(battleState2)
        };
        services = new ArrayList<DataService>(0);

        while (battleState2.me.alive) {

            if (battleState2.opponent.known() && services.size() == 0) {
                initStrategies();
            }

            for (DataService service : services) {
                service.updateData(battleState2);
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

        final GFMovementLogServiceImpl enemyLogService = new GFMovementLogServiceImpl(enemySimpleMovementLog, battleState2.me.name, battleState2.opponent.name);
        final DangerService dangerService = new DangerService(mySimpleMovementLog);

        final WaveSurfingMovement waveSurfingMovement =
                new WaveSurfingMovement(dangerService, new AvoidEnemyOrbitalMovement(new OrbitalMovement(battleState2.rules.field, 800)));

        strategies = new Strategy[]{
                new FindEnemyStrategy(battleState2),
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
            turnDecision = s.getTurnDecision(battleState2);
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
                addCustomEvent(new FireCondition(firedBullet));
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
            rules = new BattleRules(getBattleFieldWidth(), getBattleFieldHeight(), LxxConstants.ROBOT_SIDE_SIZE,
                    se.getStatus().getGunHeat(), getGunCoolingRate(), se.getStatus().getEnergy(), getName());
            battleState2 = new BattleState2(rules, se.getTime(), null,
                    null, null,
                    Option.<LxxWave2>none(), Option.<LxxWave2>none(),
                    Collections.<LxxWave2>emptyList(), Collections.<LxxWave2>emptyList(),
                    Collections.<LxxBullet2>emptyList(), Collections.<LxxBullet2>emptyList(),
                    Collections.<LxxBullet2>emptyList(), Collections.<LxxBullet2>emptyList(),
                    Collections.<LxxWave2>emptyList(), Collections.<LxxWave2>emptyList());
        }
        final Vector<Event> allEvents = getAllEvents();
        final Vector<Event> allEventsCp = new Vector<Event>(allEvents);
        allEventsCp.add(se);
        allEventsList.add(allEventsCp);
        try {
            battleState2 = BattleStateFactory2.updateState(rules, battleState2, se.getStatus(), allEvents, turnDecision);
        } catch (RuntimeException t) {
            t.printStackTrace();
            throw t;
        }
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
}
