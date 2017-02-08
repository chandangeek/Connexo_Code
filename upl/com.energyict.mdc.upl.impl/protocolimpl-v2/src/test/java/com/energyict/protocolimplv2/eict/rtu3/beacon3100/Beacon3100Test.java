package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.crypto.KeyStoreService;
import com.energyict.mdc.upl.crypto.X509Service;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link Beacon3100} protocol.
 *
 * @author alex
 */
@RunWith(MockitoJUnitRunner.class)
public final class Beacon3100Test {

    /**
     * AARE.
     */
    private static final byte[] AARE = ProtocolTools.getBytesFromHexString("000100010010002B6129A109060760857405080101A203020100A305A103020100BE10040E0800065F1F040000000001000007", "");

    /**
     * AARE with the correct responding AP title.
     */
    private static final byte[] AARE_HMAC = ProtocolTools.getBytesFromHexString("00010001001000376135A109060760857405080101A203020100A305A103020100A40A0408AABBCCDDEEFFABDCBE10040E0800065F1F040000000000800007", "");

    /**
     * Get response when we are using a data object for the FC.
     */
    private static final byte[] GET_RESPONSE_DATA = ProtocolTools.getBytesFromHexString("0001000100100009C40143000600000C1C", "");

    /**
     * Release response.
     */
    private static final byte[] RLRE = ProtocolTools.getBytesFromHexString("00010001001000056303800100", "");

    /**
     * Invalid challenge response.
     */
    private static final byte[] CHALLENGE_RESPONSE_INVALID = ProtocolTools.getBytesFromHexString("000100010010002FC701430001000202092000000000000000000000000000000000000000000000000000000000000000000600000C1C", "");

    /**
     * AK used for the HMAC.
     */
    private static final String AK = "000102030405060708090A0B0C0D0E0F";

    /**
     * Client system title.
     */
    private static final byte[] CLIENT_SYSTEM_TITLE = ProtocolTools.getBytesFromHexString("45494354434F4D4D", "");

    /**
     * The server system title.
     */
    private static final byte[] SERVER_SYSTEM_TITLE = ProtocolTools.getBytesFromHexString("AABBCCDDEEFFABDC", "");

    /**
     * The HMAC response, prefix.
     */
    private static final byte[] HMAC_RESPONSE_PRE = ProtocolTools.getBytesFromHexString("000100010010002FC7014300010002020920", "");

    /**
     * The HMAC response, postfix.
     */
    private static final byte[] HMAC_RESPONSE_POST = ProtocolTools.getBytesFromHexString("0600000C1C", "");

    /**
     * The current response.
     */
    private byte[] currentResponse;

    /**
     * The current index.
     */
    private int currentIndex;

    /**
     * The HMAC FC response.
     */
    private byte[] hmacFcResponse;

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private IssueFactory issueFactory;
    @Mock
    private ObjectMapperService objectMapperService;
    @Mock
    private DeviceMasterDataExtractor deviceMasterDataExtractor;
    @Mock
    private KeyStoreService keyStoreService;
    @Mock
    private X509Service x509Service;
    @Mock
    private DeviceGroupExtractor deviceGroupExtractor;

