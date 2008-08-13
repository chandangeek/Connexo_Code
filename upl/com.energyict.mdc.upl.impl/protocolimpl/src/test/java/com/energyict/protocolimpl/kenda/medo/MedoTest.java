//package com.energyict.protocolimpl.kenda.medo;
//
//
//import static org.junit.Assert.*;
//
//import java.io.IOException;
//import java.util.Calendar;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.energyict.protocol.UnsupportedException;
//
//public class MedoTest {
//
//	Medo medo;
//	
//	@Before
//	public void setUp() throws Exception {
//		medo = new Medo();
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//	
//	@Test
//	public void getFirmwareVersionTest(){
//		try {
//			String firm = medo.getFirmwareVersion();
//			assertEquals("MEDO Metering Equipment Digital Outstation - V1.3", firm);
//		} catch (UnsupportedException e) {
//			fail();
//			e.printStackTrace();
//		} catch (IOException e) {
//			fail();
//			e.printStackTrace();
//		}
//	}
//	
//	@Test
//	public void buildHeaderTest(){ // test header and checksum
//		byte[] testedHeader;
//		testedHeader = medo.addCheckSum(medo.buildHeader()); // add checksum to header
//		assertEquals(0,testedHeader[testedHeader.length-1]); // test of header checksum
//		assertTrue(medo.verifyCheckSum(testedHeader));
//		testedHeader[10]=1;
//		assertFalse(medo.verifyCheckSum(testedHeader));
//		testedHeader[9]=1; // set one in the header array => checkSum = 255;
//		testedHeader[10]=(byte) 255;
//		assertTrue(medo.verifyCheckSum(testedHeader));
//		testedHeader[8]=10;
//		assertFalse(medo.verifyCheckSum(testedHeader));
//		testedHeader[10]=(byte) (256-11);
//		assertTrue(medo.verifyCheckSum(testedHeader));
//		assertEquals(testedHeader.length,11);
//		// buildHeader passed all tests
//	}
//	
//	@Test
//	public void buildIdentTest(){
//		byte command=medo.buildIdent(false, true, false, (byte) 0);
//		assertEquals(command,(byte) 0x40);
//		command=medo.buildIdent(false, false, true, command);
//		assertEquals(command,(byte) 0x20);
//		command=medo.buildIdent(false, false, false, command);
//		assertEquals(command,(byte) 0x00);
//		command=medo.buildIdent(true, false, false, command);
//		assertEquals(command,(byte) 0x80);
//		command=medo.buildIdent(true, true, true, command);
//		assertEquals(command,(byte) 0xE0);
//		//buildident passed all tests
//	}
//	
//	@Test
//	public void blockProcessingTest(){ // test the block processing (10+245+1)
//		byte[] block;
//		byte[][] testedBlock ;
//		block=new byte[200];
//		testedBlock=new byte[1][];
//		testedBlock[0]=medo.addCheckSum(block);
//		assertEquals(testedBlock[0].length,201);
//		testedBlock=medo.blockProcessing(block);
//		assertTrue(medo.verifyCheckSum(testedBlock[0]));
//		// test on break up of frame
//		block=new byte[800]; // break up in 4 pieces
//		testedBlock=medo.blockProcessing(block);
//		// check number of segments
//		assertEquals(testedBlock.length,4);
//		// check first segment length
//		assertEquals(testedBlock[0].length,256);
//		// check first segment checksum, checksum should be influenced by ident
//		assertEquals(testedBlock[0][0],(byte) 0x40); // first block should have F=true and L=false
//		assertTrue(medo.verifyCheckSum(testedBlock[0]));		
//		assertEquals(testedBlock[1].length,256);
//		assertEquals(testedBlock[1][0],(byte) 0x00); // first block should have F=true and L=false
//		assertEquals(testedBlock[2].length,256);
//		assertEquals(testedBlock[2][0],(byte) 0x00); // first block should have F=true and L=false
//		assertEquals(testedBlock[3].length,800-10-3*245+11); //frame - header - 3*prev dataframes + header&checksum
//		assertEquals(testedBlock[3][0],(byte) 0x20); // first block should have F=true and L=false
//		
//		block=new byte[256]; // 2 sections (1 byte in surplus)
//		for(int i=0; i<256; i++){
//			block[i]=(byte) i;
//		}
//		testedBlock=medo.blockProcessing(block);
//		// check number of segments
//		assertEquals(testedBlock.length,2);
//		// check first segment length
//		assertEquals(testedBlock[0].length,256);
//		assertEquals(testedBlock[0][0],(byte) 0x40); // first block should have F=true and L=false
//		assertEquals(testedBlock[1][0],(byte) 0x20); // last block ident check
//		assertTrue(medo.verifyCheckSum(testedBlock[0]));				
//		assertTrue(medo.verifyCheckSum(testedBlock[1]));
//		// blockProcessing passed all tests
//	}
//	
//	/*
//	 * Tests for MedoFullPersonality table, starting with every subobject
//	 */
//	@Test
//	public void MedoFullPersonalityTest(){
//		/*
//		 * Tests on instances created in MedoFull/PartPersonality
//		 */
//		MedoTelno mt =new MedoTelno();
//		MedoTelno mt2=new MedoTelno("01234567890123456789");
//		mt.numberPatch("0123456789".toCharArray());
//		assertEquals(mt.getcharArray()[8],'8');
//		assertEquals(mt.getcharArray()[15],' ');
//		assertEquals(mt.getcharArray()[11],' ');
//		assertEquals(mt.getcharArray().length,"0123456789      ".length());
//		// telephone number parsed correctly
//		assertEquals(mt2.getTelno()[0],'0');
//		assertEquals(mt2.getTelno()[15],'5');
//		assertEquals(mt2.getbyteArray()[15],(byte) '5'); // byte array test
//		MedoUPI mu=new MedoUPI('m','d');
//		mu.setd('a');
//		mu.setm('m');
//		assertEquals(mu.getdchar(),'a');
//		assertEquals(mu.getbyteArray()[0],(byte) 'm'); // byte array test
//		// MedoComDtls is to be removed from the project
//		MedoComDtls mcd=new MedoComDtls();
//		mcd.addString("peter");
//		assertTrue(mcd.addString("staelens"));
//		mcd.addString("Junit");
//		mcd.addString("testcode langer dan 32 bytes in deze string om de parser te testen");
//		assertFalse(mcd.addString("een lijn te veel"));
//		assertEquals(mcd.getbyteArray().length,128);     	// byte array test	
//		assertEquals(mcd.getbyteArray()[32], (byte) 's');	// byte array test	
//		assertEquals(mcd.getbyteArray()[127], (byte) ' '); 	// byte array test
//		// MedoComDtls passed all tests
//		MedoCLK mclk = new MedoCLK();
//		mclk.setDate((char) 2,(char) 7,(char) 8);
//		mclk.setTime((byte) 30,(byte) 18,(byte) 14);
//		assertEquals(mclk.getbyteArray()[4],(byte) 7); // byte array test
//		Calendar calendar=Calendar.getInstance();
//		MedoCLK mclk2 = new MedoCLK(calendar);
//		assertEquals(mclk2.getbyteArray()[5],(byte) 8); // byte array test
//		mclk2.setDate((byte) 13, (byte) 8, (byte) 79);
//		assertEquals(mclk2.getbyteArray()[5],79);  // byte array test
//		// MedoCLK passed all tests 
//		MedoRlytab mr= new MedoRlytab();
//		assertEquals(mr.getbyteArray().length,5);
//		mr.setWidth((short) 300);
//		
//		/*
//		 * All instantiated objects have been tested
//		 * serialization to byte arrays was tested extensively
//		 * Tests of MedoPartPersonalityTable follows
//		 */
//		MedoPartPersonalityTable MPPT=new MedoPartPersonalityTable();
//		MedoPartPersonalityTable MPPT2;
//		// test of short-char-short parsers
//		System.out.println();
//		for(short s=-16000; s <16000; s++){
//			assertEquals(MPPT.parseCharToShort(MPPT.parseShortToChar(s)),s);
//		}
//		
//		assertEquals(MPPT.getDestab()[0].getbyteArray().length,16);
//		assertEquals(MPPT.parseToByteArray().length,750);
//		
//		// fill in fields in the MedoPartPersonalityTable object and test serialization
//		MPPT.setComtad(mclk2); // fill in date 13/08/1979
//		assertEquals(MPPT.parseToByteArray()[617],13);
//		assertEquals(MPPT.parseToByteArray()[618],8);
//		assertEquals(MPPT.parseToByteArray()[619],79);
//		MedoTelno[] mTArray=new MedoTelno[4];
//		mTArray[0]=mt;
//		mTArray[1]=new MedoTelno();
//		mTArray[2]=mt2;	
//		mTArray[3]=new MedoTelno();
//		MPPT.setDestab(mTArray);
//		MPPT.setDumax((char) 250);
//		MPPT.setDuwait((short) 5000);
//		assertEquals(MPPT.parseToByteArray()[15],(byte) ' ');
//		assertEquals(MPPT.parseToByteArray()[47],(byte) '5');
//		// tested serialized fields are on the correct position
//		// test back and forward serialization
//		byte[] bMPPT=MPPT.parseToByteArray(); // retrieve serialized matrix
//		MPPT2 = new MedoPartPersonalityTable(bMPPT); // feed serialized matrix in the object
//		for(int i=0;i<750;i++){
//			assertEquals(MPPT.parseToByteArray()[i],MPPT2.parseToByteArray()[i]);			
//		}
//		assertEquals(MPPT.getDuwait(),MPPT2.getDuwait()); // test on parsed shorts
//		assertEquals(MPPT.getDuwait(),(short) 5000);		
//		assertEquals(MPPT2.getDuwait(),(short) 5000);
//		
//		MedoFullPersonalityTable MPPT3=new MedoFullPersonalityTable();
//		MedoFullPersonalityTable MPPT4;
//		MPPT3.setDestab(mTArray);
//		MPPT3.setDumax((char) 250);
//		MPPT3.setDuwait((short) 5000);
//		MPPT3.setSlave((char) 23);
//		assertEquals(MPPT.parseToByteArray()[15],(byte) ' ');
//		assertEquals(MPPT.parseToByteArray()[47],(byte) '5');
//		// tested serialized fields are on the correct position
//		// test back and forward serialization
//		bMPPT=MPPT3.parseToByteArray(); // retrieve serialized matrix
//		MPPT4 = new MedoFullPersonalityTable(bMPPT); // feed serialized matrix in the object
//		for(int i=0;i<750;i++){
//			assertEquals(MPPT.parseToByteArray()[i],MPPT2.parseToByteArray()[i]);			
//		}
//		assertEquals(MPPT3.getDuwait(),MPPT2.getDuwait()); // test on parsed shorts
//		assertEquals(MPPT3.getDuwait(),(short) 5000);		
//		assertEquals(MPPT4.getDuwait(),(short) 5000);
//
//		// serialization and deserialization works
//	}
//	@Test
//	public void MedoStatusTest(){
//		/*
//		 * MedoStatus test
//		 */
//		MedoCLK mclk = new MedoCLK();
//		mclk.setDate((char) 2,(char) 7,(char) 8);
//		mclk.setTime((byte) 30,(byte) 18,(byte) 14);
//		MedoStatus MedStat=new MedoStatus();
//		MedoStatus MedStat2;
//		MedStat.setClk(mclk);
//		MedStat.setBatlow((char) 25);
//		assertEquals(MedStat.parseToByteArray().length,110);
//		MedStat2=new MedoStatus(MedStat.parseToByteArray());
//		for(int i=0;i<110;i++){
//			assertEquals(MedStat.parseToByteArray()[i],MedStat2.parseToByteArray()[i]);			
//		}
//	}
//	
//	@Test
//	public void MedoRequestReadMeterDemandsTest(){
//		MedoRequestReadMeterDemands request = new MedoRequestReadMeterDemands();
//		
//	}
//	@Test
//	public void MedoReturnedReadMeterDemandsTest(){
//		//char[] c=new char[6];
//		//MedoReturnedReadMeterDemands returned = new MedoReturnedReadMeterDemands(c);
//		
//	}
//	
//	/*
//	 * Stream tests
//	 */
//	@Test
//	public void MedoFileStreamReads(){
//		try {
//			MedoFileReader mfr=new MedoFileReader("c:/temp/","medostrp.txt");
//			ComStruc[] com=mfr.getCommandReply();
//			System.out.println(com.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		// furhter testing has been done by visually checking read data and file data
//	}
//	
//	@Test
//	public void MedoCommandStructuresTest(){
//		// register tests
//		Parsers command=null;
//		boolean flag=false;
//		MedoCommandFactory medoComFact=new MedoCommandFactory(); // command factory!!!!
//		try {
//			MedoFileReader mfr=new MedoFileReader("c:/temp/","medostrp.txt");
//			ComStruc[] com=mfr.getCommandReply();
//			for(ComStruc s:com){ // run trough command sequences
//				command=medoComFact.addCommand(s, command);
//				flag=medoComFact.isReady();  // command sequence is complete
//				if(flag && !medoComFact.isType()){ // flag is true and data is received
//					if(command instanceof MedoStatus){
//						MedoStatus ms=(MedoStatus) command;
//						ms.printData();
//					}
//					if(command instanceof MedoFullPersonalityTable){
//						MedoFullPersonalityTable mfpt=(MedoFullPersonalityTable) command;
//						mfpt.printData();
//					}
//					if(command instanceof MedoReadDialReadings){
//						MedoReadDialReadings mrdr=(MedoReadDialReadings) command;
//						mrdr.printData();
//					}
//					if(command instanceof MedoPowerFailDetails){
//						MedoPowerFailDetails mpfd=(MedoPowerFailDetails) command;
//						mpfd.printData();
//					}
//					if(command instanceof MedoReturnedReadMeterDemands){
//						MedoReturnedReadMeterDemands md=(MedoReturnedReadMeterDemands) command;
//						md.printData();
//					}
//				}
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//	}	
//}
