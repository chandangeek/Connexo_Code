package com.energyict.dlms.cosem;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.ProtocolLink;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.energyict.dlms.cosem.ImageTransfer.ByteArrayImageBlockSupplier;
import com.energyict.dlms.cosem.ImageTransfer.ImageBlockSupplier;
import com.energyict.dlms.cosem.ImageTransfer.RandomAccessFileImageBlockSupplier;
import org.mockito.Mockito;

/**
 * Tests for the {@link ImageTransfer} class.
 * 
 * @author alex
 */
public final class ImageTransferTest {
	
	/** Using this for the byte array supplier. */
	private static final byte[] IMAGE_BYTES = new byte[] {
		0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
		0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
		0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
		0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
		0x40, 0x41, 0x42
	};

	/** Temp folder, using this for the random access tests. */
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	/**
	 * Tests the byte array image block provider.
	 */
	@Test
	public final void testByteArrayImageBlockSupplier() throws Exception {
		final ByteArrayImageBlockSupplier supplier = new ByteArrayImageBlockSupplier(IMAGE_BYTES);
		this.runTestsOn(supplier);
	}
	
	/**
	 * Tests the random access file block provider.
	 */
	@Test
	public final void testRandomAccessFileBlockSupplier() throws Exception {
		final File tempFile = this.tempFolder.newFile();
		
		try (final OutputStream stream = new FileOutputStream(tempFile)) {
			stream.write(IMAGE_BYTES);
			stream.flush();
		}
		
		try (final RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "r")) {
			this.runTestsOn(new RandomAccessFileImageBlockSupplier(randomAccessFile));
		}
	}

	/**
	 * Runs the test cases on the given supplier.
	 * 
	 * @param 	supplier	The supplier to run the tests on.
	 */
	private final void runTestsOn(final ImageBlockSupplier supplier) throws Exception {
		assertThat(supplier.getSize()).isEqualTo(IMAGE_BYTES.length);
		
		final int blockSize = 2;
		final int numberOfBlocks = (supplier.getSize() / blockSize) + 1;
		
		// Blocks are zero-based. Request block 10, which should be equal to  0x14, 0x15
		assertThat(supplier.getBlock(10, blockSize, true)).isEqualTo(new byte[] { 0x14, 0x15 });
		assertThat(supplier.getBlock(10, blockSize, false)).isEqualTo(new byte[] { 0x14, 0x15 });
		
		// Now go to the last block.
		assertThat(supplier.getBlock(numberOfBlocks - 1, blockSize, false)).isEqualTo(new byte[] { 0x42 });
		
		// Check that it gets padded if we ask it to.
		assertThat(supplier.getBlock(numberOfBlocks - 1, blockSize, true)).isEqualTo(new byte[] { 0x42, 0x00 });
		
		// Now check that it blows up if appropriate.
		
		// Request a bogus block number.
		try {
			supplier.getBlock(-1, 10, false);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (IllegalArgumentException e) {
		}
		
		// Request a bogus block number.
		try {
			supplier.getBlock(100, 10, false);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (IllegalArgumentException e) {
		}
		
		// Use a bogus block size.
		try {
			supplier.getBlock(10, 0, false);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public final void testInitializeFOTAEnexisBehaviour() throws IOException {
		ProtocolLink protocolLink = Mockito.mock(ProtocolLink.class);
		DLMSConnection dlmsConnection = Mockito.mock(DLMSConnection.class);
		Mockito.when(protocolLink.getDLMSConnection()).thenReturn(dlmsConnection);
		Mockito.when(dlmsConnection.getInvokeIdAndPriorityHandler()).thenReturn(new NonIncrementalInvokeIdAndPriorityHandler((byte) 0x421));
		Mockito.when(dlmsConnection.sendRequest(Mockito.any())).thenReturn(new byte[] {0x00, 0x00, 0x00, 0x0D, 0x00, 0x00, 0x03, 0x01});
		ImageTransfer transfer = new ImageTransfer(protocolLink);
		transfer.initializeFOTA(true);
		byte[] expected = new byte[] {(byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC3, (byte) 0x01, (byte) 0x21, (byte) 0x00, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x01,
				(byte) 0x0F, (byte) 0x00 };
		Mockito.verify(dlmsConnection).sendUnconfirmedRequest(expected);
	}


	@Test
	public final void testInitializeFOTADefaultBehaviour() throws IOException {
		ProtocolLink protocolLink = Mockito.mock(ProtocolLink.class);
		DLMSConnection dlmsConnection = Mockito.mock(DLMSConnection.class);
		Mockito.when(protocolLink.getDLMSConnection()).thenReturn(dlmsConnection);
		Mockito.when(dlmsConnection.getInvokeIdAndPriorityHandler()).thenReturn(new NonIncrementalInvokeIdAndPriorityHandler((byte) 0x421));
		Mockito.when(dlmsConnection.sendRequest(Mockito.any())).thenReturn(new byte[] {0x00, 0x00, 0x00, 0x0D, 0x00, 0x00, 0x03, 0x01});
		ImageTransfer transfer = new ImageTransfer(protocolLink);
		transfer.initializeFOTA(false);
		byte[] expected = new byte[] {(byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0xC3, (byte) 0x01, (byte) 0x21, (byte) 0x00, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x01, (byte) 0x01,
				(byte) 0x02, (byte) 0x02,  // structure 2 objects
				(byte) 0x09, (byte) 0x03, (byte) 0x01, (byte) 0x0F, (byte) 0x00, // octet string with length of 3 bytes
				(byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 }; // double long unsigned fixed size of 4 bytes
		Mockito.verify(dlmsConnection).sendUnconfirmedRequest(expected);
	}



}