    /**
     * Tests the read frame counter.
     */
    @Test
    public final void testReadFramecounterNonPreEstablishedStandard() {
        this.currentResponse = AARE;
        this.currentIndex = 0;

        final ComChannel channel = mock(ComChannel.class);
        when(channel.getComChannelType()).thenReturn(ComChannelType.SocketComChannel);

        when(channel.write(any(byte[].class))).thenAnswer(
                new Answer<Integer>() {
                    @Override
                    public Integer answer(final InvocationOnMock invocation) throws Throwable {
                        final byte[] request = (byte[]) invocation.getArguments()[0];

                        return request.length;
                    }
                }
        );

        doAnswer(new Answer<Integer>() {
            @Override
            public final Integer answer(final InvocationOnMock invocation) throws Throwable {
                final byte[] buffer = (byte[]) invocation.getArguments()[0];
                final int offset = (int) invocation.getArguments()[1];
                final int length = (int) invocation.getArguments()[2];

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

        final Beacon3100 protocol = new Beacon3100(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, objectMapperService, deviceMasterDataExtractor, this.deviceGroupExtractor, this.x509Service, this.keyStoreService);
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
    public final void testReadFramecounterStandardPreEstablished() {
        this.currentResponse = GET_RESPONSE_DATA;
        this.currentIndex = 0;

        final ComChannel channel = mock(ComChannel.class);
        when(channel.getComChannelType()).thenReturn(ComChannelType.SocketComChannel);

        when(channel.write(any(byte[].class))).thenAnswer(
                new Answer<Integer>() {
                    @Override
                    public Integer answer(final InvocationOnMock invocation) throws Throwable {
                        final byte[] request = (byte[]) invocation.getArguments()[0];

                        return request.length;
                    }
                }
        );

        doAnswer(new Answer<Integer>() {
            @Override
            public final Integer answer(final InvocationOnMock invocation) throws Throwable {
                final byte[] buffer = (byte[]) invocation.getArguments()[0];
                final int offset = (int) invocation.getArguments()[1];
                final int length = (int) invocation.getArguments()[2];

                for (int i = 0; i < length; i++) {
                    buffer[offset + i] = currentResponse[currentIndex];
                    currentIndex++;
                }

                return length;
            }

        }).when(channel).read(any(byte[].class), anyInt(), anyInt());

        final DeviceProtocolSecurityPropertySet securityProps = mock(DeviceProtocolSecurityPropertySet.class);
        when(securityProps.getSecurityProperties()).thenReturn(TypedProperties.empty());

        final Beacon3100 protocol = new Beacon3100(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, objectMapperService, deviceMasterDataExtractor, this.deviceGroupExtractor, this.x509Service, this.keyStoreService);
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
    @Test
    public final void testReadFramecounterNonPreEstablishedHMACInvalidChallengeResponse() {
        this.currentResponse = AARE_HMAC;
        this.currentIndex = 0;

        final ComChannel channel = mock(ComChannel.class);
        when(channel.getComChannelType()).thenReturn(ComChannelType.SocketComChannel);

        when(channel.write(any(byte[].class))).thenAnswer(
                new Answer<Integer>() {
                    @Override
                    public Integer answer(final InvocationOnMock invocation) throws Throwable {
                        final byte[] request = (byte[]) invocation.getArguments()[0];

                        return request.length;
                    }
                }
        );

        doAnswer(new Answer<Integer>() {
            @Override
            public final Integer answer(final InvocationOnMock invocation) throws Throwable {
                final byte[] buffer = (byte[]) invocation.getArguments()[0];
                final int offset = (int) invocation.getArguments()[1];
                final int length = (int) invocation.getArguments()[2];

                for (int i = 0; i < length; i++) {
                    buffer[offset + i] = currentResponse[currentIndex];
                    currentIndex++;

                    if (currentIndex == currentResponse.length) {
                        if (currentResponse == AARE_HMAC) {
                            currentResponse = CHALLENGE_RESPONSE_INVALID;
                        } else {
                            currentResponse = RLRE;
                        }

                        currentIndex = 0;
                    }
                }

                return length;
            }

        }).when(channel).read(any(byte[].class), anyInt(), anyInt());

        final DeviceProtocolSecurityPropertySet securityProps = mock(DeviceProtocolSecurityPropertySet.class);
        when(securityProps.getSecurityProperties()).thenReturn(TypedProperties.empty());

        final Beacon3100 protocol = new Beacon3100(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, objectMapperService, deviceMasterDataExtractor, this.deviceGroupExtractor, this.x509Service, this.keyStoreService);
        protocol.setSecurityPropertySet(securityProps);

        protocol.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(1);
        protocol.getDlmsSessionProperties().getProperties().setProperty(DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, Boolean.FALSE);
        protocol.getDlmsSessionProperties().getProperties().setProperty(Beacon3100ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, Boolean.TRUE);
        protocol.getDlmsSessionProperties().getProperties().setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), AK);

        assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(1);

        try {
            protocol.readFrameCounter(channel);

            fail("Should have failed because we replied with an invalid challenge response !");
        } catch (CommunicationException e) {
            // This is to be expected.
        }
    }

    /**
     * This is the only way you can actually use HMAC.
     */
    @Test
    public final void testReadFramecounterNonPreEstablishedHMAC() {
        this.currentResponse = AARE_HMAC;
        this.currentIndex = 0;

        final ComChannel channel = mock(ComChannel.class);
        when(channel.getComChannelType()).thenReturn(ComChannelType.SocketComChannel);

        when(channel.write(any(byte[].class))).thenAnswer(
                new Answer<Integer>() {
                    @Override
                    public Integer answer(final InvocationOnMock invocation) throws Throwable {
                        final byte[] request = (byte[]) invocation.getArguments()[0];

                        if (currentResponse == null && hmacFcResponse == null) {
                            final byte[] hmacChallenge = Arrays.copyOfRange(request, 23, request.length);
                            final byte[] challengeResponse = generateHmacResponse(hmacChallenge);

                            hmacFcResponse = new byte[HMAC_RESPONSE_PRE.length + challengeResponse.length + HMAC_RESPONSE_POST.length];

                            System.arraycopy(HMAC_RESPONSE_PRE, 0, hmacFcResponse, 0, HMAC_RESPONSE_PRE.length);
                            System.arraycopy(challengeResponse, 0, hmacFcResponse, HMAC_RESPONSE_PRE.length, challengeResponse.length);
                            System.arraycopy(HMAC_RESPONSE_POST, 0, hmacFcResponse, HMAC_RESPONSE_PRE.length + challengeResponse.length, HMAC_RESPONSE_POST.length);

                            currentResponse = hmacFcResponse;
                        }

                        return request.length;
                    }
                }
        );

        doAnswer(new Answer<Integer>() {
            @Override
            public final Integer answer(final InvocationOnMock invocation) throws Throwable {
                final byte[] buffer = (byte[]) invocation.getArguments()[0];
                final int offset = (int) invocation.getArguments()[1];
                final int length = (int) invocation.getArguments()[2];

                for (int i = 0; i < length; i++) {
                    buffer[offset + i] = currentResponse[currentIndex];
                    currentIndex++;

                    if (currentIndex == currentResponse.length) {
                        if (hmacFcResponse == null) {
                            currentResponse = null;
                        } else {
                            currentResponse = RLRE;
                        }

                        currentIndex = 0;
                    }
                }

                return length;
            }

        }).when(channel).read(any(byte[].class), anyInt(), anyInt());

        final DeviceProtocolSecurityPropertySet securityProps = mock(DeviceProtocolSecurityPropertySet.class);
        when(securityProps.getSecurityProperties()).thenReturn(TypedProperties.empty());

        final Beacon3100 protocol = new Beacon3100(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, objectMapperService, deviceMasterDataExtractor, this.deviceGroupExtractor, this.x509Service, this.keyStoreService);
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

    /**
     * Generates the HMAC frame counter response.
     *
     * @return The HMAC FC response.
     */
    private byte[] generateHmacResponse(final byte[] hmacChallenge) throws GeneralSecurityException {
        final byte[] hmacInput = new byte[SERVER_SYSTEM_TITLE.length + CLIENT_SYSTEM_TITLE.length + hmacChallenge.length + 4];

        final long fc = 3100;

        final byte[] fcBytes = new byte[4];

        fcBytes[0] = (byte) ((fc & 0xFF000000l) >> 24);
        fcBytes[1] = (byte) ((fc & 0xFF0000l) >> 16);
        fcBytes[2] = (byte) ((fc & 0xFF00l) >> 8);
        fcBytes[3] = (byte) (fc & 0xFF);

        System.arraycopy(SERVER_SYSTEM_TITLE, 0, hmacInput, 0, SERVER_SYSTEM_TITLE.length);
        System.arraycopy(CLIENT_SYSTEM_TITLE, 0, hmacInput, SERVER_SYSTEM_TITLE.length, CLIENT_SYSTEM_TITLE.length);
        System.arraycopy(hmacChallenge, 0, hmacInput, SERVER_SYSTEM_TITLE.length + CLIENT_SYSTEM_TITLE.length, hmacChallenge.length);
        System.arraycopy(fcBytes, 0, hmacInput, SERVER_SYSTEM_TITLE.length + CLIENT_SYSTEM_TITLE.length + hmacChallenge.length, fcBytes.length);

        // Perform the HMAC.
        final Key ak = new SecretKeySpec(ProtocolTools.getBytesFromHexString(AK, ""), "HmacSHA256");

        final Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(ak);

        return hmac.doFinal(hmacInput);
    }

    /**
     * Tests that the implementation uses non-pre-established even when configured differently when asked to use frame counter authentication.
     *
     * @throws Exception
     */
    @Test
    public final void testInvalidConfiguration() throws Exception {
        this.currentResponse = AARE_HMAC;
        this.currentIndex = 0;

        final ComChannel channel = mock(ComChannel.class);
        when(channel.getComChannelType()).thenReturn(ComChannelType.SocketComChannel);
        when(channel.write(any(byte[].class))).thenAnswer(
                new Answer<Integer>() {
                    @Override
                    public Integer answer(final InvocationOnMock invocation) throws Throwable {
                        final byte[] request = (byte[]) invocation.getArguments()[0];

                        if (currentResponse == null && hmacFcResponse == null) {
                            final byte[] hmacChallenge = Arrays.copyOfRange(request, 23, request.length);
                            final byte[] challengeResponse = generateHmacResponse(hmacChallenge);

                            hmacFcResponse = new byte[HMAC_RESPONSE_PRE.length + challengeResponse.length + HMAC_RESPONSE_POST.length];

                            System.arraycopy(HMAC_RESPONSE_PRE, 0, hmacFcResponse, 0, HMAC_RESPONSE_PRE.length);
                            System.arraycopy(challengeResponse, 0, hmacFcResponse, HMAC_RESPONSE_PRE.length, challengeResponse.length);
                            System.arraycopy(HMAC_RESPONSE_POST, 0, hmacFcResponse, HMAC_RESPONSE_PRE.length + challengeResponse.length, HMAC_RESPONSE_POST.length);

                            currentResponse = hmacFcResponse;
                        }

                        return request.length;
                    }
                }
        );

        doAnswer(new Answer<Integer>() {
            @Override
            public final Integer answer(final InvocationOnMock invocation) throws Throwable {
                final byte[] buffer = (byte[]) invocation.getArguments()[0];
                final int offset = (int) invocation.getArguments()[1];
                final int length = (int) invocation.getArguments()[2];

                for (int i = 0; i < length; i++) {
                    buffer[offset + i] = currentResponse[currentIndex];
                    currentIndex++;

                    if (currentIndex == currentResponse.length) {
                        if (hmacFcResponse == null) {
                            currentResponse = null;
                        } else {
                            currentResponse = RLRE;
                        }

                        currentIndex = 0;
                    }
                }

                return length;
            }

        }).when(channel).read(any(byte[].class), anyInt(), anyInt());

        final DeviceProtocolSecurityPropertySet securityProps = mock(DeviceProtocolSecurityPropertySet.class);
        when(securityProps.getSecurityProperties()).thenReturn(TypedProperties.empty());

        final Beacon3100 protocol = new Beacon3100(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, objectMapperService, deviceMasterDataExtractor, this.deviceGroupExtractor, this.x509Service, this.keyStoreService);
        protocol.setSecurityPropertySet(securityProps);

        protocol.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(1);
        protocol.getDlmsSessionProperties().getProperties().setProperty(DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED, Boolean.TRUE);
        protocol.getDlmsSessionProperties().getProperties().setProperty(Beacon3100ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, Boolean.TRUE);
        protocol.getDlmsSessionProperties().getProperties().setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), AK);

        assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(1);

        protocol.readFrameCounter(channel);

        // We return 3100 as the FC, so make sure we have set it.
        assertThat(protocol.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter()).isEqualTo(3101);
    }

}
