package com.energyict.protocolimpl.cm10;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

public class MeterDemandsTable {
	
	private CM10 cm10Protocol;
	
	public MeterDemandsTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public void parse(byte[] data, ProfileData profileData, Date start) throws IOException {
		PowerFailDetailsTable powerFailDetailsTable = cm10Protocol.getPowerFailDetailsTable();
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.setTime(start);
		int numberOfChannels = cm10Protocol.getNumberOfChannels();
		int i = 0; 
		int intervalInSeconds = cm10Protocol.getProfileInterval();
		int length = data.length;
		while (i < length) {
			cal.add(Calendar.SECOND, intervalInSeconds);
			Date endOfInterval = cal.getTime();
			IntervalData intervalData = new IntervalData(endOfInterval); 
			boolean powerUp = powerFailDetailsTable.isPowerUp(endOfInterval);
			boolean powerDown = powerFailDetailsTable.isPowerDown(endOfInterval);
			if (powerDown) {
				intervalData.addEiStatus(IntervalStateBits.POWERDOWN);
				intervalData.addProtocolStatus(IntervalStateBits.POWERDOWN);
				//cm10Protocol.getLogger().info(endOfInterval + ", POWERDOWN");
			}
			if (powerUp) {
				intervalData.addEiStatus(IntervalStateBits.POWERUP);
				intervalData.addProtocolStatus(IntervalStateBits.POWERUP);
				//cm10Protocol.getLogger().info(endOfInterval + ", POWERUP");
			}
			for (int j = 0; j < numberOfChannels; j++) {
				BigDecimal value = new BigDecimal(ProtocolUtils.getLongLE(data, i + (j * 2), 2));
				if (value.equals(new BigDecimal(16383))) {
					intervalData.addEiStatus(IntervalStateBits.MISSING);
					intervalData.addProtocolStatus(IntervalStateBits.MISSING);
					intervalData.addValue(new BigDecimal(0));
					//cm10Protocol.getLogger().info(endOfInterval + ", MISSING");
				}
				else {
					intervalData.addValue(value);
				}
			}
			profileData.addInterval(intervalData);
			i = i + (numberOfChannels * 2); // 2 bytes per channel interval record
		}
	}

}
