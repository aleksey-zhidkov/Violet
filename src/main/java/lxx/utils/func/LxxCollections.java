package lxx.utils.func;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LxxCollections {

    public static<T> T find(Collection<T> coll, F1<T, Boolean> predicate) {
        for (T e : coll) {
            if (predicate.f(e)) {
                return e;
            }
        }

        return null;
    }

    public static <T> List<T> filter(List<T> myBulletsInAir, F1<T, Boolean> predicate) {
        final List<T> res = new ArrayList<T>();

        for (final T cnd : myBulletsInAir) {
            if (predicate.f(cnd)) {
                res.add(cnd);
            }
        }

        return res;
    }

    public static <T> List<T>[] split(List<T> myBulletsInAir, F1<T, Boolean> predicate) {
        final ArrayList[] res = {new ArrayList<T>(), new ArrayList<T>()};

        for (final T cnd : myBulletsInAir) {
            if (predicate.f(cnd)) {
                res[0].add(cnd);
            } else {
                res[1].add(cnd);
            }
        }

        return res;
    }

}
