package com.elster.jupiter.validation.impl;

import static com.elster.jupiter.util.Ranges.does;
import static com.elster.jupiter.util.conditions.Where.where;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

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
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Upcast;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
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
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + ValidationService.COMPONENTNAME, immediate = true)
public final class ValidationServiceImpl implements ValidationService, InstallService {

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
    ValidationServiceImpl(Clock clock, EventService eventService, MeteringService meteringService, OrmService ormService, QueryService queryService, NlsService nlsService, UserService userService, Publisher publisher) {
        this.clock = clock;
        this.eventService = eventService;
        this.meteringService = meteringService;
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        activate();
        install();
        // subscribe manually when not using OSGI
        ValidationEventHandler handler = new ValidationEventHandler();
        handler.setValidationService(this);
        publisher.addSubscriber(handler);
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
                meter.getCurrentMeterActivation().ifPresent(activation -> {
                    getUpdatedMeterActivationValidations(activation).stream()
                            .forEach(m -> {
                                m.activate();
                                m.save();
                            });
                });
            } // else already active
        } else {
            createMeterValidation(meter, true);
            meter.getCurrentMeterActivation().ifPresent(this::getUpdatedMeterActivationValidations);
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
    public void updateLastChecked(MeterActivation meterActivation, Instant date) {
        if (date == null) {
            throw new IllegalArgumentException("Last checked date is absent");
        }
        getUpdatedMeterActivationValidations(meterActivation);
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(meterActivation);
        validations.stream().forEach(v -> saveLastChecked(v, date));
    }

    @Override
    public void updateLastChecked(Channel channel, Instant date) {
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
                    cv.updateLastChecked(date);
                    cv.getMeterActivationValidation().save();
                });
    }

    private void saveLastChecked(IMeterActivationValidation validation, Instant date) {
        validation.updateLastChecked(date);
        validation.save();
    }

    @Override
    public boolean isValidationActive(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel is absent");
        }
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(channel.getMeterActivation());
        return validations.stream()
                .map(m -> m.getChannelValidation(channel))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(ChannelValidation::hasActiveRules);
    }

    @Override
    public Optional<Instant> getLastChecked(MeterActivation meterActivation) {
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(meterActivation);

        List<Instant> dates = validations.stream()
                .filter(m -> m.getChannelValidations().stream().anyMatch(ChannelValidation::hasActiveRules))
                .map(IMeterActivationValidation::getMinLastChecked)
                .collect(Collectors.toList());
        Comparator<Instant> comparator = nullsFirst(naturalOrder());
        return Optional.ofNullable(dates.iterator().hasNext() ? Ordering.from(comparator).min(dates) : null);
    }

    @Override
    public Optional<Instant> getLastChecked(Channel channel) {
    	return getActiveIMeterActivationValidations(channel.getMeterActivation()).stream()
            .map(m -> m.getChannelValidation(channel))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ChannelValidation::getLastChecked)
            .filter(Objects::nonNull)
            .min(naturalOrder());
    }

    @Override
    public Optional<? extends ValidationRuleSet> getValidationRuleSet(long id) {
        return dataModel.mapper(IValidationRuleSet.class).getOptional(id);
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(String name) {
        Condition condition = where("name").isEqualTo(name).and(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        return getRuleSetQuery().select(condition).stream().findFirst();        
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
    public void validate(MeterActivation meterActivation) {
    	throw new UnsupportedOperationException();
    }
    
    @Override
    public void validate(MeterActivation meterActivation, Range<Instant> interval) {
        if (isValidationActive(meterActivation)) {
            List<IMeterActivationValidation> meterActivationValidations = getUpdatedMeterActivationValidations(meterActivation);
            meterActivationValidations.stream()
                    .filter(MeterActivationValidation::isActive)
                    .forEach(m -> m.validate(interval));
        }
    }

    @Override
    public void validateForNewData(MeterActivation meterActivation, Range<Instant> interval) {
        boolean validationActive = isValidationActive(meterActivation);
        List<IMeterActivationValidation> meterActivationValidations = getUpdatedMeterActivationValidations(meterActivation);

        meterActivationValidations.stream()
                .peek(m -> {
                    if ((!validationActive || !m.isActive()) && does(interval).startBefore(m.getMaxLastChecked())) {
                        handleDataOverwrite(m, interval);
                    }
                })
                .filter(m -> validationActive)
                .filter(MeterActivationValidation::isActive)
                .forEach(m -> m.validate(interval));
    }

    private void handleDataOverwrite(IMeterActivationValidation meterActivationValidation, Range<Instant> interval) {
        meterActivationValidation.getChannelValidations().stream()
                .filter(c -> does(interval).startBefore(c.getLastChecked()))
                .map(IChannelValidation.class::cast)
                .forEach(c -> c.updateLastChecked(interval.lowerEndpoint()));
        meterActivationValidation.save();
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
    public void validate(MeterActivation meterActivation, String readingTypeCode, Range<Instant> interval) {
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

    private Optional<IMeterActivationValidation> getForRuleSet(List<IMeterActivationValidation> meterActivations, ValidationRuleSet ruleSet) {
        for (IMeterActivationValidation meterActivation : meterActivations) {
            if (ruleSet.equals(meterActivation.getRuleSet())) {
                return Optional.of(meterActivation);
            }
        }
        return Optional.empty();
    }

    List<IMeterActivationValidation> getIMeterActivationValidations(MeterActivation meterActivation) {
        Condition condition = where("meterActivation").isEqualTo(meterActivation).and(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        return dataModel.query(IMeterActivationValidation.class, ChannelValidation.class).select(condition);
    }

    private List<IMeterActivationValidation> getActiveIMeterActivationValidations(MeterActivation meterActivation) {
        Condition condition = where("meterActivation").isEqualTo(meterActivation).and(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull()).and(where("active").isEqualTo(true));
        return dataModel.query(IMeterActivationValidation.class, ChannelValidation.class).select(condition);
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
   
    List<IMeterActivationValidation> getMeterActivationValidations(Channel channel) {
    	return dataModel.query(IMeterActivationValidation.class, ChannelValidation.class).select(where("channelValidations.channel").isEqualTo(channel));
    }

    List<ChannelValidation> getChannelValidations(Channel channel) {
        return dataModel.mapper(ChannelValidation.class).find("channel", channel);
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
    public ValidationEvaluator getEvaluator(Meter meter, Range<Instant> interval) {
        return new ValidationEvaluatorForMeter(this, meter, interval);
    }

    @Override
    public void addValidatorFactory(ValidatorFactory validatorfactory) {
    	addResource(validatorfactory);
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
