package com.energyict.protocolimpl.iec1107.ppm.opus;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppm.PPM;
import com.energyict.protocolimpl.iec1107.ppm.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppm.RegisterFactory;
import com.energyict.protocolimpl.iec1107.ppm.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppm.register.LoadProfileStatus;
import com.energyict.protocolimpl.iec1107.ppm.register.ScalingFactor;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** @author fbo */

public class OpusProfileParser {

	private RegisterFactory rFactory = null;
	private PPM ppm = null;
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
	public OpusProfileParser(PPM ppm, RegisterFactory registerFactory, Date meterTime) throws IOException {

		this.ppm = ppm;
		this.rFactory = registerFactory;
		this.meterTime = meterTime;

		this.loadDef = this.rFactory.getLoadProfileDefinition();
		this.scalingFactor = this.rFactory.getScalingFactor();

		this.nrOfChannels = this.loadDef.getNrOfChannels();
		this.intervalLength = this.rFactory.getIntegrationPeriod().intValue();

	}

	public ProfileData getProfileData() throws IOException {
		ProfileData profileData = new ProfileData();
		profileData.setChannelInfos(this.loadDef.toChannelInfoList());

		for (int i = 0; i < this.days.size(); i++) {
			Day day = (Day) this.days.get(i);
			for (int iindex = 0; iindex < day.fillIndex; iindex++) {
				Interval interval = (Interval) day.interval.get(iindex);
				boolean canAdd = !interval.isPowerDown();
				canAdd = canAdd && !interval.isBlank();
				canAdd = canAdd && interval.hasMinimumAge();
				if (canAdd) {
					profileData.addInterval(interval.toIntervalData());
				}
			}
		}

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

			if (list.size() != 11) {
				String msg = "Received message has unexpected format.  ";
				msg += "( #fields=" + list.size() + " )";
				throw new IOException(msg);
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

		this.currentIntervalTime = ProtocolUtils.getCalendar(this.ppm.getTimeZone());

		List list = PPMUtils.split(definitionMessage, 1);
		if (list.size() != 11) {
			String msg = "Received message has unexpected format.  ";
			msg += "( #fields=" + list.size() + " )";
			throw new IOException(msg);
		}

		String dayString = PPMUtils.parseBCDString((byte[]) list.get(2));
		String monthString = PPMUtils.parseBCDString((byte[]) list.get(3));

		int day = Integer.parseInt(dayString);
		int month = Integer.parseInt(monthString);

		int year = this.currentIntervalTime.get(Calendar.YEAR);

		Calendar tempCalendar = ProtocolUtils.getCalendar(this.ppm.getTimeZone());
		tempCalendar.set(year, month - 1, day, 0, 0, 0);
		Date tempDate = tempCalendar.getTime();

		if (tempDate.after(this.meterTime)) {
			year -= 1;
		}

		this.currentIntervalTime.set(year, month - 1, day, 0, 0, 0);

		addDay();
		addInterval();

	}

	public void addReading(byte[] input) throws IOException {
		if (this.currentInterval.fillIndex == this.nrOfChannels) {
			addInterval();
		}
		this.currentInterval.addReading(input);
	}

	public void addInterval() {

		Interval i = new Interval();
		i.time = getNextIntervalTime();
		this.currentDay.addInterval(i);
		this.currentInterval = i;

	}

	public Day addDay() {
		Day day = new Day();
		this.days.add(day);
		this.currentDay = day;
		return day;
	}

	public Date getNextIntervalTime() {
		if (this.currentIntervalTime == null) {
			this.currentIntervalTime = ProtocolUtils.getCalendar(this.ppm.getTimeZone());
			this.currentIntervalTime.set(this.currentIntervalTime.get(Calendar.YEAR), this.month - 1, this.day, 0, 0, 0);
		}

		this.currentIntervalTime.add(Calendar.MINUTE, this.intervalLength);

		return this.currentIntervalTime.getTime();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.days.size(); i++) {
			sb.append(this.days.get(i).toString() + " \n");
		}
		return sb.toString();
	}

	class Interval {

		private final byte[] powerDownCode = { 0x39, 0x39, 0x39, 0x39, 0x39, 0x39 };

		private Date time = null;
		private String packetNr = null;
		private byte[] status = null;
		private byte[][] byteValue = new byte[5][];
		private String displayString;
		private int fillIndex = 0;

