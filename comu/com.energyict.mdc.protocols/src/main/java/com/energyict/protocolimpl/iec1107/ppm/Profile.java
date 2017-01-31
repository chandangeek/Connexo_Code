/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.ppm.opus.OpusProfileParser;
import com.energyict.protocolimpl.iec1107.ppm.parser.ProfileParser;
import com.energyict.protocolimpl.iec1107.ppm.parser.ProfileReverseParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <strong>Profile responsibilties: </strong>
 * <ol>
 * <li>Control/Manage reading meter profile data</li>
 * </ol>
 *
 * @author fbo
 */
public class Profile {

	/** The following statics are only used for debugging purposes */
	private static final boolean	READFROMFILE			= false;
	private static final boolean	WRITETOFILE				= false;
	private static final String		FILENAME				= "1257407814849_PR";
	private static final String		PATHNAME				= "C:\\EnergyICT\\WorkingDir\\ppm_profiles\\";
	private static final String		EXTENSION				= ".hex";

	/** Some time statics */
	private static final int		HOURS_PER_DAY			= 24;
	private static final int		MINUTES_PER_HOUR		= 60;
	private static final int		SECONDS_PER_MINUTE		= 60;
	private static final int		MS_PER_SECOND			= 1000;

	/** Some profile statics */
	private static final int		CHANNEL_BYTE_SIZE		= 6;
	private static final int		STATUS_BYTE_SIZE		= 2;
	private static final int		E4_BYTE_SIZE			= 2;
	private static final int		DATE_BYTE_SIZE			= 4;
	private static final int		OPUSVALUES_PER_PACKET	= 8;
	private static final long		OPUS_SPECIALDAY_NR		= 10;

	private PPM ppm = null;
	private	RegisterFactory rFactory = null;
	private Logger log = null;

	private Date beginDate = null;
	private Date endDate = null;
	private Date meterDate = null;

	private boolean includeEvents = false;

	/**
	 * Creates a new instance of Profile
	 */
	public Profile(PPM ppm, RegisterFactory rf) throws IOException {
		this.ppm = ppm;
		rFactory = rf;
		if (ppm != null) {
			log = ppm.getLogger();
		}
	}

	/**
	 * Read the meterDate from the device
	 *
	 * @return The date of the meter
	 * @throws IOException
	 */
	private Date getMeterDate() throws IOException {
		if (meterDate == null) {
			this.meterDate = rFactory.getTimeDate();
		}
		return meterDate;
	}

	/**
	 * Given a begin- and enddate will return a Profile object with all the
	 * meter readings and events.
	 *
	 * @param beginDate for retrieving meterreadings
	 * @param endDate for retrieving meterreadings
	 */
	public ProfileData getProfileData(Date beginDate, Date endDate, boolean includeEvents) throws IOException {
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.includeEvents = includeEvents;

		if (ppm.isOpus()) {
			return doOpusProtocol();
		} else {
			return doIECProtocol();
		}
	}

	/**
	 * Get the {@link ProfileData} using the OPUS protocol version
	 *
	 * @return The {@link ProfileData} from the meter
	 * @throws NestedIOException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	private ProfileData doOpusProtocol() throws IOException {
		OpusProfileParser opp = new OpusProfileParser(ppm, rFactory, getMeterDate());

		long bDate = daySince1970(ppm.getTimeZone(), beginDate);
		long eDate = daySince1970(ppm.getTimeZone(), endDate);
		long mDate = daySince1970(ppm.getTimeZone(), getMeterDate());

		Calendar current = Calendar.getInstance( ppm.getTimeZone() );
		current.setTime( beginDate );

		String logMessage = "Retrieve profile begin:" + beginDate + " end: " + endDate;
		ppm.getLogger().fine( logMessage );

		for (; (bDate <= mDate ) && (bDate <= eDate ); bDate++) {

			int nrHours = PPMUtils.hoursInDay( current );
			logMessage = "Retieve profile for " + current.getTime() + " nrHours= " + nrHours;
			ppm.getLogger().fine( logMessage );

			/*
			 * The nr of packets to retrieve is calculated:
			 * NR PACKETS = NR CHANNELS * NR INTERVALS per day
			 * NR INTERVALS per day = LENGTH OF DAY (in sec) / PROFILE INTERVAL
			 * In 1 packet there is room for 8 values, so devide by 8
			 */
			int nrSeconds = nrHours * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;
			int nrPackets = (ppm.getNumberOfChannels() * (nrSeconds / ppm.getProfileInterval())) / OPUSVALUES_PER_PACKET;

			long opusDayNr = mDate - bDate + OPUS_SPECIALDAY_NR;
			boolean doAdd = true;

			if( opusDayNr == OPUS_SPECIALDAY_NR ) {
				Calendar mCalendar = ProtocolUtils.getCalendar( ppm.getTimeZone() );
				mCalendar.setTime(meterDate);
				int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
				if( hour <= 0 ) {
					long iPeriod = rFactory.getIntegrationPeriod().intValue();
					long minutes = mCalendar.get(Calendar.MINUTE);
					if( minutes < (iPeriod+(PPM.MINIMUM_INTERVAL_AGE/MS_PER_SECOND/SECONDS_PER_MINUTE) ) ) {
						doAdd = false;
					}
				}
			}

			if(doAdd) {
				opp.add(ppm.getOpusConnection().readRegister("550", 0, (int) opusDayNr, nrPackets, true));
			}

