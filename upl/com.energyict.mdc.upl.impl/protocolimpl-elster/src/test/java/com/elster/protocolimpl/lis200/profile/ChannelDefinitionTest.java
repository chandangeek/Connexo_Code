package com.elster.protocolimpl.lis200.profile;

import com.elster.protocolimpl.lis200.ChannelDefinition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChannelDefinitionTest {

	@Test
	public void testChannelInfo1() {
		ChannelDefinition ci = new ChannelDefinition("CHN0", 1);
		assertEquals(0, ci.getChannelNo());
		assertEquals("I", ci.getChannelType());
		assertEquals(1, ci.getArchiveColumn());
	}		
		
	@Test
	public void testChannelInfo2() {
		ChannelDefinition ci = new ChannelDefinition("CHN002", 0);
		assertEquals(2, ci.getChannelNo());
		assertEquals("I", ci.getChannelType());
	}		
	@Test
	public void testChannelInfo3() {
		ChannelDefinition ci = new ChannelDefinition("CHN009[C]", 0);
		assertEquals(9, ci.getChannelNo());
		assertEquals("C", ci.getChannelType());
		assertEquals(9, ci.getChannelOv());
	}
	
	@Test
	public void testChannelInfo4() {
		ChannelDefinition ci = new ChannelDefinition("CHN007[C7]", 0);
		assertEquals(7, ci.getChannelNo());
		assertEquals("C", ci.getChannelType());
		assertEquals(7, ci.getChannelOv());
	}

	@Test(expected=NumberFormatException.class)
    public void testNumberFormatException() {
		@SuppressWarnings("unused")
		ChannelDefinition ci = new ChannelDefinition("CHN", 0);
    }	
}
