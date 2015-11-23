package com.elster.insight.usagepoint.data;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.data.exceptions.InvalidLastCheckedException;
import com.elster.insight.usagepoint.data.impl.MessageSeeds;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

/**
 * Created by tgr on 9/09/2014.
 */
public class UsagePointValidationImpl implements UsagePointValidation {

    private final ValidationService validationService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final UsagePoint usagePoint;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private transient Meter meter;
    private transient ValidationEvaluator evaluator;

    public UsagePointValidationImpl(ValidationService validationService, Clock clock, Thesaurus thesaurus, UsagePoint usagePoint, UsagePointConfigurationService usagePointConfigurationService) {
        this.validationService = validationService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.usagePoint = usagePoint;
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    @Override
    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
        return getEvaluator().getValidationResult(qualities);
    }

    @Override
    public boolean isValidationActive() {
        return getEvaluator().isValidationEnabled(fetchMeter());
    }

    @Override
    public boolean isValidationOnStorage() {
        return getEvaluator().isValidationOnStorageEnabled(fetchMeter());
    }

    @Override
    public void activateValidationOnStorage(Instant lastChecked) {
        activateValidation(lastChecked, true);
    }

    @Override
    public void activateValidation(Instant lastChecked) {
        activateValidation(lastChecked, false);
    }

    void activateValidation(Instant lastChecked, boolean onStorage) {
        Meter koreMeter = this.fetchMeter();
        if (koreMeter.hasData()) {
            if (lastChecked == null) {
                throw InvalidLastCheckedException.lastCheckedCannotBeNull(this.usagePoint, this.thesaurus, MessageSeeds.LAST_CHECKED_CANNOT_BE_NULL);
            }
            this.getMeterActivationsMostRecentFirst(koreMeter)
                .filter(each -> this.isEffectiveOrStartsAfterLastChecked(lastChecked, each))
                .forEach(each -> this.applyLastChecked(lastChecked, each));
        }
        this.validationService.activateValidation(koreMeter);
        if (onStorage) {
            this.validationService.enableValidationOnStorage(koreMeter);
        }
        else {
            this.validationService.disableValidationOnStorage(koreMeter);
        }
    }

    private boolean isEffectiveOrStartsAfterLastChecked(Instant lastChecked, MeterActivation meterActivation) {
        return meterActivation.isEffectiveAt(lastChecked) || meterActivation.getInterval().startsAfter(lastChecked);
    }

    private void applyLastChecked(Instant lastChecked, MeterActivation meterActivation) {
        Optional<Instant> meterActivationLastChecked = validationService.getLastChecked(meterActivation);
        if (meterActivation.isCurrent()) {
            if (meterActivationLastChecked.isPresent() && lastChecked.isAfter(meterActivationLastChecked.get())) {
                throw InvalidLastCheckedException.lastCheckedAfterCurrentLastChecked(this.usagePoint, meterActivationLastChecked.get(), lastChecked, this.thesaurus, MessageSeeds.LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED);
            }
            this.validationService.updateLastChecked(meterActivation, lastChecked);
        } else {
            Instant lastCheckedDateToSet = this.smallest(meterActivationLastChecked.orElse(meterActivation.getStart()), lastChecked);
            validationService.updateLastChecked(meterActivation, lastCheckedDateToSet);
        }
    }

    private Instant smallest(Instant instant1, Instant instant2) {
        return Ordering.natural().min(instant1, instant2);
    }

    @Override
    public void deactivateValidation() {
        this.validationService.deactivateValidation(this.fetchMeter());
    }

    @Override
    public boolean isValidationActive(Channel channel, Instant when) {
        if (!isValidationActive()) {
            return false;
        }
        return evaluator.isValidationEnabled(channel);
    }

//    @Override
//    public boolean isValidationActive(Register<?> register, Instant when) {
//        if (!isValidationActive()) {
//            return false;
//        }
//        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
//        return found.isPresent() ? evaluator.isValidationEnabled(found.get()) : hasActiveRules(register);
//    }

    @Override
    public boolean allDataValidated(Channel channel, Instant when) {
        return getEvaluator().isAllDataValidated(channel.getMeterActivation());
    }

//    @Override
//    public boolean allDataValidated(Register<?> register, Instant when) {
//        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
//        return !found.isPresent() || getEvaluator().isAllDataValidated(found.get().getMeterActivation());
//    }

    @Override
    public Optional<Instant> getLastChecked() {
        return getLastChecked(fetchMeter());
    }

    @Override
    public Optional<Instant> getLastChecked(Channel channel) {
        return getLastChecked(channel.getMainReadingType());
    }

//    @Override
//    public Optional<Instant> getLastChecked(Register<?> register) {
//        return getLastChecked(register.getReadingType());
//    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Range<Instant> interval) {
        
        return getEvaluator().getValidationStatus(channel, readings, channel.getMeterActivation().getRange().intersection(interval));
    }

