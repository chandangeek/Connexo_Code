/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import com.energyict.mdc.protocol.api.device.data.IntervalData;

import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

import java.io.IOException;

/**
 * @author fbo
 *
 */
class DayAssembler implements Assembler {

	private int dayNr = 0;
	private ProfileParser profileParser;

	/**
	 * @param profileParser
	 */
	public DayAssembler(ProfileParser profileParser) {
		this.profileParser = profileParser;
	}

	public void workOn(Assembly ta) throws IOException {

		if( ta.getTarget() != null ) {
			createProfileData( (Day) ta.getTarget() );
		}

		((Byte) ta.pop()).byteValue();
		byte[] date = new byte[2];
		getProfileParser().getAssembly().read(date, 0, 2);
		Day day = new Day((int) PPMUtils.hex2dec(date[0]), (int) PPMUtils.hex2dec(date[1]), getProfileParser());

		ta.setTarget(day);
		this.dayNr++;
	}

	/**
	 * Create the profile data after a complete day has been parsed. The time is
	 * actually not that important, but it's just a good time.
	 *
	 * @param aDay
	 */
	public void createProfileData(Day aDay) {

		if (aDay.isEmpty()) {
			return;
		}

		for (int hi = 0; hi < aDay.getReading().length; hi++) {

			if (!aDay.getReading()[hi].isEmpty()) {
				IntervalData i = new IntervalData(aDay.getReading()[hi].getDate());

				if (aDay.getStatus()[hi] != null) {
					i.setEiStatus(aDay.getStatus()[hi].getEIStatus());
				}

				for (int vi = 0; vi < aDay.getReading()[hi].getValue().length; vi++) {
					i.addValue(aDay.getReading()[hi].getValue(vi));
				}

				getProfileParser().getTargetProfileData().addInterval(i);
			}

		}
	}

	/**
	 * @return
	 */
	public ProfileParser getProfileParser() {
		return profileParser;
	}

}