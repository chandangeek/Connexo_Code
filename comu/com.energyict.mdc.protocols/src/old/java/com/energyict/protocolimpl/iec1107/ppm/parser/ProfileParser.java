/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm.parser;

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
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** @author fbo */

public class ProfileParser {

	/* debug */
	//private boolean parseFirstDayOnly = true;// KV 22072005 unused code
	private boolean dbg = false;

	private PPM ppm = null;
	private RegisterFactory rFactory = null;
	private Date meterTime;

	private Assembler[] assemblerTable = new Assembler[256];
	private Assembly assembly = null;

	private ProfileData targetProfileData = null;
	//IntervalData targetIntervalData = null; // KV 22072005 unused code

	private int nrOfChannels = 1;
	private int integrationPeriod = 30;
	private ScalingFactor scalingFactor = null;
	private LoadProfileDefinition loadDef = null;

	private FFAssembler ffAssembler = new FFAssembler();
	private DayAssembler dayAssembler = new DayAssembler();
	private NumberAssembler numberAssembler = new NumberAssembler();
	private AAAssembler aaAssembler = new AAAssembler();

	public ProfileParser(PPM ppm, RegisterFactory registerFactory, Date meterTime, LoadProfileDefinition loadDef, boolean dbg) throws IOException {

		this.dbg = dbg;

		this.ppm = ppm;
		this.rFactory = registerFactory;
		this.meterTime = meterTime;
		this.loadDef = loadDef;
		this.nrOfChannels = loadDef.getNrOfChannels();

		if (!this.dbg) {
			this.integrationPeriod = this.rFactory.getIntegrationPeriod().intValue() * 60;
			this.scalingFactor = this.rFactory.getScalingFactor();
		} else {
			this.integrationPeriod = 1800;
			this.scalingFactor = ScalingFactor.REGISTER_CATEGORY_3;
		}

		setAssemblerTable(0xFF, 0xFF, this.ffAssembler);
		setAssemblerTable(0xAA, 0xAA, this.aaAssembler);
		setAssemblerTable(0xE4, 0xE4, this.dayAssembler);
		setAssemblerTable(0x00, 0x99, this.numberAssembler);

	}

	public void setInput(InputStream inputStream) {
		this.assembly = new Assembly(inputStream);
	}

	private void setAssemblerTable(int from, int to, Assembler assembler) {
		for (int i = from; i <= to; i++) {
			if ((i >= 0) && (i < this.assemblerTable.length)) {
				this.assemblerTable[i] = assembler;
			}
		}
	}

	private int getNrOfChannels() throws IOException {
		return this.nrOfChannels;
	}

	public void match() throws IOException {

		int character = this.assembly.read();
		do {
			this.assembly.push(new Byte((byte) character));
			this.assemblerTable[character].workOn(this.assembly);
			character = this.assembly.read();
		} while (character != -1);

		if (this.assembly.getTarget() != null) {
			this.dayAssembler.createProfileData((Day) this.assembly.getTarget());
		}

		if (!((Day) this.assembly.getTarget()).isEmpty()) {
			System.out.println(this.assembly.getTarget());
		}

	}

	public ProfileData getProfileData() throws IOException {
		if (this.targetProfileData == null) {
			this.targetProfileData = new ProfileData();
		}
		this.targetProfileData.setChannelInfos(this.loadDef.toChannelInfoList());
		match();

		return this.targetProfileData;
	}

	public static long hex2dec(byte value) {
		return Long.parseLong(Long.toHexString(value & 0xFF));
	}

	public interface Assembler {
		abstract void workOn(Assembly a) throws IOException;
	}

	class FFAssembler implements Assembler {
		public void workOn(Assembly ta) {
			((Byte) ta.pop()).byteValue();
		}
	}

	class DayAssembler implements Assembler {

		int dayNr = 0;

