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
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.SelfObjectValidator;
import com.elster.jupiter.metering.impl.config.SelfValid;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
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

    private Stream<Meter> convertMeterActivationsToStreamOfMeters(List<MeterActivation> meterActivations) {
        return DecoratedStream.decorate(meterActivations.stream())
                .filter(ma -> ma.getMeterRole().isPresent() && ma.getMeter().isPresent())
                .distinct(ma -> ma.getMeter().get())
                .map(ma -> ma.getMeter().get());
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

        ElementVisitor<Activation> activateVisitor = new MeterActivationModificationVisitor();
        this.activationChanges.forEach(activation ->
                getMeterTimeLine(activation.getMeter(), this.meterTimeLines).adjust(activation, activateVisitor));
        this.usagePoint.touch();
        this.usagePoint.refreshMeterActivations();
        this.activationChanges.stream()
                .map(Activation::getMeter)
                .filter(Objects::nonNull)
                .map(MeterImpl.class::cast)
                .forEach(MeterImpl::refreshMeterActivations);

        eventService.postEvent(EventType.METER_ACTIVATED.topic(), this.usagePoint);
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        Map<Meter, TimeLine<Activation, Instant>> validationTimeLines = new HashMap<>();
        this.meterTimeLines.entrySet().forEach(entry -> {
            TimeLine<Activation, Instant> timeLine = new TimeLine<>(Activation::getRange, RangeComparatorFactory.INSTANT_DEFAULT);
            timeLine.addAll(entry.getValue().getElements(VirtualActivation::new));
            validationTimeLines.put(entry.getKey(), timeLine);
        });
        FormValidationActivationVisitor formValidationVisitor = new FormValidationActivationVisitor(context);
        this.activationChanges.forEach(activation ->
                getMeterTimeLine(activation.getMeter(), validationTimeLines, VirtualActivation::new).adjust(activation, formValidationVisitor));
        boolean result = formValidationVisitor.getResult();
        result &= validateMetersCapabilities(context);
        result &= validateByCustomValidators(context);
        return result;
    }

    private boolean validateMetersCapabilities(ConstraintValidatorContext context) {
        boolean result = true;
        List<EffectiveMetrologyConfigurationOnUsagePoint> emcList = this.usagePoint.getEffectiveMetrologyConfigurations();
        Map<Meter, Map<UsagePointMetrologyConfiguration, Set<MeterRole>>> processedMap = new HashMap<>();
        for (Activation activation : activationChanges) {
            Map<UsagePointMetrologyConfiguration, Set<MeterRole>> processedMeterRolesMap = processedMap.get(activation.getMeter());
            if (processedMeterRolesMap == null) {
                processedMeterRolesMap = new HashMap<>();
                processedMap.put(activation.getMeter(), processedMeterRolesMap);
            }
            for (EffectiveMetrologyConfigurationOnUsagePoint emc : emcList) {
                if (!emc.getRange().isConnected(activation.getRange())) {
                    continue;
                }
                UsagePointMetrologyConfiguration metrologyConfiguration = emc.getMetrologyConfiguration();
                Set<MeterRole> processedMeterRoles = processedMeterRolesMap.get(metrologyConfiguration);
                if (processedMeterRoles == null) {
                    processedMeterRoles = new HashSet<>();
                    processedMeterRolesMap.put(metrologyConfiguration, processedMeterRoles);
                }
                if (processedMeterRoles.add(activation.getMeterRole())) {
                    List<ReadingTypeRequirement> mandatoryReadingTypeRequirements = metrologyConfiguration.getMandatoryReadingTypeRequirements();
                    List<ReadingTypeRequirement> unmatchedRequirements = getUnmatchedMeterReadingTypeRequirements(metrologyConfiguration, mandatoryReadingTypeRequirements, activation.getMeter(), activation
                            .getMeterRole());
                    if (!unmatchedRequirements.isEmpty()) {
                        result = false;
                        String messageTemplate = this.metrologyConfigurationService.getThesaurus()
                                .getString(MessageSeeds.UNSATISFIED_METROLOGY_REQUIREMENT.getKey(), MessageSeeds.UNSATISFIED_METROLOGY_REQUIREMENT.getDefaultFormat());
                        String errorMessage = MessageFormat.format(messageTemplate, unmatchedRequirements
                                .stream()
                                .map(ReadingTypeRequirement::getDescription)
                                .collect(Collectors.joining(", ")));
                        context.buildConstraintViolationWithTemplate(errorMessage)
                                .addPropertyNode(activation.getMeterRole().getKey())
                                .addConstraintViolation();
                    }
                }
            }
        }
        return result;
    }

    private List<ReadingTypeRequirement> getUnmatchedMeterReadingTypeRequirements(UsagePointMetrologyConfiguration metrologyConfiguration,
                                                                                  List<ReadingTypeRequirement> mandatoryReadingTypeRequirements,
                                                                                  Meter meter, MeterRole meterRole) {
        List<ReadingType> readingTypesOnMeter = new ArrayList<>();
        meter.getHeadEndInterface()
                .map(headEndInterface -> headEndInterface.getCapabilities(meter))
                .ifPresent(endDeviceCapabilities -> readingTypesOnMeter.addAll(endDeviceCapabilities.getConfiguredReadingTypes()));
        return metrologyConfiguration.getRequirements(meterRole)
                .stream()
                .filter(mandatoryReadingTypeRequirements::contains)
                .filter(requirement -> !readingTypesOnMeter.stream().anyMatch(requirement::matches))
                .collect(Collectors.toList());
    }

    private boolean validateByCustomValidators(ConstraintValidatorContext context) {
        boolean result = true;
        for (Activation activation : this.activationChanges) {
            try {
                this.metrologyConfigurationService.validateUsagePointMeterActivation(activation.getMeterRole(), activation.getMeter(), this.usagePoint);
            } catch (CustomUsagePointMeterActivationValidationException ex) {
                result = false;
                context.buildConstraintViolationWithTemplate(ex.getLocalizedMessage())
                        .addPropertyNode(activation.getMeterRole().getKey())
                        .addConstraintViolation();
            }
        }
        return result;
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

        void setMeter(Meter meter);

        Activation split(Instant breakTime);

        void save();
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
        public void setMeter(Meter meter) {
            this.meter = meter;
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
        public void setMeter(Meter meter) {
            this.meterActivation.setMeter(meter);
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

    enum ElementPosition {
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
    interface ElementVisitor<T> {
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
    static class TimeLine<T, I extends Comparable<? super I>> {
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

        public List<T> getElements() {
            return this.getElements(Function.identity());
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

    class MeterActivationModificationVisitor implements ElementVisitor<Activation> {

        protected Activation createNewMeterActivation(Activation activation) {
            MeterActivationImpl meterActivation = metrologyConfigurationService.getDataModel()
                    .getInstance(MeterActivationImpl.class)
                    .init(activation.getMeter(), activation.getMeterRole(), activation.getUsagePoint(), activation.getRange());
            meterActivation.save();
            return new WrappedActivation(meterActivation);
        }

        protected boolean theSameUsagePoint(Activation element, Activation modifier) {
            return element.getUsagePoint() != null && element.getUsagePoint().equals(modifier.getUsagePoint());
        }

        protected boolean theSameMeterRole(Activation element, Activation modifier) {
            return element.getMeterRole() != null && element.getMeterRole().equals(modifier.getMeterRole());
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
            element.setUsagePoint(usagePoint);
            element.save();
            extendActivationStart(element, modifier, position);
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, ElementPosition position) {
            Activation activation = element.split(modifier.getRange().lowerEndpoint());
            activation.setMeterRole(modifier.getMeterRole());
            activation.setUsagePoint(usagePoint);
            activation.save();
            extendActivationEnd(activation, modifier, position);
            return Collections.singletonList(activation);
        }
    }

    class MeterActivationClearVisitor extends MeterActivationModificationVisitor {
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
            if (!theSameUsagePoint(element, modifier) || !theSameMeterRole(element, modifier)) {
                return null;
            }
            element.setUsagePoint(null);
            element.save();
            return null;
        }

        @Override
        public List<Activation> split(Activation element, Activation modifier, ElementPosition position) {
            if (!theSameUsagePoint(element, modifier) || !theSameMeterRole(element, modifier)) {
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
            if (!theSameUsagePoint(element, modifier) || !theSameMeterRole(element, modifier)) {
                return null;
            }
            Activation activation = element.split(modifier.getRange().upperEndpoint());
            element.setUsagePoint(null);
            element.save();
            return Collections.singletonList(activation);
        }

        @Override
        public List<Activation> cropEnd(Activation element, Activation modifier, ElementPosition position) {
            if (!theSameUsagePoint(element, modifier) || !theSameMeterRole(element, modifier)) {
                return null;
            }
            Activation activation = element.split(modifier.getRange().lowerEndpoint());
            activation.setUsagePoint(null);
            activation.save();
            return Collections.singletonList(activation);
        }
    }

    class FormValidationActivationVisitor extends MeterActivationModificationVisitor {
        private final ConstraintValidatorContext context;
        private boolean result = true;

        public FormValidationActivationVisitor(ConstraintValidatorContext context) {
            this.context = context;
        }

        public boolean getResult() {
            return this.result;
        }

        @Override
        protected Activation createNewMeterActivation(Activation activation) {
            return new VirtualActivation(activation.getRange().lowerEndpoint(), activation.getUsagePoint(), activation.getMeter(), activation.getMeterRole());
        }

        private List<Activation> compareActivations(Activation element, Activation modifier) {
            if (element.getMeterRole() != null && element.getUsagePoint() != null && !theSameUsagePoint(element, modifier)) {
                this.result = false;
                String errorMessage = metrologyConfigurationService.getThesaurus()
                        .getFormat(MessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT)
                        .format(element.getMeter().getMRID(), element.getUsagePoint().getMRID(), element.getMeterRole().getDisplayName());
                context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode(modifier.getMeterRole().getKey()).addConstraintViolation();
            }
            if (element.getUsagePoint() != null && theSameUsagePoint(element, modifier)
                    && element.getMeterRole() != null && !theSameMeterRole(element, modifier)) {
                this.result = false;
                this.context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT + "}")
                        .addPropertyNode(modifier.getMeterRole().getKey()).addConstraintViolation();
            }
            return null;
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
