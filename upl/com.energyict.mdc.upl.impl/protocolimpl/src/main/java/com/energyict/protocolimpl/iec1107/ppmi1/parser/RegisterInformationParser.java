package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import com.energyict.protocolimpl.iec1107.ppmi1.register.RegisterInformation;
import com.energyict.protocolimpl.iec1107.ppmi1.MetaRegister;


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

	byte[] touInput; 
	byte[] mdTouInput;

	RegisterInformation registerInformation;

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

		registerInformation = new RegisterInformation();

		matchTOUAllocation();
		matchMDTOUAllocation();

		return registerInformation;
	}

	private void matchTOUAllocation() {

		int nrTotalRegisters = registerInformation.energyDefinition.length;

		for (int trIndex = 0; trIndex < nrTotalRegisters; trIndex++) {

			MetaRegister sourceReg = registerInformation.energyDefinition[trIndex];
			char[] totalRegisterAssignment = toCharArray(touInput[trIndex]);

	
			if (totalRegisterAssignment[0] == '1')
				registerInformation.tou8.setSourceRegister(sourceReg);

			if (totalRegisterAssignment[1] == '1')
				registerInformation.tou7.setSourceRegister(sourceReg);

			if (totalRegisterAssignment[2] == '1')
				registerInformation.tou6.setSourceRegister(sourceReg);

			if (totalRegisterAssignment[3] == '1')
				registerInformation.tou5.setSourceRegister(sourceReg);

			if (totalRegisterAssignment[4] == '1')
				registerInformation.tou4.setSourceRegister(sourceReg);

			if (totalRegisterAssignment[5] == '1')
				registerInformation.tou3.setSourceRegister(sourceReg);

			if (totalRegisterAssignment[6] == '1')
				registerInformation.tou2.setSourceRegister(sourceReg);

			if (totalRegisterAssignment[7] == '1')
				registerInformation.tou1.setSourceRegister(sourceReg);

		}
	}

	private void matchMDTOUAllocation() {

		int nrTotalRegisters = registerInformation.demandDefinition.length;
		
		for (int trIndex = 0; trIndex < nrTotalRegisters; trIndex++) {

			MetaRegister sourceReg = 
				registerInformation.demandDefinition[trIndex];
			
			char[] totalRegisterAssignment = toCharArray(mdTouInput[trIndex]);
			
			if (totalRegisterAssignment[4] == '1') {
				registerInformation.mdTou4.setSourceRegister(sourceReg);
				registerInformation.cmdTou4.setSourceRegister(sourceReg);
			}
			
			if (totalRegisterAssignment[5] == '1'){
				registerInformation.mdTou3.setSourceRegister(sourceReg);
				registerInformation.cmdTou3.setSourceRegister(sourceReg);
			}
			
			if (totalRegisterAssignment[6] == '1'){
				registerInformation.mdTou2.setSourceRegister(sourceReg);
				registerInformation.cmdTou2.setSourceRegister(sourceReg);
			}	
			
			if (totalRegisterAssignment[7] == '1'){	
				registerInformation.mdTou1.setSourceRegister(sourceReg);
				registerInformation.cmdTou1.setSourceRegister(sourceReg);
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