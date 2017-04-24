/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractRequest.java
 *
 * Created on 16 oktober 2005, 17:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractRequest {

    abstract protected void parse(ResponseData responseData) throws IOException;
    abstract protected RequestData getRequestData();

    // IDENTIFICATION SERVICE
    static public final int IDENTIFICATION=0x20; // Identification request

    // READ SERVICE
    static public final int FULL_READ=0x30; // <tableid>
    static public final int PARTIAL_READ_INDEX_1=0x31;
    static public final int PARTIAL_READ_INDEX_2=0x32;
    static public final int PARTIAL_READ_INDEX_3=0x33;
    static public final int PARTIAL_READ_INDEX_4=0x34;
    static public final int PARTIAL_READ_INDEX_5=0x35;
    static public final int PARTIAL_READ_INDEX_6=0x36;
    static public final int PARTIAL_READ_INDEX_7=0x37;
    static public final int PARTIAL_READ_INDEX_8=0x38;
    static public final int PARTIAL_READ_INDEX_9=0x39;
    static public final int PARTIAL_READ_DEFAULT=0x3E; // transfer default table
    static public final int PARTIAL_READ_OFFSET=0x3F; // <tableid><offset><count>

    // WRITE SERVICE
    static public final int FULL_WRITE=0x40; // <tableid><tabledata>
    static public final int PARTIAL_WRITE_INDEX_1=0x41;
    static public final int PARTIAL_WRITE_INDEX_2=0x42;
    static public final int PARTIAL_WRITE_INDEX_3=0x43;
    static public final int PARTIAL_WRITE_INDEX_4=0x44;
    static public final int PARTIAL_WRITE_INDEX_5=0x45;
    static public final int PARTIAL_WRITE_INDEX_6=0x46;
    static public final int PARTIAL_WRITE_INDEX_7=0x47;
    static public final int PARTIAL_WRITE_INDEX_8=0x48;
    static public final int PARTIAL_WRITE_INDEX_9=0x49;
    static public final int PARTIAL_WRITE_OFFSET=0x4F; // <tableid><offset><tabledata>

    // LOGON SERVICE
    static public final int LOGON=0x50;

    // SECURITY SERVICE
    static public final int SECURITY=0x51;

    // LOGOFF SERVICE
    static public final int LOGOFF=0x52;

    // AUTHENTICATE SERVICE
    static public final int AUTHENTICATE=0x53;

    // NEGOTIATE SERVICE
    static public final int NEGOTIATE=0x60; // no baudrate included in the request. Stay at the default baudrate
    static public final int NEGOTIATE_BAUD_1=0x61; // baudrate x included in request 300 baud
    static public final int NEGOTIATE_BAUD_2=0x62; // x = 1..11 600 baud
    static public final int NEGOTIATE_BAUD_3=0x63; // ... 1200 baud
    static public final int NEGOTIATE_BAUD_4=0x64; // 2400 baud
    static public final int NEGOTIATE_BAUD_5=0x65; // 4800 baud
    static public final int NEGOTIATE_BAUD_6=0x66; // 9600 baud
    static public final int NEGOTIATE_BAUD_7=0x67; // 14400 baud
    static public final int NEGOTIATE_BAUD_8=0x68; // 19200 baud
    static public final int NEGOTIATE_BAUD_9=0x69; // 28800 baud
    static public final int NEGOTIATE_BAUD_A=0x6A; // 57600 baud
    static public final int NEGOTIATE_BAUD_B=0x6B; //

    // TERMINATE SERVICE
    static public final int TERMINATE=0x21;



    private PSEMServiceFactory psemServiceFactory;

    AbstractResponse response;

    /** Creates a new instance of AbstractRequest */
    public AbstractRequest(PSEMServiceFactory psemServiceFactory) {
        this.psemServiceFactory=psemServiceFactory;
    }

    protected void prepareBuild() throws IOException {
        // override to provide extra functionality...
    }

    public void build() throws IOException {
        prepareBuild();
        // send request
        ResponseData responseData = getPSEMServiceFactory().getC12ProtocolLink().getC12Layer2().sendRequest(getRequestData());
        parse(responseData);
    }

    public PSEMServiceFactory getPSEMServiceFactory() {
        return psemServiceFactory;
    }

    public AbstractResponse getResponse() {
        return response;
    }

}
