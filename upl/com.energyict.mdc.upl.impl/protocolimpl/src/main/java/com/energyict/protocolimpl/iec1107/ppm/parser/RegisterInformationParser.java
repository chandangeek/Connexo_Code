package com.energyict.protocolimpl.iec1107.ppm.parser;

import com.energyict.protocolimpl.iec1107.ppm.MetaRegister;
import com.energyict.protocolimpl.iec1107.ppm.register.RegisterInformation;


/**
 * The meter organizes the TOU allocations in a very twisted, hardware oriented
 * manner.
 * <p>
 * There is a one-to-many relationship between "Total Registers" and "TOU
 * Registers". So it would make sense to store the foreign key with the TOU
 * Registers.
 * <p>
 * The meter does the opposite, and gives every Total Register a collection of
 * TOU Registers.
 * 
 * <pre>
 * 
 * 
 *    TIME OF USE REGISTER ALLOCATION
 * 
 *    	741 - Import kWh
 *    	742 - Export kWh
 *    	743 - Import kvarh
 *    	744 - Export kvarh
 *    	745 - Total kVAh
 * 
 *    Bit format: 0 = inactive, 1 = active
 * 
 * 
 * 
 *    			741		742		743		744		745
 *    msb
 *    			 _		 _		 _		 _		 _
 *    		0	|_|		|_|		|_|		|_|		|_|		TOU register 1
 *    		1	|_|		|_|		|_|		|_|		|_|		TOU register 2
 *    		2	|_|		|_|		|_|		|_|		|_|		TOU register 3
 *    		3	|_|		|_|		|_|		|_|		|_|		TOU register 4
 *    		4	|_|		|_|		|_|		|_|		|_|		TOU register 5
 *    		5	|_|		|_|		|_|		|_|		|_|		TOU register 6
 *    		6	|_|		|_|		|_|		|_|		|_|		TOU register 7
 *    		7	|_|		|_|		|_|		|_|		|_|		TOU register 8
 *    lsb
 * 
 *    There are 8 Time Of Use Registers (=Rates) that can be assigned for storing
 *    use of the Total Registers (=Cumulative).
 * 
 * 
 * 
 * </pre>
 * <pre>
 * 
 * 
 *    MAXIMUM DEMAND TIME OF USE REGISTER ALLOCATION
 * 
 *    	751 - Import kWh
 *    	752 - Export kWh
 *    	753 - Import kvarh
 *    	754 - Export kvarh
 *    	755 - Total kVAh
 * 
 *    Bit format: 0 = inactive, 1 = active
 * 
 * 
 * 
 *    			751		752		753		754		755
 *    msb
 *    			 _		 _		 _		 _		 _
 *    		0	|_|		|_|		|_|		|_|		|_|
 *    		1	|_|		|_|		|_|		|_|		|_|
 *    		2	|_|		|_|		|_|		|_|		|_|
 *    		3	|_|		|_|		|_|		|_|		|_|
 *    		4	|_|		|_|		|_|		|_|		|_|		MD TOU register 1
 *    		5	|_|		|_|		|_|		|_|		|_|		MD TOU register 2
 *    		6	|_|		|_|		|_|		|_|		|_|		MD TOU register 3
 *    		7	|_|		|_|		|_|		|_|		|_|		MD TOU register 4
 *    lsb
 * 
 * 
 * 
 * </pre>
 * 
 * This parsers takes all the allocation bytes, and uses them to build up
 * a RegisterInformation object.
 * 
 * @author fbo
 */

public class RegisterInformationParser {

	private byte[] touInput;
	private byte[] mdTouInput;

	private RegisterInformation registerInformation;

	/**
	 * Set the input of the parser
	 * 
	 * @param reg741
	 *            Register 741
	 * @param reg742
	 *            Register 742
	 * @param reg743
	 *            Register 743
	 * @param reg744
	 *            Register 744
	 * @param reg745
	 *            Register 745
	 * @param reg751
	 *            Register 751
	 * @param reg752
	 *            Register 752
	 * @param reg753
	 *            Register 753
	 * @param reg754
	 *            Register 754
	 * @param reg755
	 *            Register 755
	 */
	public void set(byte reg741, byte reg742, byte reg743, byte reg744,
			byte reg745, byte reg751, byte reg752, byte reg753, byte reg754,
			byte reg755) {

		this.touInput = new byte[]{reg741, reg742, reg743, reg744,
				reg745};

		this.mdTouInput = new byte[]{reg751, reg752, reg753, reg754,
				reg755};

	}

	public RegisterInformation match() {

		this.registerInformation = new RegisterInformation();

		matchTOUAllocation();
		matchMDTOUAllocation();

		return this.registerInformation;
	}

	private void matchTOUAllocation() {

		int nrTotalRegisters = this.registerInformation.getEnergyDefinition().length;

		for (int trIndex = 0; trIndex < nrTotalRegisters; trIndex++) {

			MetaRegister sourceReg = this.registerInformation.getEnergyDefinition()[trIndex];
			char[] totalRegisterAssignment = toCharArray(this.touInput[trIndex]);


			if (totalRegisterAssignment[0] == '1') {
				this.registerInformation.getTou8().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[1] == '1') {
				this.registerInformation.getTou7().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[2] == '1') {
				this.registerInformation.getTou6().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[3] == '1') {
				this.registerInformation.getTou5().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[4] == '1') {
				this.registerInformation.getTou4().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[5] == '1') {
				this.registerInformation.getTou3().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[6] == '1') {
				this.registerInformation.getTou2().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[7] == '1') {
				this.registerInformation.getTou1().setSourceRegister(sourceReg);
			}

		}
	}

	private void matchMDTOUAllocation() {

		int nrTotalRegisters = this.registerInformation.getDemandDefinition().length;

		for (int trIndex = 0; trIndex < nrTotalRegisters; trIndex++) {

			MetaRegister sourceReg =
				this.registerInformation.getDemandDefinition()[trIndex];

			char[] totalRegisterAssignment = toCharArray(this.mdTouInput[trIndex]);

			if (totalRegisterAssignment[4] == '1') {
				this.registerInformation.getMdTou4().setSourceRegister(sourceReg);
				this.registerInformation.getCmdTou4().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[5] == '1'){
				this.registerInformation.getMdTou3().setSourceRegister(sourceReg);
				this.registerInformation.getCmdTou3().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[6] == '1'){
				this.registerInformation.getMdTou2().setSourceRegister(sourceReg);
				this.registerInformation.getCmdTou2().setSourceRegister(sourceReg);
			}

			if (totalRegisterAssignment[7] == '1'){
				this.registerInformation.getMdTou1().setSourceRegister(sourceReg);
				this.registerInformation.getCmdTou1().setSourceRegister(sourceReg);
			}
		}

	}

	/**Converts byte to array of 'bits'.  Bits in this case being characters
	 * 0 & 1. */
	private char[] toCharArray(byte b) {

		char[] result = {'0', '0', '0', '0', '0', '0', '0', '0'};

		int i = b&0xFF;
		char[] bitArray = Integer.toBinaryString(i).toCharArray();
		System.arraycopy(bitArray, 0, result, 8 - bitArray.length,
				bitArray.length);

		return result;
	}

}