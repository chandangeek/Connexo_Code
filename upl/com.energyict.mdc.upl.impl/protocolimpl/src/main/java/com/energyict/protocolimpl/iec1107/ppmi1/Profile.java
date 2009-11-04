package com.energyict.protocolimpl.iec1107.ppmi1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.iec1107.ppmi1.opus.OpusProfileParser;
import com.energyict.protocolimpl.iec1107.ppmi1.parser.ProfileParser;
import com.energyict.protocolimpl.iec1107.ppmi1.parser.ProfileReverseParser;

/**
 * <strong>Profile responsibilties: </strong>
 * <ol>
 * <li>Control/Manage reading meter profile data</li>
 * </ol>
 *
 * @author fbo
 */
public class Profile {

	public static final boolean	READFROMFILE		= true;
	public static final String	FILENAME			= "1257338670532_PR";

	private static final int	HOURS_PER_DAY		= 24;
	private static final int	MINUTES_PER_HOUR	= 60;
	private static final int	SECONDS_PER_MINUTE	= 60;
	private static final int	MS_PER_SECOND		= 1000;

	private static final int	CHANNEL_BYTE_SIZE	= 5;
	private static final int	STATUS_BYTE_SIZE	= 1;
	private static final int	E4_BYTE_SIZE		= 2;
	private static final int	DATE_BYTE_SIZE		= 4;

	private PPM ppm = null;
	private RegisterFactory rFactory = null;
	private Logger log = null;

	private Date beginDate = null;
	private Date endDate = null;
	private Date meterDate = null;

	/**
	 * Creates a new instance of Profile
	 */
	public Profile(PPM ppm, RegisterFactory rf) throws IOException, UnsupportedException {
		this.ppm = ppm;
		rFactory = rf;
		if (ppm != null) {
			log = ppm.getLogger();
		}
	}

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

		String logString = "getProfileData() beginDate=" + beginDate;
		logString += " endDate=" + endDate;

		log.log(Level.INFO, logString);

