package com.energyict.protocolimpl.kenda.meteor;

public class MeteorExtendedPersonalityTable extends Parsers implements MeteorCommandAbstract{
	
	protected char dumbPrinter=0;
	protected char[] siteName=new char[64];
	protected char[] unAssigned=new char[64]; // problem in the datasheet!!!!!!!!!!!
	protected char[] osName=new char[64];
	protected char[] prHeader=new char[64];
	protected char[] prTrail=new char[64];
	protected char[] osName2=new char[64];
	protected MeteorChanName[] chanName=new MeteorChanName[96];
	protected char[] prOrder= new char[96];
	protected MeteorCommsSetup[] commsSetup=new MeteorCommsSetup[4];
	protected char[] filler=new char[48];
	protected MeteorSetupInfo[] setupInfo= new MeteorSetupInfo[5];
	protected char[] powerType= new char[48];
	protected char[] circuitNo= new char[48];
	protected MeteorCCTName[] cctName=new MeteorCCTName[6];
	
	/*
	 * constructors
	 */
	MeteorExtendedPersonalityTable(){}
	MeteorExtendedPersonalityTable(char[] c){
		process(c);
	}
	MeteorExtendedPersonalityTable(byte[] b){
		process(parseBArraytoCArray(b));
	}
	/*
	 * (non-Javadoc)
	 * @see com.energyict.protocolimpl.meteor.MeteorCommandAbstract#parseToByteArray()
	 */
	public byte[] parseToByteArray() {
		// serialize
		String serial="";
		serial+=dumbPrinter;
		serial+=new String(siteName);
		serial+=new String(unAssigned);
		serial+=new String(osName);
		serial+=new String(prHeader);
		serial+=new String(prTrail);
		serial+=new String(osName2);
		for (int i=0; i<96; i++){
			serial+=chanName[i].toString();
		}
		serial+=new String(prOrder);
		for (int i=0; i<4; i++){
			serial+=commsSetup[i].toString();
		}
		serial+=new String(filler);
		for (int i=0; i<5; i++){
			serial+=setupInfo[i].toString();
		}
		serial+=new String(powerType);
		serial+=new String(circuitNo);
		for (int i=0; i<6; i++){
			serial+=cctName[i].toString();
		}
		return parseCArraytoBArray(serial.toCharArray());  // can be done in one command
	}
	public void printData() {
		System.out.println("dumbPrinter:          "+NumberToString(dumbPrinter));
		System.out.print  ("siteName:          ");
		for(int i=0; i<64; i++){
			System.out.print(NumberToString(siteName[i])+" ");
		}
		System.out.println();
		System.out.print  ("unAssigned:        ");
		for(int i=0; i<64; i++){
			System.out.print(NumberToString(unAssigned[i])+" ");
		}
		System.out.println();
		System.out.print  ("osName:            ");
		for(int i=0; i<64; i++){
			System.out.print(NumberToString(osName[i])+" ");
		}
		System.out.println();
		System.out.print  ("prHeader:          ");
		for(int i=0; i<64; i++){
			System.out.print(NumberToString(prHeader[i])+" ");
		}
		System.out.println();
		System.out.print  ("prTrail:           ");
		for(int i=0; i<64; i++){
			System.out.print(NumberToString(prTrail[i])+" ");
		}
		System.out.println();		
		System.out.print  ("osName2:           ");
		for(int i=0; i<64; i++){
			System.out.print(NumberToString(osName2[i])+" ");
		}
		System.out.println();
		System.out.print  ("chanName:          ");
		for (int i=0; i<96; i++){
			chanName[i].printData();
		}
		System.out.println();
		System.out.print  ("prOrder:           ");	
		for(int i=0; i<96; i++){
			System.out.print(NumberToString(prOrder[i]));			
		}
		System.out.println();
		System.out.print  ("commSetup:         ");	
		for (int i=0; i<4; i++){
			commsSetup[i].printData();
		}
		System.out.println();
		System.out.println(new String(filler));
		System.out.print  ("setupInfo:         ");	
		for (int i=0; i<5; i++){
			setupInfo[i].printData();
		}
		System.out.println();
		System.out.print  ("powerType:         ");	
		for (int i=0; i<48; i++){
			System.out.print(NumberToString(powerType[i]));
		}
		System.out.println();
		System.out.print  ("circuitNo:         ");	
		for (int i=0; i<48; i++){
			System.out.print(NumberToString(circuitNo[i]));
		}
		System.out.println();
		System.out.print  ("cctName:           ");	
		for (int i=0; i<6; i++){
			cctName[i].printData();
		}
		System.out.println();
	}
	private void process(char[] c) {
		// un-serialize
		String s = new String(c);
		dumbPrinter= s.charAt(0);
		for (int i=0; i<64; i++){
			siteName[i]=s.charAt(1+i);
			osName[i]=s.charAt(129+i); // mistake in the datasheet?
			prHeader[i]=s.charAt(193+i);
			prTrail[i]=s.charAt(257+i);
			osName2[i]=s.charAt(321+i);
		}
		for (int i=0; i<96; i++){
			chanName[i]=new MeteorChanName(s.substring(385+i*8,385+(i+1)*8 ).toCharArray());
			prOrder[i]=s.charAt(1153+i);
		}
		for (int i=0; i<4; i++){
			commsSetup[i]=new MeteorCommsSetup(s.substring(1249+i*4,1249+(i+1)*4).toCharArray());
		}
		for (int i=0; i<46; i++){
			filler[i]=s.charAt(1265+i);
		}
		for (int i=0; i<5; i++){
			setupInfo[i]=new MeteorSetupInfo(s.substring(1311+i*32, 1311+(i+1)*32).toCharArray());
		}
		for (int i=0; i<48; i++){
			powerType[i]=s.charAt(1471+i);
			circuitNo[i]=s.charAt(1519+i);			
		}
		for (int i=0; i<6; i++){
			cctName[i]=new MeteorCCTName(s.substring(1567+i*16, 1567+(i+1)*16).toCharArray());
		}		
	}
	
	
	
}
