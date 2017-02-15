/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.elster.opus;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ObisCodeMapper {

	private Opus opus = null;
	private RegisterValue rv;
	private Quantity q;
	private Calendar calendar;
	private ArrayList dataArray;

	public ObisCodeMapper(Opus opus) {
		this.opus = opus;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws UnsupportedException, NoSuchRegisterException, IOException {
		if( obisCode.getA()!=1 ||
			obisCode.getC()!=0 ||
			obisCode.getD()!=7 ||
			obisCode.getE()!=0 ||
			obisCode.getB()>opus.getNumberOfChannels() ||
			obisCode.getB()<0){
			// check validity of the code
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		}
		switch(obisCode.getF()){
			case 255:
				q = new Quantity(new BigDecimal(subFactory(3,obisCode)), Unit.get(BaseUnit.UNITLESS));
				rv = new RegisterValue(obisCode, q, null, getTime());
				break;
			case -1:
				q = new Quantity(new BigDecimal(subFactory(4,obisCode)), Unit.get(BaseUnit.UNITLESS));
				rv = new RegisterValue(obisCode, q, null, getTime());
				break;
			case 0:
				q = new Quantity(new BigDecimal(subFactory(5,obisCode)), Unit.get(BaseUnit.UNITLESS));
				rv = new RegisterValue(obisCode, q, null, getTime());
				break;
			default:
				throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		}
		return rv;
	}
	private Date getTime() {
		return calendar.getTime();
	}

	private int subFactory(int command,ObisCode obisCode) throws IOException{
		// ALL statemachine 1
		int channel, readVal, x, y;
		calendar = Calendar.getInstance();
		channel = obisCode.getB()-1;
		dataArray = opus.getOcf().command(command, opus.getAttempts(), opus.getTimeOut(), null);
		if(dataArray.size()==1){
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!, data was metastable, try again later");
		}else{
			x=(int) Math.floor(channel/8);
			y=channel%8;
			String[] str=(String[]) dataArray.get(2+2*x);
			readVal = Integer.parseInt(str[y]);
			str=(String[]) dataArray.get(0);
			calendar.set(Integer.parseInt(str[4])+2000,
						 Integer.parseInt(str[3])-1,
						 Integer.parseInt(str[2]));
			if(command>3){ // let time as it is on command 3
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 	   0);	// reset minutes
				calendar.set(Calendar.SECOND, 	   0);	// reset seconds
				calendar.set(Calendar.MILLISECOND, 0);	// reset milliseconds
				long newcal=calendar.getTimeInMillis()+24*3600*1000;
				calendar.setTimeInMillis(newcal); // midnight
			}
		}
		return readVal;
	}


}
