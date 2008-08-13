//package com.energyict.protocolimpl.elster.opus;
//
//import static org.junit.Assert.*;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Calendar;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.energyict.protocolimpl.medo.Medo;
//import com.energyict.protocolimpl.meteor.ComStruc;
//import com.energyict.protocolimpl.meteor.MeteorFileReader;
//
//
//public class OpusTest {
//	Opus opus;
//	
//	@Before
//	public void setUp() throws Exception {
//		opus = new Opus();
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//	
//	@Test
//	public void OpusBuildPacketStringBuilderTest(){
//		OpusBuildPacket obp= new OpusBuildPacket();
//		assertEquals(obp.stringBuilder(1), "001");
//		assertEquals(obp.stringBuilder(12), "012");
//		assertEquals(obp.stringBuilder(123), "123");
//		assertEquals(obp.stringBuilder(1234), "234");
//	}
//	
//	@Test
//	public void OpusFileStreamReads(){
//		ComStruc[] coms;
//		try {
//			OpusFileReader mfr=new OpusFileReader("c:/temp/","opusstrp.txt");
//			coms=mfr.getCommandReply();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	@Test
//	public void checkSumTest() throws IOException{
//		OpusFileReader mfr=new OpusFileReader("c:/temp/","opusstrp.txt");
//		ComStruc[] com=mfr.getCommandReply();
//		OpusBuildPacket OB;
//		for(ComStruc c:com){
//			if(c.getByteArray().length>=32){
//				OB=new OpusBuildPacket(c.getByteArray());				
//				System.out.println(OB.getCommand());
//				assertTrue(OB.verifyCheckSum());
//				int temp=OB.getCheckSum();
//				OB.generateCheckSum();
//				assertEquals(temp,OB.getCheckSum());
//			}
//		}
//	}
//	
//	@Test
//	public void OpusBuildPacketTest(){
//		
//	}
///*	Methods set private, for testing set protected
//	@Test
//	public void OpusTest(){
//		Calendar cal=Calendar.getInstance();
//		assertEquals(10,opus.getCommandnr(cal.getTime()));
//		cal.set(Calendar.DAY_OF_MONTH, 16);
//		assertEquals(11,opus.getCommandnr(cal.getTime()));
//		cal.set(Calendar.DAY_OF_MONTH, 1);
//		assertEquals(26,opus.getCommandnr(cal.getTime()));
//		cal.set(Calendar.MONTH, 5);
//		assertEquals(26+30,opus.getCommandnr(cal.getTime()));
//	}
//	*/
//	
//	@Test
//	public void CheckSumTest() throws IOException{
//		/*
//		 * Problem: checksum is given in ASCII code, derived from a binary operation
//		 * 			when a checksum is verified, parsers can not be used 
//		 * Solution: check checksum on validity of characters
//		 * 			if not valid, generate the wrong checksum and let the system send a NAK
//		 */
//		OpusBuildPacket obp=new OpusBuildPacket();
//		// 3 valid codes
//		ArrayList <String> array=new ArrayList<String>();
//		
//		array.add((char) 0x01+ "007032057#S007C009#0#0#0#0#0#0#0##061"+(char) 0x03);
//		array.add((char) 0x01+ "007032058#0#0#0#0#0#0#0#0##168"+(char) 0x03);
//		array.add((char) 0x01+ "007032059#0#0#0#0#0#0#0#0##169"+(char) 0x03);
//		// 3 illegal codes
//		array.add((char) 0x01+ "007032059#0#0#0#5#0#0#0#0##169"+(char) 0x03); // wrong checksum value
//		array.add((char) 0x01+ "007032059#0#0#0#0#0#0#0#0##1-9"+(char) 0x03); // checksum corrupt
//		array.add((char) 0x01+ "  df 20 9 0#0#0#d#0#0#0#0##169"+(char) 0x03); // part of string corrupt, checksum correct
//		array.add((char) 0x01+ "ajhd!')àà)ç(rjkgmnv,'!çà^çè!!'"+(char) 0x03); // complete string corrupt
//
//		boolean temp;int tel=0;
//		for(String s:array){
//			tel++;
//			obp=new OpusBuildPacket(s.toCharArray()); // can throw error on checksum
//			temp=obp.verifyCheckSum(); // verify checksum on mathematical correctness
//			if(tel<4){
//				assertTrue(temp);
//			}else{
//				assertFalse(temp);
//			}
//		}
//
//
//	}
//}
