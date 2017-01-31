/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectFactory;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sRegister;

import java.io.IOException;

public class SiemensS4sObisCodeMapper {

	private S4sObjectFactory s4sObjectFactory;

	public SiemensS4sObisCodeMapper(S4sObjectFactory objectFactory) {
		this.s4sObjectFactory = objectFactory;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws FlagIEC1107ConnectionException, ConnectionException, IOException {
		RegisterValue registerValue = null;

		if( (obisCode.getA() == 1) && (obisCode.getC() == 1) && (obisCode.getD() == 8)){	// Active Power
			if((obisCode.getB() >= 0) && (obisCode.getB() < 4)){
				S4sRegister register;
				switch(obisCode.getE()){
				case 1: register = s4sObjectFactory.getRegister(SiemensS4sRegisterMapper.TOTAL_REGISTER_A);break;
				case 2: register = s4sObjectFactory.getRegister(SiemensS4sRegisterMapper.TOTAL_REGISTER_B);break;
				case 3: register = s4sObjectFactory.getRegister(SiemensS4sRegisterMapper.TOTAL_REGISTER_C);break;
				case 4: register = s4sObjectFactory.getRegister(SiemensS4sRegisterMapper.TOTAL_REGISTER_D);break;
				default: throw new UnsupportedException("Obiscode " + obisCode + " is not supported.");
				}

				RegisterValue rv = new RegisterValue(obisCode, register.getRegisterQuantity());
				return rv;
			}
		}

		throw new UnsupportedException("Obiscode " + obisCode + " is not supported.");
	}

}
