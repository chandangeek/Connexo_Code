package com.energyict.dlms.cosem;

import com.energyict.dlms.*;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCosemObjectTest {

    @Mock
    private DLMSConnection dlmsConnection;

    private AbstractCosemObject cosemObject;

    @Test
    public void CheckCosemPDUResponseHeaderTest() {

        String iskraResponse = "100018c701C1000100091048bdb63e5df88885d67330f89ea444bd";
        String iskraResponseFailure = "100005c701c10b00";
        String z3Response = "010016c701c100091091ec1f928d8e6ace9823717c15c61271";
        String z3ResponseFailure = "010005c701c10103";


        Data data = new Data(null, null);
        try {

            DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
            ProtocolLink protocolLink = new MockProtocolLink(new MockDLMSConnection(), meterConfig);
            data = new Data(protocolLink, null);
            assertArrayEquals(DLMSUtils.hexStringToByteArray("091048bdb63e5df88885d67330f89ea444bd"), data.checkCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(iskraResponse)));

            try {
                data.checkCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(iskraResponseFailure));
            } catch (DataAccessResultException e) {
                if (!e.getMessage().equalsIgnoreCase("Cosem Data-Access-Result exception Object unavailable")) {
                    e.printStackTrace();
                    fail();
                }
            }

            meterConfig = DLMSMeterConfig.getInstance("WKP::OLD");
            protocolLink = new MockProtocolLink(new MockDLMSConnection(), meterConfig);
            data = new Data(protocolLink, null);
            assertArrayEquals(DLMSUtils.hexStringToByteArray("091091ec1f928d8e6ace9823717c15c61271"), data.checkCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(z3Response)));


            try {
                data.checkCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(z3ResponseFailure));
            } catch (DataAccessResultException e) {
                if (!e.getMessage().equalsIgnoreCase("Cosem Data-Access-Result exception R/W denied")) {
                    e.printStackTrace();
                    fail();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public final void ConfirmedServiceErrorTest() {

        String confirmedServiceErrorResponseEncryptionFailed = "1000050E060006";

        Data data = new Data(null, null);

        DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
        ProtocolLink protocolLink = new MockProtocolLink(new MockDLMSConnection(), meterConfig);
        data = new Data(protocolLink, null);
        try {
            data.checkCosemPDUResponseHeader(DLMSUtils.hexStringToByteArray(confirmedServiceErrorResponseEncryptionFailed));
        } catch (Exception e) {
            assertTrue(e instanceof IOException);
            if (!e.getMessage().equalsIgnoreCase("Confirmed Service Error - 'Write error' - Reason: " + ServiceError.APP_REFERENCE_CIPHER_ERROR.getDescription())) {
                e.printStackTrace();
                fail();
            }
        }
    }

    @Test
    public final void sendAndReceiveValidResponseTestMatchingInvokeIds() throws IOException {
        cosemObject = Mockito.mock(AbstractCosemObject.class, Mockito.CALLS_REAL_METHODS);
        cosemObject.setInvokeIdAndPriorityHandler(new IncrementalInvokeIdAndPriorityHandler());
        doReturn(Mockito.mock(DLMSConnection.class)).when(cosemObject).getDLMSConnection();

        // return correct invoke id
        doReturn((byte) 0x41).when(cosemObject).extractInvokeIdFromResponse(any(byte[].class));

        cosemObject.sendAndReceiveValidResponse(new byte[0]);

        assertEquals("Response was valid, so expecting 0.", 0, cosemObject.getNrOfInvalidResponseFrames());
    }

    @Test(expected = IOException.class)
    public final void sendAndReceiveValidResponseTestAlwaysNonMatchingInvokeIds() throws IOException {
        cosemObject = Mockito.mock(AbstractCosemObject.class, Mockito.CALLS_REAL_METHODS);
        cosemObject.setInvokeIdAndPriorityHandler(new IncrementalInvokeIdAndPriorityHandler());
        DLMSConnection dlmsConnection = Mockito.mock(DLMSConnection.class);
        doReturn(dlmsConnection).when(cosemObject).getDLMSConnection();
        doReturn(new byte[0]).when(dlmsConnection).sendRequest(any(byte[].class));

        // return always invalid invoke id
        doReturn((byte) 0x00).when(cosemObject).extractInvokeIdFromResponse(any(byte[].class));

        try {
            cosemObject.sendAndReceiveValidResponse(new byte[0]);
        } catch (IOException e) {
            assertEquals("Response always invalid, expecting maximum number of invalid responses.", AbstractCosemObject.MAX_NR_OF_INVOKE_ID_MISMATCH, cosemObject.getNrOfInvalidResponseFrames());
            throw e;
        }
    }

    @Test
    public final void sendAndReceiveValidResponseTestNonMatchingInvokeIds() throws IOException {
        cosemObject = Mockito.mock(AbstractCosemObject.class, Mockito.CALLS_REAL_METHODS);
        cosemObject.setInvokeIdAndPriorityHandler(new IncrementalInvokeIdAndPriorityHandler());
        DLMSConnection dlmsConnection = Mockito.mock(DLMSConnection.class);
        doReturn(dlmsConnection).when(cosemObject).getDLMSConnection();
        doReturn(new byte[0]).when(dlmsConnection).sendRequest(any(byte[].class));

        // return always invalid invoke id
        when(cosemObject.extractInvokeIdFromResponse(any(byte[].class))).thenReturn((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x41);

        cosemObject.sendAndReceiveValidResponse(new byte[0]);
        assertEquals("Response valid 4th time.", 3, cosemObject.getNrOfInvalidResponseFrames());
    }
}