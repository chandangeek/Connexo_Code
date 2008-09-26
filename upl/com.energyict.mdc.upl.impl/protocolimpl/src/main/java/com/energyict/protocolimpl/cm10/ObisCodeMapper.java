package com.energyict.protocolimpl.cm10;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;


public class ObisCodeMapper {
	
	private CM10 cm10Protocol;
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(CM10 cm10Protocol) {
        this.cm10Protocol=cm10Protocol;
    }
    
    public String getRegisterInfo() throws IOException {
        return cm10Protocol.getRegisterFactory().getRegisterInfo();
    }
    
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	if( obisCode.getA()!=1 || 
				obisCode.getC()!=82 ||
				obisCode.getD()!=128 ||
				obisCode.getE()!=0 ||
				obisCode.getB()>cm10Protocol.getNumberOfChannels() ||
				obisCode.getB()<= 0)
				throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    	
    	
    	short[] channelVal;
		RegisterValue reg;
		CurrentDialReadingsTable currentDialReadingsTable = cm10Protocol.getCurrentDialReadingsTable();
		long[] currentDialreadings = currentDialReadingsTable.getValues();
		Date toTime = currentDialReadingsTable.getToTime();
		Quantity quantity = new Quantity(new BigDecimal(currentDialreadings[obisCode.getB() - 1]), Unit.get(BaseUnit.UNITLESS));
		reg = new RegisterValue(obisCode, quantity, null, toTime);				
		return reg;
    }
       

}

