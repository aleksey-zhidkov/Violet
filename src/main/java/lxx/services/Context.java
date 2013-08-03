package lxx.services;

import lxx.events.*;
import lxx.logs.KdTreeMovementLog;
import lxx.logs.MovementLog;
import lxx.model.LxxRobot;
import lxx.utils.GuessFactor;

import java.util.HashMap;
import java.util.Map;

public class Context {

    public final DangerService dangerService;
    public final GFMovementLogServiceImpl enemyLogService;
    private final Map<String, EventsChannel> bulletsEventsChannels = new HashMap<String, EventsChannel>();
    private final String myName;
    private final EventsChannel battleEventsChannel;

    public Context(KdTreeMovementLog<GuessFactor> mySimpleMovementLog, MovementLog<GuessFactor> enemySimpleMovementLog,
                   String myName, String opponentName) {
        assert !LxxRobot.UNKNOWN.equals(myName);
        assert !LxxRobot.UNKNOWN.equals(opponentName);
        this.myName = myName;

        final EventsChannel opponentBulletsEventsChannel = new EventsChannel();
        this.bulletsEventsChannels.put(opponentName, opponentBulletsEventsChannel);

        final EventsChannel myBulletsEventsChannel = new EventsChannel();
        this.bulletsEventsChannels.put(myName, myBulletsEventsChannel);

        battleEventsChannel = new EventsChannel();

        dangerService = new DangerService(mySimpleMovementLog);
        opponentBulletsEventsChannel.addBulletDetectedEventListener(dangerService);
        opponentBulletsEventsChannel.addWaveGoneEventListener(dangerService);

        enemyLogService = new GFMovementLogServiceImpl(enemySimpleMovementLog, myName, opponentName);
        myBulletsEventsChannel.addBulletFiredListener(enemyLogService);
        battleEventsChannel.addTickEventsListener(enemyLogService);
    }

    public EventsChannel getMyBulletsEventsChannel() {
        return bulletsEventsChannels.get(myName);
    }

    public EventsChannel getBulletsEventsChannel(String robotName) {
        return bulletsEventsChannels.get(robotName);
    }

    public EventsChannel getBattleEventsChannel() {
        return battleEventsChannel;
    }
}
