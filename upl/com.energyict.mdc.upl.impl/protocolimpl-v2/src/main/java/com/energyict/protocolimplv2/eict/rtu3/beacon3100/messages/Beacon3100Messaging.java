package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages;

import com.energyict.cbo.*;
import com.energyict.cpo.BusinessObject;
import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.ImageTransfer.RandomAccessFileImageBlockSupplier;
import com.energyict.dlms.cosem.methods.NetworkInterfaceType;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Group;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.*;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects.BroadcastUpgrade;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects.DeviceInfoSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSync;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.AuthenticationMechanism;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.util.function.Consumer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 22/06/2015 - 9:53
 */
public class Beacon3100Messaging extends AbstractMessageExecutor implements DeviceMessageSupport {

    private static final ObisCode MULTICAST_FIRMWARE_UPGRADE_OBISCODE = ObisCode.fromString("0.0.44.0.128.255");
    private static final ObisCode MULTICAST_METER_PROGRESS = ProtocolTools.setObisCodeField(MULTICAST_FIRMWARE_UPGRADE_OBISCODE, 1, (byte) (-1 * ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS));
    private final static List<DeviceMessageSpec> supportedMessages;
    private static final String TEMP_DIR = "java.io.tmpdir";
    private static final ObisCode DEVICE_NAME_OBISCODE = ObisCode.fromString("0.0.128.0.9.255");
    private static final String SEPARATOR = ";";
    private static final String SEPARATOR2 = ",";
    /**
     * We lock the critical section where we write the firmware file, making sure that we don't corrupt it.
     */
    private static final Lock firmwareFileLock = new ReentrantLock();

    static {
        supportedMessages = new ArrayList<>();
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);

        supportedMessages.add(DeviceActionMessage.SyncMasterdataForDC);
        supportedMessages.add(DeviceActionMessage.SyncDeviceDataForDC);
        supportedMessages.add(DeviceActionMessage.PauseDCScheduler);
        supportedMessages.add(DeviceActionMessage.ResumeDCScheduler);
        supportedMessages.add(DeviceActionMessage.SyncOneConfigurationForDC);
        supportedMessages.add(DeviceActionMessage.TRIGGER_PRELIMINARY_PROTOCOL);

        supportedMessages.add(PLCConfigurationDeviceMessage.PingMeter);

