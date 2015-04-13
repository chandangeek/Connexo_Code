package com.energyict.dlms.cosem.generalblocktransfer;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author sva
 * @since 27/03/2015 - 14:26
 */
public class GeneralBlockTransferHandlerTest {

    /**
     * Test scenario to test a successful general block transfer flow<br/>
     * Schematic view of the flow (window size = 5): (R= received, S= sent)
     * <ul>
     * <li>R> BN: 1, BNA: 0, not streaming</li>
     * <li>S> BN: 1, BNA: 1, last block, not streaming</li>
     * <li>R> BN: 2, BNA: 1, streaming</li>
     * <li>R> BN: 3, BNA: 1, streaming</li>
     * <li>R> BN: 4, BNA: 1, streaming</li>
     * <li>R> BN: 5, BNA: 1, streaming</li>
     * <li>R> BN: 6, BNA: 1, not last block, not streaming</li>
     * <li>S> BN: 2, BNA: 6, last block, not streaming</li>
     * <li>R> BN: 7, BNA: 2, streaming</li>
     * <li>R> BN: 8, BNA: 2, streaming</li>
     * <li>R> BN: 9, BNA: 2, last block, not streaming</li>
     * </ul>
     */
    @Test
    public void testHandleSuccessfulGeneralBlockTransfer() throws Exception {
        final byte[] rawResponse = ProtocolTools.getBytesFromHexString("e00500010000820002c402", "");
        final byte[] requestNextBlockData_1 = ProtocolTools.getBytesFromHexString("e0850001000100", "");
        final byte[] requestNextBlockData_2 = ProtocolTools.getBytesFromHexString("e0850002000600", "");
        final byte[] expectedApduResponseData = ProtocolTools.getBytesFromHexString("c4020102030405060708090A0B0C0D0E0F", "");

        List<byte[]> responses = new ArrayList<>();
        responses.add(ProtocolTools.getBytesFromHexString("e045000200018200020102", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000300018200020304", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000400018200020506", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000500018200020708", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e00500060001820002090A", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000700028200020B0C", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000800028200020D0E", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e085000900028200010F", ""));
        TestDlmsConnection dlmsConnection = new TestDlmsConnection(responses);

        GeneralBlockTransferHandler handler = new GeneralBlockTransferHandler(dlmsConnection, Logger.getAnonymousLogger());
        handler.setUseLegacyHDLCHeader(false);

        // Business methods
        byte[] apduResponse = handler.handleGeneralBlockTransfer(rawResponse, new byte[0], false);

        // Asserts
        assertArrayEquals(requestNextBlockData_1, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(requestNextBlockData_2, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(expectedApduResponseData, apduResponse);
    }


    /**
     * Test scenario to test a successful general block transfer flow<br/>
     * In this test, the responses coming from the device will contain the GBT block and also already the beginning of next GBT block
     */
    @Test
    public void testHandleSuccessfulGeneralBlockTransferWithAdditionalData() throws Exception {
        final byte[] rawResponse = ProtocolTools.getBytesFromHexString("e00500010000820002c402", "");
        final byte[] requestNextBlockData_1 = ProtocolTools.getBytesFromHexString("e0850001000100", "");
        final byte[] requestNextBlockData_2 = ProtocolTools.getBytesFromHexString("e0850002000600", "");
        final byte[] expectedApduResponseData = ProtocolTools.getBytesFromHexString("c4020102030405060708090A0B0C0D0E0F", "");

        List<byte[]> responses = new ArrayList<>();
        responses.add(ProtocolTools.getBytesFromHexString("e045000200018200020102e045", ""));
        responses.add(ProtocolTools.getBytesFromHexString("000300018200020304e045", ""));
        responses.add(ProtocolTools.getBytesFromHexString("000400018200020506e045", ""));
        responses.add(ProtocolTools.getBytesFromHexString("000500018200020708e005", ""));
        responses.add(ProtocolTools.getBytesFromHexString("00060001820002090A", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000700028200020B0Ce045", ""));
        responses.add(ProtocolTools.getBytesFromHexString("000800028200020D0Ee085", ""));
        responses.add(ProtocolTools.getBytesFromHexString("000900028200010F", ""));
        TestDlmsConnection dlmsConnection = new TestDlmsConnection(responses);

        GeneralBlockTransferHandler handler = new GeneralBlockTransferHandler(dlmsConnection, Logger.getAnonymousLogger());
        handler.setUseLegacyHDLCHeader(false);

        // Business methods
        byte[] apduResponse = handler.handleGeneralBlockTransfer(rawResponse, new byte[0], false);

        // Asserts
        assertArrayEquals(requestNextBlockData_1, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(requestNextBlockData_2, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(expectedApduResponseData, apduResponse);
    }

    /**
     * Test scenario to test a general block transfer flow, including a retry for a missing block<br/>
     * Schematic view of the flow (window size = 5): (R= received, S= sent)
     * <ul>
     * <li>R> BN: 1, BNA: 0, not streaming</li>
     * <li>S> BN: 1, BNA: 1, last block, not streaming</li>
     * <li>R> BN: 2, BNA: 1, streaming</li>
     * <li>R> BN: 3, BNA: 1, streaming</li>
     * <li>R> BN: 5, BNA: 1, streaming</li>
     * <li>R> BN: 6, BNA: 1, not last block, not streaming</li>
     * <li>## BLock 4 is missing > fetch missing bock</li>
     * <li>## S> BN 2, BNA: 3, last block, not streaming</li>
     * <li>## R> BN 4, BNA 2, last block, not streaming </li>
     * <li>## End of missing block fetch</li>
     * <li>S> BN: 3, BNA: 6, last block, not streaming</li>
     * <li>R> BN: 7, BNA: 3, streaming</li>
     * <li>R> BN: 8, BNA: 3, streaming</li>
     * <li>R> BN: 9, BNA: 3, last block, not streaming</li>
     * </ul>
     */
    @Test
    public void testHandleGeneralBlockTransferHavingMissingBlock() throws Exception {
        final byte[] rawResponse = ProtocolTools.getBytesFromHexString("e00500010000820002c402", "");
        final byte[] requestNextBlockData_1 = ProtocolTools.getBytesFromHexString("e0850001000100", "");
        final byte[] requestNextBlockData_2 = ProtocolTools.getBytesFromHexString("e0850002000600", "");
        final byte[] requestMissingBlockData = ProtocolTools.getBytesFromHexString("e0810002000300", "");
        final byte[] expectedApduResponseData = ProtocolTools.getBytesFromHexString("c4020102030405060708090A0B0C0D0E0F", "");

        List<byte[]> responses = new ArrayList<>();
        responses.add(ProtocolTools.getBytesFromHexString("e045000200018200020102", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000300018200020304", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000500018200020708", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e00500060001820002090A", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e001000400028200020506", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e00500040003820002090A", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000700038200020B0C", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000800038200020D0E", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e085000900038200010F", ""));
        TestDlmsConnection dlmsConnection = new TestDlmsConnection(responses);

        GeneralBlockTransferHandler handler = new GeneralBlockTransferHandler(dlmsConnection, Logger.getAnonymousLogger());
        handler.setUseLegacyHDLCHeader(false);

        // Business methods
        byte[] apduResponse = handler.handleGeneralBlockTransfer(rawResponse, new byte[0], false);

        // Asserts
        assertArrayEquals(requestNextBlockData_1, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(requestMissingBlockData, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(requestNextBlockData_2, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(expectedApduResponseData, apduResponse);
    }

    /**
     *  Test scenario to test a general block transfer flow, including a retry for blocks<br/>
     * Schematic view of the flow (window size = 5): (R= received, S= sent)
     * <ul>
     * <li>R> BN: 1, BNA: 0, not streaming</li>
     * <li>S> BN: 1, BNA: 1, last block, not streaming</li>
     * <li>R> BN: 2, BNA: 1, streaming</li>
     * <li>R> BN: 3, BNA: 1, streaming</li>
     * <li>R> BN: 4, BNA: 1, streaming</li>
     * <li>R> BN: invalid response - blocks 5 and 6 not received</li>
     * <li>S> BN: 2, BNA: 4, last block, not streaming</li>
     * <li>R> BN: 5, BNA: 2, streaming</li>
     * <li>S> BN: 3, BNA: 5, last block, not streaming</li>
     * <li>R> BN: 6, BNA: 3, not last block, not streaming</li>
     * <li>S> BN: 4, BNA: 6, last block, not streaming</li>
     * <li>R> BN: 7, BNA: 4, streaming</li>
     * <li>R> BN: 8, BNA: 4, streaming</li>
     * <li>R> BN: 9, BNA: 4, last block, not streaming</li>
     * </ul>
     */
    @Test
    public void testHandleGeneralBlockTransferHavingMissingLastBlock() throws Exception {
        final byte[] rawResponse = ProtocolTools.getBytesFromHexString("e00500010000820002c402", "");
        final byte[] requestNextBlockData_1 = ProtocolTools.getBytesFromHexString("e0850001000100", "");
        final byte[] requestLostBlockRecovery_1 = ProtocolTools.getBytesFromHexString("e0810002000400", "");
        final byte[] requestLostBlockRecovery_2 = ProtocolTools.getBytesFromHexString("e0810003000500", "");
        final byte[] requestNextBlockData_2 = ProtocolTools.getBytesFromHexString("e0850004000600", "");
        final byte[] expectedApduResponseData = ProtocolTools.getBytesFromHexString("c4020102030405060708090A0B0C0D0E0F", "");

        List<byte[]> responses = new ArrayList<>();
        responses.add(ProtocolTools.getBytesFromHexString("e045000200018200020102", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000300018200020304", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000400018200020506", ""));
        responses.add(ProtocolTools.getBytesFromHexString("0000000000000000000000", ""));   //Invalid response
        responses.add(ProtocolTools.getBytesFromHexString("e005000500028200020708", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e00500060003820002090A", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000700048200020B0C", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e045000800048200020D0E", ""));
        responses.add(ProtocolTools.getBytesFromHexString("e085000900048200010F", ""));
        TestDlmsConnection dlmsConnection = new TestDlmsConnection(responses);

        GeneralBlockTransferHandler handler = new GeneralBlockTransferHandler(dlmsConnection, Logger.getAnonymousLogger());
        handler.setUseLegacyHDLCHeader(false);

        // Business methods
        byte[] apduResponse = handler.handleGeneralBlockTransfer(rawResponse, new byte[0], false);

        // Asserts
        assertArrayEquals(requestNextBlockData_1, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(requestLostBlockRecovery_1, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(requestLostBlockRecovery_2, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(requestNextBlockData_2, dlmsConnection.getRequestQueue().poll());
        assertArrayEquals(expectedApduResponseData, apduResponse);
    }

    private class TestDlmsConnection implements DlmsV2Connection {

        private static final int WINDOW_SIZE = 5;
        private static final boolean USE_GBT = true;

        private final Queue<byte[]> requestQueue;
        private final Queue<byte[]> responseQueue;

        private int maxRetries = 3;

        public TestDlmsConnection(List<byte[]> responses) throws InterruptedException {
            this.requestQueue = new LinkedList<>();
            this.responseQueue = new LinkedList<>();
            for (byte[] response : responses) {
                responseQueue.add(response);
            }
        }

        @Override
        public void sendUnconfirmedRequest(byte[] request) {
            requestQueue.add(request);  // Put the request on the queue
        }

        @Override
        public byte[] readResponseWithRetries(byte[] retryRequest) {
            return responseQueue.poll(); // Take the next response from the queue
        }

        public Queue<byte[]> getRequestQueue() {
            return requestQueue;
        }

        public Queue<byte[]> getResponseQueue() {
            return responseQueue;
        }

        @Override
        public void setRetries(int retries) {
            this.maxRetries = retries;
        }

        @Override
        public int getMaxRetries() {
            return maxRetries;
        }

        @Override
        public int getMaxTries() {
            return getMaxRetries() + 1;
        }

        @Override
        public boolean useGeneralBlockTransfer() {
            return USE_GBT;
        }

        @Override
        public int getGeneralBlockTransferWindowSize() {
            return WINDOW_SIZE;
        }

        @Override
        public void prepareComChannelForReceiveOfNextPacket() {
        }

        // Unsupported methods
        @Override
        public void connectMAC() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disconnectMAC() {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] sendRequest(byte[] request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] sendRequest(byte[] request, boolean isAlreadyEncrypted) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId, int hhuSignonBaudRateCode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HHUSignOn getHhuSignOn() {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] sendRawBytes(byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTimeout(long timeout) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTimeout() {
            throw new UnsupportedOperationException();
        }
    }
}