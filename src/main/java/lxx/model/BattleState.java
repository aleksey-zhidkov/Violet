package lxx.model;

import lxx.utils.*;
import lxx.utils.func.F1;
import lxx.utils.func.LxxCollections;
import robocode.Bullet;
import robocode.Rules;

import java.util.*;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class BattleState {

    private final List<LxxWave> myBullets;
    private final List<LxxWave> enemyBullets;

    public final BattleRules rules;
    public final LxxRobot me;
    public final LxxRobot enemy;
    public final long time;
    public final BattleState prevState;
    public final LxxWave myFiredBullet;
    public final LxxWave enemyFiredBullet;

    public BattleState(final BattleState prev, LxxRobot me, LxxRobot enemy) {
        this.prevState = prev;
        this.time = me.time;
        this.rules = prev.rules;
        this.me = me;
        this.enemy = enemy;

        myBullets = updateWaves(prev.myBullets, prev.me, prev.enemy, me, enemy);
        myFiredBullet = LxxCollections.find(myBullets, new F1<LxxWave, Boolean>() {
            @Override
            public Boolean f(LxxWave lxxWave) {
                return lxxWave.time == prev.time;
            }
        });

        enemyBullets = updateWaves(prev.enemyBullets, prev.enemy, prev.me, enemy, me);
        enemyFiredBullet = LxxCollections.find(enemyBullets, new F1<LxxWave, Boolean>() {
            @Override
            public Boolean f(LxxWave lxxWave) {
                return lxxWave.time == prev.time;
            }
        });
    }

    public BattleState(BattleRules battleRules, LxxRobot me, LxxRobot enemy) {
        this.prevState = null;
        this.time = me.time;
        this.rules = battleRules;
        this.me = me;
        this.enemy = enemy;

        myBullets = Collections.emptyList();
        enemyBullets = Collections.emptyList();
        myFiredBullet = null;
        enemyFiredBullet = null;
    }

    public List<LxxWave> getEnemyBullets(final APoint pnt, double flightTimeLimit, int cnt) {
        final ArrayList<LxxWave> lst = new ArrayList<LxxWave>();

        for (LxxWave bullet : enemyBullets) {
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

    private static List<LxxWave> updateWaves(List<LxxWave> robotBullets,
                                             LxxRobot oldRobot, LxxRobot oldOpponent,
                                             LxxRobot robot, LxxRobot opponent) {
        final List<LxxWave> newBullets = new ArrayList<LxxWave>(robotBullets);
        if (robot.firePower > 0) {
            final LxxWave lxxWave = new LxxWave(oldRobot, oldOpponent, Rules.getBulletSpeed(robot.firePower), oldRobot.time);
            newBullets.add(lxxWave);
        }

        final boolean hasBullets = robot.bullets.size() > 0;
        for (Iterator<LxxWave> iter = newBullets.iterator(); iter.hasNext(); ) {
            final LxxWave w = iter.next();
            // todo (azhidkov): make check more precise
            boolean isWaveRemoved = false;
            if (!opponent.alive || w.aDistance(opponent) < w.speed * (robot.time - w.time)) {
                iter.remove();
                isWaveRemoved = true;
            }

            if (hasBullets) {
                for (Iterator<Bullet> bulletIterator = robot.bullets.iterator(); bulletIterator.hasNext(); ) {
                    final Bullet bullet = bulletIterator.next();
                    if (abs(w.launcher.distance(bullet.getX(), bullet.getY()) -
                            (oldRobot.time - w.time) * w.speed) < w.speed + 0.1) {
                        if (!isWaveRemoved) {
                            iter.remove();
                        }
                        bulletIterator.remove();
                    }
                }
            }
        }

        return newBullets;
    }

    public Collection<LxxWave> getMyBullets() {
        return Collections.unmodifiableCollection(myBullets);
    }

    public Collection<LxxWave> getEnemyBullets() {
        return Collections.unmodifiableCollection(enemyBullets);
    }

    public LxxRobot getRobot(String robotName) {
        return me.name.equals(robotName) ? me : enemy;
    }
}
