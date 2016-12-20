package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages;

import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.CertificateAlias;
import com.energyict.cbo.CertificateWrapperId;
import com.energyict.cbo.Password;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.cpo.BusinessObject;
import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.ConcentratorSetup;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.FirewallSetup;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.ImageTransfer.RandomAccessFileImageBlockSupplier;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.methods.NetworkInterfaceType;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.encryption.asymetric.keyagreement.KeyAgreementImpl;
import com.energyict.encryption.asymetric.signature.ECDSASignatureImpl;
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.encryption.kdf.NIST_SP_800_56_KDF;
import com.energyict.mdw.core.CertificateWrapper;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.ECCCurve;
import com.energyict.mdw.core.Group;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.UserFile;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks.Beacon3100LogBookFactory;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.MulticastMeterState;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.MulticastProperty;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.MulticastProtocolConfiguration;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.MulticastSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.MulticastUpgradeState;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects.BroadcastUpgrade;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects.DeviceInfoSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSerializer;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataSync;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirewallConfigurationMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.UplinkConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.AuthenticationMechanism;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import com.energyict.util.function.Consumer;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.BlocksPerCycle;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.BroadcastClientWPort;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayAfterLastBlock;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayBetweenBlockSentFast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayBetweenBlockSentSlow;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayPerBlock;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.LogicalDeviceLSap;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MaxCycles;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MeterTimeZone;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastClientWPort;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.PadLastBlock;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.RequestedBlockSize;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelBroadcast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelMulticast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelUnicast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityPolicyBroadcast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityPolicyMulticastV0;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepActivate;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepEnable;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepVerify;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UnicastClientWPort;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UnicastFrameCounterType;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UseTransferredBlockStatus;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.certificateEntityAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.certificateIssuerAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.certificateTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.commonNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterSerialNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 22/06/2015 - 9:53
 */
public class Beacon3100Messaging extends AbstractMessageExecutor implements DeviceMessageSupport {

    private static final ObisCode MULTICAST_FIRMWARE_UPGRADE_OBISCODE = ObisCode.fromString("0.0.44.0.128.255");
    private static final ObisCode MULTICAST_METER_PROGRESS = ProtocolTools.setObisCodeField(MULTICAST_FIRMWARE_UPGRADE_OBISCODE, 1, (byte) (-1 * ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS));
    private static final String TEMP_DIR = "java.io.tmpdir";
    private static final ObisCode DEVICE_NAME_OBISCODE = ObisCode.fromString("0.0.128.0.9.255");
    private static final String SEPARATOR = ";";
    private static final String SEPARATOR2 = ",";

    /**
     * The set of supported messages (which, ironically, is not a Set).
     */
    private static final List<DeviceMessageSpec> SUPPORTED_MESSAGES = new ArrayList<>();

    /**
     * We lock the critical section where we write the firmware file, making sure that we don't corrupt it.
     */
    private static final Lock FIRMWARE_FILE_LOCK = new ReentrantLock();

    static {
        SUPPORTED_MESSAGES.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);

