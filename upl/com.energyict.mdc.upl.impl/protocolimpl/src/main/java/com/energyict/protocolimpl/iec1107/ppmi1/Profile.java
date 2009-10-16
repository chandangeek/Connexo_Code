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

	/** reference to the MeterProtocol */
	PPM ppm = null;
	/** registerfactory is used for reading 550 register */
	RegisterFactory rFactory = null;

	Date beginDate = null;
	Date endDate = null;
	Date meterDate = null;

	Logger log = null;

	/**
	 * Creates a new instance of Profile
	 */
	public Profile(PPM ppm, RegisterFactory rf) throws IOException,
	UnsupportedException {
		this.ppm = ppm;
		rFactory = rf;
		if (ppm != null) {
			log = ppm.getLogger();
		}
	}

	Date getMeterDate() throws IOException {
		if (meterDate == null) {
			this.meterDate = rFactory.getTimeDate();
		}
		return meterDate;
	}

	/**
	 * given a begin- and enddate will return a Profile object with all the
	 * meter readings and events.
	 *
	 * @param beginDate
	 *            for retrieving meterreadings
	 * @param endDate
	 *            for retrieving meterreadings
	 */
	ProfileData getProfileData(Date beginDate, Date endDate,
			boolean includeEvents) throws IOException {

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

	/* The nr of packets to retrieve is calculated:
	 *
	 * NR PACKETS = NR CHANNELS * NR INTERVALS per day
	 * NR INTERVALS per day = LENGTH OF DAY (in sec) / PROFILE INTERVAL
	 *
	 *
	 * In 1 packet there is room for 8 values, so devide by 8
	 */
	private ProfileData doOpusProtocol() throws NestedIOException,
	ConnectionException, IOException {

		OpusProfileParser opp = new OpusProfileParser(ppm, rFactory, getMeterDate());

		long bDate = daySince1970(ppm.getTimeZone(), beginDate);
		long eDate = daySince1970(ppm.getTimeZone(), endDate);
		long mDate = daySince1970(ppm.getTimeZone(), getMeterDate());

		Calendar current = Calendar.getInstance( ppm.getTimeZone() );
		current.setTime( beginDate );

		String log = "Retrieve profile begin:" + beginDate + " end: " + endDate;
		ppm.getLogger().fine( log );

		for (; (bDate <= mDate ) && (bDate <= eDate ); bDate++) {

			int nrHours = PPMUtils.hoursInDay( current );

			log =   "Retieve profile for " + current.getTime() +
			" nrHours= " + nrHours;
			ppm.getLogger().fine( log );

			int nrSeconds = nrHours * 60 * 60;
			int nrPackets = ( ppm.getNumberOfChannels() *
					( nrSeconds / ppm.getProfileInterval() ) ) / 8;

			long opusDayNr = mDate - bDate + 10;
			boolean doAdd = true;

			if( opusDayNr == 10 ) {
				Calendar mCalendar = ProtocolUtils.getCalendar( ppm.getTimeZone() );
				mCalendar.setTime(meterDate);
				int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
				if( hour <= 0 ) {
					long iPeriod = rFactory.getIntegrationPeriod().intValue();
					long minutes = mCalendar.get(Calendar.MINUTE);
					if( minutes < (iPeriod+(PPM.MINIMUM_INTERVAL_AGE/1000/60) ) ) {
						doAdd = false;
					}
				}
			}

			if(doAdd) {
				opp.add(ppm.opusConnection.readRegister(  "550", 0,  (int) opusDayNr,
						nrPackets, true));
			}

			current.add( Calendar.DAY_OF_YEAR, 1 );

		}

		return opp.getProfileData();
	}

	private ProfileData doIECProtocol() throws IOException {

		int intPeriodInSec = rFactory.getIntegrationPeriod().intValue() * 60;

		long byteSize = nrBytesToRetrieve(beginDate, endDate, intPeriodInSec,
				ppm.getNumberOfChannels());

		log.log(Level.INFO, "IEC protocol, byteSize= " + byteSize);

		byte[] data = rFactory.getRegisterRawData(
				OpticalRegisterFactory.R_LOAD_PROFILE, (int) byteSize);

		/* debugging purposes */
		//		FileOutputStream fos = new FileOutputStream("c:\\20040823AA"
		//				+ System.currentTimeMillis());
		//		fos.write(data);
		//		fos.close();
		Date date = rFactory.getTimeDate();
		int nrChannels = rFactory.getLoadProfileDefinition().getNrOfChannels();
		int intervalLength = rFactory.getIntegrationPeriod().intValue();

		ProfileReverseParser prp = new ProfileReverseParser(date, nrChannels,
				intervalLength * 60, ppm.getTimeZone() );
		prp.setInput(data);

		ProfileParser pp = new ProfileParser( ppm, rFactory,
				rFactory.getTimeDate(),
				rFactory.getLoadProfileDefinition(), false);
		pp.setInput(new ByteArrayInputStream(prp.match()));

		return pp.getProfileData();
	}

	/** channel value size (is a constant) */
	private static final int CHANNEL_BYTE_SIZE = 6;

	/** status size (is a constant) */
	private static final int STATUS_BYTE_SIZE = 2;
	private static final int E4_BYTE_SIZE = 2;
	private static final int DATE_BYTE_SIZE = 4;

	/**
	 * ( nr sec day / int. duration sec ) + 1 +1 for extra iperiod
	 */
	static int maxIntervalsPerDay(int interval) {
		return ((24 * 60 * 60) / interval) + 1;
	}

	static int minIntervalsPerDay(int interval) {
		return ((24 * 60 * 60) / interval);
	}

	/**
	 * Calculate difference and add 1 ( there is always at least 1 day ) Also
	 * only a complete day can be parsed. Take start DAY and end DAY, and
	 * calculate difference.
	 */
	static long nrDaysInPeriodIEC(Date start, Date end) {
		// day index since start of count
		long sd = start.getTime() / 1000 / 60 / 60 / 24;
		long ed = end.getTime() / 1000 / 60 / 60 / 24;

		return ed - sd + 1;
	}

	/**
	 * Calculate difference and subtract 1; today is retrieved separately.
	 */
	static long nrDaysInPeriodOPUS(Date start, Date end) {
		// day index since start of count
		long sd = start.getTime() / 1000 / 60 / 60 / 24;
		long ed = end.getTime() / 1000 / 60 / 60 / 24;

		return ed - sd;
	}

	static long daySince1970(TimeZone timeZone, Date date) {

		Calendar inputCalendar = ProtocolUtils.getCalendar( timeZone );
		inputCalendar.setTime(date);

		Calendar calendar = ProtocolUtils.getCalendar( timeZone );
		calendar.clear();

		int day = inputCalendar.get(Calendar.DAY_OF_MONTH);
		int month = inputCalendar.get(Calendar.MONTH);
		int year = inputCalendar.get(Calendar.YEAR);

		calendar.set(year, month, day);

		return calendar.getTimeInMillis() / 1000 / 60 / 60 / 24;

	}

	static int nrBytesPerIntegrationPeriod(int nrChannels) {
		return STATUS_BYTE_SIZE + (CHANNEL_BYTE_SIZE * nrChannels);
	}

	public static long dayByteSizeMin(int nrChannels, int intervalLength) {
		long nbip = nrBytesPerIntegrationPeriod(nrChannels);
		long dayByteSize = (minIntervalsPerDay(intervalLength) * nbip)
		+ E4_BYTE_SIZE + DATE_BYTE_SIZE;
		return dayByteSize;
	}

	public static long dayByteSizeMax(int nrChannels, int intervalLength) {
		long nbip = nrBytesPerIntegrationPeriod(nrChannels);
		long dayByteSize = (maxIntervalsPerDay(intervalLength) * nbip)
		+ E4_BYTE_SIZE + DATE_BYTE_SIZE;
		return dayByteSize;
	}

	static long nrBytesToRetrieve(Date start, Date end, int intervalLength,
			int nrChannels) {

		long nd = nrDaysInPeriodIEC(start, end);
		long total = dayByteSizeMax(nrChannels, intervalLength) * nd;

		//System.out.print("\nStartdate = [" + start + "] ");
		//System.out.println(" enddate = [" + end + " ] ");

		//System.out.print(" intervallength= [" + intervalLength + " sec]");
		//System.out.print(" nr channels= [" + nrChannels + "] ");

		//System.out.print(" nr Days in period =[" + nd + "]");
		//System.out.print(" number bytes to retrieve =[" + total + "]");

		return total;

	}



}
