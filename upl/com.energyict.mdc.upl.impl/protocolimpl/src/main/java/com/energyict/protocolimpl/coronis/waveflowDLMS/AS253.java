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
	byte[] getRequest() {
		return new byte[]{(byte)0x32,(byte)0x05,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x60,(byte)0x01,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x03,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x03}; 
	}
	
}