        SUPPORTED_MESSAGES.add(DeviceActionMessage.SyncMasterdataForDC);
        SUPPORTED_MESSAGES.add(DeviceActionMessage.SyncDeviceDataForDC);
        SUPPORTED_MESSAGES.add(DeviceActionMessage.PauseDCScheduler);
        SUPPORTED_MESSAGES.add(DeviceActionMessage.ResumeDCScheduler);
        SUPPORTED_MESSAGES.add(DeviceActionMessage.SyncOneConfigurationForDC);
        SUPPORTED_MESSAGES.add(DeviceActionMessage.TRIGGER_PRELIMINARY_PROTOCOL);

        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.PingMeter);

        // supportedMessages.add(FirmwareDeviceMessage.BroadcastFirmwareUpgrade);
        SUPPORTED_MESSAGES.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER);
        SUPPORTED_MESSAGES.add(FirmwareDeviceMessage.DataConcentratorMulticastFirmwareUpgrade);
        SUPPORTED_MESSAGES.add(FirmwareDeviceMessage.ReadMulticastProgress);
        SUPPORTED_MESSAGES.add(FirmwareDeviceMessage.TRANSFER_SLAVE_FIRMWARE_FILE_TO_DATA_CONCENTRATOR);
        SUPPORTED_MESSAGES.add(FirmwareDeviceMessage.CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);
        SUPPORTED_MESSAGES.add(FirmwareDeviceMessage.START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);

        SUPPORTED_MESSAGES.add(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
        SUPPORTED_MESSAGES.add(SecurityMessage.ACTIVATE_DLMS_SECURITY_VERSION1);
        SUPPORTED_MESSAGES.add(SecurityMessage.AGREE_NEW_ENCRYPTION_KEY);
        SUPPORTED_MESSAGES.add(SecurityMessage.AGREE_NEW_AUTHENTICATION_KEY);
        SUPPORTED_MESSAGES.add(SecurityMessage.CHANGE_SECURITY_SUITE);
        SUPPORTED_MESSAGES.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);
        SUPPORTED_MESSAGES.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
        SUPPORTED_MESSAGES.add(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD);
        SUPPORTED_MESSAGES.add(SecurityMessage.EXPORT_END_DEVICE_CERTIFICATE);
        SUPPORTED_MESSAGES.add(SecurityMessage.EXPORT_SUB_CA_CERTIFICATES);
        SUPPORTED_MESSAGES.add(SecurityMessage.EXPORT_ROOT_CA_CERTIFICATE);
        SUPPORTED_MESSAGES.add(SecurityMessage.IMPORT_CA_CERTIFICATE);
        SUPPORTED_MESSAGES.add(SecurityMessage.IMPORT_END_DEVICE_CERTIFICATE);
        SUPPORTED_MESSAGES.add(SecurityMessage.DELETE_CERTIFICATE_BY_SERIAL_NUMBER);
        SUPPORTED_MESSAGES.add(SecurityMessage.DELETE_CERTIFICATE_BY_TYPE);
        SUPPORTED_MESSAGES.add(SecurityMessage.GENERATE_KEY_PAIR);
        SUPPORTED_MESSAGES.add(SecurityMessage.GENERATE_CSR);

        SUPPORTED_MESSAGES.add(UplinkConfigurationDeviceMessage.EnableUplinkPing);
        SUPPORTED_MESSAGES.add(UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress);
        SUPPORTED_MESSAGES.add(UplinkConfigurationDeviceMessage.WriteUplinkPingInterval);
        SUPPORTED_MESSAGES.add(UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout);
        //supportedMessages.add(PPPConfigurationDeviceMessage.SetPPPIdleTime);
        //supportedMessages.add(NetworkConnectivityMessage.PreferGPRSUpstreamCommunication);
        SUPPORTED_MESSAGES.add(NetworkConnectivityMessage.EnableModemWatchdog);
        SUPPORTED_MESSAGES.add(NetworkConnectivityMessage.SetModemWatchdogParameters2);
        SUPPORTED_MESSAGES.add(NetworkConnectivityMessage.SetPrimaryDNSAddress);
        SUPPORTED_MESSAGES.add(NetworkConnectivityMessage.SetSecondaryDNSAddress);
        SUPPORTED_MESSAGES.add(NetworkConnectivityMessage.EnableNetworkInterfaces);
        SUPPORTED_MESSAGES.add(NetworkConnectivityMessage.SetHttpPort);
        SUPPORTED_MESSAGES.add(NetworkConnectivityMessage.SetHttpsPort);
        SUPPORTED_MESSAGES.add(ConfigurationChangeDeviceMessage.EnableGzipCompression);
        SUPPORTED_MESSAGES.add(ConfigurationChangeDeviceMessage.EnableSSL);
        SUPPORTED_MESSAGES.add(ConfigurationChangeDeviceMessage.SetAuthenticationMechanism);
        SUPPORTED_MESSAGES.add(ConfigurationChangeDeviceMessage.SetMaxLoginAttempts);
        SUPPORTED_MESSAGES.add(ConfigurationChangeDeviceMessage.SetLockoutDuration);
        SUPPORTED_MESSAGES.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION);
        SUPPORTED_MESSAGES.add(AlarmConfigurationMessage.ENABLE_EVENT_NOTIFICATIONS);
        SUPPORTED_MESSAGES.add(ConfigurationChangeDeviceMessage.SetDeviceName);
        SUPPORTED_MESSAGES.add(ConfigurationChangeDeviceMessage.SetNTPAddress);
        SUPPORTED_MESSAGES.add(ConfigurationChangeDeviceMessage.SyncNTPServer);
        SUPPORTED_MESSAGES.add(DeviceActionMessage.RebootApplication);
        SUPPORTED_MESSAGES.add(FirewallConfigurationMessage.ActivateFirewall);
        SUPPORTED_MESSAGES.add(FirewallConfigurationMessage.DeactivateFirewall);
        SUPPORTED_MESSAGES.add(FirewallConfigurationMessage.ConfigureFWGPRS);
        SUPPORTED_MESSAGES.add(FirewallConfigurationMessage.ConfigureFWLAN);
        SUPPORTED_MESSAGES.add(FirewallConfigurationMessage.ConfigureFWWAN);
        SUPPORTED_MESSAGES.add(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD1);
        SUPPORTED_MESSAGES.add(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD2);
        SUPPORTED_MESSAGES.add(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetSecurityLevel);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetRoutingConfiguration);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetPathDiscoveryTime);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMetricType);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetPanId);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetTMRTTL);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMaxFrameRetries);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMacA);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMacK);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMinimumCWAttempts);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMaxBe);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMaxCSMABackOff);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetMinBe);

        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.SetAutomaticRouteManagement);
        //supportedMessages.add(PLCConfigurationDeviceMessage.EnableKeepAlive);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveScheduleInterval);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveBucketSize);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetMinInactiveMeterTime);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxInactiveMeterTime);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveRetries);
        //supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveTimeout);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.EnableG3PLCInterface);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.KickMeter);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.AddMetersToBlackList);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.RemoveMetersFromBlackList);
        SUPPORTED_MESSAGES.add(PLCConfigurationDeviceMessage.PathRequestWithTimeout);

        // Logbook resets.
        SUPPORTED_MESSAGES.add(LogBookDeviceMessage.ResetMainLogbook);
        SUPPORTED_MESSAGES.add(LogBookDeviceMessage.ResetSecurityLogbook);
        SUPPORTED_MESSAGES.add(LogBookDeviceMessage.ResetCoverLogbook);
        SUPPORTED_MESSAGES.add(LogBookDeviceMessage.ResetCommunicationLogbook);
        SUPPORTED_MESSAGES.add(LogBookDeviceMessage.ResetVoltageCutLogbook);
    }

    private MasterDataSync masterDataSync;
    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor = null;

    public Beacon3100Messaging(Beacon3100 protocol) {
        super(protocol);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return SUPPORTED_MESSAGES;
    }

    @Override
    @SuppressWarnings("rawtypes")
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
        } else if (propertySpec.getName().equals(DeviceMessageConstants.certificateAliasAttributeName)) {
            //Load the certificate with that alias from the EIServer DLMS trust store and encode it.
            String alias = (String) messageAttribute;
            String certificateEncoded = new CertificateAlias(alias).getCertificateEncoded();
            if (certificateEncoded == null || certificateEncoded.isEmpty()) {
                throw new ApplicationException("Certificate with alias '" + alias + "' does not exist in the key store");
            }
            return certificateEncoded;
        } else if (propertySpec.getName().equals(DeviceMessageConstants.certificateWrapperIdAttributeName)) {
            //Load the certificate with that id from the CertificateWrapper table and encode it.
            int id = ((BigDecimal) messageAttribute).intValue();
            CertificateWrapper certificateWrapper = MeteringWarehouse.getCurrent().getCertificateWrapperFactory().findById(id);
            if (certificateWrapper == null) {
                throw new ApplicationException("CertificateWrapper with id '" + id + "' does not exist in the EIServer database");
            }
            return certificateWrapper.getBase64Certificate();
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
            FIRMWARE_FILE_LOCK.lock();

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
            FIRMWARE_FILE_LOCK.unlock();
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
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES)) {
                        collectedMessage = configurePartialMulticastBlockTransfer(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.TRANSFER_SLAVE_FIRMWARE_FILE_TO_DATA_CONCENTRATOR)) {
                        collectedMessage = transferSlaveFirmwareFileToDC(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES)) {
                        collectedMessage = startMulticastBlockTransferToSlaveDevices(collectedMessage);
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
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.ACTIVATE_DLMS_SECURITY_VERSION1)) {
                        activateAdvancedDlmsEncryption(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.AGREE_NEW_ENCRYPTION_KEY)) {
                        collectedMessage = agreeNewKey(collectedMessage, 0);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.AGREE_NEW_AUTHENTICATION_KEY)) {
                        collectedMessage = agreeNewKey(collectedMessage, 2);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_SECURITY_SUITE)) {
                        changeSecuritySuite(pendingMessage);
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
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.EXPORT_END_DEVICE_CERTIFICATE)) {
                        collectedMessage = exportEndDeviceCertificate(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.EXPORT_SUB_CA_CERTIFICATES)) {
                        collectedMessage = exportSubCACertificates(collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.EXPORT_ROOT_CA_CERTIFICATE)) {
                        collectedMessage = exportRootCACertificate(collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.IMPORT_CA_CERTIFICATE)) {
                        importCACertificate(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.IMPORT_END_DEVICE_CERTIFICATE)) {
                        importEndDeviceCertificate(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.DELETE_CERTIFICATE_BY_SERIAL_NUMBER)) {
                        deleteCertificateBySerialNumber(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.DELETE_CERTIFICATE_BY_TYPE)) {
                        deleteCertificateByType(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.GENERATE_KEY_PAIR)) {
                        generateKeyPair(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.GENERATE_CSR)) {
                        collectedMessage = generateCSR(collectedMessage, pendingMessage);
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

                        String[] macAddressesAndProtocols = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceGroupAttributeName).getValue().split(SEPARATOR);

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
                    } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetMainLogbook)) {
                        this.resetLogbook(Beacon3100LogBookFactory.MAIN_LOGBOOK);
                    } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetSecurityLogbook)) {
                        this.resetLogbook(Beacon3100LogBookFactory.SECURITY_LOGBOOK);
                    } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetCoverLogbook)) {
                        this.resetLogbook(Beacon3100LogBookFactory.COVER_LOGBOOK);
                    } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetCommunicationLogbook)) {
                        this.resetLogbook(Beacon3100LogBookFactory.COMMUNICATION_LOGBOOK);
                    } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetVoltageCutLogbook)) {
                        this.resetLogbook(Beacon3100LogBookFactory.VOLTAGE_LOGBOOK);
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

    private void generateKeyPair(OfflineDeviceMessage pendingMessage) throws IOException {
        String certificateTypeAttributeValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, certificateTypeAttributeName).getValue();
        SecurityMessage.CertificateType certificateType = SecurityMessage.CertificateType.fromName(certificateTypeAttributeValue);

        getCosemObjectFactory().getSecuritySetup().generateKeyPair(certificateType.getId());
    }

    private CollectedMessage generateCSR(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        String certificateTypeAttributeValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, certificateTypeAttributeName).getValue();
        SecurityMessage.CertificateType certificateType = SecurityMessage.CertificateType.fromName(certificateTypeAttributeValue);

        byte[] encodedCSR = getCosemObjectFactory().getSecuritySetup().generateCSR(certificateType.getId());

        //Show the CSR in the protocol info of the message
        collectedMessage.setDeviceProtocolInformation(ProtocolTools.getHexStringFromBytes(encodedCSR, ""));
        return collectedMessage;
    }

    /**
     * Delete a certain certificate from the DLMS device.
     * The certificate is identified by its serial number and issuer.
     * <p/>
     * Note that this does not remove that certificate from our own persisted key store,
     * this needs to be done manually, using the API.
     */
    private void deleteCertificateBySerialNumber(OfflineDeviceMessage pendingMessage) throws IOException {
        String serialNumber = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, meterSerialNumberAttributeName).getValue();
        String certificateIssuer = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, certificateIssuerAttributeName).getValue();

        getCosemObjectFactory().getSecuritySetup().deleteCertificate(serialNumber, certificateIssuer);
    }

    /**
     * Delete a certain certificate from the DLMS device.
     * The certificate is identified by its entity, type and common name (system title)
     * <p/>
     * Note that this does not remove that certificate from our own persisted key store,
     * this needs to be done manually, using the API.
     */
    private void deleteCertificateByType(OfflineDeviceMessage pendingMessage) throws IOException {
        String certificateEntityAttributeValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, certificateEntityAttributeName).getValue();
        SecurityMessage.CertificateEntity certificateEntity = SecurityMessage.CertificateEntity.fromName(certificateEntityAttributeValue);
        String certificateTypeAttributeValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, certificateTypeAttributeName).getValue();
        SecurityMessage.CertificateType certificateType = SecurityMessage.CertificateType.fromName(certificateTypeAttributeValue);
        String commonName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, commonNameAttributeName).getValue();

        //Parse the common name as ASCII by default.
        byte[] systemTitle = commonName.getBytes();

        //Parse the CN of the client (ComServer) or server (DLMS device) as hex.
        if (certificateEntity == SecurityMessage.CertificateEntity.Client || certificateEntity == SecurityMessage.CertificateEntity.Server) {
            try {
                systemTitle = ProtocolTools.getBytesFromHexString(commonName, "");
            } catch (NumberFormatException e) {
                systemTitle = commonName.getBytes();
            }
        }

        getCosemObjectFactory().getSecuritySetup().deleteCertificate(certificateEntity.getId(), certificateType.getId(), systemTitle);
    }

    /**
     * Imports an X.509 v3 certificate of a public key of a CA.
     * The Beacon recognizes the entity by the Common Name (CN) of the certificate.
     * In case of sub-CA or root-CA certificates, their CN must end in "CA".
     */
    private void importCACertificate(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String encodedCertificateString = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.certificateAliasAttributeName).getValue();
        byte[] encodedCertificate = ProtocolTools.getBytesFromHexString(encodedCertificateString, "");
        getCosemObjectFactory().getSecuritySetup().importCertificate(encodedCertificate);
    }

    /**
     * Imports an X.509 v3 certificate of a public key of an end-device.
     * The Beacon recognizes the entity by the Common Name (CN) of the certificate.
     * In case of client (ComServer) certificates, this is the system title of the ComServer.
     * In case of server (Beacon) certificates, this is the system title of the Beacon device.
     * <p/>
     * The Beacon recognizes the certificate type (signing/key agreement/TLS) by the certificate extension(s)
     */
    private void importEndDeviceCertificate(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String base64EncodedCertificate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.certificateWrapperIdAttributeName).getValue();
        byte[] derEncodedCertificate = Base64.decodeBase64(base64EncodedCertificate);

        getCosemObjectFactory().getSecuritySetup().importCertificate(derEncodedCertificate);
    }

    /**
     * Finds the CA certificates (that are not self-signed) in the Beacon and returns them as collected data.
     */
    private CollectedMessage exportSubCACertificates(CollectedMessage collectedMessage) throws IOException {
        //Find the infos of all CA certificates (both root and sub) currently stored in the Beacon
        List<SecuritySetup.CertificateInfo> caCertInfo = new ArrayList<>();
        List<SecuritySetup.CertificateInfo> certificateInfos = getSecuritySetup().readCertificates();
        for (SecuritySetup.CertificateInfo certificateInfo : certificateInfos) {
            if (certificateInfo.getCertificateEntity().equals(SecurityMessage.CertificateEntity.CertificationAuthority)) {
                caCertInfo.add(certificateInfo);
            }
        }

        //Now read out the sub-CA certificates (by serial number and issuer name)
        StringBuilder protocolInfo = new StringBuilder();
        List<CertificateAlias> subCACertificateAliases = new ArrayList<>();
        for (SecuritySetup.CertificateInfo caCertificateInfo : caCertInfo) {
            X509Certificate x509Certificate = getSecuritySetup().exportCertificate(caCertificateInfo.getSerialNumber(), caCertificateInfo.getIssuer());

            //Not self signed, so a Sub-CA certificate.
            if (!x509Certificate.getIssuerDN().equals(x509Certificate.getSubjectDN())) {
                if (protocolInfo.length() == 0) {
                    protocolInfo.append("Added sub-CA certificate(s) to the EIServer DLMS trust store under the following alias(es): ");
                } else {
                    protocolInfo.append(", ");
                }

                String alias = "Beacon_sub_CA_" + x509Certificate.getSerialNumber();
                CertificateAlias certificateAlias = new CertificateAlias(alias);
                certificateAlias.setCertificateEncoded(getEncodedCertificate(x509Certificate));
                subCACertificateAliases.add(certificateAlias);

                protocolInfo.append(alias);
            }
        }

        if (subCACertificateAliases.isEmpty()) {
            collectedMessage.setDeviceProtocolInformation("The Beacon device contained no sub-CA certificates");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        } else {
            collectedMessage = MdcManager.getCollectedDataFactory().createCollectedMessageWithCertificates(
                    new DeviceIdentifierById(getProtocol().getOfflineDevice().getId()),
                    collectedMessage.getMessageIdentifier(),
                    subCACertificateAliases);
            collectedMessage.setDeviceProtocolInformation(protocolInfo.toString());
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        }

        return collectedMessage;
    }

    /**
     * Finds the self-signed CA certificate in the Beacon and returns it as collected data.
     */
    private CollectedMessage exportRootCACertificate(CollectedMessage collectedMessage) throws IOException {
        //Find the infos of all CA certificates (both root and sub) currently stored in the Beacon
        List<SecuritySetup.CertificateInfo> caCertInfo = new ArrayList<>();
        List<SecuritySetup.CertificateInfo> certificateInfos = getSecuritySetup().readCertificates();
        for (SecuritySetup.CertificateInfo certificateInfo : certificateInfos) {
            if (certificateInfo.getCertificateEntity().equals(SecurityMessage.CertificateEntity.CertificationAuthority)) {
                caCertInfo.add(certificateInfo);
            }
        }

        //Now read out the root-CA certificate (by serial number and issuer name)
        StringBuilder protocolInfo = new StringBuilder();
        List<CertificateAlias> rootCACertificateAliases = new ArrayList<>();
        for (SecuritySetup.CertificateInfo caCertificateInfo : caCertInfo) {
            X509Certificate x509Certificate = getSecuritySetup().exportCertificate(caCertificateInfo.getSerialNumber(), caCertificateInfo.getIssuer());

            //Self-signed, root-CA certificate
            if (x509Certificate.getIssuerDN().equals(x509Certificate.getSubjectDN())) {
                if (protocolInfo.length() == 0) {
                    protocolInfo.append("Added root-CA certificate(s) to the EIServer DLMS trust store under the following alias(es): ");
                } else {
                    protocolInfo.append(", ");
                }

                String alias = "Beacon_root_CA_" + x509Certificate.getSerialNumber();
                CertificateAlias certificateAlias = new CertificateAlias(alias);
                certificateAlias.setCertificateEncoded(getEncodedCertificate(x509Certificate));
                rootCACertificateAliases.add(certificateAlias);

                protocolInfo.append(alias);
            }
        }

        if (rootCACertificateAliases.isEmpty()) {
            collectedMessage.setDeviceProtocolInformation("The Beacon device contained no self-signed root-CA certificate.");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        } else {
            collectedMessage = MdcManager.getCollectedDataFactory().createCollectedMessageWithCertificates(
                    new DeviceIdentifierById(getProtocol().getOfflineDevice().getId()),
                    collectedMessage.getMessageIdentifier(),
                    rootCACertificateAliases);
            collectedMessage.setDeviceProtocolInformation(protocolInfo.toString());
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        }

        return collectedMessage;
    }

    /**
     * Export a server end-device certificate of type signing/key agreement/TLS from the Beacon.
     * It will be stored as a CertificateWrapper in EIServer, and the relevant property on the device will be filled in
     * <p/>
     * Note that the different security suites each have their own certificate, based on a certain elliptical curve.
     * The Beacon returns the proper certificate for the security suite it is currently operating in.
     */
    private CollectedMessage exportEndDeviceCertificate(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        SecurityMessage.CertificateType certificateType = SecurityMessage.CertificateType.fromName(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, certificateTypeAttributeName).getValue());

        if (getProtocol().getDlmsSessionProperties().getSecuritySuite() == 0
                && (certificateType.equals(SecurityMessage.CertificateType.DigitalSignature)
                || certificateType.equals(SecurityMessage.CertificateType.KeyAgreement))) {
            throw new ProtocolException("Cannot export ECDSA or ECDH certificates from the Beacon if it operates in suite 0.");
        }

        //The server system-title
        byte[] responseSystemTitle = getProtocol().getDlmsSession().getAso().getSecurityContext().getResponseSystemTitle();

        X509Certificate x509Certificate = getSecuritySetup().exportCertificate(SecurityMessage.CertificateEntity.Server.getId(), certificateType.getId(), responseSystemTitle);

        String propertyName = "";
        CertificateWrapperId propertyValue = new CertificateWrapperId(x509Certificate);

        //Server certificate for signing/key agreement is modelled as a security property
        if (SecurityMessage.CertificateType.DigitalSignature.equals(certificateType) || SecurityMessage.CertificateType.KeyAgreement.equals(certificateType)) {
            propertyName = SecurityMessage.CertificateType.DigitalSignature.equals(certificateType) ?
                    SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString() :
                    SecurityPropertySpecName.SERVER_KEY_AGREEMENT_CERTIFICATE.toString();

            //Note that updating the alias security property will also add the given certificate under that alias, to the DLMS key store.
            //If the key store already contains a certificate for that alias, an error is thrown, and the security property will not be updated either.
            collectedMessage = MdcManager.getCollectedDataFactory().createCollectedMessageWithUpdateSecurityProperty(
                    new DeviceIdentifierById(getProtocol().getOfflineDevice().getId()),
                    collectedMessage.getMessageIdentifier(),
                    propertyName,
                    propertyValue);

            //Server certificate for TLS is modelled as a general property
        } else if (SecurityMessage.CertificateType.TLS.equals(certificateType)) {
            propertyName = DlmsSessionProperties.SERVER_TLS_CERTIFICATE;

            //Note that updating the alias general property will also add the given certificate under that alias, to the DLMS key store.
            //If the key store already contains a certificate for that alias, an error is thrown, and the general property will not be updated either.
            collectedMessage = MdcManager.getCollectedDataFactory().createCollectedMessageWithUpdateGeneralProperty(
                    new DeviceIdentifierById(getProtocol().getOfflineDevice().getId()),
                    collectedMessage.getMessageIdentifier(),
                    propertyName,
                    propertyValue);
        }

        String msg = "Property '" + propertyName + "' on the Beacon device is updated with the ID referring to the new CertificateWrapper. This represents the server end-device certificate, with serial number'" + x509Certificate.getSerialNumber().toString() + "' and issuerDN '" + x509Certificate.getIssuerDN().getName() + "').";
        getLogger().info(msg);
        collectedMessage.setDeviceProtocolInformation(msg);
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessage;
    }

    /**
     * Return a hex string representing the encoded X509 v3 certificate
     */
    private String getEncodedCertificate(X509Certificate x509Certificate) throws ProtocolException {
        try {
            return ProtocolTools.getHexStringFromBytes(x509Certificate.getEncoded(), "");
        } catch (CertificateEncodingException e) {
            throw new ProtocolException(e, "Could not encode the received X509 v3 certificate (subject: '" + x509Certificate.getSubjectDN().getName() + "'): " + e.getMessage());
        }
    }

    private void changeSecuritySuite(OfflineDeviceMessage pendingMessage) throws IOException {
        int securitySuite = getSingleIntegerAttribute(pendingMessage);
        getCosemObjectFactory().getSecuritySetup().writeSecuritySuite(new TypeEnum(securitySuite));
        getProtocol().getDlmsSession().getAso().getSecurityContext().setSecuritySuite(securitySuite);
        getProtocol().getDlmsSessionProperties().setSecuritySuite(securitySuite);
    }

    private CollectedMessage agreeNewKey(CollectedMessage collectedMessage, int keyId) throws IOException {
        if (getProtocol().getDlmsSessionProperties().getSecuritySuite() == 0) {
            throw new ProtocolException("Key agreement is not supported in DLMS suite 0.");
        }
        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
        ECCCurve eccCurve = securityContext.getECCCurve();

        Structure keyAgreementData = new Structure();
        keyAgreementData.addDataType(new TypeEnum(keyId));

        //Holds a new ephemeral key pair. We send its public key to the server side.
        //We use its private key here, combined with the received ephemeral public key of the server to derive the new key.
        KeyAgreementImpl keyAgreement = new KeyAgreementImpl(eccCurve);
        PublicKey ephemeralPublicKey = keyAgreement.getEphemeralPublicKey();
        byte[] ephemeralPublicKeyEncoded = KeyUtils.toRawData(getProtocol().getDlmsSession().getAso().getSecurityContext().getECCCurve(), ephemeralPublicKey);

        ECDSASignatureImpl ecdsaSignature = new ECDSASignatureImpl(eccCurve);
        SecurityProvider securityProvider = getProtocol().getDlmsSessionProperties().getSecurityProvider();
        if (!(securityProvider instanceof GeneralCipheringSecurityProvider)) {
            throw CodingException.protocolImplementationError("General signing and ciphering is not yet supported in the protocol you are using");
        }
        byte[] signData = ProtocolTools.concatByteArrays(
                new byte[]{(byte) keyId},
                ephemeralPublicKeyEncoded
        );
        PrivateKey clientPrivateSigningKey = ((GeneralCipheringSecurityProvider) securityProvider).getClientPrivateSigningKey();
        if (clientPrivateSigningKey == null) {
            throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY);
        }
        byte[] signature = ecdsaSignature.sign(signData, clientPrivateSigningKey);

        byte[] keyData = ProtocolTools.concatByteArrays(ephemeralPublicKeyEncoded, signature);
        keyAgreementData.addDataType(OctetString.fromByteArray(keyData));

        Array array = new Array();
        array.addDataType(keyAgreementData);
        byte[] response = getCosemObjectFactory().getSecuritySetup().keyAgreement(array);

        //Now verify the received server ephemeral public key and use it to derive the shared secret.
        Array resultArray = AXDRDecoder.decode(response, Array.class);
        AbstractDataType dataType = resultArray.getDataType(0);
        Structure structure = dataType.getStructure();
        if (structure == null) {
            throw new ProtocolException("Expected the response of the key agreement to be an array of structures. However, the first element of the array was of type '" + dataType.getClass().getSimpleName() + "'.");
        }
        if (structure.nrOfDataTypes() != 2) {
            throw new ProtocolException("Expected the response of the key agreement to be structures with 2 elements each. However, the received structure contains " + structure.nrOfDataTypes() + " elements.");
        }
        OctetString octetString = structure.getDataType(1).getOctetString();
        if (octetString == null) {
            throw new ProtocolException("The responding key_data should be of type octetstring, but was of type '" + structure.getDataType(1).getClass().getSimpleName() + "'.");
        }
        byte[] serverKeyData = octetString.getOctetStr();

        int keySize = KeyUtils.getKeySize(eccCurve);
        byte[] serverEphemeralPublicKeyBytes = ProtocolTools.getSubArray(serverKeyData, 0, keySize);
        byte[] serverSignature = ProtocolTools.getSubArray(serverKeyData, keySize, serverKeyData.length);

        byte[] serverSignData = ProtocolTools.concatByteArrays(
                new byte[]{(byte) keyId},
                serverEphemeralPublicKeyBytes
        );
        ecdsaSignature = new ECDSASignatureImpl(eccCurve);
        X509Certificate serverSignatureCertificate = ((GeneralCipheringSecurityProvider) securityProvider).getServerSignatureCertificate();
        if (serverSignatureCertificate == null) {
            throw DeviceConfigurationException.missingProperty(SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString());
        }
        boolean verify = ecdsaSignature.verify(serverSignData, serverSignature, serverSignatureCertificate.getPublicKey());
        if (!verify) {
            throw new ProtocolException("Verification of the received signature failed, cannot agree on new key with server");
        }

        PublicKey serverEphemeralPublicKey = KeyUtils.toECPublicKey(eccCurve, serverEphemeralPublicKeyBytes);
        byte[] secretZ = keyAgreement.generateSecret(serverEphemeralPublicKey);

        byte[] agreedKey = NIST_SP_800_56_KDF.getInstance().derive(
                securityContext.getKeyDerivingHashFunction(),
                secretZ,
                securityContext.getKeyDerivingEncryptionAlgorithm(),
                securityContext.getSystemTitle(),
                securityContext.getResponseSystemTitle()
        );

        String securityPropertyName = "";
        if (keyId == 0) {
            securityPropertyName = SecurityPropertySpecName.ENCRYPTION_KEY.toString();
            getProtocol().getDlmsSessionProperties().getSecurityProvider().changeEncryptionKey(agreedKey);
        } else if (keyId == 2) {
            securityPropertyName = SecurityPropertySpecName.AUTHENTICATION_KEY.toString();
            getProtocol().getDlmsSessionProperties().getSecurityProvider().changeAuthenticationKey(agreedKey);
        }

        //Special kind of collected message: it includes the update of the relevant security property with the new, agreed key.
        collectedMessage = MdcManager.getCollectedDataFactory().createCollectedMessageWithUpdateSecurityProperty(
                new DeviceIdentifierById(getProtocol().getOfflineDevice().getId()),
                collectedMessage.getMessageIdentifier(),
                securityPropertyName,
                ProtocolTools.getHexStringFromBytes(agreedKey, ""));

        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessage;
    }

    /**
     * Let the Beacon do a multicast firmware upgrade to a number of AM540 slave devices.
     * https://confluence.eict.vpdc/display/G3IntBeacon3100/Meter+multicast+upgrade
     */
    private CollectedMessage dcMulticastUpgrade(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {

        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getValue();

        String unicastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, UnicastClientWPort).getValue();
        String broadcastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, BroadcastClientWPort).getValue();
        String multicastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, MulticastClientWPort).getValue();
        String logicalDeviceLSap = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, LogicalDeviceLSap).getValue();
        String securityLevelUnicast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityLevelUnicast).getValue();
        String securityLevelBroadcast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityLevelBroadcast).getValue();
        String securityPolicyBroadcast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityPolicyBroadcast).getValue();
        String delayAfterLastBlock = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayAfterLastBlock).getValue();
        String delayPerBlock = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayPerBlock).getValue();
        String delayBetweenBlockSentFast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayBetweenBlockSentFast).getValue();
        String delayBetweenBlockSentSlow = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayBetweenBlockSentSlow).getValue();
        String blocksPerCycle = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, BlocksPerCycle).getValue();
        String maxCycles = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, MaxCycles).getValue();
        String requestedBlockSize = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, RequestedBlockSize).getValue();
        String padLastBlock = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, PadLastBlock).getValue();
        String useTransferredBlockStatus = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, UseTransferredBlockStatus).getValue();

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

        String skipStepEnable = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SkipStepEnable).getValue();
        String skipStepVerify = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SkipStepVerify).getValue();
        String skipStepActivate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SkipStepActivate).getValue();
        String unicastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, UnicastClientWPort).getValue();
        String multicastClientWPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, MulticastClientWPort).getValue();
        String unicastFrameCounterType = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, UnicastFrameCounterType).getValue();
        String timeZone = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, MeterTimeZone).getValue();
        String securityLevelMulticast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityLevelMulticast).getValue();
        String securityPolicyMulticastV0 = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, SecurityPolicyMulticastV0).getValue();
        String delayBetweenBlockSentFast = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DelayBetweenBlockSentFast).getValue();

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

        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getValue();

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

        try {//by activating the image we trigger the multicast block transfer to slave devices
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
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getValue());
        getCosemObjectFactory().getFirewallSetup().setWANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWLAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getValue());
        getCosemObjectFactory().getFirewallSetup().setLANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWGPRS(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getValue());
        getCosemObjectFactory().getFirewallSetup().setGPRSPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void deactivateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getFirewallSetup().deactivate();
    }

    private void activateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getFirewallSetup().activate();
    }

    private void changeGPRSParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getValue();
        String apn = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, apnAttributeName).getValue();
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
        final String hexMacAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.macAddress).getValue();
        final long timeoutInMillis = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.timeout).getValue());

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
        String macAddress = pendingMessage.getDeviceMessageAttributes().get(0).getValue();

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

        for (String macAddress : pendingMessage.getDeviceMessageAttributes().get(0).getValue().split(SEPARATOR)) {
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

        for (String macAddress : pendingMessage.getDeviceMessageAttributes().get(0).getValue().split(SEPARATOR)) {
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
        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getValue(); // Will return empty string if the MessageAttribute could not be found

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
        String hex = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        final CosemObjectFactory cof = getCosemObjectFactory();
        cof.getAssociationLN().changeHLSSecret(ProtocolTools.getBytesFromHexString(hex, ""));
    }

    private void enableNetworkInterfaces(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isEthernetWanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ETHERNET_WAN).getValue());
        boolean isEthernetLanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ETHERNET_LAN).getValue());
        boolean isWirelessWanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.WIRELESS_WAN).getValue());
        boolean isIp6_TunnelEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.IP6_TUNNEL).getValue());
        boolean isPlc_NetworkEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.PLC_NETWORK).getValue());
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
        String wrappedHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newWrappedEncryptionKeyAttributeName).getValue();
        String plainHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newEncryptionKeyAttributeName).getValue();
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
        String wrappedHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName).getValue();
        String plainHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newAuthenticationKeyAttributeName).getValue();

        Array authenticationKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey, "")));
        authenticationKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(authenticationKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(plainHexKey, ""));
    }

    private void activateAdvancedDlmsEncryption(OfflineDeviceMessage pendingMessage) throws IOException {
        int securitySuite = getProtocol().getDlmsSessionProperties().getSecuritySuite();

        boolean authenticatedRequests = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.authenticatedRequestsAttributeName).getValue());
        boolean encryptedRequests = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.encryptedRequestsAttributeName).getValue());
        boolean signedRequests = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.signedRequestsAttributeName).getValue());
        boolean authenticatedResponses = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.authenticatedResponsesAttributeName).getValue());
        boolean encryptedResponses = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.encryptedResponsesAttributeName).getValue());
        boolean signedResponse = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.signedResponsesAttributeName).getValue());

        int securityPolicy = 0;
        if (securitySuite == 0) {
            if ((authenticatedRequests != authenticatedResponses) || (encryptedRequests != encryptedResponses)) {
                throw new ProtocolException("It is not possible to set a different security level for requests than for responses, in DLMS suite 0.");
            }
            if (signedRequests || signedResponse) {
                throw new ProtocolException("It is not possible to apply digital signing in DLMS suite 0");
            }

            securityPolicy |= (authenticatedRequests ? 1 : 0);
            securityPolicy |= (encryptedRequests ? 1 : 0) << 1;
        } else {
            securityPolicy |= (authenticatedRequests ? 1 : 0) << 2;
            securityPolicy |= (encryptedRequests ? 1 : 0) << 3;
            securityPolicy |= (signedRequests ? 1 : 0) << 4;
            securityPolicy |= (authenticatedResponses ? 1 : 0) << 5;
            securityPolicy |= (encryptedResponses ? 1 : 0) << 6;
            securityPolicy |= (signedResponse ? 1 : 0) << 7;
        }

        getSecuritySetup().activateSecurity(new TypeEnum(securityPolicy));

        //Start using the new security_policy immediately.
        getProtocol().getDlmsSessionProperties().setDataTransportSecurityLevel(securityPolicy);
        getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().setDataTransportSecurityLevel(securityPolicy);
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
        String address = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getNTPServerAddress().writeNTPServerName(address);
    }

    private void syncNTPServer(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getNTPServerAddress().ntpSync();
    }

    private void setDeviceName(OfflineDeviceMessage pendingMessage) throws IOException {
        String name = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getData(DEVICE_NAME_OBISCODE).setValueAttr(OctetString.fromString(name));
    }

    private void rebootApplication(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getLifeCycleManagement().restartApplication();
    }

    private void rebootDevice(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getLifeCycleManagement().rebootDevice();
    }

    private void enableEventNotifications(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getCosemObjectFactory().getBeaconEventPushNotificationConfig().enable(enable);
    }

    private void setModemWatchdogParameters(OfflineDeviceMessage pendingMessage) throws IOException {

        //Note that all values here are expressed in seconds
        int modemWatchdogInterval = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemWatchdogInterval).getValue());
        int modemWatchdogInitialDelay = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemWatchdogInitialDelay).getValue());
        int pppDaemonResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.PPPDaemonResetThreshold).getValue());
        int modemResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemResetThreshold).getValue());
        int systemRebootThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.systemRebootThreshold).getValue());

        getCosemObjectFactory().getModemWatchdogConfiguration().writeExtendedConfigParameters(
                modemWatchdogInterval,
                modemWatchdogInitialDelay,
                pppDaemonResetThreshold,
                modemResetThreshold,
                systemRebootThreshold
        );
    }

    private void enableModemWatchdog(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getCosemObjectFactory().getModemWatchdogConfiguration().enableWatchdog(enable);
    }

    private void configurePushEventNotification(OfflineDeviceMessage pendingMessage) throws IOException {
        String transportTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transportTypeAttributeName).getValue();
        int transportType = AlarmConfigurationMessage.TransportType.valueOf(transportTypeString).getId();

        String destinationAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName).getValue();

        String messageTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.messageTypeAttributeName).getValue();
        int messageType = AlarmConfigurationMessage.MessageType.valueOf(messageTypeString).getId();

        getCosemObjectFactory().getBeaconEventPushNotificationConfig().writeSendDestinationAndMethod(transportType, destinationAddress, messageType);
    }

    private void writeUplinkPingTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer timeout = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getCosemObjectFactory().getUplinkPingConfiguration().writeTimeout(timeout);
    }

    private void changePasswordUser1(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getWebPortalConfig().changeUser1Password(newPassword);
    }

    private void changePasswordUser2(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getWebPortalConfig().changeUser2Password(newPassword);
    }

    private void changeUserPassword(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.usernameAttributeName).getValue();
        String newPassword = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.passwordAttributeName).getValue();

        getCosemObjectFactory().getWebPortalConfig().changeUserPassword(userName, newPassword);
    }

    private void writeUplinkPingInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer interval = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getCosemObjectFactory().getUplinkPingConfiguration().writeInterval(interval);
    }

    private void writeUplinkPingDestinationAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String destinationAddress = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getUplinkPingConfiguration().writeDestAddress(destinationAddress);
    }

    private void enableUplinkPing(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getCosemObjectFactory().getUplinkPingConfiguration().enableUplinkPing(enable);
    }

    private int getSingleIntegerAttribute(OfflineDeviceMessage pendingMessage) {
        return Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
    }

    private void writePrimaryDNSAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String address = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getIPv4Setup().setPrimaryDNSAddress(address);
    }

    private void writeSecondaryDNSAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String address = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getIPv4Setup().setSecondaryDNSAddress(address);
    }

    private void setHttpPort(OfflineDeviceMessage pendingMessage) throws IOException {
        String httpPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SetHttpPortAttributeName).getValue();
        getCosemObjectFactory().getWebPortalConfig().setHttpPort(httpPort);
    }

    private void setHttpsPort(OfflineDeviceMessage pendingMessage) throws IOException {
        String httpsPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SetHttpsPortAttributeName).getValue();
        getCosemObjectFactory().getWebPortalConfig().setHttpsPort(httpsPort);
    }

    private void setMaxLoginAttempts(OfflineDeviceMessage pendingMessage) throws IOException {
        /*final long logAttempts = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_MAX_LOGIN_ATTEMPTS).getDeviceMessageAttributeValue());*/
        String logAttempts = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_MAX_LOGIN_ATTEMPTS).getValue();
        getCosemObjectFactory().getWebPortalConfig().setMaxLoginAttempts(logAttempts);
    }

    private void setLockoutDuration(OfflineDeviceMessage pendingMessage) throws IOException {
        String duration = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_LOCKOUT_DURATION).getValue();
        getCosemObjectFactory().getWebPortalConfig().setLockoutDuration(duration);
    }

    private void enableGzipCompression(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enableGzip = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ENABLE_GZIP_COMPRESSION).getValue());
        getCosemObjectFactory().getWebPortalConfig().enableGzipCompression(enableGzip);
    }

    private void enableSSL(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enableSSL = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.enableSSL).getValue());
        getCosemObjectFactory().getWebPortalConfig().enableSSL(enableSSL);
    }

    private void setAuthenticationMechanism(OfflineDeviceMessage pendingMessage) throws IOException {
        String authName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_AUTHENTICATION_MECHANISM).getValue();
        int auth = AuthenticationMechanism.fromAuthName(authName);
        getCosemObjectFactory().getWebPortalConfig().setAuthenticationMechanism(auth);
    }

    /**
     * Performs a reset on a {@link ProfileGeneric}.
     *
     * @param obisCode The OBIS code.
     * @throws IOException If an IO error occurs.
     */
    private final void resetLogbook(final ObisCode obisCode) throws IOException {
        this.getCosemObjectFactory().getProfileGeneric(obisCode).reset();
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
