package com.elster.jupiter.util.time;


import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * {@link IntermittentInterval} represents a list of consecutive {@link Interval}s that do not overlap nor abut. It is probably best
 * understood as a set of {@link java.util.Date}s that are represented by the simplest sorted set of {@link Interval}s.<br>
 * It is an immutable value type. As such the operation that modify a {@link IntermittentInterval} simply return another instance.
 * {@link IntermittentInterval} will merge all {@link Interval}s that are added when possible. <br> {@link IntermittentInterval} is useful for
 * detecting what points in time a collection of business objects that each have a {@link Interval} cover, and whether there are
 * any gaps. Or whether this collection of business object cover the exact same points in time as another collection of business
 * objects.<br>
 * It offers powerful set like operations such as union, intersection, difference and negative.<br>
 * None of the methods accept null for any of its parameters, and none return null, except for toSpanningPeriod(), which returns
 * null for an empty {@link Interval}. This has to do with the fact that there is no straightforward representation of an empty
 * {@link Interval}.
 *
 * @author Tom De Greyt
 */
public final class IntermittentInterval implements Iterable<Interval> {

    public static final IntermittentInterval ALWAYS = new IntermittentInterval(new Interval(null, null));
    public static final IntermittentInterval NEVER = new IntermittentInterval();

    public enum IntervalComparators implements Comparator<Interval> {

        FROM_COMPARATOR {
            public int compare(Interval o1, Interval o2) {
                if (o1.getStart() == null) {
                    return o2.getStart() == null ? 0 : -1;
                }
                return o2.getStart() == null ? 1 : o1.getStart().compareTo(o2.getStart());
            }
        }
    }


    /*
      * Helper class for iterating over two sanitized Lists of Intervals at the same time to detect all possible Interval intersections
      */
    private static class PeriodListPair implements Iterable<PeriodPair> {

        private final List<Interval> first;
        private final List<Interval> second;

        private List<Interval> getFirst() {
            return first;
        }

        private List<Interval> getSecond() {
            return second;
        }

        private class PeriodPairIterator implements Iterator<PeriodPair> {

            private Iterator<Interval> iter1 = getFirst().iterator();
            private Iterator<Interval> iter2 = getSecond().iterator();
            private PeriodPair current;

            public boolean hasNext() {
                if (isFirst()) {
                    return iter1.hasNext() && iter2.hasNext();
                }
                return current.firstMustStep() ? iter1.hasNext() : iter2.hasNext();
            }

            public PeriodPair next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                current = isFirst() ? createFirstInstance() : createNextInstance();
                return current;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private PeriodPair createFirstInstance() {
                return new PeriodPair(iter1.next(), iter2.next());
            }

            private PeriodPair createNextInstance() {
                return current.firstMustStep() ? new PeriodPair(iter1.next(), current.getSecond()) : new PeriodPair(current
                        .getFirst(), iter2.next());
            }

            private boolean isFirst() {
                return current == null;
            }
        }

        public PeriodListPair(List<Interval> first, List<Interval> second) {
            this.first = first;
            this.second = second;
        }

        public Iterator<PeriodPair> iterator() {
            return new PeriodPairIterator();
        }
    }

    private static class PeriodPair {

        private final Interval first;
        private final Interval second;

        public PeriodPair(Interval first, Interval second) {
            this.first = first;
            this.second = second;
        }

        private Interval getFirst() {
            return first;
        }

        private Interval getSecond() {
            return second;
        }

        public boolean firstMustStep() {
            if (getFirst().includes(getSecond())) {
                return false;
            }
            if (getSecond().includes(getFirst())) {
                return true;
            }
            return IntervalComparators.FROM_COMPARATOR.compare(getFirst(), getSecond()) < 0;
        }

        public Interval intersection() {
            return getFirst().intersection(getSecond());
        }

        public boolean overlap() {
            return getFirst().overlaps(getSecond());
        }
    }

    private List<Interval> intervals = new ArrayList<Interval>();
    private int hash;
    private Interval spanningPeriod;

