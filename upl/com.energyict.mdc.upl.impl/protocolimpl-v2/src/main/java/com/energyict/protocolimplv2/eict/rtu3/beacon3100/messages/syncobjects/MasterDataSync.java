package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:13
 */
public class MasterDataSync {

    private final Beacon3100Messaging beacon3100Messaging;
    private final ObjectMapperService objectMapperService;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final DeviceMasterDataExtractor deviceMasterDataExtractor;
    private final NlsService nlsService;

    protected StringBuilder info = new StringBuilder();

    protected DeviceMessageStatus syncStatus = null;

    public MasterDataSync(Beacon3100Messaging beacon3100Messaging, ObjectMapperService objectMapperService, IssueFactory issueFactory, PropertySpecService propertySpecService, DeviceMasterDataExtractor deviceMasterDataExtractor, NlsService nlsService) {
        this.beacon3100Messaging = beacon3100Messaging;
        this.objectMapperService = objectMapperService;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.deviceMasterDataExtractor = deviceMasterDataExtractor;
        this.nlsService = nlsService;
    }

    protected Beacon3100Messaging getBeacon3100Messaging() {
        return beacon3100Messaging;
    }

    protected ObjectMapperService getObjectMapperService() {
        return objectMapperService;
    }

    protected IssueFactory getIssueFactory() {
        return issueFactory;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected DeviceMasterDataExtractor getDeviceMasterDataExtractor() {
        return deviceMasterDataExtractor;
    }

    protected NlsService getNlsService() {
        return nlsService;
    }

    /**
     * Sync all master data of the device types (tasks, schedules, security levels, master data obiscodes, etc)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CollectedMessage syncMasterData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage, final boolean fullSync) throws IOException {
        this.info.setLength(0);

        AllMasterData allMasterData;
        try {
            final String serializedMasterData = pendingMessage.getPreparedContext();    //This context field contains the serialized version of the master data.
            if (serializedMasterData.contains("DeviceConfigurationException")) {
                return generateFailedMessage(pendingMessage, collectedMessage, serializedMasterData);
            }
            final JSONObject jsonObject = new JSONObject(serializedMasterData);
            allMasterData = objectMapperService.newJacksonMapper().readValue(new StringReader(jsonObject.toString()), AllMasterData.class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        List<Issue> issues = new ArrayList<>();

        final ScheduleManager scheduleManager = this.getScheduleManager();
        final DeviceTypeManager deviceTypeManager = this.getDeviceTypeManager();
        final ClientTypeManager clientTypeManager = this.getClientTypeManager();
        final ConcentratorSetup concentratorSetup = this.getConcentratorSetup();
        final SAPAssignment sapAssignment = this.getSAPAssignment();

        final MasterDataAnalyser analyzer = new MasterDataAnalyser(allMasterData,
                scheduleManager,
                deviceTypeManager,
                clientTypeManager,
                concentratorSetup,
                sapAssignment,
                this.getIsFirmwareVersion140OrAbove(),
                !this.readOldObisCodes());

        final List<MasterDataAnalyser.SyncAction<?>> actions = analyzer.analyze(fullSync);

        for (final MasterDataAnalyser.SyncAction<?> action : actions) {
            try {
                this.info.append(action.toString());
                action.execute();
                this.info.append(" - OK");
            } catch (Exception ex) {
                info.append("  > " + ex.getMessage());
                issues.add(issueFactory.createWarning(pendingMessage, "Cannot execute action " + action.toString() + ": " + ex.getMessage()));
            }
            info.append("\n");
        }

        this.info.append("\nPlan executed, sync complete.");


        collectedMessage.setDeviceProtocolInformation(getInfoMessage());
        if (syncStatus != null) {
            collectedMessage.setNewDeviceMessageStatus(syncStatus);
        }

        //Now see if there were any warning while parsing the EIServer model, and add them as proper issues.
        for (int index = 0; index < allMasterData.getWarningKeys().size(); index++) {
            String warningKey = allMasterData.getWarningKeys().get(index);
            String warningArgument = allMasterData.getWarningArguments().get(index);
            issues.add(this.issueFactory.createWarning(pendingMessage, warningKey, warningArgument));
        }

        if (!issues.isEmpty()) {
            collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, issues);
        }

        return collectedMessage;
    }

    /**
     * Returns a reference to the {@link ConcentratorSetup}.
     *
     * @return A reference to the {@link ConcentratorSetup}.
     * @throws NotInObjectListException If the {@link ConcentratorSetup} was not in the object-list.
     */
    private final ConcentratorSetup getConcentratorSetup() throws NotInObjectListException {
        if (this.readOldObisCodes()) {
            return this.getProtocol().getDlmsSession().getCosemObjectFactory().getConcentratorSetup();
        } else {
            return this.getProtocol().getDlmsSession().getCosemObjectFactory().getConcentratorSetup(Beacon3100Messaging.CONCENTRATOR_SETUP_NEW_LOGICAL_NAME);
        }
    }

    /**
     * Returns the {@link SAPAssignment}.
     *
     * @return The {@link SAPAssignment}.
     * @throws NotInObjectListException If the {@link SAPAssignment} is not in the object-list.
     */
    private final SAPAssignment getSAPAssignment() throws NotInObjectListException {
        return this.getProtocol().getDlmsSession().getCosemObjectFactory().getSAPAssignment();
    }

    private DeviceTypeManager getDeviceTypeManager() throws NotInObjectListException {
        if (readOldObisCodes()) {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();
        } else {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager(DeviceTypeManager.NEW_FW_OBISCODE);
        }
    }

    private ClientTypeManager getClientTypeManager() throws NotInObjectListException {
        if (readOldObisCodes()) {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();
        } else {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager(Beacon3100Messaging.CLIENT_MANAGER_NEW_OBISCODE);
        }
    }

    private ScheduleManager getScheduleManager() throws NotInObjectListException {
        if (readOldObisCodes()) {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();
        } else {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager(Beacon3100Messaging.SCHEDULE_MANAGER_NEW_OBISCODE);
        }

    }


    // truncate to max 4000 - as supported by varchar field in oracle
    protected String getInfoMessage() {
        String message = info.toString();
        return message.substring(0, Math.min(4000, message.length()));
    }

    /**
     * Sync the meter details. This assumes that the relevant device types are already synced!
     */
    public CollectedMessage syncDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        Beacon3100MeterDetails[] meterDetails;
        try {
            final String serializedMasterData = pendingMessage.getPreparedContext();    //This context field contains the serialized version of the master data.
            if (serializedMasterData.contains("DeviceConfigurationException")) {
                return generateFailedMessage(pendingMessage, collectedMessage, serializedMasterData);
            }
            final JSONArray jsonObject = new JSONArray(serializedMasterData);
            meterDetails = objectMapperService.newJacksonMapper().readValue(new StringReader(jsonObject.toString()), Beacon3100MeterDetails[].class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        syncDevices(meterDetails);

        boolean cleanupUnusedMasterData = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.cleanUpUnusedDeviceTypesAttributeName).getValue());
        if (cleanupUnusedMasterData) {
            // We'll just trim the fat from the Beacon here.
            final AllMasterData masterData = new AllMasterData();

            final MasterDataAnalyser analyzer = new MasterDataAnalyser(masterData,
                    this.getScheduleManager(),
                    this.getDeviceTypeManager(),
                    this.getClientTypeManager(),
                    this.getConcentratorSetup(),
                    this.getSAPAssignment(),
                    this.getIsFirmwareVersion140OrAbove(),
                    !this.readOldObisCodes());

            final List<MasterDataAnalyser.SyncAction<?>> plan = analyzer.analyze(true);

            for (final MasterDataAnalyser.SyncAction<?> action : plan) {
                action.execute();
            }
        }

        return collectedMessage;
    }

