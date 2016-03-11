package com.elster.insight.usagepoint.config.impl.aggregation;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Models a simple stack, a last in first out storage of elements.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (14:15)
 */
public class Stack<E> {

    private final Deque<E> elements = new LinkedList<>();

    public void push(E element) {
        this.elements.addFirst(element);
    }

    public E pop() {
        return this.elements.removeFirst();
    }

    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    public int size() {
        return this.elements.size();
    }

}