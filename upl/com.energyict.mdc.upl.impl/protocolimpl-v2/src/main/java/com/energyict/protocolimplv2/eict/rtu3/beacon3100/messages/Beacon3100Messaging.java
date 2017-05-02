package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.ConcentratorSetup;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.EventPushNotificationConfig;
import com.energyict.dlms.cosem.FirewallSetup;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.ImageTransfer.RandomAccessFileImageBlockSupplier;
import com.energyict.dlms.cosem.InactiveFirmwareIC;
import com.energyict.dlms.cosem.ModemWatchdogConfiguration;
import com.energyict.dlms.cosem.NTPServerAddress;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.ScheduleManager;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.UplinkPingConfiguration;
import com.energyict.dlms.cosem.WebPortalSetupV1;
import com.energyict.dlms.cosem.WebPortalSetupV1.Role;
import com.energyict.dlms.cosem.WebPortalSetupV1.WebPortalAuthenticationMechanism;
import com.energyict.dlms.cosem.methods.NetworkInterfaceType;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.encryption.asymetric.ECCCurve;
import com.energyict.encryption.asymetric.keyagreement.KeyAgreementImpl;
import com.energyict.encryption.asymetric.signature.ECDSASignatureImpl;
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.encryption.kdf.NIST_SP_800_56_KDF;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.CertificateAliasFinder;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.CertificateAlias;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.BeaconCache;
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
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers.Beacon3100RegisterFactory;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DLMSConfigurationDeviceMessage;
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
import com.energyict.protocolimplv2.messages.enums.DLMSGatewayNotificationRelayType;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.messages.validators.KeyMessageChangeValidator;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
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
    private static final ObisCode DEVICE_NAME_OLD_OBISCODE = ObisCode.fromString("0.0.128.0.9.255");
    private static final ObisCode DEVICE_NAME_NEW_OBISCODE = ObisCode.fromString("0.136.96.128.0.255");
    private static final ObisCode DEVICE_HOST_NAME_OLD_OBISCODE = ObisCode.fromString("0.0.128.0.24.255");
    private static final ObisCode DEVICE_HOST_NAME_NEW_OBISCODE = ObisCode.fromString("0.128.96.128.0.255");
    private static final ObisCode DEVICE_LOCATION_OLD_OBISCODE = ObisCode.fromString("0.0.128.0.32.255");
    private static final ObisCode DEVICE_LOCATION_NEW_OBISCODE = ObisCode.fromString("0.136.96.160.0.255");
    private static final ObisCode MULTI_APN_COFIG_OLD_OBISCODE = ObisCode.fromString("0.128.25.3.0.255");
    private static final ObisCode MULTI_APN_COFIG_NEW_OBISCODE = ObisCode.fromString("0.162.96.160.0.255");
    private static final ObisCode LIFE_CYCLEMANAGEMENT_NEW_OBISCODE = ObisCode.fromString("0.128.96.160.0.255");
    public static final ObisCode REMOTE_SHELL_SETUP_NEW_OBISCODE = ObisCode.fromString("0.128.96.193.0.255");
    public static final ObisCode SNMP_SETUP_NEW_OBISCODE = ObisCode.fromString("0.128.96.194.0.255");
    public static final ObisCode RTU_DISCOVERY_SETUP_NEW_OBISCODE = ObisCode.fromString("0.128.96.195.0.255");
    public static final ObisCode TIME_SERVER_NEW_OBISCODE = ObisCode.fromString("0.128.96.196.0.255");

    /**
     * Old (pre-1.9) OBIS of the web portal setup IC.
     */
    private static final ObisCode WEB_PORTAL_SETUP_OLD_OBIS = ObisCode.fromString("0.0.128.0.13.255");

    /**
     * New (1.9 and up) OBIS of the web portal setup IC.
     */
    public static final ObisCode WEB_PORTAL_CONFIG_NEW_OBISCODE = ObisCode.fromString("0.128.96.197.0.255");

    public static final ObisCode PING_SERVICE_NEW_OBISCODE = ObisCode.fromString("0.160.96.144.0.255");
    public static final ObisCode SCHEDULE_MANAGER_NEW_OBISCODE = ObisCode.fromString("0.187.96.160.0.255");
    public static final ObisCode CLIENT_MANAGER_NEW_OBISCODE = ObisCode.fromString("0.187.96.170.0.255");
    public static final ObisCode MODEM_WATCHDOG_NEW_OBISCODE = ObisCode.fromString("0.162.96.128.0.255");
    public static final ObisCode G3_NETWORK_MANAGEMENT_NEW_OBISCODE = ObisCode.fromString("0.168.96.128.0.255");

    /**
     * New logical name of the concentrator setup object.
     */
    public static final ObisCode CONCENTRATOR_SETUP_NEW_LOGICAL_NAME = ObisCode.fromString("0.187.96.128.0.255");

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
    private final ObjectMapperService objectMapperService;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final DeviceMasterDataExtractor deviceMasterDataExtractor;
    private final DeviceGroupExtractor deviceGroupExtractor;
    private final DeviceExtractor deviceExtractor;
    private final CertificateAliasFinder certificateAliasFinder;
    private final CertificateWrapperExtractor certificateWrapperExtractor;

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> standardMessages = new ArrayList<>(Arrays.asList(
                NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(this.propertySpecService, this.nlsService, this.converter),

                DeviceActionMessage.SyncMasterdataForDC.get(this.propertySpecService, this.nlsService, this.converter),
                DeviceActionMessage.SyncDeviceDataForDC.get(this.propertySpecService, this.nlsService, this.converter),
                DeviceActionMessage.PauseDCScheduler.get(this.propertySpecService, this.nlsService, this.converter),
                DeviceActionMessage.ResumeDCScheduler.get(this.propertySpecService, this.nlsService, this.converter),
                DeviceActionMessage.SyncOneConfigurationForDC.get(this.propertySpecService, this.nlsService, this.converter),
                DeviceActionMessage.TRIGGER_PRELIMINARY_PROTOCOL.get(this.propertySpecService, this.nlsService, this.converter),
                DeviceActionMessage.RemoveLogicalDevice.get(this.propertySpecService, this.nlsService, this.converter),

                PLCConfigurationDeviceMessage.PingMeter.get(this.propertySpecService, this.nlsService, this.converter),

                // FirmwareDeviceMessage.BroadcastFirmwareUpgrade.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.DataConcentratorMulticastFirmwareUpgrade.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.ReadMulticastProgress.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.TRANSFER_SLAVE_FIRMWARE_FILE_TO_DATA_CONCENTRATOR.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES.get(this.propertySpecService, this.nlsService, this.converter),
                FirmwareDeviceMessage.COPY_ACTIVE_FIRMWARE_TO_INACTIVE_PARTITION.get(this.propertySpecService, this.nlsService, this.converter),

                SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.ACTIVATE_DLMS_SECURITY_VERSION1.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.AGREE_NEW_ENCRYPTION_KEY.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.AGREE_NEW_AUTHENTICATION_KEY.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_SECURITY_SUITE.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_CLIENT.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_CLIENT.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_CLIENT.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_HLS_SECRET_PASSWORD.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_HLS_SECRET_PASSWORD_FOR_CLIENT.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.EXPORT_END_DEVICE_CERTIFICATE.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.EXPORT_SUB_CA_CERTIFICATES.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.EXPORT_ROOT_CA_CERTIFICATE.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.IMPORT_CA_CERTIFICATE.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.IMPORT_CLIENT_END_DEVICE_CERTIFICATE.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.IMPORT_SERVER_END_DEVICE_CERTIFICATE.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.DELETE_CERTIFICATE_BY_SERIAL_NUMBER.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.DELETE_CERTIFICATE_BY_TYPE.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.GENERATE_KEY_PAIR.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.GENERATE_CSR.get(this.propertySpecService, this.nlsService, this.converter),

                UplinkConfigurationDeviceMessage.EnableUplinkPing.get(this.propertySpecService, this.nlsService, this.converter),
                UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress.get(this.propertySpecService, this.nlsService, this.converter),
                UplinkConfigurationDeviceMessage.WriteUplinkPingInterval.get(this.propertySpecService, this.nlsService, this.converter),
                UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout.get(this.propertySpecService, this.nlsService, this.converter),
                // PPPConfigurationDeviceMessage.SetPPPIdleTime.get(this.propertySpecService, this.nlsService, this.converter),
                // NetworkConnectivityMessage.PreferGPRSUpstreamCommunication.get(this.propertySpecService, this.nlsService, this.converter),
                NetworkConnectivityMessage.EnableModemWatchdog.get(this.propertySpecService, this.nlsService, this.converter),
                NetworkConnectivityMessage.SetModemWatchdogParameters2.get(this.propertySpecService, this.nlsService, this.converter),
                NetworkConnectivityMessage.SetPrimaryDNSAddress.get(this.propertySpecService, this.nlsService, this.converter),
                NetworkConnectivityMessage.SetSecondaryDNSAddress.get(this.propertySpecService, this.nlsService, this.converter),
                NetworkConnectivityMessage.EnableNetworkInterfaces.get(this.propertySpecService, this.nlsService, this.converter),
                NetworkConnectivityMessage.SetHttpPort.get(this.propertySpecService, this.nlsService, this.converter),
                NetworkConnectivityMessage.SetHttpsPort.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.EnableGzipCompression.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.EnableSSL.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.SetAuthenticationMechanism.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.SetMaxLoginAttempts.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.SetLockoutDuration.get(this.propertySpecService, this.nlsService, this.converter),
                AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION.get(this.propertySpecService, this.nlsService, this.converter),
                AlarmConfigurationMessage.ENABLE_EVENT_NOTIFICATIONS.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.SetDeviceName.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.SetDeviceHostName.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.SetNTPAddress.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.SyncNTPServer.get(this.propertySpecService, this.nlsService, this.converter),
                ConfigurationChangeDeviceMessage.ConfigureAPNs.get(this.propertySpecService, this.nlsService, this.converter),
                DeviceActionMessage.RebootApplication.get(this.propertySpecService, this.nlsService, this.converter),
                FirewallConfigurationMessage.ActivateFirewall.get(this.propertySpecService, this.nlsService, this.converter),
                FirewallConfigurationMessage.DeactivateFirewall.get(this.propertySpecService, this.nlsService, this.converter),
                FirewallConfigurationMessage.ConfigureFWGPRS.get(this.propertySpecService, this.nlsService, this.converter),
                FirewallConfigurationMessage.ConfigureFWLAN.get(this.propertySpecService, this.nlsService, this.converter),
                FirewallConfigurationMessage.ConfigureFWWAN.get(this.propertySpecService, this.nlsService, this.converter),
                SecurityMessage.CHANGE_WEBPORTAL_PASSWORD.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetSecurityLevel.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetRoutingConfiguration.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMaxJoinWaitTime.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetPathDiscoveryTime.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMetricType.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetPanId.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetTMRTTL.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMaxFrameRetries.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetHighPriorityWindowSize.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetCSMAFairnessLimit.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMacA.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMacK.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMinimumCWAttempts.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMaxBe.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMaxCSMABackOff.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.SetMinBe.get(this.propertySpecService, this.nlsService, this.converter),

                PLCConfigurationDeviceMessage.SetAutomaticRouteManagement.get(this.propertySpecService, this.nlsService, this.converter),
                // PLCConfigurationDeviceMessage.EnableKeepAlive.get(this.propertySpecService, this.nlsService, this.converter),
                // PLCConfigurationDeviceMessage.SetKeepAliveScheduleInterval.get(this.propertySpecService, this.nlsService, this.converter),
                // PLCConfigurationDeviceMessage.SetKeepAliveBucketSize.get(this.propertySpecService, this.nlsService, this.converter),
                // PLCConfigurationDeviceMessage.SetMinInactiveMeterTime.get(this.propertySpecService, this.nlsService, this.converter),
                // PLCConfigurationDeviceMessage.SetMaxInactiveMeterTime.get(this.propertySpecService, this.nlsService, this.converter),
                // PLCConfigurationDeviceMessage.SetKeepAliveRetries.get(this.propertySpecService, this.nlsService, this.converter),
                // PLCConfigurationDeviceMessage.SetKeepAliveTimeout.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.EnableG3PLCInterface.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.KickMeter.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.AddMetersToBlackList.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.RemoveMetersFromBlackList.get(this.propertySpecService, this.nlsService, this.converter),
                PLCConfigurationDeviceMessage.PathRequestWithTimeout.get(this.propertySpecService, this.nlsService, this.converter),

                // Logbook resets.
                LogBookDeviceMessage.ResetMainLogbook.get(this.propertySpecService, this.nlsService, this.converter),
                LogBookDeviceMessage.ResetSecurityLogbook.get(this.propertySpecService, this.nlsService, this.converter),
                LogBookDeviceMessage.ResetCoverLogbook.get(this.propertySpecService, this.nlsService, this.converter),
                LogBookDeviceMessage.ResetCommunicationLogbook.get(this.propertySpecService, this.nlsService, this.converter),
                LogBookDeviceMessage.ResetVoltageCutLogbook.get(this.propertySpecService, this.nlsService, this.converter),

                DLMSConfigurationDeviceMessage.MeterPushNotificationSettings.get(this.propertySpecService, this.nlsService, this.converter),

                AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_SINGLE_ALARM_REGISTER.get(this.propertySpecService, this.nlsService, this.converter),
                AlarmConfigurationMessage.RESET_BITS_IN_ALARM_SINGLE_REGISTER.get(this.propertySpecService, this.nlsService, this.converter),
                AlarmConfigurationMessage.WRITE_FILTER_FOR_SINGLE_ALARM_REGISTER.get(this.propertySpecService, this.nlsService, this.converter),
                AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_CIPHERING.get(this.propertySpecService, this.nlsService, this.converter),
                AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_SEND_TEST_NOTIFICATION.get(this.propertySpecService, this.nlsService, this.converter)
        ));

        if (!readOldObisCodes()) {
            standardMessages.add(DeviceActionMessage.SyncAllDevicesWithDC.get(this.propertySpecService, this.nlsService, this.converter));
            standardMessages.add(DeviceActionMessage.SyncOneDeviceWithDC.get(this.propertySpecService, this.nlsService, this.converter));
            standardMessages.add(DeviceActionMessage.SyncOneDeviceWithDCAdvanced.get(this.propertySpecService, this.nlsService, this.converter));
            standardMessages.add(DeviceActionMessage.SetBufferForAllLoadProfiles.get(this.propertySpecService, this.nlsService, this.converter));
            standardMessages.add(DeviceActionMessage.SetBufferForSpecificLoadProfile.get(this.propertySpecService, this.nlsService, this.converter));
            standardMessages.add(DeviceActionMessage.SetBufferForAllEventLogs.get(this.propertySpecService, this.nlsService, this.converter));
            standardMessages.add(DeviceActionMessage.SetBufferForSpecificEventLog.get(this.propertySpecService, this.nlsService, this.converter));
            standardMessages.add(DeviceActionMessage.SetBufferForAllRegisters.get(this.propertySpecService, this.nlsService, this.converter));
            standardMessages.add(DeviceActionMessage.SetBufferForSpecificRegister.get(this.propertySpecService, this.nlsService, this.converter));
        }

        return standardMessages;
    }

    private MasterDataSync masterDataSync;
    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor = null;

    public Beacon3100Messaging(Beacon3100 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, ObjectMapperService objectMapperService, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMasterDataExtractor deviceMasterDataExtractor, DeviceGroupExtractor deviceGroupExtractor, DeviceExtractor deviceExtractor, CertificateAliasFinder certificateAliasFinder, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(protocol, collectedDataFactory, issueFactory);
        this.objectMapperService = objectMapperService;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.deviceMasterDataExtractor = deviceMasterDataExtractor;
        this.deviceGroupExtractor = deviceGroupExtractor;
        this.deviceExtractor = deviceExtractor;
        this.certificateAliasFinder = certificateAliasFinder;
        this.certificateWrapperExtractor = certificateWrapperExtractor;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getValueFactory().getValueTypeName().equals(Password.class.getName())) {
            return ((Password) messageAttribute).getValue();
        }

        if (propertySpec.getName().equals(DeviceMessageConstants.broadcastDevicesGroupAttributeName)) {
            DeviceInfoSerializer serializer = new DeviceInfoSerializer(this.deviceMasterDataExtractor, this.deviceGroupExtractor, this.objectMapperService);
            return serializer.serializeDeviceInfo(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastInitialTimeBetweenBlocksAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.timeout)) {
            return String.valueOf(((Duration) messageAttribute).toMillis()); //Return value in ms
        } else if (propertySpec.getName().equals(DeviceMessageConstants.modemWatchdogInterval)
                || propertySpec.getName().equals(DeviceMessageConstants.modemWatchdogInitialDelay)
                || propertySpec.getName().equals(DeviceMessageConstants.PPPDaemonResetThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.modemResetThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.systemRebootThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((Duration) messageAttribute).getSeconds()); //Return value in seconds
        } else if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
        } else if (propertySpec.getName().equals(DeviceMessageConstants.encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.deviceGroupAttributeName)) {
            DeviceGroup group = (DeviceGroup) messageAttribute;
            StringBuilder macAddresses = new StringBuilder();

            for (Device device : deviceGroupExtractor.members(group)) {
                String callHomeId = deviceExtractor.protocolProperty(device, LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "");
                if (!callHomeId.isEmpty()) {
                    if (macAddresses.length() != 0) {
                        macAddresses.append(SEPARATOR);
                    }
                    macAddresses.append(callHomeId);

                    //Also add the java protocol class of the device, for the TRIGGER_PRELIMINARY_PROTOCOL message only
                    if (offlineDeviceMessage.getSpecification().equals(DeviceActionMessage.TRIGGER_PRELIMINARY_PROTOCOL)) {
                        macAddresses.append(SEPARATOR2).append(deviceExtractor.getDeviceProtocolPluggableClass(device));
                    }
                }
            }
            return macAddresses.toString();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.CACertificateAliasAttributeName)) {
            //Load the certificate with that alias from the EIServer DLMS trust store and encode it.
            String alias = (String) messageAttribute;
            CertificateAlias certificateAlias = certificateAliasFinder.from(alias);
            String certificateEncoded = certificateAlias.getCertificateEncoded();
            if (certificateEncoded == null || certificateEncoded.isEmpty()) {
                return "";  //Message executor will recognize this and set the message to failed
            }
            return certificateEncoded;
        } else if (propertySpec.getName().equals(DeviceMessageConstants.clientCertificateAliasAttributeName)) {
            //Load the certificate with that alias from the EIServer DLMS key store and encode it.
            String alias = (String) messageAttribute;
            CertificateAlias certificateAlias = certificateAliasFinder.from(alias); //TODO use privateKeyAliasFinder hear instead!
            String certificateEncoded = certificateAlias.getCertificateEncoded();
            if (certificateEncoded == null || certificateEncoded.isEmpty()) {
                return "";  //Message executor will recognize this and set the message to failed
            }
            return certificateEncoded;
        } else if (propertySpec.getName().equals(DeviceMessageConstants.certificateWrapperAttributeName)) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) messageAttribute;
            return certificateWrapperExtractor.getBase64EncodedCertificate(certificateWrapper);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.DelayAfterLastBlock)
                || propertySpec.getName().equals(DeviceMessageConstants.DelayPerBlock)
                || propertySpec.getName().equals(DeviceMessageConstants.DelayBetweenBlockSentFast)
                || propertySpec.getName().equals(DeviceMessageConstants.DelayBetweenBlockSentSlow)) {
            return String.valueOf(((Duration) messageAttribute).toMillis());
        } else {
            return messageAttribute.toString();     //Works for BigDecimal, boolean and (hex)string property specs
        }
    }

    @Override
    public Optional<String> prepareMessageContext(com.energyict.mdc.upl.meterdata.Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {

        MasterDataSerializer masterDataSerializer = new MasterDataSerializer(this.objectMapperService, this.propertySpecService, this.deviceMasterDataExtractor, getBeacon3100Properties());
        MulticastSerializer multicastSerializer = masterDataSerializer.multicastSerializer();


        try {
            if (deviceMessage.getMessageId() == DeviceActionMessage.SyncMasterdataForDC.id()) {
                return Optional.of(masterDataSerializer.serializeMasterData(device, readOldObisCodes()));
            } else if (deviceMessage.getMessageId() == DeviceActionMessage.SyncDeviceDataForDC.id()) {
                return Optional.of(masterDataSerializer.serializeMeterDetails(device));
            } else if (deviceMessage.getMessageId() == DeviceActionMessage.SyncOneConfigurationForDC.id()) {
                int configId = ((BigDecimal) deviceMessage.getAttributes().get(0).getValue()).intValue();
                return Optional.of(masterDataSerializer.serializeMasterDataForOneConfig(configId, readOldObisCodes()));
            } else if (deviceMessage.getMessageId() == FirmwareDeviceMessage.DataConcentratorMulticastFirmwareUpgrade.id()) {
                return Optional.of(multicastSerializer.serialize(device, offlineDevice, deviceMessage, getBeacon3100Properties()));
            } else if (deviceMessage.getMessageId() == FirmwareDeviceMessage.CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES.id()) {
                return Optional.of(multicastSerializer.serialize(device, offlineDevice, deviceMessage, getBeacon3100Properties()));
            } else if (deviceMessage.getMessageId() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS.id()
                    || deviceMessage.getMessageId() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_CLIENT.id()) {
                new KeyMessageChangeValidator().validateNewKeyValueForFreeTextClient(offlineDevice.getId(), deviceMessage, SecurityPropertySpecName.AUTHENTICATION_KEY);
            } else if (deviceMessage.getMessageId() == SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS.id()
                    || deviceMessage.getMessageId() == SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_CLIENT.id()) {
                new KeyMessageChangeValidator().validateNewKeyValueForFreeTextClient(offlineDevice.getId(), deviceMessage, SecurityPropertySpecName.ENCRYPTION_KEY);
            } else if (deviceMessage.getMessageId() == SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS.id()
                    || deviceMessage.getMessageId() == SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_CLIENT.id()) {
                new KeyMessageChangeValidator().validateNewKeyValueForFreeTextClient(offlineDevice.getId(), deviceMessage, SecurityPropertySpecName.MASTER_KEY);
            }
        } catch (DeviceConfigurationException e) {
            return Optional.of("DeviceConfigurationException " + e.getMessage());
        }

        return Optional.empty();
    }

    private Beacon3100Properties getBeacon3100Properties() {
        return (Beacon3100Properties) getProtocol().getDlmsSessionProperties();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

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
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncAllDevicesWithDC)) {
                        collectedMessage = getMasterDataSync().syncAllDeviceData(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncOneDeviceWithDC)) {
                        collectedMessage = getMasterDataSync().syncOneDeviceData(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncOneDeviceWithDCAdvanced)) {
                        collectedMessage = getMasterDataSync().syncOneDeviceWithDCAdvanced(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.PauseDCScheduler)) {
                        setSchedulerState(SchedulerState.PAUSED);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ResumeDCScheduler)) {
                        setSchedulerState(SchedulerState.RUNNING);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.RemoveLogicalDevice)) {
                        removeLogicalDevice(pendingMessage, collectedMessage);
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
                        collectedMessage = new BroadcastUpgrade(this, this.propertySpecService, objectMapperService, this.certificateWrapperExtractor).broadcastFirmware(pendingMessage, collectedMessage);
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
                        rebootDevice();
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.RebootApplication)) {
                        rebootApplication();
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetDeviceName)) {
                        setDeviceName(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetDeviceHostName)) {
                        setDeviceHostName(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetDeviceLocation)) {
                        setDeviceLocation(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetNTPAddress)) {
                        setNTPAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SyncNTPServer)) {
                        syncNTPServer(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SET_DEVICE_LOG_LEVEL)) {
                        setDeviceLogLevel(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureAPNs)) {
                        configureAPNs(pendingMessage);
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
                        changeAuthKey(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_CLIENT)) {
                        changeAuthKey(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS)) {
                        changeEncryptionKey(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_CLIENT)) {
                        changeEncryptionKey(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS)) {
                        changeMasterKey(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_CLIENT)) {
                        changeMasterKey(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD)) {
                        changeHlsSecret(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD_FOR_CLIENT)) {
                        changeHlsSecret(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.EXPORT_END_DEVICE_CERTIFICATE)) {
                        collectedMessage = exportEndDeviceCertificate(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.EXPORT_SUB_CA_CERTIFICATES)) {
                        collectedMessage = exportSubCACertificates(collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.EXPORT_ROOT_CA_CERTIFICATE)) {
                        collectedMessage = exportRootCACertificate(collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.IMPORT_CA_CERTIFICATE)) {
                        importCACertificate(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.IMPORT_CLIENT_END_DEVICE_CERTIFICATE)) {
                        importClientEndDeviceCertificate(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.IMPORT_SERVER_END_DEVICE_CERTIFICATE)) {
                        importServerEndDeviceCertificate(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.DELETE_CERTIFICATE_BY_SERIAL_NUMBER)) {
                        deleteCertificateBySerialNumber(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.DELETE_CERTIFICATE_BY_TYPE)) {
                        deleteCertificateByType(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.GENERATE_KEY_PAIR)) {
                        generateKeyPair(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.GENERATE_CSR)) {
                        collectedMessage = generateCSR(collectedMessage, pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ActivateFirewall)) {
                        activateFirewall();
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.DeactivateFirewall)) {
                        deactivateFirewall();
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWGPRS)) {
                        configureFWGPRS(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWLAN)) {
                        configureFWLAN(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWWAN)) {
                        configureFWWAN(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(DLMSConfigurationDeviceMessage.MeterPushNotificationSettings)) {
                        configureDLMSGateway(pendingMessage);
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
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableNetworkInterfacesForSetupObject)) {
                        enableInterfacesForSetupObject(pendingMessage);
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
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY)) {
                        this.changeHLSSecretUsingServiceKey(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.COPY_ACTIVE_FIRMWARE_TO_INACTIVE_PARTITION)) {
                        copyActiveFirmwareToInactive();
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_SINGLE_ALARM_REGISTER)) {
                        resetAlarmDescriptor(pendingMessage);
                        collectedMessage.setDeviceProtocolInformation("Alarm description reset for " + Beacon3100RegisterFactory.ALARM_DESCRIPTOR);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_SINGLE_REGISTER)) {
                        resetAllAlarmBits();
                        collectedMessage.setDeviceProtocolInformation("Alarm bits reset for " + Beacon3100RegisterFactory.ALARM_BITS_REGISTER);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.WRITE_FILTER_FOR_SINGLE_ALARM_REGISTER)) {
                        writeAlarmFilter(pendingMessage);
                        collectedMessage.setDeviceProtocolInformation("Alarm filter written in " + Beacon3100RegisterFactory.ALARM_FILTER);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_CIPHERING)) {
                        collectedMessage = configurePushSetupNotificationCiphering(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_SEND_TEST_NOTIFICATION)) {
                        collectedMessage = configurePushSetupSendTestNotification(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SetBufferForAllLoadProfiles)) {
                        collectedMessage = getMasterDataSync().setBufferForAllLoadProfiles(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SetBufferForSpecificLoadProfile)) {
                        collectedMessage = getMasterDataSync().setBufferForSpecificLoadProfile(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SetBufferForAllEventLogs)) {
                        collectedMessage = getMasterDataSync().setBufferForAllEventLogs(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SetBufferForSpecificEventLog)) {
                        collectedMessage = getMasterDataSync().setBufferForSpecificEventLog(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SetBufferForAllRegisters)) {
                        collectedMessage = getMasterDataSync().setBufferForAllRegisters(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SetBufferForSpecificRegister)) {
                        collectedMessage = getMasterDataSync().setBufferForSpecificRegister(pendingMessage, collectedMessage);
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
            } catch (IndexOutOfBoundsException | NullPointerException | IllegalArgumentException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.toString());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            } finally {
                result.addCollectedMessage(collectedMessage);
            }
        }

        return result;
    }

    private void removeLogicalDevice(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String mac = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.clientMacAddress).getValue();
        getLogger().info("Removing client with MAC address: " + mac);
        final ConcentratorSetup concentratorSetup = readOldObisCodes() ? this.getCosemObjectFactory().getConcentratorSetup() : this.getCosemObjectFactory().getConcentratorSetup(CONCENTRATOR_SETUP_NEW_LOGICAL_NAME);
        byte[] macAddress = ProtocolTools.getBytesFromHexString(mac.replace(":", ""), 2);
        concentratorSetup.removeLogicalDevice(macAddress);
        getLogger().info(" - removed ok");
    }

    //Sub classes can override this implementation
    protected CollectedMessage changeHLSSecretUsingServiceKey(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        throw new ProtocolException("Service keys can only be injected by the HSM crypto-protocol");
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
        String encodedCertificateString = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.CACertificateAliasAttributeName).getValue();
        if (encodedCertificateString == null || encodedCertificateString.isEmpty()) {
            throw new ProtocolException("The certificate with the specified alias does not exist in the EIServer persisted trust store");
        }

        byte[] encodedCertificate = ProtocolTools.getBytesFromHexString(encodedCertificateString, "");
        getCosemObjectFactory().getSecuritySetup().importCertificate(encodedCertificate);
    }

    /**
     * Imports an X.509 v3 certificate of a public key of this client (ComServer)
     * The Beacon recognizes the entity by the Common Name (CN) of the certificate.
     * In case of client (ComServer) certificates, this is the system title of the ComServer.
     * <p/>
     * The Beacon recognizes the certificate type (signing/key agreement/TLS) by the certificate extension(s)
     */
    private void importClientEndDeviceCertificate(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String encodedCertificateString = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.clientCertificateAliasAttributeName).getValue();
        if (encodedCertificateString == null || encodedCertificateString.isEmpty()) {
            throw new ProtocolException("The certificate with the specified alias does not exist in the EIServer persisted key store");
        }

        byte[] encodedCertificate = ProtocolTools.getBytesFromHexString(encodedCertificateString, "");
        getCosemObjectFactory().getSecuritySetup().importCertificate(encodedCertificate);
    }

    /**
     * Imports an X.509 v3 certificate of a public key of a server end-device.
     * The Beacon recognizes the entity by the Common Name (CN) of the certificate.
     * In case of server (Beacon) certificates, this is the system title of the Beacon device.
     * <p/>
     * The Beacon recognizes the certificate type (signing/key agreement/TLS) by the certificate extension(s)
     */
    private void importServerEndDeviceCertificate(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String base64EncodedCertificate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.certificateWrapperAttributeName).getValue();
        if (base64EncodedCertificate == null || base64EncodedCertificate.isEmpty()) {
            throw new ProtocolException("The CertificateWrapper with the given ID does not exist in the EIServer database");
        }

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
        List<com.energyict.mdc.upl.security.CertificateAlias> subCACertificateAliases = new ArrayList<>();
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
                CertificateAlias certificateAlias = certificateAliasFinder.newFrom(alias, getEncodedCertificate(x509Certificate));
                subCACertificateAliases.add(certificateAlias);

                protocolInfo.append(alias);
            }
        }

        if (subCACertificateAliases.isEmpty()) {
            collectedMessage.setDeviceProtocolInformation("The Beacon device contained no sub-CA certificates");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        } else {
            collectedMessage = this.getCollectedDataFactory().createCollectedMessageWithCertificates(
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
        List<com.energyict.mdc.upl.security.CertificateAlias> rootCACertificateAliases = new ArrayList<>();
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
                CertificateAlias certificateAlias = certificateAliasFinder.newFrom(alias, getEncodedCertificate(x509Certificate));
                rootCACertificateAliases.add(certificateAlias);

                protocolInfo.append(alias);
            }
        }

        if (rootCACertificateAliases.isEmpty()) {
            collectedMessage.setDeviceProtocolInformation("The Beacon device contained no self-signed root-CA certificate.");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        } else {
            collectedMessage = this.getCollectedDataFactory().createCollectedMessageWithCertificates(
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
        CollectedCertificateWrapper collectedCertificateWrapper = this.getCollectedDataFactory().createCollectedCertificateWrapper(x509Certificate);

        //Server certificate for signing/key agreement is modelled as a security property
        if (SecurityMessage.CertificateType.DigitalSignature.equals(certificateType) || SecurityMessage.CertificateType.KeyAgreement.equals(certificateType)) {
            if (SecurityMessage.CertificateType.DigitalSignature.equals(certificateType)) {
                propertyName = SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString();
            } else {
                propertyName = SecurityPropertySpecName.SERVER_KEY_AGREEMENT_CERTIFICATE.toString();
            }

            //Note that updating the alias security property will also add the given certificate under that alias, to the DLMS key store.
            //If the key store already contains a certificate for that alias, an error is thrown, and the security property will not be updated either.
            collectedMessage = this.getCollectedDataFactory().createCollectedMessageWithUpdateSecurityProperty(
                    new DeviceIdentifierById(getProtocol().getOfflineDevice().getId()),
                    collectedMessage.getMessageIdentifier(),
                    propertyName,
                    collectedCertificateWrapper);

            //Server certificate for TLS is modelled as a general property
        } else if (SecurityMessage.CertificateType.TLS.equals(certificateType)) {
            propertyName = DlmsSessionProperties.SERVER_TLS_CERTIFICATE;

            //Note that updating the alias general property will also add the given certificate under that alias, to the DLMS key store.
            //If the key store already contains a certificate for that alias, an error is thrown, and the general property will not be updated either.
            collectedMessage = this.getCollectedDataFactory().createCollectedMessageWithUpdateGeneralProperty(
                    new DeviceIdentifierById(getProtocol().getOfflineDevice().getId()),
                    collectedMessage.getMessageIdentifier(),
                    propertyName,
                    collectedCertificateWrapper);
        }

        String msg = "Property '" + propertyName + "' on the Beacon device is updated with the ID referring to the new CertificateWrapper. This represents the server end-device certificate, with serial number '" + x509Certificate.getSerialNumber().toString() + "' and issuerDN '" + x509Certificate.getIssuerDN().getName() + "').";
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
            byte[] oldEncryptionKey = getProtocol().getDlmsSession().getProperties().getSecurityProvider().getGlobalKey();
            if (!Arrays.equals(oldEncryptionKey, agreedKey)) { //reset FC values after the EK key change
                securityContext.setFrameCounter(1);
                securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
            }
        } else if (keyId == 2) {
            securityPropertyName = SecurityPropertySpecName.AUTHENTICATION_KEY.toString();
            getProtocol().getDlmsSessionProperties().getSecurityProvider().changeAuthenticationKey(agreedKey);
        }

        //Special kind of collected message: it includes the update of the relevant security property with the new, agreed key.
        collectedMessage = this.getCollectedDataFactory().createCollectedMessageWithUpdateSecurityProperty(
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

        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getValue();
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
            protocolConfiguration = objectMapperService.newJacksonMapper().readValue(new StringReader(jsonObject.toString()), MulticastProtocolConfiguration.class);
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
            protocolConfiguration = objectMapperService.newJacksonMapper().readValue(new StringReader(jsonObject.toString()), MulticastProtocolConfiguration.class);
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

        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getValue();
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

            CollectedRegister deviceRegister = this.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterDataIdentifierByObisCodeAndDevice(MULTICAST_METER_PROGRESS, new DialHomeIdDeviceIdentifier(macAddress)));
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
    private void triggerPreliminaryProtocol(final String macAddress, final String protocolName) throws IOException {
        if (getLogger().isLoggable(Level.INFO)) {
            getLogger().log(Level.INFO, "Triggering preliminary protocol for meter [" + macAddress + "], using protocol [" + protocolName + "]");
        }

        final byte[] mac = ParseUtils.hexStringToByteArray(macAddress);

        final ConcentratorSetup concentratorSetup = readOldObisCodes() ? this.getCosemObjectFactory().getConcentratorSetup() : this.getCosemObjectFactory().getConcentratorSetup(CONCENTRATOR_SETUP_NEW_LOGICAL_NAME);
        concentratorSetup.triggerPreliminaryProtocol(mac, protocolName);

        if (getLogger().isLoggable(Level.INFO)) {
            getLogger().log(Level.INFO, "Triggered preliminary protocol for meter [" + macAddress + "], using protocol [" + protocolName + "]");
        }
    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new Beacon3100PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice(), readOldObisCodes(), this.getCollectedDataFactory(), this.getIssueFactory());
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

    private void deactivateFirewall() throws IOException {
        getCosemObjectFactory().getFirewallSetup().deactivate();
    }

    private void activateFirewall() throws IOException {
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
        final int pingTime = getG3NetworkManagement().pingNode(hexMacAddress, ((int) timeoutInMillis) / 1000);
        getProtocol().getDlmsSession().getDLMSConnection().setTimeout(normalTimeout);

        collectedMessage.setDeviceProtocolInformation(pingTime + " ms");
        return collectedMessage;
    }

    private CollectedMessage kickMeter(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String macAddress = pendingMessage.getDeviceMessageAttributes().get(0).getValue();

        final boolean result = getG3NetworkManagement().detachNode(macAddress);

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


        final boolean result = getG3NetworkManagement().addToBlacklist(macAddresses);

        if (!result) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            final String errorMsg = "The Beacon was not able to add the meter(s) to the blacklist";
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
        }

        return collectedMessage;
    }

    private G3NetworkManagement getG3NetworkManagement() throws NotInObjectListException {
        if (readOldObisCodes()) {
            return getCosemObjectFactory().getG3NetworkManagement();
        } else {
            return getCosemObjectFactory().getG3NetworkManagement(G3_NETWORK_MANAGEMENT_NEW_OBISCODE);
        }
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

        final boolean result = getG3NetworkManagement().removeFromBlacklist(macAddresses);

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
            masterDataSync = new MasterDataSync(this, objectMapperService, this.getIssueFactory(), propertySpecService, deviceMasterDataExtractor);
        }
        return masterDataSync;
    }

    private void setSchedulerState(SchedulerState state) throws IOException {
        getScheduleManager().writeSchedulerState(state.toDLMSEnum());
    }

    private ScheduleManager getScheduleManager() throws NotInObjectListException {
        if (readOldObisCodes()) {
            return getCosemObjectFactory().getScheduleManager();
        } else {
            return getCosemObjectFactory().getScheduleManager(SCHEDULE_MANAGER_NEW_OBISCODE);
        }
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getValue();
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
        int clientId = getClientId(pendingMessage);

        getAssociationLN(clientId).changeHLSSecret(ProtocolTools.getBytesFromHexString(hex, ""));
    }

    protected AssociationLN getAssociationLN(int clientId) throws IOException {
        if (clientId != 0) {
            Beacon3100.ClientConfiguration client = Beacon3100.ClientConfiguration.getByID(clientId);

            if (client != null) {
                return getCosemObjectFactory().getAssociationLN(client.getAssociationLN());
            } else {
                throw new IOException("Could not get Beacon3100 client with id " + clientId);
            }
        }
        // legacy Beacon version Support
        return getCosemObjectFactory().getAssociationLN();
    }

    private void enableInterfacesForSetupObject(OfflineDeviceMessage pendingMessage) throws IOException {
        String attributeName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.setupObjectAttributeName).getValue();
        NetworkConnectivityMessage.BeaconSetupObject beaconSetupObject = NetworkConnectivityMessage.BeaconSetupObject.valueOf(attributeName);
        boolean isEthernetWanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ETHERNET_WAN).getValue());
        boolean isEthernetLanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ETHERNET_LAN).getValue());
        boolean isWirelessWanEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.WIRELESS_WAN).getValue());
        boolean isIp6_TunnelEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.IP6_TUNNEL).getValue());
        boolean isPlc_NetworkEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.PLC_NETWORK).getValue());
        boolean allInterfacesEnabled = isEthernetWanEnabled && isEthernetLanEnabled && isWirelessWanEnabled && isIp6_TunnelEnabled && isPlc_NetworkEnabled;

        final Set<NetworkInterfaceType> enabledInterfaces = this.getInterfacesToEnable(isEthernetWanEnabled, isEthernetLanEnabled, isWirelessWanEnabled, isIp6_TunnelEnabled, isPlc_NetworkEnabled, allInterfacesEnabled);

        switch (beaconSetupObject) {
            case Web_Portal_Config_New_ObisCode: {
                final WebPortalSetupV1 webportalSetup = this.getCosemObjectFactory().getWebPortalSetupV1(WEB_PORTAL_SETUP_OLD_OBIS);
                webportalSetup.setEnabledInterfaces(enabledInterfaces);

                break;
            }

            case Web_Portal_Config_Old_ObisCode: {
                final WebPortalSetupV1 webportalSetup = this.getCosemObjectFactory().getWebPortalSetupV1(WEB_PORTAL_CONFIG_NEW_OBISCODE);
                webportalSetup.setEnabledInterfaces(enabledInterfaces);

                break;
            }

            default: {
                Array interfacesArray = new Array();

                for (final NetworkInterfaceType enabledIface : enabledInterfaces) {
                    interfacesArray.addDataType(new TypeEnum(enabledIface.getNetworkType()));
                }

                enableInterfacesOnBeaconSetupObject(beaconSetupObject, interfacesArray);
            }
        }
    }

    private final Set<NetworkInterfaceType> getInterfacesToEnable(boolean isEthernetWanEnabled, boolean isEthernetLanEnabled, boolean isWirelessWanEnabled, boolean isIp6_TunnelEnabled, boolean isPlc_NetworkEnabled, boolean allInterfacesEnabled) {
        final Set<NetworkInterfaceType> enabledInterfaces = EnumSet.noneOf(NetworkInterfaceType.class);

        if (allInterfacesEnabled) {
            enabledInterfaces.add(NetworkInterfaceType.ALL);
        } else {
            if (isEthernetLanEnabled) enabledInterfaces.add(NetworkInterfaceType.ETHERNET_LAN);
            if (isEthernetWanEnabled) enabledInterfaces.add(NetworkInterfaceType.ETHERNET_WAN);
            if (isWirelessWanEnabled) enabledInterfaces.add(NetworkInterfaceType.WIRELESS_WAN);
            if (isPlc_NetworkEnabled) enabledInterfaces.add(NetworkInterfaceType.PLC_NETWORK);
            if (isIp6_TunnelEnabled) enabledInterfaces.add(NetworkInterfaceType.IP6_TUNNEL);
        }

        return enabledInterfaces;
    }

    private void enableInterfacesOnBeaconSetupObject(NetworkConnectivityMessage.BeaconSetupObject beaconSetupObject, Array interfacesArray) throws IOException {
        switch (beaconSetupObject) {
            case Remote_Shell_Old_ObisCode:
                getCosemObjectFactory().getRemoteShellSetup().enableInterfaces(interfacesArray);
                break;
            case Remote_Shell_New_ObisCode:
                getCosemObjectFactory().getRemoteShellSetup(REMOTE_SHELL_SETUP_NEW_OBISCODE).enableInterfaces(interfacesArray);
                break;
            case SNMP_Old_ObisCode:
                getCosemObjectFactory().getSNMPSetup().enableInterfaces(interfacesArray);
                break;
            case SNMP_New_ObisCode:
                getCosemObjectFactory().getSNMPSetup(SNMP_SETUP_NEW_OBISCODE).enableInterfaces(interfacesArray);
                break;
            case RTU_Discovery_Old_ObisCode:
                getCosemObjectFactory().getRtuDiscoverySetup().enableInterfaces(interfacesArray);
                break;
            case RTU_Discovery_New_ObisCode:
                getCosemObjectFactory().getRtuDiscoverySetup(RTU_DISCOVERY_SETUP_NEW_OBISCODE).enableInterfaces(interfacesArray);
                break;
        }
    }

    protected CollectedMessage changeEncryptionKey(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        String newKey = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.newEncryptionKeyAttributeName);
        changeKeyAndUseNewKey(pendingMessage, SecurityMessage.KeyID.GLOBAL_UNICAST_ENCRYPTION_KEY.getId(), DeviceMessageConstants.newWrappedEncryptionKeyAttributeName);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(newKey, ""));

        int clientInUse = getProtocol().getDlmsSession().getProperties().getClientMacAddress();
        int clientToChangeKeyFor = getClientId(pendingMessage);

        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
        if (clientInUse == clientToChangeKeyFor) {
            securityContext.setFrameCounter(1);
        } else {
            ((BeaconCache) getProtocol().getDeviceCache()).setTXFrameCounter(clientToChangeKeyFor, 1);
        }
        securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);

        return collectedMessage;
    }

    protected SecuritySetup getSecuritySetup() throws IOException {
        return getSecuritySetup(0); //default security Setup Object for legacy Beacon versions
    }

    protected SecuritySetup getSecuritySetup(int clientId) throws IOException {
        if (clientId != 0) {
            Beacon3100.ClientConfiguration client = Beacon3100.ClientConfiguration.getByID(clientId);

            if (client != null) {
                return getCosemObjectFactory().getSecuritySetup(client.getSecuritySetupOBIS());
            } else {
                throw new IOException("Could not get Beacon3100 client with id " + clientId);
            }
        }

        // legacy Beacon version Support
        return getCosemObjectFactory().getSecuritySetup();
    }

    protected int getClientId(OfflineDeviceMessage pendingMessage) {
        String clientIdParam = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.clientMacAddress).getValue();
        if (clientIdParam != null) {
            if (!clientIdParam.isEmpty()) {
                try {
                    return Integer.parseInt(clientIdParam);
                } catch (Exception ex) {
                    // swallow
                }
            }
        }

        return 0;
    }

    protected CollectedMessage changeAuthKey(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        String newKey = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.newAuthenticationKeyAttributeName);
        changeKeyAndUseNewKey(pendingMessage, SecurityMessage.KeyID.AUTHENTICATION_KEY.getId(), DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(newKey, ""));
        return collectedMessage;
    }

    protected CollectedMessage changeMasterKey(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        String newKey = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.newMasterKeyAttributeName);
        changeKeyAndUseNewKey(pendingMessage, SecurityMessage.KeyID.MASTER_KEY.getId(), DeviceMessageConstants.newWrappedMasterKeyAttributeName);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeMasterKey(ProtocolTools.getBytesFromHexString(newKey, ""));
        return collectedMessage;
    }

    private void changeKeyAndUseNewKey(OfflineDeviceMessage pendingMessage, int keyId, String wrappedKeyAttributeName) throws IOException {
        String newWrappedKey = getDeviceMessageAttributeValue(pendingMessage, wrappedKeyAttributeName);
        byte[] keyBytes = ProtocolTools.getBytesFromHexString(newWrappedKey, "");
        int clientId = getClientId(pendingMessage);

        Array keyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(keyId));
        keyData.addDataType(OctetString.fromByteArray(keyBytes));
        keyArray.addDataType(keyData);

        getSecuritySetup(clientId).transferGlobalKey(keyArray);
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
        getNtpServerAddress().writeNTPServerName(address);
    }

    private NTPServerAddress getNtpServerAddress() throws NotInObjectListException {
        if (readOldObisCodes()) {
            return getCosemObjectFactory().getNTPServerAddress();
        } else {
            return getCosemObjectFactory().getNTPServerAddress(TIME_SERVER_NEW_OBISCODE);
        }
    }

    private void syncNTPServer(OfflineDeviceMessage pendingMessage) throws IOException {
        getNtpServerAddress().ntpSync();
    }

    private void setDeviceName(OfflineDeviceMessage pendingMessage) throws IOException {
        if (readOldObisCodes()) {
            writeOctetStringData(pendingMessage, DEVICE_NAME_OLD_OBISCODE);
        } else {
            writeOctetStringData(pendingMessage, DEVICE_NAME_NEW_OBISCODE);
        }
    }

    private void setDeviceHostName(OfflineDeviceMessage pendingMessage) throws IOException {
        if (readOldObisCodes()) {
            writeOctetStringData(pendingMessage, DEVICE_HOST_NAME_OLD_OBISCODE);
        } else {
            writeOctetStringData(pendingMessage, DEVICE_HOST_NAME_NEW_OBISCODE);
        }
    }

    private void setDeviceLocation(OfflineDeviceMessage pendingMessage) throws IOException {
        if (readOldObisCodes()) {
            writeOctetStringData(pendingMessage, DEVICE_LOCATION_OLD_OBISCODE);
        } else {
            writeOctetStringData(pendingMessage, DEVICE_LOCATION_NEW_OBISCODE);
        }
    }

    private void writeOctetStringData(OfflineDeviceMessage pendingMessage, ObisCode objectObisCode) throws IOException {
        String name = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getData(objectObisCode).setValueAttr(OctetString.fromString(name));
    }

    private void rebootApplication() throws IOException {
        if (readOldObisCodes()) {
            getCosemObjectFactory().getLifeCycleManagement().restartApplication();
        } else {
            getCosemObjectFactory().getLifeCycleManagement(LIFE_CYCLEMANAGEMENT_NEW_OBISCODE).restartApplication();
        }
    }

    private void rebootDevice() throws IOException {
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

        getModemWatchdogConfiguration().writeExtendedConfigParameters(
                modemWatchdogInterval,
                modemWatchdogInitialDelay,
                pppDaemonResetThreshold,
                modemResetThreshold,
                systemRebootThreshold
        );
    }

    private void enableModemWatchdog(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getModemWatchdogConfiguration().enableWatchdog(enable);
    }

    private ModemWatchdogConfiguration getModemWatchdogConfiguration() throws NotInObjectListException {
        if (readOldObisCodes()) {
            return getCosemObjectFactory().getModemWatchdogConfiguration();
        } else {
            return getCosemObjectFactory().getModemWatchdogConfiguration(MODEM_WATCHDOG_NEW_OBISCODE);
        }

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
        getUplinkPingConfiguration().writeTimeout(timeout);
    }

    private void changePasswordUser1(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getWebPortalConfig().changeUser1Password(newPassword);
    }

    private void changePasswordUser2(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getWebPortalConfig().changeUser2Password(newPassword);
    }

    /**
     * Change the password for a particular role.
     *
     * @param pendingMessage The message.
     * @throws IOException If an IO error occurs.
     */
    private void changeUserPassword(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.usernameAttributeName).getValue();
        String newPassword = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.passwordAttributeName).getValue();

        this.getWebportalSetupICv1().setPassword(Role.forName(userName), newPassword.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Returns the {@link WebPortalSetupV1} instance.
     *
     * @return The {@link WebPortalSetupV1} instance.
     * @throws NotInObjectListException If the object is not known.
     */
    private final WebPortalSetupV1 getWebportalSetupICv1() throws NotInObjectListException {
        if (this.readOldObisCodes()) {
            return this.getCosemObjectFactory().getWebPortalSetupV1(WEB_PORTAL_SETUP_OLD_OBIS);
        } else {
            return this.getCosemObjectFactory().getWebPortalSetupV1(WEB_PORTAL_CONFIG_NEW_OBISCODE);
        }
    }

    private void writeUplinkPingInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer interval = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getUplinkPingConfiguration().writeInterval(interval);
    }

    private void writeUplinkPingDestinationAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String destinationAddress = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getUplinkPingConfiguration().writeDestAddress(destinationAddress);
    }

    private UplinkPingConfiguration getUplinkPingConfiguration() throws NotInObjectListException {
        if (readOldObisCodes()) {
            return getCosemObjectFactory().getUplinkPingConfiguration();
        } else {
            return getCosemObjectFactory().getUplinkPingConfiguration(PING_SERVICE_NEW_OBISCODE);
        }
    }

    private void enableUplinkPing(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getUplinkPingConfiguration().enableUplinkPing(enable);
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

        this.getWebportalSetupICv1().setHttpsPort(Integer.parseInt(httpPort));
    }

    private void setHttpsPort(OfflineDeviceMessage pendingMessage) throws IOException {
        String httpsPort = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SetHttpsPortAttributeName).getValue();

        this.getWebportalSetupICv1().setHttpsPort(Integer.parseInt(httpsPort));
    }

    private void setMaxLoginAttempts(OfflineDeviceMessage pendingMessage) throws IOException {
        String logAttempts = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_MAX_LOGIN_ATTEMPTS).getValue();

        this.getWebportalSetupICv1().setMaxLoginAttempts(Integer.parseInt(logAttempts));
    }

    private void setLockoutDuration(OfflineDeviceMessage pendingMessage) throws IOException {
        String duration = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_LOCKOUT_DURATION).getValue();

        this.getWebportalSetupICv1().setLockoutDuration(Long.parseLong(duration));
    }

    private void enableGzipCompression(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enableGzip = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ENABLE_GZIP_COMPRESSION).getValue());

        this.getWebportalSetupICv1().setGzipEnabled(enableGzip);
    }

    private void enableSSL(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enableSSL = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.enableSSL).getValue());

        this.getWebportalSetupICv1().setGzipEnabled(enableSSL);
    }

    private void setAuthenticationMechanism(OfflineDeviceMessage pendingMessage) throws IOException {
        String authName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SET_AUTHENTICATION_MECHANISM).getValue();
        int auth = AuthenticationMechanism.fromAuthName(authName);

        this.getWebportalSetupICv1().setWebPortalAuthenticationMechanism(WebPortalAuthenticationMechanism.forValue(auth));
    }

    private void setDeviceLogLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        String logLevel = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceLogLevel).getValue();
        int level = ConfigurationChangeDeviceMessage.DeviceLogLevel.valueOf(logLevel).getId();
        getCosemObjectFactory().getConcentratorSetup().setDeviceLogLevel(new TypeEnum(level));
    }

    private void configureDLMSGateway(OfflineDeviceMessage pendingMessage) throws IOException {
        String relayOptionName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.RelayMeterNotifications).getValue();
        int relayMeterNotification = DLMSGatewayNotificationRelayType.fromOptionName(relayOptionName);
        boolean decypherMeterNotifications = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.DecipherMeterNotifications).getValue());
        boolean dropUnencryptedNotifications = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.DropUnencryptedMeterNotifications).getValue());

        getCosemObjectFactory().getDLMSGatewaySetup().setNotificationDecipher(decypherMeterNotifications);
        getCosemObjectFactory().getDLMSGatewaySetup().setNotificationRelaying(relayMeterNotification);
        getCosemObjectFactory().getDLMSGatewaySetup().setNotificationDropUnencrypted(dropUnencryptedNotifications);
    }

    private void configureAPNs(OfflineDeviceMessage pendingMessage) throws IOException {
        final int activeApn = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activeAPN).getValue());
        final String apnConfigurations = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.apnConfigurations).getValue();
        if (readOldObisCodes()) {
            getCosemObjectFactory().getData(MULTI_APN_COFIG_OLD_OBISCODE).setValueAttr(createApnConfigs(activeApn, apnConfigurations));
        } else {
            getCosemObjectFactory().getData(MULTI_APN_COFIG_NEW_OBISCODE).setValueAttr(createApnConfigs(activeApn, apnConfigurations));
        }
    }

    private Structure createApnConfigs(final int activeApn, final String providedAPNConfigurations) throws ProtocolException {
        Structure apnConfiguration = new Structure();
        Array apnConfigs = new Array();
        List<String> apnConfigList = Arrays.asList(providedAPNConfigurations.trim().split(";"));
        if (apnConfigList.size() == 0) {
            throw new ProtocolException("Provided list of APNs is empty. Please provide the correct configuration");
        }
        for (String apnConfig : apnConfigList) {
            String[] configEntries = apnConfig.trim().split(",");
            if (configEntries.length != 3) {
                throw new ProtocolException("The expected number of entries for an apn config is 3 and we receive " + configEntries.length + ". Please provide the correct configuration");
            }
            String apnName = configEntries[0].trim();
            String userName = configEntries[1].trim();
            String password = configEntries[2].trim();

            Structure apnConfigStructure = new Structure();
            apnConfigStructure.addDataType(OctetString.fromString(apnName));
            apnConfigStructure.addDataType(OctetString.fromString(userName));
            apnConfigStructure.addDataType(OctetString.fromString(password));
            apnConfigs.addDataType(apnConfigStructure);
        }

        apnConfiguration.addDataType(new Unsigned8(activeApn));
        apnConfiguration.addDataType(apnConfigs);
        return apnConfiguration;
    }

    private CollectedMessage copyActiveFirmwareToInactive() throws ProtocolException {
        try {
            InactiveFirmwareIC inactiveFirmwareIC = getCosemObjectFactory().getInactiveFirmwareIC();
            inactiveFirmwareIC.copyActiveFirmwareToInactiveFirmware();
        } catch (NotInObjectListException e) {
            throw new ProtocolException(e, "Inactive firmware IC object (class_id = 20027, version = 0, logical_name = 0.128.96.132.0.255) not found in object list." + e.getMessage());
        } catch (IOException e) {
            throw new ProtocolException(e, "Calling method copy_active_firmware_to_inactive_firmware from Inactive firmware IC object (class_id = 20027, version = 0, logical_name = 0.128.96.132.0.255) failed." + e.getMessage());
        }

        return null;
    }

    /**
     * Performs a reset on a {@link ProfileGeneric}.
     *
     * @param obisCode The OBIS code.
     * @throws IOException If an IO error occurs.
     */
    private void resetLogbook(final ObisCode obisCode) throws IOException {
        this.getCosemObjectFactory().getProfileGeneric(obisCode).reset();
    }

    /**
     * Returns the {@link Logger} instance to be used.
     *
     * @return The logger instance to be used.
     */
    private Logger getLogger() {
        return this.getProtocol().getLogger();
    }


    private void resetAlarmDescriptor(OfflineDeviceMessage pendingMessage) throws IOException {
        BigDecimal alarmBits = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmBitMaskAttributeName).getValue());
        getCosemObjectFactory().getData(ObisCode.fromString(Beacon3100RegisterFactory.ALARM_DESCRIPTOR)).setValueAttr(new BitString(alarmBits.longValue(), 45));
    }

    protected void resetAllAlarmBits() throws IOException {
        Data data = getCosemObjectFactory().getData(ObisCode.fromString(Beacon3100RegisterFactory.ALARM_BITS_REGISTER));
        data.setValueAttr(new BitString(0, 45)); // to reset the alarm bits we have to write zero back to the register
    }

    protected void writeAlarmFilter(OfflineDeviceMessage pendingMessage) throws IOException {
        BigDecimal filter = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmFilterAttributeName).getValue());
        Data data = getProtocol().getDlmsSession().getCosemObjectFactory().getData(ObisCode.fromString(Beacon3100RegisterFactory.ALARM_FILTER));
        data.setValueAttr(new BitString(filter.longValue(), 45));
    }

    private CollectedMessage configurePushSetupNotificationCiphering(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int notificationCiphering = AlarmConfigurationMessage.NotificationCipheringType.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.notificationCiphering).getValue()).getId();
        ObisCode pushSetupObisCode = EventPushNotificationConfig.getDefaultObisCode();
        EventPushNotificationConfig eventPushNotificationConfig = getCosemObjectFactory().getEventPushNotificationConfig(pushSetupObisCode);
        eventPushNotificationConfig.writeNotificationCiphering(notificationCiphering);

        return collectedMessage;
    }

    private CollectedMessage configurePushSetupSendTestNotification(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String echoTestNotification = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.echoTestNotification).getValue();
        ObisCode pushSetupObisCode = EventPushNotificationConfig.getDefaultObisCode();
        EventPushNotificationConfig eventPushNotificationConfig = getCosemObjectFactory().getEventPushNotificationConfig(pushSetupObisCode);

        eventPushNotificationConfig.setSendTestNotificationMethod(echoTestNotification);
        return collectedMessage;
    }

    public boolean readOldObisCodes() {
        return getBeacon3100Properties().getReadOldObisCodes();
    }
}