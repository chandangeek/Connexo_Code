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
    
    /** Creates a new instance of FunctionCodeFactory */
    public FunctionCodeFactory(Modbus modbus) {
        this.modbus=modbus;
    }

    public Modbus getModbus() {
        return modbus;
    }

    public AbstractRequest getRequest(int functionCode, int[] vals) throws IOException {
        if (functionCode == FunctionCode.READ_INPUT_REGISTER.getFunctionCode())
            return getReadInputRegistersRequest(vals[0], vals[1]);
        else if (functionCode == FunctionCode.READ_HOLDING_REGISTER.getFunctionCode())
            return getReadHoldingRegistersRequest(vals[0], vals[1]);
        else if (functionCode == FunctionCode.WRITE_SINGLE_COIL.getFunctionCode())
            return getWriteSingleCoil(vals[0], vals[1]);
        else if (functionCode == FunctionCode.WRITE_SINGLE_REGISTER.getFunctionCode())
            return getWriteSingleRegister(vals[0], vals[1]);
        else if (functionCode == FunctionCode.REPORT_SLAVE_ID.getFunctionCode())
            return getReportSlaveId();
        else if (functionCode == FunctionCode.READ_DEVICE_ID.getFunctionCode())
            return getReadDeviceIdentification(vals[0], vals[1]);
        else if (functionCode == FunctionCode.WRITE_MULTIPLE_REGISTER.getFunctionCode()) {
            byte[] data = ParseUtils.convert2ByteArray(vals,2);
            return getWriteMultipleRegisters(vals[0], vals[1], data);
        } else if (functionCode == FunctionCode.WRITE_MULTIPLE_COILS.getFunctionCode()) {
            byte[] data = ParseUtils.convert2ByteArray(vals,2);
            return getWriteMultipleCoils(vals[0], vals[1], data);
        }else if (functionCode == FunctionCode.READ_FILE_RECORD.getFunctionCode()){
            return readFileRecordRequest(vals[0], vals[1], vals[1]);
        }

        else return null;
    }

    public ReadFileRecordRequest readFileRecordRequest(int fileNumber, int recordNumber, int recordLength)throws IOException {
        ReadFileRecordRequest readFileRecordRequest = new ReadFileRecordRequest(this);
        readFileRecordRequest.setFileRecordParameters(fileNumber, recordNumber, recordLength);
        readFileRecordRequest.build();
        return readFileRecordRequest;
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

    public ReadStatuses getReadCoilStatuses(int startingAddress, int quantityOfCoils) throws IOException {
        ReadStatuses readStatuses = new ReadStatuses(this, FunctionCode.READ_COIL_STATUS);
        readStatuses.setRequestSpecifications(startingAddress, quantityOfCoils);
        readStatuses.build();
        return readStatuses;
    }

    public ReadStatuses getReadInputStatuses(int startingAddress, int quantityOfInputs) throws IOException {
        ReadStatuses readStatuses = new ReadStatuses(this, FunctionCode.READ_INPUT_STATUS);
        readStatuses.setRequestSpecifications(startingAddress, quantityOfInputs);
        readStatuses.build();
        return readStatuses;
    }

    public WriteSingleRegister getWriteSingleRegister(int writeRegisterAddress, int writeRegisterValue) throws IOException {
        WriteSingleRegister writeSingleRegister = new WriteSingleRegister(this);
        writeSingleRegister.writeRegister(writeRegisterAddress, writeRegisterValue);
        writeSingleRegister.build();
        return writeSingleRegister;
    }

    public WriteSingleCoil getWriteSingleCoil(int writeCoilAddress, int writeCoilValue) throws IOException {
        WriteSingleCoil writeSingleCoil = new WriteSingleCoil(this);
        writeSingleCoil.writeCoil(writeCoilAddress, writeCoilValue);
        writeSingleCoil.build();
        return writeSingleCoil;
    }

    public WriteMultipleRegisters getWriteMultipleRegisters(int writeStartingAddress, int writeQuantityOfRegisters, byte[] registerValues) throws IOException {
        WriteMultipleRegisters writeMultipleRegisters = new WriteMultipleRegisters(this);
        writeMultipleRegisters.writeRegister(writeStartingAddress, writeQuantityOfRegisters, registerValues);
        writeMultipleRegisters.build();
        return writeMultipleRegisters;
    }

    public WriteMultipleCoils getWriteMultipleCoils(int writeStartingAddress, int writeQuantityOfCoils, byte[] registerValues) throws IOException {
        WriteMultipleCoils writeMultipleCoils = new WriteMultipleCoils(this);
        writeMultipleCoils.writeCoil(writeStartingAddress, writeQuantityOfCoils, registerValues);
        writeMultipleCoils.build();
        return writeMultipleCoils;
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