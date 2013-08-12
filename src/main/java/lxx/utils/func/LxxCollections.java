package lxx.utils.func;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class LxxCollections {

    private LxxCollections() {
    }

    public static <T> Option<T> find(Collection<T> coll, F1<T, Boolean> predicate) {
        for (T e : coll) {
            if (predicate.f(e)) {
                return Option.of(e);
            }
        }

        return Option.none();
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

}
