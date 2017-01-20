package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointManagementException;
import com.elster.jupiter.metering.UsagePointMeterActivationException;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.SelfObjectValidator;
import com.elster.jupiter.metering.impl.config.SelfValid;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.util.RangeComparatorFactory;
import com.elster.jupiter.util.geo.SpatialCoordinates;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private boolean useThrowingValidator;
    private FormValidation formValidation = FormValidation.SET_METERS;

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

    @Override
    public UsagePointMeterActivator throwingValidation() {
        this.useThrowingValidator = true;
        return this;
    }

    @Override
    public UsagePointMeterActivator withFormValidation(FormValidation formValidation) {
        this.formValidation = formValidation;
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
        // Apply 'clear' modifications, i.e. detach usage point for specific roles
        this.meterTimeLines = new HashMap<>();
        convertMeterActivationsToStreamOfMeters(this.usagePoint.getMeterActivations())
                .forEach(meter -> getMeterTimeLine(meter, this.meterTimeLines));
        ElementVisitor<Activation> clearVisitor = new MeterActivationClearVisitor();
        this.deactivationChanges.forEach(activation ->
                convertMeterActivationsToStreamOfMeters(this.usagePoint.getMeterActivations(activation.getMeterRole()))
                        .forEach(meter -> getMeterTimeLine(meter, this.meterTimeLines).adjust(activation, clearVisitor)));
        // Validate changes
        startValidation();
        // Apply activation changes
        ElementVisitor<Activation> activateVisitor = new MeterActivationModificationVisitor(this.metrologyConfigurationService.getDataModel());
        this.activationChanges.forEach(activation ->
                getMeterTimeLine(activation.getMeter(), this.meterTimeLines).adjust(activation, activateVisitor));
        this.usagePoint.touch();
        refreshMeterActivations();
        // Notify
        eventService.postEvent(EventType.USAGEPOINT_UPDATED.topic(), this.usagePoint);
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
            ((MeterImpl) meter).refreshMeterActivations();
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

    private void startValidation() {
        if (!this.useThrowingValidator) {
            Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), this);
        } else {
            ValidationReport validationReport = new ThrowingValidationReport(this.metrologyConfigurationService.getThesaurus());
            validate(validationReport);
        }
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        ValidationReport validationReport = FormValidation.DEFINE_METROLOGY_CONFIGURATION.equals(formValidation)
                ? new FormValidationReportWhenDefineMetrologyConfiguration(context, this.metrologyConfigurationService.getThesaurus())
                : new FormValidationReport(context, this.metrologyConfigurationService.getThesaurus());
        validate(validationReport);
        return validationReport.isValid();
    }

    private void validate(ValidationReport validationReport) {
        // check that we can manage meter activations
        UsagePointStage.Key usagePointStage = this.usagePoint.getState().getStage().getKey();
        if (usagePointStage != UsagePointStage.Key.PRE_OPERATIONAL) {
            validationReport.usagePointIncorrectStage();
            return;
        }
        // prepare time lines and virtualize all meter activations, so our changes will not have permanent effect
        Map<Meter, TimeLine<Activation, Instant>> validationTimeLines = new HashMap<>();
        this.meterTimeLines.entrySet().forEach(entry -> {
            TimeLine<Activation, Instant> timeLine = new TimeLine<>(Activation::getRange, RangeComparatorFactory.INSTANT_DEFAULT);
            timeLine.addAll(entry.getValue().getElements(VirtualActivation::new));
            validationTimeLines.put(entry.getKey(), timeLine);
        });
        // check resulting meter activations for affected meters
        ValidateActivationsForSingleMeterVisitor meterActivationVisitor = new ValidateActivationsForSingleMeterVisitor(validationReport);
        this.activationChanges.forEach(activation ->
                getMeterTimeLine(activation.getMeter(), validationTimeLines, VirtualActivation::new).adjust(activation, meterActivationVisitor));
        // check usage point activations for overlaps
        ValidateOverlappingUsagePointActivationsVisitor usagePointActivationVisitor = new ValidateOverlappingUsagePointActivationsVisitor(validationReport);
        validationTimeLines.entrySet().forEach(entry ->
                this.activationChanges.stream()
                        .filter(activation -> !entry.getKey().equals(activation.getMeter()))
                        .forEach(activation -> entry.getValue().adjust(activation, usagePointActivationVisitor)));
        validateMetersCapabilities(validationReport);
        validateByCustomValidators(validationReport);
    }

    private void validateMetersCapabilities(ValidationReport validationReport) {
        List<EffectiveMetrologyConfigurationOnUsagePoint> emcList = this.usagePoint.getEffectiveMetrologyConfigurations();
        for (Activation activation : this.activationChanges) {
            Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> requirementsMap = new HashMap<>();
            for (EffectiveMetrologyConfigurationOnUsagePoint emc : emcList) {
                if (!(emc.getRange().isConnected(activation.getRange()) && !emc.getRange().intersection(activation.getRange()).isEmpty())) {
                    continue;
                }
                UsagePointMetrologyConfiguration metrologyConfiguration = emc.getMetrologyConfiguration();
                List<ReadingTypeRequirement> mandatoryReadingTypeRequirements = emc.getReadingTypeRequirements();
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
                    requirementsMap.put(metrologyConfiguration, unmatchedRequirements);
                }
            }
            if (!requirementsMap.isEmpty()) {
                validationReport.meterHasUnsatisfiedRequirements(activation.getMeter(), activation.getMeterRole(), requirementsMap);
            }
        }
    }

    private void validateByCustomValidators(ValidationReport validationReport) {
        for (Activation activation : this.activationChanges) {
            // TODO here possible double check for the same combination of meter and role if different intervals were used
            try {
                this.metrologyConfigurationService.validateUsagePointMeterActivation(activation.getMeterRole(), activation.getMeter(), activation.getUsagePoint());
            } catch (CustomUsagePointMeterActivationValidationException ex) {
                validationReport.activationWasFailedByCustomValidator(activation.getMeter(), activation.getMeterRole(), activation.getUsagePoint(), ex);
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

        default Instant getStart() {
            return getRange().lowerEndpoint();
        }

        Instant getEnd();

        default boolean hasTheSameUsagePointAs(Activation modifier) {
            return getUsagePoint() != null && getUsagePoint().equals(modifier.getUsagePoint());
        }

        default boolean hasTheSameMeterRoleAs(Activation modifier) {
            return getMeterRole() != null && getMeterRole().equals(modifier.getMeterRole());
        }

        default boolean hasTheSameMeterAs(Activation modifier) {
            return getMeter() != null && getMeter().equals(modifier.getMeter());
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
            this.start = activation.getStart();
            this.meterRole = activation.getMeterRole();
            this.usagePoint = activation.getUsagePoint();
            this.meter = activation.getMeter();
            this.end = activation.getEnd();
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

        @Override
        public Instant getEnd() {
            return this.end;
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

        @Override
        public Instant getEnd() {
            return this.meterActivation.getEnd();
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
                last.endAt(modifier.getEnd());
            }
        }

        private void extendActivationStart(Activation first, Activation modifier, ElementPosition position) {
            if ((position == ElementPosition.FIRST || position == ElementPosition.SINGLE)
                    && modifier.getStart().isBefore(first.getStart())) {
                first.startAt(modifier.getStart());
            }
        }

        @Override
        public List<Activation> before(Activation first, Activation modifier, ElementPosition position) {
            Activation activation = createNewMeterActivation(modifier);
            if (first != null) {
                first.startAt(modifier.getEnd());
                first.save();
            }
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> after(Activation last, Activation modifier, ElementPosition position) {
            last.endAt(modifier.getEnd());
            Activation activation = last.split(modifier.getStart());
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
            activations.add(element.split(modifier.getEnd()));
            Activation activation = element.split(modifier.getStart());
            activation.setMeterRole(modifier.getMeterRole());
            activation.setUsagePoint(modifier.getUsagePoint());
            activation.save();
            activations.add(activation);
            return activations;
        }

        @Override
        public List<Activation> cropStart(Activation element, Activation modifier, ElementPosition position) {
            Activation activation = element.split(modifier.getEnd());
            element.setMeterRole(modifier.getMeterRole());
            element.setUsagePoint(modifier.getUsagePoint());
            element.save();
            extendActivationStart(element, modifier, position);
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, ElementPosition position) {
            Activation activation = element.split(modifier.getStart());
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
            activations.add(element.split(modifier.getEnd()));
            Activation activation = element.split(modifier.getStart());
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
            Activation activation = element.split(modifier.getEnd());
            element.setUsagePoint(null);
            element.save();
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, ElementPosition position) {
            if (!element.hasTheSameUsagePointAs(modifier) || !element.hasTheSameMeterRoleAs(modifier)) {
                return null;
            }
            Activation activation = element.split(modifier.getStart());
            activation.setUsagePoint(null);
            activation.save();
            return Collections.singletonList(activation);
        }
    }

    private interface ValidationReport {
        boolean isValid();

        void meterActiveOnDifferentUsagePoint(Meter meter, MeterRole currentRole, MeterRole desiredRole, UsagePoint meterCurrentUsagePoint, Range<Instant> conflictActivationRange);

        void meterActiveWithDifferentMeterRole(Meter meter, MeterRole currentRole, MeterRole desiredRole, Range<Instant> conflictActivationRange);

        void usagePointHasMeterOnThisRole(Meter meterActiveOnRole, MeterRole meterRole, Range<Instant> conflictActivationRange);

        void meterHasUnsatisfiedRequirements(Meter meter, MeterRole meterRole, Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements);

        void activationWasFailedByCustomValidator(Meter meter, MeterRole meterRole, UsagePoint usagePoint, CustomUsagePointMeterActivationValidationException ex);

        void usagePointIncorrectStage();
    }

    private static class FormValidationReport implements ValidationReport {
        private final ConstraintValidatorContext context;
        private final Thesaurus thesaurus;
        private boolean valid = true;

        public FormValidationReport(ConstraintValidatorContext context, Thesaurus thesaurus) {
            this.context = context;
            this.thesaurus = thesaurus;
            this.context.disableDefaultConstraintViolation();
        }

        @Override
        public boolean isValid() {
            return this.valid;
        }

        @Override
        public void meterActiveOnDifferentUsagePoint(Meter meter, MeterRole currentRole, MeterRole desiredRole, UsagePoint meterCurrentUsagePoint, Range<Instant> conflictActivationRange) {
            this.valid = false;
            String errorMessage = this.thesaurus.getFormat(MessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT)
                    .format(meter.getName(), meterCurrentUsagePoint.getName(), currentRole.getDisplayName());
            this.context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode(desiredRole.getKey()).addConstraintViolation();
        }

        @Override
        public void meterActiveWithDifferentMeterRole(Meter meter, MeterRole currentRole, MeterRole desiredRole, Range<Instant> conflictActivationRange) {
            this.valid = false;
            this.context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT + "}")
                    .addPropertyNode(desiredRole.getKey()).addConstraintViolation();
        }

        @Override
        public void usagePointHasMeterOnThisRole(Meter meterActiveOnRole, MeterRole meterRole, Range<Instant> conflictActivationRange) {
            this.valid = false;
            String message = this.thesaurus.getFormat(MessageSeeds.USAGE_POINT_ALREADY_ACTIVE_WITH_GIVEN_ROLE).format(meterActiveOnRole.getName(), meterRole.getDisplayName());
            this.context.buildConstraintViolationWithTemplate(message).addPropertyNode(meterRole.getKey()).addConstraintViolation();
        }

        @Override
        public void meterHasUnsatisfiedRequirements(Meter meter, MeterRole meterRole, Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
            this.valid = false;
            String errorMessage =
                    this.thesaurus
                            .getFormat(MessageSeeds.UNSATISFIED_METROLOGY_REQUIREMENT)
                            .format(unsatisfiedRequirements
                                    .values()
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .map(ReadingTypeRequirement::getDescription)
                                    .collect(Collectors.joining(", ")));
            this.context.buildConstraintViolationWithTemplate(errorMessage)
                    .addPropertyNode(meterRole.getKey())
                    .addConstraintViolation();
        }

        @Override
        public void activationWasFailedByCustomValidator(Meter meter, MeterRole meterRole, UsagePoint usagePoint, CustomUsagePointMeterActivationValidationException ex) {
            this.valid = false;
            this.context.buildConstraintViolationWithTemplate(ex.getLocalizedMessage())
                    .addPropertyNode(meterRole.getKey())
                    .addConstraintViolation();
        }

        @Override
        public void usagePointIncorrectStage() {
            this.valid = false;
            String errorMessage = this.thesaurus.getFormat(MessageSeeds.USAGE_POINT_INCORRECT_STAGE).format();
            this.context.buildConstraintViolationWithTemplate(errorMessage)
                    .addPropertyNode("usagepoint")
                    .addConstraintViolation();
        }
    }

    private static class FormValidationReportWhenDefineMetrologyConfiguration extends FormValidationReport {

        public FormValidationReportWhenDefineMetrologyConfiguration(ConstraintValidatorContext context, Thesaurus thesaurus) {
            super(context, thesaurus);
        }

        @Override
        public void meterHasUnsatisfiedRequirements(Meter meter, MeterRole meterRole, Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
            super.valid = false;

            for (Map.Entry<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirementEntry : unsatisfiedRequirements.entrySet()) {
                Map<MetrologyPurpose, List<ReadingTypeRequirement>> requirements = unsatisfiedRequirementEntry.getKey().getContracts()
                        .stream()
                        .collect(Collectors.toMap(MetrologyContract::getMetrologyPurpose, this::getReadingTypeRequirements, (a, b) -> a));

                requirements.entrySet()
                        .stream()
                        .filter(metrologyPurposeEntry -> metrologyPurposeEntry.getValue()
                                .stream()
                                .anyMatch(req -> unsatisfiedRequirementEntry.getValue().contains(req)))
                        .forEach(metrologyPurposeEntry -> {
                            String errorMessage =
                                    super.thesaurus
                                            .getFormat(MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENT_FOR_METER)
                                            .format(meter.getName(), metrologyPurposeEntry.getKey().getName());
                            super.context.buildConstraintViolationWithTemplate(errorMessage)
                                    .addPropertyNode("id")
                                    .addConstraintViolation();
                        });
            }
        }

        public List<ReadingTypeRequirement> getReadingTypeRequirements(MetrologyContract metrologyContract) {
            ReadingTypeRequirementsCollector requirementsCollector = new ReadingTypeRequirementsCollector();
            metrologyContract.getDeliverables()
                    .stream()
                    .map(ReadingTypeDeliverable::getFormula)
                    .map(Formula::getExpressionNode)
                    .forEach(expressionNode -> expressionNode.accept(requirementsCollector));
            return requirementsCollector.getReadingTypeRequirements();
        }
    }

    private static class ThrowingValidationReport implements ValidationReport {
        private final Thesaurus thesaurus;

        public ThrowingValidationReport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        public boolean isValid() {
            return true; // it always valid if we can call this method
        }

        @Override
        public void meterActiveOnDifferentUsagePoint(Meter meter, MeterRole currentRole, MeterRole desiredRole, UsagePoint meterCurrentUsagePoint, Range<Instant> conflictActivationRange) {
            throw UsagePointMeterActivationException.meterActiveOnDifferentUsagePoint(this.thesaurus, meter, currentRole, desiredRole, meterCurrentUsagePoint, conflictActivationRange);
        }

        @Override
        public void meterActiveWithDifferentMeterRole(Meter meter, MeterRole currentRole, MeterRole desiredRole, Range<Instant> conflictActivationRange) {
            throw UsagePointMeterActivationException.meterActiveWithDifferentMeterRole(this.thesaurus, meter, currentRole, desiredRole, conflictActivationRange);
        }

        @Override
        public void usagePointHasMeterOnThisRole(Meter meterActiveOnRole, MeterRole meterRole, Range<Instant> conflictActivationRange) {
            throw UsagePointMeterActivationException.usagePointHasMeterOnThisRole(this.thesaurus, meterActiveOnRole, meterRole, conflictActivationRange);
        }

        @Override
        public void meterHasUnsatisfiedRequirements(Meter meter, MeterRole meterRole, Map<UsagePointMetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements) {
            throw UsagePointMeterActivationException.meterHasUnsatisfiedRequirements(this.thesaurus, meter, meterRole, unsatisfiedRequirements);
        }

        @Override
        public void activationWasFailedByCustomValidator(Meter meter, MeterRole meterRole, UsagePoint usagePoint, CustomUsagePointMeterActivationValidationException ex) {
            throw UsagePointMeterActivationException.activationWasFailedByCustomValidator(this.thesaurus, meter, meterRole, usagePoint, ex);
        }

        @Override
        public void usagePointIncorrectStage() {
            throw UsagePointManagementException.incorrectStage(this.thesaurus);
        }
    }

    private static class ValidateActivationsForSingleMeterVisitor extends MeterActivationModificationVisitor {
        private final ValidationReport report;
        private final Set<Activation> processed = new HashSet<>();

        public ValidateActivationsForSingleMeterVisitor(ValidationReport report) {
            super(null);
            this.report = report;
        }

        @Override
        protected Activation createNewMeterActivation(Activation activation) {
            return new VirtualActivation(activation.getStart(), activation.getUsagePoint(), activation.getMeter(), activation.getMeterRole());
        }

        private void compareActivations(Activation element, Activation modifier) {
            if (element.getMeterRole() != null && element.getUsagePoint() != null && !element.hasTheSameUsagePointAs(modifier) && this.processed.add(modifier)) {
                this.report.meterActiveOnDifferentUsagePoint(element.getMeter(), element.getMeterRole(), modifier.getMeterRole(), element.getUsagePoint(), element.getRange());
            }
            if (element.getUsagePoint() != null && element.hasTheSameUsagePointAs(modifier)
                    && element.getMeterRole() != null && !element.hasTheSameMeterRoleAs(modifier) && this.processed.add(modifier)) {
                this.report.meterActiveWithDifferentMeterRole(element.getMeter(), element.getMeterRole(), modifier.getMeterRole(), element.getRange());
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

    private static class ValidateOverlappingUsagePointActivationsVisitor implements ElementVisitor<Activation> {
        private final ValidationReport report;

        public ValidateOverlappingUsagePointActivationsVisitor(ValidationReport report) {
            this.report = report;
        }

        private List<Activation> compareActivations(Activation element, Activation modifier) {
            if (element.getMeter() != null && !element.hasTheSameMeterAs(modifier)
                    && element.getMeterRole() != null && element.hasTheSameMeterRoleAs(modifier)
                    && element.getUsagePoint() != null && element.hasTheSameUsagePointAs(modifier)) {
                this.report.usagePointHasMeterOnThisRole(element.getMeter(), element.getMeterRole(), element.getRange());
            }
            return null;
        }

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
            return compareActivations(element, modifier);
        }

        @Override
        public List<Activation> split(Activation element, Activation modifier, ElementPosition position) {
            return compareActivations(element, modifier);
        }

        @Override
        public List<Activation> cropStart(Activation element, Activation modifier, ElementPosition position) {
            return compareActivations(element, modifier);
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, ElementPosition position) {
            return compareActivations(element, modifier);
        }
    }
}
