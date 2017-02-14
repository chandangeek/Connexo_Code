/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

public final class Priority implements Comparable<Priority> {

    private final int urgency;

    public static final Priority HIGHEST = new Priority(Integer.MAX_VALUE);
    public static final Priority LOWEST = new Priority(Integer.MIN_VALUE);
    public static final Priority NORMAL = new Priority(0);

    private Priority(int urgency) {
        this.urgency = urgency;
    }

    public static Priority get(int order) {
        return new Priority(order);
    }

    @Override
    public int compareTo(Priority other) {
        return Integer.compare(urgency, other.urgency);
    }

    public boolean isHighest() {
        return urgency == Integer.MAX_VALUE;
    }

    public boolean isLowest() {
        return urgency == Integer.MIN_VALUE;
    }

    public Priority higher() {
        if (isHighest()) {
            throw new IllegalStateException();
        }
        return Priority.get(urgency + 1);
    }

    public Priority lower() {
        if (isLowest()) {
            throw new IllegalStateException();
        }
        return Priority.get(urgency - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Priority priority = (Priority) o;

        return urgency == priority.urgency;

    }

    @Override
    public int hashCode() {
        return urgency;
    }
}
