/**
 * @version 2.0
 * @author Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the DLMS COSEM meter protocol of the Iskra ME37x meter with LN referencing.
 * <BR>
 * <B>@beginchanges</B><BR>
KV|11042007|Initial version
KV|23072007|Work around due to a bug in the meter to allow requesting more then 1 day of load profile for data compression meters
GN|03032008|Added external MBus functionality
GN|07112008|Only read the MBus unit when mbus is enabled, older meters don't have the MBus register...
 * @endchanges
 */
package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.HDLCConnection;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.DemandResetProtocol;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.dlms.CapturedObjects;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.google.common.base.Supplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

public class IskraME37X extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, CacheMechanism, RegisterProtocol, MessageProtocol, DemandResetProtocol, SerialNumberSupport {

    private static final byte DEBUG = 0;  // KV 16012004 changed all DEBUG values
    private static final byte DL_COSEMPDU_DATA_OFFSET = 0x07;
    private static final byte AARE_APPLICATION_CONTEXT_NAME = (byte) 0xA1;
    private static final byte AARE_RESULT = (byte) 0xA2;
    private static final byte AARE_RESULT_SOURCE_DIAGNOSTIC = (byte) 0xA3;
    private static final byte AARE_USER_INFORMATION = (byte) 0xBE;
    private static final byte AARE_TAG = 0x61;
    private static final byte ACSE_SERVICE_USER = (byte) 0xA1;
    private static final byte ACSE_SERVICE_PROVIDER = (byte) 0xA2;
    private static final byte DLMS_PDU_INITIATE_RESPONSE = (byte) 0x08;
    private static final byte DLMS_PDU_CONFIRMED_SERVICE_ERROR = (byte) 0x0E;
    private static final int MAX_ADDRESS_LENGTH = 16;

    private static final int iNROfIntervals = 50000;

    private static final int ELECTRICITY = 0x00;
    private static final int MBUS = 0x01;

    private static final String CONNECT = "CONNECT";
    private static final String DISCONNECT = "DISCONNECT";

    private static final byte[] connectMsg = new byte[]{0x11, 0x01};
    private static final byte[] disconnectMsg = new byte[]{0x11, 0x00};
    private final PropertySpecService propertySpecService;

    private DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("ISK");
    private DLMSCache dlmsCache = new DLMSCache();
    private Logger logger = null;
    private TimeZone timeZone = null;

    private String strID = null;
    private String strPassword = null;
    private String serialNumber = null;
    private String rtuType = null;
    private String firmwareVersion;

    private List messages = new ArrayList(9);

    private int iInterval = 0;
    private int iHDLCTimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iSecurityLevelProperty;
    private int iRequestTimeZone;
    private int iRoundtripCorrection;
    private int iClientMacAddress;
    private int iServerUpperMacAddress;
    private int iServerLowerMacAddress;
    private int extendedLogging;
    private int dataContainerOffset = -1;
    public static int metertype = -1;

    private int numberOfChannels = -1;
    private int configProgramChanges = -1;
    private int deviation = -1;
    private int addressingMode;
    private int connectionMode;

    private String version = null;
    private String serialnr = null;
    private String nodeId;

