package lxx.utils.func;

public abstract class Option<T> {

    public static final Option NONE = new None();

    public static <T> Option<T> of(T value) {
        if (value == null) {
            return NONE;
        } else {
            return new Some<T>(value);
        }
    }

    public abstract boolean defined();

    public abstract T get();

    public abstract T getOr(T defaultValue);

    public abstract T getOr(RuntimeException failReason);

    public abstract <R> Option<R> map(F1<T, R> mapper);

    public abstract T getNullable();

    public abstract boolean empty();

    public static <T> Option<T> none() {
        return NONE;
    }

    private static final class Some<T> extends Option<T> {

        private final T value;

        private Some(T value) {
            this.value = value;
        }

        @Override
        public boolean defined() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T getOr(T defaultValue) {
            return value;
        }

        @Override
        public T getOr(RuntimeException failReason) {
            return value;
        }

        @Override
        public <R> Option<R> map(F1<T, R> mapper) {
            return Option.of(mapper.f(value));
        }

        @Override
        public T getNullable() {
            return value;
        }

        @Override
        public boolean empty() {
            return false;
        }
    }

    private static class None<T> extends Option<T> {

        @Override
        public boolean defined() {
            return false;
        }

        @Override
        public T get() {
            throw new IllegalStateException("Get from none");
        }

        @Override
        public T getOr(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T getOr(RuntimeException failReason) {
            throw failReason;
        }

        @Override
        public <R> Option<R> map(F1<T, R> mapper) {
            return (Option<R>) this;
        }

        @Override
        public T getNullable() {
            return null;
        }

        @Override
        public boolean empty() {
            return true;
        }

    }

}

