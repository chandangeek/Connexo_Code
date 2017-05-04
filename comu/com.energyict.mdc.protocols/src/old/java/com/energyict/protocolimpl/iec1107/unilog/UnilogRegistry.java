package com.energyict.protocolimpl.iec1107.unilog;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegisterDataParse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fbo
 */
public class UnilogRegistry extends AbstractVDEWRegistry {

    private static final UnilogRegister[] UNILOG_REGISTERS = new UnilogRegister[]{
            new UnilogRegister("Battery capacity used by connected UNILOG GPRS modem", "0.1.96.6.1.255", "C.90.21"),
            new UnilogRegister("Number of hours connected with a GSM network", "0.0.96.50.0.4", "C.90.4"),
            new UnilogRegister("Number of hours GSM communication ", "0.0.96.50.0.5", "C.90.5"),
            new UnilogRegister("Battery voltage UNILOG", "0.1.96.6.3.255", "C.90.6"),
            new UnilogRegister("Signal strenght GSM main cell", "0.0.96.50.0.6", "C.90.7", null, VDEWRegister.VDEW_STRING),
            new UnilogRegister("Metrological software revision number", "7.0.0.2.0.255", "7-0:0.2.0", null, VDEWRegister.VDEW_STRING),
            new UnilogRegister("Input divider CH_1", "7.1.0.7.2.255", "1:0.7.2"),
            new UnilogRegister("Input divider CH_2", "7.2.0.7.2.255", "2:0.7.2"),
            new UnilogRegister("Register input CH_1", "7.1.23.0.0.255", "1:23.0.0"),
            new UnilogRegister("Register input CH_2", "7.2.23.0.0.255", "2:23.0.0"),
            new UnilogRegister("Actual flow average CH_1", "7.1.43.0.0.255", "1:43.0.0"),
            new UnilogRegister("Actual flow average CH_2", "7.2.43.0.0.255", "2:43.0.0"),
            new UnilogRegister("Current month max flow average CH_1", "7.1.43.6.0.255", "1:43.6.0"),
            new UnilogRegister("Current month max flow average CH_2", "7.2.43.6.0.255", "2:43.6.0")
    };

    public final String R_DEVICE_ADDRESS = "0.01.1";
    public final String R_METROLOGICAL_REVISION_NUMBER = "0.2.0";
    public final String R_MEASUREMENT_PERIOD_FLOW = "0.8.0";
    public final String R_REGISTRATION_PERIOD_LOAD_PROFILE = "0.8.5";
    public final String R_TIME = "0.9.1";
    public final String R_DATE = "0.9.2";
    public final String R_TIME_DATE = "0.9.1 0.9.2";

    /**
     * @param meterExceptionInfo
     * @param protocolLink
     */
    public UnilogRegistry(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink) {
        super(meterExceptionInfo, protocolLink);
    }

    /**
     * Initialize the registers
     */
    protected void initRegisters() {

        for (int i = 0; i < UNILOG_REGISTERS.length; i++) {
            UnilogRegister unilogRegister = UNILOG_REGISTERS[i];
            add(unilogRegister);
        }

        registers.put(R_DEVICE_ADDRESS, new VDEWRegister(R_DEVICE_ADDRESS, VDEWRegister.VDEW_STRING, 0, -1, null, VDEWRegister.NOT_WRITEABLE, VDEWRegister.CACHED));
        registers.put(R_METROLOGICAL_REVISION_NUMBER, new VDEWRegister(R_METROLOGICAL_REVISION_NUMBER, VDEWRegister.VDEW_STRING, 0, -1, null, VDEWRegister.NOT_WRITEABLE, VDEWRegister.CACHED));
        registers.put(R_MEASUREMENT_PERIOD_FLOW, new VDEWRegister(R_MEASUREMENT_PERIOD_FLOW, VDEWRegister.VDEW_INTEGER, 0, -1, null, VDEWRegister.NOT_WRITEABLE, VDEWRegister.CACHED));
        registers.put(R_REGISTRATION_PERIOD_LOAD_PROFILE, new VDEWRegister(R_REGISTRATION_PERIOD_LOAD_PROFILE, VDEWRegister.VDEW_INTEGER, 0, -1, null, VDEWRegister.NOT_WRITEABLE, VDEWRegister.CACHED));
        registers.put(R_TIME, new VDEWRegister(R_TIME, VDEWRegister.VDEW_TIMESTRING, 0, -1, null, VDEWRegister.WRITEABLE, VDEWRegister.NOT_CACHED));
        registers.put(R_DATE, new VDEWRegister(R_DATE, VDEWRegister.VDEW_DATESTRING, 0, -1, null, VDEWRegister.WRITEABLE, VDEWRegister.NOT_CACHED));
        registers.put(R_TIME_DATE, new VDEWRegister(R_TIME_DATE, VDEWRegisterDataParse.VDEW_TIMEDATE, 0, -1, null, VDEWRegister.NOT_WRITEABLE, VDEWRegister.NOT_CACHED));
    }

    private void add(UnilogRegister register) {
        registers.put(register.getRegisterId(), register.getVdewRegister());
    }

    private UnilogRegister findUnilogRegisterByObisCode(ObisCode obisCode) {
        for (int i = 0; i < UNILOG_REGISTERS.length; i++) {
            UnilogRegister unilogRegister = UNILOG_REGISTERS[i];
            if ((unilogRegister.getObis() != null) && (unilogRegister.getObis().equals(obisCode))) {
                return unilogRegister;
            }
        }
        return null;
    }

    public Object getRegister(ObisCode obisCode) throws IOException {
        UnilogRegister reg = findUnilogRegisterByObisCode(obisCode);
        if (reg != null) {
            return getRegister(reg.getRegisterId());
        } else {
            throw new IOException("Register with obis [" + obisCode + "] not supported.");
        }
    }

    /**
     * Get a list of the suppored obiscodes
     *
     * @return
     */
    public static List<ObisCode> getSupportedObisCodes() {
        List<ObisCode> obisCodes = new ArrayList<ObisCode>();
        for (int i = 0; i < UNILOG_REGISTERS.length; i++) {
            ObisCode obis = UNILOG_REGISTERS[i].getObis();
            if (obis != null) {
                obisCodes.add(obis);
            }
        }
        return obisCodes;
    }

    /**
     * Getter for the uniflo registers.
     * @return
     */
    public static UnilogRegister[] getUnilogRegisters() {
        return UNILOG_REGISTERS;
    }
}