/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1.opus;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.RegisterFactory;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** @author fbo */

public class OpusProfileParser {

	private PPM ppm = null;

	private RegisterFactory rFactory = null;
	private Date meterTime = null;
	private LoadProfileDefinition loadDef = null;
	private ScalingFactor scalingFactor = null;
	private int nrOfChannels = 5;

	/* Length of an interval in minutes */
	private int intervalLength = 0;
	private ArrayList days = new ArrayList();
	private Calendar currentIntervalTime = null;
	private int month = 9;
	private int day = 1;
	private Day currentDay = null;
	private Interval currentInterval = null;

	/* Init the class */
	public OpusProfileParser(PPM ppm, RegisterFactory registerFactory, Date meterTime)
	throws IOException {

		this.ppm = ppm;
		this.rFactory = registerFactory;
		this.meterTime = meterTime;

		this.loadDef = rFactory.getLoadProfileDefinition();
		this.scalingFactor = rFactory.getScalingFactor();

		nrOfChannels = loadDef.getNrOfChannels();
		intervalLength = rFactory.getIntegrationPeriod().intValue();

	}

	public ProfileData getProfileData() throws IOException {
		ProfileData profileData = new ProfileData();
		profileData.setChannelInfos(loadDef.toChannelInfoList());

		for (int i = 0; i < days.size(); i++) {
			Day day = (Day) days.get(i);
			for (int iindex = 0; iindex < day.fillIndex; iindex++) {
				Interval interval = (Interval) day.interval.get(iindex);
				boolean canAdd = !interval.isPowerDown();
				canAdd = canAdd && !interval.isBlank();
				canAdd = canAdd && interval.hasMinimumAge();
				if ( canAdd ) {
					profileData.addInterval(interval.toIntervalData());
				}
			}
		}
		profileData.generateEvents();

		return profileData;
	}

	public void add(OpusResponse opusResponse) throws IOException {

		if (opusResponse == null) {
			return;
		}
		if (!opusResponse.isDefinitionMessageValid()) {
			return;
		}

		parseDefinitionMessage(opusResponse.getDefinitionMessage());

		for (int i = 0; i < opusResponse.getDataMessages().size(); i++) {
			byte[] data = (byte[]) opusResponse.getDataMessages().get(i);

			List list = PPMUtils.split(data, 1);

			if( list.size() != 11 ) {
				String msg = "Received message has unexpected format.  ";
				msg += "( #fields=" + list.size() + " )";
				throw new IOException( msg );
			}

			for (int li = 1; li < list.size() - 2; li++) {
				addReading((byte[]) list.get(li));
			}

		}
	}

	/**
	 * From the meter we only get a day and a month number for a profileentry.
	 * We need to match this with a year. To do this we take the meterdate, and
	 * see if the day and month of the profileentry have alread happened in this
	 * year. If not these entries are from the previous year.
	 * @throws IOException
	 */
	public void parseDefinitionMessage(byte[] definitionMessage) throws IOException {
		currentIntervalTime = ProtocolUtils.getCalendar(ppm.getTimeZone());

		List list = PPMUtils.split(definitionMessage, 1);

		if( list.size() != 11 ) {
			String msg = "Received message has unexpected format.  ";
			msg += "( #fields=" + list.size() + " )";
			throw new IOException( msg );
		}

		String dayString = PPMUtils.parseBCDString((byte[]) list.get(2));
		String monthString = PPMUtils.parseBCDString((byte[]) list.get(3));

		int day = Integer.parseInt(dayString);
		int month = Integer.parseInt(monthString);

		int year = currentIntervalTime.get(Calendar.YEAR);

		Calendar tempCalendar = ProtocolUtils.getCalendar(ppm.getTimeZone());
		tempCalendar.set(year, month - 1, day, 0, 0, 0);
		Date tempDate = tempCalendar.getTime();

		if (tempDate.after(meterTime)) {
			year -= 1;
		}

		currentIntervalTime.set(year, month - 1, day, 0, 0, 0);

		addDay();
		addInterval();

	}

	public void addReading(byte[] input) throws IOException {
		if (currentInterval.fillIndex == nrOfChannels) {
			addInterval();
		}
		currentInterval.addReading(input);
	}

	public void addInterval() {

		Interval i = new Interval();
		i.time = getNextIntervalTime();
		currentDay.addInterval(i);
		currentInterval = i;

	}

	public Day addDay() {
		Day day = new Day();
		days.add(day);
		currentDay = day;
		return day;
	}

