package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointManageException;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.SelfObjectValidator;
import com.elster.jupiter.metering.impl.config.SelfValid;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.RangeComparatorFactory;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.streams.DecoratedStream;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
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
import java.util.Objects;
import java.util.Optional;
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
    private Map<Meter, TimeLine<Activation, Instant>> meterTimeLines;

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
        }
        return null;
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
        this.activationChanges.add(new VirtualActivation(start, this.usagePoint, meter, meterRole));
        updateMeterDefaultLocation(meter);
        return this;
    }

    @Override
    public UsagePointMeterActivator clear(MeterRole meterRole) {
        return clear(this.usagePoint.getInstallationTime(), meterRole);
    }

    @Override
    public UsagePointMeterActivator clear(Instant from, MeterRole meterRole) {
        from = checkTimeBounds(generalizeDatesToMinutes(from));
        this.deactivationChanges.add(new VirtualActivation(from, this.usagePoint, meterRole));
        return this;
    }

    public UsagePointMeterActivator clear(Range<Instant> range, MeterRole meterRole) {
        Instant start = checkTimeBounds(generalizeDatesToMinutes(range.lowerEndpoint()));
        VirtualActivation activation = new VirtualActivation(start, this.usagePoint, meterRole);
        if (range.hasUpperBound()) {
            activation.endAt(generalizeDatesToMinutes(range.upperEndpoint()));
        }
        this.deactivationChanges.add(activation);
        return this;
    }

    @Override
    public void complete() {
        if (this.activationChanges.isEmpty() && this.deactivationChanges.isEmpty()) {
            return;
        }
        this.meterTimeLines = new HashMap<>();
        convertMeterActivationsToStreamOfMeters(this.usagePoint.getMeterActivations())
                .forEach(meter -> getMeterTimeLine(meter, this.meterTimeLines));
        ElementVisitor<Activation> clearVisitor = new MeterActivationClearVisitor();
        this.deactivationChanges.forEach(activation ->
                convertMeterActivationsToStreamOfMeters(this.usagePoint.getMeterActivations(activation.getMeterRole()))
                        .forEach(meter -> getMeterTimeLine(meter, this.meterTimeLines).adjust(activation, clearVisitor)));
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), this);
        ElementVisitor<Activation> activateVisitor = new MeterActivationModificationVisitor(this.metrologyConfigurationService.getDataModel());
        this.activationChanges.forEach(activation ->
                getMeterTimeLine(activation.getMeter(), this.meterTimeLines).adjust(activation, activateVisitor));
        this.usagePoint.touch();
        refreshMeterActivations();
        eventService.postEvent(EventType.METER_ACTIVATED.topic(), this.usagePoint);
    }

    private Stream<Meter> convertMeterActivationsToStreamOfMeters(List<MeterActivation> meterActivations) {
        return DecoratedStream.decorate(meterActivations.stream())
                .filter(ma -> ma.getMeterRole().isPresent() && ma.getMeter().isPresent())
                .distinct(ma -> ma.getMeter().get())
                .map(ma -> ma.getMeter().get());
    }

    private TimeLine<Activation, Instant> getMeterTimeLine(Meter meter, Map<Meter, TimeLine<Activation, Instant>> meterTimeLines) {
        return getMeterTimeLine(meter, meterTimeLines, WrappedActivation::new);
    }

    private TimeLine<Activation, Instant> getMeterTimeLine(Meter meter, Map<Meter, TimeLine<Activation, Instant>> meterTimeLines, Function<MeterActivation, Activation> mapper) {
        TimeLine<Activation, Instant> timeLine = meterTimeLines.get(meter);
        if (timeLine == null) {
            timeLine = new TimeLine<>(Activation::getRange, RangeComparatorFactory.INSTANT_DEFAULT);
            timeLine.addAll(meter.getMeterActivations().stream().map(mapper).collect(Collectors.toList()));
            meterTimeLines.put(meter, timeLine);
        }
        return timeLine;
    }

    private void refreshMeterActivations() {
        this.usagePoint.refreshMeterActivations();
        this.activationChanges.stream()
                .map(Activation::getMeter)
                .filter(Objects::nonNull)
                .map(MeterImpl.class::cast)
                .forEach(MeterImpl::refreshMeterActivations);
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        ValidationReport validationReport = new FormValidationReport(context, this.metrologyConfigurationService.getThesaurus());
        validate(validationReport);
        validationReport.process();
        return validationReport.isValid();
    }

    private void validate(ValidationReport validationReport) {
        if (this.usagePoint.getConnectionState() != ConnectionState.UNDER_CONSTRUCTION) {
            throw UsagePointManageException.incorrectState(this.metrologyConfigurationService.getThesaurus(), this.usagePoint.getMRID());
        }
        Map<Meter, TimeLine<Activation, Instant>> validationTimeLines = new HashMap<>();
        this.meterTimeLines.entrySet().forEach(entry -> {
            TimeLine<Activation, Instant> timeLine = new TimeLine<>(Activation::getRange, RangeComparatorFactory.INSTANT_DEFAULT);
            timeLine.addAll(entry.getValue().getElements(VirtualActivation::new));
            validationTimeLines.put(entry.getKey(), timeLine);
        });
        ValidateActivationVisitor formValidationVisitor = new ValidateActivationVisitor(validationReport);
        this.activationChanges.forEach(activation ->
                getMeterTimeLine(activation.getMeter(), validationTimeLines, VirtualActivation::new).adjust(activation, formValidationVisitor));
        validateMetersCapabilities(validationReport);
        validateByCustomValidators(validationReport);
    }

    private void validateMetersCapabilities(ValidationReport validationReport) {
        List<EffectiveMetrologyConfigurationOnUsagePoint> emcList = this.usagePoint.getEffectiveMetrologyConfigurations();
        for (Activation activation : this.activationChanges) {
            for (EffectiveMetrologyConfigurationOnUsagePoint emc : emcList) {
                if (!emc.getRange().isConnected(activation.getRange())) {
                    continue;
                }
                UsagePointMetrologyConfiguration metrologyConfiguration = emc.getMetrologyConfiguration();
                List<ReadingTypeRequirement> mandatoryReadingTypeRequirements = metrologyConfiguration.getMandatoryReadingTypeRequirements();
                List<ReadingType> readingTypesOnMeter = new ArrayList<>();
                activation.getMeter().getHeadEndInterface()
                        .map(headEndInterface -> headEndInterface.getCapabilities(activation.getMeter()))
                        .ifPresent(endDeviceCapabilities -> readingTypesOnMeter.addAll(endDeviceCapabilities.getConfiguredReadingTypes()));
                List<ReadingTypeRequirement> unmatchedRequirements = metrologyConfiguration.getRequirements(activation.getMeterRole())
                        .stream()
                        .filter(mandatoryReadingTypeRequirements::contains)
                        .filter(requirement -> !readingTypesOnMeter.stream().anyMatch(requirement::matches))
                        .collect(Collectors.toList());
                if (!unmatchedRequirements.isEmpty()) {
                    validationReport.hasUnmatchedRequirements(activation, unmatchedRequirements);
                }
            }
        }
    }

    private void validateByCustomValidators(ValidationReport validationReport) {
        for (Activation activation : this.activationChanges) {
            try {
                this.metrologyConfigurationService.validateUsagePointMeterActivation(activation.getMeterRole(), activation.getMeter(), activation.getUsagePoint());
            } catch (CustomUsagePointMeterActivationValidationException ex) {
                validationReport.failedByCustomValidator(activation, ex.getLocalizedMessage());
            }
        }
    }

    private void updateMeterDefaultLocation(Meter meter) {
        Optional<Location> usagePointLocation = usagePoint.getLocation();
        Optional<SpatialCoordinates> usagePointSpatialCoordinates = usagePoint.getSpatialCoordinates();
        if (usagePointLocation.isPresent()
                && (!meter.getLocation().isPresent() || meter.getLocation().get().getId() == usagePointLocation.get().getId())) {
            this.metrologyConfigurationService.getDataModel().mapper(Location.class).getOptional(usagePointLocation.get().getId()).ifPresent(meter::setLocation);
            if (usagePointSpatialCoordinates.isPresent() &&
                    (!meter.getSpatialCoordinates().isPresent() || meter.getSpatialCoordinates()
                            .get().equals(usagePointSpatialCoordinates.get()))) {
                meter.setSpatialCoordinates(usagePointSpatialCoordinates.get());
                meter.update();
            }
        }
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

    private interface Activation {
        Range<Instant> getRange();

        Meter getMeter();

        MeterRole getMeterRole();

        UsagePoint getUsagePoint();

        void endAt(Instant end);

        void startAt(Instant start);

        void setUsagePoint(UsagePoint usagePoint);

        void setMeterRole(MeterRole meterRole);

        Activation split(Instant breakTime);

        void save();

        default boolean hasTheSameUsagePointAs(Activation modifier) {
            return getUsagePoint() != null && getUsagePoint().equals(modifier.getUsagePoint());
        }

        default boolean hasTheSameMeterRoleAs(Activation modifier) {
            return getMeterRole() != null && getMeterRole().equals(modifier.getMeterRole());
        }
    }

    private static final class VirtualActivation implements Activation {
        private Instant start;
        private Instant end;
        private Meter meter;
        private MeterRole meterRole;
        private UsagePoint usagePoint;

        public VirtualActivation(Instant start, UsagePoint usagePoint, MeterRole meterRole) {
            this.start = start;
            this.meterRole = meterRole;
            this.usagePoint = usagePoint;
        }

        public VirtualActivation(Instant start, UsagePoint usagePoint, Meter meter, MeterRole meterRole) {
            this.start = start;
            this.meterRole = meterRole;
            this.usagePoint = usagePoint;
            this.meter = meter;
        }

        public VirtualActivation(Activation activation) {
            Range<Instant> activationRange = activation.getRange();
            this.start = activationRange.lowerEndpoint();
            this.meterRole = activation.getMeterRole();
            this.usagePoint = activation.getUsagePoint();
            this.meter = activation.getMeter();
            this.end = activationRange.hasUpperBound() ? activationRange.upperEndpoint() : null;
        }

        public VirtualActivation(MeterActivation meterActivation) {
            this.start = meterActivation.getStart();
            this.meterRole = meterActivation.getMeterRole().orElse(null);
            this.usagePoint = meterActivation.getUsagePoint().orElse(null);
            this.meter = meterActivation.getMeter().orElse(null);
            this.end = meterActivation.getEnd();
        }

        public Range<Instant> getRange() {
            return this.end == null ? Range.atLeast(this.start) : Range.closedOpen(this.start, this.end);
        }

        @Override
        public Meter getMeter() {
            return meter;
        }

        @Override
        public MeterRole getMeterRole() {
            return meterRole;
        }

        @Override
        public UsagePoint getUsagePoint() {
            return usagePoint;
        }

        @Override
        public void endAt(Instant end) {
            this.end = end;
        }

        @Override
        public void startAt(Instant start) {
            this.start = start;
        }

        @Override
        public void setUsagePoint(UsagePoint usagePoint) {
            this.usagePoint = usagePoint;
        }

        @Override
        public void setMeterRole(MeterRole meterRole) {
            this.meterRole = meterRole;
        }

        @Override
        public Activation split(Instant breakTime) {
            VirtualActivation activation = new VirtualActivation(breakTime, this.usagePoint, this.meter, this.meterRole);
            endAt(breakTime);
            return activation;
        }

        @Override
        public void save() {
            // do nothing
        }
    }

    private static final class WrappedActivation implements Activation {
        private final MeterActivationImpl meterActivation;

        public WrappedActivation(MeterActivation meterActivation) {
            this.meterActivation = (MeterActivationImpl) meterActivation;
        }

        @Override
        public Range<Instant> getRange() {
            return this.meterActivation.getRange();
        }

        @Override
        public Meter getMeter() {
            return this.meterActivation.getMeter().orElse(null);
        }

        @Override
        public MeterRole getMeterRole() {
            return this.meterActivation.getMeterRole().orElse(null);
        }

        @Override
        public UsagePoint getUsagePoint() {
            return this.meterActivation.getUsagePoint().orElse(null);
        }

        @Override
        public void endAt(Instant end) {
            this.meterActivation.doEndAt(end);
        }

        @Override
        public void startAt(Instant start) {
            this.meterActivation.advanceStartDate(start);
        }

        @Override
        public void setUsagePoint(UsagePoint usagePoint) {
            if (usagePoint == null) {
                this.meterActivation.detachUsagePoint();
            } else {
                this.meterActivation.doSetUsagePoint(usagePoint);
            }
        }

        @Override
        public void setMeterRole(MeterRole meterRole) {
            this.meterActivation.doSetMeterRole(meterRole);
        }

        @Override
        public Activation split(Instant breakTime) {
            return new WrappedActivation(this.meterActivation.split(breakTime));
        }

        @Override
        public void save() {
            this.meterActivation.save();
        }
    }

    private enum ElementPosition {
        FIRST, MIDDLE, LAST, SINGLE
    }

    /**
     * Methods of this class will be called based on position which modifier takes relative to an object from timeline.
     * <p></p>
     * For example: if in timeline we have an object with range {@code [10..17)} and we call {@link TimeLine#adjust(Object, ElementVisitor)}
     * method with modifier with range {@code [3..7)}, then the {@link #before(Object, Object, ElementPosition)} method will be called,
     * because modifier is located on timeline before the original object.
     * <p></p>
     * All of these methods can return a list with new objects which should be added to timeline after the {@link TimeLine#adjust(Object, ElementVisitor)}
     * operation.
     *
     * @param <T> type of objects in timeline
     */
    private interface ElementVisitor<T> {
        List<T> before(T first, T modifier, ElementPosition position);

        List<T> after(T last, T modifier, ElementPosition position);

        List<T> replace(T element, T modifier, ElementPosition position);

        List<T> split(T element, T modifier, ElementPosition position);

        List<T> cropStart(T element, T modifier, ElementPosition position);

        List<T> cropEnd(T element, T modifier, ElementPosition position);
    }

    /**
     * Contains list of sorted object. Gaps and overlaps are not allowed.
     *
     * @param <T>
     * @param <I>
     */
    private static class TimeLine<T, I extends Comparable<? super I>> {
        private final Function<T, Range<I>> rangeExtractor;
        private final Comparator<T> comparator;
        private final List<T> ranges;

        public TimeLine(Function<T, Range<I>> rangeExtractor, Comparator<Range<I>> comparator) {
            this.rangeExtractor = rangeExtractor;
            this.ranges = new ArrayList<>();
            this.comparator = (t1, t2) -> comparator.compare(rangeExtractor.apply(t1), rangeExtractor.apply(t2));
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

        public List<T> getElements(Function<T, T> mapper) {
            Collections.sort(this.ranges, this.comparator);
            return this.ranges.stream().map(mapper).collect(Collectors.toList());
        }

        public void adjust(T modifier, ElementVisitor<T> elementVisitor) {
            Iterator<T> rangesIterator = this.ranges.iterator();
            List<T> newElementsAccumulator = new ArrayList<>();
            if (!rangesIterator.hasNext()) {
                addAll(elementVisitor.before(null, modifier, ElementPosition.FIRST), newElementsAccumulator);
            } else {
                ElementPosition elementPosition = ElementPosition.FIRST;
                Range<I> modifierRange = this.rangeExtractor.apply(modifier);
                while (rangesIterator.hasNext()) {
                    T element = rangesIterator.next();
                    if (!rangesIterator.hasNext()) {
                        elementPosition = elementPosition == ElementPosition.FIRST ? ElementPosition.SINGLE : ElementPosition.LAST;
                    }
                    adjustElement(element, elementPosition, modifier, modifierRange, elementVisitor, newElementsAccumulator);
                    elementPosition = ElementPosition.MIDDLE;
                }
            }
            addAll(newElementsAccumulator);
        }

        private void adjustElement(T element, ElementPosition elementPosition, T modifier, Range<I> modifierRange, ElementVisitor<T> elementVisitor, List<T> newElementsAccumulator) {
            Range<I> elementRange = this.rangeExtractor.apply(element);
            if (!elementRange.isConnected(modifierRange)) {
                int comparison = this.comparator.compare(element, modifier);
                if (comparison > 0 && (elementPosition == ElementPosition.FIRST || elementPosition == ElementPosition.SINGLE)) {
                    addAll(elementVisitor.before(element, modifier, elementPosition), newElementsAccumulator);
                } else if (comparison < 0 && (elementPosition == ElementPosition.LAST || elementPosition == ElementPosition.SINGLE)) {
                    addAll(elementVisitor.after(element, modifier, elementPosition), newElementsAccumulator);
                }
            } else if (modifierRange.encloses(elementRange)) {
                addAll(elementVisitor.replace(element, modifier, elementPosition), newElementsAccumulator);
            } else if (elementRange.lowerEndpoint().compareTo(modifierRange.lowerEndpoint()) < 0
                    && modifierRange.hasUpperBound()
                    && (!elementRange.hasUpperBound() || elementRange.upperEndpoint().compareTo(modifierRange.upperEndpoint()) > 0)) {
                addAll(elementVisitor.split(element, modifier, elementPosition), newElementsAccumulator);
            } else {
                Range<I> intersection = elementRange.intersection(modifierRange);
                if (!intersection.isEmpty()) {
                    if (intersection.lowerEndpoint().compareTo(elementRange.lowerEndpoint()) > 0) {
                        addAll(elementVisitor.cropEnd(element, modifier, elementPosition), newElementsAccumulator);
                    } else {
                        addAll(elementVisitor.cropStart(element, modifier, elementPosition), newElementsAccumulator);
                    }
                }
            }
        }
    }

    private static class MeterActivationModificationVisitor implements ElementVisitor<Activation> {
        private final DataModel dataModel;

        public MeterActivationModificationVisitor(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        Activation createNewMeterActivation(Activation activation) {
            MeterActivationImpl meterActivation = this.dataModel.getInstance(MeterActivationImpl.class)
                    .init(activation.getMeter(), activation.getMeterRole(), activation.getUsagePoint(), activation.getRange());
            meterActivation.save();
            return new WrappedActivation(meterActivation);
        }

        private void extendActivationEnd(Activation last, Activation modifier, ElementPosition position) {
            if (position == ElementPosition.LAST || position == ElementPosition.SINGLE) {
                last.endAt(modifier.getRange().hasUpperBound() ? modifier.getRange().upperEndpoint() : null);
            }
        }

        private void extendActivationStart(Activation first, Activation modifier, ElementPosition position) {
            if ((position == ElementPosition.FIRST || position == ElementPosition.SINGLE)
                    && modifier.getRange().lowerEndpoint().isBefore(first.getRange().lowerEndpoint())) {
                first.startAt(modifier.getRange().lowerEndpoint());
            }
        }

        @Override
        public List<Activation> before(Activation first, Activation modifier, ElementPosition position) {
            Activation activation = createNewMeterActivation(modifier);
            if (first != null) {
                first.startAt(modifier.getRange().upperEndpoint());
                first.save();
            }
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> after(Activation last, Activation modifier, ElementPosition position) {
            last.endAt(modifier.getRange().hasUpperBound() ? modifier.getRange().upperEndpoint() : null);
            Activation activation = last.split(modifier.getRange().lowerEndpoint());
            activation.setUsagePoint(modifier.getUsagePoint());
            activation.save();
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> replace(Activation element, Activation modifier, ElementPosition position) {
            element.setMeterRole(modifier.getMeterRole());
            element.setUsagePoint(modifier.getUsagePoint());
            element.save();
            extendActivationStart(element, modifier, position);
            extendActivationEnd(element, modifier, position);
            return null;
        }

        @Override
        public List<Activation> split(Activation element, Activation modifier, ElementPosition position) {
            List<Activation> activations = new ArrayList<>();
            activations.add(element.split(modifier.getRange().upperEndpoint()));
            Activation activation = element.split(modifier.getRange().upperEndpoint());
            activation.setMeterRole(modifier.getMeterRole());
            activation.setUsagePoint(modifier.getUsagePoint());
            activation.save();
            activations.add(activation);
            return activations;
        }

        @Override
        public List<Activation> cropStart(Activation element, Activation modifier, ElementPosition position) {
            Activation activation = element.split(modifier.getRange().upperEndpoint());
            element.setMeterRole(modifier.getMeterRole());
            element.setUsagePoint(modifier.getUsagePoint());
            element.save();
            extendActivationStart(element, modifier, position);
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, ElementPosition position) {
            Activation activation = element.split(modifier.getRange().lowerEndpoint());
            activation.setMeterRole(modifier.getMeterRole());
            activation.setUsagePoint(modifier.getUsagePoint());
            activation.save();
            extendActivationEnd(activation, modifier, position);
            return Collections.singletonList(activation);
        }
    }

    private static class MeterActivationClearVisitor implements ElementVisitor<Activation> {
        @Override
        public List<Activation> before(Activation first, Activation modifier, ElementPosition position) {
            return null; // do nothing
        }

        @Override
        public List<Activation> after(Activation last, Activation modifier, ElementPosition position) {
            return null; // do nothing
        }

        @Override
        public List<Activation> replace(Activation element, Activation modifier, ElementPosition position) {
            if (!element.hasTheSameUsagePointAs(modifier) || !element.hasTheSameMeterRoleAs(modifier)) {
                return null;
            }
            element.setUsagePoint(null);
            element.save();
            return null;
        }

        @Override
        public List<Activation> split(Activation element, Activation modifier, ElementPosition position) {
            if (!element.hasTheSameUsagePointAs(modifier) || !element.hasTheSameMeterRoleAs(modifier)) {
                return null;
            }
            List<Activation> activations = new ArrayList<>();
            activations.add(element.split(modifier.getRange().upperEndpoint()));
            Activation activation = element.split(modifier.getRange().lowerEndpoint());
            activation.setUsagePoint(null);
            activation.save();
            activations.add(activation);
            return activations;
        }

        @Override
        public List<Activation> cropStart(Activation element, Activation modifier, ElementPosition position) {
            if (!element.hasTheSameUsagePointAs(modifier) || !element.hasTheSameMeterRoleAs(modifier)) {
                return null;
            }
            Activation activation = element.split(modifier.getRange().upperEndpoint());
            element.setUsagePoint(null);
            element.save();
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, ElementPosition position) {
            if (!element.hasTheSameUsagePointAs(modifier) || !element.hasTheSameMeterRoleAs(modifier)) {
                return null;
            }
            Activation activation = element.split(modifier.getRange().lowerEndpoint());
            activation.setUsagePoint(null);
            activation.save();
            return Collections.singletonList(activation);
        }
    }

    private static abstract class ValidationReport {
        private boolean isValid = true;
        private final Map<Activation, Activation> activeOnDifferentUsagePoint = new HashMap<>();
        private final List<Activation> activeOnDifferentMeterRole = new ArrayList<>();
        private final Map<Activation, String> failedByCustomValidators = new HashMap<>();
        private final Map<Activation, List<ReadingTypeRequirement>> unmatchedRequirements = new HashMap<>();

        public boolean isValid() {
            return this.isValid;
        }

        public void activeOnDifferentUsagePoint(Activation newActivation, Activation existingActivation) {
            this.isValid = false;
            this.activeOnDifferentUsagePoint.put(newActivation, existingActivation);
        }

        public void activeOnDifferentMeterRole(Activation activation) {
            this.isValid = false;
            this.activeOnDifferentMeterRole.add(activation);
        }

        public void failedByCustomValidator(Activation activation, String message) {
            this.isValid = false;
            this.failedByCustomValidators.put(activation, message);
        }

        public void hasUnmatchedRequirements(Activation activation, List<ReadingTypeRequirement> requirements) {
            this.isValid = false;
            this.unmatchedRequirements.put(activation, requirements);
        }

        public Map<Activation, Activation> getActiveOnDifferentUsagePoint() {
            return Collections.unmodifiableMap(this.activeOnDifferentUsagePoint);
        }

        public List<Activation> getActiveOnDifferentMeterRole() {
            return Collections.unmodifiableList(this.activeOnDifferentMeterRole);
        }

        public Map<Activation, String> getFailedByCustomValidators() {
            return Collections.unmodifiableMap(this.failedByCustomValidators);
        }

        public Map<Activation, List<ReadingTypeRequirement>> getUnmatchedRequirements() {
            return Collections.unmodifiableMap(this.unmatchedRequirements);
        }

        public abstract void process();
    }

    private static class FormValidationReport extends ValidationReport {

        private final ConstraintValidatorContext context;
        private final Thesaurus thesaurus;

        public FormValidationReport(ConstraintValidatorContext context, Thesaurus thesaurus) {
            this.context = context;
            this.thesaurus = thesaurus;
        }

        @Override
        public void process() {
            if (!isValid()) {
                this.context.disableDefaultConstraintViolation();
                getActiveOnDifferentUsagePoint().entrySet().forEach(entry -> {
                    Activation existingActivation = entry.getValue();
                    String errorMessage = this.thesaurus.getFormat(MessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT)
                            .format(existingActivation.getMeter().getMRID(), existingActivation.getUsagePoint().getMRID(), existingActivation.getMeterRole().getDisplayName());
                    this.context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode(entry.getKey().getMeterRole().getKey()).addConstraintViolation();
                });
                getActiveOnDifferentMeterRole().stream().forEach(activation ->
                        this.context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT + "}")
                                .addPropertyNode(activation.getMeterRole().getKey()).addConstraintViolation());
                getUnmatchedRequirements().entrySet().stream().forEach(entry -> {
                    String messageTemplate = this.thesaurus.getString(MessageSeeds.UNSATISFIED_METROLOGY_REQUIREMENT.getKey(),
                            MessageSeeds.UNSATISFIED_METROLOGY_REQUIREMENT.getDefaultFormat());
                    String errorMessage = MessageFormat.format(messageTemplate,
                            entry.getValue().stream().map(ReadingTypeRequirement::getDescription).collect(Collectors.joining(", ")));
                    this.context.buildConstraintViolationWithTemplate(errorMessage)
                            .addPropertyNode(entry.getKey().getMeterRole().getKey())
                            .addConstraintViolation();
                });
                getFailedByCustomValidators().entrySet().forEach(entry ->
                        this.context.buildConstraintViolationWithTemplate(entry.getValue())
                                .addPropertyNode(entry.getKey().getMeterRole().getKey())
                                .addConstraintViolation());
            }
        }
    }

    private static class ValidateActivationVisitor extends MeterActivationModificationVisitor {
        private final ValidationReport report;

        public ValidateActivationVisitor(ValidationReport report) {
            super(null);
            this.report = report;
        }

        @Override
        protected Activation createNewMeterActivation(Activation activation) {
            return new VirtualActivation(activation.getRange().lowerEndpoint(), activation.getUsagePoint(), activation.getMeter(), activation.getMeterRole());
        }

        private void compareActivations(Activation element, Activation modifier) {
            if (element.getMeterRole() != null && element.getUsagePoint() != null && !element.hasTheSameUsagePointAs(modifier)) {
                this.report.activeOnDifferentUsagePoint(modifier, element);
            }
            if (element.getUsagePoint() != null && element.hasTheSameUsagePointAs(modifier)
                    && element.getMeterRole() != null && !element.hasTheSameMeterRoleAs(modifier)) {
                this.report.activeOnDifferentMeterRole(modifier);
            }
        }

        @Override
        public List<Activation> replace(Activation element, Activation modifier, ElementPosition position) {
            compareActivations(element, modifier);
            return super.replace(element, modifier, position);
        }

        @Override
        public List<Activation> split(Activation element, Activation modifier, ElementPosition position) {
            compareActivations(element, modifier);
            return super.split(element, modifier, position);
        }

        @Override
        public List<Activation> cropStart(Activation element, Activation modifier, ElementPosition position) {
            compareActivations(element, modifier);
            return super.cropStart(element, modifier, position);
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, ElementPosition position) {
            compareActivations(element, modifier);
            return super.cropEnd(element, modifier, position);
        }
    }
}
