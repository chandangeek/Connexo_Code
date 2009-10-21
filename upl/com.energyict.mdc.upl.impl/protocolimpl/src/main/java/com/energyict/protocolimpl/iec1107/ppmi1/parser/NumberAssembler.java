/**
 * 
 */
package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus;

class NumberAssembler implements Assembler {

	private static final int	BYTES_PER_VALUE		= 2;
	private static final int	BYTES_PER_STATUS	= 1;

	private int byteNr;
	private int[] val = null;

	private ProfileParser profileParser;

	public NumberAssembler(ProfileParser profileParser) {
		this.profileParser = profileParser;
	}

	public void workOn(Assembly ta) throws IOException {

		Day day = (Day) ta.getTarget();
		int tempVal = (int) PPMUtils.hex2dec(((Byte) ta.pop()).byteValue());

		if (day == null) {
			return;
		}

		getVal()[this.byteNr] = (byte) tempVal;
		this.byteNr++;

		if (this.byteNr != (getProfileParser().getNrOfChannels() * BYTES_PER_VALUE + BYTES_PER_STATUS)) {
			return;
		}

		// TODO can be 49 too ... // sh*t!
		if ((day.getReadIndex() < 48) && day.getReading()[day.getReadIndex()].getDate().before(getProfileParser().getMeterTime())) {

			/* 1) create a status object */
			day.setStatus(new LoadProfileStatus((byte) getVal()[0]), day.getReadIndex());

			/* 2) create a reading */
			for (int vi = 0; vi < getProfileParser().getNrOfChannels(); vi++) {
				day.getReading()[day.getReadIndex()].setValue(constructValue(getVal(), (vi * BYTES_PER_VALUE) + BYTES_PER_STATUS), vi);
			}

			/* 3) some debugging info */
			day.setReadingString(
					" ->" + getVal()[0] +
					" " + getVal()[1] +
					" " + getVal()[2],
					day.getReadIndex()
			);

		}
		this.byteNr = 0;
		day.incReadIndex();

	}

	public void setByteNr(int byteNr) {
		this.byteNr = byteNr;
	}

	int[] getVal() {
		if (this.val == null) {
			this.val = new int[(getProfileParser().getNrOfChannels() * BYTES_PER_VALUE) + BYTES_PER_STATUS];
		}
		return this.val;
	}

	private LoadProfileStatus constructStatus(int[] iArray) {
		return new LoadProfileStatus((byte) iArray[0]);
	}

	//	private BigDecimal constructValue(int[] iArray, int i) throws IOException {
	//		long v = iArray[i] * 10000;
	//		v += (iArray[i + 1] * 100);
	//		v += iArray[i + 2];
	//		return getProfileParser().getScalingFactor().toProfileNumber(v);
	//	}

	private BigDecimal constructValue(int[] iArray, int i) throws IOException {
		long v = iArray[i] * 100;
		v += iArray[i + 1];
		return getProfileParser().getScalingFactor().toProfileNumber(v);
	}

	public ProfileParser getProfileParser() {
		return profileParser;
	}
}