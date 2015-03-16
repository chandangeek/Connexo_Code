package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.*;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + ValidationService.COMPONENTNAME, immediate = true)
public class ValidationServiceImpl implements ValidationService, InstallService {

    public static final String DESTINATION_NAME = "DataValidation";
    public static final String SUBSCRIBER_NAME = "DataValidation";
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile Clock clock;
    private volatile MessageService messageService;
    private volatile TaskService taskService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile UserService userService;

    private final List<ValidatorFactory> validatorFactories = new CopyOnWriteArrayList<>();
    private final List<ValidationRuleSetResolver> ruleSetResolvers = new CopyOnWriteArrayList<>();
    private Optional<DestinationSpec> destinationSpec = Optional.empty();

    public ValidationServiceImpl() {
    }

    @Inject
    ValidationServiceImpl(Clock clock,MessageService messageService, EventService eventService, TaskService taskService, MeteringService meteringService, MeteringGroupsService meteringGroupsService, OrmService ormService, QueryService queryService, NlsService nlsService, UserService userService, Publisher publisher) {
        this.clock = clock;
        this.messageService = messageService;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.taskService = taskService;
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
                bind(TaskService.class).toInstance(taskService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
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
        new InstallerImpl(dataModel, eventService, thesaurus, userService,messageService).install(true, true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "NLS", "EVT", "MTR", "MTG", TaskService.COMPONENTNAME);

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
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
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
        return dataModel.getInstance(ValidationRuleSetImpl.class).init(name); 
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name, String description) {
        ValidationRuleSet set = dataModel.getInstance(ValidationRuleSetImpl.class).init(name, description);
        set.save();
        return set;
    }

    @Override
    public DestinationSpec getDestination() {
        if (!destinationSpec.isPresent()) {
            destinationSpec = messageService.getDestinationSpec(DESTINATION_NAME);
        }
        return destinationSpec.orElse(null);
    }

    @Override
    public void activateValidation(Meter meter) {
        Optional<MeterValidationImpl> meterValidation = getMeterValidation(meter);
        if (meterValidation.isPresent()) {
            if (!meterValidation.get().getActivationStatus()) {
                meterValidation.get().setActivationStatus(true);
                meterValidation.get().save();
                meter.getCurrentMeterActivation()
                	.map(this::updatedMeterActivationValidationsFor)
                	.ifPresent(MeterActivationValidationContainer::activate);
            } // else already active
        } else {
            createMeterValidation(meter, true);
            meter.getCurrentMeterActivation().ifPresent(this::updatedMeterActivationValidationsFor);
        }
    }

    @Override
    public void deactivateValidation(Meter meter) {
        getMeterValidation(meter)
        	.filter(MeterValidationImpl::getActivationStatus)
        	.ifPresent( meterValidation -> {
        		meterValidation.setActivationStatus(false);
        		meterValidation.save();
        	});
        }

    @Override
    public boolean validationEnabled(Meter meter) {
        return getMeterValidation(meter).filter(MeterValidationImpl::getActivationStatus).isPresent();
    }

    Optional<MeterValidationImpl> getMeterValidation(Meter meter) {
        return dataModel.mapper(MeterValidationImpl.class).getOptional(meter.getId());
    }

    private void createMeterValidation(Meter meter, boolean active) {
        MeterValidationImpl meterValidation = new MeterValidationImpl(dataModel).init(meter);
        meterValidation.setActivationStatus(active);
        meterValidation.save();
    }

    @Override
    public void updateLastChecked(MeterActivation meterActivation, Instant date) {
        updatedMeterActivationValidationsFor(meterActivation).updateLastChecked(Objects.requireNonNull(date));       
    }

    @Override
    public void updateLastChecked(Channel channel, Instant date) {
    	activeMeterActivationValidationsFor(Objects.requireNonNull(channel).getMeterActivation())
    		.updateLastChecked(channel, Objects.requireNonNull(date));
     }

    
    @Override
    public boolean isValidationActive(Channel channel) {
    	return activeMeterActivationValidationsFor(Objects.requireNonNull(channel).getMeterActivation()).isValidationActive(channel);    		
    }

    @Override
    public Optional<Instant> getLastChecked(MeterActivation meterActivation) {
    	return activeMeterActivationValidationsFor(Objects.requireNonNull(meterActivation)).getLastChecked();
    }

    @Override
    public Optional<Instant> getLastChecked(Channel channel) {
    	return activeMeterActivationValidationsFor(Objects.requireNonNull(channel).getMeterActivation()).getLastChecked(channel);    	
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
    	if (isValidationActive(meterActivation)) {
    		updatedMeterActivationValidationsFor(meterActivation).validate();
    	}
    }
    
    @Override
    public void validate(MeterActivation meterActivation, ReadingType readingType) {
    	if (isValidationActive(meterActivation)) {
    		updatedMeterActivationValidationsFor(meterActivation).validate(readingType);
    	}
    }
    
    public void validate(MeterActivation meterActivation, Map<Channel,Range<Instant>> ranges) {
    	MeterActivationValidationContainer container = updatedMeterActivationValidationsFor(meterActivation);
    	container.moveLastCheckedBefore(ranges);
    	if (isValidationActive(meterActivation)) {
    		container.validate();
    	}
    }

    private boolean isValidationActive(MeterActivation meterActivation) {
    	Optional<Meter> meter = meterActivation.getMeter();
    	return meter
    		.flatMap(this::getMeterValidation)
    		.map(MeterValidationImpl::getActivationStatus)
    		.orElse(!meter.isPresent());
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
    
    MeterActivationValidationContainer activeMeterActivationValidationsFor(MeterActivation meterActivation) {
    	return MeterActivationValidationContainer.of(getActiveIMeterActivationValidations(meterActivation));
    }
    
    MeterActivationValidationContainer updatedMeterActivationValidationsFor(MeterActivation meterActivation) {
    	return MeterActivationValidationContainer.of(getUpdatedMeterActivationValidations(meterActivation));
    }

    private Optional<IMeterActivationValidation> getForRuleSet(List<IMeterActivationValidation> meterActivations, ValidationRuleSet ruleSet) {
    	return meterActivations.stream().filter(meterActivation -> ruleSet.equals(meterActivation.getRuleSet())).findFirst();
    }

    List<IMeterActivationValidation> getIMeterActivationValidations(MeterActivation meterActivation) {
        Condition condition = where("meterActivation").isEqualTo(meterActivation).and(where("obsoleteTime").isNull());
        return dataModel.query(IMeterActivationValidation.class, IChannelValidation.class).select(condition);
    }

    private List<IMeterActivationValidation> getActiveIMeterActivationValidations(MeterActivation meterActivation) {
        Condition condition = where("meterActivation").isEqualTo(meterActivation).and(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull()).and(where("active").isEqualTo(true));
        return dataModel.query(IMeterActivationValidation.class, IChannelValidation.class).select(condition);
    }

    private IMeterActivationValidation applyRuleSet(ValidationRuleSet ruleSet, MeterActivation meterActivation) {
        IMeterActivationValidation meterActivationValidation = new MeterActivationValidationImpl(dataModel, clock).init(meterActivation);
        meterActivationValidation.setRuleSet(ruleSet);
        meterActivation.getChannels().stream()
                .filter(c -> !ruleSet.getRules(c.getReadingTypes()).isEmpty())
                .forEach(meterActivationValidation::addChannelValidation);
        meterActivationValidation.save();
        return meterActivationValidation;
    }

    public List<? extends IMeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation) {
        return getUpdatedMeterActivationValidations(meterActivation);
    }

    public List<? extends IMeterActivationValidation> getActiveMeterActivationValidations(MeterActivation meterActivation) {
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

    List<? extends IChannelValidation> getChannelValidations(Channel channel) {
        return dataModel.mapper(IChannelValidation.class).find("channel", channel);
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

	private Optional<? extends IMeterActivationValidation> findMeterActivationValidation(MeterActivation meterActivation, ValidationRuleSet ruleSet) {
		return getMeterActivationValidations(meterActivation).stream()
		.filter(meterActivationValidation -> meterActivationValidation.getRuleSet().equals(ruleSet))
		.findFirst();
	}

	@Override
	public void activate(MeterActivation meterActivation, ValidationRuleSet ruleSet) {
		findMeterActivationValidation(meterActivation, ruleSet).ifPresent(meterActivationValidation -> {
			meterActivationValidation.activate();
			meterActivationValidation.save();
		});			
	}

	@Override
	public void deactivate(MeterActivation meterActivation, ValidationRuleSet ruleSet) {
		findMeterActivationValidation(meterActivation, ruleSet).ifPresent(meterActivationValidation -> {
			meterActivationValidation.deactivate();
			meterActivationValidation.save();
		});			
	}

	@Override
	public List<ValidationRuleSet> activeRuleSets(MeterActivation meterActivation) {
		return getActiveIMeterActivationValidations(meterActivation).stream()
			.map(IMeterActivationValidation::getRuleSet)
			.collect(Collectors.toList());				
	}

    @Override
    public DataValidationTaskBuilder newTaskBuilder() {
        return new DataValidationTaskBuilderImpl(dataModel, this);
    }

    @Override
    public Query<DataValidationTask> findValidationTasksQuery(){
        Query<DataValidationTask> ruleSetQuery = queryService.wrap(dataModel.query(DataValidationTask.class));
        return ruleSetQuery;
    }

    @Override
    public List<DataValidationTask> findValidationTasks() {
        return findValidationTasksQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
    }

    @Override
    public Optional<DataValidationTask> findValidationTask(long id) {
        return dataModel.mapper(DataValidationTask.class).getOptional(id);
    }
    
}
