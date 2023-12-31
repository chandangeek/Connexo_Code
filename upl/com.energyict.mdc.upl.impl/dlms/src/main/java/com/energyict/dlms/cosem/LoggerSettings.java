package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.LoggerSettingsAttributes;
import com.energyict.dlms.cosem.methods.LoggerSettingsMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class LoggerSettings extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.4.255");
    public static final ObisCode BEACON_DEBUG_LOG_OBIS_CODE = ObisCode.fromString("0.192.96.128.0.255");

    private TypeEnum serverLogLevel;
    private TypeEnum webPortalLogLevel;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public LoggerSettings(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.LOGGER_SETTINGS.getClassId();
    }

    /**
     * Read the server log level from the device
     *
     * @return the server log level
     * @throws java.io.IOException
     */
    public TypeEnum readServerLogLevel() throws IOException {
        this.serverLogLevel = new TypeEnum(getResponseData(LoggerSettingsAttributes.SERVER_LOG_LEVEL), 0);
        return this.serverLogLevel;
    }

    /**
     * Getter for the server log level
     *
     * @return the server log level
     * @throws java.io.IOException
     */
    public TypeEnum getServerLogLevel() throws IOException {
        if (this.serverLogLevel == null) {
            readServerLogLevel();
        }
        return this.serverLogLevel;
    }

    /**
     * Setter for the server log level
     *
     * @param serverLogLevel the server log level
     * @throws java.io.IOException
     */
    public void writeServerLogLevel(TypeEnum serverLogLevel) throws IOException {
        checkLogLevelRange(serverLogLevel);
        write(LoggerSettingsAttributes.SERVER_LOG_LEVEL, serverLogLevel.getBEREncodedByteArray());
        this.serverLogLevel = serverLogLevel;
    }

    /**
     * Read the web portal log level from the device
     *
     * @return the web portal level
     * @throws java.io.IOException
     */
    public TypeEnum readWebPortalLogLevel() throws IOException {
        this.webPortalLogLevel = new TypeEnum(getResponseData(LoggerSettingsAttributes.WEB_PORTAL_LOG_LEVEL), 0);
        return this.webPortalLogLevel;
    }

    /**
     * Getter for the web portal level
     *
     * @return the web portal level
     * @throws java.io.IOException
     */
    public TypeEnum getWebPortalLogLevel() throws IOException {
        if (this.webPortalLogLevel == null) {
            readWebPortalLogLevel();
        }
        return this.webPortalLogLevel;
    }

    /**
     * Setter for the web portal level
     *
     * @param webPortalLogLevel the web portal log level
     * @throws java.io.IOException
     */
    public void writeWebPortalLogLevel(TypeEnum webPortalLogLevel) throws IOException {
        checkLogLevelRange(webPortalLogLevel);
        write(LoggerSettingsAttributes.WEB_PORTAL_LOG_LEVEL, webPortalLogLevel.getBEREncodedByteArray());
        this.webPortalLogLevel = webPortalLogLevel;
    }

    private void checkLogLevelRange(TypeEnum logLevel) throws IOException {
        if (logLevel.getValue() < 0 || logLevel.getValue() > 0x07) {
            throw new IOException("Invalid log level (" + logLevel.getValue() + "). Log level should be in range in range 0 to 7");
        }
    }

    /**
     * Setter for the remote syslog configuration
     *
     * @param remoteSyslogConfig the new remote syslog configuration
     * @throws java.io.IOException
     */
    public void writeRemoteSyslogConfig(Structure remoteSyslogConfig) throws IOException {
        write(LoggerSettingsAttributes.REMOTE_SYSLOG_CONFIG, remoteSyslogConfig.getBEREncodedByteArray());
    }

    /**
     * @return Beacon logging
     * @throws IOException
     */
    public OctetString fetchLogging() throws IOException {
        byte[] responseData = this.methodInvoke(LoggerSettingsMethods.FETCH_LOGGING, new Integer8(0));
        return AXDRDecoder.decode(responseData, OctetString.class);
    }
}
