package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.Upcast;
import com.elster.jupiter.util.comparators.NullSafeOrdering;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.MeterValidation;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + ValidationService.COMPONENTNAME, immediate = true)
public final class ValidationServiceImpl implements ValidationService, InstallService {

    private static final Upcast<IValidationRuleSet, ValidationRuleSet> UPCAST = new Upcast<>();
    private static final Ordering<ChannelValidation> NULLSAFE_ORDER_BY_LASTCHECKED = new Ordering<ChannelValidation>() {
        @Override
        public int compare(ChannelValidation left, ChannelValidation right) {
            return NullSafeOrdering.NULL_IS_SMALLEST.<Date>get().compare(getLastChecked(left), getLastChecked(right));
        }

        private Date getLastChecked(ChannelValidation validation) {
            return validation == null ? null : validation.getLastChecked();
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

    public ValidationServiceImpl() {
    }

    @Inject
    ValidationServiceImpl(Clock clock, EventService eventService, MeteringService meteringService, OrmService ormService, QueryService queryService, NlsService nlsService) {
        this.clock = clock;
        this.eventService = eventService;
        this.meteringService = meteringService;
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
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
            }
        });
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public void install() {
        new InstallerImpl(dataModel, eventService, thesaurus).install(true, true);
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
    public MeterValidation createMeterValidation(MeterActivation activation) {
        MeterValidation meterValidation = MeterValidationImpl.from(dataModel, activation);
        meterValidation.save();
        return meterValidation;
    }

    @Override
    public Optional<MeterValidation> getMeterValidation(MeterActivation meterActivation) {
        return dataModel.mapper(MeterValidation.class).getOptional(meterActivation.getId());
    }

    @Override
    public void setMeterValidationStatus(MeterActivation meterActivation, boolean status) {
        Optional<MeterValidation> meterValidationRef = getMeterValidation(meterActivation);
        if (!meterValidationRef.isPresent()) {
            meterValidationRef = Optional.of(createMeterValidation(meterActivation));
        }
        MeterValidation meterValidation = meterValidationRef.get();
        meterValidation.setActivationStatus(status);
        meterValidation.save();
    }

    @Override
    public void setLastChecked(MeterActivation meterActivation, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Last Checked Date is absent");
        }
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(meterActivation);
        for (MeterActivationValidation validation : validations) {
            for (ChannelValidation channelValidation : validation.getChannelValidations()) {
                channelValidation.setLastChecked(date);
            }
            validation.save();
        }
    }

    @Override
    public Date getLastChecked(MeterActivation meterActivation) {
        List<IMeterActivationValidation> validations = getActiveIMeterActivationValidations(meterActivation);
        Set<ChannelValidation> chennelValidations = new LinkedHashSet<>();
        for (MeterActivationValidation validation : validations) {
            chennelValidations.addAll(validation.getChannelValidations());
        }
        Date date = getMinLastChecked(new ArrayList<ChannelValidation>(chennelValidations));
        return date == null ? clock.now() : date;
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
        Optional<MeterValidation> found = getMeterValidation(meterActivation);
        if (found.isPresent() && found.get().getActivationStatus()) {
            List<MeterActivationValidation> meterActivationValidations = getOrCreateMeterActivationValidations(meterActivation);
            for (MeterActivationValidation meterActivationValidation : activeOnly(meterActivationValidations)) {
                meterActivationValidation.validate(interval);
            }
        }
    }

    Iterable<MeterActivationValidation> activeOnly(Iterable<MeterActivationValidation> validations) {
        return Iterables.filter(validations, new Predicate<MeterActivationValidation>() {
            @Override
            public boolean apply(MeterActivationValidation input) {
                return input.isActive();
            }
        });
    }

    @Override
    public List<MeterActivationValidation> getOrCreateMeterActivationValidations(MeterActivation meterActivation) {
        return manageMeterActivationValidations(meterActivation);
    }

