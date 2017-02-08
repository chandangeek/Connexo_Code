/*
 * Connection62056.java
 *
 * Created on 14 februari 2007, 9:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.Connection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 *
 * @author Koen
 * Changes:
 * GNA|22012009| Added a safetyTimeout to use in the transportLayer. If communication fails, then the communication time can be customized
 */
public class Connection62056 extends Connection  implements ProtocolConnection {

    private static final int DEBUG=0;


    InputStream inputStream;
    OutputStream outputStream;
    int timeout;
    int safetyTimeout;
    int maxRetries;
    long forcedDelay;
    int echoCancelling;
    HalfDuplexController halfDuplexController;
    String serialNumber;
    int securityLevel;
    int halfDuplex;
    int interKarTimeoutValue;
    String nodeId;
    private int t1Timeout;
    int sourceTransportAddress;
    int destinationTransportAddress;
    int delayAfterConnect;

    // protocollayers
    private Physical6205641 physical6205641;
    private Datalink6205641 datalink6205641;
    private Transport6205651 transport6205651;
    private LayerManager layerManager;

    /** Creates a new instance of TrimeranConnection
     * @param delayAfterConnect */
    public Connection62056(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController,
            String serialNumber,
            int securityLevel,
            int halfDuplex,
            int t1Timeout,
            int sourceTransportAddress,
            int destinationTransportAddress, int delayAfterConnect) {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout=timeout;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
        this.securityLevel=securityLevel;
        this.halfDuplex=halfDuplex;
        this.setT1Timeout(t1Timeout);
        this.sourceTransportAddress=sourceTransportAddress;
        this.destinationTransportAddress=destinationTransportAddress;
        this.delayAfterConnect = delayAfterConnect;

    } // EZ7Connection(...)

    public Connection62056(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			HalfDuplexController halfDuplexController,
			String infoTypeSerialNumber, int infoTypeSecurityLevel,
			int infoTypeHalfDuplex, int timeout, int sourceTransportAddress,
			int destinationTransportAddress, int delayAfterConnect,
			int safetyTimeout) throws ConnectionException {
    	this(inputStream, outputStream, timeout, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, infoTypeSerialNumber, infoTypeSecurityLevel, infoTypeHalfDuplex, safetyTimeout, sourceTransportAddress, destinationTransportAddress, delayAfterConnect);
    	this.safetyTimeout = safetyTimeout;
	}

	public void initProtocolLayers() {
        setPhysical6205641(new Physical6205641(this));
        getPhysical6205641().initPhysical();
        setDatalink6205641(new Datalink6205641(this));
        getDatalink6205641().initDatalink();
        setTransport6205651(new Transport6205651(this));
        getTransport6205651().initTransport();
        setLayerManager(new LayerManager(this));
        getLayerManager().init(sourceTransportAddress,destinationTransportAddress);
    }

    public com.energyict.protocol.meteridentification.MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException {
    	delayAndFlush(delayAfterConnect);
        this.nodeId=nodeId;
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) {
        return null;
    }

    public void disconnectMAC() {
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }

    public Physical6205641 getPhysical6205641() {
        return physical6205641;
    }

    private void setPhysical6205641(Physical6205641 physical6205641) {
        this.physical6205641 = physical6205641;
    }


    public Datalink6205641 getDatalink6205641() {
        return datalink6205641;
    }

    private void setDatalink6205641(Datalink6205641 datalink6205641) {
        this.datalink6205641 = datalink6205641;
    }

    public int getT1Timeout() {
        return t1Timeout;
    }

    private void setT1Timeout(int t1Timeout) {
        this.t1Timeout = t1Timeout;
    }

    public byte getTIMEOUT_ERROR() {
        return super.TIMEOUT_ERROR;
    }
    public byte getPROTOCOL_ERROR() {
        return super.PROTOCOL_ERROR;
    }


    public void sendData(byte[] data) throws IOException {
        if (DEBUG>=1){
        	System.out.println("KV_DEBUG> sendData, "+ProtocolUtils.outputHexString(data)+", "+System.currentTimeMillis());
        }
//        getHalfDuplexController().setRTS(true);
        sendOut(data);
//        getHalfDuplexController().setRTS(false);
    }

    private static final int WAIT_FOR_LENGTH=0;
    private static final int WAIT_FOR_FRAME=1;

    public byte[] receiveData() throws IOException {

        long interFrameTimeout;
        int kar;
        int count=0;
        int retry=0;
        int len=0;
        int checksum=0;
        int checksumRx=0;
        ByteArrayOutputStream frameArrayOutputStream = new ByteArrayOutputStream();
        int state = WAIT_FOR_LENGTH;

        int type=0;
        interFrameTimeout = System.currentTimeMillis() + getT1Timeout();

        frameArrayOutputStream.reset();

        copyEchoBuffer();
        while(true) {

            if ((kar = readIn()) != -1) {

                frameArrayOutputStream.write(kar);

                switch(state) {
                    case WAIT_FOR_LENGTH:
                        interFrameTimeout = System.currentTimeMillis() + getT1Timeout();
                        len=kar+4;
                        if ((kar >= 0) && (kar <= 0x7E)) {
                            if (DEBUG >= 1) {
								System.out.println("KV_DEBUG> collect frame with len="+len);
							}
                            state = WAIT_FOR_FRAME;
                            len--;
                        } else {
							frameArrayOutputStream.reset();
						}

                        break; // WAIT_FOR_LENGTH

                    case WAIT_FOR_FRAME:
                        if (len--<=1) {
                            if (DEBUG>=1) {
								System.out.println("KV_DEBUG> Frame received... "+ProtocolUtils.outputHexString(frameArrayOutputStream.toByteArray())+", "+System.currentTimeMillis());
							}
                            return (frameArrayOutputStream.toByteArray());
                        }
                        break; // WAIT_FOR_FRAME

                } // switch(state)

            } // if ((kar = readIn()) != -1)

            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
            }

        } // while(true)

    } // public byte[] receiveData()

    public Transport6205651 getTransport6205651() {
        return transport6205651;
    }

    public void setTransport6205651(Transport6205651 transport6205651) {
        this.transport6205651 = transport6205651;
    }

    public LayerManager getLayerManager() {
        return layerManager;
    }

    public void setLayerManager(LayerManager layerManager) {
        this.layerManager = layerManager;
    }

    public int getSafetyTimeout(){
    	return this.safetyTimeout;
    }

}