		public void workOn(Assembly ta) throws IOException {

			if (ta.getTarget() != null) {
				createProfileData((Day) ta.getTarget());
				if (ProfileParser.this.dbg) {
					System.out.println("Day\n" + ta.getTarget());
				}
			}

			((Byte) ta.pop()).byteValue();
			byte[] date = new byte[2];
			ProfileParser.this.assembly.read(date, 0, 2);
			Day day = new Day((int) hex2dec(date[0]), (int) hex2dec(date[1]));

			ta.setTarget(day);
			this.dayNr++;
		}

		/* Create the profile data after a complete day has been parsed.  The time
		 * is actually not that important, but it's just a good time. */
		public void createProfileData(Day aDay) {

			if (aDay.isEmpty()) {
				return;
			}

			for (int hi = 0; hi < aDay.reading.length; hi++) {

				if (!aDay.reading[hi].isEmpty()) {
					IntervalData i = new IntervalData(aDay.reading[hi].date);

					if (aDay.status[hi] != null) {
						i.setEiStatus(aDay.status[hi].getEIStatus());
					}

					for (int vi = 0; vi < aDay.reading[hi].value.length; vi++) {
						i.addValue(aDay.reading[hi].value[vi]);
					}

					ProfileParser.this.targetProfileData.addInterval(i);
				}

			}
		}

	}

	class NumberAssembler implements Assembler {

		int byteNr;
		int[] val = null;

		public void workOn(Assembly ta) throws IOException {

			Day day = (Day) ta.getTarget();
			int tempVal = (int) hex2dec(((Byte) ta.pop()).byteValue());

			if (day == null) {
				return;
			}

			getVal()[this.byteNr] = (byte) tempVal;
			this.byteNr++;

			if (this.byteNr != (ProfileParser.this.nrOfChannels * 3 + 1)) {
				return;
			}

			if ((day.readIndex < 48) && day.reading[day.readIndex].date.before(ProfileParser.this.meterTime)) {

				/* 1) create a status object */
				day.status[day.readIndex] = new LoadProfileStatus((byte) getVal()[0]);

				/* 2) create a reading */
				for (int vi = 0; vi < ProfileParser.this.nrOfChannels; vi++) {
					day.reading[day.readIndex].value[vi] = constructValue(getVal(), (vi * 3) + 1);

				}

				/* 3) some debugging info */
				day.readingString[day.readIndex] = " ->" + getVal()[0] + " " + getVal()[1] + " " + getVal()[2] + " " + getVal()[3];

			}
			this.byteNr = 0;
			day.readIndex++;

		}

		int[] getVal() {
			if (this.val == null) {
				this.val = new int[(ProfileParser.this.nrOfChannels * 3) + 1];
			}
			return this.val;
		}

		// KV 22072005 unused code ?????????
		//	private void createIntervalData(Date endDate, int[] iArray)
		//			throws IOException {
		//		IntervalData i = ProfileParser.this.targetIntervalData = new IntervalData(endDate);
		//		i.setEiStatus(constructStatus(iArray).getEIStatus());
		//		for (int ci = 0; ci < nrOfChannels; ci++)
		//			i.addValue(constructValue(iArray, 1 + ci));
		//
		//	}

		private LoadProfileStatus constructStatus(int[] iArray) {
			return new LoadProfileStatus((byte) iArray[0]);
		}

		private BigDecimal constructValue(int[] iArray, int i) throws IOException {
			long v = iArray[i] * 10000;
			v += (iArray[i + 1] * 100);
			v += iArray[i + 2];
			return ProfileParser.this.scalingFactor.toProfileNumber(v);
		}

	}

	class AAAssembler implements Assembler {