			current.add( Calendar.DAY_OF_YEAR, 1 );

		}

		ProfileData pd = opp.getProfileData();
		if (isIncludeEvents()) {
			pd.generateEvents();
		}

		return pd;
	}

	/**
	 * Get the {@link ProfileData} using the IEC1107 protocol version
	 *
	 * @return The {@link ProfileData} from the meter
	 * @throws IOException
	 */
	private ProfileData doIECProtocol() throws IOException {
		boolean readFromFile = READFROMFILE;
		byte[] data;

		int intPeriodInSec = rFactory.getIntegrationPeriod().intValue() * SECONDS_PER_MINUTE;

		long byteSize = nrBytesToRetrieve(beginDate, endDate, intPeriodInSec, ppm.getNumberOfChannels());
		log.log(Level.INFO, "IEC protocol, byteSize= " + byteSize);

		if (readFromFile) {
			data = PPMUtils.fromFile(PATHNAME + FILENAME + EXTENSION);
		} else {
			data = rFactory.getRegisterRawData(RegisterFactory.R_LOAD_PROFILE, (int) byteSize);
			if (WRITETOFILE) {
				PPMUtils.toFile(data, PATHNAME + System.currentTimeMillis()+ "_PR" + EXTENSION);
			}
		}

		Date date = rFactory.getTimeDate();
		int nrChannels = rFactory.getLoadProfileDefinition().getNrOfChannels();
		int intervalLength = rFactory.getIntegrationPeriod().intValue();

		ProfileReverseParser prp = new ProfileReverseParser(date, nrChannels, intervalLength * SECONDS_PER_MINUTE, ppm.getTimeZone());
		prp.setInput(data);

		ProfileParser pp = new ProfileParser(ppm, rFactory, rFactory.getTimeDate(), rFactory.getLoadProfileDefinition(), false);
		pp.setInput(new ByteArrayInputStream(prp.match()));

		ProfileData pd = pp.getProfileData();

		if (isIncludeEvents()) {
			pd.generateEvents();
		}
		return pd;
	}

	/**
	 * ( nr sec day / int. duration sec ) + 1 +1 for extra iperiod
	 *
	 * @param interval The profileInterval in seconds
	 * @return The maximum of intervals in one day
	 */
	private static int maxIntervalsPerDay(int interval) {
		return minIntervalsPerDay(interval) + 1;
	}

	/**
	 * @param interval The profile interval in seconds
	 * @return The minimum of intervals in opne day
	 */
	private static int minIntervalsPerDay(int interval) {
		return ((HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE) / interval);
	}

	/**
	 * Calculate difference and add 1 ( there is always at least 1 day ) Also
	 * only a complete day can be parsed. Take start DAY and end DAY, and
	 * calculate difference.
	 */
	static long nrDaysInPeriodIEC(Date start, Date end) {
		return nrDaysInPeriodOPUS(start, end)+ 1;
	}

	/**
	 * Calculate difference and subtract 1; today is retrieved separately.
	 */
	private static long nrDaysInPeriodOPUS(Date start, Date end) {
		// day index since start of count
		long sd = start.getTime() / (MS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY);
		long ed = end.getTime() / (MS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY);
		return ed - sd;
	}

	/**
	 * Get the number of days passed since 1970 for a given {@link Date} and {@link TimeZone}
	 *
	 * @param timeZone The {@link TimeZone} used in the calculations
	 * @param date The date to count to
	 * @return The number of days from 1970 to the given date
	 */
	private static long daySince1970(TimeZone timeZone, Date date) {

		Calendar inputCalendar = ProtocolUtils.getCalendar( timeZone );
		inputCalendar.setTime(date);

		Calendar calendar = ProtocolUtils.getCalendar( timeZone );
		calendar.clear();

		int day = inputCalendar.get(Calendar.DAY_OF_MONTH);
		int month = inputCalendar.get(Calendar.MONTH);
		int year = inputCalendar.get(Calendar.YEAR);

		calendar.set(year, month, day);

		return calendar.getTimeInMillis() / MS_PER_SECOND / SECONDS_PER_MINUTE / MINUTES_PER_HOUR / HOURS_PER_DAY;
	}

	/**
	 * @param nrChannels
	 * @return
	 */
	private static int nrBytesPerIntegrationPeriod(int nrChannels) {
		return STATUS_BYTE_SIZE + (CHANNEL_BYTE_SIZE * nrChannels);
	}

	/**
	 * @param nrChannels
	 * @param intervalLength
	 * @return
	 */
	public static long dayByteSizeMin(int nrChannels, int intervalLength) {
		return (minIntervalsPerDay(intervalLength) * nrBytesPerIntegrationPeriod(nrChannels)) + E4_BYTE_SIZE + DATE_BYTE_SIZE;
	}

	/**
	 * @param nrChannels
	 * @param intervalLength
	 * @return
	 */
	public static long dayByteSizeMax(int nrChannels, int intervalLength) {
		return (maxIntervalsPerDay(intervalLength) * nrBytesPerIntegrationPeriod(nrChannels)) + E4_BYTE_SIZE + DATE_BYTE_SIZE;
	}

	/**
	 * Calculate the number of bytes to read from the profile, given a timeframe
	 * (Start and end date), the interval length and the number of channels.
	 *
	 * @param start The startdate
	 * @param end The enddate
	 * @param intervalLength The length of the interval in seconds
	 * @param nrChannels The number of channels in the profile
	 * @return A long value containing the number of bytes to read from the profile
	 */
	private static long nrBytesToRetrieve(Date start, Date end, int intervalLength, int nrChannels) {
		long numberOfDaysInPeriod = nrDaysInPeriodIEC(start, end);
		return dayByteSizeMax(nrChannels, intervalLength) * numberOfDaysInPeriod;
	}

	/**
	 * Getter for the includeEvents property.
	 *
	 * @return true if events should be generated
	 */
	public boolean isIncludeEvents() {
		return includeEvents;
	}

}
