package com.itwookie.utils;

import java.util.Objects;

public class Priorized<T> implements Comparable<Priorized> {

    private Priority priority;
    private T value;

    public Priorized(T value, Priority priority) {
        this.value = value;
        this.priority = priority;
    }

    public Priorized(T value) {
        this.value = value;
        this.priority = Priority.Normal;
    }

    public Priority priority() {
        return priority;
    }

    public T get() {
        return value;
    }

    @Override
    public int compareTo(Priorized o) {
        return priority().compareTo(o.priority());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Priorized<?> priorized = (Priorized<?>) o;
        return priority == priorized.priority &&
                Objects.equals(value, priorized.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, value);
    }

}
