package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.cpo.AuditTrail;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DuplicateException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdObjectShadow;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.ShadowList;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.Phenomenon;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.DeviceTypeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdw.amr.RegisterSpecFactory;
import com.energyict.mdw.core.ChannelSpecFactory;
import com.energyict.mdw.core.DeviceCollectionMethodType;
import com.energyict.mdw.core.EndDevice;
import com.energyict.mdw.core.LoadProfileSpecFactory;
import com.energyict.mdw.core.LogBookSpecFactory;
import com.energyict.mdw.interfacing.mdc.MdcInterface;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.ChannelSpecShadow;
import com.energyict.mdw.shadow.DeviceConfigurationShadow;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.mdw.shadow.LoadProfileShadow;
import com.energyict.mdw.shadow.LoadProfileSpecShadow;
import com.energyict.mdw.shadow.LogBookShadow;
import com.energyict.mdw.shadow.LogBookSpecShadow;
import com.energyict.mdw.shadow.amr.RegisterShadow;
import com.energyict.mdw.shadow.amr.RegisterSpecShadow;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: gde
 * Date: 5/11/12
 */
public class DeviceConfigurationImpl extends PersistentNamedObject<DeviceConfiguration> implements DeviceConfiguration, ServerDeviceConfiguration {

    private String description;
    private boolean active;

    private Device prototype;
    private DeviceType deviceType;
    private List<RegisterSpec> registerSpecs = new ArrayList<>();
    private List<ChannelSpec> channelSpecs = new ArrayList<>();
    private List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>();
    private List<LogBookSpec> logBookSpecs = new ArrayList<>();
    private DeviceCommunicationConfiguration communicationConfiguration;
    private Date modificationDate;
    private Clock clock;

    @Inject
    protected DeviceConfigurationImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super(DeviceConfiguration.class, dataModel, eventService, thesaurus);
        this.clock = clock;

