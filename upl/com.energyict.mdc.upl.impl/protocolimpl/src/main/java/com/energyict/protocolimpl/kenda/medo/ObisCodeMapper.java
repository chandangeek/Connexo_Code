package com.energyict.protocolimpl.kenda.medo;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class ObisCodeMapper {

	private Calendar calendar=Calendar.getInstance();
	private Medo medo;

	public ObisCodeMapper(Medo medo) {
		this.medo = medo;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws UnsupportedException, NoSuchRegisterException, IOException {
		int[] channelVal;
		RegisterValue reg;
		Quantity q;
		if( obisCode.getA()!=1 ||
				obisCode.getC()!=82 ||
				obisCode.getD()!=128 ||
				obisCode.getE()!=0){
				// check validity of the code
				throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
			}
		channelVal=medo.getMcf().retrieveLastProfileData(medo.getProfileInterval());
		q = new Quantity(new BigDecimal(channelVal[obisCode.getB()-1]), Unit.get(BaseUnit.UNITLESS));
		reg = new RegisterValue(obisCode, q, null, getTime());
		return reg;
	}
	private Date getTime() {
		return calendar.getTime();
	}
}