		public void workOn(Assembly ta) throws IOException {

			ta.pop(); /* clear Stack, and NumberAssembler */
			ProfileParser.this.numberAssembler.byteNr = 0;

			System.out.println(ProfileParser.this.assembly);

			byte[] jmpSize = new byte[2];
			ta.read(jmpSize, 0, 2);

			long jmp = Long.parseLong(PPMUtils.toHexaString(jmpSize[1]) + PPMUtils.toHexaString(jmpSize[0]), 16) - 3;

			System.out.println("jump Size = " + jmp);

			if (ta.getTarget() != null) {/* Calculate number of hours under jump */
				Day aDay = (Day) ta.getTarget();
				aDay.readIndex += (jmp + 3) / (1 + (3 * ProfileParser.this.nrOfChannels));
			}

			for (int i = 0; i < jmp; i++) {
				ta.read();
			}

			System.out.println(ProfileParser.this.assembly);

		}

	}

	/**
	 * This class is mainly meant for debugging purposes, it can display itself
	 * in an pretty and structured way.
	 */
	class Day {

		private static final int	SECONDS_PER_DAY	= 24 * 3600;

		int readIndex = 0;

		int day = 0;
		int month = 0;

		LoadProfileStatus[] status = null;
		Interval[] reading = null;
		String[] readingString = null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM H:mm");

		public Day(int day, int month) throws IOException {
			Calendar c = ProtocolUtils.getCalendar(ProfileParser.this.ppm.getTimeZone());
			c.set(c.get(Calendar.YEAR), month - 1, day, 0, 0, 0);

			/*
			 * The meter only uses the month and the day, so the year is never
			 * stored together with the profile data. During the new year period,
			 * this can cause problems with the time stamp, because the profile data
			 * from the meter is data from the previous year, but the calendar is
			 * created with the new year (the time of the commserver). This will
			 * result in profile time stamps in the future. This bug is fixed in the
			 * following code.
			 */
			int currentMonth = ProtocolUtils.getCalendar(ProfileParser.this.ppm.getTimeZone()).get(Calendar.MONTH);
			if (currentMonth < c.get(Calendar.MONTH)) {
				c.add(Calendar.YEAR, -1);
			}

			int iSec = ProfileParser.this.integrationPeriod;
			int iPerDay = SECONDS_PER_DAY/ iSec + 1;

			this.day = day;
			this.month = month;

			this.reading = new Interval[iPerDay];
			this.status = new LoadProfileStatus[iPerDay];
			this.readingString = new String[iPerDay];

			for (int i = 0; i < iPerDay; i++) {
				c.add(Calendar.SECOND, iSec);
				this.reading[i] = new Interval();
				this.reading[i].date = c.getTime();
			}
		}

		boolean isEmpty() {
			for (int i = 0; i < this.reading.length; i++) {
				if (!this.reading[i].isEmpty()) {
					return false;
				}
			}
			return true;
		}

		class Interval {
			Date date;
			BigDecimal[] value;

			Interval() {
				this.value = new BigDecimal[ProfileParser.this.loadDef.getNrOfChannels()];
			}

			boolean isEmpty() {
				for (int i = 0; i < this.value.length; i++) {
					if (this.value[i] != null) {
						return false;
					}
				}
				return true;
			}

			public String toString() {
				StringBuffer sb = new StringBuffer();
				sb.append("[ ");
				for (int i = 0; i < this.value.length; i++) {
					sb.append(this.value[i] + " ");
				}

				sb.append(" ]");
				return sb.toString();
			}
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Day :: day " + this.day + " month " + this.month + "\n");

			for (int i = 0; i < this.status.length; i++) {
				sb.append("[" + this.sdf.format(this.reading[i].date) + "]#");

				for (int ii = 0; ii < this.reading[i].value.length; ii++) {
					sb.append(" [" + this.reading[i].value[ii] + "]");
					sb.append(" [" + ProfileParser.this.loadDef.toList().get(ii) + "]");
				}

				sb.append(" - " + this.status[i]);
				sb.append(" " + this.readingString[i] + "\n");
			}
			sb.append("\n");
			return sb.toString();
		}

	}

}
