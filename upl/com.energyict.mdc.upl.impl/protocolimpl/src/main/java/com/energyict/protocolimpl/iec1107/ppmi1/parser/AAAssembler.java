package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.io.IOException;

import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

class AAAssembler implements Assembler {

	private ProfileParser	profileParser;

	public AAAssembler(ProfileParser profileParser) {
		this.profileParser = profileParser;
	}

	public void workOn(Assembly ta) throws IOException {

		ta.pop(); /* clear Stack, and NumberAssembler */
		getProfileParser().getNumberAssembler().setByteNr(0);

		System.out.println(getProfileParser().getAssembly());

		byte[] jmpSize = new byte[2];
		ta.read(jmpSize, 0, 2);

		long jmp = Long.parseLong(PPMUtils.toHexaString(jmpSize[1]) + PPMUtils.toHexaString(jmpSize[0]), 16) - 3;

		System.out.println("jump Size = " + jmp);

		if (ta.getTarget() != null) {/* Calculate number of hours under jump */
			Day aDay = (Day) ta.getTarget();
			aDay.addToReadIndex((int) ((jmp + 3) / (1 + (3 * getProfileParser().getNrOfChannels()))));
		}

		for (int i = 0; i < jmp; i++) {
			ta.read();
		}

		System.out.println(getProfileParser().getAssembly());

	}

	public ProfileParser getProfileParser() {
		return profileParser;
	}

}
