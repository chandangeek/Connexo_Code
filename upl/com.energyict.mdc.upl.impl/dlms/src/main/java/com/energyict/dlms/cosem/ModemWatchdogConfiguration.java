package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributes.ModemWatchdogConfigurationAttributes;
import com.energyict.dlms.cosem.methods.ModemWatchdogConfigurationMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class ModemWatchdogConfiguration extends AbstractCosemObject {

    private Structure wdConfiguration = null;
    private BooleanObject isModemWatchdogEnabled = null;

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.162.96.128.0.255");
    public static final ObisCode OBIS_CODE_LEGACY = ObisCode.fromString("0.0.128.0.11.255");

    public static final int WD_CONFIG = 2;
    public static final int IS_MODEM_WATCHDOG_ENABLED = 3;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public ModemWatchdogConfiguration(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }


    public static ObisCode getLegacyObisCode() {
        return OBIS_CODE_LEGACY;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.MODEM_WATCHDOG_SETUP.getClassId();
    }

    public void enableWatchdog(boolean enable) throws IOException {
        methodInvoke(ModemWatchdogConfigurationMethods.ENABLE, new BooleanObject(enable).getBEREncodedByteArray());
    }

    public void writeConfigParameters(int interval, int pppResetThreshold, int modemResetThreshold, int deviceRebootThreshold) throws IOException {
        Structure structure = new Structure();
        structure.addDataType(new Unsigned16(interval));
        structure.addDataType(new Unsigned16(pppResetThreshold));
        structure.addDataType(new Unsigned16(modemResetThreshold));
        structure.addDataType(new Unsigned16(deviceRebootThreshold));
        write(ModemWatchdogConfigurationAttributes.CONFIG_PARAMETERS, structure.getBEREncodedByteArray());
    }

    /**
     * Read WD_ENABLED attribute from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public Structure readWDConfigAttribute() throws IOException {
        this.wdConfiguration = new Structure(getResponseData(WD_CONFIG), 0, 0);
        return this.wdConfiguration;
    }

    /**
     * Read IS_MODEM_WATCHDOG_ENABLED attribute from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public BooleanObject readModemWatchdogEnabledAttribute() throws IOException {
        this.isModemWatchdogEnabled = new BooleanObject(getResponseData(IS_MODEM_WATCHDOG_ENABLED), 0);
        return this.isModemWatchdogEnabled;
    }

    /**
     * Beacon3100 added an extra entry (initialDelay) in the structure
     */
    public void writeExtendedConfigParameters(int interval, int initialDelay, int pppResetThreshold, int modemResetThreshold, int deviceRebootThreshold) throws IOException {
        Structure structure = new Structure();
        structure.addDataType(new Unsigned16(interval));
        structure.addDataType(new Unsigned16(initialDelay));
        structure.addDataType(new Unsigned16(pppResetThreshold));
        structure.addDataType(new Unsigned16(modemResetThreshold));
        structure.addDataType(new Unsigned16(deviceRebootThreshold));
        write(ModemWatchdogConfigurationAttributes.CONFIG_PARAMETERS, structure.getBEREncodedByteArray());
    }

}