package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Test the different utilities methods for their in- and output
 * 
 * @author gna
 * @since 24/08/2009
 */
public class S4sObjectUtilsTest extends S4sObjectUtils{

	@Test
	public void getAsciiConvertedDecimalByteArray(){
		byte[] byteArray = new byte[]{56, 48, 57, 48, 52, 50};
		byte[] expected = new byte[]{80, 90, 42};
		assertArrayEquals(expected,getAsciiConvertedDecimalByteArray(byteArray));
	}
	
	@Test
	public void revertByteArrayTest(){
		byte[] byteArray = new byte[]{56, 48, 57, 48, 52, 50};
		byte[] expected = new byte[]{50, 52, 48, 57, 48, 56};
		assertArrayEquals(expected, revertByteArray(byteArray));
	}
	
	@Test
	public void switchNibblesTest(){
		byte[] byteArray = new byte[]{56, 48, 57, 48, 52, 50};
		byte[] expected = new byte[]{48, 56, 48, 57, 50, 52};
		assertArrayEquals(expected, switchNibbles(byteArray));
	}
}
