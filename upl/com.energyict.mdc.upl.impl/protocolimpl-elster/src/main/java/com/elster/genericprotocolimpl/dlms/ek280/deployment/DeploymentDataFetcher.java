package com.elster.genericprotocolimpl.dlms.ek280.deployment;

import com.elster.genericprotocolimpl.dlms.ek280.EK280;
import com.elster.genericprotocolimpl.dlms.ek280.EK280Properties;
import com.elster.protocolimpl.dlms.Dlms;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 16/12/11
 * Time: 9:40
 */
public class DeploymentDataFetcher {

    private static final ObisCode INSTALLATION_DATE_OBIS = ObisCode.fromString("0.0.96.52.0.255");
    private static final ObisCode PHONE_NUMBER_OBIS = ObisCode.fromString("0.3.96.12.6.255");
    private static final ObisCode PDR_OBIS = ObisCode.fromString("0.0.96.1.10.255");
    private static final ObisCode METER_SERIAL_OBIS = ObisCode.fromString("7.0.0.2.14.255");

    private final EK280Properties properties;
    private final Logger logger;
    private final Dlms dlmsProtocol;

    public DeploymentDataFetcher(EK280 ek280) {
        this.properties = ek280.getProperties();
        this.logger = ek280.getLogger();
        this.dlmsProtocol = ek280.getDlmsProtocol();
    }

    public EK280Properties getProperties() {
        return properties;
    }

    public Logger getLogger() {
        return logger;
    }

    public Dlms getDlmsProtocol() {
        return dlmsProtocol;
    }

    /**
     * Read the installation date from the EK280
     *
     * @return the installation date or null if not found
     */
    public Date readInstallationDate() {
        if (getProperties().isExtractInstallationDate()) {
            try {
                RegisterValue registerValue = getDlmsProtocol().readRegister(INSTALLATION_DATE_OBIS);
                String textValue = registerValue == null ? null : registerValue.getText();
                if ((textValue == null) || (textValue.length() == 0)) {
                    throw new IOException("Unable to get installation date from empty register text value!");
                }

                // TODO: Add the actual implementation of the installation date

            } catch (IOException e) {
                getLogger().severe("Discovery session: Unable to read installation date [" + INSTALLATION_DATE_OBIS + "]. Using ChannelBackLog property to calculate installation date.");
            }
        } else {
            getLogger().severe("Discovery session: Extract installation date from [" + INSTALLATION_DATE_OBIS + "] disabled. Using ChannelBackLog property to calculate installation date.");
        }
        return null;
    }

    /**
     * Read the device phone number from the EK280
     *
     * @return the device phone number from the EK280.
     */
    public String readPhoneNumber() {
        try {
            RegisterValue registerValue = getDlmsProtocol().readRegister(PHONE_NUMBER_OBIS);
            String textValue = registerValue == null ? null : registerValue.getText();
            if ((textValue == null) || (textValue.length() == 0)) {
                throw new IOException("Phone number is empty.");
            }
            return textValue; // TODO: add validation for device phone number
        } catch (IOException e) {
            getLogger().severe("Discovery session: Unable to read device phone number [" + PHONE_NUMBER_OBIS + "]. " + e.getMessage());
        }
        return "";
    }

    public String readPdr() {
        try {
            RegisterValue registerValue = getDlmsProtocol().readRegister(PDR_OBIS);
            String textValue = registerValue == null ? null : registerValue.getText();
            if ((textValue == null) || (textValue.length() == 0)) {
                throw new IOException("PDR number is empty.");
            }
            return textValue; // TODO: add validation for pdr number
        } catch (IOException e) {
            getLogger().severe("Discovery session: Unable to read PDR number [" + PDR_OBIS + "]. " + e.getMessage());
        }
        return "";
    }

    public String readMeterSerial() {
        try {
            RegisterValue registerValue = getDlmsProtocol().readRegister(METER_SERIAL_OBIS);
            String textValue = registerValue == null ? null : registerValue.getText();
            if ((textValue == null) || (textValue.length() == 0)) {
                throw new IOException("Meter serial number is empty.");
            }
            return textValue; // TODO: add validation for meter serial number
        } catch (IOException e) {
            getLogger().severe("Discovery session: Unable to read meter serial number [" + METER_SERIAL_OBIS + "]. " + e.getMessage());
        }
        return "";
    }

}
