/**
 *
 */
package com.energyict.dlms;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * @author jme
 *
 */
public class ReceiveBufferTest {

	/**
	 * Test method for {@link com.energyict.dlms.ReceiveBuffer#ReceiveBuffer()}.
	 */
	@Test
	public final void testReceiveBuffer() {
		ReceiveBuffer receiveBuffer = new ReceiveBuffer();
		assertEquals(0, receiveBuffer.bytesReceived());
		assertArrayEquals(new byte[0], receiveBuffer.getArray());
	}

	/**
	 * Test method for {@link com.energyict.dlms.ReceiveBuffer#addArray(byte[])}.
	 * @throws IOException
	 */
	@Test
	public final void testAddArrayByteArray() throws IOException {
		ReceiveBuffer receiveBuffer = new ReceiveBuffer();
		assertEquals(0, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[5]);
		assertEquals(5, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[10]);
		assertEquals(15, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[0]);
		assertEquals(15, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[15]);
		assertEquals(30, receiveBuffer.bytesReceived());
	}

	/**
	 * Test method for {@link com.energyict.dlms.ReceiveBuffer#addArray(byte[], int)}.
	 */
	@Test
	public final void testAddArrayByteArrayInt() {
		ReceiveBuffer receiveBuffer = new ReceiveBuffer();
		assertEquals(0, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[15], 0);
		assertEquals(15, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[15], 5);
		assertEquals(25, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[15], 10);
		assertEquals(30, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[15], 15);
		assertEquals(30, receiveBuffer.bytesReceived());
	}

	/**
	 * Test method for {@link com.energyict.dlms.ReceiveBuffer#getArray()}.
	 */
	@Test
	public final void testGetArray() {
		ReceiveBuffer receiveBuffer = new ReceiveBuffer();
		assertArrayEquals(new byte[0], receiveBuffer.getArray());
		receiveBuffer.addArray(new byte[15], 0);
		assertArrayEquals(new byte[15], receiveBuffer.getArray());
		receiveBuffer.addArray(new byte[15], 5);
		assertArrayEquals(new byte[25], receiveBuffer.getArray());
		receiveBuffer.addArray(new byte[15], 10);
		assertArrayEquals(new byte[30], receiveBuffer.getArray());
		receiveBuffer.addArray(new byte[15], 15);
		assertArrayEquals(new byte[30], receiveBuffer.getArray());
	}

	/**
	 * Test method for {@link com.energyict.dlms.ReceiveBuffer#bytesReceived()}.
	 * @throws IOException
	 */
	@Test
	public final void testBytesReceived() throws IOException {
		ReceiveBuffer receiveBuffer = new ReceiveBuffer();
		assertEquals(0, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[15]);
		assertEquals(15, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[15]);
		assertEquals(30, receiveBuffer.bytesReceived());
		receiveBuffer.addArray(new byte[15], 5);
		assertEquals(40, receiveBuffer.bytesReceived());
	}

}
