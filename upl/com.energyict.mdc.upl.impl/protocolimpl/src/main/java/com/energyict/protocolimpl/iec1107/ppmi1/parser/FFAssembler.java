package com.energyict.protocolimpl.iec1107.ppmi1.parser;

class FFAssembler implements Assembler {

	public void workOn(Assembly ta) {
		((Byte) ta.pop()).byteValue();
	}

}