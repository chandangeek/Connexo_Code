/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.dialer.connection.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The link layer accepts, performs and controls transmission service functions
 * requiried by the higher layers.
 *
 * - provides access to the transmission medium;
 * - serializes and deserializes frames;
 * - adds and removes frame delimiters if not performed by the line coupler;
 * - detects frame size errors;
 * - recognizes frames addressed to a designated station
 * - protects messages against loss and error within predetermined data
 *   integrity limits by generation and supervision of error detecting
 *   codes, by indicating detected errors and by controlling certain error
 *   recovery procedures;
 * - reports on persistent transmission errors;
 * - reports on the status of the link configuration
 * - handles frames of different lengths efficiently
 * - supports initiation and maintenance functions
 *
 * Link Service Class
 *
 * S1 SEND/NO REPLY
 * Transmit message; neither aknowledgement nor answer is requested within
 * link layer.
 *
 * S2 SEND/CONFIRM
 * Supports evetn initiated or spontaneous information transfers. The link
 * layer in the receiving station checks the received message: if no errors
 * are detected and the receiving buffer is available, a positive
 * acknowledgement (ACK) is returned to the initiator.  if the receving buffer
 * is not available a negative acknowledgement (NACK) may be returned.
 *
 * S3 REQUEST/RESPOND
 * Supports "Read" operations. The link layer of the receiving station
 * supplies the requested data if available.  Otherwise it answers with a
 * negative acknowledgement. No answers are generated upon detecting frame
 * errors.
 */

public class LinkLayer extends Connection {

    Address address;
    Ziv5Ctd ziv ;
    FrameFactory frameFactory;
    AsduFactory asduFactory;
    boolean fcb;

    int timeoutMilli = 3000;
    int retries;

    public LinkLayer(InputStream inputStream, OutputStream outputStream,
        long l, int i, Ziv5Ctd ziv, int retries ) throws ConnectionException {

        super(inputStream, outputStream, l, i);
        this.ziv = ziv;
        init();
        fcb = false;
        this.retries = retries;
    }

    void init( ){
        address = new Address(1);
        frameFactory = ziv.frameFactory;
    }

    void connect( ) throws IOException  {
        try {
            Frame frame = frameFactory.createFixed(FunctionCode.PRIMARY[9]);
            requestRespondConnect( (FixedFrame) frame );
            frame = frameFactory.createFixed(FunctionCode.PRIMARY[0]);
            sendConfirmConnect( frame );
            frame = frameFactory.createFixed(FunctionCode.PRIMARY[9]);
            requestRespondConnect( (FixedFrame)frame );
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }

    /** during connect init the fcb is not used */
    Frame sendConfirmConnect( Frame frame ) throws IOException, ParseException {
        sendRawData(frame.toByteArray().toByteArray());
        Frame result = receive();
        return result;
    }

    /** during connect init the fcb is not used */
    Frame requestRespondConnect( FixedFrame frame ) throws IOException, ParseException {
        sendRawData( frame.toByteArray().toByteArray() );
        return receive();
    }


    void send( Frame frame ) throws IOException {
        String dbg = frame.toByteArray().toHexaString();
        dbg += " " + frame;
        frame.setPrm(true);
        sendRawData(frame.toByteArray().toByteArray());
    }


    /** double dispatch */
    Frame requestRespond( Frame frame ) throws IOException, ParseException {
        return frame.requestRespond(this);
    }

    Frame requestRespond( VariableFrame frame ) throws IOException, ParseException {
        int tryNr = 0;
        try {

            while( tryNr < retries ) {
                tryNr = tryNr + 1;
                // step 1
                sendRawData( setFcb(frame).toByteArray().toByteArray() );

                // step 2
                Frame rFrame = receive();
                if( ! rFrame.isConfirmAck() )
                    throw new IOException( "received NAK" );

                // step 3
                Frame sFrame = frameFactory.createFixed(FunctionCode.PRIMARY[0xb]);
                send(setFcb(sFrame));

                // step 4
                return (VariableFrame)receive();
            }

            throw new IOException( "Severed connection. ");

        } catch( IOException ioe ) {
            ioe.printStackTrace();
        } catch( ParseException pe ) {
            pe.printStackTrace();
        }
        return null;
    }

    Frame requestRespond( FixedFrame frame ) throws IOException, ParseException {
        sendRawData( setFcb( frame ).toByteArray().toByteArray() );
        return receive();
    }

    /** not sure if this method belongs here */
    Frame requestRespond( Asdu asdu ) throws IOException {
        try {
            Frame frame =
                frameFactory.createVariable( FunctionCode.PRIMARY[3], asdu );
            return requestRespond((VariableFrame)frame);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    Frame setFcb( Frame frame ) {
        fcb = !fcb;
        return frame.setFcb(fcb).setFcv(true);
    }

    public Frame receive( ) throws IOException, ParseException {

        ByteArray buffer = new ByteArray();
        copyEchoBuffer();
        long endTime = System.currentTimeMillis() + timeoutMilli;

        int aChar;
        while( (aChar = readIn()) == -1 && isNotTimeOut(endTime) );
        buffer.add((byte)aChar);

        if( aChar == Frame.START_FIXED ) {
            for( int i = 0; i < 5 && isNotTimeOut(endTime); i ++ ){
                if( (aChar = readIn()) != -1 ) buffer.add((byte) aChar);
            }
        } else {
            int length1 = readIn();
            int length2 = readIn();
            if( length1 == length2 ) {
                buffer.add((byte)length1).add((byte)length2);
                int bytesLeft = length1 + 4;
                for( int i = 0; i < bytesLeft && isNotTimeOut(endTime); i ++ ) {
                    if( (aChar = readIn()) != -1 ) buffer.add((byte) aChar);
                }
            } else {
                String msg =
                    "Length fields do not match " + Integer.toHexString( aChar)
                    + " " + Integer.toHexString( length1) + " " + Integer.toHexString( length2 );

                throw new IOException( msg );
            }
        }
        Frame frame = frameFactory.parse(buffer);
        return frame;
    }

    private boolean isNotTimeOut( long endTime ){
        return System.currentTimeMillis() < endTime;
    }

}

