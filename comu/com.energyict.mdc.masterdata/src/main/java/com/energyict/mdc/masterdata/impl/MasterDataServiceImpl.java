package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
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
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

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
    private volatile Environment environment;

    public MasterDataServiceImpl() {
        super();
    }

    @Inject
    public MasterDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, Environment environment) {
        this(ormService, eventService, nlsService, meteringService, mdcReadingTypeUtilService, true, environment);
    }

    public MasterDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, boolean createDefaults, Environment environment) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
        this.setEnvironment(environment);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install(true, createDefaults);
        }
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
    public Finder<RegisterMapping> findAllRegisterMappings() {
        return DefaultFinder.of(RegisterMapping.class, this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public Optional<RegisterMapping> findRegisterMapping(long id) {
        return this.getDataModel().mapper(RegisterMapping.class).getUnique("id", id);
    }

    @Override
    public RegisterMapping newRegisterMapping(String name, ObisCode obisCode, Unit unit, ReadingType readingType, int timeOfUse) {
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
        return this.getDataModel().getInstance(RegisterMappingImpl.class).initialize(name, obisCode, phenomenon, readingType, timeOfUse);
    }

    @Override
    public Optional<RegisterMapping> findRegisterMappingByName(String name) {
        return this.getDataModel().mapper((RegisterMapping.class)).getUnique("name", name);
    }

    @Override
    public Optional<RegisterMapping> findRegisterMappingByReadingType(ReadingType readingType) {
        return this.getDataModel().mapper(RegisterMapping.class).getUnique("readingType", readingType);
    }

    @Override
    public Optional<RegisterMapping> findRegisterMappingByObisCodeAndUnitAndTimeOfUse(ObisCode obisCode, Unit unit, int timeOfUse) {
        List<RegisterMapping> registerMappings = this.getDataModel().query(RegisterMapping.class, Phenomenon.class).
                select(where(RegisterMappingImpl.Fields.OBIS_CODE.fieldName()).isEqualTo(obisCode.toString()).
                        and(where(RegisterMappingImpl.Fields.UNIT.fieldName()).isEqualTo(unit.dbString()).
                                and(where(RegisterMappingImpl.Fields.TIME_OF_USE.fieldName()).isEqualTo(timeOfUse))));
        if (registerMappings.isEmpty()) {
            return Optional.absent();
        }
        else {
            return Optional.of(registerMappings.get(0));
        }
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
        if (group.getRegisterMappings().isEmpty()) {
            throw new RegisterTypesRequiredException();
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "MDC Master data");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
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

    @Reference
    public void setEnvironment (Environment environment) {
        this.environment = environment;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MeteringService.class).toInstance(meteringService);
                bind(MdcReadingTypeUtilService.class).toInstance(mdcReadingTypeUtilService);
                bind(MasterDataService.class).toInstance(MasterDataServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        this.install(true, true);
    }

    private void install(boolean exeuteDdl, boolean createDefaults) {
        new Installer(this.dataModel, this.thesaurus, eventService, this.meteringService, this.mdcReadingTypeUtilService, this).install(exeuteDdl, createDefaults);
    }

}