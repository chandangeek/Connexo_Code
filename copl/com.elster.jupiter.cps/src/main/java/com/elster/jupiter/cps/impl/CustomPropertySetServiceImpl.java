package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
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
import com.elster.jupiter.util.streams.Predicates;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySetService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (13:35)
 */
@Component(name = "com.elster.jupiter.cps", service = {CustomPropertySetService.class, ServerCustomPropertySetService.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + CustomPropertySetService.COMPONENT_NAME)
public class CustomPropertySetServiceImpl implements ServerCustomPropertySetService, InstallService, TranslationKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(CustomPropertySetServiceImpl.class.getName());

    private volatile OrmService ormService;
    private volatile DataModel dataModel;
    private volatile boolean installed = false;
    private volatile Thesaurus thesaurus;
    /**
     * Holds the {@link CustomPropertySet}s that were published on the whiteboard.
     */
    private volatile List<CustomPropertySet> publishedPropertySets = new CopyOnWriteArrayList<>();
    /**
     * Holds the {@link CustomPropertySet} that were taken from the whiteboard,
     * registered if that was not the case yet and then wrapping it in an {@link ActiveCustomPropertySet}.
     * Registereing a CustomPropertySet involves creating a DataModel and a Table for it.
     */
    private volatile List<ActiveCustomPropertySet> activePropertySets = new CopyOnWriteArrayList<>();

    // For OSGi purposes
    public CustomPropertySetServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public CustomPropertySetServiceImpl(OrmService ormService, NlsService nlsService, boolean install, CustomPropertySet... customPropertySets) {
        this();
        this.setOrmService(ormService, install);
        this.setNlsService(nlsService);
        Stream.of(customPropertySets).forEach(this::addCustomPropertySet);
        this.activate();
        if (install) {
            this.install();
        }
    }

    @SuppressWarnings("unused")
    @Reference
    public void setOrmService(OrmService ormService) {
        this.setOrmService(ormService, true);
    }

    private void setOrmService(OrmService ormService, boolean install) {
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

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
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
                customPropertySet.getPersistenceSupport().getModule().ifPresent(customModule -> customModule.configure(this.binder()));
                bind(DataModel.class).toInstance(dataModel);
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
             * on the whiteboard will registere immediately instead
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
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "EVT", "NLS");
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
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
    public void addCustomPropertySet(CustomPropertySet customPropertySet) {
        if (this.installed) {
            this.registerCustomPropertySet(customPropertySet);
        }
        else {
            this.publishedPropertySets.add(customPropertySet);
        }
    }

    private void registerCustomPropertySet(CustomPropertySet customPropertySet) {
        DataModel dataModel = this.ormService.newDataModel(customPropertySet.getId(), customPropertySet.getName());
        this.addTableFor(customPropertySet, dataModel);
        dataModel.register(this.getCustomPropertySetModule(dataModel, customPropertySet));
        dataModel.install(true, true);
        RegisteredCustomPropertySet registeredCustomPropertySet =
                this.dataModel
                    .mapper(RegisteredCustomPropertySet.class)
                    .getUnique(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.getName(), customPropertySet.getId())
                    .map(Function.identity())
                    .orElseGet(() -> this.createRegisteredCustomPropertySet(customPropertySet));
        this.activePropertySets.add(new ActiveCustomPropertySet(customPropertySet, dataModel, registeredCustomPropertySet));
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

    private RegisteredCustomPropertySetImpl createRegisteredCustomPropertySet(CustomPropertySet customPropertySet) {
        RegisteredCustomPropertySetImpl registeredCustomPropertySet = this.dataModel.getInstance(RegisteredCustomPropertySetImpl.class);
        registeredCustomPropertySet.initialize(customPropertySet, customPropertySet.defaultViewPrivileges(), customPropertySet.defaultEditPrivileges());
        registeredCustomPropertySet.save();
        return registeredCustomPropertySet;
    }

    @Override
    public void removeCustomPropertySet(CustomPropertySet customPropertySet) {
        Iterator<ActiveCustomPropertySet> iterator = this.activePropertySets.iterator();
        while (iterator.hasNext()) {
            ActiveCustomPropertySet activeCustomPropertySet = iterator.next();
            if (activeCustomPropertySet.getCustomPropertySet().getId().equals(customPropertySet.getId())) {
                iterator.remove();
            }
        }
    }

    @Override
    public List<RegisteredCustomPropertySet> findActiveCustomPropertySets() {
        return this.dataModel
                .stream(RegisteredCustomPropertySetImpl.class)
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
    public Optional<CustomPropertySet> findActiveCustomPropertySet(String id) {
        return this.activePropertySets
                .stream()
                .filter(acps -> acps.getCustomPropertySet().getId().equals(id))
                .map(ActiveCustomPropertySet::getCustomPropertySet)
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
        CustomPropertySetValues properties = CustomPropertySetValues.empty();
        if (customPropertyValuesEntity.isPresent()) {
            customPropertyValuesEntity.get().copyTo(properties);
            if (customPropertySet.isVersioned()) {
                IntervalExtractor.from(customPropertyValuesEntity.get()).into(properties);
            }
        }
        return properties;
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> Optional<T> getValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject) {
        ActiveCustomPropertySet activeCustomPropertySet = this.activePropertySets
                .stream()
                .filter(activeSet -> activeSet.getCustomPropertySet().getId().equals(customPropertySet.getId()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Custom property set " + customPropertySet.getId() + " is not active or not active any longer"));
        if (activeCustomPropertySet.getCustomPropertySet().isVersioned()) {
            throw new UnsupportedOperationException("Custom property set " + customPropertySet.getId() + " is versioned, you need to call CustomPropertySetService#getPersistentValuesFor(CustomPropertySet, Class<Domain>, Instant)");
        }
        return activeCustomPropertySet.getValuesEntityFor(businesObject);
    }

    @Override
    public <D, T extends PersistentDomainExtension<D>> Optional<T> getValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp) {
        ActiveCustomPropertySet activeCustomPropertySet = this.activePropertySets
                .stream()
                .filter(activeSet -> activeSet.getCustomPropertySet().getId().equals(customPropertySet.getId()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Custom property set " + customPropertySet.getId() + " is not active or not active any longer"));
        if (!activeCustomPropertySet.getCustomPropertySet().isVersioned()) {
            throw new UnsupportedOperationException("Custom property set " + customPropertySet.getId() + " is NOT versioned, you need to call CustomPropertySetService#getPersistentValuesFor(CustomPropertySet, Class<Domain>)");
        }
        return activeCustomPropertySet.getValuesEntityFor(businesObject, effectiveTimestamp);
    }

    private class TableBuilder {
        private final DataModel dataModel;
        private final CustomPropertySet customPropertySet;
        private Table underConstruction;
        private Column domainReference;
        private Column customPropertySetReference;

        private TableBuilder(DataModel dataModel, CustomPropertySet customPropertySet) {
            super();
            this.dataModel = dataModel;
            this.customPropertySet = customPropertySet;
        }

        Table underConstruction() {
            return underConstruction;
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
        }

        private void initializeUnderConstruction() {
            this.underConstruction =
                    this.dataModel.addTable(
                            this.tableNameFor(this.customPropertySet),
                            this.customPropertySet.getPersistenceSupport().getPersistenceClass());
        }

        void addColumns() {
            this.domainReference = this.addDomainColumnTo(this.underConstruction, this.customPropertySet);
            this.customPropertySetReference = this.addPropertySetColumnTo(this.underConstruction, this.customPropertySet);
            this.customPropertySet.getPersistenceSupport().addCustomPropertyColumnsTo(this.underConstruction);
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
                        .map(persistenceSupport.domainFieldName())
                        .number()
                        .conversion(ColumnConversion.NUMBER2LONG)
                        .skipOnUpdate()
                        .add();
            table
                .foreignKey(persistenceSupport.domainForeignKeyName())
                .on(domainReference)
                .references(customPropertySet.getDomainClass())
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
                    .map(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .skipOnUpdate()
                    .add();
            table
                .foreignKey(this.customPropertySetForeignKeyName(customPropertySet))
                .on(cps)
                .references(RegisteredCustomPropertySet.class)
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
            return "PK_CPS_" + customPropertySet.getId().hashCode();
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
            return "FK_CPS_" + customPropertySet.getId().hashCode();
        }

    }

    private class VersionedTableBuilder extends TableBuilder {

        private Column effectivityStartColumn;

        private VersionedTableBuilder(DataModel dataModel, CustomPropertySet customPropertySet) {
            super(dataModel, customPropertySet);
        }

        @SuppressWarnings("unchecked")
        @Override
        void addColumns() {
            super.addColumns();
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