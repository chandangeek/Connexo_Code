package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.InboundFrameException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 14:55
 * Author: khe
 */
public class ACE4000Connection {

    private static final String START = "<MPush>";
    private ComChannel comChannel;
    private ACE4000 ace4000;
    private boolean inbound;

    public ACE4000Connection(ComChannel comChannel, ACE4000 ace4000, boolean inbound) {
        this.comChannel = comChannel;
        this.ace4000 = ace4000;
        this.inbound = inbound;
    }

    /**
     * Send out a request to the device
     */
    public void write(byte[] frame) {
        comChannel.startWriting();
        comChannel.write(frame);
    }

    /**
     * If mustKeepListening is true, keep reading until a timeout occurs, and return the result (or throw an exception if nothing was received)
     * If mustKeepListening is false, return the result immediately, don't wait for possible next frames
     */
    public List<String> readFrames(boolean mustKeepListening) {

        // keep reading until you get no data for [timeout] period
        int timeout = ace4000.getProperties().getTimeout();
        long timeoutMoment = getCurrentSystemTime() + timeout;
        StringBuilder msg = new StringBuilder();
        comChannel.startReading();

        while (true) {
            if (comChannel.available() > 0) {
                while (comChannel.available() > 0) {
                    delay(10);  //Wait for full frame
                    byte[] buffer = new byte[comChannel.available()];
                    comChannel.read(buffer);
                    for (byte b : buffer) {
                        msg.append((char) (b & 0xFF));
                    }
                }
                if (!mustKeepListening) {    //Return single frame, don't wait for timeout
                    return splitConcatenatedFrames(msg.toString());
                } else {
                    //Update the timeout moment, to make sure that we listen long enough
                    timeoutMoment = getCurrentSystemTime() + timeout;
                }
            } else {
                delay(100);
            }
            if (getCurrentSystemTime() - timeoutMoment > 0) {
                if (msg.toString().isEmpty()) {
                    if (inbound) {
                        throw InboundFrameException.timeoutException(String.format("Timeout: didn't receive an inbound frame after %d ms.", timeout));
                    } else {
                        IOException cause = new IOException(String.format("Timeout: didn't receive an outbound frame after %d ms.", timeout));
                        throw ConnectionCommunicationException.numberOfRetriesReached(cause, ace4000.getProperties().getRetries() + 1);
                    }
                } else {
                    return splitConcatenatedFrames(msg.toString());    //Return the received frames after waiting for a timeout
                }
            }
        }
    }

    private List<String> splitConcatenatedFrames(String xml) {
        List<String> receivedFrames = new ArrayList<>();
        String[] messages = xml.split(START);              //Split concatenated messages
        for (String message : messages) {
            if (!"".equals(message)) {
                receivedFrames.add(START + message);
            }
        }
        return receivedFrames;
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    public long getCurrentSystemTime() {
        return System.currentTimeMillis();
    }
}