    /**
     * Creates an empty {@link IntermittentInterval} instance.
     */
    public IntermittentInterval() {
        intervals = new ArrayList<Interval>();
        hash = calculateHash();
    }

    /**
     * Creates a {@link IntermittentInterval} instance containing {@link Interval}s that are the result of merging and sorting all
     * {@link Interval}s in the given {@link Iterable}.
     *
     * @param Intervals the {@link Interval}s to add.
     */
    public IntermittentInterval(Iterable<Interval> Intervals) {
        intervals = new ArrayList<Interval>();
        for (Interval interval : Intervals) {
            if (!interval.isEmpty()) {
                intervals.add(interval);
            }
        }
        sanitize();
        hash = calculateHash();
    }

    /**
     * Creates a {@link IntermittentInterval} instance containing {@link Interval}s that are the result of merging and sorting all
     * given {@link Interval}s.
     *
     * @param Intervals the {@link Interval}s to add.
     */
    public IntermittentInterval(Interval... Intervals) {
        this(Arrays.asList(Intervals));
    }

    /**
     * Creates a {@link IntermittentInterval} that is the result of adding the {@link Interval} to the list, and resolving overlaps.
     *
     * @param period the {@link Interval} to add
     * @return the {@link IntermittentInterval} that contains all {@link java.util.Date}s this one does and all {@link java.util.Date}s that the given
     *         {@link Interval} contains, never null
     */
    public final IntermittentInterval addInterval(Interval period) {
        if (period.isEmpty()) {
            return this;
        }
        return copy().doAdd(period);
    }

