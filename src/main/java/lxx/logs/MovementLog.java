package lxx.logs;

import ags.utils.KdTree;
import lxx.model.LxxRobot2;

import java.util.ArrayList;

public interface MovementLog<T> {

    void addEntry(LxxRobot2 observer, LxxRobot2 observable, T entry);

    ArrayList<KdTree.Entry<T>> getEntries(LxxRobot2 observer, LxxRobot2 observable, int count);

    int size();
}
