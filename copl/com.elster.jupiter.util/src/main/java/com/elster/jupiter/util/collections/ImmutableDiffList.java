/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Wrapper around a DiffList which decorates it to be Immutable.
 *
 * @param <E>
 */
public class ImmutableDiffList<E> implements DiffList<E> {

    private final DiffList<E> wrapped;
    private final List<E> asImmutableList;

    public ImmutableDiffList(DiffList<E> source) {
        this.wrapped = source;
        this.asImmutableList = Collections.unmodifiableList(wrapped);
    }

    public boolean add(E e) {
        return asImmutableList.add(e);
    }

    public void add(int index, E element) {
        asImmutableList.add(index, element);
    }

    public boolean addAll(Collection<? extends E> c) {
        return asImmutableList.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return asImmutableList.addAll(index, c);
    }

    @Override
    public void clear() {
        asImmutableList.clear();
    }

    @Override
    public boolean contains(Object o) {
        return asImmutableList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return asImmutableList.containsAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return asImmutableList.equals(o);
    }

    @Override
    public E get(int index) {
        return asImmutableList.get(index);
    }

    @Override
    public int hashCode() {
        return asImmutableList.hashCode();
    }

    @Override
    public int indexOf(Object o) {
        return asImmutableList.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return asImmutableList.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return asImmutableList.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return asImmutableList.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return asImmutableList.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return asImmutableList.listIterator(index);
    }

    @Override
    public E remove(int index) {
        return asImmutableList.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return asImmutableList.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return asImmutableList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return asImmutableList.retainAll(c);
    }

    public E set(int index, E element) {
        return asImmutableList.set(index, element);
    }

    @Override
    public int size() {
        return asImmutableList.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return asImmutableList.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return asImmutableList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return asImmutableList.toArray(a);
    }

    @Override
    public Collection<E> getAdditions() {
        return wrapped.getAdditions();
    }

    @Override
    public Collection<E> getRemaining() {
        return wrapped.getRemaining();
    }

    @Override
    public Collection<E> getRemovals() {
        return wrapped.getRemovals();
    }

    @Override
    public boolean hasChanged() {
        return wrapped.hasChanged();
    }

    @Override
    public void addAsOriginal(E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DiffList<E> immutableView() {
        return this;
    }

    @Override
    public List<E> originalList() {
        return wrapped.originalList();
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }
}