        // TODO See if it is required to inject Providers/Builders for the specs
    }


    @Override
    public DeviceCommunicationConfiguration getCommunicationConfiguration() {
        if (DeviceCollectionMethodType.COMSERVER.equals(this.getDeviceType().getDeviceCollectionMethodType())) {
            if (this.communicationConfiguration == null) {
                if (this.getMdcInterface() != null) {
                    this.communicationConfiguration = this.getMdcInterface().findDeviceCommunicationConfigurationFor(this);
                }
            }
        }
        return this.communicationConfiguration;
    }

    public String getDescription() {
        return this.description;
    }

    public int getPrototypeId() {
        return getPrototypeDevice() == null ? 0 : getPrototypeDevice().getId();
    }

    public Device getPrototypeDevice() {
        return this.prototype;
    }

    @Override
    public void validateNewDeviceShadow(DeviceShadow shadow) throws BusinessException {
        if (shadow.getDeviceConfigId() == getId()) {
            validateShadows(shadow.getChannelShadows(), getChannelSpecs(), new ShadowSpecIdProvider<ChannelShadow>() {
                @Override
                public int getSpecID(ChannelShadow shadow) {
                    return shadow.getChannelSpecId();
                }
            }, "channel");
            validateShadows(shadow.getRtuRegisterShadows(), getRegisterSpecs(), new ShadowSpecIdProvider<RegisterShadow>() {
                @Override
                public int getSpecID(RegisterShadow shadow) {
                    return shadow.getRtuRegisterSpecId();
                }
            }, "register");
            validateShadows(shadow.getLoadProfiles(), getLoadProfileSpecs(), new ShadowSpecIdProvider<LoadProfileShadow>() {
                @Override
                public int getSpecID(LoadProfileShadow shadow) {
                    return shadow.getLoadProfileSpecId();
                }
            }, "loadprofile");
            validateShadows(shadow.getLogBooks(), getLogBookSpecs(), new ShadowSpecIdProvider<LogBookShadow>() {
                @Override
                public int getSpecID(LogBookShadow shadow) {
                    return shadow.getLogBookSpecId();
                }
            }, "logbook");
        }
    }

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        return NameIsRequiredException.deviceConfigurationNameIsRequired(this.thesaurus);
    }

    @Override
    public void notifyDelete() {
        //TODO
    }

    private interface ShadowSpecIdProvider<T extends IdObjectShadow> {

        public int getSpecID(T shadow);
    }

    private <T extends IdObjectShadow> void validateShadows(ShadowList<T> shadows, List<? extends IdBusinessObject> specs, ShadowSpecIdProvider<T> specIdProvider, String objectType) throws BusinessException {
        for (IdBusinessObject spec : specs) {
            T shadowForSpec = findShadowForSpec(spec, shadows, specIdProvider);
            if (shadowForSpec == null) {
                throw new BusinessException("invalidDeviceShadowForSpecXMissingY", "Invalid deviceShadow for config '{0}' : missing '{1}Shadow'", getName(), objectType);
            }
        }
        checkNumberOfShadows(shadows, specs.size(), objectType);
    }

    private <T extends IdObjectShadow> T findShadowForSpec(IdBusinessObject spec, ShadowList<T> shadows, ShadowSpecIdProvider<T> specIdProvider) {
        for (T shadow : shadows) {
            if (specIdProvider.getSpecID(shadow) == spec.getId()) {
                return shadow;
            }
        }
        return null;
    }

    private void checkNumberOfShadows(ShadowList shadowList, int expectedSize, String shadowName) throws BusinessException {
        if (expectedSize != shadowList.size()) {
            throw new BusinessException("invalidDeviceShadowForSpecXWrongYShadowCount", "Invalid deviceShadow for config '{0}': wrong number of {1} shadows", getName(), shadowName);
        }
    }

    protected void doInit(DeviceConfigurationShadow shadow) throws SQLException, BusinessException {
        copyNew(shadow);
        validateNew(shadow);
        postNew();
        if (DeviceCollectionMethodType.COMSERVER.equals(getDeviceType().getDeviceCollectionMethodType())) {
            // Will be null during import process since a separate Command needs to process that
            if (shadow.getCommunicationConfigurationShadow() != null) {
                this.createCommunicationConfiguration(shadow);
            }
        }

        createLoadProfileSpecs(shadow.getLoadProfileSpecShadows());
        createChannelSpecs(shadow.getChannelSpecShadows().getNewShadows());
        createLogBookSpecs(shadow.getLogBookSpecShadows());
        createRegisterSpecs(shadow.getRegisterSpecShadows().getNewShadows());
        created();
    }

    private void createCommunicationConfiguration(DeviceConfigurationShadow shadow) throws SQLException, BusinessException {
        shadow.getCommunicationConfigurationShadow().setDeviceConfigurationId(this.getId());
        MdcInterface mdcInterface = this.getMdcInterface();
        if (mdcInterface != null) {
            this.communicationConfiguration = mdcInterface.createDeviceCommunicationConfiguration(this, shadow.getCommunicationConfigurationShadow());
        } else {
            throw new BusinessException("mdcInterfaceIsMissingForCommunicationConfigurationDetails", "The MDC interface is missing and required to create the communication configuration details");
        }
    }

    private void createLoadProfileSpecs(ShadowList<LoadProfileSpecShadow> loadProfileSpecShadows) throws BusinessException, SQLException {
        LoadProfileSpecFactory factory = getLoadProfileSpecFactory();
        List<LoadProfileSpec> specs = new ArrayList<>();
        for (LoadProfileSpecShadow shadow : loadProfileSpecShadows.getNewShadows()) {
            shadow.setDeviceConfigId(getId());
            specs.add(factory.create(shadow));
        }
        loadProfileSpecs = specs;
    }

    private void createLogBookSpecs(ShadowList<LogBookSpecShadow> logBookSpecShadows) throws BusinessException, SQLException {
        LogBookSpecFactory factory = getLogBookSpecFactory();
        for (LogBookSpecShadow logBookSpecShadow : logBookSpecShadows.getNewShadows()) {
            logBookSpecShadow.setDeviceConfigId(getId());
            factory.create(logBookSpecShadow);
        }
        logBookSpecs = null;
    }

    protected void validateNew(DeviceConfigurationShadow shadow) throws BusinessException {
        validate(shadow);
        validateConstraint(shadow);
    }

    @Override
    public void validateUpdate(DeviceConfigurationShadow shadow) throws BusinessException {
        if (!shadow.getName().equals(getName())) {
            validateConstraint(shadow);
        }
        validate(shadow);
    }

    protected void validate(DeviceConfigurationShadow shadow) throws BusinessException {
        if (getActive() && !shadow.getRegisterSpecShadows().getDeletedShadows().isEmpty()) {
            throw new BusinessException("deleteRegisterSpecsFromActiveDeviceConfigIsNotAllowed",
                    "It's not allowed to delete register specifications of an active device configuration");
        }
        if (getActive() && !shadow.getLoadProfileSpecShadows().getDeletedShadows().isEmpty()) {
            throw new BusinessException("deleteLoadProfileSpecsFromActiveDeviceConfigIsNotAllowed",
                    "It's not allowed to delete load profile specifications of an active device configuration");
        }
        if (getActive() && !shadow.getLogBookSpecShadows().getDeletedShadows().isEmpty()) {
            throw new BusinessException("deleteLogBookSpecsFromActiveDeviceConfigIsNotAllowed",
                    "It's not allowed to delete logbook specifications of an active device configuration");
        }
        if (getActive() && !shadow.getChannelSpecShadows().getDeletedShadows().isEmpty()) {
            throw new BusinessException("deleteChannelSpecsFromActiveDeviceConfigIsNotAllowed",
                    "It's not allowed to delete channel specifications of an active device configuration");
        }
        validateLoadProfileSpecs(shadow);
        validateLogBookSpecs(shadow);
        validateRegisterSpecs(shadow);
        validateChannelSpecs(shadow);
    }

    private void validateChannelSpecs(DeviceConfigurationShadow shadow) throws BusinessException {
        List<ChannelSpecShadow> channelSpecShadowsTocheck = shadow.getChannelSpecShadows();
        Map<Integer, Set<String>> loadProfileCSobisCodeSet = new HashMap<>();
        for (ChannelSpecShadow each : channelSpecShadowsTocheck) {
            ObisCode obisCode = findObisCodeFromChannelSpecShadow(each);
            final String obisCodeValue = obisCode.getValue();
            Set<String> channelSpecObisCodeSet = loadProfileCSobisCodeSet.get(each.getLoadProfileTypeId());
            if (channelSpecObisCodeSet == null) {
                channelSpecObisCodeSet = new HashSet<>();
                loadProfileCSobisCodeSet.put(each.getLoadProfileTypeId(), channelSpecObisCodeSet);
            }
            if (!channelSpecObisCodeSet.contains(obisCodeValue)) {
                channelSpecObisCodeSet.add(obisCodeValue);
            } else {
                if (!obisCode.anyChannel()) {
                    throwExistingObisForChannelSpecError(obisCodeValue);
                }
            }
        }
    }

    private void validateLoadProfileSpecs(DeviceConfigurationShadow shadow) throws BusinessException {
        List<LoadProfileSpecShadow> loadProfileSpecShadowsToCheck = new ArrayList<>();
        loadProfileSpecShadowsToCheck.addAll(shadow.getLoadProfileSpecShadows().getNewShadows());
        loadProfileSpecShadowsToCheck.addAll(shadow.getLoadProfileSpecShadows().getUpdatedShadows());
        Set<Integer> referencedLoadProfileTypeIds = new HashSet<>();
        for (LoadProfileSpecShadow each : loadProfileSpecShadowsToCheck) {
            if (referencedLoadProfileTypeIds.contains(each.getLoadProfileTypeId())) {
                throw new BusinessException("onlyOneLoadProfileSpecPerLoadProfileTypeAllowed",
                        "It's not allowed for a device configuration to have multiple load profile specifications referencing the same load profile type");
            }
            referencedLoadProfileTypeIds.add(each.getLoadProfileTypeId());
        }

        loadProfileSpecShadowsToCheck = shadow.getLoadProfileSpecShadows();
        Set<String> LPobisCodeSet = new HashSet<>();
        for (LoadProfileSpecShadow each : loadProfileSpecShadowsToCheck) {
            final String obisCodeValue = findObisCodeFromLoadProfileSpecShadow(each).getValue();
            if (!LPobisCodeSet.contains(obisCodeValue)) {
                LPobisCodeSet.add(obisCodeValue);
            } else {
                throwExistingObisForLoadProfileSpecError(obisCodeValue);
            }
        }
    }

    private void validateRegisterSpecs(DeviceConfigurationShadow shadow) throws BusinessException {
        List<RegisterSpecShadow> registerSpecShadowsToCheck = shadow.getRegisterSpecShadows();
        Set<String> obisCodeSet = new HashSet<>();
        for (RegisterSpecShadow each : registerSpecShadowsToCheck) {
            final String obisCodeValue = findObisCodeFromRegisterSpecShadow(each).getValue();
            if (!obisCodeSet.contains(obisCodeValue)) {
                obisCodeSet.add(obisCodeValue);
            } else {
                throwExistingObisForRegisterSpecError(obisCodeValue);
            }
        }
    }

    private void validateLogBookSpecs(DeviceConfigurationShadow shadow) throws BusinessException {
        List<LogBookSpecShadow> logBookSpecShadowsToCheck = new ArrayList<>();
        logBookSpecShadowsToCheck.addAll(shadow.getLogBookSpecShadows().getNewShadows());
        logBookSpecShadowsToCheck.addAll(shadow.getLogBookSpecShadows().getUpdatedShadows());
        Set<Integer> referencedLogBookTypeIds = new HashSet<>();
        for (LogBookSpecShadow each : logBookSpecShadowsToCheck) {
            if (referencedLogBookTypeIds.contains(each.getLogBookTypeId())) {
                throw new BusinessException("onlyOneLogBookSpecPerLogBookTypeAllowed",
                        "It's not allowed for a device configuration to have multiple logbook specifications referencing the same logbook type");
            }
            referencedLogBookTypeIds.add(each.getLogBookTypeId());
        }

        logBookSpecShadowsToCheck = shadow.getLogBookSpecShadows();
        Map<String, String> obisCodeAndNameMap = new HashMap<>();
        for (LogBookSpecShadow eachLogBookSpecShadow : logBookSpecShadowsToCheck) {
            LogBookType logBookType = findLogBookType(eachLogBookSpecShadow);
            final String obisCodeValue = logBookType.getObisCode().getValue();
            final String logBookSpecName = logBookType.getName();
            if (!obisCodeAndNameMap.containsKey(obisCodeValue)) {
                obisCodeAndNameMap.put(obisCodeValue, logBookSpecName);
            } else {
                throwExistingObisError(obisCodeValue, logBookSpecName, obisCodeAndNameMap.get(obisCodeValue));
            }
        }
    }

    private void throwExistingObisForLoadProfileSpecError(String obisCodeValue) throws BusinessException {
        throw new BusinessException("noDuplicateObiscodesAllowedForLoadProfile",
                "It's not allowed to have load profile specifications with the same obis code {0} for device config {1}",
                obisCodeValue, this.getName());
    }

    private void throwExistingObisError(String obisCodeValue, String logBookSpecName, String otherLogBookSpecName) throws BusinessException {
        throw new BusinessException("noDuplicateObiscodesAllowed",
                "It's not allowed to have logbook specifications with the same obis code '{0}', types: {1} and {2} for device config {3}",
                obisCodeValue, logBookSpecName, otherLogBookSpecName, this.getName());
    }

    private void throwExistingObisForRegisterSpecError(String obisCodeValue) throws BusinessException {
        throw new BusinessException("noDuplicateObiscodesAllowedForRegisterSpec",
                "It's not allowed to have device register specifications with the same obis code '{0}' for device config '{1}'",
                obisCodeValue, this.getName());
    }

    private void throwExistingObisForChannelSpecError(String obisCodeValue) throws BusinessException {
        throw new BusinessException("noDuplicateObiscodesAllowedForChannelSpec",
                "It's not allowed to have channel specifications with the same obis code '{0}' for device config '{1}'",
                obisCodeValue, this.getName());
    }

    protected void validateConstraint(DeviceConfigurationShadow shadow) throws DuplicateException {
        String name = shadow.getName();
        DeviceType deviceType = getRtuTypeFactory().find(shadow.getDeviceTypeId());

        if (getDeviceConfigFactory().hasForNameAndDeviceType(name, deviceType)) {
            throw new DuplicateException("duplicateDeviceConfigXForDeviceTypeY",
                    "A device configuration with the name '{0}' already exists for device type '{1}'",
                    name, deviceType.getName());
        }
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public DeviceConfigurationShadow getShadow() {
        DeviceConfigurationShadow deviceConfigurationShadow = new DeviceConfigurationShadow(this);
        deviceConfigurationShadow.setDeviceCommunicationConfigurationConstructionValidation(new DeviceCommunicationConfigurationConstructionValidationImpl());
        return deviceConfigurationShadow;
    }

    protected void doUpdate(DeviceConfigurationShadow shadow) throws BusinessException, SQLException {
        DeviceCommunicationConfiguration communicationConfiguration = this.getCommunicationConfiguration();
        if (communicationConfiguration != null) {
            communicationConfiguration.update(shadow.getCommunicationConfigurationShadow());
        } else if (shadow.getCommunicationConfigurationShadow() != null && shadow.getCommunicationConfigurationShadow().isDirty()) {
            createCommunicationConfiguration(shadow);
        }
        validateUpdate(shadow);
        copyUpdate(shadow);
        post();

        handleLoadProfilesChannelsAndRegisters(shadow);
        updateLogBookSpecs(shadow.getLogBookSpecShadows());
        this.communicationConfiguration = null;
        updated();
    }

    private void updateLogBookSpecs(ShadowList<LogBookSpecShadow> specShadows) throws BusinessException, SQLException {
        if (!specShadows.isDirty()) {
            return;
        }
        // 1. Delete
        LogBookSpecFactory factory = getLogBookSpecFactory();
        for (LogBookSpecShadow specShadow : specShadows.getDeletedShadows()) {
            LogBookSpec target = factory.find(specShadow.getId());
            if (target != null) {
                target.delete();
            }
        }
        // 2. Update
        for (LogBookSpecShadow specShadow : specShadows.getUpdatedShadows()) {
            LogBookSpec target = factory.find(specShadow.getId());
            if (target != null) {
                target.update(specShadow);
            }
        }
        // 3. Insert of new ones
        for (LogBookSpecShadow specShadow : specShadows.getNewShadows()) {
            specShadow.setDeviceConfigId(getId());
            factory.create(specShadow);
        }
        logBookSpecs = null;
    }

    private void createRegisterSpecs(List<RegisterSpecShadow> specsTocreate) throws SQLException, BusinessException {
        RegisterSpecFactory factory = getRtuRegisterSpecFactory();
        for (RegisterSpecShadow specShadow : specsTocreate) {
            specShadow.setDeviceConfigId(getId());
            // Make sure the linked channels are the newly created ones:
            if (specShadow.getLinkedChannelSpecShadow() != null) {
                for (ChannelSpec channelSpec : getChannelSpecs()) {
                    if (channelSpec.getName().equals(specShadow.getLinkedChannelSpecShadow().getName())) {
                        specShadow.setLinkedChannelSpecId(channelSpec.getId());
                        break;
                    }
                }
            }
            factory.create(specShadow);
        }
        registerSpecs = null;
    }

    private void updateRegisterSpecs(List<RegisterSpecShadow> specsToUpdate) throws SQLException, BusinessException {
        // 2. Update
        RegisterSpecFactory factory = getRtuRegisterSpecFactory();
        for (RegisterSpecShadow specShadow : specsToUpdate) {
            RegisterSpec target = factory.find(specShadow.getId());
            if (target != null) {
                target.update(specShadow);
            }
        }
        registerSpecs = null;
    }

    private void deleteRegisterSpecs(List<RegisterSpecShadow> specsTodelete) throws BusinessException, SQLException {
        RegisterSpecFactory factory = getRtuRegisterSpecFactory();
        for (RegisterSpecShadow specShadow : specsTodelete) {
            RegisterSpec target = factory.find(specShadow.getId());
            if (target != null) {
                target.delete();
            }
        }
        registerSpecs = null;
    }

    private List<RegisterSpecShadow> getRegisterSpecUpdatesToChannelSpecs(List<RegisterSpecShadow> registerShadows, List<ChannelSpecShadow> channelSpecs) {
        RegisterSpecFactory factory = getRtuRegisterSpecFactory();
        List<RegisterSpecShadow> result = new ArrayList<>();
        for (RegisterSpecShadow registerShadow : registerShadows) {
            RegisterSpec spec = factory.find(registerShadow.getId());
            if (idInShadowList(spec.getLinkedChannelSpecId(), channelSpecs)) {
                result.add(registerShadow);
            }
        }
        return result;
    }

    private boolean idInShadowList(int idToFind, List<ChannelSpecShadow> channelSpecs) {
        for (ChannelSpecShadow channelSpec : channelSpecs) {
            if (channelSpec.getId() == idToFind) {
                return true;
            }
        }
        return false;
    }

    private void handleLoadProfilesChannelsAndRegisters(DeviceConfigurationShadow shadow) throws BusinessException, SQLException {
        // Rem since register spec refer to channels specs, we need first to :
        deleteRegisterSpecs(shadow.getRegisterSpecShadows().getDeletedShadows());
        List<RegisterSpecShadow> registerSpecShadowsToUpdate = shadow.getRegisterSpecShadows().getUpdatedShadows();
        List<RegisterSpecShadow> registerSpecShadowsToDeletedChannelSpecs = getRegisterSpecUpdatesToChannelSpecs(registerSpecShadowsToUpdate, shadow.getChannelSpecShadows().getDeletedShadows());
        updateRegisterSpecs(registerSpecShadowsToDeletedChannelSpecs);
        registerSpecShadowsToUpdate.removeAll(registerSpecShadowsToDeletedChannelSpecs);

        // Then we can do since channel specs refer load profile specs, we first have to...
        deleteChannelSpecs(shadow.getChannelSpecShadows().getDeletedShadows());

        List<ChannelSpecShadow> channelsToUpdateToExistingLoadProfiles = getChannelSpecsToExistingLoadProfilesOrNone(shadow.getChannelSpecShadows()
                .getUpdatedShadows(), shadow.getLoadProfileSpecShadows());
        updateChannelSpecs(channelsToUpdateToExistingLoadProfiles);
        updateLoadProfileSpecs(shadow.getLoadProfileSpecShadows()); // and AFTERWARDS possibly delete load profile specs
        List<ChannelSpecShadow> updatedShadows = new ArrayList<>(shadow.getChannelSpecShadows().getUpdatedShadows());
        updatedShadows.removeAll(channelsToUpdateToExistingLoadProfiles);
        updateChannelSpecs(updatedShadows);
        createChannelSpecs(shadow.getChannelSpecShadows().getNewShadows());

        // Now we need to update the new and updated specs with the correct linkedChannelSpecId if spec id is 0
        for (RegisterSpecShadow specShadow : shadow.getRegisterSpecShadows()) {
            if (specShadow.getLinkedChannelSpecId() == 0 && specShadow.getLinkedChannelSpecShadow() != null && specShadow.getLinkedChannelSpecShadow().getDeviceConfigId() == getId()) {
                List<ChannelSpec> channelSpecs = getChannelSpecs();
                for (ChannelSpec channelSpec : channelSpecs) {
                    if (channelSpec.getName().equals(specShadow.getLinkedChannelSpecShadow().getName())) {
                        specShadow.setLinkedChannelSpecId(channelSpec.getId());
                    }
                }
            }
        }
        updateRegisterSpecs(registerSpecShadowsToUpdate);
        createRegisterSpecs(shadow.getRegisterSpecShadows().getNewShadows());

    }

    private List<ChannelSpecShadow> getChannelSpecsToExistingLoadProfilesOrNone(List<ChannelSpecShadow> updatedShadows, ShadowList<LoadProfileSpecShadow> loadProfileSpecShadows) {
        List<LoadProfileSpecShadow> loadProfileSpecsToCheck = new ArrayList<>(loadProfileSpecShadows.getUpdatedShadows());
        loadProfileSpecsToCheck.addAll(loadProfileSpecShadows.getRemainingShadows());
        List<ChannelSpecShadow> result = new ArrayList<>();
        for (ChannelSpecShadow updatedShadow : updatedShadows) {
            if (updatedShadow.getLoadProfileTypeId() == 0 || linksToSpecOfList(updatedShadow, loadProfileSpecsToCheck)) {
                result.add(updatedShadow);
            }
        }
        return result;
    }

    private boolean linksToSpecOfList(ChannelSpecShadow updatedShadow, List<LoadProfileSpecShadow> loadProfileSpecsToCheck) {
        for (LoadProfileSpecShadow shadow : loadProfileSpecsToCheck) {
            if (shadow.getLoadProfileTypeId() == updatedShadow.getLoadProfileTypeId()) {
                return true;
            }
        }
        return false;
    }

    private void createChannelSpecs(List<ChannelSpecShadow> specShadows) throws BusinessException, SQLException {
        ChannelSpecFactory factory = getChannelSpecFactory();
        for (ChannelSpecShadow specShadow : specShadows) {
            specShadow.setDeviceConfigId(getId());
            factory.create(specShadow);
        }
        channelSpecs = null;
    }

    private void updateChannelSpecs(List<ChannelSpecShadow> specShadows) throws BusinessException, SQLException {
        ChannelSpecFactory factory = getChannelSpecFactory();
        for (ChannelSpecShadow specShadow : specShadows) {
            ChannelSpec target = factory.find(specShadow.getId());
            if (target != null) {
                target.update(specShadow);
            }
        }
        channelSpecs = null;
    }

    private void deleteChannelSpecs(List<ChannelSpecShadow> specShadows) throws BusinessException, SQLException {
        ChannelSpecFactory factory = getChannelSpecFactory();
        for (ChannelSpecShadow specShadow : specShadows) {
            ChannelSpec target = factory.find(specShadow.getId());
            if (target != null) {
                target.delete();
            }
        }
        channelSpecs = null;
    }

    private void updateLoadProfileSpecs(ShadowList<LoadProfileSpecShadow> specShadows) throws SQLException, BusinessException {
        if (!specShadows.isDirty()) {
            return;
        }
        // 1. Delete
        LoadProfileSpecFactory factory = getLoadProfileSpecFactory();
        for (LoadProfileSpecShadow specShadow : specShadows.getDeletedShadows()) {
            LoadProfileSpec target = factory.find(specShadow.getId());
            if (target != null) {
                target.delete();
            }
        }
        // 2. Update
        for (LoadProfileSpecShadow specShadow : specShadows.getUpdatedShadows()) {
            LoadProfileSpec target = factory.find(specShadow.getId());
            if (target != null) {
                target.update(specShadow);
            }
        }
        // 3. Insert of new ones
        for (LoadProfileSpecShadow specShadow : specShadows.getNewShadows()) {
            specShadow.setDeviceConfigId(getId());
            factory.create(specShadow);
        }
        loadProfileSpecs = null;
    }

    public List<RegisterSpec> getRegisterSpecs() {
        return Collections.unmodifiableList(this.registerSpecs);
    }

    @Override
    public void createRegisterSpec(RegisterMapping registerMapping) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteRegisterSpec(RegisterSpec registerSpec) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<ChannelSpec> getChannelSpecs() {
        return Collections.unmodifiableList(this.channelSpecs);
    }

    @Override
    public void createChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteChannelSpec(ChannelSpec channelSpec) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<LoadProfileSpec> getLoadProfileSpecs() {
        return Collections.unmodifiableList(this.loadProfileSpecs);
    }

    @Override
    public void createLoadProfileSpec(LoadProfileType loadProfileType) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LogBookSpec> getLogBookSpecs() {
        return Collections.unmodifiableList(this.logBookSpecs);
    }

    @Override
    public void createLogBookSpec(LogBookType logBookType) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteLogBookSpec(LogBookSpec logBookSpec) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getActive() {
        return active;
    }

    public void activate() {
        execute(new Transaction<Object>() {
            public Object doExecute() throws SQLException, BusinessException {
                doActivate();
                return null;
            }
        });
    }

    public void doActivate() {
        this.active = true;
        updateField("active", 1);
        updateAuditInfo(AuditTrail.ACTION_UPDATE);
    }

    public void deactivate() throws SQLException, BusinessException {
        execute(new Transaction<Object>() {
            public Object doExecute() throws SQLException, BusinessException {
                doDeactivate();
                return null;
            }
        });
    }

    public void doDeactivate() throws SQLException, BusinessException {
        if (getBaseFactory().hasAnyForDeviceConfig(this)) {
            throw new BusinessException("cannotDeactivateDeviceConfig",
                    "Device configuration '{0}' cannot be deactivated since it is still in use", this.getName());
        }
        this.active = false;
        updateField("active", 0);
        updateAuditInfo(AuditTrail.ACTION_UPDATE);
    }

    @Override
    public void save() {
        this.modificationDate = this.clock.now();
        super.save();
    }

    @Override
    protected void postNew() {
        validateRequiredFields();
        this.getDataMapper().persist(this);
    }

    private void validateRequiredFields() {
        validateDeviceTypeExists();
    }

    private void validateDeviceTypeExists() {
        if (this.deviceType == null) {
            throw new DeviceTypeIsRequiredException(this.thesaurus);
        }
    }

    @Override
    protected void post() {
        this.getDataMapper().update(this);
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected void validateDelete() {
        if (getActive()) {
            throw new BusinessException("cannotDeleteDeviceConfigSinceActive",
                    "Device configuration '{0}' cannot be deleted because it is still active", this.getName());
        }
    }

    @Override
    protected void deleteDependents() throws SQLException, BusinessException {
        super.deleteDependents();
        DeviceCommunicationConfiguration communicationConfiguration = this.getCommunicationConfiguration();
        if (communicationConfiguration != null) {
            communicationConfiguration.delete();
        }
        for (LogBookSpec logBookSpec : getLogBookSpecFactory().findByDeviceConfig(this)) {
            logBookSpec.delete();
        }
        for (RegisterSpec registerSpec : getRtuRegisterSpecFactory().findByDeviceConfig(this)) {
            registerSpec.delete();
        }
        for (ChannelSpec channelSpec : getChannelSpecFactory().findByDeviceConfig(this)) {
            channelSpec.delete();
        }
        for (LoadProfileSpec loadProfileSpec : getLoadProfileSpecFactory().findByDeviceConfig(this)) {
            loadProfileSpec.delete();
        }
    }

    public boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId) {
        if (getLogBookSpecs() != null && !getLogBookSpecs().isEmpty()) {
            for (LogBookSpec logBookSpec : getLogBookSpecs()) {
                if (logBookSpec.getLogBookType().getId() == logBookTypeId && logBookSpec.getId() != updateId) {
                    return true;
                }
            }
        }
        return false;
    }


}