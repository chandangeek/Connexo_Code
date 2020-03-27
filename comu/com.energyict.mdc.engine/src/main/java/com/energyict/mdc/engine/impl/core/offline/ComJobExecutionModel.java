package com.energyict.mdc.engine.impl.core.offline;


import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.config.impl.SecurityPropertySetImpl;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.impl.OfflineDeviceForComTaskGroup;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionGroup;
import com.energyict.mdc.engine.impl.core.offline.adapters.MapDeviceIdentifierMeterReadingAdapter;
import com.energyict.mdc.engine.impl.core.offline.identifiers.*;
import com.energyict.mdc.engine.impl.core.remote.*;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.ManualMeterReadingsTask;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.upl.meterdata.RegisterGroup;
import com.energyict.mdc.upl.meterdata.identifiers.*;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;

/**
 * The execution model, which contains all necessary info required during execution of a ComJob.
 * This model should be preloaded/created when the offline ComServer is still online, in order to ease
 * <b>offline</b> execution of a ComJob
 * <p/>
 * Note that this model is uniquely identified by the (database) ID of its device.
 * This is used to compare models and in the filename of the persisted model.
 *
 * @author sva, khe
 * @since 11/07/2014 - 16:06
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ComJobExecutionModel implements CanProvideDescriptionTitle {

    private static final String MMR_TITLE = "Manual meter readings";
    private static final String MISSING_MMR_TITLE = "Missing manual meter readings";

    /**
     * The ComJob for which model is made
     */
    private ComJob comJob;

    /**
     * The offline variant of the <b>master</b> device
     */
    private OfflineDevice offlineDevice;

    /**
     * The list of all SecurityPropertySets of the master device
     */
    private List<SecurityPropertySet> securityPropertySets;

    /**
     * The list of SecurityProperties mapped to their SecurityPropertySet
     */
    private Map<SecurityPropertySet, List<DeviceMasterDataExtractor.SecurityProperty>> securitySetPropertiesMap;

    /**
     * The map of ComTaskEnablements for each unique Device and ComTask combination
     */
    private Map<DeviceComTaskWrapper, ComTaskEnablement> comTaskEnablementMap;

    /**
     * The map of previous values for every MMR register that has a reference validation configured
     */
    private Map<OfflineRegister, Reading> previousValuesMap;

    /**
     * The map of the configured EIServer registers, containing some metadata like "isCumulative" etc
     */
    private Map<OfflineRegister, Register> registerMap;

    /**
     * The map of dialect properties for each unique comTaskExecutionId;
     */
    private Map<Long, TypedProperties> protocolDialectPropertiesPerComTaskExecutionIdMap;

    // Collected data objects (~the results of the execution)
    private Map<DeviceIdentifier, DeviceIdentifier> deviceGatewayMap;
    private Map<DeviceIdentifier, MeterReading> meterReadingMap;
    private Map<DeviceIdentifier, MeterReading> manualMeterReadingsMap;
    private Map<LoadProfileIdentifier, CollectedLoadProfile> collectedLoadProfileMap;
    private Map<LoadProfileIdentifier, Long> loadProfileReadDateMap;
    private Map<LogBookIdentifier, CollectedLogBook> collectedLogBookMap;
    private Map<LogBookIdentifier, Long> logBookReadDateMap;
    private Map<LogBookIdentifier, Long> logBookLastReadingsMap;
    private List<DeviceMessageInformationWrapper> collectedDeviceMessageInformationList;
    private Map<Long, Long> comTaskExecutionStartTimes;
    private Date connectionStartTime = null;

    //Helper objects that hold states for the GUI
    private ComJobResult result = ComJobResult.Pending;
    private boolean connectionTaskSuccess = false;
    private boolean active = true;
    private ComJobState state = ComJobState.Pending;
    private List<ComTaskExecution> successFullComTaskExecutions;
    private List<ComTaskExecution> failedComTaskExecutions;

    private final long comPortId;

    private final ComServer.LogLevel communicationLogLevel;
    private final Object lock = new Object();   //Lock to handle concurrency on ComJobState field
    private boolean completed;
    private boolean mmrHaveChanged = false;

    private boolean skipped;
    private String completionCode; // lookup string value
    private String reasonCode; // free text

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private ComJobExecutionModel() {
        comPortId = -1;
        communicationLogLevel = null;
    }

    public ComJobExecutionModel(ComJob comJob, long comPortId, ComServer.LogLevel communicationLogLevel) {
        this.comJob = comJob;
        this.comPortId = comPortId;
        this.communicationLogLevel = communicationLogLevel;
    }

    @XmlAttribute
    private long getComPortId() {
        return comPortId;
    }

    public void initializeModel(RemoteComServerDAOImpl remoteComServerDAO) {
        DeviceIdentifier deviceIdentifierOfMaster = new DeviceIdentifierById(getConnectionTask().getDevice().getId());
        OfflineDeviceForComTaskGroup offlineDeviceContext = new OfflineDeviceForComTaskGroup(getComTaskExecutions());
        this.offlineDevice = remoteComServerDAO.findOfflineDevice(deviceIdentifierOfMaster, offlineDeviceContext).get();
        this.securityPropertySets = remoteComServerDAO.findAllSecurityPropertySetsForDevice(deviceIdentifierOfMaster);
//        initializeAllSecuritySetProperties(remoteComServerDAO, deviceIdentifierOfMaster);
        initializeAllProtocolDialectProperties(remoteComServerDAO);
        initializeComTaskEnablements(remoteComServerDAO);
//        initializeValidationRules(remoteComServerDAO);
    }

    /**
     * Fetch the validation rules for every MMR register.
     * Also fetch relevant metadata, e.g. the previous register reading that can be used during validation.
     */
