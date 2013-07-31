package lxx.model;

import lxx.utils.BattleRules;
import lxx.utils.func.Option;

import java.util.Collections;
import java.util.List;

public class BattleState2 {

    public final long time;

    public final BattleRules rules;
    public final BattleState2 prevState;

    public final LxxRobot2 me;
    public final LxxRobot2 opponent;

    public final Option<LxxWave2> myFiredBullet;
    public final Option<LxxWave2> opponentFiredBullet;

    public final List<LxxWave2> myBulletsInAir;
    public final List<LxxWave2> opponentBulletsInAir;

    public final List<LxxBullet2> myInterceptedBullets;
    public final List<LxxBullet2> opponentInterceptedBullets;

    public final List<LxxBullet2> myHitBullets;
    public final List<LxxBullet2> opponentHitBullets;

    public final List<LxxWave2> myGoneBullets;
    public final List<LxxWave2> opponentGoneBullets;

    public BattleState2(BattleRules rules, long time, BattleState2 prevState, LxxRobot2 me, LxxRobot2 opponent,
                        Option<LxxWave2> myFiredBullet, Option<LxxWave2> opponentFiredBullet,
                        List<LxxWave2> myBulletsInAir, List<LxxWave2> opponentBulletsInAir,
                        List<LxxBullet2> myInterceptedBullets, List<LxxBullet2> opponentInterceptedBullets,
                        List<LxxBullet2> myHitBullets, List<LxxBullet2> opponentHitBullets,
                        List<LxxWave2> myGoneBullets, List<LxxWave2> opponentGoneBullets) {
        this.rules = rules;
        this.time = time;
        this.prevState = prevState;
        
        this.me = me;
        this.opponent = opponent;
        
        this.myFiredBullet = myFiredBullet;
        this.opponentFiredBullet = opponentFiredBullet;

        this.myBulletsInAir = Collections.unmodifiableList(myBulletsInAir);
        this.opponentBulletsInAir = Collections.unmodifiableList(opponentBulletsInAir);
        
        this.myInterceptedBullets = Collections.unmodifiableList(myInterceptedBullets);
        this.opponentInterceptedBullets = Collections.unmodifiableList(opponentInterceptedBullets);
        
        this.myHitBullets = Collections.unmodifiableList(myHitBullets);
        this.opponentHitBullets = Collections.unmodifiableList(opponentHitBullets);

        this.myGoneBullets = Collections.unmodifiableList(myGoneBullets);
        this.opponentGoneBullets = Collections.unmodifiableList(opponentGoneBullets);
    }

    public LxxRobot2 getRobot(String robotName) {
        if (robotName.equals(me.name)) {
            return me;
        } else if (robotName.equals(opponent.name)) {
            return opponent;
        }

        throw new IllegalArgumentException("Unknown robot: " + robotName);
    }

    public Option<LxxWave2> getRobotFiredBullet(String robotName) {
        return me.name.equals(robotName) ? myFiredBullet : opponentFiredBullet;
    }
}