//    @Override
//    public List<DataValidationStatus> getValidationStatus(Register<?> register, List<? extends BaseReading> readings, Range<Instant> interval) {
//        return ((UsagePointImpl) register.getUsagePoint()).findKoreChannels(register).stream()
//                .filter(k -> does(k.getMeterActivation().getRange()).overlap(interval))
//                .flatMap(k -> getEvaluator().getValidationStatus(k, readings, k.getMeterActivation().getRange().intersection(interval)).stream())
//                .collect(Collectors.toList());
//    }

    @Override
    public void validateData() {
        List<? extends MeterActivation> meterActivations = usagePoint.getMeterActivations();
        if (!meterActivations.isEmpty()) {
            Range<Instant> range = meterActivations.get(0).getRange();
            ValidationEvaluator evaluator = this.validationService.getEvaluator(this.fetchMeter(), range);
            meterActivations.forEach(meterActivation -> {
                if (!evaluator.isAllDataValidated(meterActivation)) {
                    this.validationService.validate(meterActivation);
                }
            });
        }
    }

//    @Override
//    public void validateLoadProfile(LoadProfile loadProfile) {
//        loadProfile.getChannels().forEach(this::validateChannel);
//    }

    @Override
    public void validateChannel(Channel channel) {
        validate(channel.getMainReadingType());
    }

//    @Override
//    public void validateRegister(Register<?> register) {
//        validate(register.getReadingType());
//    }

    @Override
    public void setLastChecked(Channel channel, Instant start) {
        this.validationService.updateLastChecked(channel, start);
    }

//    @Override
//    public void setLastChecked(Register<?> register, Instant start) {
//        getUsagePoint()
//            .findKoreChannels(register)
//            .stream()
//            .forEach(c -> this.validationService.updateLastChecked(c, start));
//    }

    private boolean hasActiveRules(Channel channel) {
        return hasActiveRules(channel.getMainReadingType());
    }

//    private boolean hasActiveRules(Register<?> register) {
//        return hasActiveRules(register.getReadingType());
//    }

    private boolean hasActiveRules(ReadingType readingType) {
        
        Optional<MetrologyConfiguration> holder = usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(usagePoint);
        List<ValidationRuleSet> validationRuleSets = new ArrayList<ValidationRuleSet>();
        if (holder.isPresent())
            validationRuleSets = holder.get().getValidationRuleSets();
        return validationRuleSets.stream()
                .flatMap(s -> s.getRules().stream())
                .anyMatch(r -> r.getReadingTypes().contains(readingType));
    }

//    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Channel channel, Instant when) {
//        return findKoreChannel(channel.getReadingType(), when);
//    }
//
//    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Register<?> register, Instant when) {
//        ReadingType readingType = register.getReadingType();
//        return findKoreChannel(readingType, when);
//    }

//    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(ReadingType readingType, Instant when) {
//        return fetchKoreMeter().getMeterActivations().stream()
//                .filter(m -> m.getRange().contains(when)) // TODO verify with Karel
//                .flatMap(m -> m.getChannels().stream())
//                .filter(c -> c.getReadingTypes().contains(readingType))
//                .findFirst();
//    }

    private Optional<Instant> getLastChecked(Meter meter) {
        return getMeterActivationsMostRecentFirst(meter)
                .map(validationService::getLastChecked)  // may be use evaluator to allow caching this
                .flatMap(asStream())
                .findAny();
    }

    private Optional<Instant> getLastChecked(ReadingType readingType) {
        return getEvaluator().getLastChecked(fetchMeter(), readingType);
    }

    private Stream<MeterActivation> getMeterActivationsMostRecentFirst(Meter meter) {
        TreeSet<MeterActivation> meterActivations = new TreeSet<>(byInterval());
        meterActivations.addAll(meter.getMeterActivations());
        return meterActivations.stream();
    }

    private Comparator<MeterActivation> byInterval() {
        return Comparator.comparing(MeterActivation::getRange, byStart());
    }

    private Comparator<? super Range<Instant>> byStart() {
        return (o1, o2) -> {
            if (!o1.hasLowerBound()) {
                return !o2.hasLowerBound() ? -1 : 0;
            }
            if (!o2.hasLowerBound()) {
                return 1;
            }
            return o1.lowerEndpoint().compareTo(o2.lowerEndpoint());
        };
    }

    private Range<Instant> interval(List<? extends BaseReading> readings) {
        Instant min = readings.stream().map(BaseReading::getTimeStamp).min(naturalOrder()).get();
        Instant max = readings.stream().map(BaseReading::getTimeStamp).max(naturalOrder()).get();
        return Range.closed(min, max);
    }

    private void validate(ReadingType readingType) {
        fetchMeter().getMeterActivations().stream()
                .forEach(meterActivation -> validationService.validate(meterActivation, readingType));
    }

    private Meter fetchMeter() {
        if (meter == null) {
            //TODO for 10.1 we'll assume the meter exists
            meter = usagePoint.getMeter(clock.instant()).get();
        }
        return meter;
    }

    private ValidationEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = validationService.getEvaluator(fetchMeter(), Range.atMost(clock.instant()));
        }
        return evaluator;
    }

}
