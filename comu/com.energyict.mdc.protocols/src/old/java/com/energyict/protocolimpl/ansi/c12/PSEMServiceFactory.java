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

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.tables.IdentificationFeature;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
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
    public boolean c1222 = false;
    protected C1222Buffer c1222Buffer = null;

    public C1222Buffer getC1222Buffer() {
		return c1222Buffer;
	}

	public void setC1222Buffer(C1222Buffer c1222Buffer) {
		this.c1222Buffer = c1222Buffer;
	}

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

        if (c1222)
        {
	        ConnectRequest connectRequest = new ConnectRequest(this);
	        connectRequest.connect(userId, user.getBytes());
	        connectRequest.build();

	        LogonRequest logonRequest = new LogonRequest(this);
	        logonRequest.logon(c1222Buffer.getPassword());
	        logonRequest.build();
        }
        else
        {
	        if (state != STATE_ID)
	            throw new IOException("PSEMServiceFactory, logon, wrong state for logon. Should be in ID STATE...");
	        LogonRequest logonRequest = new LogonRequest(this);
	        logonRequest.logon(userId, user.getBytes());
	        logonRequest.build();
        }
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
        if ((state != STATE_SESSION) && (state != STATE_ID)) {
            c12ProtocolLink.getLogger().info("PSEMServiceFactory, terminate - no terminate needed, because not in state SESSION STATE or ID STATE.");
        } else {
            /* When terminate is invoked due to an exception being thrown - it could be the case the c1222Buffer is not reset, but still is dirty (= contains info of last request/response).
            *  E.g.: This is the case when EAXPrimeEncoder throws an IOException
            *
            *  So we should reset the c1222Buffer, to be sure the buffer is clean.
             */
            if (c1222) {
                c1222Buffer.reset();
            }
            TerminateRequest terminateRequest = new TerminateRequest(this);
            terminateRequest.build();
            state = STATE_BASE;
        }
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
    	if (c1222) {
    		logOnC1222(c12UserId, c12User, password, securityLevel, passwordFormat, 80, 3);
        } else {
    		logOn(c12UserId, c12User, password, securityLevel, passwordFormat, 80, 3);
        }
    }

    public void logOnC1222(int c12UserId, String c12User, String password, int securityLevel, int passwordFormat, int packetSize, int nrOfPackets) throws IOException {
        doLogon(c12UserId,c12User); // ID -> SESSION
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

    public boolean isC1222() {
        return c1222;
    }

    public void setC1222(boolean c1222) {
        this.c1222 = c1222;
    }
}
