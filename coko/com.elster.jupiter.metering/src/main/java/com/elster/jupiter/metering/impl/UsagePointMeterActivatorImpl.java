package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.impl.config.SelfObjectValidator;
import com.elster.jupiter.metering.impl.config.SelfValid;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.util.RangeComparatorFactory;
import com.elster.jupiter.util.streams.DecoratedStream;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SelfValid
public class UsagePointMeterActivatorImpl implements UsagePointMeterActivator, SelfObjectValidator {

    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final EventService eventService;

    private List<Activation> activationChanges;
    private List<Activation> deactivationChanges;
    private UsagePointImpl usagePoint;

    @Inject
    public UsagePointMeterActivatorImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService) {
        this.activationChanges = new ArrayList<>();
        this.deactivationChanges = new ArrayList<>();
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.eventService = eventService;
    }

    UsagePointMeterActivatorImpl init(UsagePointImpl usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    private Instant generalizeDatesToMinutes(Instant when) {
        if (when != null) {
            return when.truncatedTo(ChronoUnit.MINUTES);
        } else {
            return when;
        }
    }

    private Instant checkTimeBounds(Instant time) {
        if (time == null || this.usagePoint.getInstallationTime().isAfter(time)) {
            throw new IllegalArgumentException("Activation time can't be less than usage point installation time");
        }
        return time;
    }

    @Override
    public UsagePointMeterActivator activate(Meter meter, MeterRole meterRole) {
        return activate(this.usagePoint.getInstallationTime(), meter, meterRole);
    }

    @Override
    public UsagePointMeterActivator activate(Instant start, Meter meter, MeterRole meterRole) {
        if (meter == null || meterRole == null) {
            throw new IllegalArgumentException("Meter and meter role can't be null");
        }
        start = checkTimeBounds(generalizeDatesToMinutes(start));
        this.activationChanges.add(new Activation(start, meter, meterRole));
        return this;
    }

    @Override
    public UsagePointMeterActivator clear(MeterRole meterRole) {
        return clear(this.usagePoint.getInstallationTime(), meterRole);
    }

    @Override
    public UsagePointMeterActivator clear(Instant from, MeterRole meterRole) {
        from = checkTimeBounds(generalizeDatesToMinutes(from));
        this.deactivationChanges.add(new Activation(from, meterRole));
        return this;
    }

    private TimeLine<Activation, Instant> getMeterTimeLine(Meter meter, Map<Meter, TimeLine<Activation, Instant>> meterTimeLines) {
        TimeLine<Activation, Instant> timeLine = meterTimeLines.get(meter);
        if (timeLine == null) {
            timeLine = new TimeLine<>(Activation::getRange, RangeComparatorFactory.INSTANT_DEFAULT);
            timeLine.addAll(meter.getMeterActivations().stream().map(Activation::new).collect(Collectors.toList()));
            meterTimeLines.put(meter, timeLine);
        }
        return timeLine;
    }

    private Stream<Meter> convertMeterActivationsToStreamOfMeters(List<MeterActivation> meterActivations) {
        return DecoratedStream.decorate(meterActivations.stream())
                .filter(ma -> ma.getMeterRole().isPresent() && ma.getMeter().isPresent())
                .distinct(ma -> ma.getMeter().get())
                .map(ma -> ma.getMeter().get());
    }

    @Override
    public void complete() {
        Map<Meter, TimeLine<Activation, Instant>> meterTimeLines = new HashMap<>();
        convertMeterActivationsToStreamOfMeters(this.usagePoint.getMeterActivations())
                .forEach(meter -> getMeterTimeLine(meter, meterTimeLines));
        AdjustActionVisitor<Activation> clearVisitor = new MeterActivationClearVisitor();
        this.deactivationChanges.forEach(activation ->
                convertMeterActivationsToStreamOfMeters(this.usagePoint.getMeterActivations(activation.meterRole))
                        .forEach(meter -> getMeterTimeLine(meter, meterTimeLines)));
        this.deactivationChanges.forEach(activation ->
                getMeterTimeLine(activation.meter, meterTimeLines).adjust(activation, clearVisitor));
        AdjustActionVisitor<Activation> activateVisitor = new MeterActivationModificationVisitor();
        this.activationChanges.forEach(activation ->
                getMeterTimeLine(activation.meter, meterTimeLines).adjust(activation, activateVisitor));
        // 4 Post event
        eventService.postEvent(EventType.METER_ACTIVATED.topic(), this.usagePoint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointMeterActivatorImpl that = (UsagePointMeterActivatorImpl) o;
        return usagePoint.equals(that.usagePoint);
    }

    @Override
    public int hashCode() {
        return this.usagePoint.hashCode();
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        return true; // TODO
    }

    private static final class Activation implements Comparable<Activation> {
        Instant start;
        Instant end;
        Meter meter;
        MeterRole meterRole;
        MeterActivationImpl meterActivation;

        Activation(Instant start, MeterRole meterRole) {
            this.start = start;
            this.meterRole = meterRole;
        }

        Activation(Instant start, Meter meter, MeterRole meterRole) {
            this.start = start;
            this.meterRole = meterRole;
            this.meter = meter;
        }

        Activation(MeterActivation meterActivation) {
            this.start = meterActivation.getStart();
            this.end = meterActivation.getEnd();
            this.meter = meterActivation.getMeter().orElse(null);
            this.meterRole = meterActivation.getMeterRole().orElse(null);
            this.meterActivation = (MeterActivationImpl) meterActivation;
        }

        Range<Instant> getRange() {
            if (this.meterActivation != null) {
                return this.meterActivation.getRange();
            }
            return this.end == null ? Range.atLeast(this.start) : Range.closedOpen(this.start, this.end);
        }

        @Override
        public int compareTo(Activation other) {
            return RangeComparatorFactory.INSTANT_DEFAULT.compare(getRange(), other.getRange());
        }
    }

    enum RangePosition {
        FIRST, MIDDLE, LAST, SINGLE
    }

    static abstract class AdjustActionVisitor<T> {
        public List<T> before(T first, T modifier, RangePosition position) {
            return null;
        }

        public List<T> after(T last, T modifier, RangePosition position) {
            return null;
        }

        public List<T> replace(T element, T modifier, RangePosition position) {
            return null;
        }

        public List<T> split(T element, T modifier, RangePosition position) {
            return null;
        }

        public List<T> cropStart(T element, T modifier, RangePosition position) {
            return null;
        }

        public List<T> cropEnd(T element, T modifier, RangePosition position) {
            return null;
        }
    }

    class MeterActivationModificationVisitor extends AdjustActionVisitor<Activation> {

        protected MeterActivationImpl createNewMeterActivation(Meter meter, MeterRole meterRole, Range<Instant> range) {
            MeterActivationImpl meterActivation = metrologyConfigurationService.getDataModel()
                    .getInstance(MeterActivationImpl.class)
                    .init(meter, meterRole, usagePoint, range);
            meterActivation.save();
            return meterActivation;
        }

        protected void copyMultipliers(MeterActivation target, MeterActivation source) {
            source.getMultipliers().entrySet().stream()
                    .forEach(entry -> target.setMultiplier(entry.getKey(), entry.getValue()));
        }

        protected boolean extendActivationEnd(Activation last, Activation modifier, RangePosition position) {
            if (position == RangePosition.LAST || position == RangePosition.SINGLE) {
                last.meterActivation.doEndAt(modifier.getRange().hasUpperBound() ? modifier.getRange().upperEndpoint() : null);
                return true;
            }
            return false;
        }

        protected void extendActivationStart(Activation first, Activation modifier, RangePosition position) {
            if ((position == RangePosition.FIRST || position == RangePosition.SINGLE)
                    && modifier.getRange().lowerEndpoint().isBefore(first.meterActivation.getRange().lowerEndpoint())) {
                first.meterActivation.advanceStartDate(modifier.getRange().lowerEndpoint());
            }
        }

        @Override
        public List<Activation> before(Activation first, Activation modifier, RangePosition position) {
            Activation activation = new Activation(createNewMeterActivation(modifier.meter, modifier.meterRole, modifier.getRange()));
            if (first != null) {
                first.meterActivation.advanceStartDate(modifier.getRange().upperEndpoint());
                first.meterActivation.save();
            }
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> after(Activation last, Activation modifier, RangePosition position) {
            last.meterActivation.doEndAt(modifier.getRange().lowerEndpoint());
            MeterActivationImpl ma = createNewMeterActivation(modifier.meter, modifier.meterRole, modifier.getRange());
            copyMultipliers(ma, last.meterActivation);
            return Collections.singletonList(new Activation(ma));
        }

        @Override
        public List<Activation> replace(Activation element, Activation modifier, RangePosition position) {
            element.meterActivation.doSetMeterRole(modifier.meterRole);
            element.meterActivation.doSetUsagePoint(usagePoint);
            extendActivationStart(element, modifier, position);
            if (!extendActivationEnd(element, modifier, position)) {
                element.meterActivation.save();
            }
            return null;
        }

        @Override
        public List<Activation> split(Activation element, Activation modifier, RangePosition position) {
            List<Activation> activations = new ArrayList<>();
            activations.add(new Activation(element.meterActivation.split(modifier.getRange().upperEndpoint())));
            Activation activation = new Activation(element.meterActivation.split(modifier.getRange().upperEndpoint()));
            activation.meterActivation.doSetMeterRole(modifier.meterRole);
            activation.meterActivation.doSetUsagePoint(usagePoint);
            activation.meterActivation.save();
            activations.add(activation);
            return activations;
        }

        @Override
        public List<Activation> cropStart(Activation element, Activation modifier, RangePosition position) {
            MeterActivation ma = element.meterActivation.split(modifier.getRange().upperEndpoint());
            element.meterActivation.doSetMeterRole(modifier.meterRole);
            element.meterActivation.doSetUsagePoint(usagePoint);
            element.meterActivation.save();
            extendActivationStart(element, modifier, position);
            return Collections.singletonList(new Activation(ma));
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, RangePosition position) {
            MeterActivation ma = element.meterActivation.split(modifier.getRange().lowerEndpoint());
            Activation activation = new Activation(ma);
            activation.meterActivation.doSetMeterRole(modifier.meterRole);
            activation.meterActivation.doSetUsagePoint(usagePoint);
            activation.meterActivation.save();
            extendActivationEnd(activation, modifier, position);
            return Collections.singletonList(activation);
        }
    }

    class MeterActivationClearVisitor extends MeterActivationModificationVisitor {
        @Override
        public List<Activation> before(Activation first, Activation modifier, RangePosition position) {
            return null; // do nothing
        }

        @Override
        public List<Activation> after(Activation last, Activation modifier, RangePosition position) {
            return null; // do nothing
        }

        @Override
        public List<Activation> replace(Activation element, Activation modifier, RangePosition position) {
            element.meterActivation.detachUsagePoint();
            return null;
        }

        @Override
        public List<Activation> split(Activation element, Activation modifier, RangePosition position) {
            List<Activation> activations = new ArrayList<>();
            activations.add(new Activation(element.meterActivation.split(modifier.getRange().upperEndpoint())));
            Activation activation = new Activation(element.meterActivation.split(modifier.getRange().upperEndpoint()));
            activation.meterActivation.detachUsagePoint();
            activations.add(activation);
            return activations;
        }

        @Override
        public List<Activation> cropStart(Activation element, Activation modifier, RangePosition position) {
            MeterActivation ma = element.meterActivation.split(modifier.getRange().upperEndpoint());
            element.meterActivation.detachUsagePoint();
            return Collections.singletonList(new Activation(ma));
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, RangePosition position) {
            MeterActivation ma = element.meterActivation.split(modifier.getRange().lowerEndpoint());
            Activation activation = new Activation(ma);
            activation.meterActivation.detachUsagePoint();
            return Collections.singletonList(activation);
        }
    }

    static class TimeLine<T, I extends Comparable> {
        private final Function<T, Range<I>> rangeExtractor;
        private final Comparator<T> comparator;
        private List<T> ranges;

        public TimeLine(Function<T, Range<I>> rangeExtractor, Comparator<Range<I>> comparator) {
            this.rangeExtractor = rangeExtractor;
            this.ranges = new ArrayList<>();
            this.comparator = (t1, t2) -> comparator.compare(rangeExtractor.apply(t1), rangeExtractor.apply(t2));
        }

        private void add(T element, Collection<T> collection) {
            if (element != null) {
                collection.add(element);
            }
        }

        public void add(T element) {
            add(element, this.ranges);
            Collections.sort(this.ranges, this.comparator);
        }

        public void addAll(Collection<T> elements, Collection<T> target) {
            if (elements != null && !elements.isEmpty()) {
                target.addAll(elements);
            }
        }

        public void addAll(Collection<T> elements) {
            addAll(elements, this.ranges);
            Collections.sort(this.ranges, this.comparator);
        }

        public void adjust(T modifier, AdjustActionVisitor<T> actionVisitor) {
            Iterator<T> rangesIterator = this.ranges.iterator();
            List<T> newElementsAccumulator = new ArrayList<>();
            if (!rangesIterator.hasNext()) {
                addAll(actionVisitor.before(null, modifier, RangePosition.FIRST), newElementsAccumulator);
            } else {
                RangePosition elementPosition = RangePosition.FIRST;
                Range<I> modifierRange = this.rangeExtractor.apply(modifier);
                while (rangesIterator.hasNext()) {
                    T element = rangesIterator.next();
                    if (!rangesIterator.hasNext()) {
                        elementPosition = elementPosition == RangePosition.FIRST ? RangePosition.SINGLE : RangePosition.LAST;
                    }
                    adjustElement(element, elementPosition, modifier, modifierRange, actionVisitor, newElementsAccumulator);
                    elementPosition = RangePosition.MIDDLE;
                }
            }
            addAll(newElementsAccumulator);
        }

        private void adjustElement(T element, RangePosition elementPosition, T modifier, Range<I> modifierRange, AdjustActionVisitor<T> actionVisitor, List<T> newElementsAccumulator) {
            Range<I> elementRange = this.rangeExtractor.apply(element);
            if (!elementRange.isConnected(modifierRange)) {
                int comparison = this.comparator.compare(element, modifier);
                if (comparison > 0 && (elementPosition == RangePosition.FIRST || elementPosition == RangePosition.SINGLE)) {
                    addAll(actionVisitor.before(element, modifier, elementPosition), newElementsAccumulator);
                } else if (comparison < 0 && (elementPosition == RangePosition.LAST || elementPosition == RangePosition.SINGLE)) {
                    addAll(actionVisitor.after(element, modifier, elementPosition), newElementsAccumulator);
                }
            } else if (modifierRange.encloses(elementRange)) {
                addAll(actionVisitor.replace(element, modifier, elementPosition), newElementsAccumulator);
            } else if (elementRange.encloses(modifierRange)) {
                addAll(actionVisitor.split(element, modifier, elementPosition), newElementsAccumulator);
            } else {
                Range<I> intersection = elementRange.intersection(modifierRange);
                if (!intersection.isEmpty()) {
                    if (intersection.lowerEndpoint().compareTo(elementRange.lowerEndpoint()) > 0) {
                        addAll(actionVisitor.cropEnd(element, modifier, elementPosition), newElementsAccumulator);
                    } else {
                        addAll(actionVisitor.cropStart(element, modifier, elementPosition), newElementsAccumulator);
                    }
                }
            }
        }
    }
}
