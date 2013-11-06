package com.elster.jupiter.util.collections;

import java.util.Collection;

public interface Stack<E> extends Collection<E> {

    /**
     * Pushes an element onto the stack represented by this stack (in other
     * words, at the top of this stack) if it is possible to do so
     * immediately without violating capacity restrictions, throwing an
     * <tt>IllegalStateException</tt> if no space is currently available.
     *
     * @param e the element to push
     * @throws IllegalStateException if the element cannot be added at this
     *         time due to capacity restrictions
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this deque
     * @throws NullPointerException if the specified element is null and this
     *         deque does not permit null elements
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this deque
     */
    void push(E e);

    /**
     * Pops an element from the stack represented by this stack.  In other
     * words, removes and returns the top element of this stack.
     *
     * @return the element at the top of this stack
     * @throws java.util.NoSuchElementException if this stack is empty
     */
    E pop();

    /**
     * Retrieves, but does not remove, the top of the stack represented by
     * this Stack, or returns <tt>null</tt> if this stack is empty.
     *
     * @return the head of the stack represented by this stack, or
     *         <tt>null</tt> if this stack is empty
     */
    E peek();

}