    private static final byte[] aarqlowlevel17 = {
            (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
            (byte) 0x60, // AARQ
            (byte) 0x37, // bytes to follow
            (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01, //application context name , LN no ciphering
            (byte) 0xAA, (byte) 0x02, (byte) 0x07, (byte) 0x80, // ACSE requirements
            (byte) 0xAB, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01};

    private static final byte[] aarqlowlevel17_2 = {
            (byte) 0xBE, (byte) 0x0F, (byte) 0x04, (byte) 0x0D,
            (byte) 0x01, // initiate request
            (byte) 0x00, (byte) 0x00, (byte) 0x00, // unused parameters
            (byte) 0x06,  // dlms version nr
            (byte) 0x5F, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x1D, // proposed conformance
            (byte) 0x21, (byte) 0x34};

    private static final byte[] aarqlowlevelANY = {
            (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
            (byte) 0x60, // AARQ
            (byte) 0x36, // bytes to follow
            (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07,
            (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01, //application context name , LN no ciphering
            (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, // ACSE requirements
            (byte) 0x8B, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01};

    private static final byte[] aarqlowlevelANY_2 = {(byte) 0xBE, (byte) 0x10, (byte) 0x04, (byte) 0x0E,
            (byte) 0x01, // initiate request
            (byte) 0x00, (byte) 0x00, (byte) 0x00, // unused parameters
            (byte) 0x06,  // dlms version nr
            (byte) 0x5F, (byte) 0x1F, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x1D, // proposed conformance
            (byte) 0x21, (byte) 0x34};

    private static final byte[] aarqlowlevelOLD = {
            (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
            (byte) 0x60, // AARQ
            (byte) 0x35, // bytes to follow
            (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07,
            (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01, //application context name , LN no ciphering
            (byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, // ACSE requirements
            (byte) 0x8B, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x01};

    private static final byte[] aarqlowlevelOLD_2 = {(byte) 0xBE, (byte) 0x0F, (byte) 0x04, (byte) 0x0D,
            (byte) 0x01, // initiate request
            (byte) 0x00, (byte) 0x00, (byte) 0x00, // unused parameters
            (byte) 0x06,  // dlms version nr
            (byte) 0x5F, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x1D, // proposed conformance
            (byte) 0x21, (byte) 0x34};

    private static final byte[] aarqlowestlevel = {
            (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
            (byte) 0x60, // AARQ
            (byte) 0x1D, // bytes to follow
            (byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x01, //application context name , LN no ciphering
            (byte) 0xBE, (byte) 0x10, (byte) 0x04, (byte) 0x0E,
            (byte) 0x01, // initiate request
            (byte) 0x00, (byte) 0x00, (byte) 0x00, // unused parameters
            (byte) 0x06,  // dlms version nr
            (byte) 0x5F, (byte) 0x1F, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x7E, (byte) 0x1F, // proposed conformance
            (byte) 0xFF, (byte) 0xFF};

    private CapturedObjects capturedObjects = null;
    private DLMSConnection dlmsConnection = null;
    private CosemObjectFactory cosemObjectFactory = null;
    private ObisCodeMapper ocm = null;

    private ObisCode loadProfileObisCode = null;
    private ObisCode loadProfileObisCode1 = ObisCode.fromString("1.0.99.1.0.255");
    private ObisCode loadProfileObisCode2 = ObisCode.fromString("1.0.99.2.0.255");
    private ObisCode loadProfileObisCode97 = ObisCode.fromString("1.0.99.97.0.255");
    private ObisCode breakerObisCode = ObisCode.fromString("0.0.128.30.21.255");
    private ObisCode eventLogObisCode = ObisCode.fromString("1.0.99.98.0.255");

    public IskraME37X(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;

        // lazy initializing
        numberOfChannels = -1;
        configProgramChanges = -1;
        iInterval = 0;
        version = null;
        serialnr = null;

        try {
            cosemObjectFactory = new CosemObjectFactory(this);
            if (connectionMode == 0) {
                dlmsConnection = new HDLCConnection(inputStream, outputStream, iHDLCTimeoutProperty, 100, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress, iServerUpperMacAddress, addressingMode);
            } else {
                dlmsConnection = new TCPIPConnection(inputStream, outputStream, iHDLCTimeoutProperty, 100, iProtocolRetriesProperty, iClientMacAddress, iServerUpperMacAddress, getLogger());
            }
            dlmsConnection.setIskraWrapper(1);

            if (rtuType.equalsIgnoreCase("mbus")) {
                metertype = MBUS;
            } else {
                metertype = ELECTRICITY;
            }

        } catch (DLMSConnectionException e) {
            //logger.severe ("dlms: Device clock is outside tolerance window. Setting clock");
            throw new IOException(e.getMessage());
        }
    }

    private byte[] getLowLevelSecurity() {
        if ("1.7".compareTo(firmwareVersion) == 0) {
            return buildaarq(aarqlowlevel17, aarqlowlevel17_2);
        } else if ("OLD".compareTo(firmwareVersion) == 0) {
            return buildaarq(aarqlowlevelOLD, aarqlowlevelOLD_2);
        } else {
            return buildaarq(aarqlowlevelANY, aarqlowlevelANY_2);
        }
    }

    private byte[] buildaarq(byte[] aarq1, byte[] aarq2) {
        byte[] aarq = null;
        int i, t = 0;
        // prepare aarq buffer
        aarq = new byte[3 + aarq1.length + 1 + (strPassword == null ? 0 : strPassword.length()) + aarq2.length];
        // copy aarq1 to aarq buffer
        for (i = 0; i < aarq1.length; i++) {
            aarq[t++] = aarq1[i];
        }

        // calling authentification
        aarq[t++] = (byte) 0xAC; // calling authentification tag
        aarq[t++] = (byte) ((strPassword == null ? 0 : strPassword.length()) + 2); // length to follow
        aarq[t++] = (byte) 0x80; // tag representation
        // copy password to aarq buffer
        aarq[t++] = (byte) (strPassword == null ? 0 : strPassword.length());
        for (i = 0; i < (strPassword == null ? 0 : strPassword.length()); i++) {
            aarq[t++] = (byte) strPassword.charAt(i);
        }


        // copy in aarq2 to aarq buffer
        for (i = 0; i < aarq2.length; i++) {
            aarq[t++] = aarq2[i];
        }

        aarq[4] = (byte) ((aarq.length & 0xFF) - 5); // Total length of frame - headerlength

        return aarq;
    }

    private void requestApplAssoc(int iLevel) throws IOException {
        byte[] aarq;
        if (iLevel == 0) {
            aarq = aarqlowestlevel;
        } else if (iLevel == 1) {
            aarq = getLowLevelSecurity();
        } else {
            aarq = getLowLevelSecurity();
        }
        doRequestApplAssoc(aarq);

    }

    private void doRequestApplAssoc(byte[] aarq) throws IOException {
        byte[] responseData;
        responseData = getDLMSConnection().sendRequest(aarq);
        CheckAARE(responseData);
        if (DEBUG >= 2) {
            ProtocolUtils.printResponseData(responseData);
        }
    }

    private void CheckAARE(byte[] responseData) throws IOException {
        int i;
        String strResultSourceDiagnostics = "";
        InitiateResponse initiateResponse = new InitiateResponse();

        i = 0;
        while (true) {
            if (responseData[i] == AARE_TAG) {
                i += 2; // skip tag & length
                while (true) {
                    if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME) {
                        i++; // skip tag
                        i += responseData[i]; // skip length + data
                    } // if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME)

                    else if (responseData[i] == AARE_RESULT) {
                        i++; // skip tag
                        if ((responseData[i] == 3) &&
                                (responseData[i + 1] == 2) &&
                                (responseData[i + 2] == 1) &&
                                (responseData[i + 3] == 0)) {
                            // Result OK
                            return;
                        }
                        i += responseData[i]; // skip length + data
                    } // else if (responseData[i] == AARE_RESULT)

                    else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC) {
                        i++; // skip tag
                        if (responseData[i] == 5) // check length
                        {
                            if (responseData[i + 1] == ACSE_SERVICE_USER) {
                                if ((responseData[i + 2] == 3) &&
                                        (responseData[i + 3] == 2) &&
                                        (responseData[i + 4] == 1)) {
                                    if (responseData[i + 5] == 0x00) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_USER";
                                    } else if (responseData[i + 5] == 0x01) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_USER, no reason given";
                                    } else if (responseData[i + 5] == 0x02) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Application Context Name Not Supported";
                                    } else if (responseData[i + 5] == 0x0B) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Not Recognised";
                                    } else if (responseData[i + 5] == 0x0C) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Required";
                                    } else if (responseData[i + 5] == 0x0D) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Failure";
                                    } else if (responseData[i + 5] == 0x0E) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Required";
                                    } else {
                                        throw new IOException("Application Association Establishment failed, ACSE_SERVICE_USER, unknown result!");
                                    }
                                } else {
                                    throw new IOException("Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_USER,  wrong tag");
                                }
                            } // if (responseData[i+1] == ACSE_SERVICE_USER)
                            else if (responseData[i + 1] == ACSE_SERVICE_PROVIDER) {
                                if ((responseData[i + 2] == 3) &&
                                        (responseData[i + 3] == 2) &&
                                        (responseData[i + 4] == 1)) {
                                    if (responseData[i + 5] == 0x00) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER!";
                                    } else if (responseData[i + 5] == 0x01) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Reason Given!";
                                    } else if (responseData[i + 5] == 0x02) {
                                        strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Common ACSE Version!";
                                    } else {
                                        throw new IOException("Application Association Establishment Failed, ACSE_SERVICE_PROVIDER, unknown result");
                                    }
                                } else {
                                    throw new IOException("Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_PROVIDER,  wrong tag");
                                }
                            } // else if (responseData[i+1] == ACSE_SERVICE_PROVIDER)
                            else {
                                throw new IOException("Application Association Establishment Failed, result_source_diagnostic,  wrong tag");
                            }
                        } else {
                            throw new IOException("Application Association Establishment Failed, result_source_diagnostic, wrong length");
                        }

                        i += responseData[i]; // skip length + data
                    } // else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC)

                    else if (responseData[i] == AARE_USER_INFORMATION) {
                        i++; // skip tag
                        if (responseData[i + 2] > 0) { // length of octet string
                            if (DLMS_PDU_INITIATE_RESPONSE == responseData[i + 3]) {
                                initiateResponse.bNegotiatedQualityOfService = responseData[i + 4];
                                initiateResponse.bNegotiatedDLMSVersionNR = responseData[i + 5];
                                initiateResponse.lNegotiatedConformance = (ProtocolUtils.getInt(responseData, i + 8) & 0x00FFFFFF); // conformance has only 3 bytes, 24 bit
                                initiateResponse.sServerMaxReceivePduSize = ProtocolUtils.getShort(responseData, i + 12);
                                initiateResponse.sVAAName = ProtocolUtils.getShort(responseData, i + 14);
                                /*
                                System.out.println(initiateResponse.bNegotiatedDLMSVersionNR + " "+
                                                   initiateResponse.bNegotiatedQualityOfService + " "+
                                                   initiateResponse.lNegotiatedConformance + " "+
                                                   initiateResponse.sServerMaxReceivePduSize + " " +
                                                   initiateResponse.sVAAName);
                                */

                            } else if (DLMS_PDU_CONFIRMED_SERVICE_ERROR == responseData[i + 3]) {
                                if (0x01 == responseData[i + 4]) {
                                    strResultSourceDiagnostics += ", InitiateError";
                                } else if (0x02 == responseData[i + 4]) {
                                    strResultSourceDiagnostics += ", getStatus";
                                } else if (0x03 == responseData[i + 4]) {
                                    strResultSourceDiagnostics += ", getNameList";
                                } else if (0x13 == responseData[i + 4]) {
                                    strResultSourceDiagnostics += ", terminateUpload";
                                } else {
                                    throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown ConfirmedServiceError choice");
                                }

                                if (0x06 != responseData[i + 5]) {
                                    strResultSourceDiagnostics += ", No ServiceError tag";
                                }

                                if (0x00 == responseData[i + 6]) {
                                    strResultSourceDiagnostics += "";
                                } else if (0x01 == responseData[i + 6]) {
                                    strResultSourceDiagnostics += ", DLMS version too low";
                                } else if (0x02 == responseData[i + 6]) {
                                    strResultSourceDiagnostics += ", Incompatible conformance";
                                } else if (0x03 == responseData[i + 6]) {
                                    strResultSourceDiagnostics = ", pdu size too short";
                                } else if (0x04 == responseData[i + 6]) {
                                    strResultSourceDiagnostics = ", refused by the VDE handler";
                                } else {
                                    throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons ");
                                }
                            } else {
                                throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons!");
                            }

                        } // if (responseData[i+2] > 0) --> length of the octet string

                        i += responseData[i]; // skip length + data
                    } // else if (responseData[i] == AARE_USER_INFORMATION)
                    else {
                        i++; // skip tag
                        // Very tricky, suppose we receive a length > 128 because of corrupted data,
                        // then if we keep byte, it is signed and we can enter a LOOP because length will
                        // be subtracted from i!!!
                        i += (((int) responseData[i]) & 0x000000FF); // skip length + data
                    }

                    if (i++ >= (responseData.length - 1)) {
                        i = (responseData.length - 1);
                        break;
                    }
                } // while(true)

            } // if (responseData[i] == AARE_TAG)

            if (i++ >= (responseData.length - 1)) {
                i = (responseData.length - 1);
                break;
            }
        } // while(true)

        throw new IOException("Application Association Establishment Failed" + strResultSourceDiagnostics);

    }

    private CapturedObjects getCapturedObjects() throws IOException {
        if (capturedObjects == null) {
            int i;
            int j = 0;
            DataContainer dataContainer = null;
            try {
                ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(loadProfileObisCode);
                meterConfig.setCapturedObjectList(profileGeneric.getCaptureObjectsAsUniversalObjects());
                dataContainer = profileGeneric.getCaptureObjectsAsDataContainer();

                capturedObjects = new CapturedObjects(dataContainer.getRoot().element.length);
                for (i = 0; i < dataContainer.getRoot().element.length; i++) {

                    if (i >= 2) {

                        if (rtuType.equalsIgnoreCase("mbus")) {
                            if (bytesToObisString(dataContainer.getRoot().getStructure(i).getOctetString(1).getArray()).indexOf("0.1.128.50.0.255") == 0) {
                                capturedObjects.add(j,
                                        dataContainer.getRoot().getStructure(i).getInteger(0),
                                        dataContainer.getRoot().getStructure(i).getOctetString(1).getArray(),
                                        dataContainer.getRoot().getStructure(i).getInteger(2));
                                if (dataContainerOffset == -1) {
                                    dataContainerOffset = i - 2;
                                }
                                j++;
                            }
                        } else {
                            if (bytesToObisString(dataContainer.getRoot().getStructure(i).getOctetString(1).getArray()).indexOf("0.1.128.50.0.255") != 0) {
                                capturedObjects.add(j,
                                        dataContainer.getRoot().getStructure(i).getInteger(0),
                                        dataContainer.getRoot().getStructure(i).getOctetString(1).getArray(),
                                        dataContainer.getRoot().getStructure(i).getInteger(2));
                                if (dataContainerOffset == -1) {
                                    dataContainerOffset = i - 2;
                                }
                                j++;
                            }
                        }
                    } else {
                        capturedObjects.add(j,
                                dataContainer.getRoot().getStructure(i).getInteger(0),
                                dataContainer.getRoot().getStructure(i).getOctetString(1).getArray(),
                                dataContainer.getRoot().getStructure(i).getInteger(2));
                        j++;
                    }
                }
            } catch (java.lang.ClassCastException e) {
                System.out.println("Error retrieving object: " + e.getMessage());
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                System.out.println("Index error: " + e.getMessage());
            }

        }

        return capturedObjects;

    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (numberOfChannels == -1) {
            numberOfChannels = getCapturedObjects().getNROfChannels();
        }
        return numberOfChannels;
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (iInterval == 0) {
            iInterval = getCosemObjectFactory().getProfileGeneric(loadProfileObisCode).getCapturePeriod();
        }
        return iInterval;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        int iNROfIntervals = getNROfIntervals();
        Calendar fromCalendar = ProtocolUtils.getCalendar(timeZone);
        fromCalendar.add(Calendar.MINUTE, (-1) * iNROfIntervals * (getProfileInterval() / 60));
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }

    private ProfileData doGetProfileData(Calendar fromCalendar, Calendar toCalendar, boolean includeEvents) throws IOException {
        byte bNROfChannels = (byte) getNumberOfChannels();
        return doGetDemandValues(fromCalendar,
                bNROfChannels,
                includeEvents);
    }

    private ProfileData doGetDemandValues(Calendar fromCalendar, byte bNROfChannels, boolean includeEvents) throws IOException {

        ProfileData profileData = new ProfileData();
        DataContainer dataContainer = getCosemObjectFactory().getProfileGeneric(loadProfileObisCode).getBuffer(fromCalendar, ProtocolUtils.getCalendar(getTimeZone()));
        for (int channelId = 0; channelId < bNROfChannels; channelId++) {
            if (!rtuType.equalsIgnoreCase("mbus")) {
                RegisterValue scalerRegister = readRegister(capturedObjects.getProfileDataChannel(channelId));
                ScalerUnit scalerUnit = new ScalerUnit(scalerRegister.getQuantity().getUnit().getScale(),
                        scalerRegister.getQuantity().getUnit());
                ChannelInfo ci = new ChannelInfo(channelId, "IskraME37x_channel_" + channelId, scalerUnit.getEisUnit());
                if (isObisCodeCumulative(capturedObjects.getProfileDataChannel(channelId))) {
                    ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
                }
                profileData.addChannel(ci);
            } else if (bytesToObisString(capturedObjects.getProfileDataChannel(channelId).getLN()).indexOf("0.1.128.50.0.255") == 0) {
                if (DEBUG == 1) {
                    System.out.println("We got a MBUS channel");
                }
                // don't show the events on the mbus meter
                includeEvents = false;

                RegisterValue scalerRegister = readRegister(capturedObjects.getProfileDataChannel(channelId));
                ScalerUnit scalerUnit = new ScalerUnit(scalerRegister.getQuantity().getUnit().getScale(),
                        scalerRegister.getQuantity().getUnit());
                ChannelInfo ci2 = new ChannelInfo(channelId, "IskraME37x_channel_" + channelId, scalerUnit.getEisUnit());
                ci2.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
                profileData.addChannel(ci2);
            }
        }

        buildProfileData(dataContainer, profileData);

        if (includeEvents) {
            profileData.getMeterEvents().addAll(getLogbookData(fromCalendar, ProtocolUtils.getCalendar(getTimeZone())));
            // Apply the events to the channel statusvalues
            profileData.applyEvents(getProfileInterval() / 60);
        }

        return profileData;
    }

    public boolean isObisCodeCumulative(ObisCode obisCode) {
        // no abstract code AND d field time integral 1, 7 or 8 These time integrals specify values from first start of measurement (origin)
        return (obisCode.getA() != 0) && (obisCode.getC() != 0) && ((obisCode.getD() == 8) || (obisCode.getD() == 17) || (obisCode.getD() == 18));
    }

    private String bytesToObisString(byte[] channelLN) {
        String str = "";
        for (int i = 0; i < channelLN.length; i++) {
            if (i > 0) {
                str += ".";
            }
            str += "" + ((int) channelLN[i] & 0xff);
        }
        return str;
    }

    private List getLogbookData(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        Logbook logbook = new Logbook(timeZone);
        return logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(eventLogObisCode).getBuffer(fromCalendar, toCalendar));
    }

