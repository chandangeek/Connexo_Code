package com.energyict.protocols.mdc.inbound.general;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ComServerExecutionException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.exceptions.InboundFrameException;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocols.mdc.inbound.general.frames.AbstractInboundFrame;
import com.energyict.protocols.mdc.inbound.general.frames.DeployFrame;
import com.energyict.protocols.mdc.inbound.general.frames.EventFrame;
import com.energyict.protocols.mdc.inbound.general.frames.EventPOFrame;
import com.energyict.protocols.mdc.inbound.general.frames.RegisterFrame;
import com.energyict.protocols.mdc.inbound.general.frames.RequestFrame;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Date: 25/06/12
 * Time: 16:22
 * Author: khe
 */
public class InboundConnection {

    private static final String REQUEST_TAG = "</REQUEST>";
    private static final String EVENT_TAG = "</EVENT>";
    private static final String EVENTPO_TAG = "</EVENTPO>";
    private static final String REGISTER_TAG = "</REGISTER>";
    private static final String DEPLOY_TAG = "</DEPLOY>";
    private static final String EVENTPO = "EVENTPO";
    private static final int DEFAULT_DELAY_MILLIS = 10;

    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final Clock clock;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private IdentificationService identificationService;
    private ComChannel comChannel;
    private int timeout;
    private int retries;

    /**
     * Contains the received frame(s) that should be ACK'ed after finding the Device in the database
     */
    private List<AbstractInboundFrame> framesToAck;

    public InboundConnection(ComChannel comChannel, int timeout, int retries, Clock clock, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, Thesaurus thesaurus, IdentificationService identificationService) {
        this.comChannel = comChannel;
        this.timeout = timeout;
        this.retries = retries;
        this.clock = clock;
        this.issueService = issueService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.identificationService = identificationService;
    }

    /**
     * Create and send an acknowledgement for the received frame.
     * E.g. acknowledgement for a request frame is <REQUEST>OK</REQUEST>
     * Don't send an acknowledgement for an eventPO frame, since this frame is send if the meter lost its power...
     *
     * @param fullFrame the received frame
     */
    public void sendAck(AbstractInboundFrame fullFrame) {
        String frameTag = getFrameTag(fullFrame.getFrame());
        if (!frameTag.equalsIgnoreCase(EVENTPO)) {
            send(createAck(frameTag));
        }
    }

    /**
     * Send a single "I" request, read and return the response
     */
    public String sendIRequestAndReadResponse() throws InboundTimeOutException {
        send("I".getBytes());
        return readVariableFrame();
    }

    /**
     * Send a double "I" request, read and return the response
     */
    public String sendDoubleIRequestAndReadResponse() throws InboundTimeOutException {
        send("II\r\n".getBytes());
        return readVariableFrame();
    }

    /**
     * Read in a frame after the I request was sent.
     * This method reads in bytes until a timeout occurs and returns the result.
     *
     * @throws ComServerExecutionException
     *          when no bytes were received after a certain time
     */
    public String readVariableFrame() throws InboundTimeOutException {
        return readVariableLength(null, null);   //Read in a frame until a timeout occurs
    }

    public AbstractInboundFrame readAndAckInboundFrame() throws InboundTimeOutException {
        AbstractInboundFrame inboundFrame = readInboundFrame(null);
        shouldAck(inboundFrame);
        return inboundFrame;
    }

    private void shouldAck(AbstractInboundFrame inboundFrame) {
        if (framesToAck == null) {
            framesToAck = new ArrayList<>();
        }
        framesToAck.add(inboundFrame);
    }

    /**
     * Acknowledge all received frames, after the Device has been found in the database
     */
    public void ackFrames() {
        framesToAck.forEach(this::sendAck);
    }

    /**
     * Read in a full inbound frame. Structure is <TAG>message</TAG>
     *
     * @param retryRequest this is what we send in case of timeouts. Nothing is sent if the request is null.
     * @return the full inbound frame
     * @throws ComServerExecutionException
     *          if a timeout occurs
     */
    public AbstractInboundFrame readInboundFrame(byte[] retryRequest) throws InboundTimeOutException {
        StringBuilder sb = new StringBuilder();
        String partialFrame = readVariableLength("</", retryRequest);

        String startTag = getFrameTag(partialFrame);
        String endTag = readFixedLength(startTag.length() + 1, null);

        sb.append(partialFrame);
        sb.append(endTag);

        return parseInboundFrame(sb.toString());
    }

