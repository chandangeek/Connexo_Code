/**
 *
 */
package com.energyict.protocolimpl.dlms.as220.plc;

import java.io.IOException;
import java.util.Date;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author jme
 *
 */
public class PLC {

	private static final ObisCode PLC_STATISTICS_OBISCODE = ObisCode.fromString("0.0.53.0.0.255");

	private final AS220 as220;

	/**
	 * @param as220
	 */
	public PLC(AS220 as220) {
		this.as220 = as220;
	}

	/**
	 * @return
	 */
	public AS220 getAs220() {
		return as220;
	}

	public ProfileData getStatistics(Date from, Date to) throws IOException {
		ProfileGeneric pg = getAs220().getCosemObjectFactory().getProfileGeneric(PLC_STATISTICS_OBISCODE);

		byte[] profile = pg.getBufferData();
		System.out.println(ProtocolTools.getHexStringFromBytes(profile));
		PLCStatistics plcStatictics = new PLCStatistics(profile, getAs220().getTimeZone());
		System.out.println(plcStatictics);

		return null;
	}


}
