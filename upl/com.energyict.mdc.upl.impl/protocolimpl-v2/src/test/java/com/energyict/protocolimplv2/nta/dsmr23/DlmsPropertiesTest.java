package com.energyict.protocolimplv2.nta.dsmr23;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;

/**
 * Tests for {@link DlmsProperties}.
 * 
 * @author alex
 */
public final class DlmsPropertiesTest {

	/**
	 * Tests the {@link DlmsSessionProperties#PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED} property.
	 */
	@Test
	public final void testPublicClientPreEstablished() {
		final DlmsProperties props = new DlmsProperties();
		
		props.getProperties().setProperty(DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, true);
		
		assertThat(props.isPublicClientPreEstablished()).isTrue();
		
		props.getProperties().setProperty(DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, false);
		
		assertThat(props.isPublicClientPreEstablished()).isFalse();
		
		props.getProperties().removeProperty(DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED);
		
		assertThat(props.isPublicClientPreEstablished()).isFalse();
	}
}