    /**
     * Parse the received string contents
     *
     * @param frame the received string
     * @return the parsing result
     * @throws ComServerExecutionException
     *          when an unknown frame type was received
     */
    private AbstractInboundFrame parseInboundFrame(String frame) {
        if (frame.contains(REQUEST_TAG)) {
            return new RequestFrame(frame, issueService, identificationService);
        }
        if (frame.contains(EVENT_TAG)) {
            return new EventFrame(frame, issueService, thesaurus, identificationService, collectedDataFactory, meteringService);
        }
        if (frame.contains(EVENTPO_TAG)) {
            return new EventPOFrame(frame, issueService, identificationService, collectedDataFactory, meteringService);
        }
        if (frame.contains(DEPLOY_TAG)) {
            return new DeployFrame(frame, issueService, identificationService, collectedDataFactory);
        }
        if (frame.contains(REGISTER_TAG)) {
            return new RegisterFrame(frame, clock, issueService, readingTypeUtilService, identificationService, collectedDataFactory);
        }
        throw new InboundFrameException(MessageSeeds.INBOUND_UNEXPECTED_FRAME, frame, "Unexpected frame type: '" + getFrameTag(frame) + "'. Expected REQUEST, DEPLOY, EVENT, EVENTPO or REGISTER");
    }

    private String getFrameTag(String frame) {
        String[] splitResult = frame.split(">");
        return splitResult[0].substring(1);
    }

    /**
     * Read in characters until you find the end tag, or (if empty end tag) until a timeout occurs.
     *
     * @param retryRequest in case of timeouts, send a retry.
     * @param endString    Stop reading in bytes when this string is found. If null, read in bytes until a timeout occurs.
     * @return the partial frame
     * @throws ComServerExecutionException
     *          in case of timeout after x retries
     */
    private String readVariableLength(String endString, byte[] retryRequest) throws InboundTimeOutException {
        comChannel.startReading();
        StringBuilder sb = new StringBuilder();
        long timeoutMoment = System.currentTimeMillis() + timeout;
        int retryCount = 0;

        while (endString == null || -1 == sb.indexOf(endString)) {    //Read until you find the endString characters or a timeout occurs
            this.readAvailableByteOrDelay(sb);
            if (System.currentTimeMillis() > timeoutMoment) {
                if (!sb.toString().isEmpty()) {
                    return sb.toString();    //Stop listening, return the result
                }
                retryCount++;
                timeoutMoment = System.currentTimeMillis() + timeout;
                if (retryCount > retries) {
                    throw new InboundTimeOutException(String.format("Timeout while waiting for inbound frame, after %d ms, using %d retries", timeout, retries));
                }
                if (retryRequest != null) {    //Send retry and wait again
                    send(retryRequest);
                    comChannel.startReading();
                }
            }
        }
        return sb.toString();
    }

    private void readAvailableByteOrDelay(StringBuilder sb) {
        if (comChannel.available() > 0) {
            sb.append(readByte());
        } else {
            delay();
        }
    }

    /**
     * Read in bytes until you have reached the asked length
     *
     * @param length       the number of bytes that should be read
     * @param retryRequest in case of timeouts, send a retry.
     * @return the bytes that were read out
     * @throws ComServerExecutionException
     *          in case of timeout after x retries
     */
    private String readFixedLength(int length, byte[] retryRequest) {
        StringBuilder sb = new StringBuilder();
        comChannel.startReading();
        long timeoutMoment = System.currentTimeMillis() + timeout;
        int retryCount = 0;

        while (sb.length() < length) {
            this.readAvailableByteOrDelay(sb);
            if (System.currentTimeMillis() > timeoutMoment) {
                retryCount++;
                timeoutMoment = System.currentTimeMillis() + timeout;
                if (retryCount > retries) {
                    throw new InboundFrameException(MessageSeeds.INBOUND_TIMEOUT, "Timeout while waiting for inbound frame, after " + timeout + " ms, using " + retries + " retries.");
                }
                if (retryRequest != null) {    //Send retry and wait again
                    send(retryRequest);
                    comChannel.startReading();
                    sb.setLength(0);    // Clear the stringBuilder
                }
            }
        }
        return sb.toString();
    }

    private String readByte() {
        return new String(new byte[]{(byte) comChannel.read()});
    }

    private void delay() {
        this.delay(DEFAULT_DELAY_MILLIS);
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void send(byte[] bytes) {
        comChannel.startWriting();
        comChannel.write(bytes);
    }

    private byte[] createAck(String frameTag) {
        return ("<" + frameTag + ">" + "OK" + "</" + frameTag + ">").getBytes();
    }

}
