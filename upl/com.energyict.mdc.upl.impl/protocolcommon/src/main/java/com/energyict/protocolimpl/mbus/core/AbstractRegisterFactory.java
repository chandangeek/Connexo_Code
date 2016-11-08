/*
 * AbstractRegisterFactory.java
 *
 * Created on 8 oktober 2007, 13:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author kvds
 */
public abstract class AbstractRegisterFactory {

    protected abstract int getTimeIndex();

    private MBus mBus;
    private List<RegisterValue> registerValues;
    private List dataRecords;

    public AbstractRegisterFactory(MBus mBus) {
        this.setMBus(mBus);
    }

    public RegisterValue findRegisterValue(ObisCode obisCode) throws NoSuchRegisterException {
        for (RegisterValue registerValue : getRegisterValues()) {
            if (registerValue.getObisCode().equals(obisCode)) {
                return registerValue;
            }
        }
        throw new NoSuchRegisterException("Register "+obisCode+" is not supported!");
    }

    public void init(CIField72h cIField72h) { //List dataRecords) {
        this.dataRecords=cIField72h.getDataRecords();
        setRegisterValues(new ArrayList<>());
        Iterator it = dataRecords.iterator();
        int code= getMBus().isUseZeroBased() ? 0 : 1;
        while(it.hasNext()) {
            DataRecord dataRecord = (DataRecord)it.next();
            StringBuilder builder = new StringBuilder();
            ValueInformationBlock block = dataRecord.getDataRecordHeader().getValueInformationBlock();
            if (block != null) {
                ValueInformationfieldCoding vifCoding = block.getValueInformationfieldCoding();
                builder.append(vifCoding != null ? vifCoding.getDescription() : block.toString());
                if (block.getValueInformationfieldCodings()!=null) {
                    for (int i=0;i< block.getValueInformationfieldCodings().size();i++) {
                        builder.append(", ").append(((ValueInformationfieldCoding) block.getValueInformationfieldCodings().get(i)).getDescription());
                    }
                }

                if (dataRecord.getDataRecordHeader().getDataInformationBlock().getStorageNumber() != 0) {
                    builder.append(", historical value ").append(dataRecord.getDataRecordHeader().getDataInformationBlock().getStorageNumber());
                }
                if (dataRecord.getDataRecordHeader().getDataInformationBlock().getTariffNumber() != 0) {
                    builder.append(", tariff ").append(dataRecord.getDataRecordHeader().getDataInformationBlock().getTariffNumber());
                }

                String registerText = ((dataRecord.getText() != null) || (dataRecord.getText().length() <= 0)) ? dataRecord.getText() : builder.toString();
                RegisterValue registerValue = new RegisterValue(ObisCode.fromString("0.0.96.99."+(code/256)+"."+(code%256)),dataRecord.getQuantity(),dataRecord.getDate(),null,null,new Date(),-1, registerText);
                getRegisterValues().add(registerValue);
                code++;
            }
        }

        addHeaderRegisterValues(cIField72h);
    }

    private void addHeaderRegisterValues(CIField72h cIField72h) {
        getRegisterValues().add(new RegisterValue(ObisCode.fromString("0.0.96.99.255.248"),null,null,null,null,new Date(),-1,"MBUS header meter ID "+cIField72h.getMeter3LetterId()+" ("+Integer.toHexString(cIField72h.getManufacturerIdentification())+")"));
        getRegisterValues().add(new RegisterValue(ObisCode.fromString("0.0.96.99.255.249"),new Quantity(new BigDecimal(cIField72h.getManufacturerIdentification()),Unit.get("")),null,null,null,new Date(),-1,"MBUS header manufacturer identification"+" ("+Integer.toHexString(cIField72h.getManufacturerIdentification())+")"));
        getRegisterValues().add(new RegisterValue(ObisCode.fromString("0.0.96.99.255.250"),new Quantity(new BigDecimal(cIField72h.getVersion()),Unit.get("")),null,null,null,new Date(),-1,"MBUS header version"+" ("+Integer.toHexString(cIField72h.getVersion())+")"));
        getRegisterValues().add(new RegisterValue(ObisCode.fromString("0.0.96.99.255.251"),new Quantity(new BigDecimal(cIField72h.getDeviceType().getId()),Unit.get("")),null,null,null,new Date(),-1,"MBUS header devicetype "+cIField72h.getDeviceType().toString()+" ("+Integer.toHexString(cIField72h.getDeviceType().getId())+")"));
        getRegisterValues().add(new RegisterValue(ObisCode.fromString("0.0.96.99.255.252"),new Quantity(new BigDecimal(cIField72h.getAccessNumber()),Unit.get("")),null,null,null,new Date(),-1,"MBUS header access number"));
        getRegisterValues().add(new RegisterValue(ObisCode.fromString("0.0.96.99.255.253"),new Quantity(new BigDecimal(cIField72h.getIdentificationNumber()),Unit.get("")),null,null,null,new Date(),-1,"MBUS header identification number"));
        getRegisterValues().add(new RegisterValue(ObisCode.fromString("0.0.96.99.255.254"),new Quantity(new BigDecimal(cIField72h.getStatusByte()),Unit.get("")),null,null,null,new Date(),-1,"MBUS header status byte"));
        getRegisterValues().add(new RegisterValue(ObisCode.fromString("0.0.96.99.255.255"),new Quantity(new BigDecimal(cIField72h.getSignatureField()),Unit.get("")),null,null,null,new Date(),-1,"MBUS header signature field"));
    }

    private DataRecord getDataRecord(int index) {
       return (DataRecord)dataRecords.get(index);
    }

    public MBus getMBus() {
        return mBus;
    }

    private void setMBus(MBus mBus) {
        this.mBus = mBus;
    }

    public List<RegisterValue> getRegisterValues() {
        return registerValues;
    }

    private void setRegisterValues(List<RegisterValue> registerValues) {
        this.registerValues = registerValues;
    }

    public Date getTime() {
        return getDataRecord(getTimeIndex()).getDate();
    }

}
