package com.energyict.protocolimplv2.dlms.ei7.frames;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;

public class DailyReadings {
	public Unsigned32 unixTime;
	public Unsigned16 dailyDiagnostic;
	public Unsigned32 currentIndexOfConvertedVolume;
	public Unsigned32 currentIndexOfConvertedVolumeUnderAlarm;
	public Unsigned8 currentActiveTariff;
}
