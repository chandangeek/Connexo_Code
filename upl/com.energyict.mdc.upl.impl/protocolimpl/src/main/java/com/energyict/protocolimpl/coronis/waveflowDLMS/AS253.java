package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;
import java.util.*;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.*;

public class AS253 extends AbstractDLMS {

	@Override
	void doTheValidateProperties(Properties properties) {
		setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "1.0.99.1.0.255")));		
	}	
	
}
