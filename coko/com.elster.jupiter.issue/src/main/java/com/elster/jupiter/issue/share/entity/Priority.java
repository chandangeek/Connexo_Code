package com.elster.jupiter.issue.share.entity;

public final class Priority implements Comparable<Priority> {

    private volatile long urgency;
    private volatile long impact;

    public static final Priority HIGHEST = new Priority(100L, Long.MAX_VALUE);
    public static final Priority LOWEST = new Priority(0, 0);

    private Priority(long urgency, long impact) {
        this.urgency = urgency;
    }

    public static Priority get(long urgency, long impact) {
        return new Priority(urgency, impact);
    }

    public static Priority get(long urgency) {
        return get(urgency, 0L);
    }

    @Override
    // for the moment there is no weight attributed to urgency with respect to impact
    public int compareTo(Priority other) {
        return Long.compare(this.urgency + this.impact, other.urgency + other.impact);
    }

    public boolean isHighest() {
        return urgency > 100L;
    }

    public boolean isLowest() {
        return urgency < 0 && impact < 0;
    }

    public void increaseUrgency() {
        ++this.urgency;
        if (isHighest()) {
            throw new IllegalStateException();
        }
    }

    public void lowerUrgency() {
        --this.urgency;
        if (isLowest()) {
            throw new IllegalStateException();
        }
    }

    public void clearImpact() {
        this.impact = 0;
    }

    public long getUrgency() {
        return urgency;
    }

    public long getImpact() {
        return impact;
    }

    public void setImpact(int impact) {
        this.impact = impact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Priority priority = (Priority) o;

        if (getUrgency() != priority.getUrgency()) {
            return false;
        }
        return getImpact() == priority.getImpact();

    }

    @Override
    public int hashCode() {
        int result = (int) (getUrgency() ^ (getUrgency() >>> 32));
        result = 31 * result + (int) (getImpact() ^ (getImpact() >>> 32));
        return result;
    }
}
