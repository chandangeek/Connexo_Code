/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;

public final class Priority implements Comparable<Priority>, Cloneable {

    @Valid
    private int urgency;
    @Valid
    private int impact;

    private Rank rank;

    private static final int MAX_VAL = 50;
    private static final int MIN_VAL = 1;

    public static final Priority DEFAULT = new Priority(MAX_VAL / 5, MIN_VAL * 5);

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

    private static String SEPARATOR = ":";


    public Priority() {
    }

    private void setRank() {
        int priorityValue = this.urgency + this.impact - 1;
        int res = priorityValue / 10;
        switch (res) {
            case 0:
            case 1:
                this.rank = Rank.VERY_LOW;
                break;
            case 2:
            case 3:
                this.rank = Rank.LOW;
                break;
            case 4:
            case 5:
                this.rank = Rank.MEDIUM;
                break;
            case 6:
            case 7:
                this.rank = Rank.HIGH;
                break;
            case 8:
            case 9:
            case 10:
                this.rank = Rank.VERY_HIGH;
                break;
            default:
                this.rank = Rank.LOW;
                break;
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
        return urgency >= MAX_VAL || impact >= MAX_VAL;
    }

    public boolean isLowest() {
        return urgency <= MIN_VAL || impact <= MIN_VAL;
    }

    public int increaseUrgency() {
        if (!isHighest()) {
            return ++this.urgency;
        } else {
            return MAX_VAL;
        }
    }

    public int lowerUrgency() {
        if (!isLowest()) {
            return --this.urgency;
        } else {
            return MIN_VAL;
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

    public static Priority fromStringValue(String stringValue) {
        if (stringValue == null || stringValue.isEmpty() || !stringValue.contains(SEPARATOR)) {
            return null;
        }
        String[] parts = stringValue.split(SEPARATOR);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Incorrectly formatted priority.Please check format and range.");
        }

        if (Arrays.stream(parts)
                .anyMatch(element -> element.split(",").length > 2
                        || element.split(".").length > 2)) {
            throw new IllegalArgumentException("Incorrectly formatted priority.Please check format and range.");
        }

        int urgency = Integer.valueOf(parts[0].contains(",") ? String.valueOf(parts[0].replace(",", ".")) : parts[0]);
        int impact = Integer.valueOf(parts[1].contains(",") ? String.valueOf(parts[1].replace(",", ".")) : parts[1]);

        if (urgency > 50 || impact > 50 || urgency < 1 || impact < 1) {
            throw new IllegalArgumentException("Incorrectly formatted priority.Please check format and range.");
        }

        return new Priority(urgency, impact);
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

        return getUrgency() == priority.getUrgency() && getImpact() == priority.getImpact();

    }

    @Override
    public int hashCode() {
        int result = (getUrgency() ^ (getUrgency() >>> 32));
        result = 31 * result + (getImpact() ^ (getImpact() >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return urgency + ":"
                + impact;
    }
}
