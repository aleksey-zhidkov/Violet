package lxx.logs;

import ags.utils.KdTree;
import lxx.model.LxxRobot;

import java.util.ArrayList;

public class KdTreeMovementLog<T> implements MovementLog<T> {

    private final KdTree<T> tree;
    private final LocationFactory locationFactory;

    public KdTreeMovementLog(KdTree<T> tree, LocationFactory locationFactory) {
        this.tree = tree;
        this.locationFactory = locationFactory;
    }

    @Override
    public void addEntry(LxxRobot observer, LxxRobot observable, T entry) {
        tree.addPoint(locationFactory.getLocation(observer, observable), entry);
    }

    @Override
    public ArrayList<KdTree.Entry<T>> getEntries(LxxRobot observer, LxxRobot observable, int count) {
        return (ArrayList<KdTree.Entry<T>>) tree.nearestNeighbor(locationFactory.getLocation(observer, observable), count, true);
    }

    @Override
    public int size() {
        return tree.size();
    }
}