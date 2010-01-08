package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.cosem.Clock;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ClockController;

/**
 * @author jme
 *
 */
public class AS220ClockController implements ClockController {

	private static final int		DATA_LENGTH		= 12;
	private static final int		BYTE_LENGTH		= 8;

	private static final ObisCode	CLOCK_OBISCODE	= ObisCode.fromString("0.0.1.0.0.255");

	private final DLMSSNAS220		as220;

	public AS220ClockController(DLMSSNAS220 as220) {
		this.as220 = as220;
	}

	public DLMSSNAS220 getAs220() {
		return as220;
	}

	public Date getTime() throws IOException {
		Clock clock = getAs220().getCosemObjectFactory().getClock(CLOCK_OBISCODE);
		Date date = clock.getDateTime();
		getAs220().setDstFlag(clock.getDstFlag());
		return date;
	}

	public void setTime() throws IOException {
		setTime(new Date());
	}

	public void setTime(Date date) throws IOException {
		Calendar calendar = null;
		if (getAs220().isRequestTimeZone()) {
			if (getAs220().getDstFlag() == 0) {
				calendar = ProtocolUtils.getCalendar(false, getAs220().requestTimeZone());
				calendar.setTime(date);
			} else if (getAs220().getDstFlag() == 1) {
				calendar = ProtocolUtils.getCalendar(true, getAs220().requestTimeZone());
				calendar.setTime(date);
			} else {
				throw new IOException("setTime(), dst flag is unknown! setTime() before getTime()!");
			}
		} else {
			calendar = ProtocolUtils.initCalendar(false, getAs220().getTimeZone());
		}
		calendar.add(Calendar.MILLISECOND, getAs220().getRoundTripCorrection());
		doSetTime(calendar);
	}

	/**
	 * Write the time from the given {@link Calendar} to the device
	 *
	 * @param calendar
	 * @throws IOException
	 */
	private void doSetTime(Calendar calendar) throws IOException {
		byte[] byteTimeBuffer = new byte[15];

		byteTimeBuffer[0] = 1;
		byteTimeBuffer[1] = DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING;
		byteTimeBuffer[2] = DATA_LENGTH; // length
		byteTimeBuffer[3] = (byte) (calendar.get(Calendar.YEAR) >> BYTE_LENGTH);
		byteTimeBuffer[4] = (byte) calendar.get(Calendar.YEAR);
		byteTimeBuffer[5] = (byte) (calendar.get(Calendar.MONTH) + 1);
		byteTimeBuffer[6] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
		byte dayOfWeek = (byte) calendar.get(Calendar.DAY_OF_WEEK);
		byteTimeBuffer[7] = (dayOfWeek-- == 1) ? (byte) 7 : dayOfWeek;
		byteTimeBuffer[8] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		byteTimeBuffer[9] = (byte) calendar.get(Calendar.MINUTE);
		byteTimeBuffer[10] = (byte) calendar.get(Calendar.SECOND);
		byteTimeBuffer[11] = (byte) 0xFF;
		byteTimeBuffer[12] = (byte) 0x80;
		byteTimeBuffer[13] = 0x00;

		if (getAs220().isRequestTimeZone()) {
			if (getAs220().getDstFlag() == 0) {
				byteTimeBuffer[14] = 0x00;
			} else if (getAs220().getDstFlag() == 1) {
				byteTimeBuffer[14] = (byte) 0x80;
			} else {
				throw new IOException("doSetTime(), dst flag is unknown! setTime() before getTime()!");
			}
		} else {
			if (getAs220().getTimeZone().inDaylightTime(calendar.getTime())) {
				byteTimeBuffer[14] = (byte) 0x80;
			} else {
				byteTimeBuffer[14] = 0x00;
			}
		}

		getAs220().getCosemObjectFactory().getGenericWrite((short) getAs220().getMeterConfig().getClockSN(), DLMSCOSEMGlobals.TIME_TIME).write(byteTimeBuffer);

	}

}
