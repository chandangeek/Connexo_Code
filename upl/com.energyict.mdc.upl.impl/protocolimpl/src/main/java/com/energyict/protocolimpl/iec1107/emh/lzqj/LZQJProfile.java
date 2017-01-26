/*
 * A1500Profile.java
 *
 * Created on 23 december 2004, 16:23
 */

package com.energyict.protocolimpl.iec1107.emh.lzqj;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author Koen Changes: KV 20012005 Initial version
 */
public class LZQJProfile extends VDEWProfile {

	/**
	 * Creates a new instance of LZQJProfile
	 */
	public LZQJProfile(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink,
			AbstractVDEWRegistry abstractVDEWRegistry) {
		super(meterExceptionInfo, protocolLink, abstractVDEWRegistry, false);
	}

    /**
     * Check if the device is using longName ObisCodes. The device uses longName obisCodes when there is more then 1 pulse channel defined.
     * The header of the Profile will then contain something like <i>P.02(040211001500)(00000000)(15)(2)(1-1:1.5)(kW)(1-2:3.5)(kvar)</i>,
     * where the 1-<b>1</b>: indicates the channel
     *
     * @param profileInterval the profileInterval.
     * @return true if we find a colon in the header, false in all other cases.
     */
    public boolean checkForLongObisCodes(int profileInterval) {
        try {
            Calendar cal1 = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
            cal1.setTime(new Date());
            cal1.add(Calendar.SECOND, -profileInterval);
            Calendar cal2 = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
            cal2.setTime(new Date());
            byte[] response = readRawData(cal1, cal2, 1);
            String profileHeader = new String(response);
            return profileHeader.indexOf(":") > 0;
        } catch (IOException e) {
            return false;
        }
    }

	/**
	 * Fetch the {@link ProfileData} from a certain date in the past. Include the events if necessary.
	 * 
	 * @param lastReading
	 *            - the date in the past
	 * @param includeEvents
	 *            - boolean to indicate whether the events should be read
	 * @return a filled up ProfileData object
	 * @throws IOException
	 *             when something goes wrong during fetching
	 */
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
		fromCalendar.setTime(lastReading);

