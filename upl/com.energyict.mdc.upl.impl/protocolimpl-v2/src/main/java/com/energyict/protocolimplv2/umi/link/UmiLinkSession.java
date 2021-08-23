package com.energyict.protocolimplv2.umi.link;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimplv2.umi.connection.UmiConnection;
import com.energyict.protocolimplv2.umi.packet.AppLayerEncryptedPacket;
import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UmiLinkSession implements IUmiLinkSession {
    private final UmiConnection connection;
    private int sequenceNumber;
    private short destinationId;
    private short sourceId;
    private boolean sessionEstablished;
    private boolean busy = false;
    // RETRANSMISSION_TIMEOUT is chosen based on communication tests
    private static final int RETRANSMISSION_TIMEOUT = 20000;
    private static final int RETRANSMISSION_ATTEMPTS = 3;
    private static final int MAX_FRAME_PAYLOAD_LENGTH = 250;
    private static final int MAX_FRAGMENTED_FRAME_PAYLOAD_LENGTH = 244;
    private static final int MAX_LINK_SEQUENCE_NUMBER = 0xF;

    public UmiLinkSession(UmiConnection connection) {
        this.connection = connection;
        this.sequenceNumber = 0;
        this.destinationId = 0;
        this.sourceId = 0;
        this.sessionEstablished = false;
    }

    public void sendUmiResync() throws IOException {
        LinkLayerFrame frame = getUmiResyncFrame(LinkFrameType.RESYNC);
        LinkLayerFrame responseFrame = trySend(frame.getRaw());
        if (responseFrame == null) {
            throw new ProtocolException("Umi re-sync response is not received in time");
        }
        switch (responseFrame.getLinkFrameHeaderData().getLinkFrameType()) {
            case RESYNC_RESPONSE:
                sequenceNumber = 1;
                destinationId = responseFrame.getLinkFrameHeaderData().getSource();
                sourceId = responseFrame.getLinkFrameHeaderData().getDestination();
                sessionEstablished = true;
                break;
            case ERROR:
                IData payload = responseFrame.getPayload();
                if (payload == null || payload.getLength() == 0) {
                    throw new ProtocolException("Received an error frame with no payload");
                }
                ByteBuffer buffer = ByteBuffer.wrap(payload.getRaw().clone()).order(ByteOrder.LITTLE_ENDIAN);
                LinkLayerError error = LinkLayerError.fromId(buffer.getShort());
                throw new ProtocolException(error.getDescription());
        }
    }

    private LinkLayerFrame trySend(byte[] raw) throws IOException {
        /**
         * Send/receive model, the response packet is returned as soon as it was received,
         * but it waits long enough to decide the response is lost and the retransmission of the request is needed.
         * Otherwise the late-arrived packet will break the sequence and will be considered as 'out of order'
         * which will lead to broken communication
         */
        long retransmissionTimer = System.currentTimeMillis() + RETRANSMISSION_TIMEOUT;
        for (int i = 0; i < RETRANSMISSION_ATTEMPTS; i++) {
            connection.send(raw);
            byte[] response = null;
            do {
                response = connection.receive();
            } while ((response == null || response.length == 0) && retransmissionTimer > System.currentTimeMillis());
            if (response != null && response.length != 0) {
                LinkLayerFrame responseFrame = new LinkLayerFrame(response);
                if (hasValidateCrc(responseFrame)) {
                    return responseFrame;
                }
            }
        }
        throw ConnectionCommunicationException.numberOfRetriesReached(new ProtocolException("Link layer number of retransmission retries reached"), RETRANSMISSION_ATTEMPTS);
    }

    private boolean hasValidateCrc(LinkLayerFrame responseFrame) {
        return Arrays.equals(responseFrame.calculateCrc(), responseFrame.getCrc().getRaw());
    }

    private LinkLayerFrame analyseResponse(LinkLayerFrame sentFrame, LinkLayerFrame responseFrame) throws IOException {
        LinkFrameHeaderData sentFrameHeaderData = sentFrame.getLinkFrameHeaderData();
        if (responseFrame == null && !(sentFrameHeaderData.getLinkFrameType().equals(LinkFrameType.RESYNC_RESPONSE))) {
            throw new ProtocolException("Response frame was not received in time");
        }
        LinkLayerFrame nextFrameToSend = null;

        LinkFrameHeaderData responseFrameHeaderData = responseFrame.getLinkFrameHeaderData();

        if (responseFrameHeaderData.getSequence() == MAX_LINK_SEQUENCE_NUMBER) {
            sendUmiResync();
            this.sequenceNumber = 0;
        }
        switch (responseFrameHeaderData.getLinkFrameType()) {
            case SIMPLE_RESPONSE:
                break;
            case FRAGMENT:
                FragmentFrame frame = new FragmentFrame(responseFrame.getPayload().getRaw());
                if (frame.getFragmentHeaderData().getFragmentType().equals(FragmentType.FIRST)) {
                    this.busy = true;
                } else if (frame.getFragmentHeaderData().getFragmentType().equals(FragmentType.LAST)) {
                    this.busy = false;
                }
                LinkFrameHeaderData data = new LinkFrameHeaderData.Builder()
                        .setBusy(busy ? (byte) 1 : (byte) 0)
                        .setFrameType(LinkFrameType.FRAGMENT_RESPONSE)
                        .setSource((byte) this.sourceId)
                        .setDestination(responseFrameHeaderData.getSource())
                        .build();
                FragmentHeaderData fragmentHeaderData = new FragmentHeaderData(FragmentType.NEXT);

                nextFrameToSend = new LinkLayerFrame.Builder()
                        .setLinkFrameHeaderData(data)
                        .setPayload(fragmentHeaderData)
                        .build();
                break;
            case FRAGMENT_RESPONSE:
                FragmentFrame fragmentResponseFrame = new FragmentFrame(responseFrame.getPayload().getRaw());
                FragmentType fragmentType = fragmentResponseFrame.getFragmentHeaderData().getFragmentType();
                if (fragmentType.equals(FragmentType.ERROR)) {
                    ByteBuffer buffer = ByteBuffer.wrap(fragmentResponseFrame.getPayload().getRaw().clone()).order(ByteOrder.LITTLE_ENDIAN);
                    FragmentationError fragmentationError = FragmentationError.fromId(buffer.getShort());
                    throw new ProtocolException("Fragmentation error: " + fragmentationError.getDescription());
                }
                if (!fragmentType.equals(FragmentType.ACK)) {
                    throw new ProtocolException("Unexpected fragmentation frame type. ACK was expected, but " + fragmentType.name() + " was received");
                }
                break;
            case RESYNC_RESPONSE:
                this.sequenceNumber++;
                this.destinationId = responseFrameHeaderData.getSource();
                this.sourceId = responseFrameHeaderData.getDestination();
                this.sessionEstablished = true;
                break;
            case RESYNC:
                nextFrameToSend = getUmiResyncFrame(LinkFrameType.RESYNC_RESPONSE);
                this.sequenceNumber = 0;
                break;
            case ERROR:
                IData payload = responseFrame.getPayload();
                if (payload == null || payload.getLength() == 0) {
                    throw new ProtocolException("Received an error frame with no payload");
                }
                ByteBuffer buffer = ByteBuffer.wrap(payload.getRaw().clone()).order(ByteOrder.LITTLE_ENDIAN);
                LinkLayerError error = LinkLayerError.fromId(buffer.getShort());
                throw new ProtocolException(error.getDescription());
        }
        return nextFrameToSend;
    }

    private LinkLayerFrame getUmiResyncFrame(LinkFrameType type) {
        LinkFrameHeaderData headerData = new LinkFrameHeaderData.Builder()
                .setFrameType(type)
                .setSource((byte) this.sourceId)
                .setDestination((byte) this.destinationId)
                .build();
        return new LinkLayerFrame.Builder()
                .setLinkFrameHeaderData(headerData)
                .setPayload(new LittleEndianData(0))
                .build();
    }


    public boolean isSessionEstablished() {
        return this.sessionEstablished;
    }

    public byte[] sendGenericLink(AppLayerEncryptedPacket appLayerEncryptedPacketRaw) throws IOException {
        byte[] bytesToSend = appLayerEncryptedPacketRaw.getRaw();
        ByteArrayOutputStream fullResponse = new ByteArrayOutputStream();
        List<LinkLayerFrame> requestFrames = new ArrayList<>();

        if (bytesToSend.length < MAX_FRAME_PAYLOAD_LENGTH) {
            LinkFrameHeaderData headerData = new LinkFrameHeaderData.Builder()
                    .setSequence((byte) this.sequenceNumber)
                    .setFrameType(LinkFrameType.SIMPLE)
                    .setFramePayloadLength(appLayerEncryptedPacketRaw.getLength())
                    .setSource((byte) this.sourceId)
                    .setDestination((byte) this.destinationId)
                    .build();
            requestFrames.add(new LinkLayerFrame.Builder()
                    .setLinkFrameHeaderData(headerData)
                    .setPayload(appLayerEncryptedPacketRaw)
                    .build());
        } else {
            int currentIndex = 0;
            FragmentType fragmentType = FragmentType.FIRST;
            int data = 0x00;

            while (currentIndex < bytesToSend.length) {
                if (currentIndex == 0) {
                    fragmentType = FragmentType.FIRST;
                    data = bytesToSend.length;
                } else if (currentIndex > 0 && (bytesToSend.length - currentIndex) > MAX_FRAGMENTED_FRAME_PAYLOAD_LENGTH) {
                    fragmentType = FragmentType.MIDDLE;
                } else {
                    fragmentType = FragmentType.LAST;
                }

                FragmentHeaderData fragmentHeaderData = new FragmentHeaderData(fragmentType, data);
                byte[] fragmentPayload = Arrays.copyOfRange(bytesToSend, currentIndex,
                        Math.min(MAX_FRAGMENTED_FRAME_PAYLOAD_LENGTH, bytesToSend.length - currentIndex));

                currentIndex += Math.min(MAX_FRAGMENTED_FRAME_PAYLOAD_LENGTH, bytesToSend.length - currentIndex);

                FragmentFrame fragmentFrame = new FragmentFrame(fragmentHeaderData, new LittleEndianData(fragmentPayload));


                LinkFrameHeaderData headerData = new LinkFrameHeaderData.Builder()
                        .setSequence((byte) this.sequenceNumber)
                        .setFrameType(LinkFrameType.FRAGMENT)
                        .setFramePayloadLength(fragmentFrame.getLength())
                        .setSource((byte) this.sourceId)
                        .setDestination((byte) this.destinationId)
                        .build();

                LinkLayerFrame frame = new LinkLayerFrame.Builder()
                        .setLinkFrameHeaderData(headerData)
                        .setPayload(fragmentFrame.getPayload())
                        .build();

                requestFrames.add(frame);
            }
        }
        for (LinkLayerFrame frame : requestFrames) {
            LinkLayerFrame request = frame;
            do {
                LinkLayerFrame response = trySend(request.getRaw());
                if (response != null) {
                    request = analyseResponse(request, response);
                    byte[] purePayload = response.getLinkFrameHeaderData().getLinkFrameType().equals(LinkFrameType.FRAGMENT)
                            ? Arrays.copyOfRange(response.getPayload().getRaw(), FragmentHeaderData.SIZE, response.getPayload().getLength())
                            : Arrays.copyOf(response.getPayload().getRaw(), response.getPayload().getLength());

                    fullResponse.write(purePayload);
                }
                this.sequenceNumber++;
            } while (request != null);

        }
        return fullResponse.toByteArray();

    }
}