		if (ppm.isOpus()) {
			return doOpusProtocol();
		} else {
			return doIECProtocol();
		}
	}

	/**
	 * Get the {@link ProfileData} using the OPUS protocol version
	 * @return The {@link ProfileData} from the meter
	 * @throws NestedIOException
	 * @throws ConnectionException
	 * @throws IOException
	 */
	private ProfileData doOpusProtocol() throws NestedIOException, ConnectionException, IOException {
		OpusProfileParser opp = new OpusProfileParser(ppm, rFactory, getMeterDate());

		long bDate = daySince1970(ppm.getTimeZone(), beginDate);
		long eDate = daySince1970(ppm.getTimeZone(), endDate);
		long mDate = daySince1970(ppm.getTimeZone(), getMeterDate());

		Calendar current = Calendar.getInstance( ppm.getTimeZone() );
		current.setTime( beginDate );

		String log = "Retrieve profile begin:" + beginDate + " end: " + endDate;
		ppm.getLogger().fine( log );

		for (; (bDate <= mDate) && (bDate <= eDate); bDate++) {

			int nrHours = PPMUtils.hoursInDay( current );

			log = "Retieve profile for " + current.getTime() + " nrHours= " + nrHours;
			ppm.getLogger().fine(log);

			/*
			 * The nr of packets to retrieve is calculated:
			 * NR PACKETS = NR CHANNELS * NR INTERVALS per day
			 * NR INTERVALS per day = LENGTH OF DAY (in sec) / PROFILE INTERVAL
			 * In 1 packet there is room for 8 values, so devide by 8
			 */
			int nrSeconds = nrHours * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;
			int nrPackets = (ppm.getNumberOfChannels() * (nrSeconds / ppm.getProfileInterval())) / 8;

			long opusDayNr = mDate - bDate + 10;
			boolean doAdd = true;

			if( opusDayNr == 10 ) {
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

		return opp.getProfileData();
	}

	/**
	 * Get the {@link ProfileData} using the IEC1107 protocol version
	 * @return The {@link ProfileData} from the meter
	 * @throws IOException
	 */
	private ProfileData doIECProtocol() throws IOException {
		boolean readFromFile = READFROMFILE;
		byte[] data;
		String fileName = FILENAME;
		String pathName = "C:\\EnergyICT\\WorkingDir\\ppm_profiles\\";
		String extName = ".hex";

		int intPeriodInSec = rFactory.getIntegrationPeriod().intValue() * SECONDS_PER_MINUTE;

		long byteSize = nrBytesToRetrieve(beginDate, endDate, intPeriodInSec, ppm.getNumberOfChannels());
		log.log(Level.INFO, "IEC protocol, byteSize= " + byteSize);

		if (readFromFile) {
			data = PPMUtils.fromFile(pathName + fileName + extName);
		} else {
			data = rFactory.getRegisterRawData(OpticalRegisterFactory.R_LOAD_PROFILE, (int) byteSize);
			PPMUtils.toFile(data, pathName + System.currentTimeMillis()+ "_PR" + extName);
		}

		Date date = rFactory.getTimeDate();
		int nrChannels = rFactory.getLoadProfileDefinition().getNrOfChannels();
		int intervalLength = rFactory.getIntegrationPeriod().intValue();

		ProfileReverseParser prp = new ProfileReverseParser(date, nrChannels, intervalLength * SECONDS_PER_MINUTE, ppm.getTimeZone());
		prp.setInput(data);

		ProfileParser pp = new ProfileParser(ppm, rFactory, rFactory.getTimeDate(), rFactory.getLoadProfileDefinition(), false);
		pp.setInput(new ByteArrayInputStream(prp.match()));

		return pp.getProfileData();
	}

	/**
	 * ( nr sec day / int. duration sec ) + 1 +1 for extra iperiod
	 */
	private static int maxIntervalsPerDay(int interval) {
		return ((HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE) / interval) + 1;
	}

	private static int minIntervalsPerDay(int interval) {
		return ((HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE) / interval);
	}

	/**
	 * Calculate difference and add 1 ( there is always at least 1 day ) Also
	 * only a complete day can be parsed. Take start DAY and end DAY, and
	 * calculate difference.
	 */
	private static long nrDaysInPeriodIEC(Date start, Date end) {
		// day index since start of count
		long sd = start.getTime() / MS_PER_SECOND / SECONDS_PER_MINUTE / MINUTES_PER_HOUR / HOURS_PER_DAY;
		long ed = end.getTime() / MS_PER_SECOND / SECONDS_PER_MINUTE / MINUTES_PER_HOUR / HOURS_PER_DAY;
		return ed - sd + 1;
	}

	/**
	 * Calculate difference and subtract 1; today is retrieved separately.
	 */
	private static long nrDaysInPeriodOPUS(Date start, Date end) {
		// day index since start of count
		long sd = start.getTime() / MS_PER_SECOND / SECONDS_PER_MINUTE / MINUTES_PER_HOUR / HOURS_PER_DAY;
		long ed = end.getTime() / MS_PER_SECOND / SECONDS_PER_MINUTE / MINUTES_PER_HOUR / HOURS_PER_DAY;
		return ed - sd;
	}

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

	private static int nrBytesPerIntegrationPeriod(int nrChannels) {
		return STATUS_BYTE_SIZE + (CHANNEL_BYTE_SIZE * nrChannels);
	}

	public static long dayByteSizeMin(int nrChannels, int intervalLength) {
		long nbip = nrBytesPerIntegrationPeriod(nrChannels);
		return (minIntervalsPerDay(intervalLength) * nbip) + E4_BYTE_SIZE + DATE_BYTE_SIZE;
	}

	public static long dayByteSizeMax(int nrChannels, int intervalLength) {
		long nbip = nrBytesPerIntegrationPeriod(nrChannels);
		return (maxIntervalsPerDay(intervalLength) * nbip) + E4_BYTE_SIZE + DATE_BYTE_SIZE;
	}

	private static long nrBytesToRetrieve(Date start, Date end, int intervalLength, int nrChannels) {
		long nd = nrDaysInPeriodIEC(start, end);
		return dayByteSizeMax(nrChannels, intervalLength) * nd;
	}



}