    // 0.0.1.0.0.255
    private int getProfileClockChannelIndex() throws IOException {
        for (int i = 0; i < capturedObjects.getNROfObjects(); i++) {
            if (!capturedObjects.isChannelData(i)) {
                if (ObisCode.fromByteArray(capturedObjects.getLN(i)).equals(ObisCode.fromString("0.0.1.0.0.255"))) {
                    return i;
                }
            }
        }
        throw new IOException("Iskra MT37x, no clock channel found in captureobjects!");
    }

    // 1.0.96.240.0.255
    private int getProfileStatusChannelIndex() {
        for (int i = 0; i < capturedObjects.getNROfObjects(); i++) {
            if (!capturedObjects.isChannelData(i)) {
                if (ObisCode.fromByteArray(capturedObjects.getLN(i)).equals(ObisCode.fromString("1.0.96.240.0.255"))) {
                    return i;
                }
            }
        }
        return -1;
    }


    private void buildProfileData(DataContainer dataContainer, ProfileData profileData) throws IOException {
        Calendar calendar = null;
        int i, protocolStatus;
        boolean currentAdd = true, previousAdd = true;
        IntervalData previousIntervalData = null, currentIntervalData;

        if (DEBUG >= 1) {
            dataContainer.printDataContainer();
        }

        if (dataContainer.getRoot().element.length == 0) {
            throw new IOException("No entries in Load Profile Datacontainer.");
        }

        for (i = 0; i < dataContainer.getRoot().element.length; i++) { // for all retrieved intervals
            try {
                calendar = dataContainer.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex()).toCalendar(timeZone);
            } catch (ClassCastException e) {
                // absorb
                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG> buildProfileData, ClassCastException ," + e.toString());
                }
                if (calendar != null) {
                    calendar.add(Calendar.MINUTE, (getProfileInterval() / 60));
                }
            }
            if (calendar != null) {
                if (getProfileStatusChannelIndex() != -1) {
                    protocolStatus = dataContainer.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex());
                } else {
                    protocolStatus = 0;
                }

