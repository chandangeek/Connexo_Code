package com.energyict.protocolimpl.kenda.medo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;

public class ObisCodeMapper {
	
	private Medo medo;
	
	public ObisCodeMapper(Medo medo) {
		this.medo = medo;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws UnsupportedException, NoSuchRegisterException, IOException {
		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}
}
