package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Upcast;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.isNull;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + ValidationService.COMPONENTNAME, immediate = true)
public final class ValidationServiceImpl implements ValidationService, InstallService {

    private static final Upcast<IValidationRuleSet, ValidationRuleSet> UPCAST = new Upcast<>();
    private static final Function<IMeterActivationValidation, Date> METER_ACTIVATION_MIN_LAST_CHECKED = input -> input == null ? null : input.getMinLastChecked();

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

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "NLS", "EVT", "MTR");

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
                getUpdatedMeterActivationValidations(currentMeterActivation.get());
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
            throw new IllegalArgumentException("Last checked date is absent");
        }
        getUpdatedMeterActivationValidations(meterActivation);
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(meterActivation);
        validations.stream().forEach(v -> saveLastChecked(v, date));
    }

    @Override
    public void updateLastChecked(Channel channel, Date date) {
        if (channel == null || date == null) {
            throw new IllegalArgumentException("Last checked date or channel is absent");
        }
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(channel.getMeterActivation());
        validations.stream().map(m -> m.getChannelValidation(channel))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ChannelValidation::hasActiveRules)
                .map(ChannelValidationImpl.class::cast)
                .forEach(cv -> {
                    cv.setLastChecked(date);
                    cv.getMeterActivationValidation().save();
                });
    }

    private void saveLastChecked(IMeterActivationValidation validation, Date date) {
        validation.updateLastChecked(date);
        validation.save();
    }

    @Override
    public boolean isValidationActive(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel is absent");
        }
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
        return Optional.fromNullable(dates.stream().min(naturalOrder()).orElse(null));
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
        if (isValidationActive(meterActivation)) {
            List<IMeterActivationValidation> meterActivationValidations = getUpdatedMeterActivationValidations(meterActivation);
            meterActivationValidations.stream()
                    .filter(MeterActivationValidation::isActive)
                    .forEach(m -> m.validate(interval));
        }
    }

    @Override
    public void validateForNewData(MeterActivation meterActivation, Interval interval) {
        boolean validationActive = isValidationActive(meterActivation);
        List<IMeterActivationValidation> meterActivationValidations = getUpdatedMeterActivationValidations(meterActivation);

        meterActivationValidations.stream()
                .peek(m -> {
                    if (!validationActive || !m.isActive() && interval.startsBefore(m.getMaxLastChecked())) {
                        handleDataOverwrite(m, interval);
                    }
                })
                .filter(m -> validationActive)
                .filter(MeterActivationValidation::isActive)
                .forEach(m -> m.validate(interval));
    }

    private void handleDataOverwrite(IMeterActivationValidation meterActivationValidation, Interval interval) {
        meterActivationValidation.getChannelValidations().stream()
                .filter(c -> interval.startsBefore(c.getLastChecked()))
                .map(IChannelValidation.class::cast)
                .forEach(c -> c.setLastChecked(interval.getStart()));
        meterActivationValidation.save();

        meterActivationValidation.getChannelValidations().stream()
                .map(ChannelValidation::getChannel)
                .flatMap(c -> c.findReadingQuality(interval).stream())
                .filter(isValidationQuality())
                .forEach(ReadingQualityRecord::delete);
    }

    private Predicate<ReadingQualityRecord> isValidationQuality() {
        return isSuspect().or(isValidationRuleQuality());
    }

    private Predicate<ReadingQualityRecord> isValidationRuleQuality() {
        return isOfMDM().and(isOfValidationRule().or(isMissing()));
    }

    private Predicate<ReadingQualityRecord> isMissing() {
        return q -> QualityCodeIndex.KNOWNMISSINGREAD.equals(q.getType().qualityIndex().orElse(null));
    }

    private Predicate<ReadingQualityRecord> isOfValidationRule() {
        return q -> QualityCodeCategory.VALIDATION.equals(q.getType().category().orElse(null));
    }

    private Predicate<ReadingQualityRecord> isOfMDM() {
        return q -> QualityCodeSystem.MDM.equals(q.getType().system().orElse(null));
    }

    private Predicate<ReadingQualityRecord> isSuspect() {
        return q -> QualityCodeIndex.SUSPECT.equals(q.getType().qualityIndex().orElse(null));
    }

    private boolean isValidationActive(MeterActivation meterActivation) {
        boolean validationActive = true;
        // when meterActivation is not linked to a meter, validation is always run.
        if (meterActivation.getMeter().isPresent()) {
            // if meteractivation is linked to a meter, we only run validation when it is actively configured on the meter to run it
            Optional<MeterValidationImpl> found = getMeterValidation(meterActivation.getMeter().get());
            validationActive = found.isPresent() && found.get().getActivationStatus();
        }
        return validationActive;
    }

    @Override
    public void validate(MeterActivation meterActivation, String readingTypeCode, Interval interval) {
        if (isValidationActive(meterActivation)) {
            List<IMeterActivationValidation> meterActivationValidations = getUpdatedMeterActivationValidations(meterActivation);
            meterActivationValidations.stream()
                    .filter(MeterActivationValidation::isActive)
                    .forEach(m -> m.validate(interval, readingTypeCode));
        }

    }

    List<IMeterActivationValidation> getUpdatedMeterActivationValidations(MeterActivation meterActivation) {
        List<ValidationRuleSet> ruleSets = ruleSetResolvers.stream()
                .flatMap(r -> r.resolve(meterActivation).stream())
                .collect(Collectors.toList());
        List<IMeterActivationValidation> existingMeterActivationValidations = getIMeterActivationValidations(meterActivation);
        List<IMeterActivationValidation> returnList = ruleSets.stream()
                .map(r -> Pair.of(r, getForRuleSet(existingMeterActivationValidations, r)))
                .map(p -> p.getLast().orElseGet(() -> applyRuleSet(p.getFirst(), meterActivation)))
                .collect(Collectors.toList());

        existingMeterActivationValidations.stream()
                .filter(m -> !ruleSets.contains(m.getRuleSet()))
                .forEach(IMeterActivationValidation::makeObsolete);

        return returnList;
    }

    private java.util.Optional<IMeterActivationValidation> getForRuleSet(List<IMeterActivationValidation> meterActivations, ValidationRuleSet ruleSet) {
        for (IMeterActivationValidation meterActivation : meterActivations) {
            if (ruleSet.equals(meterActivation.getRuleSet())) {
                return java.util.Optional.of(meterActivation);
            }
        }
        return java.util.Optional.empty();
    }

    List<IMeterActivationValidation> getIMeterActivationValidations(MeterActivation meterActivation) {
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

        meterActivation.getChannels().stream()
                .filter(c -> !ruleSet.getRules(c.getReadingTypes()).isEmpty())
                .forEach(meterActivationValidation::addChannelValidation);

        meterActivationValidation.save();
        return meterActivationValidation;
    }

/*    @Override
    public List<? extends MeterActivationValidation> getMeterActivationValidationsForMeterActivation(MeterActivation meterActivation) {
        return getOrCreateMeterActivationValidations(meterActivation);
    }*/

    @Override
    public List<? extends MeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation) {
        return getUpdatedMeterActivationValidations(meterActivation);
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

    Query<IValidationRule> getAllValidationRuleQuery() {
        return queryService.wrap(dataModel.query(IValidationRule.class));
    }

    List<ChannelValidation> getChannelValidations(Channel channel) {
        return dataModel.mapper(ChannelValidation.class).find("channel", channel);
    }

    private Date getMinLastChecked(Iterable<Date> dates) {
        Comparator<Date> comparator = nullsFirst(naturalOrder());
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
    public ValidationEvaluator getEvaluator() {
        return new ValidationEvaluatorImpl(this);
    }

    @Override
    public ValidationEvaluator getEvaluator(Meter meter, Interval interval) {
        return new ValidationEvaluatorForMeter(this, meter, interval);
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
