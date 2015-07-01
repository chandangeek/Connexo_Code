package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.ClientTypeManager;
import com.energyict.dlms.cosem.DeviceTypeManager;
import com.energyict.dlms.cosem.ScheduleManager;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.protocol.security.SecurityPropertySet;
import com.energyict.mdc.protocol.tasks.*;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.tasks.*;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterGroup;
import com.energyict.mdw.core.*;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdwswing.decorators.mdc.NextExecutionSpecsShadowDecorator;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.RTU3;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects.*;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.security.AS330DSecuritySupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 22/06/2015 - 9:53
 */
public class RTU3Messaging extends AbstractMessageExecutor implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;

    static {
        supportedMessages = new ArrayList<>();
        supportedMessages.add(DeviceActionMessage.SyncMasterdataForDC);
        supportedMessages.add(DeviceActionMessage.PauseDCScheduler);
        supportedMessages.add(DeviceActionMessage.ResumeDCScheduler);
    }

    public RTU3Messaging(RTU3 protocol) {
        super(protocol);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncMasterdataForDC)) {
                    collectedMessage = syncMasterData(pendingMessage, collectedMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.PauseDCScheduler)) {
                    setSchedulerState(SchedulerState.PAUSED);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ResumeDCScheduler)) {
                    setSchedulerState(SchedulerState.RUNNING);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            } finally {
                result.addCollectedMessage(collectedMessage);
            }
        }

        return null;
    }

    private void setSchedulerState(SchedulerState state) throws IOException {
        getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager().writeSchedulerState(state.toDLMSEnum());
    }

    private CollectedMessage syncMasterData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        AllMasterData allMasterData;
        try {
            final String serializedMasterData = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
            final JSONObject jsonObject = new JSONObject(serializedMasterData);
            allMasterData = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), AllMasterData.class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        syncSchedules(allMasterData);
        syncClientTypes(allMasterData);
        syncDeviceTypes(allMasterData);
        syncDevices(allMasterData);
        return collectedMessage;
    }

    private void syncDevices(AllMasterData allMasterData) throws IOException {
        for (RTU3MeterDetails rtu3MeterDetails : allMasterData.getMeterDetailsList()) {
            getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().assignDeviceType(rtu3MeterDetails.toStructure());
        }
    }

    private void syncDeviceTypes(AllMasterData allMasterData) throws IOException {
        final DeviceTypeManager deviceTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();
        final Array deviceTypesArray = deviceTypeManager.readDeviceTypes();

        List<Long> deviceTypeIds = new ArrayList<>();
        for (AbstractDataType deviceType : deviceTypesArray) {
            if (deviceType.isStructure() && deviceType.getStructure().nrOfDataTypes() > 0) {
                final long deviceTypeId = deviceType.getStructure().getDataType(0).longValue(); //First element of the structure is the deviceType ID
                deviceTypeIds.add(deviceTypeId);
            }
        }

        List<Long> syncedDeviceTypes = new ArrayList<>();
        for (RTU3DeviceType deviceType : allMasterData.getDeviceTypes()) {
            if (deviceTypeIds.contains(deviceType.getId())) {
                deviceTypeManager.updateDeviceType(deviceType.toStructure());   //TODO optimize: only update if something's different
                syncedDeviceTypes.add(deviceType.getId());
            } else {
                deviceTypeManager.addDeviceType(deviceType.toStructure());
                syncedDeviceTypes.add(deviceType.getId());
            }
        }

        //TODO should we remove old data in the DC?
        for (Long deviceTypeId : deviceTypeIds) {
            if (!syncedDeviceTypes.contains(deviceTypeId)) {
                deviceTypeManager.removeDeviceType(deviceTypeId);
            }
        }
    }

    private void syncClientTypes(AllMasterData allMasterData) throws IOException {
        final ClientTypeManager clientTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();
        final Array clientTypesArray = clientTypeManager.readClients();

        List<Long> clientTypeIds = new ArrayList<>();
        for (AbstractDataType clientType : clientTypesArray) {
            if (clientType.isStructure() && clientType.getStructure().nrOfDataTypes() > 0) {
                final long clientTypeId = clientType.getStructure().getDataType(0).longValue(); //First element of the structure is the clientType ID
                clientTypeIds.add(clientTypeId);
            }
        }

        List<Long> syncedClientTypes = new ArrayList<>();
        for (RTU3ClientType rtu3ClientType : allMasterData.getClientTypes()) {
            if (clientTypeIds.contains(rtu3ClientType.getId())) {
                clientTypeManager.updateClientType(rtu3ClientType.toStructure());     //TODO optimize: only update if something's different
                syncedClientTypes.add(rtu3ClientType.getId());
            } else {
                clientTypeManager.addClientType(rtu3ClientType.toStructure());
                syncedClientTypes.add(rtu3ClientType.getId());
            }
        }

        //TODO should we remove old data in the DC?
        for (Long clientTypeId : clientTypeIds) {
            if (!syncedClientTypes.contains(clientTypeId)) {
                clientTypeManager.removeClientType(clientTypeId);
            }
        }
    }

    private void syncSchedules(AllMasterData allMasterData) throws IOException {
        final ScheduleManager scheduleManager = getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();
        final Array schedulesArray = scheduleManager.readSchedules();

        List<Long> scheduleIds = new ArrayList<>();
        for (AbstractDataType schedule : schedulesArray) {
            if (schedule.isStructure() && schedule.getStructure().nrOfDataTypes() > 0) {
                final long scheduleId = schedule.getStructure().getDataType(0).longValue(); //First element of the structure is the schedule ID
                scheduleIds.add(scheduleId);
            }
        }

        List<Long> syncedSchedules = new ArrayList<>();
        for (RTU3Schedule rtu3Schedule : allMasterData.getSchedules()) {
            if (scheduleIds.contains(rtu3Schedule.getId())) {
                scheduleManager.updateSchedule(rtu3Schedule.toStructure());   //TODO optimize: only update if something's different
                syncedSchedules.add(rtu3Schedule.getId());
            } else {
                scheduleManager.addSchedule(rtu3Schedule.toStructure());
                syncedSchedules.add(rtu3Schedule.getId());
            }
        }

        //TODO should we remove old data in the DC?
        for (Long scheduleId : scheduleIds) {
            if (!syncedSchedules.contains(scheduleId)) {
                scheduleManager.removeSchedule(scheduleId);
            }
        }
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.dcDeviceIDAttributeName)) {

            final Device masterDevice = mw().getDeviceFactory().find(((BigDecimal) messageAttribute).intValue());
            if (masterDevice == null) {
                throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException("DC device ID", messageAttribute.toString(), "ID should reference a unique device");
            }

            final AllMasterData allMasterData = new AllMasterData();
            for (Device device : masterDevice.getDownstreamDevices()) {

                final RTU3MeterDetails meterDetails = createMeterDetails(device, masterDevice);
                allMasterData.getMeterDetailsList().add(meterDetails);

                final DeviceType deviceType = device.getDeviceType();
                if (!deviceTypeAlreadyExists(allMasterData.getDeviceTypes(), deviceType)) {
                    final int deviceTypeId = deviceType.getId();
                    final String deviceTypeName = deviceType.getName();
                    final RTU3ProtocolConfiguration protocolConfiguration = getProtocolConfiguration(masterDevice, deviceType);
                    final List<RTU3Schedulable> schedulables = getSchedulables(device);
                    final RTU3ClockSyncConfiguration clockSyncConfiguration = getClockSyncConfiguration(device);

                    final RTU3DeviceType rtu3DeviceType = new RTU3DeviceType(deviceTypeId, deviceTypeName, protocolConfiguration, schedulables, clockSyncConfiguration);
                    allMasterData.getDeviceTypes().add(rtu3DeviceType);
                }

                for (ComTaskExecution comTaskExecution : device.getComTaskExecutions()) {
                    //Don't add the security set again if it's already there (based on EIServer database ID)
                    if (!clientTypeAlreadyExists(allMasterData.getClientTypes(), getClientTypeId(device, comTaskExecution))) {  //TODO optimize: avoid doubles? e.g. two unique securitysets that both have clientmacaddress 2 and securitylevel 3:0
                        final RTU3ClientType clientType = getClientType(device, comTaskExecution);
                        allMasterData.getClientTypes().add(clientType);
                    }

                    //Don't add a schedule if one already exists that has exactly the same name (e.g. 'every day at 00:00')
                    if (!scheduleAlreadyExists(allMasterData.getSchedules(), comTaskExecution.getNextExecutionSpecs())) {
                        final NextExecutionSpecs nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs();
                        final RTU3Schedule rtu3Schedule = new RTU3Schedule(getScheduleId(nextExecutionSpecs), getScheduleName(nextExecutionSpecs), CronTabStyleConverter.convert(nextExecutionSpecs));
                        allMasterData.getSchedules().add(rtu3Schedule);
                    }
                }
            }

            return jsonSerialize(allMasterData);
        } else {
            return messageAttribute.toString();
        }
    }

    private MeteringWarehouse mw() {
        final MeteringWarehouse mw = MeteringWarehouse.getCurrent();
        if (mw == null) {
            MeteringWarehouse.createBatchContext();
            return MeteringWarehouse.getCurrent();
        } else {
            return mw;
        }
    }

    private String jsonSerialize(AllMasterData allMasterData) {
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, allMasterData);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
        return writer.toString();
    }

    private boolean scheduleAlreadyExists(List<RTU3Schedule> schedules, NextExecutionSpecs nextExecutionSpecs) {
        for (RTU3Schedule schedule : schedules) {
            if (schedule.getName().equals(getScheduleName(nextExecutionSpecs))) {
                return true;
            }
        }
        return false;
    }

    private String getScheduleName(NextExecutionSpecs nextExecutionSpecs) {
        if (nextExecutionSpecs == null) {
            return "noSchedule";
        } else if (nextExecutionSpecs.getTemporalExpression() != null) {
            return new NextExecutionSpecsShadowDecorator(nextExecutionSpecs.getShadow()).toString();
        } else if (nextExecutionSpecs.getDialCalendar() != null) {
            return nextExecutionSpecs.getDialCalendar().getName();
        } else {
            return "noSchedule";
        }
    }

    private RTU3ClockSyncConfiguration getClockSyncConfiguration(Device device) {
        boolean setClock = false;
        int min = 0;
        int max = 0xFFFF;

        for (ComTaskExecution comTaskExecution : device.getComTaskExecutions()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof ClockTask) {
                    setClock = true;
                    min = ((ClockTask) protocolTask).getMinimumClockDifference().getSeconds();
                    max = ((ClockTask) protocolTask).getMaximumClockDifference().getSeconds();
                    break;
                }
            }
            if (setClock) {
                break;
            }
        }
        return new RTU3ClockSyncConfiguration(setClock, min, max);
    }

    private List<RTU3Schedulable> getSchedulables(Device device) {
        List<RTU3Schedulable> schedulables = new ArrayList<>();
        for (ComTaskExecution comTaskExecution : device.getComTaskExecutions()) {
            final int scheduleId = getScheduleId(comTaskExecution.getNextExecutionSpecs());
            final int logicalDeviceId = 1;  //TODO is this always 1? probably yes
            int clientTypeId = getClientTypeId(device, comTaskExecution);

            ArrayList<ObisCode> loadProfileObisCodes = getLoadProfileObisCodesForComTask(device, comTaskExecution);
            ArrayList<ObisCode> registerObisCodes = getRegisterObisCodesForComTask(device, comTaskExecution);
            ArrayList<ObisCode> logBookObisCodes = getLogBookObisCodesForComTask(device, comTaskExecution);

            if (isReadMeterDataTask(loadProfileObisCodes, registerObisCodes, logBookObisCodes)) {
                final RTU3Schedulable schedulable = new RTU3Schedulable(scheduleId, logicalDeviceId, clientTypeId, loadProfileObisCodes, registerObisCodes, logBookObisCodes);
                schedulables.add(schedulable);
            }
        }
        return schedulables;
    }

    private boolean isReadMeterDataTask(ArrayList<ObisCode> loadProfileObisCodes, ArrayList<ObisCode> registerObisCodes, ArrayList<ObisCode> logBookObisCodes) {
        return !(loadProfileObisCodes.isEmpty() && registerObisCodes.isEmpty() && logBookObisCodes.isEmpty());
    }

    private int getClientTypeId(Device device, ComTaskExecution comTaskExecution) {
        return getComTaskEnablement(device, comTaskExecution).getSecurityPropertySet().getId();
    }

    /**
     * Return the hash code of the spec name.
     * We want 2 specs that have exactly the same name (e.g. 'every day at 00:00') to have the same ID.
     */
    private int getScheduleId(NextExecutionSpecs nextExecutionSpecs) {
        return getScheduleName(nextExecutionSpecs).hashCode();
    }

    private ArrayList<ObisCode> getLogBookObisCodesForComTask(Device device, ComTaskExecution comTaskExecution) {
        Set<ObisCode> logBookObisCodes = new HashSet<>();
        if (((ServerComTask) comTaskExecution.getComTask()).isConfiguredToCollectEvents()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof LogBooksTask) {
                    final List<LogBookType> logBookTypes = ((LogBooksTask) protocolTask).getLogBookTypes();
                    if (logBookTypes.isEmpty()) {
                        for (LogBook logBook : device.getLogBooks()) {
                            logBookObisCodes.add(logBook.getLogBookSpec().getDeviceObisCode());
                        }
                    } else {
                        for (LogBookType logBookType : logBookTypes) {
                            logBookObisCodes.add(logBookType.getObisCode());
                        }
                    }
                }
            }
        }
        return new ArrayList<>(logBookObisCodes);
    }

    private ArrayList<ObisCode> getRegisterObisCodesForComTask(Device device, ComTaskExecution comTaskExecution) {
        Set<ObisCode> registerObisCodes = new HashSet<>();
        if (((ServerComTask) comTaskExecution.getComTask()).isConfiguredToCollectRegisterData()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof RegistersTask) {
                    final List<RegisterGroup> registerGroups = ((RegistersTask) protocolTask).getRegisterGroups();
                    if (registerGroups.isEmpty()) {
                        for (Register register : device.getRegisters()) {
                            registerObisCodes.add(register.getDeviceObisCode());
                        }
                    } else {
                        for (Register register : device.getRegisters()) {
                            for (RegisterGroup registerGroup : registerGroups) {
                                if (register.getRegisterGroup().getId() == registerGroup.getId()) {
                                    registerObisCodes.add(register.getDeviceObisCode());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(registerObisCodes);
    }

    private ArrayList<ObisCode> getLoadProfileObisCodesForComTask(Device device, ComTaskExecution comTaskExecution) {
        Set<ObisCode> loadProfileObisCodes = new HashSet<>();
        if (((ServerComTask) comTaskExecution.getComTask()).isConfiguredToCollectLoadProfileData()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof LoadProfilesTask) {
                    final List<LoadProfileType> loadProfileTypes = ((LoadProfilesTask) protocolTask).getLoadProfileTypes();
                    if (loadProfileTypes.isEmpty()) {
                        for (LoadProfile loadProfile : device.getLoadProfiles()) {
                            loadProfileObisCodes.add(loadProfile.getDeviceObisCode());
                        }
                    } else {
                        for (LoadProfileType loadProfileType : loadProfileTypes) {
                            loadProfileObisCodes.add(loadProfileType.getObisCode());
                        }
                    }
                }
            }
        }
        return new ArrayList<>(loadProfileObisCodes);
    }

    private RTU3ClientType getClientType(Device device, ComTaskExecution comTaskExecution) {
        final int clientTypeId = getClientTypeId(device, comTaskExecution);

        final SecurityPropertySet securityPropertySet = getComTaskEnablement(device, comTaskExecution).getSecurityPropertySet();
        final List<SecurityProperty> securityProperties = device.getProtocolSecurityProperties(securityPropertySet);
        BigDecimal clientMacAddress = AS330DSecuritySupport.DEFAULT_CLIENT_MAC_ADDRESS;   //TODO make generic so we can use other physical slave protocols too
        for (SecurityProperty securityProperty : securityProperties) {
            if (securityProperty.getName().equals(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString())) {
                clientMacAddress = (BigDecimal) securityProperty.getValue();
                break;
            }
        }

        return new RTU3ClientType(clientTypeId, clientMacAddress.intValue(), securityPropertySet.getAuthenticationDeviceAccessLevelId(), securityPropertySet.getEncryptionDeviceAccessLevelId());
    }

    private ComTaskEnablement getComTaskEnablement(Device device, ComTaskExecution comTaskExecution) {
        for (ComTaskEnablement comTaskEnablement : device.getConfiguration().getCommunicationConfiguration().getEnabledComTasks()) {
            if (comTaskEnablement.getComTask().getId() == comTaskExecution.getComTask().getId()) {
                return comTaskEnablement;
            }
        }
        throw MdcManager.getComServerExceptionFactory().missingComTaskEnablement(comTaskExecution);
    }

    private RTU3ProtocolConfiguration getProtocolConfiguration(Device masterDevice, DeviceType deviceType) {
        final String javaClassName = deviceType.getDeviceProtocolPluggableClass().getJavaClassName();

        final TimeDuration defaultTimeout = new TimeDuration(TcpDeviceProtocolDialect.DEFAULT_TCP_TIMEOUT);
        final BigDecimal defaultRetries = DlmsProtocolProperties.DEFAULT_RETRIES;
        TimeDuration timeout = defaultTimeout;
        BigDecimal retries = defaultRetries;

        final List<ProtocolDialectProperties> allProtocolDialectProperties = masterDevice.getAllProtocolDialectProperties();
        if (!allProtocolDialectProperties.isEmpty()) {
            final TypedProperties properties = allProtocolDialectProperties.get(0).getTypedProperties();
            timeout = properties.<TimeDuration>getTypedProperty(DlmsProtocolProperties.TIMEOUT, defaultTimeout);
            retries = properties.<BigDecimal>getTypedProperty(DlmsProtocolProperties.RETRIES, defaultRetries);
        }

        return new RTU3ProtocolConfiguration(javaClassName, retries.intValue(), (int) timeout.getMilliSeconds());
    }

    private boolean deviceTypeAlreadyExists(List<RTU3DeviceType> rtu3DeviceTypes, DeviceType deviceType) {
        for (RTU3DeviceType rtu3DeviceType : rtu3DeviceTypes) {
            if (rtu3DeviceType.getId() == deviceType.getId()) {
                return true;
            }
        }
        return false;
    }

    private boolean clientTypeAlreadyExists(List<RTU3ClientType> clientTypes, int clientTypeId) {
        for (RTU3ClientType clientType : clientTypes) {
            if (clientType.getId() == clientTypeId) {
                return true;
            }
        }
        return false;
    }

    private RTU3MeterDetails createMeterDetails(Device device, Device masterDevice) {
        final String callHomeId = device.getProtocolProperties().getStringProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
        if (callHomeId == null || callHomeId.length() != 16) {
            throw invalidFormatException(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, (callHomeId == null ? "null" : callHomeId), "Should be 16 hex characters");
        }
        try {
            ProtocolTools.getBytesFromHexString(callHomeId, "");
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw invalidFormatException(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, callHomeId, "Should be 16 hex characters");
        }

        final int deviceTypeId = device.getDeviceType().getId();

        String deviceTimeZone = DlmsProtocolProperties.DEFAULT_TIMEZONE;
        final TimeZoneInUse timeZoneInUse = device.getProtocolProperties().<TimeZoneInUse>getTypedProperty(DlmsProtocolProperties.TIMEZONE);
        if (timeZoneInUse != null && timeZoneInUse.getTimeZone() != null) {
            deviceTimeZone = timeZoneInUse.getTimeZone().getID();
        }

        final byte[] masterKey = getSecurityKey(masterDevice, SecurityPropertySpecName.MASTER_KEY.toString());    //TODO master key on RTU3 or AS330D?
        final byte[] password = getSecurityKey(device, SecurityPropertySpecName.PASSWORD.toString());
        final byte[] ak = getSecurityKey(device, SecurityPropertySpecName.AUTHENTICATION_KEY.toString());
        final byte[] ek = getSecurityKey(device, SecurityPropertySpecName.ENCRYPTION_KEY.toString());

        final String wrappedPassword = ProtocolTools.getHexStringFromBytes(wrap(password, masterKey), "");
        final String wrappedAK = ProtocolTools.getHexStringFromBytes(wrap(ak, masterKey), "");
        final String wrappedEK = ProtocolTools.getHexStringFromBytes(wrap(ek, masterKey), "");

        return new RTU3MeterDetails(callHomeId, deviceTypeId, deviceTimeZone, wrappedPassword, wrappedAK, wrappedEK);
    }

    private byte[] wrap(byte[] key, byte[] masterKey) {
        final Key keyToWrap = new SecretKeySpec(key, "AES");
        final Key kek = new SecretKeySpec(masterKey, "AES");
        try {
            final Cipher aesWrap = Cipher.getInstance("AESWrap");
            aesWrap.init(Cipher.WRAP_MODE, kek);
            return aesWrap.wrap(keyToWrap);
        } catch (GeneralSecurityException e) {
            throw MdcManager.getComServerExceptionFactory().createDataEncryptionException(e);
        }
    }

    /**
     * Iterate over every defined security set to find a certain security property.
     * If it's not defined on any security set, throw missingProperty exception
     */
    private byte[] getSecurityKey(Device device, String propertyName) {
        for (SecurityPropertySet securityPropertySet : device.getConfiguration().getCommunicationConfiguration().getSecurityPropertySets()) {
            final List<SecurityProperty> securityProperties = device.getProtocolSecurityProperties(securityPropertySet);
            for (SecurityProperty securityProperty : securityProperties) {
                if (securityProperty.getName().equals(propertyName)) {
                    final String propertyValue = (String) securityProperty.getValue();
                    return parseKey(propertyName, propertyValue);
                }
            }
        }
        throw MdcManager.getComServerExceptionFactory().missingProperty(propertyName);
    }

    private byte[] parseKey(String propertyName, String propertyValue) {
        if (propertyValue == null || propertyValue.length() != 32) {
            throw invalidFormatException(propertyName, propertyValue == null ? "null" : propertyValue, "Should be 32 hex characters");
        }
        try {
            return ProtocolTools.getBytesFromHexString(propertyValue, "");
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw invalidFormatException(propertyName, propertyValue, "Should be 32 hex characters");
        }
    }

    private ComServerExecutionException invalidFormatException(String propertyName, String propertyValue, String message) {
        return MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(propertyName, propertyValue, message);
    }

    public enum SchedulerState {

        UNKNOWN(0), NOT_RUNNING(1), RUNNING(2), PAUSED(3);

        private final int state;

        SchedulerState(int state) {
            this.state = state;
        }

        public TypeEnum toDLMSEnum() {
            return new TypeEnum(getState());
        }

        public int getState() {
            return state;
        }
    }
}