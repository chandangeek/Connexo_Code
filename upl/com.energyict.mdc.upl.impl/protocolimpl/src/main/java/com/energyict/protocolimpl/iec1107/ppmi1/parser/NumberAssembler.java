/**
 * 
 */
package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus;

class NumberAssembler implements Assembler {

	int byteNr;
	int[] val = null;

	ProfileParser profileParser;

	public void workOn(Assembly ta) throws IOException {

		Day day = (Day) ta.getTarget();
		int tempVal = (int) PPMUtils.hex2dec(((Byte) ta.pop()).byteValue());

		if (day == null) {
			return;
		}

		getVal()[this.byteNr] = (byte) tempVal;
		this.byteNr++;

		if (this.byteNr != (getProfileParser().getNrOfChannels() * 3 + 1)) {
			return;
		}

		if ((day.getReadIndex() < 48  // TODO can be 49 too ... // sh*t!
		)
		&& day.getReading()[day.getReadIndex()].getDate().before(getProfileParser().getMeterTime()) ) {

			/* 1) create a status object */
			day.setStatus(new LoadProfileStatus((byte) getVal()[0]), day.getReadIndex());

			/* 2) create a reading */
			for (int vi = 0; vi < getProfileParser().getNrOfChannels(); vi++) {
				day.getReading()[day.getReadIndex()].setValue(constructValue(getVal(), (vi * 3) + 1), vi);
			}

			/* 3) some debugging info */
			day.setReadingString(
					" ->" + getVal()[0] +
					" " + getVal()[1] +
					" " + getVal()[2] +
					" " + getVal()[3],
					day.getReadIndex()
			);

		}
		this.byteNr = 0;
		day.incReadIndex();

	}

	int[] getVal() {
		if (this.val == null) {
			this.val = new int[(getProfileParser().getNrOfChannels() * 3) + 1];
		}
		return this.val;
	}

	private LoadProfileStatus constructStatus(int[] iArray) {
		return new LoadProfileStatus((byte) iArray[0]);
	}

	private BigDecimal constructValue(int[] iArray, int i) throws IOException {
		long v = iArray[i] * 10000;
		v += (iArray[i + 1] * 100);
		v += iArray[i + 2];
		return getProfileParser().getScalingFactor().toProfileNumber(v);
	}

	public ProfileParser getProfileParser() {
		return profileParser;
	}
}