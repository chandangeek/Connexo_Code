package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Upcast;
import com.elster.jupiter.util.comparators.NullSafeOrdering;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.isNull;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + ValidationService.COMPONENTNAME, immediate = true)
public final class ValidationServiceImpl implements ValidationService, InstallService {

    private static final Upcast<IValidationRuleSet, ValidationRuleSet> UPCAST = new Upcast<>();
    private static final Function<IMeterActivationValidation, Date> METER_ACTIVATION_MIN_LAST_CHECKED = new Function<IMeterActivationValidation, Date>() {
        @Override
        public Date apply(IMeterActivationValidation input) {
            return (input == null ? null : input.getMinLastChecked());
        }
    };
    private static final Function<ChannelValidation, Date> CHANNEL_VALIDATION_LAST_CHECKED = new Function<ChannelValidation, Date>() {
        @Override
        public Date apply(ChannelValidation input) {
            return (input == null ? null : input.getLastChecked());
        }
    };
    private static final Date ETERNITY = new Date(Long.MAX_VALUE);
    public static final ReadingQuality OK_QUALITY = new ReadingQuality() {
        @Override
        public String getComment() {
            return "";
        }

        @Override
        public String getTypeCode() {
            return ReadingQualityType.MDM_VALIDATED_OK_CODE;
        }
    };


    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;
    private volatile List<ValidatorFactory> validatorFactories = new ArrayList<>();
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile List<ValidationRuleSetResolver> ruleSetResolvers = new ArrayList<>();
    private volatile UserService userService;

    public ValidationServiceImpl() {
    }