        // supportedMessages.add(FirmwareDeviceMessage.BroadcastFirmwareUpgrade);
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER);
        supportedMessages.add(FirmwareDeviceMessage.DataConcentratorMulticastFirmwareUpgrade);
        supportedMessages.add(FirmwareDeviceMessage.ReadMulticastProgress);
        supportedMessages.add(FirmwareDeviceMessage.TRANSFER_SLAVE_FIRMWARE_FILE_TO_DATA_CONCENTRATOR);
        supportedMessages.add(FirmwareDeviceMessage.CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);
        supportedMessages.add(FirmwareDeviceMessage.START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);

        supportedMessages.add(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
        supportedMessages.add(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION);
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD);

        supportedMessages.add(UplinkConfigurationDeviceMessage.EnableUplinkPing);
        supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress);
        supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingInterval);
        supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout);
        //supportedMessages.add(PPPConfigurationDeviceMessage.SetPPPIdleTime);
        //supportedMessages.add(NetworkConnectivityMessage.PreferGPRSUpstreamCommunication);
        supportedMessages.add(NetworkConnectivityMessage.EnableModemWatchdog);
        supportedMessages.add(NetworkConnectivityMessage.SetModemWatchdogParameters2);
        supportedMessages.add(NetworkConnectivityMessage.SetPrimaryDNSAddress);
        supportedMessages.add(NetworkConnectivityMessage.SetSecondaryDNSAddress);
        supportedMessages.add(NetworkConnectivityMessage.EnableNetworkInterfaces);
        supportedMessages.add(NetworkConnectivityMessage.SetHttpPort);
        supportedMessages.add(NetworkConnectivityMessage.SetHttpsPort);
        supportedMessages.add(ConfigurationChangeDeviceMessage.EnableGzipCompression);
        supportedMessages.add(ConfigurationChangeDeviceMessage.EnableSSL);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SetAuthenticationMechanism);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SetMaxLoginAttempts);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SetLockoutDuration);
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION);
        supportedMessages.add(AlarmConfigurationMessage.ENABLE_EVENT_NOTIFICATIONS);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SetDeviceName);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SetNTPAddress);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SyncNTPServer);
        supportedMessages.add(DeviceActionMessage.RebootApplication);
        supportedMessages.add(FirewallConfigurationMessage.ActivateFirewall);
        supportedMessages.add(FirewallConfigurationMessage.DeactivateFirewall);
        supportedMessages.add(FirewallConfigurationMessage.ConfigureFWGPRS);
        supportedMessages.add(FirewallConfigurationMessage.ConfigureFWLAN);
        supportedMessages.add(FirewallConfigurationMessage.ConfigureFWWAN);
        supportedMessages.add(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD1);
        supportedMessages.add(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD2);
        supportedMessages.add(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD);
        supportedMessages.add(SecurityMessage.IMPORT_CLIENT_CERTIFICATE);
        supportedMessages.add(SecurityMessage.REMOVE_CLIENT_CERTIFICATE);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetSecurityLevel);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetRoutingConfiguration);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetPathDiscoveryTime);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMetricType);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetPanId);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetTMRTTL);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxFrameRetries);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMacA);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMacK);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinimumCWAttempts);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxBe);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxCSMABackOff);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinBe);

        supportedMessages.add(PLCConfigurationDeviceMessage.SetAutomaticRouteManagement);
        //supportedMessages.add(PLCConfigurationDeviceMessage.EnableKeepAlive);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveScheduleInterval);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveBucketSize);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetMinInactiveMeterTime);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxInactiveMeterTime);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveRetries);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveTimeout);
        supportedMessages.add(PLCConfigurationDeviceMessage.EnableG3PLCInterface);
        supportedMessages.add(PLCConfigurationDeviceMessage.KickMeter);
        supportedMessages.add(PLCConfigurationDeviceMessage.AddMetersToBlackList);
        supportedMessages.add(PLCConfigurationDeviceMessage.RemoveMetersFromBlackList);
        supportedMessages.add(PLCConfigurationDeviceMessage.PathRequestWithTimeout);
    }

    private MasterDataSync masterDataSync;
    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor = null;

    public Beacon3100Messaging(Beacon3100 protocol) {
        super(protocol);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.broadcastEncryptionKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.passwordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.broadcastAuthenticationKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newAuthenticationKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newPasswordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newEncryptionKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastDevicesGroupAttributeName)) {
            return DeviceInfoSerializer.serializeDeviceInfo(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastInitialTimeBetweenBlocksAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.timeout)) {
            return String.valueOf(((TimeDuration) messageAttribute).getMilliSeconds()); //Return value in ms
        } else if (propertySpec.getName().equals(DeviceMessageConstants.modemWatchdogInterval)
                || propertySpec.getName().equals(DeviceMessageConstants.modemWatchdogInitialDelay)
                || propertySpec.getName().equals(DeviceMessageConstants.PPPDaemonResetThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.modemResetThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.systemRebootThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds()); //Return value in seconds
        } else if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateUserFileAttributeName)) {
            final UserFile userFile = (UserFile) messageAttribute;
            final File tempFile = this.writeToTempDirectory(userFile);

            return tempFile.getAbsolutePath();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.deviceGroupAttributeName)) {
            Group group = (Group) messageAttribute;
            StringBuilder macAddresses = new StringBuilder();
            for (BusinessObject businessObject : group.getMembers()) {
                if (businessObject instanceof Device) {
                    Device device = (Device) businessObject;
                    String callHomeId = device.getProtocolProperties().getTypedProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "");
                    if (!callHomeId.isEmpty()) {
                        if (macAddresses.length() != 0) {
                            macAddresses.append(SEPARATOR);
                        }
                        macAddresses.append(callHomeId);

                        //Also add the java protocol class of the device, for the TRIGGER_PRELIMINARY_PROTOCOL message only
                        if (offlineDeviceMessage.getSpecification().equals(DeviceActionMessage.TRIGGER_PRELIMINARY_PROTOCOL)) {
                            macAddresses.append(SEPARATOR2).append(device.getDeviceProtocolPluggableClass().getJavaClassName());
                        }
                    }
                } else {
                    //TODO throw proper exception
                }
            }
            return macAddresses.toString();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.DelayAfterLastBlock)
                || propertySpec.getName().equals(DeviceMessageConstants.DelayPerBlock)
                || propertySpec.getName().equals(DeviceMessageConstants.DelayBetweenBlockSentFast)
                || propertySpec.getName().equals(DeviceMessageConstants.DelayBetweenBlockSentSlow)) {
            return String.valueOf(((TimeDuration) messageAttribute).getMilliSeconds());
        } else {
            return messageAttribute.toString();     //Works for BigDecimal, boolean and (hex)string property specs
        }
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        if (deviceMessage.getSpecification().equals(DeviceActionMessage.SyncMasterdataForDC)) {
            return MasterDataSerializer.serializeMasterData(offlineDevice.getId());
        } else if (deviceMessage.getSpecification().equals(DeviceActionMessage.SyncDeviceDataForDC)) {
            return MasterDataSerializer.serializeMeterDetails(offlineDevice.getId());
        } else if (deviceMessage.getSpecification().equals(DeviceActionMessage.SyncOneConfigurationForDC)) {
            int configId = ((BigDecimal) deviceMessage.getAttributes().get(0).getValue()).intValue();
            return MasterDataSerializer.serializeMasterDataForOneConfig(configId);
        } else if (deviceMessage.getSpecification().equals(FirmwareDeviceMessage.DataConcentratorMulticastFirmwareUpgrade)) {
            return MulticastSerializer.serialize(offlineDevice, deviceMessage);
        } else if (deviceMessage.getSpecification().equals(FirmwareDeviceMessage.CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES)) {
            return MulticastSerializer.serialize(offlineDevice, deviceMessage);
        } else {
            return "";
        }
    }

    /**
     * Writes the given {@link UserFile} to the temp directory.
     *
     * @param userFile The user file to write to the temp.
     * @return The {@link File} that was written.
     */
    private final File writeToTempDirectory(final UserFile userFile) {
        final File tempDirectory = new File(System.getProperty(TEMP_DIR));
        final String fileName = new StringBuilder("beacon-3100-firmware-").append(userFile.getId()).toString();

        try {
            firmwareFileLock.lock();

            final File tempFile = new File(tempDirectory, fileName);

            if (tempFile.exists()) {
                if (this.getLogger().isLoggable(Level.INFO)) {
                    this.getLogger().log(Level.INFO, "Already have a file called [" + tempFile + "], checking file size.");
                }

                if (tempFile.length() != userFile.getFileSize()) {
                    if (this.getLogger().isLoggable(Level.INFO)) {
                        this.getLogger().log(Level.INFO, "File size differs for file [" + tempFile + "], deleting.");
                    }

                    final boolean deleted = tempFile.delete();

                    if (!deleted) {
                        throw new IllegalStateException("Could not delete file : [" + tempFile + "] : delete() returns false !");
                    }
                }
            }

            if (!tempFile.exists()) {
                if (this.getLogger().isLoggable(Level.INFO)) {
                    this.getLogger().log(Level.INFO, "Copying user file to [" + tempFile + "].");
                }

                final boolean created = tempFile.createNewFile();

                if (!created) {
                    throw new IllegalStateException("Could not create temporary file [" + tempFile + "] : create() returns false !");
                }

                try (final OutputStream outStream = new FileOutputStream(tempFile)) {
                    userFile.processFileAsStream(new Consumer<InputStream>() {
                        @Override
                        public final void accept(final InputStream inStream) {
                            final byte[] buffer = new byte[2048];

                            try {
                                int bytesRead = inStream.read(buffer);

                                while (bytesRead != -1) {
                                    outStream.write(buffer, 0, bytesRead);
                                    bytesRead = inStream.read(buffer);
                                }

                                outStream.flush();
                            } catch (IOException e) {
                                if (getLogger().isLoggable(Level.WARNING)) {
                                    getLogger().log(Level.WARNING, "IO error while writing temporary file : [" + e.getMessage() + "]", e);
                                }

                                throw new IllegalStateException("IO error while writing temporary file : [" + e.getMessage() + "]", e);
                            }
                        }
                    });
                }
            }

            return tempFile;
        } catch (SQLException | IOException e) {
            if (getLogger().isLoggable(Level.WARNING)) {
                getLogger().log(Level.WARNING, "Error while writing temporary file : [" + e.getMessage() + "]", e);
            }

            throw new IllegalStateException("Error while writing temporary file : [" + e.getMessage() + "]", e);
        } finally {
            firmwareFileLock.unlock();
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                final CollectedMessage plcMessageResult = getPLCConfigurationDeviceMessageExecutor().executePendingMessage(pendingMessage, collectedMessage);
                if (plcMessageResult != null) {
                    collectedMessage = plcMessageResult;
                } else { // if it was not a PLC message
                    if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncMasterdataForDC)) {
                        collectedMessage = getMasterDataSync().syncMasterData(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncOneConfigurationForDC)) {
                        collectedMessage = getMasterDataSync().syncMasterData(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncDeviceDataForDC)) {
                        collectedMessage = getMasterDataSync().syncDeviceData(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.PauseDCScheduler)) {
                        setSchedulerState(SchedulerState.PAUSED);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ResumeDCScheduler)) {
                        setSchedulerState(SchedulerState.RUNNING);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS)) {
                        changeGPRSParameters(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.PingMeter)) {
                        collectedMessage = pingMeter(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.KickMeter)) {
                        collectedMessage = kickMeter(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.AddMetersToBlackList)) {
                        collectedMessage = addMetersToBlackList(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.RemoveMetersFromBlackList)) {
                        collectedMessage = removeMetersFromBlackList(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.BroadcastFirmwareUpgrade)) {
                        collectedMessage = new BroadcastUpgrade(this).broadcastFirmware(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER)) {
                        upgradeFirmware(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD1)) {
                        changePasswordUser1(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD2)) {
                        changePasswordUser2(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD)) {
                        changeUserPassword(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetHttpPort)) {
                        setHttpPort(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetHttpsPort)) {
                        setHttpsPort(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.EnableGzipCompression)) {
                        enableGzipCompression(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.EnableSSL)) {
                        enableSSL(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetAuthenticationMechanism)) {
                        setAuthenticationMechanism(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetMaxLoginAttempts)) {
                        setMaxLoginAttempts(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetLockoutDuration)) {
                        setLockoutDuration(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.EnableUplinkPing)) {
                        enableUplinkPing(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress)) {
                        writeUplinkPingDestinationAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingInterval)) {
                        writeUplinkPingInterval(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout)) {
                        writeUplinkPingTimeout(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableModemWatchdog)) {
                        enableModemWatchdog(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetModemWatchdogParameters2)) {
                        setModemWatchdogParameters(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetPrimaryDNSAddress)) {
                        writePrimaryDNSAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetSecondaryDNSAddress)) {
                        writeSecondaryDNSAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.ENABLE_EVENT_NOTIFICATIONS)) {
                        enableEventNotifications(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION)) {
                        configurePushEventNotification(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.REBOOT_DEVICE)) {
                        rebootDevice(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.RebootApplication)) {
                        rebootApplication(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetDeviceName)) {
                        setDeviceName(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetNTPAddress)) {
                        setNTPAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SyncNTPServer)) {
                        syncNTPServer(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL)) {
                        changeDlmAuthLevel(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION)) {
                        activateDlmsEncryption(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS)) {
                        changeAuthKey(pendingMessage);
/*                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.IMPORT_CLIENT_CERTIFICATE)) {
                        importClientCertificate(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.REMOVE_CLIENT_CERTIFICATE)) {
                        removeClientCertificate(pendingMessage);*/
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS)) {
                        changeEncryptionKey(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD)) {
                        changeHlsSecret(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ActivateFirewall)) {
                        activateFirewall(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.DeactivateFirewall)) {
                        deactivateFirewall(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWGPRS)) {
                        configureFWGPRS(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWLAN)) {
                        configureFWLAN(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWWAN)) {
                        configureFWWAN(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.TRIGGER_PRELIMINARY_PROTOCOL)) {

                        String[] macAddressesAndProtocols = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceGroupAttributeName).getDeviceMessageAttributeValue().split(SEPARATOR);

                        for (String macAddressAndProtocol : macAddressesAndProtocols) {
                            String[] split = macAddressAndProtocol.split(SEPARATOR2);
                            if (split.length == 2) {
                                String macAddressHex = split[0];
                                String protocolName = split[1];
                                try {
                                    this.triggerPreliminaryProtocol(macAddressHex, protocolName);
                                } catch (DataAccessResultException e) {
                                    //Log the error, but continue with the next meters
                                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                                    String errorMsg = "Error while triggering action for meter '" + macAddressHex + "': " + e.getMessage();
                                    collectedMessage.setDeviceProtocolInformation(errorMsg);
                                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
                                }
                            }
                        }

                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.DataConcentratorMulticastFirmwareUpgrade)) {
                        collectedMessage = dcMulticastUpgrade(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.ReadMulticastProgress)) {
                        collectedMessage = readMulticastProgress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableNetworkInterfaces)) {
                        enableNetworkInterfaces(pendingMessage);
                    } else {   //Unsupported message
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
                        collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                    }
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException | NullPointerException | ApplicationException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.toString());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            } finally {
                result.addCollectedMessage(collectedMessage);
            }
        }

        return result;
    }

    /**
     * Let the Beacon do a multicast firmware upgrade to a number of AM540 slave devices.
     * https://confluence.eict.vpdc/display/G3IntBeacon3100/Meter+multicast+upgrade
     */
    private CollectedMessage dcMulticastUpgrade(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {

        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getDeviceMessageAttributeValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue();

        String unicastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, UnicastClientWPort).getDeviceMessageAttributeValue();
        String broadcastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, BroadcastClientWPort).getDeviceMessageAttributeValue();
        String multicastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, MulticastClientWPort).getDeviceMessageAttributeValue();
        String logicalDeviceLSap = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, LogicalDeviceLSap).getDeviceMessageAttributeValue();
        String securityLevelUnicast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityLevelUnicast).getDeviceMessageAttributeValue();
        String securityLevelBroadcast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityLevelBroadcast).getDeviceMessageAttributeValue();
        String securityPolicyBroadcast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityPolicyBroadcast).getDeviceMessageAttributeValue();
        String delayAfterLastBlock = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayAfterLastBlock).getDeviceMessageAttributeValue();
        String delayPerBlock = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayPerBlock).getDeviceMessageAttributeValue();
        String delayBetweenBlockSentFast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayBetweenBlockSentFast).getDeviceMessageAttributeValue();
        String delayBetweenBlockSentSlow = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayBetweenBlockSentSlow).getDeviceMessageAttributeValue();
        String blocksPerCycle = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, BlocksPerCycle).getDeviceMessageAttributeValue();
        String maxCycles = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, MaxCycles).getDeviceMessageAttributeValue();
        String requestedBlockSize = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, RequestedBlockSize).getDeviceMessageAttributeValue();
        String padLastBlock = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, PadLastBlock).getDeviceMessageAttributeValue();
        String useTransferredBlockStatus = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, UseTransferredBlockStatus).getDeviceMessageAttributeValue();

        ArrayList<MulticastProperty> multicastProperties = new ArrayList<>();
        multicastProperties.add(new MulticastProperty("UnicastClientWPort", unicastClientWPort));
        multicastProperties.add(new MulticastProperty("BroadcastClientWPort", broadcastClientWPort));
        multicastProperties.add(new MulticastProperty("MulticastClientWPort", multicastClientWPort));
        multicastProperties.add(new MulticastProperty("LogicalDeviceLSap", logicalDeviceLSap));
        multicastProperties.add(new MulticastProperty("SecurityLevelUnicast", securityLevelUnicast));
        multicastProperties.add(new MulticastProperty("SecurityLevelBroadcast", securityLevelBroadcast));
        multicastProperties.add(new MulticastProperty("SecurityPolicyBroadcast", securityPolicyBroadcast));
        multicastProperties.add(new MulticastProperty("DelayAfterLastBlock", delayAfterLastBlock));
        multicastProperties.add(new MulticastProperty("DelayPerBlock", delayPerBlock));
        multicastProperties.add(new MulticastProperty("DelayBetweenBlockSentFast", delayBetweenBlockSentFast));
        multicastProperties.add(new MulticastProperty("DelayBetweenBlockSentSlow", delayBetweenBlockSentSlow));
        multicastProperties.add(new MulticastProperty("BlocksPerCycle", blocksPerCycle));
        multicastProperties.add(new MulticastProperty("MaxCycles", maxCycles));
        multicastProperties.add(new MulticastProperty("RequestedBlockSize", requestedBlockSize));
        multicastProperties.add(new MulticastProperty("PadLastBlock", padLastBlock));
        multicastProperties.add(new MulticastProperty("UseTransferredBlockStatus", useTransferredBlockStatus));

        MulticastProtocolConfiguration protocolConfiguration;
        try {
            final JSONObject jsonObject = new JSONObject(pendingMessage.getPreparedContext());  //This context field contains the serialized version of the protocol configuration
            protocolConfiguration = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), MulticastProtocolConfiguration.class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }
        protocolConfiguration.getMulticastProperties().addAll(multicastProperties);

        ImageTransfer it = getCosemObjectFactory().getImageTransfer(MULTICAST_FIRMWARE_UPGRADE_OBISCODE);

        //Write the full description of all AM540 slaves that needs to be upgraded using the DC multicast
        it.writeMulticastProtocolConfiguration(protocolConfiguration.toStructure());

        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(60);
        it.setDelayBeforeSendingBlocks(5000);

        try (final RandomAccessFile file = new RandomAccessFile(new File(filePath), "r")) {
            it.upgrade(new RandomAccessFileImageBlockSupplier(file), false, imageIdentifier, true);
            it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
            it.imageActivation();
        } catch (DataAccessResultException e) {
            if (isTemporaryFailure(e)) {
                getProtocol().getLogger().log(Level.INFO, "Received 'temporary failure', meaning that the multicast upgrade will start. Moving on.");
            } else {
                throw e;
            }
        }

        return collectedMessage;
    }



    private CollectedMessage configurePartialMulticastBlockTransfer(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {

        String skipStepEnable = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SkipStepEnable).getDeviceMessageAttributeValue();
        String skipStepVerify = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SkipStepVerify).getDeviceMessageAttributeValue();
        String skipStepActivate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SkipStepActivate).getDeviceMessageAttributeValue();
        String unicastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, UnicastClientWPort).getDeviceMessageAttributeValue();
        String multicastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, MulticastClientWPort).getDeviceMessageAttributeValue();
        String unicastFrameCounterType = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, UnicastFrameCounterType).getDeviceMessageAttributeValue();
        String timeZone = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, TimeZone).getDeviceMessageAttributeValue();
        String securityLevelMulticast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityLevelMulticast).getDeviceMessageAttributeValue();
        String securityPolicyMulticastV0 = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityPolicyMulticastV0).getDeviceMessageAttributeValue();
        String delayBetweenBlockSentFast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayBetweenBlockSentFast).getDeviceMessageAttributeValue();

        ArrayList<MulticastProperty> multicastProperties = new ArrayList<>();
        multicastProperties.add(new MulticastProperty("SkipStepEnable", skipStepEnable));
        multicastProperties.add(new MulticastProperty("SkipStepVerify", skipStepVerify));
        multicastProperties.add(new MulticastProperty("SkipStepActivate", skipStepActivate));
        multicastProperties.add(new MulticastProperty("UnicastClientWPort", unicastClientWPort));
        multicastProperties.add(new MulticastProperty("MulticastClientWPort", multicastClientWPort));
        multicastProperties.add(new MulticastProperty("UnicastFrameCounterType", unicastFrameCounterType));
        multicastProperties.add(new MulticastProperty("TimeZone", timeZone));
        multicastProperties.add(new MulticastProperty("SecurityLevelMulticast", securityLevelMulticast));
        multicastProperties.add(new MulticastProperty("SecurityPolicyMulticastV0", securityPolicyMulticastV0));
        multicastProperties.add(new MulticastProperty("DelayBetweenBlockSentFast", delayBetweenBlockSentFast));

        MulticastProtocolConfiguration protocolConfiguration;
        try {
            final JSONObject jsonObject = new JSONObject(pendingMessage.getPreparedContext());  //This context field contains the serialized version of the protocol configuration
            protocolConfiguration = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), MulticastProtocolConfiguration.class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }
        protocolConfiguration.getMulticastProperties().addAll(multicastProperties);

        ImageTransfer it = getCosemObjectFactory().getImageTransfer(MULTICAST_FIRMWARE_UPGRADE_OBISCODE);

        //Write the full description of all AM540 slaves that needs to be upgraded using the DC multicast
        it.writeMulticastProtocolConfiguration(protocolConfiguration.toStructure());

        return collectedMessage;
    }

    private CollectedMessage transferSlaveFirmwareFileToDC(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {

        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getDeviceMessageAttributeValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue();

        ImageTransfer it = getCosemObjectFactory().getImageTransfer(MULTICAST_FIRMWARE_UPGRADE_OBISCODE);

        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(60);
        it.setDelayBeforeSendingBlocks(5000);

        try (final RandomAccessFile file = new RandomAccessFile(new File(filePath), "r")) {
            it.initializeAndTransferBlocks(new RandomAccessFileImageBlockSupplier(file), false, imageIdentifier);
        } catch (DataAccessResultException e) {
            if (isTemporaryFailure(e)) {
                getProtocol().getLogger().log(Level.INFO, "Received 'temporary failure', meaning that the multicast upgrade will start. Moving on.");
            } else {
                throw e;
            }
        }

        return collectedMessage;
    }

    private CollectedMessage startMulticastBlockTransferToSlaveDevices(CollectedMessage collectedMessage) throws IOException {

        ImageTransfer it = getCosemObjectFactory().getImageTransfer(MULTICAST_FIRMWARE_UPGRADE_OBISCODE);

        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(60);
        it.setDelayBeforeSendingBlocks(5000);

        try  {//by activating the image we trigger the multicast block transfer to slave devices
            it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
            it.imageVerification();
            it.imageActivation();
        } catch (DataAccessResultException e) {
            if (isTemporaryFailure(e)) {
                getProtocol().getLogger().log(Level.INFO, "Received 'temporary failure', meaning that the multicast upgrade will start. Moving on.");
            } else {
                throw e;
            }
        }

        return collectedMessage;
    }

    /**
     * Read out the progress of the multicast FW upgrade.
     * This contains information on all AM540 slave devices that are currently being upgraded.
     * <p/>
     * Note that this information will be stored on the proper AM540 slave devices in EIServer, as register 0.3.44.0.128.255
     */
    private CollectedMessage readMulticastProgress(OfflineDeviceMessage pendingMessage) throws IOException {
        Structure structure = getCosemObjectFactory().getImageTransfer(MULTICAST_FIRMWARE_UPGRADE_OBISCODE).readMulticastUpgradeProgress();
        if (structure.nrOfDataTypes() != 3) {
            throw new ProtocolException("The structure describing the upgrade_progress attribute should contain 3 elements");
        }

        List<CollectedRegister> collectedRegisters = new ArrayList<>();

        //First 2 fields of the structure describe the general progress state and will be stored as a register on the Beacon device.
        int upgradeState = structure.getDataType(0).intValue();
        OctetString upgradeProgressInfo = structure.getDataType(1).getOctetString();
        String description = MulticastUpgradeState.fromValue(upgradeState).getDescription();
        description = description + (upgradeProgressInfo == null ? "" : (". " + upgradeProgressInfo.stringValue()));
        RegisterValue registerValue = new RegisterValue(MULTICAST_METER_PROGRESS, new Quantity(upgradeState, Unit.get(BaseUnit.UNITLESS)), null, null, new Date(), new Date(), 0, description);
        CollectedRegister beaconRegister = createCollectedRegister(registerValue, pendingMessage);
        collectedRegisters.add(beaconRegister);

        //The third element in the structure is an array, describing the progress of each individual meter. These will be stored as a register value on the proper AM540 slave devices.
        AbstractDataType dataType = structure.getDataType(2);
        if (!dataType.isArray()) {
            throw new ProtocolException("The third element in the upgrade_progress structure should be an array");
        }
        Array meterProgresses = dataType.getArray();
        for (AbstractDataType meterProgress : meterProgresses) {
            StringBuilder meterProgressDescription = new StringBuilder();
            if (!meterProgress.isStructure() || meterProgress.getStructure().nrOfDataTypes() != 4) {
                throw new ProtocolException("The meter_progress should be a structure of 4 elements");
            }
            AbstractDataType dataType1 = meterProgress.getStructure().getDataType(0);
            if (!dataType1.isOctetString()) {
                throw new ProtocolException("The first element in the meter_progress structure should be an octetstring");
            }
            AbstractDataType dataType2 = meterProgress.getStructure().getDataType(1);
            if (!dataType2.isTypeEnum()) {
                throw new ProtocolException("The second element in the meter_progress structure should be an enum");
            }
            AbstractDataType dataType3 = meterProgress.getStructure().getDataType(2);
            if (!dataType3.isBitString()) {
                throw new ProtocolException("The third element in the meter_progress structure should be a bitstring");
            }
            AbstractDataType dataType4 = meterProgress.getStructure().getDataType(3);
            if (!dataType4.isOctetString()) {
                throw new ProtocolException("The fourth element in the meter_progress structure should be an octetstring");
            }
            String macAddress = ProtocolTools.getHexStringFromBytes(dataType1.getOctetString().toByteArray(), "");
            meterProgressDescription.append("Status: ");
            meterProgressDescription.append(MulticastMeterState.fromValue(dataType2.intValue()).getDescription());
            meterProgressDescription.append("\n\r ");

            meterProgressDescription.append("Transferred blocks: ");
            StringBuilder result = new StringBuilder();
            BitSet bitSet = dataType3.getBitString().asBitSet();
            for (int index = 0; index < bitSet.length(); index++) {
                result.append(bitSet.get(index) ? "1" : "0");
            }
            meterProgressDescription.append(result.toString());
            meterProgressDescription.append("\n\r ");

            meterProgressDescription.append("Info: ");
            meterProgressDescription.append(dataType4.getOctetString().stringValue());

            CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterDataIdentifierByObisCodeAndDevice(MULTICAST_METER_PROGRESS, new DialHomeIdDeviceIdentifier(macAddress)));
            deviceRegister.setCollectedData(new Quantity(dataType2.intValue(), Unit.get(BaseUnit.UNITLESS)), meterProgressDescription.toString());
            deviceRegister.setCollectedTimeStamps(new Date(), null, new Date());
            collectedRegisters.add(deviceRegister);
        }

        CollectedMessage result = createCollectedMessageWithRegisterData(pendingMessage, collectedRegisters);
        result.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return result;
    }

    /**
     * Trigger the preliminary protocol for a particular meter.
     *
     * @param macAddress   MAC address of the meter (hex).
     * @param protocolName The name of the protocol to run.
     * @throws IOException If an IO error occurs during the execution.
     */
    private final void triggerPreliminaryProtocol(final String macAddress, final String protocolName) throws IOException {
        if (getLogger().isLoggable(Level.INFO)) {
            getLogger().log(Level.INFO, "Triggering preliminary protocol for meter [" + macAddress + "], using protocol [" + protocolName + "]");
        }

        final byte[] mac = ParseUtils.hexStringToByteArray(macAddress);

        final ConcentratorSetup concentratorSetup = this.getCosemObjectFactory().getConcentratorSetup();
        concentratorSetup.triggerPreliminaryProtocol(mac, protocolName);

        if (getLogger().isLoggable(Level.INFO)) {
            getLogger().log(Level.INFO, "Triggered preliminary protocol for meter [" + macAddress + "], using protocol [" + protocolName + "]");
        }
    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new Beacon3100PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice());
        }
        return plcConfigurationDeviceMessageExecutor;
    }

    private void configureFWWAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getFirewallSetup().setWANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWLAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getFirewallSetup().setLANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWGPRS(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getFirewallSetup().setGPRSPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void deactivateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getFirewallSetup().deactivate();
    }

    private void activateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getFirewallSetup().activate();
    }

    private void changeGPRSParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getDeviceMessageAttributeValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getDeviceMessageAttributeValue();
        String apn = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, apnAttributeName).getDeviceMessageAttributeValue();
        writeGprsSettings(userName, password);
        if (apn != null) {
            getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
        }
    }

    private void writeGprsSettings(String userName, String password) throws IOException {
        PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
        pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
        if (userName != null) {
            pppat.setUserName(userName);
        }
        if (password != null) {
            pppat.setPassWord(password);
        }
        if ((userName != null) || (password != null)) {
            getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
        }
    }

    private CollectedMessage pingMeter(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        final String hexMacAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.macAddress).getDeviceMessageAttributeValue();
        final long timeoutInMillis = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.timeout).getDeviceMessageAttributeValue());

        try {
            ProtocolTools.getBytesFromHexString(hexMacAddress, "");
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw DeviceConfigurationException.invalidPropertyFormat("MAC address", hexMacAddress, "Should be 16 hex characters");
        }

        final long normalTimeout = getProtocol().getDlmsSessionProperties().getTimeout();
        final long fullRoundTripTimeout = timeoutInMillis + normalTimeout;
        getProtocol().getDlmsSession().getDLMSConnection().setTimeout(fullRoundTripTimeout);
        final int pingTime = getCosemObjectFactory().getG3NetworkManagement().pingNode(hexMacAddress, ((int) timeoutInMillis) / 1000);
        getProtocol().getDlmsSession().getDLMSConnection().setTimeout(normalTimeout);

        collectedMessage.setDeviceProtocolInformation(pingTime + " ms");
        return collectedMessage;
    }

    private CollectedMessage kickMeter(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String macAddress = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();

        final boolean result = getCosemObjectFactory().getG3NetworkManagement().detachNode(macAddress);

        if (!result) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            final String errorMsg = "The Beacon was not able to kick the meter from the network.";
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
        }

        return collectedMessage;
    }

    private CollectedMessage addMetersToBlackList(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        List<String> macAddresses = new ArrayList<>();

        for (String macAddress : pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().split(SEPARATOR)) {
            final String errorMsg = "MAC addresses should be a list of 16 hex characters, separated by a semicolon.";
            try {
                final byte[] macAddressBytes = ProtocolTools.getBytesFromHexString(macAddress, "");
                if (macAddressBytes.length != 8) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(errorMsg);
                    collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(pendingMessage, errorMsg));
                    return collectedMessage;
                }
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(errorMsg);
                collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(pendingMessage, errorMsg));
                return collectedMessage;
            }

            macAddresses.add(macAddress);
        }

        final boolean result = getCosemObjectFactory().getG3NetworkManagement().addToBlacklist(macAddresses);

        if (!result) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            final String errorMsg = "The Beacon was not able to add the meter(s) to the blacklist";
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
        }

        return collectedMessage;
    }

    private CollectedMessage removeMetersFromBlackList(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        List<String> macAddresses = new ArrayList<>();

        for (String macAddress : pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().split(SEPARATOR)) {
            final String errorMsg = "MAC addresses should be a list of 16 hex characters, separated by a semicolon.";
            try {
                final byte[] macAddressBytes = ProtocolTools.getBytesFromHexString(macAddress, "");
                if (macAddressBytes.length != 8) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(errorMsg);
                    collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(pendingMessage, errorMsg));
                    return collectedMessage;
                }
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(errorMsg);
                collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(pendingMessage, errorMsg));
                return collectedMessage;
            }

            macAddresses.add(macAddress);
        }

        final boolean result = getCosemObjectFactory().getG3NetworkManagement().removeFromBlacklist(macAddresses);

        if (!result) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            final String errorMsg = "The beacon was not able to add the meter(s) to the blacklist";
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
        }

        return collectedMessage;
    }

    private MasterDataSync getMasterDataSync() {
        if (masterDataSync == null) {
            masterDataSync = new MasterDataSync(this);
        }
        return masterDataSync;
    }

    private void setSchedulerState(SchedulerState state) throws IOException {
        getCosemObjectFactory().getScheduleManager().writeSchedulerState(state.toDLMSEnum());
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getDeviceMessageAttributeValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue(); // Will return empty string if the MessageAttribute could not be found

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();

        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(60);
        it.setDelayBeforeSendingBlocks(5000);

        try (final RandomAccessFile file = new RandomAccessFile(new File(filePath), "r")) {
            it.upgrade(new RandomAccessFileImageBlockSupplier(file), false, imageIdentifier, true);
            it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
            it.imageActivation();
        } catch (DataAccessResultException e) {
            if (isTemporaryFailure(e)) {
                getProtocol().getLogger().log(Level.INFO, "Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
            } else {
                throw e;
            }
        }
    }

    private boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }

    private void changeHlsSecret(OfflineDeviceMessage pendingMessage) throws IOException {
        String hex = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        final CosemObjectFactory cof = getCosemObjectFactory();
        cof.getAssociationLN().changeHLSSecret(ProtocolTools.getBytesFromHexString(hex, ""));
    }

    private void enableNetworkInterfaces(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isEthernetWanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ETHERNET_WAN).getDeviceMessageAttributeValue());
        boolean isEthernetLanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ETHERNET_LAN).getDeviceMessageAttributeValue());
        boolean isWirelessWanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.WIRELESS_WAN).getDeviceMessageAttributeValue());
        boolean isIp6_TunnelEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.IP6_TUNNEL).getDeviceMessageAttributeValue());
        boolean isPlc_NetworkEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.PLC_NETWORK).getDeviceMessageAttributeValue());
        boolean allInterfacesEnabled = isEthernetWanEnabled && isEthernetLanEnabled && isWirelessWanEnabled && isIp6_TunnelEnabled && isPlc_NetworkEnabled;

        Array interfacesArray = new Array();
        if (allInterfacesEnabled) {
            interfacesArray.addDataType(new TypeEnum(NetworkInterfaceType.ALL.getNetworkType()));
        } else {
            if (isEthernetWanEnabled) {
                interfacesArray.addDataType(new TypeEnum(NetworkInterfaceType.ETHERNET_WAN.getNetworkType()));
            }
            if (isEthernetLanEnabled) {
                interfacesArray.addDataType(new TypeEnum(NetworkInterfaceType.ETHERNET_LAN.getNetworkType()));
            }
            if (isWirelessWanEnabled) {
                interfacesArray.addDataType(new TypeEnum(NetworkInterfaceType.WIRELESS_WAN.getNetworkType()));
            }
            if (isIp6_TunnelEnabled) {
                interfacesArray.addDataType(new TypeEnum(NetworkInterfaceType.IP6_TUNNEL.getNetworkType()));
            }
            if (isPlc_NetworkEnabled) {
                interfacesArray.addDataType(new TypeEnum(NetworkInterfaceType.PLC_NETWORK.getNetworkType()));
            }
        }
        getCosemObjectFactory().getWebPortalConfig().enableInterfaces(interfacesArray);
    }

    protected void changeEncryptionKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String wrappedHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newWrappedEncryptionKeyAttributeName).getDeviceMessageAttributeValue();
        String plainHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newEncryptionKeyAttributeName).getDeviceMessageAttributeValue();
        String oldHexKey = ProtocolTools.getHexStringFromBytes(getProtocol().getDlmsSession().getProperties().getSecurityProvider().getGlobalKey(), "");

        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey, "")));
        encryptionKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(encryptionKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(plainHexKey, ""));

        //Reset frame counter, only if a different key has been written
        if (!oldHexKey.equalsIgnoreCase(plainHexKey)) {
            getProtocol().getDlmsSession().getAso().getSecurityContext().setFrameCounter(1);
        }
    }

    protected SecuritySetup getSecuritySetup() throws IOException {
        return getCosemObjectFactory().getSecuritySetup();
    }

    protected void changeAuthKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String wrappedHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName).getDeviceMessageAttributeValue();
        String plainHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newAuthenticationKeyAttributeName).getDeviceMessageAttributeValue();

        Array authenticationKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey, "")));
        authenticationKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(authenticationKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(plainHexKey, ""));
    }

    private void activateDlmsEncryption(OfflineDeviceMessage pendingMessage) throws IOException {
        getSecuritySetup().activateSecurity(new TypeEnum(getSingleIntegerAttribute(pendingMessage)));
    }

    private void changeDlmAuthLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        int newAuthLevel = getSingleIntegerAttribute(pendingMessage);

        AssociationLN associationLN = getCosemObjectFactory().getAssociationLN();
        AbstractDataType authMechanismName = associationLN.readAuthenticationMechanismName();
        if (authMechanismName.isOctetString()) {
            byte[] octets = ((OctetString) authMechanismName).getOctetStr();
            if (octets[octets.length - 1] != newAuthLevel) {
                octets[octets.length - 1] = (byte) newAuthLevel;
                associationLN.writeAuthenticationMechanismName(new OctetString(octets, 0));
            } else {
                throw new ProtocolException("New authenticationLevel is the same as the one that is already configured in the device, new level will not be written.");
            }
        } else if (authMechanismName.isStructure()) {
            Structure structure = (Structure) authMechanismName;
            Unsigned8 currentLevel = (Unsigned8) structure.getDataType(structure.nrOfDataTypes() - 1);
            if (currentLevel.intValue() != newAuthLevel) {
                structure.setDataType(structure.nrOfDataTypes() - 1, new Unsigned8(newAuthLevel));
                associationLN.writeAuthenticationMechanismName(structure);
            } else {
                throw new ProtocolException("New authenticationLevel is the same as the one that is already configured in the device, new level will not be written.");
            }
        } else {
            throw new ProtocolException("Returned AuthenticationMechanismName is not of the type OctetString or Structure, cannot write new value");
        }
    }

    private void setNTPAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String address = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getNTPServerAddress().writeNTPServerName(address);
    }

    private void syncNTPServer(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getNTPServerAddress().ntpSync();
    }

    private void setDeviceName(OfflineDeviceMessage pendingMessage) throws IOException {
        String name = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getData(DEVICE_NAME_OBISCODE).setValueAttr(OctetString.fromString(name));
    }

    private void rebootApplication(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getLifeCycleManagement().restartApplication();
    }

    private void rebootDevice(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getLifeCycleManagement().rebootDevice();
    }

    private void enableEventNotifications(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getBeaconEventPushNotificationConfig().enable(enable);
    }

    private void setModemWatchdogParameters(OfflineDeviceMessage pendingMessage) throws IOException {

        //Note that all values here are expressed in seconds
        int modemWatchdogInterval = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemWatchdogInterval).getDeviceMessageAttributeValue());
        int modemWatchdogInitialDelay = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemWatchdogInitialDelay).getDeviceMessageAttributeValue());
        int pppDaemonResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.PPPDaemonResetThreshold).getDeviceMessageAttributeValue());
        int modemResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemResetThreshold).getDeviceMessageAttributeValue());
        int systemRebootThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.systemRebootThreshold).getDeviceMessageAttributeValue());

        getCosemObjectFactory().getModemWatchdogConfiguration().writeExtendedConfigParameters(
                modemWatchdogInterval,
                modemWatchdogInitialDelay,
                pppDaemonResetThreshold,
                modemResetThreshold,
                systemRebootThreshold
        );
    }

    private void enableModemWatchdog(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getModemWatchdogConfiguration().enableWatchdog(enable);
    }

    private void configurePushEventNotification(OfflineDeviceMessage pendingMessage) throws IOException {
        String transportTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transportTypeAttributeName).getDeviceMessageAttributeValue();
        int transportType = AlarmConfigurationMessage.TransportType.valueOf(transportTypeString).getId();

        String destinationAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName).getDeviceMessageAttributeValue();

        String messageTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.messageTypeAttributeName).getDeviceMessageAttributeValue();
        int messageType = AlarmConfigurationMessage.MessageType.valueOf(messageTypeString).getId();

        getCosemObjectFactory().getBeaconEventPushNotificationConfig().writeSendDestinationAndMethod(transportType, destinationAddress, messageType);
    }

    private void writeUplinkPingTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer timeout = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getUplinkPingConfiguration().writeTimeout(timeout);
    }

    private void changePasswordUser1(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getWebPortalConfig().changeUser1Password(newPassword);
    }

    private void changePasswordUser2(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getWebPortalConfig().changeUser2Password(newPassword);
    }

    private void changeUserPassword(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.usernameAttributeName).getDeviceMessageAttributeValue();
        String newPassword = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.passwordAttributeName).getDeviceMessageAttributeValue();

        getCosemObjectFactory().getWebPortalConfig().changeUserPassword(userName, newPassword);
    }

    private void writeUplinkPingInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer interval = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getUplinkPingConfiguration().writeInterval(interval);
    }

    private void writeUplinkPingDestinationAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String destinationAddress = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getUplinkPingConfiguration().writeDestAddress(destinationAddress);
    }

    private void enableUplinkPing(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getUplinkPingConfiguration().enableUplinkPing(enable);
    }

    private int getSingleIntegerAttribute(OfflineDeviceMessage pendingMessage) {
        return Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
    }

    private void writePrimaryDNSAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String address = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getIPv4Setup().setPrimaryDNSAddress(address);
    }

    private void writeSecondaryDNSAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String address = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getIPv4Setup().setSecondaryDNSAddress(address);
    }

    private void setHttpPort(OfflineDeviceMessage pendingMessage) throws IOException {
        String httpPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SetHttpPortAttributeName).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getWebPortalConfig().setHttpPort(httpPort);
    }

    private void setHttpsPort(OfflineDeviceMessage pendingMessage) throws IOException {
        String httpsPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SetHttpsPortAttributeName).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getWebPortalConfig().setHttpsPort(httpsPort);
    }

    private void setMaxLoginAttempts(OfflineDeviceMessage pendingMessage) throws IOException {
        /*final long logAttempts = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_MAX_LOGIN_ATTEMPTS).getDeviceMessageAttributeValue());*/
        String logAttempts = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_MAX_LOGIN_ATTEMPTS).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getWebPortalConfig().setMaxLoginAttempts(logAttempts);
    }

    private void setLockoutDuration(OfflineDeviceMessage pendingMessage) throws IOException {
        String duration = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_LOCKOUT_DURATION).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getWebPortalConfig().setLockoutDuration(duration);
    }

    private void enableGzipCompression(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enableGzip = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ENABLE_GZIP_COMPRESSION).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getWebPortalConfig().enableGzipCompression(enableGzip);
    }

    private void enableSSL(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enableSSL = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.enableSSL).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getWebPortalConfig().enableSSL(enableSSL);
    }

    private void setAuthenticationMechanism(OfflineDeviceMessage pendingMessage) throws IOException {
        String authName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_AUTHENTICATION_MECHANISM).getDeviceMessageAttributeValue();
        int auth = AuthenticationMechanism.fromAuthName(authName);
        getCosemObjectFactory().getWebPortalConfig().setAuthenticationMechanism(auth);
    }

    /**
     * Returns the {@link Logger} instance to be used.
     *
     * @return The logger instance to be used.
     */
    private final Logger getLogger() {
        return this.getProtocol().getLogger();
    }
}
