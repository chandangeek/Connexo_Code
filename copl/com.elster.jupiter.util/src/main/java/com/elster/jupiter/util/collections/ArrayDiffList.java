/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * DiffList implementation that uses ArrayLists.
 *
 * @author Bart
 * @author tgr
 * @Since 1/26/13 7:46 PM
 */
public class ArrayDiffList<E> implements DiffList<E> {
    private final List<E> originalList;
    private final List<E> list;

    /**
     * Creates a new ArrayDiffList populated with the given collection, elements marked as elements from the original list or not.
     *
     * @param originalItems collection of which the elements will be added to the list.
     * @param currentItems true if the elements added should be considered as elements in the original list, false otherwise.
     */
    public ArrayDiffList(Collection<E> originalItems, Collection<E> currentItems) {
        this.originalList = new ArrayList<>(originalItems);
        this.list = new ArrayList<>(currentItems);
    }

    /**
     * Creates a new ArrayDiffList populated with the given collection, elements marked as elements from the original list.
     *
     * @param original collection of which the elements will be added to the list.
     */
    public static <T> ArrayDiffList<T> fromOriginal(Collection<T> original) {
        return new ArrayDiffList<>(original, original);
    }

    /**
     * Creates a new ArrayDiffList populated with the given collection, elements marked as not elements from the original list.
     *
     * @param allNew collection of which the elements will be added to the list.
     */
    public static <T> ArrayDiffList<T> withAllNew(Collection<T> allNew) {
        return new ArrayDiffList<>(Collections.<T>emptyList(), allNew);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public Iterator<E> iterator() {
        return list.iterator();
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return list.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public final boolean equals(Object o) {
        return list.equals(o);
    }

    @Override
    public final int hashCode() {
        return list.hashCode();
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        list.add(index, element);
    }

    @Override
    public E remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Collection<E> getAdditions() {
        List<E> additions = new ArrayList<>(list);
        removeOnce(additions, originalList);
        return additions;
    }

    @Override
    public Collection<E> getRemovals() {
        List<E> removals = new ArrayList<>(originalList);
        removeOnce(removals, list);
        return removals;
    }

    @Override
    public Collection<E> getRemaining() {
        List<E> remaining = new ArrayList<>(list);
        removeOnce(remaining, new ArrayList<>(getAdditions()));
        return remaining;
    }

    @Override
    public void addAsOriginal(E e) {
        list.add(e);
        originalList.add(e);
    }

    @Override
    public boolean hasChanged() {
        return !getRemovals().isEmpty() || !getAdditions().isEmpty();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public DiffList<E> immutableView() {
        return new ImmutableDiffList<>(this);
    }

    @Override
    public List<E> originalList() {
        return Collections.unmodifiableList(originalList);
    }

    private void removeOnce(List<E> source, List<E> subtractor) {
        ArrayList<E> subtractorCopy = new ArrayList<>(subtractor);
        Iterator<E> it = source.iterator();
        while (it.hasNext()) {
            E next = it.next();
            if (subtractorCopy.contains(next)) {
                it.remove();
                subtractorCopy.remove(next);
            }
        }

    }

}
