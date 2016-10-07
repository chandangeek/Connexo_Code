package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

/**
 * Tests for the {@link Beacon3100} protocol.
 * 
 * @author alex
 */
public final class Beacon3100Test {
	
	/** AARE. */
	private static final byte[] AARE = ProtocolTools.getBytesFromHexString("000100010010002B6129A109060760857405080101A203020100A305A103020100BE10040E0800065F1F040000000001000007", "");

	/** AARE with the correct responding AP title. */
	private static final byte[] AARE_HMAC = ProtocolTools.getBytesFromHexString("00010001001000376135A109060760857405080101A203020100A305A103020100A40A0408AABBCCDDEEFFABDCBE10040E0800065F1F040000000000800007", "");
	
	/** Get response when we are using a data object for the FC. */
	private static final byte[] GET_RESPONSE_DATA = ProtocolTools.getBytesFromHexString("0001000100100009C40143000600000C1C", "");
	
	/** Release response. */
	private static final byte[] RLRE = ProtocolTools.getBytesFromHexString("00010001001000056303800100", "");
	
	/** AK used for the HMAC. */
	private static final String AK = "000102030405060708090A0B0C0D0E0F";
	
	/** Client system title. */
	private static final byte[] CLIENT_SYSTEM_TITLE = ProtocolTools.getBytesFromHexString("45494354434F4D4D", "");
	
	/** The server system title. */
	private static final byte[] SERVER_SYSTEM_TITLE = ProtocolTools.getBytesFromHexString("AABBCCDDEEFFABDC", "");
	
	/** The current response. */
	private byte[] currentResponse;
	
	/** The current index. */
	private int currentIndex;
	
	/** The challenge. */
	private byte[] hmacChallenge;
	
	/**
	 * Tests the read frame counter.
	 */
	@Test
	public final void testReadFramecounterNonPreEstablishedStandard() {
		this.currentResponse = AARE;
		this.currentIndex = 0;
		
		final TypedProperties channelProperties = TypedProperties.empty();
		channelProperties.setProperty(ComChannelType.TYPE, ComChannelType.SocketComChannel.getType());
		
		final ComChannel channel = mock(ComChannel.class);
		
		when(channel.getProperties()).thenReturn(channelProperties);
		when(channel.write(any(byte[].class))).thenAnswer(
			new Answer<Integer>() {
				@Override
				public Integer answer(final InvocationOnMock invocation) throws Throwable {
					final byte[] request = (byte[])invocation.getArguments()[0];
					
					return request.length;
				}
			}
		);
		
		doAnswer(new Answer<Integer>() {
			@Override
			public final Integer answer(final InvocationOnMock invocation) throws Throwable {
				final byte[] buffer = (byte[])invocation.getArguments()[0];
				final int offset = (int)invocation.getArguments()[1];
				final int length = (int)invocation.getArguments()[2];
				
				for (int i = 0; i < length; i++) {
					buffer[offset + i] = currentResponse[currentIndex];
					currentIndex++;
				}
				
				if (currentIndex == currentResponse.length) {
					if (currentResponse == AARE) {
						currentResponse = GET_RESPONSE_DATA;
					} else if (currentResponse == GET_RESPONSE_DATA) {
						currentResponse = RLRE;
					}
					
					currentIndex = 0;
				}
				
				return length;
			}
			
		}).when(channel).read(any(byte[].class), anyInt(), anyInt());
		
		final DeviceProtocolSecurityPropertySet securityProps = mock(DeviceProtocolSecurityPropertySet.class);
		when(securityProps.getSecurityProperties()).thenReturn(TypedProperties.empty());
		
		final Beacon3100 protocol = new Beacon3100();
		protocol.setSecurityPropertySet(securityProps);
		
		protocol.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(1);
		
		assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(1);
		
		protocol.readFrameCounter(channel);
		
		// We return 3100 as the FC, so make sure we have set it.
		assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(3101);
	}
	
