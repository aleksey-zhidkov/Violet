package lxx.model;

import lxx.paint.Canvas;
import lxx.paint.Circle;
import lxx.utils.*;
import robocode.Bullet;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.signum;

public class BattleState {

    private static final List<LxxWave> allMyBullets = new ArrayList<LxxWave>();
    private static final List<LxxWave> allEnemyBullets = new ArrayList<LxxWave>();

    private final WavesState myBullets;
    private final WavesState enemyBullets;

    public final BattleRules rules;
    public final LxxRobot me;
    public final LxxRobot enemy;
    public final long time;
    public final BattleState prevState;

    public BattleState(final BattleState prev, LxxRobot me, LxxRobot enemy) {
        this.prevState = prev;
        this.time = me.time;
        this.rules = prev.rules;
        this.me = me;
        this.enemy = enemy;

        myBullets = updateWaves(prev.myBullets.inAir, prev.me, prev.enemy, me, enemy);
        if (myBullets.firedWave != null) {
            allMyBullets.add(myBullets.firedWave);
        }

        enemyBullets = updateWaves(prev.enemyBullets.inAir, prev.enemy, prev.me, enemy, me);
        if (enemyBullets.firedWave != null) {
            allEnemyBullets.add(enemyBullets.firedWave);
        }
    }

    public BattleState(BattleRules battleRules, LxxRobot me, LxxRobot enemy) {
        this.prevState = null;
        this.time = me.time;
        this.rules = battleRules;
        this.me = me;
        this.enemy = enemy;

        myBullets = new WavesState(null, Collections.<LxxBullet>emptyList(), Collections.<LxxBullet>emptyList(),
                Collections.<LxxWave>emptyList(), Collections.<LxxWave>emptyList());
        enemyBullets = new WavesState(null, Collections.<LxxBullet>emptyList(), Collections.<LxxBullet>emptyList(),
                Collections.<LxxWave>emptyList(), Collections.<LxxWave>emptyList());
    }

    public List<LxxWave> getEnemyBullets(final APoint pnt, double flightTimeLimit, int cnt) {
        final ArrayList<LxxWave> lst = new ArrayList<LxxWave>();

        for (LxxWave bullet : enemyBullets.inAir) {
            if ((bullet.aDistance(pnt) - (time - bullet.time) * bullet.speed) / bullet.speed > flightTimeLimit) {
                lst.add(bullet);
                if (lst.size() == cnt) {
                    break;
                }
            }
        }

        Collections.sort(lst, new Comparator<LxxWave>() {
            public int compare(LxxWave o1, LxxWave o2) {
                final double o1FlightTime = (o1.aDistance(pnt) - (time - o1.time) * o1.speed) / o1.speed;
                final double o2FlightTime = (o2.aDistance(pnt) - (time - o2.time) * o2.speed) / o2.speed;
                return (int) signum(o1FlightTime - o2FlightTime);
            }
        });

        return lst;
    }

    private static WavesState updateWaves(List<LxxWave> robotBullets,
                                          LxxRobot oldRobot, LxxRobot oldOpponent,
                                          LxxRobot robot, LxxRobot opponent) {
        assert !robot.alive || robotBullets.size() >= robot.hitBullets.size() + robot.interceptedBullets.size();

        final ArrayList<LxxWave> goneWaves = new ArrayList<LxxWave>();
        final ArrayList<LxxWave> inAirWaves = new ArrayList<LxxWave>();
        final ArrayList<LxxBullet> hitBullets = new ArrayList<LxxBullet>();
        final ArrayList<LxxBullet> interceptedBullets = new ArrayList<LxxBullet>();

        LxxWave firedWave = null;
        if (robot.firePower > 0) {
            firedWave = new LxxWave(oldRobot, oldOpponent, Rules.getBulletSpeed(robot.firePower), oldRobot.time);
            inAirWaves.add(firedWave);
        }

        for (LxxWave w : robotBullets) {
            LxxBullet lxxBullet = null;
            for (final Bullet bullet : robot.hitBullets) {
                if (abs(w.launcher.distance(bullet.getX(), bullet.getY()) -
                        (oldRobot.time - w.time) * w.speed) < w.speed + 0.1 && Utils.isNear(w.speed, bullet.getVelocity())) {
                    lxxBullet = new LxxBullet(w, bullet);
                    hitBullets.add(lxxBullet);
                }
            }
            for (final Bullet bullet : robot.interceptedBullets) {
                if (abs(w.launcher.distance(bullet.getX(), bullet.getY()) -
                        (oldRobot.time - w.time) * w.speed) < w.speed + 0.1 && Utils.isNear(w.speed, bullet.getVelocity())) {
                    lxxBullet = new LxxBullet(w, bullet);
                    interceptedBullets.add(lxxBullet);
                }
            }

            if (lxxBullet == null) {
                // todo (azhidkov): make check more precise
                if (w.isPassed(opponent)) {
                    w.isPassed(opponent);
                    goneWaves.add(w);
                    Canvas.BATTLE_STATE.draw(new Circle(w.launcher, w.getTraveledDistance(opponent.time)), Color.RED);
                } else {
                    inAirWaves.add(w);
                }
            }
        }

        if (robot.hitBullets.size() != hitBullets.size() || robot.interceptedBullets.size() != interceptedBullets.size()) {
            assert !robot.alive || robot.hitBullets.size() == hitBullets.size();
            assert !robot.alive || robot.interceptedBullets.size() == interceptedBullets.size();
        }

        for (LxxWave w : inAirWaves) {
            Canvas.BATTLE_STATE.draw(new Circle(w.launcher, w.getTraveledDistance(opponent.time)), new Color(255, 255, 255, 155));
        }

        return new WavesState(firedWave, Collections.unmodifiableList(hitBullets), Collections.unmodifiableList(interceptedBullets),
                Collections.unmodifiableList(goneWaves), Collections.unmodifiableList(inAirWaves));
    }

