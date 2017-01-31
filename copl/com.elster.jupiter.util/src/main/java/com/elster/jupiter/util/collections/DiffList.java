/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import java.util.Collection;
import java.util.List;

/**
 * Extension of the List interface. A DiffList is a list that can report on changes vs. its initial state.
 *
 * @author Bart
 * @author tgr
 * @since 1/26/13 6:01 PM
 */
public interface DiffList<E> extends List<E> {

    /**
     * @return a collection containing all elements in this list, not in the original.
     */
    Collection<E> getAdditions();

    /**
     * @return a collection containing all elements not in this list, yet in the original.
     */
    Collection<E> getRemovals();

    /**
     * @return a collection containing all elements in this list, also in the original.
     */
    Collection<E> getRemaining();

    /**
     * Adds the element to the list WITHOUT considering it as a new element in the list.
     * @param element The new element
     */
    void addAsOriginal(E element);

    /**
     * @return true if the list has removals or additions.
     */
    boolean hasChanged();

    /**
     * @return a DiffList backed by this instance, which is unmodifiable.
     */
    DiffList<E> immutableView();

    /**
     * @return a List representation of the original elements.
     */
    List<E> originalList();
}
