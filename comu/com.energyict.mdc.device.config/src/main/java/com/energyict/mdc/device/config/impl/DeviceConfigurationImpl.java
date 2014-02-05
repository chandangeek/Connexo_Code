package com.energyict.mdc.device.config.impl;

import com.energyict.cpo.AuditTrail;
import com.energyict.cpo.AuditTrailFactory;
import com.energyict.cpo.PersistentNamedObject;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DuplicateException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdObjectShadow;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.PrimaryKeyExternalRepresentationConvertor;
import com.energyict.mdc.common.ShadowList;
import com.energyict.mdc.common.SoftTypeId;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TypeId;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.devices.configuration.DeviceCommunicationConfiguration;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdw.amr.RegisterMappingFactoryProvider;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdw.amr.RegisterSpecFactory;
import com.energyict.mdw.amr.RegisterSpecFactoryProvider;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdw.core.ChannelSpecFactory;
import com.energyict.mdw.core.ChannelSpecFactoryProvider;
import com.energyict.mdw.core.DeviceCollectionMethodType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdw.core.DeviceConfigurationFactory;
import com.energyict.mdw.core.DeviceConfigurationFactoryProvider;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdw.core.DeviceTypeFactory;
import com.energyict.mdw.core.DeviceTypeFactoryProvider;
import com.energyict.mdw.core.EndDevice;
import com.energyict.mdw.core.LoadProfileSpec;
import com.energyict.mdw.core.LoadProfileSpecFactory;
import com.energyict.mdw.core.LoadProfileSpecFactoryProvider;
import com.energyict.mdw.core.LoadProfileTypeFactoryProvider;
import com.energyict.mdw.core.LogBookSpec;
import com.energyict.mdw.core.LogBookSpecFactory;
import com.energyict.mdw.core.LogBookSpecFactoryProvider;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdw.core.LogBookTypeFactoryProvider;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.configchange.DeviceConfigurationChanges;
import com.energyict.mdw.coreimpl.configchange.DeviceConfigurationChangesImpl;
import com.energyict.mdw.interfacing.mdc.MdcInterface;
import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;
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
import com.energyict.mdw.task.CreateDeviceTransaction;
import com.energyict.mdw.taskimpl.CloneDeviceTransaction;
import com.energyict.mdw.taskimpl.CreateDeviceFromPrototypeTransaction;
import com.energyict.mdw.taskimpl.CreateDeviceFromSpecsTransaction;
import com.energyict.mdw.xml.Command;
import com.energyict.mdw.xml.DeviceConfigurationCommand;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: gde
 * Date: 5/11/12
 */
public class DeviceConfigurationImpl extends PersistentNamedObject implements DeviceConfiguration, ServerDeviceConfiguration {

    private String description;
    private int prototypeId;
    private int deviceTypeId;
    private boolean active;

    private Device prototype;
    private List<RegisterSpec> registerSpecs;
    private List<ChannelSpec> channelSpecs;
    private List<LoadProfileSpec> loadProfileSpecs;
    private List<LogBookSpec> logBookSpecs;
    private DeviceCommunicationConfiguration communicationConfiguration;

    /**
     * The type ID of the objects this factory manages.
     */
    private final TypeId typeId;

    protected DeviceConfigurationImpl(ResultSet resultSet) throws SQLException {
        doLoad(resultSet);
        this.typeId = new SoftTypeId(MeteringWarehouse.FACTORYID_DEVICECONFIGURATION, PrimaryKeyExternalRepresentationConvertor.intToBytes(this.getId()));
    }

    protected DeviceConfigurationImpl(int id) {
        super(id);
        this.typeId = new SoftTypeId(MeteringWarehouse.FACTORYID_DEVICECONFIGURATION, PrimaryKeyExternalRepresentationConvertor.intToBytes(this.getId()));
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
        return this.prototypeId;
    }

    public Device getPrototypeDevice() {
        if (this.prototypeId == 0) {
            return null;
        }
        if (this.prototype == null) {
            this.prototype = getBaseFactory().find(this.prototypeId);
        }
        return this.prototype;
    }

    @Override
    protected void doLoad(ResultSet resultSet) throws SQLException {
        super.doLoad(resultSet);
        this.description = resultSet.getString(3);
        this.prototypeId = resultSet.getInt(4);
        this.deviceTypeId = resultSet.getInt(5);
        this.active = resultSet.getInt(6) != 0;
    }