    private CollectedMessage generateFailedMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage, String serializedMasterData) {
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        collectedMessage.setDeviceProtocolInformation(serializedMasterData);
        collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, new Exception()));
        return collectedMessage;
    }

    public AbstractDlmsProtocol getProtocol() {
        return beacon3100Messaging.getProtocol();
    }

    private boolean getIsFirmwareVersion140OrAbove() throws IOException {
        String firmwareVersion = getProtocol().getDlmsSession().getCosemObjectFactory().getData(ObisCode.fromString("0.0.0.2.1.255")).getString();
        StringTokenizer tokenizer = new StringTokenizer(firmwareVersion, ".");
        String token = tokenizer.nextToken();
        int firstNr = Integer.parseInt(token);
        if (firstNr < 1) {
            return false;
        }
        token = tokenizer.nextToken();
        int secondNr = Integer.parseInt(token);
        return secondNr >= 4;
    }

    private void syncDevices(Beacon3100MeterDetails[] allMeterDetails) throws IOException {
        boolean isFirmwareVersion140OrAbove = getIsFirmwareVersion140OrAbove();
        for (Beacon3100MeterDetails beacon3100MeterDetails : allMeterDetails) {
            if (readOldObisCodes()) {
                syncOneDevice(beacon3100MeterDetails.toStructure(isFirmwareVersion140OrAbove));
            } else {
                syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove(beacon3100MeterDetails));
            }
        }
    }

    public CollectedMessage syncAllDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        syncAllDevices(newMasterDataSerializer().getMeterDetails((int) pendingMessage.getDeviceId()));

        return collectedMessage;
    }

    private MasterDataSerializer newMasterDataSerializer() {
        return new MasterDataSerializer(objectMapperService, propertySpecService, deviceMasterDataExtractor, getBeacon3100Properties(), nlsService);
    }

    private Beacon3100Properties getBeacon3100Properties() {
        return (Beacon3100Properties) getProtocol().getDlmsSessionProperties();
    }


    private void syncAllDevices(Beacon3100MeterDetails[] allMeterDetails) throws IOException {
        for (Beacon3100MeterDetails beacon3100MeterDetails : allMeterDetails) {
            syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove(beacon3100MeterDetails));
        }
    }

    private void syncOneDevice(Structure meterDetails) throws IOException {
        getDeviceTypeManager().assignDeviceType(meterDetails);
    }

    public CollectedMessage syncOneDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int deviceId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceId).getValue());
        Beacon3100MeterDetails meterDetails = newMasterDataSerializer().getMeterDetails(deviceId, (int) pendingMessage.getDeviceId());

        if (meterDetails != null) {
            syncOneDevice(meterDetails.toStructureFWVersion10AndAbove(meterDetails));
        } else {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, new ProtocolException("Device id not found on the master device.")));
        }

        return collectedMessage;
    }

    public CollectedMessage syncOneDeviceWithDCAdvanced(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        boolean undefinedStartDate = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ignoreStartDate).getValue());
        boolean undefinedEndDate = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ignoreEndDate).getValue());
        boolean undefinedPreviousStartDate = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ignorePreviousStartDate).getValue());
        boolean undefinedPreviousEndDate = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ignorePreviousEndDate).getValue());
        long configurationId = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.currentConfigurationId).getValue());
        Long startTime = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.currentStartDate).getValue());
        Long endTime = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.currentEndDate).getValue());
        long previousConfigurationId = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.previousConfigurationId).getValue());
        Long previousStartTime = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.previousStartDate).getValue());
        Long previousEndTime = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.previousEndDate).getValue());
        int deviceId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceId).getValue());
        Beacon3100MeterDetails meterDetails = newMasterDataSerializer().getMeterDetails(deviceId, (int) pendingMessage.getDeviceId());

        if (meterDetails != null) {
            try {
                List<DeviceTypeAssignment> deviceTypeAssignements = new ArrayList<>();
                deviceTypeAssignements.add(new DeviceTypeAssignment(previousConfigurationId, undefinedPreviousStartDate ? null : new Date(previousStartTime), undefinedPreviousEndDate ? null : new Date(previousEndTime)));
                deviceTypeAssignements.add(new DeviceTypeAssignment(configurationId, undefinedStartDate ? null : new Date(startTime), undefinedEndDate ? null : new Date(endTime)));
                syncOneDevice(meterDetails, deviceTypeAssignements);
            } catch (ParseException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
                collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
                return collectedMessage;
            }
        }

        return collectedMessage;
    }

    public void updateDeviceTypeForNewFW(Beacon3100DeviceType beacon3100DeviceType) throws IOException {
        DeviceTypeManager deviceTypeManager = getDeviceTypeManager();

        info.append("*** UPDATING DeviceType " + beacon3100DeviceType.getId() + " ***\n");

        try {
            deviceTypeManager.updateDeviceType(beacon3100DeviceType.toStructure(this.readOldObisCodes()));
            info.append("- DeviceType UPDATED: [").append(beacon3100DeviceType.getId()).append("]: ").append(beacon3100DeviceType.getName()).append("\n");
        } catch (IOException ex) {
            info.append("- Could not update DeviceType [" + beacon3100DeviceType.getId() + "]: " + ex.getMessage() + "\n");
        }
    }

    private void syncOneDevice(Beacon3100MeterDetails beacon3100MeterDetails, List<DeviceTypeAssignment> deviceTypeAssignements) throws ParseException, IOException {
        beacon3100MeterDetails.setDeviceTypeAssignments(deviceTypeAssignements);

        syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove(beacon3100MeterDetails));
    }

    public CollectedMessage setBufferForSpecificRegister(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getValue();
        int bufferSize = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getValue());

        Set<Beacon3100DeviceType> beacon3100DeviceTypes = MasterDataAnalyser.getDeviceTypesInBeaconMasterdata(this.getDeviceTypeManager());

        final Set<Beacon3100DeviceType> deviceTypesToUpdate = new HashSet<>();

        for (final Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes) {
            if (beacon3100DeviceType.updateBufferSizeForRegister(ObisCode.fromString(obisCode), new Unsigned16(bufferSize))) {
                deviceTypesToUpdate.add(beacon3100DeviceType);
            }
        }

        if (deviceTypesToUpdate.size() > 0) {
            for (final Beacon3100DeviceType deviceType : deviceTypesToUpdate) {
                this.updateDeviceTypeForNewFW(deviceType);
            }

            final String protocolInformation = new StringBuilder("Updated buffer size for register [").append(obisCode).append("] to [").append(bufferSize).append("] for device types [").append(deviceTypesToUpdate).append("]").toString();
            collectedMessage.setDeviceProtocolInformation(protocolInformation);
        } else {
            final String protocolInformation = new StringBuilder("Did not update buffer size for register [").append(obisCode).append("] to [").append(bufferSize).append("], no device types define it.").toString();
            collectedMessage.setDeviceProtocolInformation(protocolInformation);
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllRegisters(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int bufferSize = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getValue());

        Set<Beacon3100DeviceType> beacon3100DeviceTypes = MasterDataAnalyser.getDeviceTypesInBeaconMasterdata(this.getDeviceTypeManager());

        StringBuilder info = new StringBuilder();
        List<Issue> issues = new ArrayList<Issue>();

        for (Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes) {
            try {
                if (beacon3100DeviceType.updateBufferSizeForAllRegisters(new Unsigned16(bufferSize))) {
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    info.append("Set registers buffer size ").append(bufferSize).append(" to deviceType: ").append(beacon3100DeviceType.getName()).append(" [").append(beacon3100DeviceType.getId()).append("]\n");
                } else {
                    String msg = "Could not set registers buffer size to deviceType: " + beacon3100DeviceType.getName() + " [" + beacon3100DeviceType.getId() + "]";
                    info.append(msg).append("\n");
                    issues.add(issueFactory.createProblem(pendingMessage, msg));
                }
            } catch (Exception ex) {
                String msg = "Could not set register buffer size to deviceType: " + beacon3100DeviceType.getName() + " [" + beacon3100DeviceType.getId() + "] " + ex.getMessage();
                info.append(msg).append("\n");
                issues.add(issueFactory.createProblem(pendingMessage, msg));
            }
        }

        if (issues.size() > 0) {
            collectedMessage.setFailureInformation(ResultType.ConfigurationError, issues);
        }
        collectedMessage.setDeviceProtocolInformation(info.toString());

        return collectedMessage;
    }

    public CollectedMessage setBufferForSpecificEventLog(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getValue();
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getValue());

        Set<Beacon3100DeviceType> beacon3100DeviceTypes = MasterDataAnalyser.getDeviceTypesInBeaconMasterdata(this.getDeviceTypeManager());

        final Set<Beacon3100DeviceType> deviceTypesToUpdate = new HashSet<>();

        for (final Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes) {
            if (beacon3100DeviceType.updateBufferSizeForEventLogs(ObisCode.fromString(obisCode), new Unsigned32(bufferSize))) {
                deviceTypesToUpdate.add(beacon3100DeviceType);
            }
        }

        if (deviceTypesToUpdate.size() > 0) {
            for (final Beacon3100DeviceType deviceType : deviceTypesToUpdate) {
                this.updateDeviceTypeForNewFW(deviceType);
            }

            final String protocolInformation = new StringBuilder("Updated buffer size for event log [").append(obisCode).append("] to [").append(bufferSize).append("] for device types [").append(deviceTypesToUpdate).append("]").toString();
            collectedMessage.setDeviceProtocolInformation(protocolInformation);
        } else {
            final String protocolInformation = new StringBuilder("Did not update buffer size for event log [").append(obisCode).append("] to [").append(bufferSize).append("], no device types define it.").toString();
            collectedMessage.setDeviceProtocolInformation(protocolInformation);
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllEventLogs(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getValue());

        Set<Beacon3100DeviceType> beacon3100DeviceTypes = MasterDataAnalyser.getDeviceTypesInBeaconMasterdata(this.getDeviceTypeManager());

        StringBuilder info = new StringBuilder();
        List<Issue> issues = new ArrayList<Issue>();

        for (Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes) {
            try {
                if (beacon3100DeviceType.updateBufferSizeForAllEventLogs(new Unsigned32(bufferSize))) {
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    info.append("Set event logs buffer size ").append(bufferSize).append(" to deviceType: ").append(beacon3100DeviceType.getName()).append(" [").append(beacon3100DeviceType.getId()).append("]\n");
                } else {
                    String msg = "Could not set event logs buffer size to deviceType: " + beacon3100DeviceType.getName() + " [" + beacon3100DeviceType.getId() + "]";
                    info.append(msg).append("\n");
                    issues.add(issueFactory.createProblem(pendingMessage, msg));
                }
            } catch (Exception ex) {
                String msg = "Could not set event logs buffer size to deviceType: " + beacon3100DeviceType.getName() + " [" + beacon3100DeviceType.getId() + "] " + ex.getMessage();
                info.append(msg).append("\n");
                issues.add(issueFactory.createProblem(pendingMessage, msg));
            }
        }

        if (issues.size() > 0) {
            collectedMessage.setFailureInformation(ResultType.ConfigurationError, issues);
        }
        collectedMessage.setDeviceProtocolInformation(info.toString());

        return collectedMessage;
    }

    public CollectedMessage setBufferForSpecificLoadProfile(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getValue();
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getValue());

        Set<Beacon3100DeviceType> beacon3100DeviceTypes = MasterDataAnalyser.getDeviceTypesInBeaconMasterdata(this.getDeviceTypeManager());

        final Set<Beacon3100DeviceType> deviceTypesToUpdate = new HashSet<>();

        for (final Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes) {
            if (beacon3100DeviceType.updateBufferSizeForLoadProfiles(ObisCode.fromString(obisCode), new Unsigned32(bufferSize))) {
                deviceTypesToUpdate.add(beacon3100DeviceType);
            }
        }

        if (deviceTypesToUpdate.size() > 0) {
            for (final Beacon3100DeviceType deviceType : deviceTypesToUpdate) {
                this.updateDeviceTypeForNewFW(deviceType);
            }

            final String protocolInformation = new StringBuilder("Updated buffer size for load profile [").append(obisCode).append("] to [").append(bufferSize).append("] for device types [").append(deviceTypesToUpdate).append("]").toString();
            collectedMessage.setDeviceProtocolInformation(protocolInformation);
        } else {
            final String protocolInformation = new StringBuilder("Did not update buffer size for load profile [").append(obisCode).append("] to [").append(bufferSize).append("], no device types define it.").toString();
            collectedMessage.setDeviceProtocolInformation(protocolInformation);
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllLoadProfiles(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getValue());

        List<Beacon3100DeviceType> beacon3100DeviceTypes = newMasterDataSerializer().getDeviceTypes((int) pendingMessage.getDeviceId(), readOldObisCodes());

        StringBuilder info = new StringBuilder();
        List<Issue> issues = new ArrayList<Issue>();

        for (Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes) {
            try {
                if (beacon3100DeviceType.updateBufferSizeForAllLoadProfiles(new Unsigned32(bufferSize))) {
                    info.append("Set load profiles buffer size ").append(bufferSize).append(" to deviceType: ").append(beacon3100DeviceType.getName()).append(" [").append(beacon3100DeviceType.getId()).append("]\n");
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                } else {
                    String msg = "Could not set load profiles buffer size to deviceType: " + beacon3100DeviceType.getName() + " [" + beacon3100DeviceType.getId() + "]";
                    info.append(msg).append("\n");
                    issues.add(issueFactory.createProblem(pendingMessage, msg));
                }
            } catch (Exception ex) {
                String msg = "Could not set buffer size to deviceType: " + beacon3100DeviceType.getName() + " [" + beacon3100DeviceType.getId() + "] " + ex.getMessage();
                info.append(msg).append("\n");
                issues.add(issueFactory.createProblem(pendingMessage, msg));
            }
        }

        if (issues.size() > 0) {
            collectedMessage.setFailureInformation(ResultType.ConfigurationError, issues);
        }
        collectedMessage.setDeviceProtocolInformation(info.toString());

        return collectedMessage;
    }

    private boolean readOldObisCodes() {
        return ((Beacon3100Properties) getProtocol().getDlmsSessionProperties()).getReadOldObisCodes();
    }
}