		ProfileData profileData = doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(getProtocolLink()
				.getTimeZone()), 1);
		if (includeEvents) {
			List meterEvents = doGetLogBook(fromCalendar, ProtocolUtils.getCalendar(getProtocolLink().getTimeZone()));
			profileData.getMeterEvents().addAll(meterEvents);
			profileData.sort();
		}

		profileData.applyEvents(getProtocolLink().getProfileInterval() / 60);
		return profileData;
	}

	/**
	 * Fetch the {@link ProfileData} from a certain date in the past to a certain date. Include the events if necessary.
	 * 
	 * @param fromReading
	 *            - the date in the past
	 * @param toReading
	 *            - the end date to read
	 * @param includeEvents
	 *            - boolean to indicate whether the events should be read
	 * @return a filled up ProfileData object
	 * @throws IOException
	 *             when something goes wrong during fetching
	 */
	public ProfileData getProfileData(Date fromReading, Date toReading, boolean includeEvents) throws IOException {
		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
		fromCalendar.setTime(fromReading);
		Calendar toCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
		toCalendar.setTime(toReading);

		ProfileData profileData = doGetProfileData(fromCalendar, toCalendar, 1);
		if (includeEvents) {
			List meterEvents = doGetLogBook(fromCalendar, toCalendar);
			profileData.getMeterEvents().addAll(meterEvents);
			profileData.sort();
		}

		profileData.applyEvents(getProtocolLink().getProfileInterval() / 60);
		return profileData;
	}

	/**
	 * {@inheritDoc}
	 */
	protected byte parseIntervalStatus(byte[] ba, int startIdx) throws IOException {
		return (byte) Integer.parseInt(parseFindString(ba, startIdx), 16);

	}

	/**
	 * Parse the string from a given byteArray with a given offset
	 * 
	 * @param data
	 *            - the byteArray to fetch out the string
	 * @param iOffset
	 *            - the offset to start from
	 * @return a String representing a value between the brackets
	 */
	private String parseFindString(byte[] data, int iOffset) {
		int start = 0, stop = 0, i = 0;
		if (iOffset >= data.length) {
			return null;
		}
		for (i = 0; i < data.length; i++) {
			if (data[i + iOffset] == '(') {
				start = i;
			}
			if ((data[i + iOffset] == ')') || (data[i + iOffset] == '*')) {
				stop = i;
				break;
			}
		}
		byte[] strparse = new byte[stop - start - 1];
		for (i = 0; i < (stop - start - 1); i++) {
			strparse[i] = data[i + start + 1 + iOffset];
		}
		return new String(strparse);
	}

	/**
	 * {@inheritDoc} <br>
	 * Because there is a lack of inheritDoc -> Build up the {@link ProfileData} object. Use the ByteArray as an input
	 * for the intervalValues. Each value in the array can have a timeStamp and/or a status indication
	 */
	protected ProfileData buildProfileData(byte[] responseData) throws IOException {
		ProfileData profileData;
		Calendar calendar = null;
		byte bStatus = 0;
		byte bNROfValues = 0;
		int profileInterval = 0;
		DataParser dp = new DataParser(getProtocolLink().getTimeZone());
		Unit[] units = null;
		boolean buildChannelInfos = false;
		int t;
		int eiCode = 0;
		IntervalData intervalDataSave = null;
		boolean partialInterval = false;
		String[] edisCodes = null;

		// We suppose that the profile contains nr of channels!!
		try {
			LZQJTimeStamp vts = new LZQJTimeStamp(getProtocolLink().getTimeZone());
			profileData = new ProfileData();

			int i = 0;
			while (true) {

				if (responseData[i] == 'P') {
					i += 4; // skip P.01
					i = gotoNextOpenBracket(responseData, i);

					if (dp.parseBetweenBrackets(responseData, i).compareTo("ERROR") == 0) {
						throw new IOException("No entries in object list.");
					}

					vts.parse(dp.parseBetweenBrackets(responseData, i));
					calendar = vts.getCalendar();

					i = gotoNextOpenBracket(responseData, i + 1);
					bStatus = parseIntervalStatus(responseData, i);

					eiCode = 0;
					for (t = 0; t < 8; t++) {
						if ((bStatus & (byte) (0x01 << t)) != 0) {
							eiCode |= mapStatus2IntervalStateBits(bStatus & (byte) (0x01 << t) & 0xFF);
						}
					}

					if (bStatus != 0) {
						System.out.println("Status : " + bStatus + "\r\nTime : " + calendar.getTime()
								+ "\r\n CalendarMode : " + vts.getMode());
					}

					// KV 02112005
					if (((bStatus & SEASONAL_SWITCHOVER) == SEASONAL_SWITCHOVER)
							&& (vts.getMode() == LZQJTimeStamp.MODE_SUMMERTIME)
							&& (!getProtocolLink().getTimeZone().inDaylightTime(calendar.getTime()))
							&& ((bStatus & DEVICE_CLOCK_SET_INCORRECT) == DEVICE_CLOCK_SET_INCORRECT)) {
						calendar.add(Calendar.MILLISECOND, -1 * getProtocolLink().getTimeZone().getDSTSavings());
					}

					if (!ParseUtils.isOnIntervalBoundary(calendar, getProtocolLink().getProfileInterval())) {
						partialInterval = true;
						// roundup to the first interval boundary
						ParseUtils.roundUp2nearestInterval(calendar, getProtocolLink().getProfileInterval());
					} else {
						partialInterval = false;
					}

					i = gotoNextOpenBracket(responseData, i + 1);
					profileInterval = Integer.parseInt(dp.parseBetweenBrackets(responseData, i));
					if ((profileInterval * 60) != getProtocolLink().getProfileInterval()) {
						throw new IOException("buildProfileData() error, mismatch between configured profileinterval ("
								+ getProtocolLink().getProfileInterval() + ") and meter profileinterval ("
								+ (profileInterval * 60) + ")!");
					}

					i = gotoNextOpenBracket(responseData, i + 1);
					// KV 06092005 K&P
					// bNROfValues = ProtocolUtils.bcd2nibble(responseData,i+1);
					bNROfValues = (byte) Integer.parseInt(dp.parseBetweenBrackets(responseData, i));

					units = new Unit[bNROfValues];
					if (bNROfValues > getProtocolLink().getNumberOfChannels()) {
						throw new IOException("buildProfileData() error, mismatch between configured nrOfChannels ("
								+ getProtocolLink().getNumberOfChannels() + ") and meter profile nrOfChannels ("
								+ bNROfValues + ")!");
					}

					// get the units
					edisCodes = new String[bNROfValues];
					for (t = 0; t < bNROfValues; t++) {// skip all obis codes
						i = gotoNextOpenBracket(responseData, i + 1);
						edisCodes[t] = dp.parseBetweenBrackets(responseData, i);
						i = gotoNextOpenBracket(responseData, i + 1);
						units[t] = Unit.get(dp.parseBetweenBrackets(responseData, i));
					}

					// KV 06092005 K&P changes
					if (!buildChannelInfos) {
						int id = 0;
						for (t = 0; t < bNROfValues; t++) {
							ChannelInfo chi = null;
							if (getProtocolLink().getProtocolChannelMap().isMappedChannels()) {
								int fysical0BasedChannelId = getFysical0BasedChannelId(edisCodes[t]);
								if (getProtocolLink().getProtocolChannelMap()
										.getProtocolChannel(fysical0BasedChannelId).getIntValue(0) != -1) {
									chi = new ChannelInfo(id, getProtocolLink().getProtocolChannelMap()
											.getProtocolChannel(fysical0BasedChannelId).getIntValue(0),
											"channel_" + id, units[t]);
									id++;
								}
								if (!getProtocolLink().isRequestHeader()) {
									if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(
											fysical0BasedChannelId).isCumul()) {
										chi.setCumulativeWrapValue(getProtocolLink().getProtocolChannelMap()
												.getProtocolChannel(fysical0BasedChannelId).getWrapAroundValue());
									}
								}
							} else {
								chi = new ChannelInfo(t, "channel_" + t, units[t]);
								if (!getProtocolLink().isRequestHeader()) {
									if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(t).isCumul()) {
										chi.setCumulativeWrapValue(getProtocolLink().getProtocolChannelMap()
												.getProtocolChannel(t).getWrapAroundValue());
									}
								}
							}

							// KV 06092005 K&P changes
							if (chi != null) {
								profileData.addChannel(chi);
							}
						}
						buildChannelInfos = true;
					}

					i = gotoNextCR(responseData, i + 1);
				} else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
					i += 1; // skip
				} else {
					// Fill profileData
					IntervalData intervalData = new IntervalData(new Date(calendar.getTime().getTime()), eiCode,
							bStatus);

					if (!keepStatus) {
						eiCode = 0;
					}

					for (t = 0; t < bNROfValues; t++) { // skip all obis codes
						i = gotoNextOpenBracket(responseData, i);
						BigDecimal bd = new BigDecimal(dp.parseBetweenBrackets(responseData, i));
						// long lVal = bd.longValue();
						// KV 06092005 K&P changes
						if (getProtocolLink().getProtocolChannelMap().isMappedChannels()) {
							int fysical0BasedChannelId = getFysical0BasedChannelId(edisCodes[t]);
							if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId)
									.getIntValue(0) != -1) {
								intervalData.addValue(bd); // new Long(lVal));
							}
						} else {
							intervalData.addValue(bd); // new Long(lVal));
						}

						i++;
					}

					if (partialInterval) {

						if (intervalDataSave != null) {
							if (intervalData.getEndTime().getTime() == intervalDataSave.getEndTime().getTime()) {
								if (DEBUG >= 1) {
									System.out
											.println("KV_DEBUG> partialInterval, add partialInterval to currentInterval");
								}
								intervalData = addIntervalData(profileData, intervalDataSave, intervalData); // add
								// intervals together to avoid double interval
								// values ...
							} else {
								if (DEBUG >= 1) {
									System.out
											.println("KV_DEBUG> partialInterval, save partialInterval to profiledata and assign currentInterval to partialInterval");
								}
								profileData.addInterval(intervalDataSave); // save
								// the partiel interval. Timestamp has been
								// adjusted to the next intervalboundary
								intervalDataSave = intervalData;
							}
						} else {
							if (DEBUG >= 1) {
								System.out
										.println("KV_DEBUG> partialInterval, assign currentInterval to partialInterval");
							}
							intervalDataSave = intervalData;
						}
					} else {
						// If there was a patrialinterval within interval x and
						// the next interval has the same timestamp as interval
						// x,
						// then we must add them together!
						// If the next interval's timestamps != timestamp of
						// interval x, save the partial interval as separate
						// entry for interval x.
						if (intervalDataSave != null) {
							if (intervalData.getEndTime().getTime() == intervalDataSave.getEndTime().getTime()) {
								if (DEBUG >= 1) {
									System.out.println("KV_DEBUG> add partialInterval to currentInterval");
								}
								intervalData = addIntervalData(profileData, intervalDataSave, intervalData); // add
								// intervals together to avoid double interval
								// values ...
							} else {
								if (DEBUG >= 1) {
									System.out.println("KV_DEBUG> save partialInterval to profiledata");
								}
								profileData.addInterval(intervalDataSave); // save
								// the partiel interval. Timestamp has been
								// adjusted to the next intervalboundary
							}
							intervalDataSave = null;
						}

						if (DEBUG >= 1) {
							System.out.println("KV_DEBUG> save currentInterval to profiledata");
						}
						// save the current interval
						profileData.addInterval(intervalData);
					}

					calendar.add(Calendar.MINUTE, profileInterval);

					i = gotoNextCR(responseData, i + 1);

				}

				if (i >= responseData.length) {
					break;
				}

			} // while(true)
		} catch (IOException e) {
			throw new IOException("buildProfileData> " + e.getMessage());
		}

		return profileData;
	}
} // LZQJProfile

