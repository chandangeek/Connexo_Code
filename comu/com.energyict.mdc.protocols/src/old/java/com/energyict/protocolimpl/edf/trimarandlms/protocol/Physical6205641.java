/*
 * Datalink6205641.java
 *
 * Created on 13 februari 2007, 17:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Physical6205641 {

    Connection62056 connection;
    Frame frame;

    public Physical6205641(Connection62056 connection) {
        this.connection=connection;
    }

    public void initPhysical() {

    }

//    int debugCount=0;

    public void request(Frame frame) throws IOException {
        // send frame
        this.frame=frame;

//        // KV_DEBUG ***DEBUG***
//        debugCount++;
//        if ((debugCount%20)==0) {
//            frame.getData()[frame.getData().length-1]=0;
//            frame.getData()[frame.getData().length-2]=0;
//            System.out.print("Simulate BAD FRAME SEND\n");
//        }

        connection.sendData(frame.getData());
    }

    public void respond() throws IOException {
        // must receive something...
        byte[] data = connection.receiveData();
        frame = new Frame();
        frame.init(data);
        connection.getDatalink6205641().indicate(frame);
    }

    public void abort() {

    }




} // public class Datalink6205641
