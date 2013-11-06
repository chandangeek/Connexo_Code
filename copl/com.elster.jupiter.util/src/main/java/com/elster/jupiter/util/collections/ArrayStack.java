package com.elster.jupiter.util.collections;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;

/**
 * Stack implementation, backed by an ArrayDeque.
 * @param <E>
 */
public final class ArrayStack<E> implements Stack<E> {

    private final ArrayDeque<E> backingDeque;

    public ArrayStack() {
        backingDeque = new ArrayDeque<>();
    }

    public ArrayStack(int initialCapacity) {
        backingDeque = new ArrayDeque<>(initialCapacity);
    }

    public ArrayStack(Collection<? extends E> collection) {
        backingDeque = new ArrayDeque<>(collection);
    }

    public boolean addAll(Collection<? extends E> c) {
        return backingDeque.addAll(c);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return backingDeque.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return backingDeque.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return backingDeque.retainAll(c);
    }

    @Override
    public String toString() {
        return backingDeque.toString();
    }

    public boolean add(E e) {
        return backingDeque.add(e);
    }

    @Override
    public void clear() {
        backingDeque.clear();
    }

    @Override
    public boolean contains(Object o) {
        return backingDeque.contains(o);
    }

    @Override
    public boolean isEmpty() {
        return backingDeque.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return backingDeque.iterator();
    }

    @Override
    public boolean remove(Object o) {
        return backingDeque.remove(o);
    }

    @Override
    public int size() {
        return backingDeque.size();
    }

    @Override
    public Object[] toArray() {
        return backingDeque.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return backingDeque.toArray(a);
    }

    @Override
    public void push(E item) {
        backingDeque.push(item);
    }

    @Override
    public E pop() {
        return backingDeque.pop();
    }

    @Override
    public E peek() {
        return backingDeque.peek();
    }

}
