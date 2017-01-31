/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;


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

