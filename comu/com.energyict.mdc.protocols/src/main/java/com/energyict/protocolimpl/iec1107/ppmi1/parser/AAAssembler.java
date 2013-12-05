package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

import java.io.IOException;

class AAAssembler implements Assembler {

	private static final int	BYTES_PER_VALUE		= 2;
	private static final int	BYTES_PER_STATUS	= 1;

	private ProfileParser	profileParser;

	public AAAssembler(ProfileParser profileParser) {
		this.profileParser = profileParser;
	}

	public void workOn(Assembly ta) throws IOException {

		ta.pop(); /* clear Stack, and NumberAssembler */
		getProfileParser().getNumberAssembler().setByteNr(0);

		byte[] jmpSize = new byte[2];
		ta.read(jmpSize, 0, 2);
		int channelCount = getProfileParser().getNrOfChannels();
		long jmp = Long.parseLong(PPMUtils.toHexaString(jmpSize[1]) + PPMUtils.toHexaString(jmpSize[0]), 16) - 3;

		if (ta.getTarget() != null) {/* Calculate number of hours under jump */
			Day aDay = (Day) ta.getTarget();
			int intervalsToJump = (int) (jmp / (channelCount * (BYTES_PER_VALUE + BYTES_PER_STATUS))) + 1;
			aDay.addToReadIndex(intervalsToJump);
		}

		for (int i = 0; i < jmp; i++) {
			ta.read();
		}

	}

	public ProfileParser getProfileParser() {
		return profileParser;
	}

}