    private List<MeterActivationValidation> manageMeterActivationValidations(MeterActivation meterActivation) {
        List<ValidationRuleSet> ruleSets = new ArrayList<>();
        for (ValidationRuleSetResolver ruleSetResolver : ruleSetResolvers) {
            ruleSets.addAll(ruleSetResolver.resolve(meterActivation));
        }
        List<IMeterActivationValidation> existingMeterActivationValidations = getIMeterActivationValidations(meterActivation);
        List<MeterActivationValidation> returnList = new ArrayList<>();
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
            meterActivationValidation.addChannelValidation(channel);
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
        return getIMeterActivationValidations(meterActivation);
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
    public List<List<ReadingQuality>> getValidationStatus(Channel channel, List<? extends BaseReading> readings) {
        List<List<ReadingQuality>> result = new ArrayList<>(readings.size());
        if (!readings.isEmpty()) {
            List<ChannelValidation> channelValidations = getChannelValidations(channel);
            Date lastChecked = getMinLastChecked(channelValidations);

            ListMultimap<Date, ReadingQuality> readingQualities = getReadingQualities(channel, getInterval(readings));
            ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
            for (BaseReading reading : readings) {
                addReadingQualities(lastChecked, reading, readingQualities, validatedAndOk, channel, result);
            }
        }
        return result;
    }

    private void addReadingQualities(Date lastChecked, BaseReading reading, ListMultimap<Date, ReadingQuality> readingQualities,
                                     ReadingQualityType validatedAndOk, Channel channel, List<List<ReadingQuality>> result) {
        List<ReadingQuality> qualities = readingQualities.get(reading.getTimeStamp());
        if (lastChecked == null || lastChecked.before(reading.getTimeStamp())) {
            result.add(qualities == null ? Collections.<ReadingQuality>emptyList() : qualities);
        } else {
            if (qualities == null || qualities.isEmpty()) {
                result.add(Arrays.asList(channel.createReadingQuality(validatedAndOk, reading.getTimeStamp())));
            } else {
                result.add(qualities);
            }
        }
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

    private ListMultimap<Date, ReadingQuality> getReadingQualities(Channel channel, Interval interval) {
        List<ReadingQuality> readingQualities = channel.findReadingQuality(interval);
        return Multimaps.index(readingQualities, new Function<ReadingQuality, Date>() {
            @Override
            public Date apply(ReadingQuality input) {
                return input.getReadingTimestamp();
            }
        });
    }

    private List<ChannelValidation> getChannelValidations(Channel channel) {
        return dataModel.mapper(ChannelValidation.class).find("channel", channel);
    }

    private Date getMinLastChecked(List<ChannelValidation> channelValidations) {
        return channelValidations.isEmpty() ? null : NULLSAFE_ORDER_BY_LASTCHECKED.min(channelValidations).getLastChecked();
    }

    @Override
    public List<Validator> getAvailableValidators() {
        ValidatorCreator validatorCreator = new DefaultValidatorCreator();
        List<Validator> result = new ArrayList<Validator>();
        for (ValidatorFactory factory : validatorFactories) {
            for (String implementation : factory.available()) {
                Validator validator = validatorCreator.getTemplateValidator(implementation);
                result.add(validator);
            }
        }
        return result;
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
            for (ValidatorFactory factory : validatorFactories) {
                if (factory.available().contains(implementation)) {
                    return factory.create(implementation, props);
                }
            }
            throw new ValidatorNotFoundException(thesaurus, implementation);
        }

        public Validator getTemplateValidator(String implementation) {
            for (ValidatorFactory factory : validatorFactories) {
                if (factory.available().contains(implementation)) {
                    return factory.createTemplate(implementation);
                }
            }
            throw new ValidatorNotFoundException(thesaurus, implementation);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.add(resolver);
    }

    @Override
    public List<ValidationRuleSetResolver> getValidationRuleSetResolvers() {
        return ImmutableList.copyOf(ruleSetResolvers);
    }

    public void removeValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.remove(resolver);
    }

    DataModel getDataModel() {
        return dataModel;
    }
}
