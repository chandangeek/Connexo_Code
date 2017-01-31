/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetSearchEnabler;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.Privileges;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySetService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (13:35)
 */
@Component(name = "com.elster.jupiter.cps",
        service = {CustomPropertySetService.class, ServerCustomPropertySetService.class, TranslationKeyProvider.class},
        property = {
            "name=" + CustomPropertySetService.COMPONENT_NAME,
            "osgi.command.scope=cps",
            "osgi.command.function=status"
        })
public class CustomPropertySetServiceImpl implements ServerCustomPropertySetService, TranslationKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(CustomPropertySetServiceImpl.class.getName());

    private volatile OrmService ormService;
    private volatile DataModel dataModel;
    private volatile UserService userService;
    private volatile boolean installed = false;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile SearchService searchService;
    private volatile UpgradeService upgradeService;

    /**
     * Holds the {@link CustomPropertySet}s that were published on the whiteboard
     * and if they were published by the system or not.
     */
    private ConcurrentMap<CustomPropertySet, Boolean> publishedPropertySets = new ConcurrentHashMap<>();
    /**
     * Holds the {@link CustomPropertySet} that were taken from the whiteboard,
     * registered if that was not the case yet and then wrapping it in an {@link ActiveCustomPropertySet}.
     * Registereing a CustomPropertySet involves creating a DataModel and a Table for it.
     */
    private ConcurrentMap<String, ActiveCustomPropertySet> activePropertySets = new ConcurrentHashMap<>();
    private ConcurrentMap<Class<?>, List<CustomPropertySetSearchEnabler>> searchEnablers = new ConcurrentHashMap<>();

    // For OSGi purposes
    public CustomPropertySetServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public CustomPropertySetServiceImpl(OrmService ormService, NlsService nlsService, TransactionService transactionService, ThreadPrincipalService threadPrincipalService, UserService userService, SearchService searchService, UpgradeService upgradeService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setTransactionService(transactionService);
        this.setThreadPrincipalService(threadPrincipalService);
        this.setUserService(userService);
        this.setSearchService(searchService);
        this.setUpgradeService(upgradeService);
        this.activate();
    }

    @SuppressWarnings("unused")
    @Reference
    public void setOrmService(OrmService ormService) {
        this.setOrmService(ormService, true);
    }

    void setOrmService(OrmService ormService, boolean install) {
        this.ormService = ormService;
        DataModel dataModel = ormService.newDataModel(CustomPropertySetService.COMPONENT_NAME, "Custom Property Sets");
        if (install) {
            for (TableSpecs tableSpecs : TableSpecs.values()) {
                tableSpecs.addTo(dataModel);
            }
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(this.getComponentName(), Layer.DOMAIN);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference(name = "ZZZ", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @SuppressWarnings("unused")
    public void registerCustomPropertySetSearchEnabler(CustomPropertySetSearchEnabler enabler) {
        List<CustomPropertySetSearchEnabler> enablers = this.searchEnablers.get(enabler.getDomainClass());
        if (enablers == null) {
            enablers = new ArrayList<>();
            this.searchEnablers.put(enabler.getDomainClass(), enablers);
        }
        enablers.add(enabler);
    }

    @SuppressWarnings("unused")
    public void unregisterCustomPropertySetSearchEnabler(CustomPropertySetSearchEnabler enabler) {
        List<CustomPropertySetSearchEnabler> enablers = this.searchEnablers.get(enabler.getDomainClass());
        if (enablers != null) {
            enablers.remove(enabler);
        }
    }

    boolean isSearchEnabledForCustomPropertySet(CustomPropertySet<?, ?> customPropertySet, List<SearchablePropertyConstriction> constrictions) {
        List<CustomPropertySetSearchEnabler> enablers = this.searchEnablers.get(customPropertySet.getDomainClass());
        return enablers != null && enablers.stream().allMatch(enabler -> enabler.enableWhen(customPropertySet, constrictions));
    }

    List<SearchableProperty> getConstrainingPropertiesForCustomPropertySet(CustomPropertySet<?, ?> customPropertySet, List<SearchablePropertyConstriction> constrictions) {
        List<CustomPropertySetSearchEnabler> enablers = this.searchEnablers.get(customPropertySet.getDomainClass());
        return enablers == null ? Collections.emptyList() : enablers.stream()
                .flatMap(enabler -> enabler.getConstrainingProperties(customPropertySet, constrictions).stream())
                .collect(Collectors.toList());
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TransactionService.class).toInstance(transactionService);
                bind(CustomPropertySetService.class).toInstance(CustomPropertySetServiceImpl.this);
                bind(ServerCustomPropertySetService.class).toInstance(CustomPropertySetServiceImpl.this);
                bind(UserService.class).toInstance(userService);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, ImmutableMap.of(
                Version.version(10, 2), UpgraderV10_2.class
        ));
        this.installed = true;
        this.registerAllCustomPropertySets();
    }

    private <D, T extends PersistentDomainExtension<D>> Module getCustomPropertySetModule(final DataModel dataModel, final CustomPropertySet<D, T> customPropertySet) {

        Module bindings = new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(CustomPropertySetService.class).toInstance(CustomPropertySetServiceImpl.this);
            }
        };
        Optional<Module> cpsModule = customPropertySet.getPersistenceSupport().module();
        if (cpsModule.isPresent()) {
            bindings = Modules.override(bindings).with(cpsModule.get());
        }
        return bindings;
    }

    private void registerAllCustomPropertySets() {
        if (this.installed) {
            this.doRegisterAllCustomPropertySets();
        }
    }

    private void doRegisterAllCustomPropertySets() {
        if (!this.publishedPropertySets.isEmpty()) {
            /* Service is already installed
             * therefore any new CustomPropertySet that is published
             * on the whiteboard will register immediately instead
             * of being added to the List of published CustomPropertySet.
             * As a consequence, it is safe to clear the List after all have been registered. */
            this.publishedPropertySets.forEach(this::registerCustomPropertySet);
            this.publishedPropertySets.clear();
        } else {
            LOGGER.fine("No custom property sets have registered yet, makes no sense to attempt to register them all right ;-)");
        }
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(MessageSeeds.values()),
                Arrays.stream(Privileges.values()),
                Arrays.stream(TranslationKeys.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public String getComponentName() {
        return CustomPropertySetService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCustomPropertySet(CustomPropertySet customPropertySet) {
        this.addCustomPropertySet(customPropertySet, false);
    }

    @Override
    public void addSystemCustomPropertySet(CustomPropertySet customPropertySet) {
        this.addSystemCustomPropertySetIfNotAlreadyActive(customPropertySet);
    }

    private void addSystemCustomPropertySetIfNotAlreadyActive(CustomPropertySet customPropertySet) {
        if (!this.isActive(customPropertySet)) {
            this.addCustomPropertySet(customPropertySet, true);
        }
    }

    private boolean isActive(CustomPropertySet customPropertySet) {
        return this.activePropertySets.containsKey(customPropertySet.getId());
    }

    private void addCustomPropertySet(CustomPropertySet customPropertySet, boolean systemDefined) {
        if (this.installed) {
            boolean noPrincipalYet = this.threadPrincipalService.getPrincipal() == null;
            try {
                if (noPrincipalYet) {
                    this.threadPrincipalService.set(this.getPrincipal());
                }
                if (!transactionService.isInTransaction()) {
                    try (TransactionContext ctx = transactionService.getContext()) {
                        this.registerCustomPropertySet(customPropertySet, systemDefined);
                        ctx.commit();
                    }
                } else {
                    this.registerCustomPropertySet(customPropertySet, systemDefined);
                }
            } catch (UnderlyingSQLFailedException | CommitException | IllegalArgumentException | IllegalStateException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            } finally {
                if (noPrincipalYet) {
                    this.threadPrincipalService.clear();
                }
            }
        } else {
            this.publishedPropertySets.put(customPropertySet, systemDefined);
        }
    }

    private Principal getPrincipal() {
        return () -> "Custom Property Set Service";
    }

    private void registerCustomPropertySet(CustomPropertySet customPropertySet, boolean systemDefined) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet =
                this.dataModel
                        .mapper(RegisteredCustomPropertySet.class)
                        .getUnique(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName(), customPropertySet.getId());
        if (registeredCustomPropertySet.isPresent()) {
            // Registered in a previous platform session, likely the system was restarted / redeployed
            DataModel dataModel = this.registerAndInstallDataModel(customPropertySet);
            this.activePropertySets.put(
                    customPropertySet.getId(),
                    new ActiveCustomPropertySet(
                            customPropertySet,
                            this.thesaurus,
                            dataModel,
                            registeredCustomPropertySet.get()));
        } else {
            // First time registration
            DataModel dataModel = this.registerAndInstallOrReuseDataModel(customPropertySet);
            RegisteredCustomPropertySetImpl newRegisteredCustomPropertySet = this.createRegisteredCustomPropertySet(customPropertySet, systemDefined);
            this.activePropertySets.put(
                    customPropertySet.getId(),
                    new ActiveCustomPropertySet(
                            customPropertySet,
                            this.thesaurus,
                            dataModel,
                            newRegisteredCustomPropertySet));
        }
        this.searchService.register(new CustomPropertySetSearchDomainExtension(this, this.activePropertySets.get(customPropertySet.getId())));
    }

    private DataModel registerAndInstallOrReuseDataModel(CustomPropertySet customPropertySet) {
        return this.ormService
                .getDataModels()
                .stream()
                .filter(dataModel -> dataModel.getName().equals(customPropertySet.getPersistenceSupport().componentName()))
                .findAny()
                .orElseGet(() -> this.registerAndInstallDataModel(customPropertySet));
    }

    private DataModel registerAndInstallDataModel(CustomPropertySet customPropertySet) {
        DataModel dataModel = this.newDataModelFor(customPropertySet);
        this.addTableFor(customPropertySet, dataModel);
        dataModel.register(this.getCustomPropertySetModule(dataModel, customPropertySet));
        PersistenceSupport persistenceSupport = customPropertySet.getPersistenceSupport();

        Map<Version, Class<? extends Upgrader>> versionClassMap = dataModel.changeVersions()
                .stream()
                .collect(Collectors.toMap(Function.identity(), version -> CustomPropertySetInstaller.class));

        upgradeService.register(
                InstallIdentifier.identifier(
                        persistenceSupport.application(),
                        persistenceSupport.componentName()),
                dataModel,
                CustomPropertySetInstaller.class,
                versionClassMap);
        return dataModel;
    }

    private static class CustomPropertySetInstaller implements FullInstaller, Upgrader {

        private final DataModel dataModel;

        @Inject
        CustomPropertySetInstaller(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
            dataModelUpgrader.upgrade(dataModel, Version.latest());
        }

        @Override
        public void migrate(DataModelUpgrader dataModelUpgrader) {
            dataModelUpgrader.upgrade(dataModel, Version.latest());
        }
    }

    private DataModel newDataModelFor(CustomPropertySet customPropertySet) {
        return this.ormService.newDataModel(customPropertySet.getPersistenceSupport().componentName(), customPropertySet.getName());
    }

    private void addTableFor(CustomPropertySet customPropertySet, DataModel dataModel) {
        this.newBuilderFor(customPropertySet, dataModel).build();
    }

    private TableBuilder newBuilderFor(CustomPropertySet customPropertySet, DataModel dataModel) {
        if (customPropertySet.isVersioned()) {
            return new VersionedTableBuilder(dataModel, customPropertySet);
        } else {
            return new TableBuilder(dataModel, customPropertySet);
        }
    }

    @SuppressWarnings("unchecked")
    private RegisteredCustomPropertySetImpl createRegisteredCustomPropertySet(CustomPropertySet customPropertySet, boolean systemDefined) {
        RegisteredCustomPropertySetImpl registeredCustomPropertySet = this.dataModel.getInstance(RegisteredCustomPropertySetImpl.class);
        registeredCustomPropertySet.initialize(customPropertySet, systemDefined, customPropertySet.defaultViewPrivileges(), customPropertySet.defaultEditPrivileges());
        registeredCustomPropertySet.create();
        return registeredCustomPropertySet;
    }

    @Override
    public void removeCustomPropertySet(CustomPropertySet customPropertySet) {
        this.activePropertySets.remove(customPropertySet.getId());
    }

    @Override
    public void removeSystemCustomPropertySet(CustomPropertySet customPropertySet) {
        this.removeCustomPropertySet(customPropertySet);
    }

    @Override
    public List<RegisteredCustomPropertySet> findActiveCustomPropertySets() {
        return this.dataModel
                .mapper(RegisteredCustomPropertySetImpl.class)
                .find(RegisteredCustomPropertySetImpl.FieldNames.SYSTEM_DEFINED.javaName(), false)
                .stream()
                .filter(RegisteredCustomPropertySetImpl::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegisteredCustomPropertySet> findActiveCustomPropertySets(Class domainClass) {
        return this.findActiveCustomPropertySets()
                .stream()
                .filter(r -> r.getCustomPropertySet().getDomainClass().equals(domainClass))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CustomPropertySet> findRegisteredCustomPropertySet(String id) {
        return Optional
                .ofNullable(this.activePropertySets.get(id))
                .map(ActiveCustomPropertySet::getCustomPropertySet);
    }

    @Override
    public Optional<RegisteredCustomPropertySet> findActiveCustomPropertySet(String id) {
        return this.dataModel
                .mapper(RegisteredCustomPropertySetImpl.class)
                .find(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName(), id)
                .stream()
                .filter(RegisteredCustomPropertySetImpl::isActive)
                .map(RegisteredCustomPropertySet.class::cast)
                .findAny();
    }

    @Override
    public void cleanupRegisteredButNotActiveCustomPropertySets() {
        this.dataModel
                .stream(RegisteredCustomPropertySetImpl.class)
                .filter(Predicates.not(RegisteredCustomPropertySetImpl::isActive))
                .forEach(RegisteredCustomPropertySetImpl::delete);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getUniqueValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues) {
        return this.toCustomPropertySetValues(customPropertySet, this.getUniqueValuesEntityFor(customPropertySet, businesObject, additionalPrimaryKeyValues), additionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getUniqueValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        return this.toCustomPropertySetValues(customPropertySet, this.getUniqueValuesEntityFor(customPropertySet, businesObject, effectiveTimestamp, additionalPrimaryKeyValues), additionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> boolean hasValueForPropertySpecs(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp, Set<PropertySpec> specs, Object... additionalPrimaryKeyValues) {
        CustomPropertySetValues propertySetValues = this.toCustomPropertySetValues(customPropertySet, this.getUniqueValuesEntityFor(customPropertySet, businesObject, true, effectiveTimestamp, additionalPrimaryKeyValues), additionalPrimaryKeyValues);
        return specs.stream().allMatch(
                propertySpec -> propertySetValues.propertyNames().contains(propertySpec.getName())
        );
    }

    private <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues toCustomPropertySetValues(CustomPropertySet<D, T> customPropertySet, Optional<T> customPropertyValuesEntity, Object... additionalPrimaryKeyValues) {
        CustomPropertySetValues properties;
        if (customPropertyValuesEntity.isPresent()) {
            if (customPropertySet.isVersioned()) {
                Interval interval = DomainExtensionAccessor.getInterval(customPropertyValuesEntity.get());
                properties = CustomPropertySetValues.emptyDuring(interval);
            } else {
                properties = CustomPropertySetValues.empty();
            }
            customPropertyValuesEntity.get().copyTo(properties, additionalPrimaryKeyValues);
        } else {
            properties = CustomPropertySetValues.empty();
        }
        return properties;
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsNotVersioned(customPropertySet, activeCustomPropertySet);
        activeCustomPropertySet.validateCurrentUserIsAllowedToEdit();
        activeCustomPropertySet.setNonVersionedValuesEntityFor(businesObject, values, additionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businessObject, T persistentDomainExtension, Object... additionalPrimaryKeyValues) {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        persistentDomainExtension.copyTo(values);
        setValuesFor(customPropertySet, businessObject, values, additionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businessObject, T persistentDomainExtension, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        persistentDomainExtension.copyTo(values);
        setValuesFor(customPropertySet, businessObject, values, effectiveTimestamp, additionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businessObject, CustomPropertySetValues values, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        activeCustomPropertySet.validateCurrentUserIsAllowedToEdit();
        activeCustomPropertySet.setVersionedValuesEntityFor(businessObject, values, effectiveTimestamp, additionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> Optional<T> getUniqueValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsNotVersioned(customPropertySet, activeCustomPropertySet);
        return activeCustomPropertySet.getNonVersionedValuesEntityFor(businesObject, additionalPrimaryKeyValues);
    }

    private <D, T extends PersistentDomainExtension<D>> void validateCustomPropertySetIsNotVersioned(CustomPropertySet<D, T> customPropertySet, ActiveCustomPropertySet activeCustomPropertySet) {
        if (activeCustomPropertySet.getCustomPropertySet().isVersioned()) {
            throw new UnsupportedOperationException("Custom property set " + customPropertySet.getId() + " is versioned, you need to call CustomPropertySetService#getUniqueValuesFor(CustomPropertySet, Class<Domain>, Instant)");
        }
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> Optional<T> getUniqueValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        return this.getUniqueValuesEntityFor(customPropertySet, businesObject, false, effectiveTimestamp, additionalPrimaryKeyValues);
    }

    private <D, T extends PersistentDomainExtension<D>> Optional<T> getUniqueValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, boolean ignorePrivileges, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        return activeCustomPropertySet.getVersionedValuesEntityFor(businesObject, ignorePrivileges, effectiveTimestamp, additionalPrimaryKeyValues);
    }

    private <D, T extends PersistentDomainExtension<D>> void validateCustomPropertySetIsVersioned(CustomPropertySet<D, T> customPropertySet, ActiveCustomPropertySet activeCustomPropertySet) {
        if (!activeCustomPropertySet.getCustomPropertySet().isVersioned()) {
            throw new UnsupportedOperationException("Custom property set " + customPropertySet.getId() + " is NOT versioned, you need to call CustomPropertySetService#getUniqueValuesFor(CustomPropertySet, Class<Domain>)");
        }
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void validateCustomPropertySetValues(CustomPropertySet<D, T> customPropertySet, CustomPropertySetValues values) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        activeCustomPropertySet.validateValuesEntity(values);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> List<CustomPropertySetValues> getAllVersionedValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues) {
        return this.getAllVersionedValuesEntitiesFor(customPropertySet, businesObject, additionalPrimaryKeyValues)
                .stream()
                .map(Optional::of)
                .map(e -> this.toCustomPropertySetValues(customPropertySet, e, additionalPrimaryKeyValues))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <D> void cleanValuesIntervalFor(ActiveCustomPropertySet activeCustomPropertySet, D businesObject, Range<Instant> newRange, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        OverlapCalculatorBuilder overlapCalculatorBuilder = this.calculateOverlapsFor(activeCustomPropertySet.getCustomPropertySet(), businesObject, additionalPrimaryKeyValues);
        this.cleanValuesIntervalFor(activeCustomPropertySet, businesObject, overlapCalculatorBuilder.whenUpdating(effectiveTimestamp, newRange), additionalPrimaryKeyValues);
    }

    @SuppressWarnings("unchecked")
    private <D> void cleanValuesIntervalFor(ActiveCustomPropertySet activeCustomPropertySet, D businesObject, Range<Instant> newRange, Object... additionalPrimaryKeyValues) {
        OverlapCalculatorBuilder overlapCalculatorBuilder = this.calculateOverlapsFor(activeCustomPropertySet.getCustomPropertySet(), businesObject, additionalPrimaryKeyValues);
        this.cleanValuesIntervalFor(activeCustomPropertySet, businesObject, overlapCalculatorBuilder.whenCreating(newRange), additionalPrimaryKeyValues);
    }

    private <D> void cleanValuesIntervalFor(ActiveCustomPropertySet activeCustomPropertySet, D businesObject, List<ValuesRangeConflict> conflicts, Object... additionalPrimaryKeyValues) {
        for (ValuesRangeConflict conflict : conflicts) {
            Instant currentConflictedIntervalStart;
            if (conflict.getValues().getEffectiveRange().hasLowerBound()) {
                currentConflictedIntervalStart = conflict.getValues().getEffectiveRange().lowerEndpoint();
            } else {
                currentConflictedIntervalStart = Instant.EPOCH;
            }
            switch (conflict.getType()) {
                case RANGE_OVERLAP_DELETE: {
                    activeCustomPropertySet.removeTimeSlicedValues(businesObject, currentConflictedIntervalStart, additionalPrimaryKeyValues);
                    break;
                }
                case RANGE_OVERLAP_UPDATE_END: {
                    activeCustomPropertySet.alignTimeSlicedValues(businesObject, currentConflictedIntervalStart, getAlignedRange(conflict), additionalPrimaryKeyValues);
                    break;
                }
                case RANGE_OVERLAP_UPDATE_START: {
                    activeCustomPropertySet.alignTimeSlicedValues(businesObject, currentConflictedIntervalStart, getAlignedRange(conflict), additionalPrimaryKeyValues);
                    break;
                }
                case RANGE_INSERTED:
                    break;
                case RANGE_GAP_BEFORE:
                    // Intentional fall-through
                case RANGE_GAP_AFTER:
                    // Intentional fall-through
                default: {
                    throw new IllegalStateException("Not expecting conflict type " + conflict.getType());
                }
            }
        }
    }

    private Range<Instant> getAlignedRange(ValuesRangeConflict conflict) {
        Range<Instant> currentRange = conflict.getValues().getEffectiveRange();
        Range<Instant> conflictingRange = conflict.getConflictingRange();
        if (conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END)) {
            if (currentRange.hasLowerBound()) {
                return Range.closedOpen(currentRange.lowerEndpoint(), conflictingRange.lowerEndpoint());
            } else {
                return Range.lessThan(conflictingRange.lowerEndpoint());
            }
        } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START)) {
            if (currentRange.hasUpperBound()) {
                return Range.closedOpen(conflictingRange.upperEndpoint(), currentRange.upperEndpoint());
            } else {
                return Range.atLeast(conflictingRange.upperEndpoint());
            }
        } else {
            return currentRange;
        }
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void setValuesVersionFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values, Range<Instant> newRange, Instant effectiveTimestamp, Object... addtionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        activeCustomPropertySet.validateCurrentUserIsAllowedToEdit();
        this.cleanValuesIntervalFor(activeCustomPropertySet, businesObject, newRange, effectiveTimestamp, addtionalPrimaryKeyValues);
        activeCustomPropertySet.removeTimeSlicedValues(businesObject, effectiveTimestamp, addtionalPrimaryKeyValues);
        Instant startTime = newRange.hasLowerBound() ? newRange.lowerEndpoint() : Instant.EPOCH;
        activeCustomPropertySet.setValuesEntityFor(businesObject, values, startTime, newRange, addtionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void setValuesVersionFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values, Range<Instant> newRange, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        activeCustomPropertySet.validateCurrentUserIsAllowedToEdit();
        this.cleanValuesIntervalFor(activeCustomPropertySet, businesObject, newRange, additionalPrimaryKeyValues);
        Instant startTime = newRange.hasLowerBound() ? newRange.lowerEndpoint() : Instant.EPOCH;
        activeCustomPropertySet.setValuesEntityFor(businesObject, values, startTime, newRange, additionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> OverlapCalculatorBuilder calculateOverlapsFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues) {
        return new OverlapCalculatorBuilderImpl(getAllVersionedValuesFor(customPropertySet, businesObject, additionalPrimaryKeyValues), thesaurus);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> List<T> getAllVersionedValuesEntitiesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        return activeCustomPropertySet.getAllNonVersionedValuesEntitiesFor(businesObject, additionalPrimaryKeyValues);
    }

    private <D, T extends PersistentDomainExtension<D>> ActiveCustomPropertySet findActiveCustomPropertySetOrThrowException(CustomPropertySet<D, T> customPropertySet) {
        return Optional
                .ofNullable(this.activePropertySets.get(customPropertySet.getId()))
                .map(Function.identity())
                .orElseThrow(() -> new IllegalArgumentException("Custom property set " + customPropertySet.getId() + " is not active or not active any longer"));
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void removeValuesFor(CustomPropertySet<D, T> customPropertySet, D businessObject, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        activeCustomPropertySet.deleteExtensions(businessObject, additionalPrimaryKeyValues);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> NonVersionedValuesEntityCustomConditionMatcher<D, T> getNonVersionedValuesEntitiesFor(CustomPropertySet<D, T> customPropertySet) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsNotVersioned(customPropertySet, activeCustomPropertySet);
        return new NonVersionedValuesEntityCustomConditionMatcherImpl<>(activeCustomPropertySet);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> VersionedValuesEntityCustomConditionMatcher<D, T> getVersionedValuesEntitiesFor(CustomPropertySet<D, T> customPropertySet) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        return new VersionedValuesEntityCustomConditionMatcherImpl<>(activeCustomPropertySet);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> SqlFragment getRawValuesSql(CustomPropertySet<D, T> customPropertySet, PropertySpec propertySpec, String alias, D businessObject, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsNotVersioned(customPropertySet, activeCustomPropertySet);
        SqlFragment sqlFragment =
                activeCustomPropertySet
                        .getRawValuesSql(
                                CustomPropertySqlSupport.columnNamesFor(activeCustomPropertySet, propertySpec),
                                businessObject,
                                additionalPrimaryKeyValues);
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT ");
        sqlBuilder.append(CustomPropertySqlSupport.toValueSelectClauseExpression(activeCustomPropertySet, propertySpec));
        sqlBuilder.append(" as ");
        sqlBuilder.append(alias);
        sqlBuilder.append(" from (");
        sqlBuilder.add(sqlFragment);
        sqlBuilder.append(") cps");
        return sqlBuilder;
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> SqlFragment getRawValuesSql(CustomPropertySet<D, T> customPropertySet, PropertySpec propertySpec, String alias, D businessObject, Range<Instant> effectiveInterval, Object... additionalPrimaryKeyValues) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        SqlFragment sqlFragment =
                activeCustomPropertySet
                        .getRawValuesSql(
                                CustomPropertySqlSupport.columnNamesFor(activeCustomPropertySet, propertySpec),
                                businessObject,
                                effectiveInterval,
                                additionalPrimaryKeyValues);
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT ");
        sqlBuilder.append(CustomPropertySqlSupport.toValueSelectClauseExpression(activeCustomPropertySet, propertySpec));
        sqlBuilder.append(" as ");
        sqlBuilder.append(alias);
        sqlBuilder.append(", cps.starttime, cps.endtime from (");
        sqlBuilder.add(sqlFragment);
        sqlBuilder.append(") cps");
        return sqlBuilder;
    }

    @SuppressWarnings("unused") // published as a gogo command
    public void status() {
        List<RegisteredCustomPropertySetImpl> registeredCustomPropertySets = this.dataModel.mapper(RegisteredCustomPropertySetImpl.class).find();
        new Status(registeredCustomPropertySets, this.publishedPropertySets.keySet()).show();
    }

    private class NonVersionedValuesEntityCustomConditionMatcherImpl<DD, TT extends PersistentDomainExtension<DD>> implements NonVersionedValuesEntityCustomConditionMatcher<DD, TT> {
        private final ActiveCustomPropertySet activeCustomPropertySet;

        private NonVersionedValuesEntityCustomConditionMatcherImpl(ActiveCustomPropertySet activeCustomPropertySet) {
            super();
            this.activeCustomPropertySet = activeCustomPropertySet;
        }

        @Override
        public List<TT> matching(Condition condition) {
            return this.activeCustomPropertySet.getNonVersionedValuesEntityFor(condition);
        }

    }

    private class VersionedValuesEntityCustomConditionMatcherImpl<DD, TT extends PersistentDomainExtension<DD>> implements VersionedValuesEntityCustomConditionMatcher<DD, TT> {
        private final ActiveCustomPropertySet activeCustomPropertySet;

        private VersionedValuesEntityCustomConditionMatcherImpl(ActiveCustomPropertySet activeCustomPropertySet) {
            super();
            this.activeCustomPropertySet = activeCustomPropertySet;
        }

        @Override
        public VersionedValuesEntityEffectivityMatcher<DD, TT> matching(Condition condition) {
            return new VersionedValuesEntityEffectivityMatcherImpl<>(this.activeCustomPropertySet, condition);
        }

    }

    private class VersionedValuesEntityEffectivityMatcherImpl<DD, TT extends PersistentDomainExtension<DD>> implements VersionedValuesEntityEffectivityMatcher<DD, TT> {
        private final ActiveCustomPropertySet activeCustomPropertySet;
        private final Condition condition;

        private VersionedValuesEntityEffectivityMatcherImpl(ActiveCustomPropertySet activeCustomPropertySet, Condition condition) {
            super();
            this.activeCustomPropertySet = activeCustomPropertySet;
            this.condition = condition;
        }

        @Override
        public List<TT> andEffectiveAt(Instant effectiveTimestamp) {
            return this.activeCustomPropertySet.getVersionedValuesEntityFor(this.condition, effectiveTimestamp);
        }

    }

    private class TableBuilder {
        private final DataModel dataModel;
        private final CustomPropertySet customPropertySet;
        private Table underConstruction;
        private Column domainReference;
        private Column customPropertySetReference;
        private List<Column> customPrimaryKeyColumns;

        private TableBuilder(DataModel dataModel, CustomPropertySet customPropertySet) {
            super();
            this.dataModel = dataModel;
            this.customPropertySet = customPropertySet;
        }

        Table underConstruction() {
            return underConstruction;
        }

        CustomPropertySet customPropertySet() {
            return customPropertySet;
        }

        Table build() {
            this.initializeUnderConstruction();
            this.addColumns();
            this.addPrimaryKey();
            return this.underConstruction;
        }

        private void addPrimaryKey() {
            this.underConstruction
                    .primaryKey(this.primaryKeyConstraintName(this.customPropertySet))
                    .on(this.primaryKeyColumns())
                    .add();
        }

        private Column[] primaryKeyColumns() {
            List<Column> primaryKeyColumns = new ArrayList<>();
            this.addPrimaryKeyColumnsTo(primaryKeyColumns);
            return primaryKeyColumns.toArray(new Column[primaryKeyColumns.size()]);
        }

        void addPrimaryKeyColumnsTo(List<Column> primaryKeyColumns) {
            primaryKeyColumns.add(this.domainReference);
            primaryKeyColumns.add(this.customPropertySetReference);
            primaryKeyColumns.addAll(this.customPrimaryKeyColumns);
        }

        @SuppressWarnings("unchecked")
        private void initializeUnderConstruction() {
            this.underConstruction =
                    this.dataModel.addTable(
                            this.tableNameFor(this.customPropertySet),
                            this.customPropertySet.getPersistenceSupport().persistenceClass());
            this.underConstruction.map(this.customPropertySet.getPersistenceSupport().persistenceClass());
            this.underConstruction.setJournalTableName(this.customPropertySet().getPersistenceSupport().journalTableName());
        }

        private void addColumns() {
            this.addPrimaryKeyColumns();
            this.underConstruction.addAuditColumns();
            this.customPropertySet.getPersistenceSupport().addCustomPropertyColumnsTo(this.underConstruction, this.customPrimaryKeyColumns);
        }

        @SuppressWarnings("unchecked")
        void addPrimaryKeyColumns() {
            this.domainReference = this.addDomainColumnTo(this.underConstruction, this.customPropertySet);
            this.customPropertySetReference = this.addPropertySetColumnTo(this.underConstruction, this.customPropertySet);
            this.customPrimaryKeyColumns = new ArrayList<>(this.customPropertySet.getPersistenceSupport().addCustomPropertyPrimaryKeyColumnsTo(this.underConstruction));
        }

        private String tableNameFor(CustomPropertySet customPropertySet) {
            return customPropertySet.getPersistenceSupport().tableName();
        }

        /**
         * Adds a column and a foreign key to the specified {@link Table}
         * that references the domain class of the {@link CustomPropertySet}.
         *
         * @param table The Table
         * @param customPropertySet The CustomPropertySet
         * @see CustomPropertySet#getDomainClass()
         */
        private Column addDomainColumnTo(Table table, CustomPropertySet customPropertySet) {
            PersistenceSupport persistenceSupport = customPropertySet.getPersistenceSupport();
            Column domainReference =
                    table
                        .column(persistenceSupport.domainColumnName())
                        .notNull()
                        .number()
                        .conversion(ColumnConversion.NUMBER2LONG)
                        .skipOnUpdate()
                        .add();
            table
                .foreignKey(persistenceSupport.domainForeignKeyName())
                .on(domainReference)
                .references(customPropertySet.getDomainClass())
                .map(persistenceSupport.domainFieldName())
                .add();
            return domainReference;
        }

        /**
         * Adds a column and a foreign key to the specified {@link Table}
         * that references the {@link CustomPropertySet}.
         *
         * @param table The Table
         * @param customPropertySet The CustomPropertySet
         */
        private Column addPropertySetColumnTo(Table table, CustomPropertySet customPropertySet) {
            Column cps = table
                    .column(HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())
                    .notNull()
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .skipOnUpdate()
                    .add();
            table
                .foreignKey(this.customPropertySetForeignKeyName(customPropertySet))
                .on(cps)
                .references(RegisteredCustomPropertySet.class)
                .map(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName())
                .add();
            return cps;
        }

        /**
         * Generates a unique name for the primary key constraint for
         * the table that holds the properties of the CustomPropertySet.
         *
         * @param customPropertySet The CustomPropertySet
         * @return The unique name for the primary key constraint
         */
        private String primaryKeyConstraintName(CustomPropertySet customPropertySet) {
            return "PK_CPS_" + Math.abs(customPropertySet.getId().hashCode());
        }

        /**
         * Generates a unique name for the foreign key from the table
         * that holds the properties of the {@link CustomPropertySet}
         * to that CustomPropertySet.
         *
         * @param customPropertySet The CustomPropertySet
         * @return The unique name for the foreign key constraint
         */
        private String customPropertySetForeignKeyName(CustomPropertySet customPropertySet) {
            return "FK_CPS_" + Math.abs(customPropertySet.getId().hashCode());
        }

    }

    private class VersionedTableBuilder extends TableBuilder {

        private Column effectivityStartColumn;

        private VersionedTableBuilder(DataModel dataModel, CustomPropertySet customPropertySet) {
            super(dataModel, customPropertySet);
        }

        @SuppressWarnings("unchecked")
        @Override
        void addPrimaryKeyColumns() {
            super.addPrimaryKeyColumns();
            List<Column> intervalColumns = this.underConstruction().addIntervalColumns(HardCodedFieldNames.INTERVAL.javaName());
            this.effectivityStartColumn = intervalColumns.get(0);
        }

        @Override
        void addPrimaryKeyColumnsTo(List<Column> primaryKeyColumns) {
            super.addPrimaryKeyColumnsTo(primaryKeyColumns);
            primaryKeyColumns.add(this.effectivityStartColumn);
        }
    }

}