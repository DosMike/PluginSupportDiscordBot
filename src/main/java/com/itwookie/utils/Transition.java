package com.itwookie.utils;

import java.util.function.BiConsumer;

public class Transition<T> {

    private T o, n;

    public Transition(T oldValue, T newValue) {
        o = oldValue;
        n = newValue;
    }

    public T getPrevious() {
        return o;
    }

    public T getCurrent() {
        return n;
    }

    public boolean changed() {
        if (o == null || n == null) return o == n;
        return o.equals(n);
    }

    /**
     * @param change will receive old and new value if changed
     */
    public void ifChanged(BiConsumer<T, T> change) {
        change.accept(o, n);
    }

}
