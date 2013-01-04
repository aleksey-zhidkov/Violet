package lxx.utils;

public class BattleRules {

    public final BattleField field;
    public final double robotWidth;
    public final double initialGunHeat;
    public final double gunCoolingRate;
    public final double initialEnergy;
    public final String myName;

    public BattleRules(double battleFieldWidth, double battleFieldHeight, double robotWidth,
                       double initialGunHeat, double gunCoolingRate, double initialEnergy, String myName) {
        this.field = new BattleField((int)robotWidth / 2, (int)robotWidth / 2, (int)(battleFieldWidth - robotWidth), (int)(battleFieldHeight - robotWidth));
        this.robotWidth = robotWidth;
        this.initialGunHeat = initialGunHeat;
        this.gunCoolingRate = gunCoolingRate;
        this.initialEnergy = initialEnergy;
        this.myName = myName;
    }

}
