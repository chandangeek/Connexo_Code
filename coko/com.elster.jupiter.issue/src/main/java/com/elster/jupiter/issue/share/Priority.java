package com.elster.jupiter.issue.share;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlTransient;

public final class Priority implements Comparable<Priority>, Cloneable {
    @Valid
    private int urgency;
    @Valid
    private int impact;

    private Rank rank;

    enum Rank {
        VERY_HIGH("Very High"),
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low"),
        VERY_LOW("Very Low");

        private final String rankName;

        Rank(String rankName) {
            this.rankName = rankName;
        }

        @Override
        public String toString() {
            return rankName;
        }
    }


    public Priority() {
    }

    private void setRank() {
        int priorityValue = this.urgency + this.impact;
        switch (priorityValue / 10) {
            case 0:
            case 1:
            case 2:
                this.rank = Rank.VERY_LOW;
            case 3:
            case 4:
                this.rank = Rank.VERY_LOW;
            case 5:
            case 6:
                this.rank = Rank.MEDIUM;
            case 7:
            case 8:
                this.rank = Rank.HIGH;
            case 9:
            case 10:
                this.rank = Rank.VERY_HIGH;
            default:
                this.rank = Rank.LOW;
        }
    }

    private Priority(int urgency, int impact) {
        this.urgency = urgency;
        this.impact = impact;
        setRank();
    }

    public static Priority get(int urgency, int impact) {
        return new Priority(urgency, impact);
    }

    @Override
    // for the moment there is no weight attributed to urgency with respect to impact
    public int compareTo(Priority other) {
        return Integer.compare(this.urgency + this.impact, other.urgency + other.impact);
    }

    public boolean isHighest() {
        return urgency > 50;
    }

    public boolean isLowest() {
        return urgency < 0 || impact < 0;
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

    public int getUrgency() {
        return urgency;
    }

    public int getImpact() {
        return impact;
    }

    public void setImpact(int impact) {
        this.impact = impact;
    }

    public Rank getRank() {
        return rank;
    }

    public Priority copy() {
        try {
            Priority result = (Priority) this.clone();
            result.urgency = this.urgency;
            result.impact = this.impact;
            return result;
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    @XmlTransient
    public boolean isEmpty() {
        return urgency == 0 && impact == 0;
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
        int result = (getUrgency() ^ (getUrgency() >>> 32));
        result = 31 * result + (getImpact() ^ (getImpact() >>> 32));
        return result;
    }
}
