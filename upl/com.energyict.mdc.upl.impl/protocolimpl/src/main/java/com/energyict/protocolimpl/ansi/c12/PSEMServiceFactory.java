/*
 * PSEMServiceFactory.java
 *
 * Created on 16 oktober 2005, 17:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocolimpl.base.*;
import java.io.*;
import com.energyict.protocolimpl.ansi.c12.tables.*;
import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author Koen
 */
public class PSEMServiceFactory {
    
    final int DEBUG=0;
    private C12ProtocolLink c12ProtocolLink;
           
    static public final int PASSWORD_BINARY=0;
    static public final int PASSWORD_ASCII=1;
    
    static private final int STATE_BASE=0;
    static private final int STATE_ID=1;
    static private final int STATE_SESSION=2;
    
    int state=STATE_BASE;
    private int tableId; // only for debugging purposes
    
    /** Creates a new instance of PSEMServiceFactory */
    public PSEMServiceFactory(C12ProtocolLink c12ProtocolLink) {
        this.c12ProtocolLink=c12ProtocolLink;
    }

    public int getState() {
        return state;
    }
    
    public C12ProtocolLink getC12ProtocolLink() {
        return c12ProtocolLink;
    }

    public IdentificationResponse getIdentificationResponse() throws IOException {
        this.setTableId(-1);
        if (state != STATE_BASE)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in BASE STATE...");
        
        IdentificationRequest identificationRequest = new IdentificationRequest(this);
        identificationRequest.build();
        state = STATE_ID;
        return (IdentificationResponse)identificationRequest.getResponse();
    }
    
    public NegotiateResponse getNegotiateResponse(int packetSize, int nrOfPackets) throws IOException {
        this.setTableId(-1);
        if (state != STATE_ID)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in ID STATE...");
        NegotiateRequest negotiateRequest = new NegotiateRequest(this);
        negotiateRequest.negotiate(packetSize, nrOfPackets, 6); // 256 bytes, 1 packet, 9600 baud
        negotiateRequest.build();
        getC12ProtocolLink().getC12Layer2().setNegotiateResponse((NegotiateResponse)negotiateRequest.getResponse());
        return (NegotiateResponse)negotiateRequest.getResponse();
    }
    
    private void doLogon(int userId, String user) throws IOException {
        this.setTableId(-1);
        if (state != STATE_ID)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in ID STATE...");
        LogonRequest logonRequest = new LogonRequest(this);
        logonRequest.logon(userId, user.getBytes());
        logonRequest.build();
        state=STATE_SESSION;
    }
    
    private void doLogoff() throws IOException {
        this.setTableId(-1);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, logoff, wrong state for logoff. Should be in SESSION STATE...");
        LogoffRequest logoffRequest = new LogoffRequest(this);
        logoffRequest.build();
        state=STATE_ID;
    }
    
    public void terminate() throws IOException {
        this.setTableId(-1);
        if ((state != STATE_SESSION) && (state != STATE_ID))
            throw new IOException("PSEMServiceFactory, terminate, wrong state for terminate. Should be in SESSION STATE or ID STATE...");
        TerminateRequest terminateRequest = new TerminateRequest(this);
        terminateRequest.build();
        state=STATE_BASE;
    }
    
    public void secure(byte[] password) throws IOException {
        this.setTableId(-1);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, secure, wrong state for secure. Should be in SESSION STATE...");
        SecurityRequest securityRequest = new SecurityRequest(this);
        securityRequest.secure(password);
        securityRequest.build();
    }
    
    public void authenticate(int securityLevel, byte[] password, byte[] ticket) throws IOException {
        this.setTableId(-1);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, secure, wrong state for secure. Should be in SESSION STATE...");
        AuthenticateRequest authenticateRequest = new AuthenticateRequest(this);
        authenticateRequest.authenticate(securityLevel, password, ticket);
        authenticateRequest.build();
    }
    
    public byte[] partialReadDefault() throws IOException {
        this.setTableId(-1);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in SESSION STATE...");
        ReadRequest readRequest = new ReadRequest(this);
        readRequest.partialReadDefault();
        readRequest.build();
        return ((ReadResponse)readRequest.getResponse()).getTableData();
    }
    