	public Date getNextIntervalTime() {
		if (currentIntervalTime == null) {
			currentIntervalTime = ProtocolUtils.getCalendar(ppm.getTimeZone());
			currentIntervalTime.set(currentIntervalTime.get(Calendar.YEAR),
					month - 1, day, 0, 0, 0);
		}

		currentIntervalTime.add(Calendar.MINUTE, intervalLength);

		return currentIntervalTime.getTime();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < days.size(); i++) {
			sb.append(days.get(i).toString() + " \n");
		}
		return sb.toString();
	}

	class Interval {

		private final byte[] powerDownCode = { 0x39, 0x39, 0x39, 0x39, 0x39,
				0x39 };

		Date time = null;

		//String packetNr = null; // KV 22072005 unused code

		/** reading is the complete entry */
		byte[][] reading = new byte[5][];

		/** only the value part of the reading */
		byte[][] byteValue = new byte[5][];

		/** status is only the status byte */
		byte[] status = null;

		// String displayString; // KV 22072005 unused code?????

		int fillIndex = 0;

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM H:mm");

		/*
		 * Finally, the D.mned thing works ... Let me explain how: The length of
		 * a reading varies as needed, so it can be 123, 1234, 12345, you get
		 * the picture.
		 *
		 * The maximum length for a value is 5 digits. But it can also have a
		 * status byte. So the maximum length for a demand value is 6 digits.
		 *
		 * If the length is shorter than 6, it does not have a status.
		 *
		 *
		 */
		void addReading(byte[] reading) throws IOException {

			//System.out.print(" reading " + PPMUtils.toHexaString(reading));

			if (reading[0] != '?') {

				int statusLenght = (reading.length == 6) ? 1 : 0;

				if (statusLenght == 0) {
					byteValue[fillIndex] = new byte[reading.length];
					System.arraycopy(reading, 0, byteValue[fillIndex], 0,
							reading.length);
				} else {
					byteValue[fillIndex] = new byte[reading.length - 1];
					System.arraycopy(reading, 1, byteValue[fillIndex], 0,
							reading.length - 1);
					status = new byte[1];
					System.arraycopy(reading, 0, status, 0, 1);

				}
			}
			this.reading[fillIndex] = reading;
			fillIndex += 1;

		}

		/**
		 * A demand value is powerdown when it has a '999999' value ( so 6 9's )
		 *
		 * @return true if demand value is powerdown (999999)
		 */
		boolean isPowerDown() {
			for (int i = 0; i < nrOfChannels; i++) {
				if (reading[i] != null
						&& reading[i].length == powerDownCode.length) {
					if ((powerDownCode[0] == reading[i][0])
							&& (powerDownCode[1] == reading[i][1])
							&& (powerDownCode[2] == reading[i][2])
							&& (powerDownCode[3] == reading[i][3])
							&& (powerDownCode[4] == reading[i][4])
							&& (powerDownCode[5] == reading[i][5])) {
						return true;
					}
				}

			}
			return false;
		}

		/**
		 * A demand value is blank when it has a '?' value ( so 1 ? )
		 *
		 * @return true if demand value blank
		 */
		boolean isBlank() {
			for (int i = 0; i < nrOfChannels; i++) {
				if (reading[i][0] == '?') {
					return true;
				}
			}
			return false;
		}

		private BigDecimal constructValue(byte[] aValue) throws IOException {
			int numberValue = 0;
			String bcdString = "";
			try {
				bcdString = PPMUtils.parseBCDString(aValue);
				numberValue = Integer.parseInt(bcdString);
			} catch (NumberFormatException e) {
				ppm.getLogger().info(e.getMessage() + " " + bcdString);
			}
			return scalingFactor.toProfileNumber(numberValue);
		}

		private LoadProfileStatus constructStatus(byte[] value)
		throws IOException {
			int status = 0;
			try {
				String bcdString = PPMUtils.parseBCDString(value);
				status = Integer.parseInt(bcdString, 16);
			} catch (NumberFormatException e) {
				ppm.getLogger().info(e.getMessage() + " " + e.getStackTrace());
			}
			return new LoadProfileStatus(status);
		}

		IntervalData toIntervalData() throws IOException {
			IntervalData intervalData = new IntervalData(time);
			if (status != null) {
				intervalData.setEiStatus(constructStatus(status).getEIStatus());
			}
			for (int index = 0; index < nrOfChannels; index++) {
				intervalData.addValue(constructValue(byteValue[index]));
			}
			return intervalData;
		}

		/**@return true if the interval is old enough to be added to the profile
		 *data */
		boolean hasMinimumAge(){
			long age = meterTime.getTime() - time.getTime();
			return age > PPM.MINIMUM_INTERVAL_AGE;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();

			try {

				sb.append(sdf.format(time) + " - ");
				if (isPowerDown()) {
					sb.append("Power down ");
				} else {
					sb.append(PPMUtils.parseBCDString(status) + " - ");
					if (status != null) {
						sb.append(constructStatus(status).toString() + " - ");
					}
					sb.append(PPMUtils.parseBCDString(byteValue[0]) + " ");
					if (byteValue[0] != null) {
						sb.append(constructValue(byteValue[0]) + " - ");
					}
					sb.append(PPMUtils.parseBCDString(byteValue[1]) + " ");
					if (byteValue[1] != null) {
						sb.append(constructValue(byteValue[1]) + " - ");
					}
					sb.append(PPMUtils.parseBCDString(byteValue[2]) + " ");
					if (byteValue[2] != null) {
						sb.append(constructValue(byteValue[2]) + " - ");
					}
					sb.append(PPMUtils.parseBCDString(byteValue[3]) + " ");
					if (byteValue[3] != null) {
						sb.append(constructValue(byteValue[3]) + " - ");
					}
					sb.append(PPMUtils.parseBCDString(byteValue[4]) + " ");
					if (byteValue[4] != null) {
						sb.append(constructValue(byteValue[4]) + " - ");
					}
				}
				sb.append("isPowerDown= " + isPowerDown());
				sb.append(" ]\n ");

			} catch (IOException e) {
			}

			return sb.toString();
		}
	}

	class Day {

		ArrayList interval;

		int fillIndex = 0;

		public Day() {
			interval = new ArrayList();
		}

		public void addInterval(Interval anInterval) {
			interval.add(anInterval);
			fillIndex += 1;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < fillIndex; i++) {
				sb.append(interval.get(i).toString());
			}
			return sb.toString();
		}

	}

}
