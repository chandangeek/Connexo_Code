package com.energyict.protocolimpl.coronis.wavetalk;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.coronis.wavetalk.core.CommonObisCodeMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ObisCodeMapper {

	private static final Map<ObisCode,String> REGISTER_MAPS = new HashMap<>();

	static {

		// get the common obis codes from the common obis code mapper

		// specific wavetalk registers
//		registerMaps.put(ObisCode.fromString("1.1.82.8.0.255"), "Input A index");
	}

	private final WaveTalk waveTalk;

    public ObisCodeMapper(final WaveTalk waveTalk) {
        this.waveTalk=waveTalk;
    }

    final String getRegisterExtendedLogging() {
    	StringBuilder strBuilder=new StringBuilder();
	    for (Entry<ObisCode, String> o : REGISTER_MAPS.entrySet()) {
		    waveTalk.getLogger().info(o.getKey().toString() + ", " + o.getValue());
	    }
    	strBuilder.append(waveTalk.getCommonObisCodeMapper().getRegisterExtendedLogging());
    	return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	String info = REGISTER_MAPS.get(obisCode);
    	if (info !=null) {
    		return new RegisterInfo(info);
    	}
    	else {
    		return CommonObisCodeMapper.getRegisterInfo(obisCode);
    	}
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return waveTalk.getCommonObisCodeMapper().getRegisterValue(obisCode);
    }

}