    // full read
    public byte[] fullRead(int tableId) throws IOException {
        this.setTableId(tableId);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in SESSION STATE...");
        ReadRequest readRequest = new ReadRequest(this);
        readRequest.fullRead(tableId);
        readRequest.build();
        return ((ReadResponse)readRequest.getResponse()).getTableData();
    }
    
    // partial read index
    public byte[] partialReadIndex(int tableId, int index, int count) throws IOException {
        this.setTableId(tableId);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in SESSION STATE...");
        ReadRequest readRequest = new ReadRequest(this);
        readRequest.partialReadIndex(tableId, index, count);
        readRequest.build();
        return ((ReadResponse)readRequest.getResponse()).getTableData();
    }
    
    // partial read index
    public byte[] partialReadOffset(int tableId, int offset, int count) throws IOException {
        this.setTableId(tableId);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in SESSION STATE...");
        ReadRequest readRequest = new ReadRequest(this);
        readRequest.partialReadOffset(tableId, offset, count);
        readRequest.build();
        return ((ReadResponse)readRequest.getResponse()).getTableData();
    }
 
    public void fullWrite(int tableId, byte[] tableData) throws IOException {
        this.setTableId(tableId);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in SESSION STATE...");
        WriteRequest writeRequest = new WriteRequest(this);
        writeRequest.fullWrite(tableId, tableData);
        writeRequest.build();
    }    
    
    // partial write index
    public void partialWriteIndex(int tableId, int index, byte[] tableData) throws IOException {
        this.setTableId(tableId);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in SESSION STATE...");
        WriteRequest writeRequest = new WriteRequest(this);
        writeRequest.partialWriteIndex(tableId, index, tableData);
        writeRequest.build();
    }
    
    // partial write offset
    public void partialWriteOffset(int tableId, int offset, byte[] tableData) throws IOException {
        this.setTableId(tableId);
        if (state != STATE_SESSION)
            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in SESSION STATE...");
        WriteRequest writeRequest = new WriteRequest(this);
        writeRequest.partialWriteOffset(tableId, offset, tableData);
        writeRequest.build();
    }    

    protected int getTableId() {
        return tableId;
    }

    private void setTableId(int tableId) {
        this.tableId = tableId;
    }
    
    public void logOn(int c12UserId, String c12User, String password, int securityLevel, int passwordFormat) throws IOException {
        logOn(c12UserId, c12User, password, securityLevel, passwordFormat, 80, 3);
    }
    
    public void logOn(int c12UserId, String c12User, String password, int securityLevel, int passwordFormat, int packetSize, int nrOfPackets) throws IOException {
        IdentificationFeature  identificationFeature = getIdentificationResponse().getIdentificationFeature0(); // BASE -> ID
        if (DEBUG>=1) System.out.println("KV_DEBUG> PSEMServiceFactory, logon, identificationFeature="+identificationFeature);
        getNegotiateResponse(packetSize, nrOfPackets);  // 80 x 3 is 240 (must be < 256)!
        doLogon(c12UserId,c12User); // ID -> SESSION
        if ((password != null) && ("".compareTo(password) != 0)) {
            if (DEBUG>=1) System.out.println("KV_DEBUG> PSEMServiceFactory, logon, password="+password);
            if (identificationFeature != null) {
                if (DEBUG>=1) System.out.println("KV_DEBUG> PSEMServiceFactory, logon, authenticate("+securityLevel+","+password+","+identificationFeature.getTicket()+")");
                // Use authenticate service 
                authenticate(securityLevel, password.getBytes(), identificationFeature.getTicket());
            }
            else {
                if (DEBUG>=1) System.out.println("KV_DEBUG> PSEMServiceFactory, logon, secure("+password+")");
                // use secure service
                if (passwordFormat==PASSWORD_BINARY) 
                    secure(ProtocolUtils.convert2ascii(password.getBytes()));    
                else if (passwordFormat==PASSWORD_ASCII) 
                    secure(ParseUtils.extendWithNULL(password.getBytes(),20));
            }
            
        } // if ((password != null) && ("".compareTo(password) != 0))
        
    } // public void logOn(int c12UserId, String c12User, String password, int securityLevel, int passwordFormat, int packetSize, int nrOfPackets) throws IOException
    
    

    
    public void logOff() throws IOException {
        doLogoff(); // SESSION -> ID
        terminate(); // SESSION/ID -> BASE
    }
    
}