	/**
	 * Tests the readout of the frame counter, pre-established.
	 */
	@Test
	public final void testReadFrameCounterStandardPreEstablished() {
		this.currentResponse = GET_RESPONSE_DATA;
		this.currentIndex = 0;
		
		final TypedProperties channelProperties = TypedProperties.empty();
		channelProperties.setProperty(ComChannelType.TYPE, ComChannelType.SocketComChannel.getType());
		
		final ComChannel channel = mock(ComChannel.class);
		
		when(channel.getProperties()).thenReturn(channelProperties);
		when(channel.write(any(byte[].class))).thenAnswer(
			new Answer<Integer>() {
				@Override
				public Integer answer(final InvocationOnMock invocation) throws Throwable {
					final byte[] request = (byte[])invocation.getArguments()[0];
					
					return request.length;
				}
			}
		);
		
		doAnswer(new Answer<Integer>() {
			@Override
			public final Integer answer(final InvocationOnMock invocation) throws Throwable {
				final byte[] buffer = (byte[])invocation.getArguments()[0];
				final int offset = (int)invocation.getArguments()[1];
				final int length = (int)invocation.getArguments()[2];
				
				for (int i = 0; i < length; i++) {
					buffer[offset + i] = currentResponse[currentIndex];
					currentIndex++;
				}
				
				return length;
			}
			
		}).when(channel).read(any(byte[].class), anyInt(), anyInt());
		
		final DeviceProtocolSecurityPropertySet securityProps = mock(DeviceProtocolSecurityPropertySet.class);
		when(securityProps.getSecurityProperties()).thenReturn(TypedProperties.empty());
		
		final Beacon3100 protocol = new Beacon3100();
		protocol.setSecurityPropertySet(securityProps);
		
		protocol.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(1);
		protocol.getDlmsSessionProperties().getProperties().setProperty(DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, Boolean.TRUE);
		
		assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(1);
		
		protocol.readFrameCounter(channel);
		
		// We return 3100 as the FC, so make sure we have set it.
		assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(3101);
	}
	
	/**
	 * This is the only way you can actually use HMAC.
	 */
	public final void testReadFramecounterNonPreEstablishedHMAC() {
		this.currentResponse = AARE_HMAC;
		this.currentIndex = 0;
		
		final TypedProperties channelProperties = TypedProperties.empty();
		channelProperties.setProperty(ComChannelType.TYPE, ComChannelType.SocketComChannel.getType());
		
		final ComChannel channel = mock(ComChannel.class);
		
		when(channel.getProperties()).thenReturn(channelProperties);
		when(channel.write(any(byte[].class))).thenAnswer(
			new Answer<Integer>() {
				@Override
				public Integer answer(final InvocationOnMock invocation) throws Throwable {
					final byte[] request = (byte[])invocation.getArguments()[0];
					
					System.out.println(ProtocolTools.getHexStringFromBytes(request, ""));
					
					return request.length;
				}
			}
		);
		
		doAnswer(new Answer<Integer>() {
			@Override
			public final Integer answer(final InvocationOnMock invocation) throws Throwable {
				final byte[] buffer = (byte[])invocation.getArguments()[0];
				final int offset = (int)invocation.getArguments()[1];
				final int length = (int)invocation.getArguments()[2];
				
				for (int i = 0; i < length; i++) {
					buffer[offset + i] = currentResponse[currentIndex];
					currentIndex++;
				}
				
				return length;
			}
			
		}).when(channel).read(any(byte[].class), anyInt(), anyInt());
		
		final DeviceProtocolSecurityPropertySet securityProps = mock(DeviceProtocolSecurityPropertySet.class);
		when(securityProps.getSecurityProperties()).thenReturn(TypedProperties.empty());
		
		final Beacon3100 protocol = new Beacon3100();
		protocol.setSecurityPropertySet(securityProps);
		
		protocol.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(1);
		protocol.getDlmsSessionProperties().getProperties().setProperty(DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, Boolean.FALSE);
		protocol.getDlmsSessionProperties().getProperties().setProperty(Beacon3100ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, Boolean.TRUE);
		protocol.getDlmsSessionProperties().getProperties().setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), AK);
		
		assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(1);
		
		protocol.readFrameCounter(channel);
		
		// We return 3100 as the FC, so make sure we have set it.
		assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(3101);
	}
	
}
