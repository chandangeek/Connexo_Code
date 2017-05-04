package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.cosem.Clock;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ClockController;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.DLMSSNAS220;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author jme
 *
 */
public class AS220ClockController implements ClockController {

	private static final int		MX_SHIFT_VALUE	= 900;
	private static final int		MIN_SHIFT_VALUE	= -900;

	private static final int		DATA_LENGTH		= 12;
	private static final int		BYTE_LENGTH		= 8;

	public static final ObisCode	CLOCK_OBISCODE	= ObisCode.fromString("0.0.1.0.0.255");

	private final AS220		as220;

	/**
	 * @param as220
	 */
	public AS220ClockController(AS220 as220) {
		this.as220 = as220;
	}

	/**
	 * @return
	 */
	public DLMSSNAS220 getAs220() {
		return as220;
	}

	public Date getTime() throws IOException {
		Clock clock = getAs220().getCosemObjectFactory().getClock(CLOCK_OBISCODE);
		clock.getDateTime().getTime();
		getAs220().setDstFlag(clock.getDstFlag());
		return clock.getDateTime();
	}

	public void setTime() throws IOException {
		setTime(new Date());
	}

	public void setTime(Date date) throws IOException {
		Date correctedTime = new Date(date.getTime());
		Calendar calendar = null;
		if (getAs220().isRequestTimeZone()) {
			if (getAs220().getDstFlag() == 0) {
				calendar = ProtocolUtils.getCalendar(false, getAs220().requestTimeZone());
				calendar.setTime(correctedTime);
			} else if (getAs220().getDstFlag() == 1) {
				calendar = ProtocolUtils.getCalendar(true, getAs220().requestTimeZone());
				calendar.setTime(correctedTime);
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
		byte[] byteTimeBuffer = new byte[14];
		byte dayOfWeek = (byte) calendar.get(Calendar.DAY_OF_WEEK);
		int ptr = 0;

		byteTimeBuffer[ptr++] = AxdrType.OCTET_STRING.getTag();
		byteTimeBuffer[ptr++] = DATA_LENGTH; // length
		byteTimeBuffer[ptr++] = (byte) (calendar.get(Calendar.YEAR) >> BYTE_LENGTH);
		byteTimeBuffer[ptr++] = (byte) calendar.get(Calendar.YEAR);
		byteTimeBuffer[ptr++] = (byte) (calendar.get(Calendar.MONTH) + 1);
		byteTimeBuffer[ptr++] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
		byteTimeBuffer[ptr++] = (dayOfWeek-- == 1) ? (byte) 7 : dayOfWeek;
		byteTimeBuffer[ptr++] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		byteTimeBuffer[ptr++] = (byte) calendar.get(Calendar.MINUTE);
		byteTimeBuffer[ptr++] = (byte) calendar.get(Calendar.SECOND);
		byteTimeBuffer[ptr++] = (byte) 0xFF;
		byteTimeBuffer[ptr++] = (byte) 0x80;
		byteTimeBuffer[ptr++] = 0x00;
		byteTimeBuffer[ptr++] = generateDstFlag(calendar);

		getAs220().getCosemObjectFactory().getGenericWrite((short) getAs220().getMeterConfig().getClockSN(), DLMSCOSEMGlobals.TIME_TIME).write(byteTimeBuffer);

	}

	/**
	 * @param calendar
	 * @return
	 * @throws IOException
	 */
	private byte generateDstFlag(Calendar calendar) throws IOException {
		byte dstFlag;
		if (getAs220().isRequestTimeZone()) {
			switch (getAs220().getDstFlag()) {
				case 0:
					dstFlag = (byte) 0x00;
					break;
				case 1:
					dstFlag = (byte) 0x80;
					break;
				default:
					throw new IOException("doSetTime(), dst flag is unknown! setTime() before getTime()!");
			}
		} else {
			dstFlag = getAs220().getTimeZone().inDaylightTime(calendar.getTime()) ? (byte) 0x80 : (byte) 0x00;
		}
		return dstFlag;
	}

	public void shiftTime() throws IOException {
		shiftTime(new Date());
	}

	public void shiftTime(Date date) throws IOException {
		Date meterTime = getTime();
		Date endDate = new Date();
		int shiftValue = (int) ((endDate.getTime() - meterTime.getTime()) / 1000);

		if (shiftValue > 900) {
			shiftValue = MX_SHIFT_VALUE;
		} else if (shiftValue < MIN_SHIFT_VALUE) {
			shiftValue = MIN_SHIFT_VALUE;
		}

		if (shiftValue == 0) {
			LogFactory.getLog(getClass()).info("Skipping shiftTime. Time is up to date. (shiftValue=0)");
			return;
		} else {
			LogFactory.getLog(getClass()).debug("Shifting time " + shiftValue + " seconds.");
		}

		getAs220().getCosemObjectFactory().getClock(CLOCK_OBISCODE).shiftTime(shiftValue);

	}

}
