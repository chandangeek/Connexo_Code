/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.dialer.connection.Connection;
import com.energyict.protocolimpl.base.CRCGenerator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * The data-link layer is responsible for the transmission and reception of
 * error-free frames. A frame is a collection of bytes in a predefined sequence.
 *
 * @author fbo
 */

class DataLinkLayer extends Connection {

    Ion ion;

    DataLinkLayer( Ion ion ) throws ConnectionException {

        super(ion.getInputStream(), ion.getOutputStream(), ion.getForceDelay(), 0);
        this.ion = ion;

    }


    ByteArray send( ByteArray byteArray ) throws IOException {

        int nrTry = 0;

        while (nrTry < ion.getRetries()) {

            try {

                Frame rFrame = null;
                ByteArray result = new ByteArray();

                List bList = byteArray.split( 238 );
                Iterator i = bList.iterator();
                int frameCount = bList.size();
                boolean firstFrame = true;

                while( i.hasNext() ) {

                    ByteArray ba = (ByteArray)i.next();
                    frameCount = frameCount - 1;

                    Frame sFrame = createFrame( ba ).setCounter(frameCount);
                    if( i.hasNext() ) {             // if more frames will be
                        sFrame.setEnableAck();      // send, ask for an ack/nack
                    }

                    if( firstFrame ) {
                        sFrame.setIsFirstFrame(true);
                        firstFrame = false;
                    }

                    sendRawData(sFrame);
                    rFrame = receiveFrame();

                    boolean isAck =
                        rFrame.isSlaveToMaster() &&
                        rFrame.isAckNak() &&
                        rFrame.isAckNakEnable();

                    if( !isAck ) continue;
                    /* if not te correct ack, retry the complete send */

                }

                result.add( rFrame.getData() );

                int transactionCode     = rFrame.getTransactionCode();
                int sourceAddress       = rFrame.getSourceAddress();
                int destAddress         = rFrame.getDestinationAddress();

                if( rFrame.isAckNakEnable() )
                    sendAck( transactionCode, destAddress, sourceAddress );

                // switch source and dest address since it is taken
                // from an incomming message

                while( rFrame.getCounter() > 0 ) {
                    rFrame = receiveFrame();
                    transactionCode = rFrame.getTransactionCode();
                    result.add( rFrame.getData() );
                    if( rFrame.isAckNakEnable() )
                        sendAck( transactionCode, destAddress, sourceAddress );
                }

                return result;

            } catch( ParseException pex ){
                pex.printStackTrace();
                nrTry = nrTry + 1;
            } catch( CrcException crce ) {
                crce.printStackTrace();
                nrTry = nrTry + 1;
            } catch (TimeOutException toe) {
                toe.printStackTrace();
                nrTry = nrTry + 1;
            } catch (IOException ioe) {
                nrTry = nrTry + 1;
                throw ioe;
            }

        }

        throw new IOException("Failed to read: nr retries exceeded");

    }

    private void sendRawData(Frame sFrame) throws IOException, ConnectionException {
        sendRawData( sFrame.toByteArray().getBytes() );
    }

    Frame receiveFrame( ) throws IOException, ParseException {

        ByteArray buffer = new ByteArray();
        copyEchoBuffer();
        long endTime = System.currentTimeMillis() + ion.getTimeout( );

        int i = 0;
        int aChar = 0;

        do {

            aChar = readIn();
            if( aChar != -1  ){
                buffer.add( (byte)aChar );
                i = i + 1;
            }

            if (isTimeOut(endTime)) {
                String msg = "Connection timed out. " + buffer.toHexaString(true);
                throw new TimeOutException(msg);
            }

        } while( i < 6 );

        int length = aChar + 1;
        i = 0;
        do {

            aChar = readIn();
            if ( aChar != -1 ) {
                buffer.add((byte) aChar);
                i = i + 1;
            }
            if( isTimeOut(endTime)) {
                String msg = "Connection timed out. " + buffer.toHexaString(true);
                throw new TimeOutException(msg);
            }

        } while( i < length );
        // length field in frame is data + 7
        // but there are data + 8 bytes to read

        // now check the crc
        int presumedCrc = CRCGenerator.calcCRCModbus( buffer.sub(4, buffer.size()-6).getBytes() );

        byte crc[] = new byte[2];
        crc[0] = (byte)(presumedCrc&0x00FF);
        crc[1] = (byte)((presumedCrc>>8)&0xFF);

        boolean crcOk = true;
        crcOk &= (buffer.sub(buffer.size()-2).get(0) == crc[0]);
        crcOk &= (buffer.sub(buffer.size()-1).get(0) == crc[1]);

        if( !crcOk )
            throw new CrcException( "Crc exception occured" );

        try {
            return Frame.parse( new Assembly( null, buffer ) );
        } catch( Exception ex ){
            throw new ParseException( "Error parsing", ex );
        }

    }

    void sendTime( ByteArray ba ) throws IOException, ConnectionException {
        Frame sFrame = createFrame( ba ).setCounter(1);
        sFrame.setCounter(0);
        sFrame.setIsFirstFrame(true);
        sendRawData(sFrame);
    }

    void sendAck( int transactionCode, int sourceAddress, int destAddress )
        throws IOException {

        Frame aFrame = new Frame( null );

        /* Very strange, but the manufacturer software ack's with slaveToMaster
         * bit on.*/
        aFrame.setIsSlaveToMaster();
        aFrame.setIsAckNak();
        aFrame.setEnableAck();
        aFrame.setSourceAddress( ion.getSource() );
        aFrame.setDestinationAddress( ion.getDestination() );
        aFrame.setTransactionCode( transactionCode );

        sendRawData(aFrame);

    }

    private boolean isTimeOut(long endTime) {
        return System.currentTimeMillis() >= endTime;
    }

    private Frame createFrame( ByteArray data ) {
        return new Frame( data )
            .setSourceAddress( ion.getSource() )
            .setDestinationAddress( ion.getDestination() );
    }


    class TimeOutException extends IOException {

        private static final long serialVersionUID = 1L;

        TimeOutException(String msg) {
            super(msg);
        }

    }

    class CrcException extends IOException {

        private static final long serialVersionUID = 1L;

        CrcException (String msg) {
            super(msg);
        }

    }

    class ParseException extends Exception {

        private static final long serialVersionUID = 1L;

        ParseException(String msg, Exception cause) {
            super(msg, cause);
        }

    }


}
