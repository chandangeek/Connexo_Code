
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

import com.energyict.obis.*;
import com.energyict.protocol.*;
import java.util.*;

/**
 *
 * @author kvds
 */
abstract public class AbstractRegisterFactory {
    
    abstract protected int getTimeIndex();
    
    private MBus mBus;
    private List registerValues;
    List dataRecords;
    
    /** Creates a new instance of AbstractRegisterFactory */
    public AbstractRegisterFactory(MBus mBus) {
        this.setMBus(mBus);
        
    }
    
    public RegisterValue findRegisterValue(ObisCode obisCode) throws NoSuchRegisterException {
        Iterator it = getRegisterValues().iterator();
        while(it.hasNext()) {
            RegisterValue registerValue = (RegisterValue)it.next();
            if (registerValue.getObisCode().equals(obisCode))
                return registerValue;
        }        
        throw new NoSuchRegisterException("Register "+obisCode+" is not supported!");
    }
    
    public void init(List dataRecords) {
        this.dataRecords=dataRecords;
        setRegisterValues(new ArrayList());
        Iterator it = dataRecords.iterator();
        int code=0;
        while(it.hasNext()) {
            DataRecord dataRecord = (DataRecord)it.next();
            StringBuffer strBuff = new StringBuffer();
            if (dataRecord.getDataRecordHeader().getValueInformationBlock() != null) {
                strBuff.append(dataRecord.getDataRecordHeader().getValueInformationBlock().getValueInformationfieldCoding().getDescription());
                if (dataRecord.getDataRecordHeader().getValueInformationBlock().getValueInformationfieldCodings()!=null) {
                    for (int i=0;i<dataRecord.getDataRecordHeader().getValueInformationBlock().getValueInformationfieldCodings().size();i++) {
                        strBuff.append(", "+((ValueInformationfieldCoding)dataRecord.getDataRecordHeader().getValueInformationBlock().getValueInformationfieldCodings().get(i)).getDescription());
                    }
                }
            
                if (dataRecord.getDataRecordHeader().getDataInformationBlock().getStorageNumber() != 0)
                    strBuff.append(", historical value "+dataRecord.getDataRecordHeader().getDataInformationBlock().getStorageNumber());
                if (dataRecord.getDataRecordHeader().getDataInformationBlock().getTariffNumber() != 0)
                    strBuff.append(", tariff "+dataRecord.getDataRecordHeader().getDataInformationBlock().getTariffNumber());


                RegisterValue registerValue = new RegisterValue(ObisCode.fromString("0.0.96.99."+(code/256)+"."+(code%256)),dataRecord.getQuantity(),dataRecord.getDate(),null,null,new Date(),-1,strBuff.toString());
                getRegisterValues().add(registerValue);
                code++;
            }
            
        }
    }    

    public DataRecord getDataRecord(int index) {
       return (DataRecord)dataRecords.get(index);   
    }
    
    public MBus getMBus() {
        return mBus;
    }

    private void setMBus(MBus mBus) {
        this.mBus = mBus;
    }

    public List getRegisterValues() {
        return registerValues;
    }

    private void setRegisterValues(List registerValues) {
        this.registerValues = registerValues;
    }
    
    public Date getTime() {
        return getDataRecord(getTimeIndex()).getDate();
        
    }
    
}
