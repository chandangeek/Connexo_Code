package com.energyict.protocolimpl.meteor;

public class MeteorPowerFailDetails extends Parsers {
	private MeteorCLK firstFailure= new MeteorCLK();
	private MeteorCLK lastRecovery= new MeteorCLK();
	private MeteorReadDialReadings dialPf=new MeteorReadDialReadings();
	private short perOut=0;
	private short secOut=0;
	private short lpfCNT=0;
	private MeteorCLK[] pfhist= new MeteorCLK[14];
	private char[] pffree={0,0,0,0,0,0};  // unused
	private MeteorCLK timeOp2= new MeteorCLK();
	private MeteorReadDialReadings dialseOp2=new MeteorReadDialReadings();
	
	
	MeteorPowerFailDetails(){
		for (int i=0; i<14; i++){
			pfhist[i]=firstFailure; // zeros
		}
	}
	
	MeteorPowerFailDetails(char[] c){
		processMeteorPowerFailDetails(c);
	}
	
	MeteorPowerFailDetails(byte[] b){
		processMeteorPowerFailDetails(parseBArraytoCArray(b));
	}
	
	private void processMeteorPowerFailDetails(char[] c){
		String s=new String(c);
		firstFailure=new MeteorCLK(s.substring(0,6).toCharArray());
		lastRecovery=new MeteorCLK(s.substring(6,12).toCharArray());
		dialPf=new MeteorReadDialReadings(s.substring(12,156).toCharArray());
		perOut=parseCharToShort(s.substring(156, 158).toCharArray());
		secOut=parseCharToShort(s.substring(158, 160).toCharArray());
		lpfCNT=parseCharToShort(s.substring(160, 162).toCharArray());
		for (int i=0; i<14; i++){
			pfhist[i]=new MeteorCLK(s.substring(162+i*6,168+i*6).toCharArray());
		}
		pffree[0]=c[246];
		pffree[1]=c[247];
		pffree[2]=c[248];
		pffree[3]=c[249];
		pffree[4]=c[250];
		pffree[5]=c[251];
	}
	
	public void printData(){		
		System.out.println("timPf:          "+firstFailure.toString());
		System.out.println("timPr:          "+lastRecovery.toString());
		System.out.println("dialPf:         "+dialPf.toString());
		System.out.println("perOut:         "+NumberToString(perOut));
		System.out.println("secOut:         "+NumberToString(secOut));
		System.out.println("lpfCNT:         "+NumberToString(lpfCNT));
		for(int i=0; i<14; i++){
			System.out.println("pfhist           :"+pfhist[i].toString());
		}
		System.out.println("pffree:          ");
		for(int i=0; i<6; i++){
			System.out.print(pffree[i]+" ");
		}
		System.out.println();
	}

	/**
	 * @return the firstFailure
	 */
	public MeteorCLK getFirstFailure() {
		return firstFailure;
	}

	/**
	 * @return the lastRecovery
	 */
	public MeteorCLK getLastRecovery() {
		return lastRecovery;
	}

	/**
	 * @return the dialPf
	 */
	public MeteorReadDialReadings getDialPf() {
		return dialPf;
	}

	/**
	 * @return the perOut
	 */
	public short getPerOut() {
		return perOut;
	}

	/**
	 * @return the secOut
	 */
	public short getSecOut() {
		return secOut;
	}

	/**
	 * @return the lpfCNT
	 */
	public short getLpfCNT() {
		return lpfCNT;
	}

	/**
	 * @return the pfhist
	 */
	public MeteorCLK[] getPfhist() {
		return pfhist;
	}

	/**
	 * @return the pffree
	 */
	public char[] getPffree() {
		return pffree;
	}

}