    private static List<LxxWave> findOldWave(List<LxxWave> waves, Bullet bullet, long time) {
        final List<LxxWave> res = new ArrayList<LxxWave>();
        for (LxxWave w : waves) {
            if (Utils.isNear(w.speed, bullet.getVelocity()) &&
                    abs(w.launcher.distance(bullet.getX(), bullet.getY()) -
                            (time - w.time) * w.speed) < w.speed + 0.1) {
                res.add(w);
            }
        }

        return res;
    }

    private static BattleState findFireTime(Bullet bullet, String owner, BattleState current) {
        BattleState bs = current;
        LxxPoint bltPos = new LxxPoint(bullet.getX(), bullet.getY());
        final double bltReverseDir = Utils.normalAbsoluteAngle(bullet.getHeadingRadians() + LxxConstants.RADIANS_180);
        while (true) {
            double dist = bltPos.distance(bs.getRobot(owner));
            if (LxxUtils.getBoundingRectangleAt(bs.getRobot(owner)).contains(bltPos)) {
                return bs;
            }

            bltPos = bltPos.project(bltReverseDir, bullet.getVelocity());
            bs = bs.prevState;
            double newDist = bltPos.distance(bs.getRobot(owner));

            if (newDist > dist) {
                return null;
            }
        }
    }

    public Collection<LxxWave> getMyBulletsInAir() {
        return myBullets.inAir;
    }

    public Collection<LxxWave> getEnemyBulletsInAir() {
        return enemyBullets.inAir;
    }

    public LxxRobot getRobot(String robotName) {
        return me.name.equals(robotName) ? me : enemy;
    }

    public LxxWave getRobotFiredBullet(String robotName) {
        return me.name.equals(robotName) ? myBullets.firedWave : enemyBullets.firedWave;
    }

    public List<LxxBullet> getMyHitBullets() {
        return myBullets.hitBullets;
    }

    public List<LxxBullet> getEnemyHitBullets() {
        return enemyBullets.hitBullets;
    }

    public List<LxxWave> getEnemyGoneWaves() {
        return enemyBullets.goneWaves;
    }

    public List<LxxBullet> getEnemyInterceptedBullets() {
        return enemyBullets.interceptedBullets;
    }

    public List<LxxBullet> getMyInterceptedBullets() {
        return myBullets.interceptedBullets;
    }

    public static class WavesState {

        public final LxxWave firedWave;
        public final List<LxxBullet> hitBullets;
        public final List<LxxBullet> interceptedBullets;
        public final List<LxxWave> goneWaves;
        public final List<LxxWave> inAir;

        public WavesState(LxxWave firedWave, List<LxxBullet> hitBullets, List<LxxBullet> interceptedBullets, List<LxxWave> goneWaves, List<LxxWave> inAir) {
            this.firedWave = firedWave;
            this.hitBullets = hitBullets;
            this.interceptedBullets = interceptedBullets;
            this.goneWaves = goneWaves;
            this.inAir = inAir;
        }

        public int size() {
            return hitBullets.size() + interceptedBullets.size() + goneWaves.size() + inAir.size();
        }
    }

}
