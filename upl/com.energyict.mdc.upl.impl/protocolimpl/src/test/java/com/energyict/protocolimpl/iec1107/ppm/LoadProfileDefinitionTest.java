/**
 * 
 */
package com.energyict.protocolimpl.iec1107.ppm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.energyict.protocolimpl.iec1107.ppm.register.LoadProfileDefinition;

/**
 * @author jme
 * 
 */
public class LoadProfileDefinitionTest {

	@Test
	public void testLoadProfileDefinition_0x1F() {
		try {
			LoadProfileDefinition lpd = new LoadProfileDefinition(new byte[] { 0x1F });
			assertNotNull(lpd);
			assertNotNull(lpd.toString());
			assertTrue(lpd.hasExportKvar());
			assertTrue(lpd.hasExportKW());
			assertTrue(lpd.hasImportKvar());
			assertTrue(lpd.hasImportKW());
			assertTrue(lpd.hasTotalKVA());
			assertEquals(5, lpd.getNrOfChannels());
			assertNotNull(lpd.toList());
			assertNotNull(lpd.toChannelInfoList());
			assertEquals(lpd.getNrOfChannels(), lpd.toList().size());
			assertEquals(lpd.getNrOfChannels(), lpd.toChannelInfoList().size());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testLoadProfileDefinition_0x0F() {
		try {
			LoadProfileDefinition lpd = new LoadProfileDefinition(new byte[] { 0x0F });
			assertNotNull(lpd);
			assertNotNull(lpd.toString());
			assertTrue(lpd.hasExportKvar());
			assertTrue(lpd.hasExportKW());
			assertTrue(lpd.hasImportKvar());
			assertTrue(lpd.hasImportKW());
			assertFalse(lpd.hasTotalKVA());
			assertEquals(4, lpd.getNrOfChannels());
			assertNotNull(lpd.toList());
			assertNotNull(lpd.toChannelInfoList());
			assertEquals(lpd.getNrOfChannels(), lpd.toList().size());
			assertEquals(lpd.getNrOfChannels(), lpd.toChannelInfoList().size());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testLoadProfileDefinition_0x0E() {
		try {
			LoadProfileDefinition lpd = new LoadProfileDefinition(new byte[] { 0x0E });
			assertNotNull(lpd);
			assertNotNull(lpd.toString());
			assertTrue(lpd.hasExportKvar());
			assertTrue(lpd.hasExportKW());
			assertTrue(lpd.hasImportKvar());
			assertFalse(lpd.hasImportKW());
			assertFalse(lpd.hasTotalKVA());
			assertEquals(3, lpd.getNrOfChannels());
			assertNotNull(lpd.toList());
			assertNotNull(lpd.toChannelInfoList());
			assertEquals(lpd.getNrOfChannels(), lpd.toList().size());
			assertEquals(lpd.getNrOfChannels(), lpd.toChannelInfoList().size());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testLoadProfileDefinition_0x03() {
		try {
			LoadProfileDefinition lpd = new LoadProfileDefinition(new byte[] { 0x03 });
			assertNotNull(lpd);
			assertNotNull(lpd.toString());
			assertFalse(lpd.hasExportKvar());
			assertTrue(lpd.hasExportKW());
			assertFalse(lpd.hasImportKvar());
			assertTrue(lpd.hasImportKW());
			assertFalse(lpd.hasTotalKVA());
			assertEquals(2, lpd.getNrOfChannels());
			assertNotNull(lpd.toList());
			assertNotNull(lpd.toChannelInfoList());
			assertEquals(lpd.getNrOfChannels(), lpd.toList().size());
			assertEquals(lpd.getNrOfChannels(), lpd.toChannelInfoList().size());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testLoadProfileDefinition_0x01() {
		try {
			LoadProfileDefinition lpd = new LoadProfileDefinition(new byte[] { 0x01 });
			assertNotNull(lpd);
			assertNotNull(lpd.toString());
			assertFalse(lpd.hasExportKvar());
			assertFalse(lpd.hasExportKW());
			assertFalse(lpd.hasImportKvar());
			assertTrue(lpd.hasImportKW());
			assertFalse(lpd.hasTotalKVA());
			assertEquals(1, lpd.getNrOfChannels());
			assertNotNull(lpd.toList());
			assertNotNull(lpd.toChannelInfoList());
			assertEquals(lpd.getNrOfChannels(), lpd.toList().size());
			assertEquals(lpd.getNrOfChannels(), lpd.toChannelInfoList().size());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testLoadProfileDefinition_0x00() {
		try {
			LoadProfileDefinition lpd = new LoadProfileDefinition(new byte[] { 0x00 });
			assertNotNull(lpd);
			assertNotNull(lpd.toString());
			assertFalse(lpd.hasExportKvar());
			assertFalse(lpd.hasExportKW());
			assertFalse(lpd.hasImportKvar());
			assertFalse(lpd.hasImportKW());
			assertFalse(lpd.hasTotalKVA());
			assertEquals(0, lpd.getNrOfChannels());
			assertNotNull(lpd.toList());
			assertNotNull(lpd.toChannelInfoList());
			assertEquals(lpd.getNrOfChannels(), lpd.toList().size());
			assertEquals(lpd.getNrOfChannels(), lpd.toChannelInfoList().size());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
