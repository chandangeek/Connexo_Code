/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.dlms.as220.plc;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.plc.statistics.PLCStatistics;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author jme
 *
 */
public class PLC {

	private static final int	NR_OF_PLC_CHANNELS	= 8;

	private static final ObisCode PLC_STATISTICS_OBISCODE = ObisCode.fromString("0.0.53.0.0.255");

	private final AS220 as220;

    private int interval = -1;

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

		Calendar fromCal = Calendar.getInstance(getAs220().getTimeZone());
		fromCal.setTime(from);

		Calendar toCal = Calendar.getInstance(getAs220().getTimeZone());
		toCal.setTime(to);

		byte[] profile = pg.getBufferData(fromCal, toCal);
		PLCStatistics plcStatictics = new PLCStatistics(profile, getAs220().getTimeZone());

		ProfileData pd = new ProfileData();
		pd.setChannelInfos(plcStatictics.getChannelInfos());
		pd.setIntervalDatas(plcStatictics.getIntervalDatas());

		return pd;
	}

	public int getNrOfChannels() {
		return NR_OF_PLC_CHANNELS;
	}

    public int getProfileInterval() throws IOException {
        if(interval == -1){
            interval = getAs220().getCosemObjectFactory().getProfileGeneric(PLC_STATISTICS_OBISCODE).getCapturePeriod();
        }
        return interval;
    }

}
