package com.energyict.genericprotocolimpl.rtuplusserver.g3;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 22/02/12
 * Time: 15:20
 */
public class RtuPlusServerInfo extends AbstractDlmsSessionTask {

    public static final ObisCode FW_VERSION_OBIS = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode SERIAL_NUMBER_OBIS = ObisCode.fromString("0.0.96.1.0.255");

    public RtuPlusServerInfo(DlmsSession session, RtuPlusServerTask task) {
        super(session, task);
    }

    public final String getFirmwareVersion() {
        try {
            Data data = getCosemObjectFactory().getData(FW_VERSION_OBIS);
            return data.getString();
        } catch (IOException e) {
            getLogger().severe("Unable to read Rtu+Server G3 firmware version! [" + e.getMessage() + "]");
            return "";
        }
    }

    public final String getSerialNumber() throws IOException {
        try {
            Data data = getCosemObjectFactory().getData(SERIAL_NUMBER_OBIS);
            return data.getString();
        } catch (IOException e) {
            throw new NestedIOException(e, "Unable to read Rtu+Server G3 serial number!");
        }
    }

    public final void validateSerialNumber() throws IOException {
        final String eisSerial = getGatewaySerialNumber();
        if (eisSerial.length() > 0) {
            final String meterSerialNumber = getSerialNumber();
            if (!eisSerial.trim().equalsIgnoreCase(meterSerialNumber)) {
                String message = "Configured serial number [" + eisSerial + "] does not match the meter serial number [" + meterSerialNumber + "]!";
                getLogger().severe(message);
                throw new IOException(message);
            } else {
                getLogger().info("Successfully connected to rtu [" + getGateway().getFullName() + "]");
            }
        } else {
            getLogger().warning("Serial number in EIServer is empty for rtu [" + getGateway().getFullName() + "]. Skipping validation.");
        }
    }

}
