package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegisterDataParse;

/**
 * @author sva
 * @since 6/11/2014 - 9:05
 */
public class NXT4Registry extends AbstractVDEWRegistry {

    public static final String TIME_DATE = "TimeDate";
    public static final String SERIAL = "Serial";
    public static final String FIRMWARE_VERSION = "FirmwareVersion";
    public static final String OPERATION_LOGBOOK = "OperationLogBook";
    public static final String SHORT_TIME_DATE = "ShortTimeDate";
    public static final String DEMAND_RESET_REGISTER = "DemandReset";

    private final NXT4 protocol;

    public NXT4Registry(NXT4 protocol) {
        super(protocol, protocol);
        this.protocol = protocol;
    }

    @Override
    protected void initRegisters() {
        this.registers.put(SERIAL, new VDEWRegister(EdisFormatter.formatObisAsEdis("1.0.0.0.0.255"), VDEWRegisterDataParse.VDEW_STRING, 0, -1, null, VDEWRegister.NOT_WRITEABLE, VDEWRegister.NOT_CACHED));
        this.registers.put(FIRMWARE_VERSION, new VDEWRegister(EdisFormatter.formatObisAsEdis("1.0.0.2.0.255"), VDEWRegisterDataParse.VDEW_STRING, 0, -1, null, VDEWRegister.NOT_WRITEABLE, VDEWRegister.NOT_CACHED));
        this.registers.put(TIME_DATE, new VDEWRegister(EdisFormatter.formatObisAsEdis("1.0.0.9.129.255"), VDEWRegisterDataParse.VDEW_STRING, 0, -1, null, VDEWRegister.WRITEABLE, VDEWRegister.NOT_CACHED));
        this.registers.put(SHORT_TIME_DATE, new VDEWRegister("0.9.1 0.9.2",VDEWRegisterDataParse.VDEW_TIMEDATE,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        this.registers.put(OPERATION_LOGBOOK, new VDEWRegister("98", VDEWRegisterDataParse.VDEW_STRING, 0, -1, null, VDEWRegister.NOT_WRITEABLE, VDEWRegister.NOT_CACHED));
        this.registers.put(DEMAND_RESET_REGISTER, new VDEWRegister("0001", VDEWRegisterDataParse.VDEW_STRING, 0, -1, null, VDEWRegister.WRITEABLE, VDEWRegister.NOT_CACHED, null, FlagIEC1107Connection.EXECUTE_COMMAND));
    }

    public NXT4 getProtocol() {
        return protocol;
    }
}