    @Inject
    ValidationServiceImpl(Clock clock, EventService eventService, MeteringService meteringService, OrmService ormService, QueryService queryService, NlsService nlsService, UserService userService) {
        this.clock = clock;
        this.eventService = eventService;
        this.meteringService = meteringService;
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        activate();
        install();
    }

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(EventService.class).toInstance(eventService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(DataModel.class).toInstance(dataModel);
                bind(ValidationService.class).toInstance(ValidationServiceImpl.this);
                bind(ValidatorCreator.class).toInstance(new DefaultValidatorCreator());
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
            }
        });
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public void install() {
        new InstallerImpl(dataModel, eventService, thesaurus, userService).install(true, true);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(ValidationService.COMPONENTNAME, "Validation");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ValidationService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name) {
        return ValidationRuleSetImpl.from(dataModel, name);
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name, String description) {
        ValidationRuleSet set = ValidationRuleSetImpl.from(dataModel, name, description);
        set.save();
        return set;
    }

    @Override
    public void activateValidation(Meter meter) {
        Optional<MeterValidationImpl> meterValidation = getMeterValidation(meter);
        if (meterValidation.isPresent()) {
            if (!meterValidation.get().getActivationStatus()) {
                meterValidation.get().setActivationStatus(true);
                meterValidation.get().save();
            } // else already active
        } else {
            createMeterValidation(meter, true);
            Optional<MeterActivation> currentMeterActivation = meter.getCurrentMeterActivation();
            if (currentMeterActivation.isPresent()) {
                manageMeterActivationValidations(currentMeterActivation.get());
            }
        }
    }

    @Override
    public void deactivateValidation(Meter meter) {
        Optional<MeterValidationImpl> meterValidation = getMeterValidation(meter);
        if (meterValidation.isPresent()) {
            if (meterValidation.get().getActivationStatus()) {
                meterValidation.get().setActivationStatus(false);
                meterValidation.get().save();
            } // else already inactive
        }
        // else MeterValidation not present which means also inactive (see validate(...)
    }

    @Override
    public boolean validationEnabled(Meter meter) {
        Optional<MeterValidationImpl> meterValidation = getMeterValidation(meter);
        return meterValidation.isPresent() && meterValidation.get().getActivationStatus();
    }

    Optional<MeterValidationImpl> getMeterValidation(Meter meter) {
        return dataModel.mapper(MeterValidationImpl.class).getOptional(meter.getId());
    }

    private void createMeterValidation(Meter meter, boolean active) {
        MeterValidationImpl meterValidation = MeterValidationImpl.from(dataModel, meter);
        meterValidation.setActivationStatus(active);
        meterValidation.save();
    }

    @Override
    public void updateLastChecked(MeterActivation meterActivation, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Last Checked Date is absent");
        }
        manageMeterActivationValidations(meterActivation);
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(meterActivation);
        for (IMeterActivationValidation validation : validations) {
            validation.updateLastChecked(date);
            validation.save();
        }
    }

    @Override
    public boolean isValidationActive(Channel channel) {
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(channel.getMeterActivation());
        List<ChannelValidation> channelValidations = validations.stream()
                .map(m -> m.getChannelValidation(channel))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ChannelValidation::hasActiveRules)
                .collect(Collectors.toList());
        return channelValidations.isEmpty() ? false : true;
    }

    @Override
    public Optional<Date> getLastChecked(MeterActivation meterActivation) {
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(meterActivation);
        return Optional.fromNullable(getMinLastChecked(FluentIterable.from(validations).transform(METER_ACTIVATION_MIN_LAST_CHECKED)));
    }

    @Override
    public Optional<Date> getLastChecked(Channel channel) {
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(channel.getMeterActivation());

        Date lastChecked = null;
        List<Date> dates = validations.stream()
                .map(m -> m.getChannelValidation(channel))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ChannelValidation::getLastChecked)
                .collect(Collectors.toList());
        if (dates.stream().anyMatch(isNull())) {
            return Optional.<Date>absent();
        }
        return Optional.fromNullable(dates.stream().reduce(min(NullSafeOrdering.NULL_IS_SMALLEST.get())).orElse(null));
    }

    private <T> BinaryOperator<T> min(final Comparator<? super T> comparator) {
        return (t1, t2) -> comparator.compare(t1, t2) <= 0 ? t1 : t2;
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(long id) {
        return dataModel.mapper(IValidationRuleSet.class).getOptional(id).transform(UPCAST);
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(String name) {
        Condition condition = Operator.EQUAL.compare("name", name).and(Operator.ISNULL.compare(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD));
        List<ValidationRuleSet> ruleSets = getRuleSetQuery().select(condition);
        return ruleSets.isEmpty() ? Optional.<ValidationRuleSet>absent() : Optional.of(ruleSets.get(0));
    }

    @Override
    public Optional<ValidationRule> getValidationRule(long id) {
        return dataModel.mapper(ValidationRule.class).getOptional(id);
    }

    @Override
    public Query<ValidationRuleSet> getRuleSetQuery() {
        Query<ValidationRuleSet> ruleSetQuery = queryService.wrap(dataModel.query(ValidationRuleSet.class));
        ruleSetQuery.setRestriction(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        return ruleSetQuery;
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets() {
        return getRuleSetQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
    }

    @Override
    public void validate(MeterActivation meterActivation, Interval interval) {
        boolean validationActive = true;
        // when meterActivation is not linked to a meter, validation is run.
        if (meterActivation.getMeter().isPresent()) {
            // if meteractivation is linked to a meter, we only run validation when it is actively configured on the meter to run it
            Optional<MeterValidationImpl> found = getMeterValidation(meterActivation.getMeter().get());
            validationActive = found.isPresent() && found.get().getActivationStatus();
        }
        if (validationActive) {
            List<IMeterActivationValidation> meterActivationValidations = manageMeterActivationValidations(meterActivation);
            for (IMeterActivationValidation meterActivationValidation : activeOnly(meterActivationValidations)) {
                meterActivationValidation.validate(interval);
            }
        }
    }

    Iterable<IMeterActivationValidation> activeOnly(Iterable<IMeterActivationValidation> validations) {
        return Iterables.filter(validations, MeterActivationValidation::isActive);
    }

    List<IMeterActivationValidation> manageMeterActivationValidations(MeterActivation meterActivation) {
        List<ValidationRuleSet> ruleSets = new ArrayList<>();
        for (ValidationRuleSetResolver ruleSetResolver : ruleSetResolvers) {
            ruleSets.addAll(ruleSetResolver.resolve(meterActivation));
        }
        List<IMeterActivationValidation> existingMeterActivationValidations = getIMeterActivationValidations(meterActivation);
        List<IMeterActivationValidation> returnList = new ArrayList<>();
        for (ValidationRuleSet ruleSet : ruleSets) {
            Optional<IMeterActivationValidation> meterActivationValidation = getForRuleSet(existingMeterActivationValidations, ruleSet);
            if (!meterActivationValidation.isPresent()) {
                returnList.add(applyRuleSet(ruleSet, meterActivation));
            } else {
                returnList.add(meterActivationValidation.get());
            }
        }
        for (IMeterActivationValidation existingMeterActivationValidation : existingMeterActivationValidations) {
            if (!ruleSets.contains(existingMeterActivationValidation.getRuleSet())) {
                existingMeterActivationValidation.makeObsolete();
            }
        }
        return returnList;
    }

    private Optional<IMeterActivationValidation> getForRuleSet(List<IMeterActivationValidation> meterActivations, ValidationRuleSet ruleSet) {
        for (IMeterActivationValidation meterActivation : meterActivations) {
            if (ruleSet.equals(meterActivation.getRuleSet())) {
                return Optional.of(meterActivation);
            }
        }
        return Optional.absent();
    }

    private List<IMeterActivationValidation> getIMeterActivationValidations(MeterActivation meterActivation) {
        Condition condition = where("meterActivation").isEqualTo(meterActivation).and(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        return dataModel.query(IMeterActivationValidation.class).select(condition);
    }

    private List<IMeterActivationValidation> getActiveIMeterActivationValidations(MeterActivation meterActivation) {
        Condition condition = where("meterActivation").isEqualTo(meterActivation).and(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull()).and(where("active").isEqualTo(true));
        return dataModel.query(IMeterActivationValidation.class).select(condition);
    }

    private IMeterActivationValidation applyRuleSet(ValidationRuleSet ruleSet, MeterActivation meterActivation) {
        IMeterActivationValidation meterActivationValidation = MeterActivationValidationImpl.from(dataModel, meterActivation);
        meterActivationValidation.setRuleSet(ruleSet);


        for (Channel channel : meterActivation.getChannels()) {
            if (!ruleSet.getRules(channel.getReadingTypes()).isEmpty()) {
                meterActivationValidation.addChannelValidation(channel);
            }
        }

        meterActivationValidation.save();
        return meterActivationValidation;
    }

/*    @Override
    public List<? extends MeterActivationValidation> getMeterActivationValidationsForMeterActivation(MeterActivation meterActivation) {
        return getOrCreateMeterActivationValidations(meterActivation);
    }*/

    @Override
    public List<? extends MeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation) {
        return manageMeterActivationValidations(meterActivation);
    }

    @Override
    public List<? extends MeterActivationValidation> getActiveMeterActivationValidations(MeterActivation meterActivation) {
        return getActiveIMeterActivationValidations(meterActivation);
    }

    @Override
    public Validator getValidator(String implementation) {
        ValidatorCreator validatorCreator = new DefaultValidatorCreator();
        return validatorCreator.getTemplateValidator(implementation);
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings) {
        List<DataValidationStatus> result = new ArrayList<>(readings.size());
        if (!readings.isEmpty()) {
            List<ChannelValidation> channelValidations = getChannelValidations(channel);
            boolean configured = !channelValidations.isEmpty();
            boolean active = channelValidations.stream().anyMatch(ChannelValidation::hasActiveRules);
            Date lastChecked = configured ? getMinLastChecked(channelValidations.stream()
                    .filter(ChannelValidation::hasActiveRules)
                    .map(ChannelValidation::getLastChecked).collect(Collectors.toSet())) : null;

            ListMultimap<String, IValidationRule> validationRuleMap = getValidationRulesPerReadingQuality(channelValidations);

            ListMultimap<Date, ReadingQualityRecord> readingQualities = getReadingQualities(channel, getInterval(readings));
            ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> qualities = (readingQualities.containsKey(reading.getTimeStamp()) ? readingQualities.get(reading.getTimeStamp()) : new ArrayList<ReadingQualityRecord>());
                if (qualities.isEmpty() && configured) {
                    if (wasValidated(lastChecked, reading.getTimeStamp())) {
                        qualities.add(channel.createReadingQuality(validatedAndOk, reading.getTimeStamp()));
                    }
                }
                boolean fullyValidated = false;
                if (configured && active) {
                    fullyValidated = (wasValidated(lastChecked, reading.getTimeStamp()));
                }
                result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, validationRuleMap));

            }
        }

        return result;
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, Interval interval) {
        List<DataValidationStatus> result = new ArrayList<>();
        List<ChannelValidation> channelValidations = getChannelValidations(channel);
        boolean configured = !channelValidations.isEmpty();
        boolean active = channelValidations.stream().anyMatch(ChannelValidation::hasActiveRules);
        Date lastChecked = configured ? getMinLastChecked(channelValidations.stream()
                .filter(ChannelValidation::hasActiveRules)
                .map(ChannelValidation::getLastChecked).collect(Collectors.toSet())) : null;

        ListMultimap<String, IValidationRule> validationRuleMap = getValidationRulesPerReadingQuality(channelValidations);

        ListMultimap<Date, ReadingQualityRecord> readingQualities = getReadingQualities(channel, interval);

        for (Date readingTimestamp : readingQualities.keySet()) {
            List<ReadingQuality> qualities = new ArrayList<>(readingQualities.containsKey(readingTimestamp) ? readingQualities.get(readingTimestamp) : new ArrayList<ReadingQuality>());
            boolean wasValidated = wasValidated(lastChecked, readingTimestamp);
            if (qualities.isEmpty() && configured && wasValidated) {
                qualities.add(OK_QUALITY);
            }
            boolean fullyValidated = configured && active && wasValidated;
            result.add(createDataValidationStatusListFor(readingTimestamp, fullyValidated, qualities, validationRuleMap));

        }
        return result;
    }

    private boolean wasValidated(Date lastChecked, Date readingTimestamp) {
        return lastChecked != null && readingTimestamp.compareTo(lastChecked) <= 0;
    }

    private ListMultimap<String, IValidationRule> getValidationRulesPerReadingQuality(List<ChannelValidation> channelValidations) {
        Query<IValidationRule> ruleQuery = getAllValidationRuleQuery();
        Set<IValidationRule> rules = channelValidations.stream()
                .map(ChannelValidation::getMeterActivationValidation)
                .map(MeterActivationValidation::getRuleSet)
                .map(ValidationRuleSet::getId)
                .map(id -> ruleQuery.select(Operator.EQUAL.compare("ruleSetId", id)))
                .flatMap(l -> l.stream())
                .collect(Collectors.toSet());
        return Multimaps.index(rules, i -> i.getReadingQualityType().getCode());
    }

    private Query<IValidationRule> getAllValidationRuleQuery() {
        return queryService.wrap(dataModel.query(IValidationRule.class));
    }

    private DataValidationStatus createDataValidationStatusListFor(Date timeStamp, boolean completelyValidated, List<? extends ReadingQuality> qualities, ListMultimap<String, IValidationRule> validationRuleMap) {
        DataValidationStatusImpl validationStatus = new DataValidationStatusImpl(timeStamp, completelyValidated);
        for (ReadingQuality quality : qualities) {
            validationStatus.addReadingQuality(quality, filterDuplicates(validationRuleMap.get(quality.getTypeCode())));
        }
        return validationStatus;
    }

    private List<IValidationRule> filterDuplicates(List<IValidationRule> iValidationRules) {
        Map<String, IValidationRule> filter = new HashMap<>();
        for (IValidationRule iValidationRule : iValidationRules) {
            if (filter.containsKey(iValidationRule.getImplementation())) {
                if (iValidationRule.getObsoleteDate() != null) {
                    filter.put(iValidationRule.getImplementation(), iValidationRule);
                }
            } else {
                filter.put(iValidationRule.getImplementation(), iValidationRule);
            }
        }
        return new ArrayList<>(filter.values());
    }

    private Interval getInterval(List<? extends BaseReading> readings) {
        Date min = null;
        Date max = null;
        for (BaseReading reading : readings) {
            if (min == null || reading.getTimeStamp().before(min)) {
                min = reading.getTimeStamp();
            }
            if (max == null || reading.getTimeStamp().after(max)) {
                max = reading.getTimeStamp();
            }
        }
        return new Interval(min, max);
    }

    private ListMultimap<Date, ReadingQualityRecord> getReadingQualities(Channel channel, Interval interval) {
        List<ReadingQualityRecord> readingQualities = channel.findReadingQuality(interval);
        return Multimaps.index(readingQualities, new Function<ReadingQualityRecord, Date>() {
            @Override
            public Date apply(ReadingQualityRecord input) {
                return input.getReadingTimestamp();
            }
        });
    }

    private List<ChannelValidation> getChannelValidations(Channel channel) {
        return dataModel.mapper(ChannelValidation.class).find("channel", channel);
    }

    private Date getMinLastChecked(Iterable<Date> dates) {
        Comparator<Date> comparator = NullSafeOrdering.NULL_IS_SMALLEST.get();
        return dates.iterator().hasNext() ? Ordering.from(comparator).min(dates) : null;
    }

    @Override
    public List<Validator> getAvailableValidators() {
        ValidatorCreator validatorCreator = new DefaultValidatorCreator();
        return validatorFactories.stream()
                .flatMap(f -> f.available().stream())
                .map(validatorCreator::getTemplateValidator)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAllDataValidated(MeterActivation meterActivation) {
        for (IMeterActivationValidation meterActivationValidation : getIMeterActivationValidations(meterActivation)) {
            if (!meterActivationValidation.isAllDataValidated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ValidationEvaluator getEvaluator() {
        return new ValidationEvaluatorImpl();
    }

    private List<ChannelValidation> getChannelValidationsWithActiveRules(Channel channel) {
        return dataModel.mapper(ChannelValidation.class).find("channel", channel, "activeRules", true);
    }



    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(ValidatorFactory validatorfactory) {
        validatorFactories.add(validatorfactory);
    }

    public void removeResource(ValidatorFactory validatorfactory) {
        validatorFactories.remove(validatorfactory);
    }

    class DefaultValidatorCreator implements ValidatorCreator {

        @Override
        public Validator getValidator(String implementation, Map<String, Object> props) {
            return validatorFactories.stream()
                    .filter(hasImplementation(implementation))
                    .findFirst()
                    .orElseThrow(() -> new ValidatorNotFoundException(thesaurus, implementation))
                    .create(implementation, props);
        }

        public Validator getTemplateValidator(String implementation) {
            return validatorFactories.stream()
                    .filter(hasImplementation(implementation))
                    .findFirst()
                    .orElseThrow(() -> new ValidatorNotFoundException(thesaurus, implementation))
                    .createTemplate(implementation);
        }

        private Predicate<ValidatorFactory> hasImplementation(String implementation) {
            return f -> f.available().contains(implementation);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.add(resolver);
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet validationRuleSet) {
        for (ValidationRuleSetResolver resolver : ruleSetResolvers) {
            if (resolver.isValidationRuleSetInUse(validationRuleSet)) {
                return true;
            }
        }
        return false;
    }

    public void removeValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.remove(resolver);
    }

    DataModel getDataModel() {
        return dataModel;
    }
}