    @Override
    protected int bindBody(PreparedStatement preparedStatement, int offset) throws SQLException {
        preparedStatement.setString(offset++, this.description);
        if (this.prototypeId == 0) {
            preparedStatement.setNull(offset++, Types.INTEGER);
        } else {
            preparedStatement.setInt(offset++, this.prototypeId);
        }
        preparedStatement.setInt(offset++, this.deviceTypeId);
        preparedStatement.setInt(offset++, this.getActive() ? 1 : 0);
        return offset;
    }

    private DeviceFactory getBaseFactory() {
        return DeviceFactoryProvider.instance.get().getDeviceFactory();
    }

    protected String getTableName() {
        return DeviceConfigurationFactoryImpl.TABLENAME;
    }

    protected String[] getColumns() {
        return DeviceConfigurationFactoryImpl.COLUMNS;
    }

    public void init(final DeviceConfigurationShadow shadow) throws SQLException, BusinessException {
        execute(new Transaction<Void>() {
            public Void doExecute() throws BusinessException, SQLException {
                doInit(shadow);
                return null;
            }
        });
    }

    @Override
    public void collectionMethodChanged(DeviceCollectionMethodType currentType) throws BusinessException, SQLException {
        switch (currentType) {
            case DeviceCollectionMethodType.HEADEND_SYSTEM:
                DeviceCommunicationConfiguration comConfiguration = this.getCommunicationConfiguration();
                if (comConfiguration != null) {
                    comConfiguration.delete();
                    this.communicationConfiguration = null;
                }
                break;
            default:
                if (this.getCommunicationConfiguration() == null) {
                    createCommunicationConfiguration(getDeviceType().getNewDeviceConfigurationShadow());
                }
        }
        updatePerformed();
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

    private RegisterSpecFactory getRtuRegisterSpecFactory() {
        return RegisterSpecFactoryProvider.instance.get().getRegisterSpecFactory();
    }

    private ChannelSpecFactory getChannelSpecFactory() {
        return ChannelSpecFactoryProvider.instance.get().getChannelSpecFactory();
    }

    private DeviceConfigurationFactory getDeviceConfigFactory() {
        return DeviceConfigurationFactoryProvider.instance.get().getDeviceConfigurationFactory();
    }

    private DeviceTypeFactory getRtuTypeFactory() {
        return DeviceTypeFactoryProvider.instance.get().getDeviceTypeFactory();
    }

    private LoadProfileSpecFactory getLoadProfileSpecFactory() {
        return LoadProfileSpecFactoryProvider.instance.get().getLoadProfileSpecFactory();
    }

    private LogBookSpecFactory getLogBookSpecFactory() {
        return LogBookSpecFactoryProvider.instance.get().getLogBookSpecFactory();
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
        validate(shadow.getName());
        int prototypeId = shadow.getPrototypeId();
        if (getRtuTypeFactory().find(deviceTypeId) == null) {
            throw new BusinessException("deviceTypeXDoesntExist", "Device type with id {0,number} does not exist", deviceTypeId);
        }
        if (prototypeId != 0) {
            EndDevice rtu = (EndDevice) getBaseFactory().find(prototypeId);
            if (rtu == null) {
                throw new BusinessException("prototypeRtuXDoesntExist", "Prototype device with id {0,number} does not exist", prototypeId);
            }
            if (rtu.getDeviceType().getId() != getDeviceType().getId()) {
                throw new BusinessException("prototypeRtuHasWrongTypeX", "Prototype device has wrong type: \"{0}\"", rtu.getDeviceType().getName());
            }
        }
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

    private LogBookType findLogBookType(LogBookSpecShadow logBookSpecShadow) {
        return LogBookTypeFactoryProvider.instance.get().getLogBookTypeFactory().find(logBookSpecShadow.getLogBookTypeId());
    }

    private ObisCode findObisCodeFromChannelSpecShadow(ChannelSpecShadow channelSpecShadow) {
        return RegisterMappingFactoryProvider.instance.get().getRegisterMappingFactory().find(channelSpecShadow.getRtuRegisterMappingId()).getObisCode();
    }


    private ObisCode findObisCodeFromLoadProfileSpecShadow(LoadProfileSpecShadow loadProfileSpecShadow) {
        return LoadProfileTypeFactoryProvider.instance.get().getLoadProfileTypeFactory().find(loadProfileSpecShadow.getLoadProfileTypeId()).getObisCode();
    }

    private ObisCode findObisCodeFromRegisterSpecShadow(RegisterSpecShadow registerSpecShadow) {
        if (registerSpecShadow.getOverruledObisCode() != null) {
            return registerSpecShadow.getOverruledObisCode();
        } else {
            return registerSpecShadow.getObisCode();
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

    protected void copyNew(DeviceConfigurationShadow shadow) {
        copy(shadow);
        this.active = false;
    }

    protected void copyUpdate(DeviceConfigurationShadow shadow) {
        copy(shadow);
    }

    protected void copy(DeviceConfigurationShadow shadow) {
        setName(shadow.getName());
        this.description = shadow.getDescription();
        this.prototypeId = shadow.getPrototypeId();
        this.prototype = null;
        this.deviceTypeId = shadow.getDeviceTypeId();
    }

    private int getDeviceTypeId() {
        return deviceTypeId;
    }

    public DeviceType getDeviceType() {
        return getRtuTypeFactory().find(deviceTypeId);
    }

    public DeviceConfigurationShadow getShadow() {
        DeviceConfigurationShadow deviceConfigurationShadow = new DeviceConfigurationShadow(this);
        deviceConfigurationShadow.setDeviceCommunicationConfigurationConstructionValidation(new DeviceCommunicationConfigurationConstructionValidationImpl());
        return deviceConfigurationShadow;
    }

    public void update(final DeviceConfigurationShadow shadow) throws SQLException, BusinessException {
        execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doUpdate(shadow);
                return null;
            }
        });
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
            if (specShadow.getLinkedChannelSpecShadow()!=null) {
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

    @Override
    protected void doUpdateAuditInfo(char action) throws SQLException, BusinessException {
        IdObjectShadow auditCopy = getShadow().getAuditCopy();
        new AuditTrailFactory().create(auditCopy, MeteringWarehouse.FACTORYID_DEVICECONFIGURATION, action);
    }

    public List<RegisterSpec> getRegisterSpecs() {
        if (registerSpecs == null) {
            registerSpecs = getRtuRegisterSpecFactory().findByDeviceConfig(this);
        }
        return registerSpecs;
    }

    public List<ChannelSpec> getChannelSpecs() {
        if (channelSpecs == null) {
            channelSpecs = getChannelSpecFactory().findByDeviceConfig(this);
        }
        return channelSpecs;
    }

    public List<LoadProfileSpec> getLoadProfileSpecs() {
        if (loadProfileSpecs == null) {
            loadProfileSpecs = getLoadProfileSpecFactory().findByDeviceConfig(this);
        }
        return loadProfileSpecs;
    }

    @Override
    public List<LogBookSpec> getLogBookSpecs() {
        if (logBookSpecs == null) {
            logBookSpecs = getLogBookSpecFactory().findByDeviceConfig(this);
        }
        return logBookSpecs;
    }

    private MdcInterface getMdcInterface() {
        return MdcInterfaceProvider.instance.get().getMdcInterface();
    }

    public boolean getActive() {
        return active;
    }

    public void activate() throws SQLException, BusinessException {
        execute(new Transaction<Object>() {
            public Object doExecute() throws SQLException, BusinessException {
                doActivate();
                return null;
            }
        });
    }

    public void doActivate() throws SQLException, BusinessException {
        this.active = true;
        updateField("active", 1);
        updateAuditInfo(AuditTrail.ACTION_UPDATE);
    }

    @Override
    protected void updatePerformed() {
        ((ServerDeviceConfigurationFactory) getDeviceConfigFactory()).clearFromCache(this);
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
    protected void validateDelete() throws SQLException, BusinessException {
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

    // Exportable Interface
    @Override
    public Command<DeviceConfiguration> createConstructor() {
        return new DeviceConfigurationCommand(this);
    }

    @Override
    public boolean isExportAllowed() {
        return true;
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

    @Override
    public CreateDeviceTransaction newDeviceTransaction () {
        Device prototype = getPrototypeDevice();
        if (prototype == null) {
            return new CreateDeviceFromSpecsTransaction(this);
        }
        else {
            return new CreateDeviceFromPrototypeTransaction(this);
        }
    }

    @Override
    public CreateDeviceTransaction newDeviceTransactionForCloning (Device device) {
        return new CloneDeviceTransaction(this, (EndDevice) device);
    }

    @Override
    public void validateCollectionUpdate(DeviceCollectionMethodType collectionMethodType) throws BusinessException {
        for (ChannelSpec channelSpec : getChannelSpecs()) {
            ((EndDeviceChannelSpec) channelSpec).validateFor(collectionMethodType);
        }
    }

    public DeviceConfigurationChanges constructDeviceConfigurationChanges(DeviceConfiguration targetConfiguration) {
        return new DeviceConfigurationChangesImpl(this, targetConfiguration);
    }

}