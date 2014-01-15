package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.io.IOException;

/**
 * The network layer provides services to allow applications to communicate with
 * devices which may require routing of the request through several
 * communications points to reach the destination and back again.  This
 * information is only sent in the first frame of any transaction with an ION
 * device and precedes the application layer.
 *
 * @author fbo
 *
 */


class NetworkLayer {

    private Ion ion;
    private DataLinkLayer datalinkLayer;

    /** message id is incremented for each package */
    int messageId = 0;

    NetworkLayer( Ion ion ) throws ConnectionException {
        this.ion = ion;
        this.datalinkLayer = new DataLinkLayer( ion );
    }

    ByteArray send( Packet packet ) throws IOException, ConnectionException {

        // Send a new packet
        messageId = messageId + 1;
        packet.setMsgId( messageId );

        ByteArray received = datalinkLayer.send( packet.toByteArray() );

        // Parse the received packet
        Packet receivedPacket = Packet.parse( new Assembly(ion, received) );

        if( receivedPacket.getMsgId() != messageId ) {
            String m = "Message " + receivedPacket.toByteArray().toHexaString(true);
            throw new MessageIdMismatchException( m );
        }

        return receivedPacket.getData();

    }

    void sendTime( Packet packet ) throws IOException {
        // Send a new packet
        messageId = messageId + 1;
        packet.setMsgId( messageId );
        datalinkLayer.sendTime( packet.toByteArray() );

    }

    /** wrap around at 999.
     * This is not required, but just used for making sure that the wrap around
     * behaviour is easy to understand and does not overflow.  */
    int getNextMessageId( ) {
        if( messageId == 999 )
            messageId = 0;
        messageId = messageId + 1;
        return messageId;
    }

    private class MessageIdMismatchException extends IOException {

        private static final long serialVersionUID = 1L;

        MessageIdMismatchException(String msg) {
            super(msg);
        }

    }

}
