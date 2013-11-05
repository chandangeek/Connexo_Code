package com.elster.jupiter.util.collections;

import java.util.Collection;

public interface Stack<E> extends Collection<E> {

    /**
     * Pushes an item onto the top of this stack.
     *
     * @param item the item to be pushed onto this stack.
     * @return the <code>item</code> argument.
     */
    E push(E item);

    /**
     * Removes the object at the top of this stack and returns that
     * object as the value of this function.
     *
     * @return The object at the top of this stack.
     * @throws java.util.EmptyStackException if this stack is empty.
     */
    E pop();

    /**
     * Looks at the object at the top of this stack without removing it
     * from the stack.
     *
     * @return the object at the top of this stack.
     * @throws java.util.EmptyStackException if this stack is empty.
     */
    E peek();

}