		private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM H:mm");

		private void addReading(byte[] reading) throws IOException {
			if ((this.fillIndex == 0) && (reading != null) && (reading.length > 6)) {
				int statusLength = reading.length - 6;
				this.status = new byte[statusLength];
				System.arraycopy(reading, 0, this.status, 0, statusLength);
				this.byteValue[this.fillIndex] = new byte[reading.length - statusLength];
				System.arraycopy(reading, statusLength, this.byteValue[this.fillIndex], 0, (reading.length - statusLength));
			} else {
				this.byteValue[this.fillIndex] = reading;
			}
			this.fillIndex += 1;
		}

		private boolean isPowerDown() {
			for (int i = 0; i < OpusProfileParser.this.nrOfChannels; i++) {
				if ((this.byteValue[i] != null) && (this.byteValue[i].length == this.powerDownCode.length)) {
					if ((this.powerDownCode[0] == this.byteValue[i][0]) && (this.powerDownCode[1] == this.byteValue[i][1]) && (this.powerDownCode[2] == this.byteValue[i][2])
							&& (this.powerDownCode[3] == this.byteValue[i][3]) && (this.powerDownCode[4] == this.byteValue[i][4]) && (this.powerDownCode[5] == this.byteValue[i][5])) {
						return true;
					}
				}

			}
			return false;
		}

		private boolean isBlank() {
			for (int i = 0; i < OpusProfileParser.this.nrOfChannels; i++) {
				if (this.byteValue[i][0] == '?') {
					return true;
				}
			}
			return false;
		}

		private BigDecimal constructValue(byte[] aValue) throws IOException {
			int numberValue = 0;
			try {
				String bcdString = PPMUtils.parseBCDString(aValue);
				numberValue = Integer.parseInt(bcdString);
			} catch (NumberFormatException e) {
			}
			return OpusProfileParser.this.scalingFactor.toProfileNumber(numberValue);
		}

		private LoadProfileStatus constructStatus(byte[] value) throws IOException {
			int status = 0;
			try {
				String bcdString = PPMUtils.parseBCDString(value);
				status = Integer.parseInt(bcdString, 16);
			} catch (NumberFormatException e) {
			}
			return new LoadProfileStatus(status);
		}

		private IntervalData toIntervalData() throws IOException {
			IntervalData intervalData = new IntervalData(this.time);
			intervalData.setEiStatus(constructStatus(this.status).getEIStatus());
			for (int index = 0; index < OpusProfileParser.this.nrOfChannels; index++) {
				intervalData.addValue(constructValue(this.byteValue[index]));
			}
			return intervalData;
		}

		/**
		 * @return true if the interval is old enough to be added to the profile
		 * data
		 */
		private boolean hasMinimumAge() {
			long age = OpusProfileParser.this.meterTime.getTime() - this.time.getTime();
			return age > PPM.MINIMUM_INTERVAL_AGE;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();

			try {

				sb.append(" [ " + this.displayString + " ] [ ");
				sb.append(this.sdf.format(this.time) + " - ");
				if (isPowerDown()) {
					sb.append("Power down ");
				} else {
					sb.append(PPMUtils.parseBCDString(this.status) + " - ");
					sb.append(constructStatus(this.status).toString() + " - ");
					sb.append(PPMUtils.parseBCDString(this.byteValue[0]) + " ");
					sb.append(constructValue(this.byteValue[0]) + " - ");
					sb.append(PPMUtils.parseBCDString(this.byteValue[1]) + " ");
					sb.append(constructValue(this.byteValue[1]) + " - ");
					sb.append(PPMUtils.parseBCDString(this.byteValue[2]) + " ");
					sb.append(constructValue(this.byteValue[2]) + " - ");
					sb.append(PPMUtils.parseBCDString(this.byteValue[3]) + " ");
					sb.append(constructValue(this.byteValue[3]) + " - ");
					sb.append(PPMUtils.parseBCDString(this.byteValue[4]) + " ");
					sb.append(constructValue(this.byteValue[4]) + " - ");
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
			this.interval = new ArrayList();
		}

		public void addInterval(Interval anInterval) {
			this.interval.add(anInterval);
			this.fillIndex += 1;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < this.fillIndex; i++) {
				sb.append(this.interval.get(i).toString());
			}
			return sb.toString();
		}

	}

}
