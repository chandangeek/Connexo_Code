/*
 * FunctionCodeFactory.java
 *
 * Created on 20 september 2005, 9:08
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class FunctionCodeFactory {

    private Modbus modbus;

    public static final int FUNCTIONCODE_READINPUTREGISTER=4;
    public static final int FUNCTIONCODE_READHOLDINGREGISTER=3;
    public static final int FUNCTIONCODE_WRITESINGLEREGISTER=6;
    public static final int FUNCTIONCODE_REPORTSLAVEID=17;
    public static final int FUNCTIONCODE_READDEVICEID=43;
    public static final int FUNCTIONCODE_WRITEMULTIPLEREGISTER=16;

    /** Creates a new instance of FunctionCodeFactory */
    public FunctionCodeFactory(Modbus modbus) {
        this.modbus=modbus;
    }

    public Modbus getModbus() {
        return modbus;
    }

    public AbstractRequest getRequest(int functionCode, int[] vals) throws IOException {
        if (functionCode == FUNCTIONCODE_READINPUTREGISTER)
            return getReadInputRegistersRequest(vals[0], vals[1]);
        else if (functionCode == FUNCTIONCODE_READHOLDINGREGISTER)
            return getReadHoldingRegistersRequest(vals[0], vals[1]);
        else if (functionCode == FUNCTIONCODE_WRITESINGLEREGISTER)
            return getWriteSingleRegister(vals[0], vals[1]);
        else if (functionCode == FUNCTIONCODE_REPORTSLAVEID)
            return getReportSlaveId();
        else if (functionCode == FUNCTIONCODE_READDEVICEID)
            return getReadDeviceIdentification(vals[0], vals[1]);
        else if (functionCode == FUNCTIONCODE_WRITEMULTIPLEREGISTER) {
            byte[] data = ParseUtils.convert2ByteArray(vals,2);
            return getWriteMultipleRegisters(vals[0], vals[1], data);
        }
        else return null;
    }

    public ReadHoldingRegistersRequest getReadHoldingRegistersRequest(int startingAddress, int quantityOfRegisters) throws IOException {
        ReadHoldingRegistersRequest readHoldingRegistersRequest = new ReadHoldingRegistersRequest(this);
        readHoldingRegistersRequest.setRegisterSpec(startingAddress, quantityOfRegisters);
        readHoldingRegistersRequest.build();
        return readHoldingRegistersRequest;
    }

    public ReadInputRegistersRequest getReadInputRegistersRequest(int startingAddress, int quantityOfRegisters) throws IOException {
        ReadInputRegistersRequest readInputRegistersRequest = new ReadInputRegistersRequest(this);
        readInputRegistersRequest.setRegisterSpec(startingAddress, quantityOfRegisters);
        readInputRegistersRequest.build();
        return readInputRegistersRequest;
    }

    public WriteSingleRegister getWriteSingleRegister(int writeRegisterAddress, int writeRegisterValue) throws IOException {
        WriteSingleRegister writeSingleRegister = new WriteSingleRegister(this);
        writeSingleRegister.writeRegister(writeRegisterAddress, writeRegisterValue);
        writeSingleRegister.build();
        return writeSingleRegister;
    }

    public WriteMultipleRegisters getWriteMultipleRegisters(int writeStartingAddress, int writeQuantityOfRegisters, byte[] registerValues) throws IOException {
        WriteMultipleRegisters writeMultipleRegisters = new WriteMultipleRegisters(this);
        writeMultipleRegisters.writeRegister(writeStartingAddress, writeQuantityOfRegisters, registerValues);
        writeMultipleRegisters.build();
        return writeMultipleRegisters;
    }

    public ReportSlaveId getReportSlaveId() throws IOException {
        ReportSlaveId rsi = new ReportSlaveId(this);
        rsi.build();
        return rsi;
    }

    public MandatoryDeviceIdentification getMandatoryReadDeviceIdentification() throws IOException {
        return new MandatoryDeviceIdentification(getReadDeviceIdentification(1,0).getDeviceObjects());
    }
    public ReadDeviceIdentification getReadDeviceIdentification(int readDeviceIdCode, int objectID) throws IOException {
        ReadDeviceIdentification readDeviceIdentification = new ReadDeviceIdentification(this);
        readDeviceIdentification.setDeviceIdSpec(readDeviceIdCode, objectID);
        readDeviceIdentification.build();
        return readDeviceIdentification;
    }


}