//    private void initializeValidationRules(RemoteComServerDAOImpl remoteComServerDAO) {
//        List<LicensedRegisterValidationRule> allowedRuleTypes = Arrays.asList(LicensedRegisterValidationRule.MIN_MAX, LicensedRegisterValidationRule.REFERENCE_REGISTER, LicensedRegisterValidationRule.REGISTER_ADVANCE);
//        for (OfflineRegister offlineRegister : getOfflineRegistersForMMR()) {
//
//            RegisterIdentifierById identifier = new RegisterIdentifierById(new DeviceIdentifierById(offlineRegister.getDeviceId()), offlineRegister.getRegisterId(), offlineRegister.getObisCode());
//            List<RegisterValidationRule> validationRules = remoteComServerDAO.getValidationRules(identifier, allowedRuleTypes);
//
//            //Also fetch the reference value in advance, so it can be used during the validation
//            RegisterValidationRule minMaxReferenceValidationRule = getMinMaxReferenceValidationRule(validationRules);
//            if (minMaxReferenceValidationRule != null) {
//                RegisterIdentifierById referenceRegisterIdentifier = new RegisterIdentifierById(new DeviceIdentifierById(offlineRegister.getDeviceId()), minMaxReferenceValidationRule.getRefRegisterId(), null);
//                BasicConsumption referenceConsumption = remoteComServerDAO.getReferenceConsumption(referenceRegisterIdentifier);
//                getReferenceValuesMap().put(offlineRegister, referenceConsumption);
//            }
//
//            //Also fetch the previous value in advance, so it can be showed in the UI and used during the validation
//            RegisterReading previousReading = remoteComServerDAO.getPreviousConsumption(identifier);
//            getPreviousValuesMap().put(offlineRegister, previousReading);
//
//            //Also fetch the register object in advance, so it can be used during the validation. This also includes metadata like numberOfDigits etc.
//            Register register = remoteComServerDAO.findRegister(identifier);
//            getRegisterMap().put(offlineRegister, register);
//
//            getValidationRulesMap().put(offlineRegister, validationRules);
//        }
//    }
//
//    private RegisterValidationRule getMinMaxReferenceValidationRule(List<RegisterValidationRule> validationRules) {
//        for (RegisterValidationRule validationRule : validationRules) {
//            if (validationRule.getValidationMethod().getValidatorFactory().getLicensedRegisterValidationRule() == LicensedRegisterValidationRule.REFERENCE_REGISTER) {
//                return validationRule;
//            }
//        }
//        return null;
//    }

    private void initializeAllSecuritySetProperties(RemoteComServerDAOImpl remoteComServerDAO, DeviceIdentifier deviceIdentifierOfMaster) {
        for (SecurityPropertySet securityPropertySet : securityPropertySets) {
            getSecuritySetPropertiesMap().put(securityPropertySet, remoteComServerDAO.getPropertiesFromSecurityPropertySet(deviceIdentifierOfMaster, securityPropertySet.getId()));
        }
    }

    private void initializeAllProtocolDialectProperties(RemoteComServerDAOImpl remoteComServerDAO) {
        for (ComTaskExecution comTaskExecution : getComTaskExecutions()) {
            getProtocolDialectPropertiesPerComTaskExecutionIdMap().put(comTaskExecution.getId(), remoteComServerDAO.findProtocolDialectPropertiesFor(comTaskExecution.getId()));
        }
    }

    private void initializeComTaskEnablements(RemoteComServerDAOImpl remoteComServerDAO) {
        List<ComTaskExecution> comTaskExecutions = comJob.getComTaskExecutions();
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            long comTaskId = comTaskExecution.getComTask().getId();
            DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(comTaskExecution.getDevice().getId());
            ComTaskEnablement comTaskEnablement = remoteComServerDAO.findComTaskEnablementByDeviceAndComTask(deviceIdentifier, comTaskId);
            DeviceComTaskWrapper key = new DeviceComTaskWrapper(deviceIdentifier, comTaskId);
            getComTaskEnablementMap().put(key, comTaskEnablement);
        }
    }

    public void resetCollectedData() {
        this.manualMeterReadingsMap = null;
        this.successFullComTaskExecutions = null;
        this.failedComTaskExecutions = null;
        this.collectedLoadProfileMap = null;
        this.collectedDeviceMessageInformationList = null;
        this.meterReadingMap = null;
        this.collectedLogBookMap = null;
        this.deviceCache = null;
        this.comSessionBuilder = null;
        setMmrHaveChanged(true);        //Rebuild the mmr journal entries in the comsession shadow
    }

    @XmlElement(type = ComTaskExecutionGroup.class, name = "comJob")
    public ComJob getComJob() {
        return comJob;
    }

    public List<ComTaskExecution> getComTaskExecutions() {
        return getComJob().getComTaskExecutions();
    }

    public ConnectionTask getConnectionTask() {
        return getComJob().getConnectionTask();
    }

    public Device getDevice() {
        return getConnectionTask().getDevice();
    }

    public Device findGatewayForDevice(int deviceId) {
        return checkDeviceIsSlaveDevice(deviceId)
                ? getDevice()   // return the master device
                : null;         // device is not a slave, thus has no gateway
    }

    @XmlElement(type = OfflineDeviceImpl.class)
    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    public OfflineDevice findOfflineDevice(ComServerDAO comServerDAO, DeviceIdentifier deviceIdentifier) {
        return MobileDeviceFactory.findOfflineDevice(comServerDAO, this, deviceIdentifier);
    }

    public OfflineRegister findOfflineRegister(ComServerDAO comServerDAO, RegisterIdentifier registerIdentifier) {
        return MobileRegisterFactory.findOfflineRegister(comServerDAO, this, registerIdentifier);
    }

    public OfflineLoadProfile findOfflineLoadProfile(ComServerDAO comServerDAO, LoadProfileIdentifier loadProfileIdentifier) {
        return MobileLoadProfileFactory.findOfflineLoadProfile(comServerDAO, this, loadProfileIdentifier);
    }

    public OfflineLogBook findOfflineLogBook(ComServerDAO comServerDAO, LogBookIdentifier logBookIdentifier) {
        return MobileLogBookFactory.findOfflineLogBook(comServerDAO, this, logBookIdentifier);
    }

    public OfflineDeviceMessage findOfflineDeviceMessage(ComServerDAO comServerDAO, MessageIdentifier deviceMessageIdentifier) {
        return MobileDeviceMessageFactory.findOfflineDeviceMessage(comServerDAO, this, deviceMessageIdentifier);
    }

    @XmlElement(type = SecurityPropertySetImpl.class)
    public List<SecurityPropertySet> getSecurityPropertySets() {
        return securityPropertySets;
    }

    /**
     * Returns the securityPropertySets for the master device
     */
    public List<SecurityPropertySet> findAllSecurityPropertySetsForDevice(DeviceIdentifier deviceIdentifier) {
        checkIsMasterDeviceIdentifier(deviceIdentifier);
        return getSecurityPropertySets();
    }

    public TypedProperties findProtocolDialectPropertiesFor(long comTaskExecutionId) {
        return getProtocolDialectPropertiesPerComTaskExecutionIdMap().get(comTaskExecutionId);
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    protected Map<Long, TypedProperties> getProtocolDialectPropertiesPerComTaskExecutionIdMap() {
        if (this.protocolDialectPropertiesPerComTaskExecutionIdMap == null) {
            this.protocolDialectPropertiesPerComTaskExecutionIdMap = new HashMap<>();
        }
        return this.protocolDialectPropertiesPerComTaskExecutionIdMap;
    }

    /**
     * Return the comTaskEnablement for any device (master or slave)
     *
     * @param deviceIdentifier must be of type com.energyict.mdc.protocol.inbound.DeviceIdentifier
     */
    public ComTaskEnablement findComTaskEnablementByDeviceAndComTask(DeviceIdentifier deviceIdentifier, int comTaskId) {
        checkDeviceIdentifierIsDeviceIdentifierById(deviceIdentifier);
        return getComTaskEnablementMap().get(new DeviceComTaskWrapper(deviceIdentifier, comTaskId));
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    @XmlAnyElement
    public Map<DeviceComTaskWrapper, ComTaskEnablement> getComTaskEnablementMap() {
        if (this.comTaskEnablementMap == null) {
            this.comTaskEnablementMap = new HashMap<>();
        }
        return this.comTaskEnablementMap;
    }

//    @XmlAttribute
//    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
//    public Map<OfflineRegister, List<RegisterValidationRule>> getValidationRulesMap() {
//        if (this.validationRulesMap == null) {
//            this.validationRulesMap = new HashMap<>();
//        }
//        return this.validationRulesMap;
//    }
//
//    @XmlAttribute
//    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
//    public Map<OfflineRegister, BasicConsumption> getReferenceValuesMap() {
//        if (this.referenceValuesMap == null) {
//            this.referenceValuesMap = new HashMap<>();
//        }
//        return this.referenceValuesMap;
//    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<OfflineRegister, Register> getRegisterMap() {
        if (this.registerMap == null) {
            this.registerMap = new HashMap<>();
        }
        return this.registerMap;
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<OfflineRegister, Reading> getPreviousValuesMap() {
        if (this.previousValuesMap == null) {
            this.previousValuesMap = new HashMap<>();
        }
        return this.previousValuesMap;
    }

    /**
     * Returns the security properties for the master device
     */
    public List<DeviceMasterDataExtractor.SecurityProperty> getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, SecurityPropertySet securityPropertySet) {
        checkIsMasterDeviceIdentifier(deviceIdentifier);
        return getSecuritySetPropertiesMap().get(securityPropertySet);
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    @JsonIgnore
    public Map<SecurityPropertySet, List<DeviceMasterDataExtractor.SecurityProperty>> getSecuritySetPropertiesMap() {
        if (this.securitySetPropertiesMap == null) {
            this.securitySetPropertiesMap = new HashMap<>();
        }
        return this.securitySetPropertiesMap;
    }

    public boolean isMMROnly(ComTaskExecution comTaskExecution) {
        for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
            if (!(protocolTask instanceof ManualMeterReadingsTask)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasMMRTask(ComTaskExecution comTaskExecution) {
        for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
            if (protocolTask instanceof ManualMeterReadingsTask) {
                return true;
            }
        }
        return false;
    }

    public String getDescriptionTitle () {
        return MMR_TITLE;
    }

    /**
     * Return a description of the collected registers for a specific MMR task
     */
    public String getManualMeterReadingsDescription(List<OfflineRegister> offlineRegisters) {
        boolean empty = true;
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        if (isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
            PropertyDescriptionBuilder registersToReadBuilder = builder.addListProperty("registers");
            for (OfflineRegister offlineRegister : offlineRegisters) {
                String text = getMMRDescription(offlineRegister);
                if (text != null) {
                    empty = false;
                    registersToReadBuilder.append("(");
                    registersToReadBuilder.append(offlineRegister.getObisCode());
                    registersToReadBuilder.append(" - ");
                    registersToReadBuilder.append(text);
                    registersToReadBuilder.append(")");
                    registersToReadBuilder.next();
                }
            }
        } else {
            builder.addProperty("nrOfRegistersToRead").append(offlineRegisters.size());
        }
        if (empty) {
            return null;
        } else {
            return builder.toString();
        }
    }

    /**
     * Return a description of the missing registers for a specific MMR task
     * Or null if there's no missing registers
     */
    public String getMissingManualMeterReadingsDescription(List<OfflineRegister> offlineRegisters) {
        boolean empty = true;
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        PropertyDescriptionBuilder registersToReadBuilder = builder.addListProperty("registers");
        for (OfflineRegister offlineRegister : offlineRegisters) {
            String text = getMMRDescription(offlineRegister);
            if (text != null) {
                empty = false;
                registersToReadBuilder.append("(");
                registersToReadBuilder.append(offlineRegister.getObisCode());
                registersToReadBuilder.append(")");
                registersToReadBuilder.next();
            }
        }
        if (empty) {
            return null;
        } else {
            return builder.toString();
        }
    }

    public List<OfflineRegister> getOfflineRegistersForRegisterGroup(ComTaskExecution comTaskExecution, List<RegisterGroup> registerGroups) {
        List<OfflineRegister> result = new ArrayList<>();
        long deviceId = comTaskExecution.getDevice().getId();   //TODO thoroughly test
        if (registerGroups.size() > 0) {
            List<OfflineRegister> allRegistersForRegisterGroup = offlineDevice.getRegistersForRegisterGroup(registerGroups);
            for (OfflineRegister register : allRegistersForRegisterGroup) {
                if (deviceId == register.getDeviceId()) {
                    result.add(register);    //Only add the registers of the master or the slave, not both
                }
            }
        } else {
            List<OfflineRegister> allRegisters = offlineDevice.getAllOfflineRegisters();
            for (OfflineRegister register : allRegisters) {
                if (deviceId == register.getDeviceId()) {
                    result.add(register);    //Only add the registers of the master or the slave, not both
                }
            }
        }
        return result;
    }

    /**
     * Return a list comtask(s) that have an MMR task.
     */
    public Map<ComTaskExecution, ManualMeterReadingsTask> getMMRTasks() {
        Map<ComTaskExecution, ManualMeterReadingsTask> mmrTasks = new HashMap<>();
        for (ComTaskExecution comTaskExecution : getComTaskExecutions()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (ManualMeterReadingsTask.class.isAssignableFrom(protocolTask.getClass())) {
                    mmrTasks.put(comTaskExecution, (ManualMeterReadingsTask) protocolTask);
                    break;
                }
            }
        }
        return mmrTasks;
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<DeviceIdentifier, MeterReading> getManualMeterReadingsMap() {
        if (manualMeterReadingsMap == null) {
            manualMeterReadingsMap = new HashMap<>();
        }
        return manualMeterReadingsMap;
    }

    public void setManualMeterReadingsMap(Map<DeviceIdentifier, MeterReading> manualMeterReadingsMap) {
        this.manualMeterReadingsMap = manualMeterReadingsMap;
        setMmrHaveChanged(true);
    }

    @XmlAttribute
    public boolean isMmrHaveChanged() {
        return mmrHaveChanged;
    }

    public void setMmrHaveChanged(boolean mmrHaveChanged) {
        this.mmrHaveChanged = mmrHaveChanged;
    }

    public String getMMRDescription(OfflineRegister offlineRegister) {
        for (MeterReading meterReading : getManualMeterReadingsMap().values()) {
            for (Reading reading : meterReading.getReadings()) {
                if (reading.getReadingTypeCode().equals(offlineRegister.getReadingTypeMRID())) {
                    return reading.getText();
                }
            }
        }
        return null;
    }

    private void appendPropertyToMessage(StringBuilder builder, ConnectionTaskProperty property) {
        this.appendPropertyToMessage(builder, property.getName(), property.getValue());
    }

    private void appendPropertyToMessage(StringBuilder builder, String propertyName, Object propertyValue) {
        builder.append(
                MessageFormat.format(
                        "{0} = {1}",
                        propertyName,
                        String.valueOf(propertyValue)
                )
        );
    }

    /**
     * Tests if the LogLevel is enabled,
     * in which case something should be logged in the ComSession.
     *
     * @param logLevel The ComServer.LogLevel
     * @return A flag that indicates if the DeviceCommand should be logged
     */
    private boolean isLogLevelEnabled(ComServer.LogLevel logLevel) {
        return getCommunicationLogLevel().compareTo(logLevel) >= 0;
    }

    @XmlAttribute
    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    @XmlAttribute
    public String getCompletionCode() {
        return completionCode;
    }

    public void setCompletionCode(String completionCode) {
        this.completionCode = completionCode;
    }

    @XmlAttribute
    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @XmlAttribute
    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public void setConnectionStartTime(Date connectionStartTime) {
        this.connectionStartTime = connectionStartTime;
    }

    @XmlAttribute
    public Date getConnectionStartTime() {
        return connectionStartTime;
    }

    public void setComTaskExecutionStartTime(ComTaskExecution comTaskExecution, Date startTime) {
        getComTaskExecutionStartTimes().put(comTaskExecution.getId(), startTime.getTime());
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<Long, Long> getComTaskExecutionStartTimes() {
        if (this.comTaskExecutionStartTimes == null) {
            this.comTaskExecutionStartTimes = new HashMap<>();
        }
        return this.comTaskExecutionStartTimes;
    }

    private enum MultipleLineListAppendMode {
        FIRST {
            @Override
            protected void startOn(StringBuilder builder) {
                builder.append("\r\n ");
            }
        },
        REMAINING {
            @Override
            protected void startOn(StringBuilder builder) {
                builder.append("\r\n ");
            }
        };

        protected abstract void startOn(StringBuilder builder);
    }

    @XmlAttribute
    public ComServer.LogLevel getCommunicationLogLevel() {
        return communicationLogLevel;
    }

    public void updateGatewayOfDevice(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        getDeviceGatewayMap().put(deviceIdentifier, gatewayDeviceIdentifier);
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<DeviceIdentifier, DeviceIdentifier> getDeviceGatewayMap() {
        if (this.deviceGatewayMap == null) {
            this.deviceGatewayMap = new HashMap<>();
        }
        return this.deviceGatewayMap;
    }

    public void addCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier, CollectedLoadProfile collectedLoadProfile) {
        getCollectedLoadProfileMap().put(loadProfileIdentifier, collectedLoadProfile);
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<LoadProfileIdentifier, CollectedLoadProfile> getCollectedLoadProfileMap() {
        if (this.collectedLoadProfileMap == null) {
            this.collectedLoadProfileMap = new HashMap<>();
        }
        return this.collectedLoadProfileMap;
    }

    public void updateLoadProfileReadDate(LoadProfileIdentifier loadProfileIdentifier, Date startTimestamp) {
        getLoadProfileReadDateMap().put(loadProfileIdentifier, startTimestamp.getTime());
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<LoadProfileIdentifier, Long> getLoadProfileReadDateMap() {
        if (this.loadProfileReadDateMap == null) {
            this.loadProfileReadDateMap = new HashMap<>();
        }
        return this.loadProfileReadDateMap;
    }

    public void addCollectedLogBook(LogBookIdentifier logBookIdentifier, CollectedLogBook collectedLogBook) {
        getCollectedLogBookMap().put(logBookIdentifier, collectedLogBook);
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<LogBookIdentifier, CollectedLogBook> getCollectedLogBookMap() {
        if (this.collectedLogBookMap == null) {
            this.collectedLogBookMap = new HashMap<>();
        }
        return this.collectedLogBookMap;
    }

    public void updateLogBookReadDate(LogBookIdentifier logBookIdentifier, Date startTimestamp) {
        getLogBookReadDateMap().put(logBookIdentifier, startTimestamp.getTime());
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<LogBookIdentifier, Long> getLogBookReadDateMap() {
        if (this.logBookReadDateMap == null) {
            this.logBookReadDateMap = new HashMap<>();
        }
        return this.logBookReadDateMap;
    }

    public void updateLogBookLastReading(LogBookIdentifier logBookIdentifier, Date startTimestamp) {
        getLogBookLastReadingsMap().put(logBookIdentifier, startTimestamp.getTime());
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<LogBookIdentifier, Long> getLogBookLastReadingsMap() {
        if (this.logBookLastReadingsMap == null) {
            this.logBookLastReadingsMap = new HashMap<>();
        }
        return this.logBookLastReadingsMap;
    }

    public void addCollectedDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
        getCollectedDeviceMessageInformationList().add(new DeviceMessageInformationWrapper(messageIdentifier, newDeviceMessageStatus, sentDate, protocolInformation));
    }

    @XmlAttribute
    public List<DeviceMessageInformationWrapper> getCollectedDeviceMessageInformationList() {
        if (this.collectedDeviceMessageInformationList == null) {
            this.collectedDeviceMessageInformationList = new ArrayList<>(0);
        }
        return this.collectedDeviceMessageInformationList;
    }

    private void checkIsMasterDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        DeviceIdentifierById masterDeviceIdentifier = new DeviceIdentifierById(getOfflineDevice().getId());
        if (!deviceIdentifier.equals(masterDeviceIdentifier)) {
            throw new UnsupportedOperationException("Unsupported identifier '" + deviceIdentifier.toString() + "' of type " + deviceIdentifier.getXmlType() + ", DAO call cannot be executed offline.");
        }
    }

    private boolean checkDeviceIsSlaveDevice(int deviceId) {
        for (com.energyict.mdc.upl.offline.OfflineDevice slaveDevice : getOfflineDevice().getAllSlaveDevices()) {
            if (deviceId == slaveDevice.getId()) {
                return true;
            }
        }
        return false;
    }

    private void checkDeviceIdentifierIsDeviceIdentifierById(DeviceIdentifier deviceIdentifier) {
        if (!(deviceIdentifier instanceof DeviceIdentifierById)) {
            throw new UnsupportedOperationException("Unsupported identifier '" + deviceIdentifier.toString() + "' of type '" + deviceIdentifier.getXmlType() + "', expecting type '" + DeviceIdentifierById.class.getCanonicalName() + "'.");
        }
    }

    public void setConnectionTaskSuccess(boolean connectionTaskSuccess) {
        this.connectionTaskSuccess = connectionTaskSuccess;
    }

    @XmlAttribute
    public boolean isConnectionTaskSuccess() {
        return connectionTaskSuccess;
    }

    @XmlAttribute
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @XmlAttribute
    public ComJobState getState() {
        synchronized (lock) {
            return state;
        }
    }

    public void setState(ComJobState state) {
        synchronized (lock) {
            this.state = state;
        }
    }

    @XmlAttribute
    public ComJobResult getResult() {
        return result;
    }

    public void setResult(ComJobResult result) {
        if (this.result == ComJobResult.Failed) {
            return;     //Don't change the result if it was set to failed before
        }
        this.result = result;
    }

    public void forceResult(ComJobResult result) {
        this.result = result;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    /**
     * Comparison of 2 models is done based on the database ID of their device
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ComJobExecutionModel)) {
            return false;
        }
        ComJobExecutionModel otherModel = (ComJobExecutionModel) obj;

        return this.getDevice().getId() == otherModel.getDevice().getId();
    }

    public void addSuccessfulComTaskExecution(ComTaskExecution comTaskExecution, boolean mmr) {
        if (!hasMMRTask(comTaskExecution) || mmr) {
            if (!getSuccessFullComTaskExecutions().contains(comTaskExecution)) {
                getSuccessFullComTaskExecutions().add(comTaskExecution);
                ((ComTaskExecutionImpl) comTaskExecution).setStatus(TaskStatus.Waiting);
            }
            if (getFailedComTaskExecutions().contains(comTaskExecution)) {
                getFailedComTaskExecutions().remove(comTaskExecution);      //Remove comtask from failed tasks, it was reset and executed again.
            }
        }
    }

    @XmlElement(type = ComTaskExecutionImpl.class)
    public List<ComTaskExecution> getSuccessFullComTaskExecutions() {
        if (successFullComTaskExecutions == null) {
            successFullComTaskExecutions = new ArrayList<>();
        }
        return successFullComTaskExecutions;
    }

    public void addFailedComTaskExecution(ComTaskExecution comTaskExecution, boolean mmr) {
        if (!hasMMRTask(comTaskExecution) || mmr) {
            if (!getFailedComTaskExecutions().contains(comTaskExecution)) {
                if (comTaskExecution instanceof ComTaskExecutionImpl) {
                    ((ComTaskExecutionImpl) comTaskExecution).setStatus(TaskStatus.Failed);
                }
                getFailedComTaskExecutions().add(comTaskExecution);
            }
            if (getSuccessFullComTaskExecutions().contains(comTaskExecution)) {
                getSuccessFullComTaskExecutions().remove(comTaskExecution);      //Remove comtask from successful tasks, it was reset and it's new execution failed
            }
        }
    }

    @XmlElement(type = ComTaskExecutionImpl.class)
    public List<ComTaskExecution> getFailedComTaskExecutions() {
        if (failedComTaskExecutions == null) {
            failedComTaskExecutions = new ArrayList<>();
        }
        return failedComTaskExecutions;
    }

    /**
     * Returns a list of all registers that can be read using MMR.
     * These are the ones explicitly defined on the MMR task(s)/
     */
    public List<OfflineRegister> getOfflineRegistersForMMR() {
        Map<ComTaskExecution, ManualMeterReadingsTask> mmrTasks = getMMRTasks();
        List<OfflineRegister> allMMRRegisters = new ArrayList<>();
        for (ComTaskExecution comTaskExecution : mmrTasks.keySet()) {
            ManualMeterReadingsTask mmrTask = mmrTasks.get(comTaskExecution);
            List<OfflineRegister> offlineRegistersForMMRTask = getOfflineRegistersForRegisterGroup(comTaskExecution, mmrTask.getRegisterGroups());
            for (OfflineRegister mmrRegister : offlineRegistersForMMRTask) {
                if (!allMMRRegisters.contains(mmrRegister)) {       //Don't add it twice
                    allMMRRegisters.add(mmrRegister);
                }
            }
        }

        return allMMRRegisters;
    }

    // New execution model data
    private DeviceProtocolCacheXmlWrapper deviceCache;
    private ComSessionBuilderXmlWrapper comSessionBuilder;
    private List<Map<LogBookIdentifier, Instant>> logBookUpdates;
    private List<Map<LoadProfileIdentifier, Instant>> loadProfileUpdates;

    public void createComSession(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
        comSessionBuilder = new ComSessionBuilderXmlWrapper(builder, stopDate, successIndicator);
    }

    @XmlElement
    public ComSessionBuilderXmlWrapper getComSessionBuilder() {
        return comSessionBuilder;
    }

    //Only used by JSON deserializing
    public void setComSessionBuilder(ComSessionBuilderXmlWrapper comSessionBuilder) {
        this.comSessionBuilder = comSessionBuilder;
    }

    @XmlAttribute
    public DeviceProtocolCacheXmlWrapper getDeviceCache() {
        return deviceCache;
    }

    //Only used by JSON deserializing
    public void setDeviceCache(DeviceProtocolCacheXmlWrapper deviceCache) {
        this.deviceCache = deviceCache;
    }

    public void addLogBookUpdate(Map<LogBookIdentifier, Instant> logBookUpdate) {
        getLogBookUpdates().add(logBookUpdate);
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public List<Map<LogBookIdentifier, Instant>> getLogBookUpdates() {
        if (logBookUpdates == null)
            logBookUpdates = new ArrayList<>();
        return logBookUpdates;
    }

    public void addLoadProfileUpdate(Map<LoadProfileIdentifier, Instant> loadProfileUpdate) {
        getLoadProfileUpdates().add(loadProfileUpdate);
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public List<Map<LoadProfileIdentifier, Instant>> getLoadProfileUpdates() {
        if (loadProfileUpdates == null)
            loadProfileUpdates = new ArrayList<>();
        return loadProfileUpdates;
    }

    /**
     * After executing a comjob, the comserver calls this method to store the register values.
     */
    public void addMeterReading(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
        getMeterReadingMap().put(deviceIdentifier, meterReading);
    }

    @XmlElement
    @XmlJavaTypeAdapter(MapDeviceIdentifierMeterReadingAdapter.class)
    public Map<DeviceIdentifier, MeterReading> getMeterReadingMap() {
        if (this.meterReadingMap == null) {
            this.meterReadingMap = new HashMap<>();
        }
        return this.meterReadingMap;
    }
}