    /**
     * Tests whether the given {@link java.util.Date} is contained in any of its {@link Interval}s.
     *
     * @param date the {@link java.util.Date} to test
     * @return true if the given {@link java.util.Date} is contained in any of its {@link Interval}s, false otherwise.
     */
    public boolean contains(Date date) {
        for (Interval period : intervals) {
            if (period.contains(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a {@link IntermittentInterval} that contains all {@link java.util.Date}s that are contained in this instance, but not contained in
     * the given {@link IntermittentInterval}.
     *
     * @param other {@link IntermittentInterval} to exclude
     * @return a {@link IntermittentInterval} that is the difference of this and the other, never null.
     */
    public IntermittentInterval difference(IntermittentInterval other) {
        IntermittentInterval copy = copy();
        for (Interval Interval : other.intervals) {
            copy.doRemove(Interval);
        }
        return copy;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return valueEquals(obj);
    }

    /**
     * Returns the {@link Interval}s that are the result of resolving all possible merges, in ascending chronological order.
     *
     * @return an immutable {@link java.util.List} of {@link Interval}s, never null.
     */
    public final List<Interval> getIntervals() {
        return Collections.unmodifiableList(intervals);
    }

    /**
     * Tests whether there are gaps between the contained {@link Interval}s. An empty {@link IntermittentInterval} will return false.
     *
     * @return true if getIntervals() would return a {@link java.util.List} containing more than 1 element.
     */
    public final boolean hasGaps() {
        return intervals.size() > 1;
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    /**
     * Creates a {@link IntermittentInterval} that contains all {@link java.util.Date}s that are contained in this instance and in the given other
     * instance.
     *
     * @param other the instance to intersect with
     * @return a {@link IntermittentInterval} that is the union of this and the given {@link IntermittentInterval}
     */
    public IntermittentInterval intersection(IntermittentInterval other) {
        if (triviallyCannotOverlap(other)) {
            return NEVER;
        }
        return complexIntersection(other);
    }

    /**
     * Tests whether this is an empty {@link IntermittentInterval}.
     *
     * @return true if getIntervals() would return an empty {@link java.util.List}, false otherwise.
     */
    public boolean isEmpty() {
        return intervals.isEmpty();
    }

    public Iterator<Interval> iterator() {
        return getIntervals().iterator();
    }

    /**
     * Creates a {@link IntermittentInterval} that contains all {@link java.util.Date}s that are not contained in this instance.
     *
     * @return a {@link IntermittentInterval} that is the negative of this one.
     */
    public IntermittentInterval negative() {
        IntermittentInterval negative = new IntermittentInterval(new Interval(null, null));
        for (Interval Interval : intervals) {
            negative.doRemove(Interval);
        }
        return negative;
    }

    /**
     * Tests whether the given {@link IntermittentInterval} contains a {@link java.util.Date} that is also contained in this one.
     *
     * @param other the {@link IntermittentInterval} to check against
     * @return true if at at least one {@link java.util.Date} exists for which contains() would return true for both this as the given
     *         {@link IntermittentInterval}, false otherwise.
     */
    public boolean overlaps(IntermittentInterval other) {
        if (triviallyCannotOverlap(other)) {
            return false;
        }
        return complexOverlap(other);
    }

    /**
     * Creates a {@link IntermittentInterval} that contains all {@link java.util.Date}s that this one does, but are not contained in the given
     * {@link Interval}.
     *
     * @param Interval the {@link Interval} to remove
     * @return a {@link IntermittentInterval} that contains all {@link java.util.Date}s that this one does, but are not contained in the given
     *         {@link Interval}, never null.
     */
    public final IntermittentInterval remove(Interval Interval) {
        return copy().doRemove(Interval);
    }

    /**
     * Creates a {@link Interval} that contains at least all {@link java.util.Date}s this {@link IntermittentInterval} contains.
     *
     * @return a {@link Interval} spanning from the earliest from to the latest to in this instance, or null if this is an empty
     *         {@link IntermittentInterval}.
     */
    public Interval toSpanningPeriod() {
        if (isEmpty()) {
            return null;
        }
        return getSpanningPeriod();
    }

    @Override
    public String toString() {
        return intervals.toString();
    }

    /**
     * Creates a {@link IntermittentInterval} that contains all {@link java.util.Date}s that are contained in this instance, or the given instance.
     *
     * @param other the {@link Interval} to unite with
     * @return a {@link IntermittentInterval} that is the union of this and the given instance, never null.
     */
    public IntermittentInterval union(IntermittentInterval other) {
        IntermittentInterval copy = copy();
        for (Interval Interval : other.intervals) {
            copy.doAdd(Interval);
        }
        return copy;
    }

    public Interval intervalAt(Date date) {
        int i = Collections.binarySearch(intervals, Interval.startAt(date), IntervalComparators.FROM_COMPARATOR);
        if (i >=0) {
            return intervals.get(i);
        }
        i = -i-2; // to index before insertion point
        if (i < 0) {
            return null;
        }
        Interval candidate = intervals.get(i);
        return candidate.contains(date) ? candidate : null;
    }

    private int calculateHash() {
        return 101 + 53 * intervals.hashCode();
    }

    private IntermittentInterval complexIntersection(IntermittentInterval other) {
        List<Interval> collected = new ArrayList<Interval>();
        for (PeriodPair possibleOverlap : possibleOverlaps(other)) {
            if (possibleOverlap.overlap()) {
                collected.add(possibleOverlap.intersection());
            }
        }
        return new IntermittentInterval(collected);
    }

    private boolean complexOverlap(IntermittentInterval other) {
        for (PeriodPair possibleOverlap : possibleOverlaps(other)) {
            if (possibleOverlap.overlap()) {
                return true;
            }
        }
        return false;
    }

    private IntermittentInterval copy() {
        IntermittentInterval union = new IntermittentInterval();
        union.intervals.addAll(getIntervals());
        return union;
    }

    private IntermittentInterval doAdd(Interval period) {
        intervals.add(insertionIndex(intervals, period), period);
        sanitize();
        return this;
    }

    private IntermittentInterval doRemove(Interval Interval) {
        int insertionIndex = insertionIndex(intervals, Interval);
        if (!intervals.isEmpty() && insertionIndex <= intervals.size()) {
            doRemoveAtInsertionIndex(Interval, insertionIndex);
        }
        return this;
    }

    private void doRemoveAtInsertionIndex(Interval interval, int insertionIndex) {
        if (insertionIndex < intervals.size() && intervals.get(insertionIndex).equals(interval)) {
            intervals.remove(insertionIndex);
        }
        eatSubsequentCompleteOverlaps(interval, insertionIndex);
        removePartialAtIndexIfNeeded(interval, insertionIndex);
        removePartialAtPreviousIndexIfNeeded(interval, insertionIndex);
    }

    private void eatSubsequentCompleteOverlaps(Interval interval, int insertionIndex) {
        while (insertionIndex < intervals.size() && interval.includes(intervals.get(insertionIndex))) {
            intervals.remove(insertionIndex);
        }
    }

    private Interval getSpanningPeriod() {
        if (spanningPeriod == null) {
            spanningPeriod = new Interval(intervals.get(0).getStart(), intervals.get(intervals.size() - 1).getEnd());
        }
        return spanningPeriod;
    }

    private int insertionIndex(List<Interval> sanitized, Interval period) {
        int search = Collections.binarySearch(sanitized, period, IntervalComparators.FROM_COMPARATOR);
        return search < 0 ? -search - 1 : search;
    }

    private boolean isCompletelyBefore(IntermittentInterval other) {
        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }
        Date lastTime = intervals.get(intervals.size() - 1).getEnd();
        Date firstTime = other.intervals.get(0).getStart();
        return lastTime != null && firstTime != null && lastTime.compareTo(firstTime) <= 0;
    }

    private void mergePossibleMerges() {
        for (int i = 0; i < intervals.size() - 1; i++) {
            while (i < intervals.size() - 1 && canBeMerged(intervals.get(i), intervals.get(i + 1))) {
                intervals.set(i, merge(intervals.get(i), intervals.get(i + 1)));
                intervals.remove(i + 1);
            }
        }
    }

    private Interval merge(Interval first, Interval second) {
        if (first.equals(second)) {
            return first;
        }
        if (!canBeMerged(first, second)) {
            throw new IllegalArgumentException();
        }
        return new Interval(earliestTime(first.getStart(), second.getStart()), latestTime(first.getEnd(), second.getEnd()));
    }



    private Date earliestTime(Date first, Date second) {
        return first == null || second == null ? null : Ordering.natural().min(first, second);
    }

    private Date latestTime(Date first, Date second) {
        return first == null || second == null ? null : Ordering.natural().max(first, second);
    }

    private boolean canBeMerged(Interval first, Interval second) {
        return first.overlaps(second) || first.abuts(second);
    }

    private PeriodListPair possibleOverlaps(IntermittentInterval other) {
        return new PeriodListPair(intervals, other.intervals);
    }

    private void removePartialAtIndexIfNeeded(Interval interval, int insertionIndex) {
        if (insertionIndex < intervals.size() && interval.overlaps(intervals.get(insertionIndex))) {
            intervals.set(insertionIndex, new Interval(interval.getEnd(), intervals.get(insertionIndex).getEnd()));
        }
    }

    private void removePartialAtPreviousIndexIfNeeded(Interval interval, int insertionIndex) {
        if (insertionIndex > 0 && insertionIndex <= intervals.size() && interval.overlaps(intervals.get(insertionIndex - 1))) {
            Interval predecessor = intervals.get(insertionIndex - 1);
            intervals.set(insertionIndex - 1, new Interval(predecessor.getStart(), interval.getStart()));
            if (predecessor.envelops(interval)) {
                intervals.add(insertionIndex, new Interval(interval.getEnd(), predecessor.getEnd()));
            }
        }
    }

    private void sanitize() {
        Collections.sort(intervals, IntervalComparators.FROM_COMPARATOR);
        mergePossibleMerges();
    }

    private boolean triviallyCannotOverlap(IntermittentInterval other) {
        return this.isEmpty() || other.isEmpty() || this.isCompletelyBefore(other) || other.isCompletelyBefore(this);
    }

    private boolean valueEquals(Object obj) {
        if (obj == null || !(obj instanceof IntermittentInterval)) {
            return false;
        }
        return this.intervals.equals(((IntermittentInterval) obj).intervals);
    }
}
