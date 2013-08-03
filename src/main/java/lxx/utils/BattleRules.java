package lxx.utils;

public class BattleRules {

    public static final double initialGunHeat = 3;

    public final BattleField field;

    public final double robotWidth;
    public final double gunCoolingRate;
    public final double initialEnergy;
    public final String myName;

    public BattleRules(double battleFieldWidth, double battleFieldHeight, double robotWidth,
                       double gunCoolingRate, double initialEnergy, String myName) {
        this.field = new BattleField((int) robotWidth / 2, (int) robotWidth / 2, (int) (battleFieldWidth - robotWidth), (int) (battleFieldHeight - robotWidth));
        this.robotWidth = robotWidth;
        this.gunCoolingRate = gunCoolingRate;
        this.initialEnergy = initialEnergy;
        this.myName = myName;
    }

}
