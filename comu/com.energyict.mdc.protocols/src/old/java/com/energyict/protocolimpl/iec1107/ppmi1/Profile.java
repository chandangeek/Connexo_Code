/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.iec1107.ppmi1.opus.OpusProfile;
import com.energyict.protocolimpl.iec1107.ppmi1.parser.ProfileParser;
import com.energyict.protocolimpl.iec1107.ppmi1.parser.ProfileReverseParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <strong>Profile responsibilities: </strong>
 * <ol>
 * <li>Control/Manage reading meter profile data</li>
 * </ol>
 *
 * @author fbo
 */
public class Profile {

	/** The following statics are only used for debugging purposes */
	private static final boolean	READFROMFILE		= false;
	private static final boolean	WRITETOFILE			= false;
	private static final String		FILENAME			= "IMSERV_DEBUG_PPM_PROFILE_30-12-2009";
	private static final String		PATHNAME			= "C:\\EnergyICT\\WorkingDir\\ppm_profiles\\";
	private static final String		EXTENSION			= ".hex";

	/** Some time statics */
	private static final int		HOURS_PER_DAY			= 24;
	private static final int		MINUTES_PER_HOUR		= 60;
	private static final int		SECONDS_PER_MINUTE		= 60;
	private static final int		MS_PER_SECOND			= 1000;

	/** Some profile statics */
	private static final int		CHANNEL_BYTE_SIZE		= 6;
	private static final int		E4_BYTE_SIZE			= 2;
	private static final int		DATE_BYTE_SIZE			= 4;

	private PPM ppm = null;
	private RegisterFactory rFactory = null;
	private Logger log = null;

	private Date beginDate = null;
	private Date endDate = null;
	private Date meterDate = null;

	private boolean includeEvents = false;

	/**
	 * Creates a new instance of Profile
	 */
	public Profile(PPM ppm, RegisterFactory rf) {
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
	public Date getMeterDate() throws IOException {
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
	 * @param includeEvents
	 * @return the {@link ProfileData} from the device for the given period
	 * @throws IOException
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
	 * @throws IOException
	 */
	private ProfileData doOpusProtocol() throws IOException {
		OpusProfile op = new OpusProfile(ppm, rFactory);
		return op.getProfileData(beginDate, endDate, isIncludeEvents(), getMeterDate());
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
			data = rFactory.getRegisterRawData(OpticalRegisterFactory.R_LOAD_PROFILE, (int) byteSize);
			if (WRITETOFILE) {
				PPMUtils.toFile(data, PATHNAME + System.currentTimeMillis()+ "_PR" + EXTENSION);
			}
		}

		int nrChannels = rFactory.getLoadProfileDefinition().getNrOfChannels();
		int intervalLength = rFactory.getIntegrationPeriod().intValue();

		ProfileReverseParser prp = new ProfileReverseParser(getMeterDate(), nrChannels, intervalLength * SECONDS_PER_MINUTE, ppm.getTimeZone());
		prp.setInput(data);

		ProfileParser pp = new ProfileParser(ppm, rFactory, getMeterDate(), rFactory.getLoadProfileDefinition());
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
		return ((HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE) / interval) + 1;
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
	 *
	 * @param start The startDate
	 * @param end The endDate
	 * @return the number of days in the given timeframe
	 */
	private static long nrDaysInPeriodIEC(Date start, Date end) {
		long diff = end.getTime() - start.getTime();
        long numberOfDays = (diff / (MS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY)) + 1;
        return numberOfDays;
	}

	/**
	 * @param nrChannels
	 * @return
	 */
	private static int nrBytesPerIntegrationPeriod(int nrChannels) {
		return CHANNEL_BYTE_SIZE * nrChannels;
	}

	/**
	 * @param nrChannels
	 * @param intervalLength
	 * @return
	 */
	public static long dayByteSizeMin(int nrChannels, int intervalLength) {
		long numberOfBytesInPeriod = nrBytesPerIntegrationPeriod(nrChannels);
		return (minIntervalsPerDay(intervalLength) * numberOfBytesInPeriod) + E4_BYTE_SIZE + DATE_BYTE_SIZE;
	}

	/**
	 * @param nrChannels
	 * @param intervalLength
	 * @return
	 */
	public static long dayByteSizeMax(int nrChannels, int intervalLength) {
		long numberOfBytesInPeriod = nrBytesPerIntegrationPeriod(nrChannels);
		return (maxIntervalsPerDay(intervalLength) * numberOfBytesInPeriod) + E4_BYTE_SIZE + DATE_BYTE_SIZE;
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
