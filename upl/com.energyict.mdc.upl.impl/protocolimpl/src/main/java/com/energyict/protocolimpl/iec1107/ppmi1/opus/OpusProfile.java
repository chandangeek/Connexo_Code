package com.energyict.protocolimpl.iec1107.ppmi1.opus;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.RegisterFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class OpusProfile {

	private static final String	PROFILE_ID	= "550";
	/** Some time statics */
	private static final int		HOURS_PER_DAY			= 24;
	private static final int		MINUTES_PER_HOUR		= 60;
	private static final int		SECONDS_PER_MINUTE		= 60;
	private static final int		MS_PER_SECOND			= 1000;

	private static final int		OPUSVALUES_PER_PACKET	= 8;
	private static final long		OPUS_SPECIALDAY_NR		= 10;

	private final PPM ppm;
	private final RegisterFactory rFactory;

	public OpusProfile(PPM ppm, RegisterFactory rFactory) {
		this.ppm = ppm;
		this.rFactory = rFactory;
	}

	public ProfileData getProfileData(Date beginDate, Date endDate, boolean includeEvents, Date meterDate) throws IOException {
		OpusProfileParser opp = new OpusProfileParser(ppm, rFactory, meterDate);

		long bDate = daySince1970(ppm.getTimeZone(), beginDate);
		long eDate = daySince1970(ppm.getTimeZone(), endDate);
		long mDate = daySince1970(ppm.getTimeZone(), meterDate);

		Calendar current = Calendar.getInstance( ppm.getTimeZone() );
		current.setTime( beginDate );

		String logMessage = "Retrieve profile begin:" + beginDate + " end: " + endDate;
		ppm.getLogger().fine( logMessage );

		for (; (bDate <= mDate) && (bDate <= eDate); bDate++) {

			int nrHours = PPMUtils.hoursInDay( current );

			logMessage = "Retieve profile for " + current.getTime() + " nrHours= " + nrHours;
			ppm.getLogger().fine(logMessage);

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
				opp.add(ppm.getOpusConnection().readRegister(PROFILE_ID, 0, (int) opusDayNr, nrPackets, true));
			}

			current.add( Calendar.DAY_OF_YEAR, 1 );

		}

		ProfileData pd = opp.getProfileData();
		if (includeEvents) {
			pd.generateEvents();
		}

		return pd;

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


}
