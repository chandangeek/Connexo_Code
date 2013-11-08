package com.energyict.protocolimpl.iec1107.ppmi1.parser;

/**
 * @author fbo
 *
 */
class FFAssembler implements Assembler {

	/* (non-Javadoc)
	 * @see com.energyict.protocolimpl.iec1107.ppmi1.parser.Assembler#workOn(com.energyict.protocolimpl.iec1107.ppmi1.parser.Assembly)
	 */
	public void workOn(Assembly ta) {
		((Byte) ta.pop()).byteValue();
	}

}