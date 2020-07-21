package com.itwookie.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImmutableSet<E> implements Set<E> {

    Set<E> wrapped;

    public ImmutableSet(Collection<E> setToCopy) {
        wrapped = new HashSet<>(setToCopy);
    }

    private ImmutableSet() {
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return wrapped.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return wrapped.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return wrapped.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return wrapped.toArray(a);
    }

    /**
     * by definition of {@link Collection} this method always returns false
     */
    @Override
    public boolean add(E e) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("This operation is not supported for immutable sets");
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return wrapped.containsAll(c);
    }

    /**
     * by definition of {@link Collection} this method always returns false
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return false;
    }

    /**
     * by definition of {@link Collection} this method always returns false
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    /**
     * by definition of {@link Collection} this method always returns false
     */
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This operation is not supported for immutable sets");
    }
}
