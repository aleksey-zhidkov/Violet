package lxx.utils.func;

import java.util.Collection;

public class LxxCollections {

    public static<T> T find(Collection<T> coll, F1<T, Boolean> predicate) {
        for (T e : coll) {
            if (predicate.f(e)) {
                return e;
            }
        }

        return null;
    }

}
