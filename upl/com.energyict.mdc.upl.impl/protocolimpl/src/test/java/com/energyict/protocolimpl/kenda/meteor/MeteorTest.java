//package com.energyict.protocolimpl.kenda.meteor;
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
//public class MeteorTest {
//
//	Meteor Meteor;
//	
//	@Before
//	public void setUp() throws Exception {
//		Meteor = new Meteor();
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//	
//	@Test
//	public void buildHeaderTest(){ // test header and checksum
//		byte[] testedHeader;
//		testedHeader = Meteor.addCheckSum(Meteor.buildHeader()); // add checksum to header
//		assertEquals(0,testedHeader[testedHeader.length-1]); // test of header checksum
//		assertTrue(Meteor.verifyCheckSum(testedHeader));
//		testedHeader[10]=1;
//		assertFalse(Meteor.verifyCheckSum(testedHeader));
//		testedHeader[9]=1; // set one in the header array => checkSum = 255;
//		testedHeader[10]=(byte) 255;
//		assertTrue(Meteor.verifyCheckSum(testedHeader));
//		testedHeader[8]=10;
//		assertFalse(Meteor.verifyCheckSum(testedHeader));
//		testedHeader[10]=(byte) (256-11);
//		assertTrue(Meteor.verifyCheckSum(testedHeader));
//		assertEquals(testedHeader.length,11);
//		// buildHeader passed all tests
//	}
//	
//	@Test
//	public void buildIdentTest(){
//		byte command=Meteor.buildIdent(false, true, false, (byte) 0);
//		assertEquals(command,(byte) 0x40);
//		command=Meteor.buildIdent(false, false, true, command);
//		assertEquals(command,(byte) 0x20);
//		command=Meteor.buildIdent(false, false, false, command);
//		assertEquals(command,(byte) 0x00);
//		command=Meteor.buildIdent(true, false, false, command);
//		assertEquals(command,(byte) 0x80);
//		command=Meteor.buildIdent(true, true, true, command);
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
//		testedBlock[0]=Meteor.addCheckSum(block);
//		assertEquals(testedBlock[0].length,201);
//		testedBlock=Meteor.blockProcessing(block);
//		assertTrue(Meteor.verifyCheckSum(testedBlock[0]));
//		// test on break up of frame
//		block=new byte[800]; // break up in 4 pieces
//		testedBlock=Meteor.blockProcessing(block);
//		// check number of segments
//		assertEquals(testedBlock.length,4);
//		// check first segment length
//		assertEquals(testedBlock[0].length,256);
//		// check first segment checksum, checksum should be influenced by ident
//		assertEquals(testedBlock[0][0],(byte) 0x40); // first block should have F=true and L=false
//		assertTrue(Meteor.verifyCheckSum(testedBlock[0]));		
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
//		testedBlock=Meteor.blockProcessing(block);
//		// check number of segments
//		assertEquals(testedBlock.length,2);
//		// check first segment length
//		assertEquals(testedBlock[0].length,256);
//		assertEquals(testedBlock[0][0],(byte) 0x40); // first block should have F=true and L=false
//		assertEquals(testedBlock[1][0],(byte) 0x20); // last block ident check
//		assertTrue(Meteor.verifyCheckSum(testedBlock[0]));				
//		assertTrue(Meteor.verifyCheckSum(testedBlock[1]));
//		// blockProcessing passed all tests
//	}
//	
//	/*
//	 * Tests for MeteorFullPersonality table, starting with every subobject
//	 */
//	@Test
//	public void MeteorFullPersonalityTest(){
//		/*
//		 * Tests on instances created in MeteorFull/PartPersonality
//		 */
//		MeteorTelno mt =new MeteorTelno();
//		MeteorTelno mt2=new MeteorTelno("01234567890123456789");
//		mt.numberPatch("0123456789".toCharArray());
//		assertEquals(mt.getcharArray()[8],'8');
//		assertEquals(mt.getcharArray()[15],' ');
//		assertEquals(mt.getcharArray()[11],' ');
//		assertEquals(mt.getcharArray().length,"0123456789      ".length());
//		// telephone number parsed correctly
//		assertEquals(mt2.getTelno()[0],'0');
//		assertEquals(mt2.getTelno()[15],'5');
//		assertEquals(mt2.getbyteArray()[15],(byte) '5'); // byte array test
//		MeteorUPI mu=new MeteorUPI('m','d');
//		mu.setd('a');
//		mu.setm('m');
//		assertEquals(mu.getdchar(),'a');
//		assertEquals(mu.getbyteArray()[0],(byte) 'm'); // byte array test
//		MeteorCLK mclk = new MeteorCLK();
//		mclk.setDate((char) 2,(char) 7,(char) 8);
//		mclk.setTime((byte) 30,(byte) 18,(byte) 14);
//		assertEquals(mclk.getbyteArray()[4],(byte) 7); // byte array test
//		Calendar calendar=Calendar.getInstance();
//		MeteorCLK mclk2 = new MeteorCLK(calendar);
//		assertEquals(mclk2.getbyteArray()[5],(byte) 8); // byte array test
//		mclk2.setDate((byte) 13, (byte) 8, (byte) 79);
//		assertEquals(mclk2.getbyteArray()[5],79);  // byte array test
//		// MeteorCLK passed all tests 
//		MeteorRlytab mr= new MeteorRlytab();
//		assertEquals(mr.getbyteArray().length,5);
//		mr.setWidth((short) 300);
//		
//		/*
//		 * All instantiated objects have been tested
//		 * serialization to byte arrays was tested extensively
//		 * Tests of MeteorPartPersonalityTable follows
//		 */
//		MeteorTelno[] mTArray=new MeteorTelno[4];
//		mTArray[0]=mt;
//		mTArray[1]=new MeteorTelno();
//		mTArray[2]=mt2;	
//		mTArray[3]=new MeteorTelno();
//		
//		MeteorFullPersonalityTable MPPT3=new MeteorFullPersonalityTable();
//		MeteorFullPersonalityTable MPPT4;
//		MPPT3.setDestab(mTArray);
//		MPPT3.setDumax((char) 250);
//		MPPT3.setDuwait((short) 5000);
//		MPPT3.setSlave((char) 23);
//		byte[] temp=MPPT3.parseToByteArray();
//		MPPT4=new MeteorFullPersonalityTable(temp);
//		assertEquals(MPPT3.getDuwait(),(short) 5000);		
//		assertEquals(MPPT4.getDuwait(),(short) 5000);
//
//		// serialization and deserialization works
//	}
//	@Test
//	public void MeteorStatusTest(){
//		/*
//		 * MeteorStatus test
//		 */
//		MeteorCLK mclk = new MeteorCLK();
//		mclk.setDate((char) 2,(char) 7,(char) 8);
//		mclk.setTime((byte) 30,(byte) 18,(byte) 14);
//		MeteorStatus MetStat=new MeteorStatus();
//		MetStat.setClk(mclk);
//		MetStat.setBatLow((char) 25);
//		MetStat.printData();
//	}
//	
//	@Test
//	public void MeteorRequestReadMeterDemandsTest(){
//		//MeteorRequestReadMeterDemands request = new MeteorRequestReadMeterDemands();
//		
//	}
//	@Test
//	public void MeteorReturnedReadMeterDemandsTest(){
//		//char[] c=new char[6];
//		//MeteorReturnedReadMeterDemands returned = new MeteorReturnedReadMeterDemands(c);
//		
//	}
//	
//	/*
//	 * Stream tests
//	 */
//	@Test
//	public void MeteorFileStreamReads(){
//		try {
//			MeteorFileReader mfr=new MeteorFileReader("c:/temp/","Meteorstrp.txt");
//			ComStruc[] com=mfr.getCommandReply();
//			System.out.println(com.toString());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		// further testing has been done by visually checking read data and file data
//	}
//	
//	@Test
//	public void MeteorCommandStructuresTest(){
//		// register tests
//		Parsers command=null;
//		boolean flag=false;
//		MeteorCommandFactory MeteorComFact=new MeteorCommandFactory(); // command factory!!!!
//		try {
//			MeteorFileReader mfr=new MeteorFileReader("c:/temp/","Meteorstrp.txt");
//			ComStruc[] com=mfr.getCommandReply();
//			for(ComStruc s:com){ // run trough command sequences
//				command=MeteorComFact.addCommand(s, command);
//				flag=MeteorComFact.isReady();  // command sequence is complete
//				if(flag && !MeteorComFact.isType()){ // flag is true and data is received
//					if(command instanceof MeteorStatus){
//						MeteorStatus ms=(MeteorStatus) command;
//						ms.printData();
//					}
//					if(command instanceof MeteorFullPersonalityTable){
//						MeteorFullPersonalityTable mfpt=(MeteorFullPersonalityTable) command;
//						mfpt.printData();
//					}
//					if(command instanceof MeteorReadDialReadings){
//						MeteorReadDialReadings mrdr=(MeteorReadDialReadings) command;
//						mrdr.printData();
//					}
//					if(command instanceof MeteorPowerFailDetails){
//						MeteorPowerFailDetails mpfd=(MeteorPowerFailDetails) command;
//						mpfd.printData();
//					}
//					if(command instanceof MeteorReturnedReadMeterDemands){
//						MeteorReturnedReadMeterDemands md=(MeteorReturnedReadMeterDemands) command;
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
