package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.Privileges;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.util.time.Interval;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySetService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (13:35)
 */
@Component(name = "com.elster.jupiter.cps", service = {CustomPropertySetService.class, ServerCustomPropertySetService.class, InstallService.class, TranslationKeyProvider.class, PrivilegesProvider.class}, property = "name=" + CustomPropertySetService.COMPONENT_NAME)
public class CustomPropertySetServiceImpl implements ServerCustomPropertySetService, InstallService, TranslationKeyProvider, PrivilegesProvider {

    private static final Logger LOGGER = Logger.getLogger(CustomPropertySetServiceImpl.class.getName());

    private volatile OrmService ormService;
    private volatile DataModel dataModel;
    private volatile UserService userService;
    private volatile boolean installed = false;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
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
    private List<ActiveCustomPropertySet> activePropertySets = new CopyOnWriteArrayList<>();

    // For OSGi purposes
    public CustomPropertySetServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public CustomPropertySetServiceImpl(OrmService ormService, NlsService nlsService, TransactionService transactionService, UserService userService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setTransactionService(transactionService);
        this.setUserService(userService);
        this.activate();
        this.install();
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

    @Override
    public String getModuleName() {
        return CustomPropertySetService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_CUSTOM_PROPERTIES.getKey(), Privileges.RESOURCE_CUSTOM_PROPERTIES_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTER_PRIVILEGES, Privileges.Constants.VIEW_PRIVILEGES)));
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_CUSTOM_PRIVILEGES.getKey(), Privileges.RESOURCE_CUSTOM_PRIVILEGES_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_CUSTOM_PROPERTIES_1, Privileges.Constants.VIEW_CUSTOM_PROPERTIES_2,
                        Privileges.Constants.VIEW_CUSTOM_PROPERTIES_3, Privileges.Constants.VIEW_CUSTOM_PROPERTIES_4,
                        Privileges.Constants.EDIT_CUSTOM_PROPERTIES_1, Privileges.Constants.EDIT_CUSTOM_PROPERTIES_2,
                        Privileges.Constants.EDIT_CUSTOM_PROPERTIES_3, Privileges.Constants.EDIT_CUSTOM_PROPERTIES_4)));
        return resources;
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
    public void setUserService(UserService userService) {
        this.userService = userService;
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
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        this.installed = this.dataModel.isInstalled();
        this.registerAllCustomPropertySets();
    }

    private <D, T extends PersistentDomainExtension<D>> Module getCustomPropertySetModule(final DataModel dataModel, final CustomPropertySet<D, T> customPropertySet) {
        return new AbstractModule() {
            @Override
            public void configure() {
                customPropertySet.getPersistenceSupport().module().ifPresent(customModule -> customModule.configure(this.binder()));
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(CustomPropertySetService.class).toInstance(CustomPropertySetServiceImpl.this);
            }
        };
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
        }
        else {
            LOGGER.fine("No custom property sets have registered yet, makes no sense to attempt to register them all right ;-)");
        }
    }

    @Override
    public void install() {
        if (!dataModel.isInstalled()) {
            new Installer(this.dataModel).install(true);
            this.installed = this.dataModel.isInstalled();
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "EVT", "NLS");
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(MessageSeeds.values()),
                Arrays.stream(Privileges.values()))
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
        this.addCustomPropertySet(customPropertySet, false, false);
    }

    @Override
    public void addSystemCustomPropertySet(CustomPropertySet customPropertySet) {
        this.addSystemCustomPropertySetIfNotAlreadyActive(customPropertySet);
    }

    private void addSystemCustomPropertySetIfNotAlreadyActive(CustomPropertySet customPropertySet) {
        if (!this.isActive(customPropertySet)) {
            this.addCustomPropertySet(customPropertySet, true, true);
        }
    }

    private boolean isActive(CustomPropertySet customPropertySet) {
        return this.activePropertySets
                    .stream()
                    .anyMatch(this.equalCustomPropertySetIdPredicate(customPropertySet));
    }

    private Predicate<ActiveCustomPropertySet> equalCustomPropertySetIdPredicate(CustomPropertySet customPropertySet) {
        return each -> each.getCustomPropertySet().getId().equals(customPropertySet.getId());
    }

    private void addCustomPropertySet(CustomPropertySet customPropertySet, boolean systemDefined, boolean inTransaction) {
        if (this.installed) {
            if (!inTransaction) {
                try (TransactionContext ctx = transactionService.getContext()) {
                    this.registerCustomPropertySet(customPropertySet, systemDefined);
                    ctx.commit();
                }
            }
            else {
                this.registerCustomPropertySet(customPropertySet, systemDefined);
            }
        }
        else {
            this.publishedPropertySets.put(customPropertySet, systemDefined);
        }
    }

    private void registerCustomPropertySet(CustomPropertySet customPropertySet, boolean systemDefined) {
        DataModel dataModel = this.registerAndInstallOrReuseDataModel(customPropertySet);
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = this.dataModel
                .mapper(RegisteredCustomPropertySet.class)
                .getUnique(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName(), customPropertySet.getId());
        if (registeredCustomPropertySet.isPresent()) {
            /* Pluggable Classes can be registered multiple times
             * with different properties but they will obviously
             * have the same CustomPropertySet. */
            if (!systemDefined) {
                throw new DuplicateCustomPropertySetException(this.thesaurus);
            }
        }
        else {
            RegisteredCustomPropertySetImpl newRegisteredCustomPropertySet = this.createRegisteredCustomPropertySet(customPropertySet, systemDefined);
            this.activePropertySets.add(new ActiveCustomPropertySet(customPropertySet, this.thesaurus, dataModel, newRegisteredCustomPropertySet));
        }
    }

    private DataModel registerAndInstallOrReuseDataModel(CustomPropertySet customPropertySet) {
        return this.ormService
                .getDataModels()
                .stream()
                .filter(dataModel -> this.dataModelHasMatchingTable(dataModel, customPropertySet))
                .findAny()
                .orElseGet(() -> this.registerAndInstallDataModel(customPropertySet));
    }

    private boolean dataModelHasMatchingTable(DataModel dataModel, CustomPropertySet customPropertySet) {
        return dataModel.getTables().stream().anyMatch(table -> table.getName().equals(customPropertySet.getPersistenceSupport().tableName()));
    }

    private DataModel registerAndInstallDataModel(CustomPropertySet customPropertySet) {
        DataModel dataModel = this.ormService.newDataModel(customPropertySet.getPersistenceSupport().componentName(), customPropertySet.getName());
        this.addTableFor(customPropertySet, dataModel);
        dataModel.register(this.getCustomPropertySetModule(dataModel, customPropertySet));
        dataModel.install(true, false);
        return dataModel;
    }

    private void addTableFor(CustomPropertySet customPropertySet, DataModel dataModel) {
        this.newBuilderFor(customPropertySet, dataModel).build();
    }

    private TableBuilder newBuilderFor(CustomPropertySet customPropertySet, DataModel dataModel) {
        if (customPropertySet.isVersioned()) {
            return new VersionedTableBuilder(dataModel, customPropertySet);
        }
        else {
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
        this.activePropertySets.removeIf(this.equalCustomPropertySetIdPredicate(customPropertySet));
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
        return this.activePropertySets
                .stream()
                .filter(acps -> acps.getCustomPropertySet().getId().equals(id))
                .map(ActiveCustomPropertySet::getCustomPropertySet)
                .findAny();
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
    public <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject) {
        return this.toCustomPropertySetValues(customPropertySet, this.getValuesEntityFor(customPropertySet, businesObject));
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp) {
        return this.toCustomPropertySetValues(customPropertySet, this.getValuesEntityFor(customPropertySet, businesObject, effectiveTimestamp));
    }

    private <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues toCustomPropertySetValues(CustomPropertySet<D, T> customPropertySet, Optional<T> customPropertyValuesEntity) {
        CustomPropertySetValues properties;
        if (customPropertyValuesEntity.isPresent()) {
            if (customPropertySet.isVersioned()) {
                Interval interval = DomainExtensionAccessor.getInterval(customPropertyValuesEntity.get());
                properties = CustomPropertySetValues.emptyDuring(interval);
            }
            else {
                properties = CustomPropertySetValues.empty();
            }
            customPropertyValuesEntity.get().copyTo(properties);
        }
        else {
            properties = CustomPropertySetValues.empty();
        }
        return properties;
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsNotVersioned(customPropertySet, activeCustomPropertySet);
        activeCustomPropertySet.validateCurrentUserIsAllowedToEdit();
        activeCustomPropertySet.setValuesEntityFor(businesObject, values);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values, Instant effectiveTimestamp) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        activeCustomPropertySet.validateCurrentUserIsAllowedToEdit();
        activeCustomPropertySet.setValuesEntityFor(businesObject, values, effectiveTimestamp);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> Optional<T> getValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsNotVersioned(customPropertySet, activeCustomPropertySet);
        return activeCustomPropertySet.getValuesEntityFor(businesObject);
    }

    private <D, T extends PersistentDomainExtension<D>> void validateCustomPropertySetIsNotVersioned(CustomPropertySet<D, T> customPropertySet, ActiveCustomPropertySet activeCustomPropertySet) {
        if (activeCustomPropertySet.getCustomPropertySet().isVersioned()) {
            throw new UnsupportedOperationException("Custom property set " + customPropertySet.getId() + " is versioned, you need to call CustomPropertySetService#getPersistentValuesFor(CustomPropertySet, Class<Domain>, Instant)");
        }
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> Optional<T> getValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        this.validateCustomPropertySetIsVersioned(customPropertySet, activeCustomPropertySet);
        return activeCustomPropertySet.getValuesEntityFor(businesObject, effectiveTimestamp);
    }

    private <D, T extends PersistentDomainExtension<D>> void validateCustomPropertySetIsVersioned(CustomPropertySet<D, T> customPropertySet, ActiveCustomPropertySet activeCustomPropertySet) {
        if (!activeCustomPropertySet.getCustomPropertySet().isVersioned()) {
            throw new UnsupportedOperationException("Custom property set " + customPropertySet.getId() + " is NOT versioned, you need to call CustomPropertySetService#getPersistentValuesFor(CustomPropertySet, Class<Domain>)");
        }
    }

    private <D, T extends PersistentDomainExtension<D>> ActiveCustomPropertySet findActiveCustomPropertySetOrThrowException(CustomPropertySet<D, T> customPropertySet) {
        return this.activePropertySets
                .stream()
                .filter(this.equalCustomPropertySetIdPredicate(customPropertySet))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Custom property set " + customPropertySet.getId() + " is not active or not active any longer"));
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> void removeValuesFor(CustomPropertySet<D, T> customPropertySet, D businessObject) {
        ActiveCustomPropertySet activeCustomPropertySet = this.findActiveCustomPropertySetOrThrowException(customPropertySet);
        activeCustomPropertySet.deleteExtensions(businessObject);
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
        void initializeUnderConstruction() {
            this.underConstruction =
                    this.dataModel.addTable(
                            this.tableNameFor(this.customPropertySet),
                            this.customPropertySet.getPersistenceSupport().persistenceClass());
            this.underConstruction.map(this.customPropertySet.getPersistenceSupport().persistenceClass());
        }

        private void addColumns() {
            this.addPrimaryKeyColumns();
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
        void initializeUnderConstruction() {
            super.initializeUnderConstruction();
            this.underConstruction().setJournalTableName(this.customPropertySet().getPersistenceSupport().tableName() + "JRNL");
        }

        @Override
        void addPrimaryKeyColumnsTo(List<Column> primaryKeyColumns) {
            super.addPrimaryKeyColumnsTo(primaryKeyColumns);
            primaryKeyColumns.add(this.effectivityStartColumn);
        }
    }
}