                currentIntervalData = getIntervalData(dataContainer.getRoot().getStructure(i), calendar, protocolStatus);

                // KV 16012004
                if (DEBUG >= 1) {
                    dataContainer.getRoot().getStructure(i).print();
                    System.out.println();
                }

                if (currentAdd & !previousAdd) {
                    if (DEBUG >= 1) {
                        System.out.println("add intervals together...");
                    }
                    currentIntervalData = addIntervalData(currentIntervalData, previousIntervalData);
                }


                // Add interval data...
                if (currentAdd) {
                    profileData.addInterval(currentIntervalData);
                }

                previousIntervalData = currentIntervalData;
                previousAdd = currentAdd;

            } // if (calendar != null)

        } // for (i=0;i<dataContainer.getRoot().element.length;i++) // for all retrieved intervals

        if (DEBUG >= 1) {
            System.out.println(profileData);
        }

    }

    private IntervalData addIntervalData(IntervalData currentIntervalData, IntervalData previousIntervalData) {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime(), currentIntervalData.getEiStatus(), currentIntervalData.getProtocolStatus());
        int current, i;
        for (i = 0; i < currentCount; i++) {
            current = currentIntervalData.get(i).intValue() + previousIntervalData.get(i).intValue();
            intervalData.addValue(new Integer(current));
        }
        return intervalData;
    }


    private static final int PROFILE_STATUS_DEVICE_DISTURBANCE = 0x01;
    private static final int PROFILE_STATUS_RESET_CUMULATION = 0x10;
    private static final int PROFILE_STATUS_DEVICE_CLOCK_CHANGED = 0x20;
    private static final int PROFILE_STATUS_POWER_RETURNED = 0x40;
    private static final int PROFILE_STATUS_POWER_FAILURE = 0x80;

    private int map(int protocolStatus) {

        int eiStatus = 0;

        if ((protocolStatus & PROFILE_STATUS_DEVICE_DISTURBANCE) == PROFILE_STATUS_DEVICE_DISTURBANCE) {
            eiStatus |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((protocolStatus & PROFILE_STATUS_RESET_CUMULATION) == PROFILE_STATUS_RESET_CUMULATION) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & PROFILE_STATUS_DEVICE_CLOCK_CHANGED) == PROFILE_STATUS_DEVICE_CLOCK_CHANGED) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & PROFILE_STATUS_POWER_RETURNED) == PROFILE_STATUS_POWER_RETURNED) {
            eiStatus |= IntervalStateBits.POWERUP;
        }
        if ((protocolStatus & PROFILE_STATUS_POWER_FAILURE) == PROFILE_STATUS_POWER_FAILURE) {
            eiStatus |= IntervalStateBits.POWERDOWN;
        }

        return eiStatus;

    }

    private IntervalData getIntervalData(DataStructure dataStructure, Calendar calendar, int protocolStatus) throws IOException {
        // Add interval data...
        IntervalData intervalData = new IntervalData(new Date(((Calendar) calendar.clone()).getTime().getTime()), map(protocolStatus), protocolStatus);

        for (int t = 0; t < getCapturedObjects().getNROfChannels(); t++) {

            if (getCapturedObjects().isChannelData(getObjectNumber(t))) {
                intervalData.addValue(new Integer(dataStructure.getInteger(getObjectNumber(t) + dataContainerOffset)));
            }
        }

        return intervalData;
    }

    private int getObjectNumber(int t) throws UnsupportedException {
        for (int i = 0; i < capturedObjects.getNROfObjects(); i++) {
            if (capturedObjects.getChannelNR(i) == t) {
                return i;
            }
        }
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    private int getNROfIntervals() {
        return iNROfIntervals;
    } // private int getNROfIntervals() throws IOException

    @Override
    public void setTime() throws IOException {
        Calendar calendar = null;
        if (iRequestTimeZone != 0) {
            calendar = ProtocolUtils.getCalendar(false, requestTimeZone());
        } else {
            calendar = ProtocolUtils.initCalendar(false, timeZone);
        }
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        doSetTime(calendar);
    }

    private void doSetTime(Calendar calendar) throws IOException {
        byte[] byteTimeBuffer = new byte[14];

        byteTimeBuffer[0] = AxdrType.OCTET_STRING.getTag();
        byteTimeBuffer[1] = 12; // length
        byteTimeBuffer[2] = (byte) (calendar.get(Calendar.YEAR) >> 8);
        byteTimeBuffer[3] = (byte) calendar.get(Calendar.YEAR);
        byteTimeBuffer[4] = (byte) (calendar.get(Calendar.MONTH) + 1);
        byteTimeBuffer[5] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byte bDOW = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        byteTimeBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteTimeBuffer[7] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byteTimeBuffer[8] = (byte) calendar.get(Calendar.MINUTE);
        byteTimeBuffer[9] = (byte) calendar.get(Calendar.SECOND);
        byteTimeBuffer[10] = (byte) 0x0; // hundreds of seconds

        byteTimeBuffer[11] = (byte) (0x80);
        byteTimeBuffer[12] = (byte) 0;

        if (timeZone.inDaylightTime(calendar.getTime())) {
            byteTimeBuffer[13] = (byte) 0x80; //0x00;
        } else {
            byteTimeBuffer[13] = (byte) 0x00; //0x00;
        }

        getCosemObjectFactory().writeObject(ObisCode.fromString("0.0.1.0.0.255"), 8, 2, byteTimeBuffer);
    }

    @Override
    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock();
        Date date = clock.getDateTime();
        return date;
    }

    private boolean verifyMeterID() throws IOException {
        return (strID == null) || ("".compareTo(strID) == 0) || (strID.compareTo(getDeviceAddress()) == 0);
    }

    public String getDeviceAddress() throws IOException {
        String devId = getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(new byte[]{0, 0, 42, 0, 0, (byte) 255}), DLMSUtils.attrLN2SN(2), 1).getString();
        return devId;
    }


    // KV 19012004
    private boolean verifyMeterSerialNR() {
        return (serialNumber == null) || ("".compareTo(serialNumber) == 0) || (serialNumber.compareTo(getSerialNumber()) == 0);
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (configProgramChanges == -1) {
            configProgramChanges = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }
        return configProgramChanges;
    }

    @Override
    public ApplicationServiceObject getAso() {
        return null;
    }

    @Override
    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }
        try {
            requestApplAssoc(iSecurityLevelProperty);

            try {

                // requestSAP();  // KV 08102004 R/W denied to read SAP!!!!!
                if (DEBUG == 1) {
                    System.out.println("cache=" + dlmsCache.getObjectList() + ", confchange=" + dlmsCache.getConfProgChange() + ", ischanged=" + dlmsCache.contentChanged());
                }
                try { // conf program change and object list stuff
                    int iConf;

                    if (dlmsCache != null && dlmsCache.getObjectList() != null) {
                        meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                        try {

                            iConf = requestConfigurationProgramChanges();
                        } catch (IOException e) {

                            e.printStackTrace();

                            iConf = -1;
                            logger.severe("Iskra MT37x: Configuration change is not accessible, request object list...");
                            requestObjectList();
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                        }

                        if (iConf != dlmsCache.getConfProgChange()) {

                            if (DEBUG >= 1) {
                                System.out.println("iConf=" + iConf + ", dlmsCache.getConfProgChange()=" + dlmsCache.getConfProgChange());
                            }

                            // KV 19112003 ************************** DEBUGGING CODE ********************************
                            //System.out.println("!!!!!!!!!! DEBUGGING CODE FORCED DLMS CACHE UPDATE !!!!!!!!!!");
                            //if (true) {
                            // ****************************************************************************
                            logger.severe("Iskra MT37x: Configuration changed, request object list...");
                            requestObjectList();           // request object list again from rtu
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                            if (DEBUG >= 1) {
                                System.out.println("after requesting objectlist (conf changed)... iConf=" + iConf + ", dlmsCache.getConfProgChange()=" + dlmsCache.getConfProgChange());
                            }
                        }
                    } else { // Cache not exist
                        logger.info("Iskra MT37x: Cache does not exist, request object list.");
                        requestObjectList();
                        try {
                            iConf = requestConfigurationProgramChanges();
                            dlmsCache = new DLMSCache();
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                            if (DEBUG >= 1) {
                                System.out.println("after requesting objectlist... iConf=" + iConf + ", dlmsCache.getConfProgChange()=" + dlmsCache.getConfProgChange());
                            }
                        } catch (IOException e) {
                            iConf = -1;
                        }
                    }
                    if (!verifyMeterID()) {
                        throw new IOException("Iskra MT37x, connect, Wrong DeviceID!, settings=" + strID + ", meter=" + getDeviceAddress());
                    }

                    // KV 19012004
                    if (!verifyMeterSerialNR()) {
                        throw new IOException("Iskra MT37x, connect, Wrong SerialNR!, settings=" + serialNumber + ", meter=" + getSerialNumber());
                    }


                    if (extendedLogging >= 1) {
                        logger.info(getRegistersInfo(extendedLogging));
                    }
                } catch (IOException e) {
                    throw new IOException("connect() error, " + e.getMessage());
                }

            } catch (IOException e) {
                throw new IOException(e.getMessage());
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    /*
    *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
    */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder builder = new StringBuilder();
        Iterator it;

        // all total and rate values...
        builder.append("********************* All instantiated objects in the meter *********************\n");
        for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            builder.append(uo.getObisCode().toString()).append(" ").append(uo.getObisCode().toString()).append("\n");
        }

        builder.append("********************* Objects captured into load profile *********************\n");
        it = getCosemObjectFactory().getProfileGeneric(loadProfileObisCode).getCaptureObjects().iterator();
        while (it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject) it.next();
            builder.append(capturedObject.getLogicalName().getObisCode().toString()).append(" ").append(capturedObject.getLogicalName().getObisCode().toString()).append(" (load profile)\n");
        }

        return builder.toString();
    }


    @Override
    public void disconnect() throws IOException {
        try {
            if (dlmsConnection != null) {


                //TODO commenting this can cause multidropped meters to fail ...

//        	  byte[] responseData = getDLMSConnection().sendRequest(rlrq_APDU);
//              CheckAARE(responseData);
//              if (DEBUG >= 1) ProtocolUtils.printResponseData(responseData);

                getDLMSConnection().disconnectMAC();
            }
        } catch (DLMSConnectionException e) {
            logger.severe("DLMSLN: disconnect(), " + e.getMessage());
        }
    }

    @Override
    public void resetDemand() throws IOException {
        ScriptTable demandResetScriptTable = getCosemObjectFactory().getScriptTable(ObisCode.fromString("0.0.10.0.1.255"));
        demandResetScriptTable.execute(0);
    }

    class InitiateResponse {

        byte bNegotiatedQualityOfService;
        byte bNegotiatedDLMSVersionNR;
        long lNegotiatedConformance;
        short sServerMaxReceivePduSize;
        short sVAAName;

        InitiateResponse() {
            bNegotiatedQualityOfService = 0;
            bNegotiatedDLMSVersionNR = 0;
            lNegotiatedConformance = 0;
            sServerMaxReceivePduSize = 0;
            sVAAName = 0;
        }
    }

    /**
     * This method requests for the COSEM object list in the remote meter. A list is byuild with LN and SN references.
     * This method must be executed before other request methods.
     *
     * @throws IOException
     */
    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
    }

    public String requestAttribute(short sIC, byte[] LN, byte bAttr) throws IOException {
        return doRequestAttribute(sIC, LN, bAttr).print2strDataContainer();
    }

    private DataContainer doRequestAttribute(int classId, byte[] ln, int lnAttr) throws IOException {
        return getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(ln), DLMSUtils.attrLN2SN(lnAttr), classId).getDataContainer();
    }

    @Override
    public String getSerialNumber() {
        UniversalObject uo;
        try {
            uo = meterConfig.getSerialNumberObject();
            return getCosemObjectFactory().getGenericRead(uo).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, iProtocolRetriesProperty + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:45 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "UNAVAILABLE";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.stringSpecOfMaxLength(ADDRESS.getName(), MAX_ADDRESS_LENGTH),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec(SECURITYLEVEL.getName()),
                this.integerSpec("RequestTimeZone"),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec("ClientMacAddress"),
                this.integerSpec("ServerUpperMacAddress"),
                this.integerSpec("ServerLowerMacAddress"),
                this.integerSpec("FirmwareVersion"),
                this.stringSpec(NODEID.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.integerSpec("ExtendedLogging"),
                this.integerSpec("LoadProfileId", 1, 2, 97),
                this.integerSpec("AddressingMode"),
                this.integerSpec("Connection"),
                this.integerSpec("DeviceType"));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec stringSpecOfMaxLength(String name, int length) {
        return this.spec(name, () -> this.propertySpecService.stringSpecOfMaximumLength(length));
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    private PropertySpec integerSpec(String name, Integer... validValues) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::integerSpec)
                .addValues(validValues)
                .markExhaustive()
                .finish();
    }

    @Override
    public void setProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            strPassword = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            iHDLCTimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "10000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "10").trim());
            iSecurityLevelProperty = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1").trim());
            iRequestTimeZone = Integer.parseInt(properties.getTypedProperty("RequestTimeZone", "0").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());

            iClientMacAddress = Integer.parseInt(properties.getTypedProperty("ClientMacAddress", "100").trim());
            iServerUpperMacAddress = Integer.parseInt(properties.getTypedProperty("ServerUpperMacAddress", "1").trim());
            iServerLowerMacAddress = Integer.parseInt(properties.getTypedProperty("ServerLowerMacAddress", "17").trim());
            firmwareVersion = properties.getTypedProperty("FirmwareVersion", "ANY");
            nodeId = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "");
            // KV 19012004 get the serialNumber
            serialNumber = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0"));

            if (Integer.parseInt(properties.getTypedProperty("LoadProfileId", "1")) == 1) {
                loadProfileObisCode = loadProfileObisCode1;
            } else if (Integer.parseInt(properties.getTypedProperty("LoadProfileId", "1")) == 2) {
                loadProfileObisCode = loadProfileObisCode2;
            } else if (Integer.parseInt(properties.getTypedProperty("LoadProfileId", "1")) == 97) {
                loadProfileObisCode = loadProfileObisCode97;
            } else {
                throw new InvalidPropertyException("IskraME37X, validateProperties, invalid LoadProfileId, " + Integer.parseInt(properties.getTypedProperty("LoadProfileId", "1")));
            }

            addressingMode = Integer.parseInt(properties.getTypedProperty("AddressingMode", "2"));
            connectionMode = Integer.parseInt(properties.getTypedProperty("Connection", "0")); // 0=HDLC, 1= TCP/IP

            rtuType = properties.getTypedProperty("DeviceType", "");

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public String getRegister(String name) throws IOException {
        return doGetRegister(name);
    }

    private String doGetRegister(String name) throws IOException {
        boolean classSpecified = false;
        if (name.indexOf(':') >= 0) {
            classSpecified = true;
        }
        DLMSObis ln = new DLMSObis(name);
        if (ln.isLogicalName()) {
            if (classSpecified) {
                return requestAttribute(ln.getDLMSClass(), ln.getLN(), (byte) ln.getOffset());
            } else {
                UniversalObject uo = getMeterConfig().getObject(ln);
                return getCosemObjectFactory().getGenericRead(uo).getDataContainer().print2strDataContainer();
            }
        } else {
            throw new NoSuchRegisterException("IskraME37x,getRegister, register " + name + " does not exist.");
        }
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    public int requestTimeZone() throws IOException {
        if (deviation == -1) {
            Clock clock = getCosemObjectFactory().getClock();
            deviation = clock.getTimeZone();
        }
        return (deviation);
    }

    @Override
    public void setCache(Serializable cacheObject) {
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    @Override
    public Serializable getCache() {
        return dlmsCache;
    }

    @Override
    public Serializable fetchCache(int deviceId, Connection connection) throws SQLException, ProtocolCacheFetchException {
        if (deviceId != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId);
            RtuDLMS rtu = new RtuDLMS(deviceId);
            return new DLMSCache(rtuCache.getObjectList(connection), rtu.getConfProgChange(connection));
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    @Override
    public void updateCache(int deviceId, Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
        if (deviceId != 0) {
            DLMSCache dc = (DLMSCache) cacheObject;
            if (dc.contentChanged()) {
                RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId);
                RtuDLMS rtu = new RtuDLMS(deviceId);
                rtuCache.saveObjectList(dc.getObjectList(), connection);
                rtu.setConfProgChange(dc.getConfProgChange(), connection);
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, iHDLCTimeoutProperty, iProtocolRetriesProperty, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, nodeId);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }

    @Override
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    @Override
    public int getRoundTripCorrection() {
        return iRoundtripCorrection;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
    }

    /**
     * Getter for property cosemObjectFactory.
     *
     * @return Value of property cosemObjectFactory.
     */
    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    @Override
    public String getFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + strID + "_" + strPassword + "_" + serialNumber + "_" + iServerUpperMacAddress + "_IskraME37x.cache";
    }

    @Override
    public StoredValues getStoredValues() {
        return null;    // not used; custom implementation in ObisCodeMapper class
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            if (ocm == null) {
                ocm = new ObisCodeMapper(this);
            }
            return ocm.getRegisterValue(obisCode);
        } catch (Exception e) {
            throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
        }
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        Iterator it = messageEntries.iterator();
        while (it.hasNext()) {
            MessageEntry messageEntry = (MessageEntry) it.next();
            if (DEBUG == 1) {
                System.out.println(messageEntry);
            }

            if (messageEntry.getContent().substring(messageEntry.getContent().indexOf("<") + 1, messageEntry.getContent().indexOf(">")).equals(CONNECT)) {
                messages.add(connectMsg);
            } else if (messageEntry.getContent().substring(messageEntry.getContent().indexOf("<") + 1, messageEntry.getContent().indexOf(">")).equals(DISCONNECT)) {
                messages.add(disconnectMsg);
            }

        }

    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (isItThisMessage(messageEntry, RtuMessageConstant.DEMAND_RESET)) {
                getLogger().info("Sending message DemandReset.");
                resetDemand();
                getLogger().info("DemandReset message successful.");
                return MessageResult.createSuccess(messageEntry);
            } else {
                cosemObjectFactory.writeObject(breakerObisCode, 1, 2, (byte[]) messages.get(0));
                messages.remove(0);

                BigDecimal breakerState = readRegister(breakerObisCode).getQuantity().getAmount();

                switch (breakerState.intValue()) {

                    case 0: {
                        if (messageEntry.getContent().substring(messageEntry.getContent().indexOf("<") + 1, messageEntry.getContent().indexOf(">")).equals(DISCONNECT)) {
                            return MessageResult.createSuccess(messageEntry);
                        } else {
                            return MessageResult.createFailed(messageEntry);
                        }
                    }

                    case 1: {
                        if (messageEntry.getContent().substring(messageEntry.getContent().indexOf("<") + 1, messageEntry.getContent().indexOf(">")).equals(CONNECT)) {
                            return MessageResult.createSuccess(messageEntry);
                        } else {
                            return MessageResult.createFailed(messageEntry);
                        }
                    }

                    default:
                        return MessageResult.createFailed(messageEntry);
                }
            }
        } catch (IOException e) {
            getLogger().info("Message failed : " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    /**
     * Checks if the given MessageEntry contains the corresponding MessageTag
     *
     * @param messageEntry the given messageEntry
     * @param messageTag   the tag to check
     * @return true if this is the message, false otherwise
     */
    protected boolean isItThisMessage(MessageEntry messageEntry, String messageTag) {
        return messageEntry.getContent().contains(messageTag);
    }

    @Override
    public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("IskraMT372Messages");

        MessageSpec msgSpec = addBasicMsg("Disconnect meter", DISCONNECT, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Connect meter", CONNECT, false);
        cat.addMessageSpec(msgSpec);

        /* Probably for the "knijpen" */
//        msgSpec = addBasicMsg("Limit current to 6A", "LIMITCURRENT6A", false);
//        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        theCategories.add(ProtocolMessageCategories.getDemandResetCategory());
        return theCategories;
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = it.next();
            if (att.getValue() == null || att.getValue().isEmpty()) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.isEmpty()) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    @Override
    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}