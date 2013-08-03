package lxx.logs;

import ags.utils.KdTree;
import lxx.model.LxxRobot;

import java.util.List;

public interface MovementLog<T> {

    void addEntry(LxxRobot observer, LxxRobot observable, T entry);

    List<KdTree.Entry<T>> getEntries(LxxRobot observer, LxxRobot observable, int count);

    int size();
}
