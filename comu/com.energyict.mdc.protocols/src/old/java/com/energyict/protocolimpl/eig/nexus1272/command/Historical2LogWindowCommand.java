package com.energyict.protocolimpl.eig.nexus1272.command;

public class Historical2LogWindowCommand extends AbstractReadCommand implements ReadCommand{

	public Historical2LogWindowCommand(int transID) {
			super(transID);
			startAddress =  new byte[] {(byte) 0x95,(byte) 0xC0};;
			numRegisters =  new byte[] {0x00, 0x40};
		}

		public byte[] getStartAddress() {
			return startAddress;
		}

		public void setStartAddress(byte[] sa) {
			startAddress = sa;
			
		}
}
