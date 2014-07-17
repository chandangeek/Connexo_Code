package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.RegisterTypesRequiredException;
import com.energyict.mdc.masterdata.exceptions.UnitHasNoMatchingPhenomenonException;
import com.energyict.mdc.masterdata.impl.finders.LoadProfileTypeFinder;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link MasterDataService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:41)
 */
@Component(name="com.energyict.mdc.masterdata", service = {MasterDataService.class, ReferencePropertySpecFinderProvider.class, InstallService.class}, property = "name=" + MasterDataService.COMPONENTNAME, immediate = true)
public class MasterDataServiceImpl implements MasterDataService, ReferencePropertySpecFinderProvider, InstallService {

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
    public List<Phenomenon> findAllPhenomena() {
        return this.getDataModel().mapper(Phenomenon.class).find();
    }

    @Override
    public List<LogBookType> findAllLogBookTypes() {
        return this.getDataModel().mapper(LogBookType.class).find();
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
    public Optional<Phenomenon> findPhenomenon(int phenomenonId) {
        return this.getDataModel().mapper(Phenomenon.class).getUnique("id", phenomenonId);
    }

    @Override
    public Phenomenon newPhenomenon(String name, Unit unit) {
        return this.getDataModel().getInstance(PhenomenonImpl.class).initialize(name, unit);
    }

    @Override
    public Optional<Phenomenon> findPhenomenonByNameAndUnit(String name, Unit unit) {
        return this.getDataModel().mapper(Phenomenon.class).
                getUnique(
                        PhenomenonImpl.Fields.NAME.fieldName(), name,
                        PhenomenonImpl.Fields.UNIT.fieldName(), unit.dbString());
    }

    @Override
    public Optional<Phenomenon> findPhenomenonByUnit(Unit unit) {
        return this.getDataModel().mapper(Phenomenon.class).getUnique(PhenomenonImpl.Fields.UNIT.fieldName(), unit.dbString());
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
        return DefaultFinder.of(MeasurementType.class, this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public Finder<ChannelType> findAllChannelTypes() {
        return DefaultFinder.of(ChannelType.class, this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public Finder<RegisterType> findAllRegisterTypes() {
        return DefaultFinder.of(RegisterType.class, this.getDataModel()).defaultSortColumn("lower(name)");
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
    public RegisterType newRegisterType(String name, ObisCode obisCode, Unit unit, ReadingType readingType, int timeOfUse) {
        Phenomenon phenomenon = null;
        if (unit != null) {
            Optional<Phenomenon> xPhenomenon = this.findPhenomenonByUnit(unit);
            if (!xPhenomenon.isPresent()) {
                throw new UnitHasNoMatchingPhenomenonException(unit);
            }
            else {
                phenomenon = xPhenomenon.get();
            }
        }
        return this.getDataModel().getInstance(RegisterTypeImpl.class).initialize(name, obisCode, phenomenon, readingType, timeOfUse);
    }

    @Override
    public ChannelType newChannelType(RegisterType templateMeasurementType, TimeDuration interval, ReadingType readingType) {
        return this.getDataModel().getInstance(ChannelTypeImpl.class).initialize(templateMeasurementType, interval, readingType);
    }

    @Override
    public Optional<MeasurementType> findMeasurementTypeByName(String name) {
        return this.getDataModel().mapper((MeasurementType.class)).getUnique("name", name);
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
    public LoadProfileType newLoadProfileType(String name, ObisCode obisCode, TimeDuration interval) {
        return LoadProfileTypeImpl.from(this.getDataModel(), name, obisCode, interval);
    }

    @Override
    public Optional<LoadProfileType> findLoadProfileType(long loadProfileTypeId) {
        return this.getDataModel().mapper(LoadProfileType.class).getUnique("id", loadProfileTypeId);
    }

    @Override
    public List<LoadProfileType> findLoadProfileTypesByName(String name) {
        return this.getDataModel().mapper(LoadProfileType.class).find("name", name);
    }

    @Override
    public void validateRegisterGroup(RegisterGroup group) {
        if (group.getRegisterTypes().isEmpty()) {
            throw new RegisterTypesRequiredException();
        }
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

    private void install(boolean exeuteDdl, boolean createDefaults) {
        new Installer(this.dataModel, this.thesaurus, eventService, this.meteringService, this.mdcReadingTypeUtilService, this).install(exeuteDdl, createDefaults);
    }

    @Override
    public Optional<ChannelType> findChannelTypeByTemplateRegisterAndInterval(RegisterType templateRegisterType, TimeDuration interval) {
        return getDataModel().mapper(ChannelType.class).getUnique(MeasurementTypeImpl.Fields.TEMPLATE_REGISTER_ID.fieldName(), templateRegisterType.getId(),
                MeasurementTypeImpl.Fields.INTERVAl.fieldName(), interval);
    }
}