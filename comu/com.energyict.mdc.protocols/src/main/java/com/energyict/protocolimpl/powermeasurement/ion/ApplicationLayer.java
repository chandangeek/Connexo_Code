/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The application layer provides services to allow applications to communicate
 * with devices which may require routing of the request through several
 * communications points to reach the destination and back again.  This
 * information is only sent in the first frame of any transaction with an ION
 * device and precedes the application layer.
 *
 * @author fbo
 *
 */

class ApplicationLayer {

    private Ion ion;
    private NetworkLayer networkLayer;
    private Authentication authentication;

    ApplicationLayer( Ion ion, Authentication authentication ) throws ConnectionException {
        this.ion = ion;
        this.networkLayer = new NetworkLayer( ion );
        this.authentication = authentication;
    }

    /**
     *
     * @param handles
     * @param method
     * @return
     * @throws IOException
     */
    List read( List handles, IonMethod method ) throws IOException {
        ArrayList rslt = new ArrayList();
        for (Iterator iter = handles.iterator(); iter.hasNext();) {
            IonHandle handle = (IonHandle) iter.next();
            rslt.add( new Command( handle, method ));
        }
        read( rslt );
        return rslt;
    }

    /**
     * Read a single command.
     */
    Command read( Command command ) throws IOException {
        ArrayList cmdList = new ArrayList();
        cmdList.add( command );
        read( cmdList );
        return command;
    }

    /**
     * Read a list of commands
     * @param commands List of Command objects
     * @throws IOException
     */
    void read( List commands ) throws IOException {

        Message message =
            new Message()
                .setEndProgram(true)
                .setExecuteProgram(true)
                .setStartProgram(true)
                .setResponse(false);

        Iterator itr = commands.iterator();
        while( itr.hasNext() )
            message.addCommand( (Command)itr.next() );

        Assembly a = new Assembly(ion, send( message ));
        Iterator i = commands.iterator();
        while( i.hasNext() ) {
            Command c = (Command)i.next();
            IonObject ionObject = ion.parse(a);
            c.setResponse(ionObject);
        }

    }

    private ByteArray send( Message message ) throws IOException, ConnectionException{


        ByteArray received =
            networkLayer.send(
                new Packet( )
                    .setSource( ion.getSource() )
                    .setDestination( ion.getDestination() )
                    .setIsResponse(false)
                    .setService( 1 )
                    .setMsgType( 0 )
                    .setAuthentication( authentication )
                    .setData( message.toByteArray() ) );

        Message receivedMessage = Message.parse( new Assembly( ion, received ) );

        return receivedMessage.getData();

    }

    void sendTime( Message message ) throws IOException, ConnectionException{

        networkLayer.sendTime(
            new Packet( )
                .setSource( ion.getSource() )
                .setDestination( ion.getDestination() )
                .setIsTimeSetMessage( true )
                .setService( 1 )
                .setMsgType( 17 )
                .setAuthentication( authentication )
                .setData( message.getData() ) );

    }

}
