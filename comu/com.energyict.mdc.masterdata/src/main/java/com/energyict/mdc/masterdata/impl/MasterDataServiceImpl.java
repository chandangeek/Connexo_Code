package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.impl.finders.LoadProfileTypeFinder;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides an implementation for the {@link MasterDataService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:41)
 */
@Component(name = "com.energyict.mdc.masterdata", service = {MasterDataService.class, ReferencePropertySpecFinderProvider.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + MasterDataService.COMPONENTNAME, immediate = true)
public class MasterDataServiceImpl implements MasterDataService, ReferencePropertySpecFinderProvider, InstallService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;

    public MasterDataServiceImpl() {
        super();
    }

    @Inject
    public MasterDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this(ormService, eventService, nlsService, meteringService, mdcReadingTypeUtilService, true);
    }

    public MasterDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, boolean createDefaults) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
        this.activate();
        this.install(true, createDefaults);
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new LoadProfileTypeFinder(this.dataModel));
        return finders;
    }


    @Override
    public LogBookType newLogBookType(String name, ObisCode obisCode) {
        return LogBookTypeImpl.from(this.getDataModel(), name, obisCode);
    }

    @Override
    public Optional<LogBookType> findLogBookType(long id) {
        return this.getDataModel().mapper(LogBookType.class).getUnique("id", id);
    }

    @Override
    public Optional<LogBookType> findLogBookTypeByName(String name) {
        return this.getDataModel().mapper(LogBookType.class).getUnique("name", name);
    }

    @Override
    public Finder<RegisterGroup> findAllRegisterGroups() {
        return DefaultFinder.of(RegisterGroup.class, this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public Optional<RegisterGroup> findRegisterGroup(long id) {
        return this.getDataModel().mapper(RegisterGroup.class).getUnique("id", id);
    }

    @Override
    public RegisterGroup newRegisterGroup(String name) {
        return RegisterGroupImpl.from(this.getDataModel(), name);
    }

    @Override
    public Finder<MeasurementType> findAllMeasurementTypes() {
        return DefaultFinder.of(MeasurementType.class, this.getDataModel());
    }

    @Override
    public Finder<ChannelType> findAllChannelTypes() {
        return DefaultFinder.of(ChannelType.class, this.getDataModel()); // TODO SORT
    }

    @Override
    public Finder<LogBookType> findAllLogBookTypes() {
        return DefaultFinder.of(LogBookType.class, this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public Finder<RegisterType> findAllRegisterTypes() {
        List<RegisterType> registerTypes = this.getDataModel().mapper(RegisterType.class).find(); // Must use in memory sorting because DB is already fixed structure: no change allowed
        Collections.sort(registerTypes, (a,b)->a.getReadingType().getFullAliasName().toLowerCase().compareTo(b.getReadingType().getFullAliasName().toLowerCase()));
        return com.energyict.mdc.common.services.ListPager.of(registerTypes);
    }

    @Override
    public Optional<RegisterType> findRegisterType(long id) {
        return this.getDataModel().mapper(RegisterType.class).getUnique("id", id);
    }

    @Override
    public Optional<ChannelType> findChannelTypeById(long id) {
        return this.getDataModel().mapper(ChannelType.class).getUnique("id", id);
    }

    @Override
    public RegisterType newRegisterType(ReadingType readingType, ObisCode obisCode) {
        return this.getDataModel().getInstance(RegisterTypeImpl.class).initialize(obisCode, readingType);
    }

    @Override
    public ChannelType newChannelType(RegisterType templateMeasurementType, TimeDuration interval, ReadingType readingType) {
        return this.getDataModel().getInstance(ChannelTypeImpl.class).initialize(templateMeasurementType, interval, readingType);
    }

    @Override
    public Optional<MeasurementType> findMeasurementTypeByReadingType(ReadingType readingType) {
        return this.getDataModel().mapper(MeasurementType.class).getUnique("readingType", readingType);
    }

    @Override
    public Optional<ChannelType> findChannelTypeByReadingType(ReadingType readingType) {
        return this.getDataModel().mapper(ChannelType.class).getUnique("readingType", readingType);
    }

    @Override
    public Optional<RegisterType> findRegisterTypeByReadingType(ReadingType readingType) {
        return this.getDataModel().mapper(RegisterType.class).getUnique("readingType", readingType);
    }

    @Override
    public List<LoadProfileType> findAllLoadProfileTypes() {
        return this.getDataModel().mapper(LoadProfileType.class).find();
    }

    @Override
    public LoadProfileType newLoadProfileType(String name, ObisCode obisCode, TimeDuration interval, Collection<RegisterType> registerTypes) {
        return LoadProfileTypeImpl.from(this.getDataModel(), name, obisCode, interval, registerTypes);
    }

    @Override
    public Optional<LoadProfileType> findLoadProfileType(long loadProfileTypeId) {
        return this.getDataModel().mapper(LoadProfileType.class).getUnique("id", loadProfileTypeId);
    }

    @Override
    public List<LoadProfileType> findLoadProfileTypesByName(String name) {
        return this.getDataModel().mapper(LoadProfileType.class).find("name", name);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        try {
            DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "MDC Master data");
            for (TableSpecs tableSpecs : TableSpecs.values()) {
                tableSpecs.addTo(dataModel);
            }
            this.dataModel = dataModel;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MeteringService.class).toInstance(meteringService);
                bind(MdcReadingTypeUtilService.class).toInstance(mdcReadingTypeUtilService);
                bind(MasterDataService.class).toInstance(MasterDataServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        try {
            this.dataModel.register(this.getModule());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void install() {
        this.install(true, true);
    }

    private void install(boolean executeDdl, boolean createDefaults) {
        new Installer(this.dataModel, eventService, this.meteringService, this).install(executeDdl, createDefaults);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "NLS", "MTR");
    }

    @Override
    public Optional<ChannelType> findChannelTypeByTemplateRegisterAndInterval(RegisterType templateRegisterType, TimeDuration interval) {
        return getDataModel()
                    .mapper(ChannelType.class)
                    .getUnique(
                            MeasurementTypeImpl.Fields.TEMPLATE_REGISTER_ID.fieldName(), templateRegisterType.getId(),
                            MeasurementTypeImpl.Fields.INTERVAL.fieldName(), interval);
    }

    @Override
    public List<ChannelType> findChannelTypeByTemplateRegister(RegisterType templateRegisterType) {
        return getDataModel().mapper(ChannelType.class).find("templateRegisterId", templateRegisterType.getId());
    }

}