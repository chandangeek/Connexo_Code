/*
 * ABBA1700RegisterReader.java
 *
 * Created on 28 april 2003, 12:08
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class ABBA1700DataIdentity {

    protected static final boolean STREAMEABLE=true;
    protected static final boolean NOT_STREAMEABLE=false;

    private byte[][] dataBlocks=null;

    private int length;
    private int sets;
    private boolean streameable;

    private ABBA1700DataIdentityFactory abba1700DataIdentityFactory=null;

    protected void setABBA1700DataIdentityFactory(ABBA1700DataIdentityFactory abba1700DataIdentityFactory) {
        this.abba1700DataIdentityFactory=abba1700DataIdentityFactory;
    }

    private ABBA1700DataIdentityFactory getABBA1700DataIdentityFactory() {
        return abba1700DataIdentityFactory;
    }

    public int getLength() {
        return length;
    }

    protected boolean isStreameable() {
        return streameable;
    }

    /** Creates a new instance of ABBA1700RegisterReader */
    protected ABBA1700DataIdentity(int length, boolean streameable) {
        this(length,1,streameable);
    }
    protected ABBA1700DataIdentity(int length, int sets, boolean streameable) {
        this.length = length;
        this.sets = sets;
        this.dataBlocks=new byte[sets][];
        this.streameable = streameable;
    }


    protected void writeRawRegister(String dataID, String value) throws FlagIEC1107ConnectionException,IOException {
        dowriteRawRegister(dataID,value);
        resetRegdata();
    }

    private void dowriteRawRegister(String dataID, String value) throws FlagIEC1107ConnectionException,IOException {
        String data = dataID+"001("+value+")";
        String retVal = getABBA1700DataIdentityFactory().getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrameAndReturn(FlagIEC1107Connection.WRITE1,data.getBytes());
        if ((retVal != null) && (retVal.indexOf("ERR") != -1))
            throw new IOException(retVal+" received! Write command failed! Possibly wrong password level!");
    }

    // streaming...
    // read register in the meter if not cached
    public byte[] readRawRegisterStream(String dataID, boolean cached, int nrOfBlocks) throws FlagIEC1107ConnectionException,IOException {
       if (getSets() != 1)
           throw new IOException("ABBA1700DataIdentity, readRawRegisterStream, error nr of sets != 1 !!!, use of method not allowed");
       if ((!cached) || (dataBlocks[0] == null)) {
            dataBlocks[0] = doReadRawRegisterStream(dataID,cached,nrOfBlocks);
       }
       return dataBlocks[0];
    }

    // read register in the meter if not cached
    protected byte[] readRawRegister(String dataID,boolean cached,int dataLength, int set) throws FlagIEC1107ConnectionException,IOException {
       if ((!cached) || (dataBlocks[set] == null)) {
            // KV 16062004 changed to use streaming if possible...
            if (getABBA1700DataIdentityFactory().getProtocolLink().isIEC1107Compatible()) {
                dataBlocks[set] = doReadRawRegister(dataID,dataLength,set);
            }
            else {
                if (isStreameable()) {
                   dataBlocks[set] = splitAndReturnDataBlocks(doReadRawRegisterStream(dataID,cached,((dataLength/256)+1)*getSets()),set);
                }
                else {
                   dataBlocks[set] = doReadRawRegister(dataID,dataLength,set);
                }
            }
       }
       return dataBlocks[set];
    }

    /*
     *   In case of streaming mode, we do not read sets separately, so we should fill the complete array...
     */
    private byte[] splitAndReturnDataBlocks(byte[] dataBlock,int set) {
        for (int i=0; i< getSets();i++) {
           dataBlocks[i] = ProtocolUtils.getSubArray2(dataBlock,getLength()*i, getLength());
        }
        return dataBlocks[set];
    }

    private byte[] doReadRawRegisterStream(String dataID, boolean cached, int nrOfBlocks) throws FlagIEC1107ConnectionException,IOException {
       byte[] dataBlock=null;
       String data = dataID+"001("+Integer.toHexString(nrOfBlocks)+")";

       // KV 27102004
       int iRetries = 0;
       while(true) {
           try {
              getABBA1700DataIdentityFactory().getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READSTREAM,data.getBytes());
              dataBlock = getABBA1700DataIdentityFactory().getProtocolLink().getFlagIEC1107Connection().receiveStreamData();
              break;
           }
           catch(FlagIEC1107ConnectionException e) {
               if (iRetries++ >= getABBA1700DataIdentityFactory().getProtocolLink().getNrOfRetries())
                   throw e;
//System.out.println("KV_DEBUG> streaming error reason "+e.getReason());
               getABBA1700DataIdentityFactory().getProtocolLink().getFlagIEC1107Connection().breakStreamingMode();
           }
       }


       String str = new String(dataBlock);
       // KV 19012004
       if (str.indexOf("ERR") != -1) {
           String exceptionId = str.substring(str.indexOf("ERR"),str.indexOf("ERR")+4);
           throw new FlagIEC1107ConnectionException("ABBA1700DataIdentity, readRawRegisterStream, "+getABBA1700DataIdentityFactory().getMeterExceptionInfo().getExceptionInfo(exceptionId));
       }
       return dataBlock;
    }

    private static final int AUTHENTICATE_REARM=270000;

    // normal iec1107 read method
    // read register in the meter
    private byte[] doReadRawRegister(String dataID,int dataLen,int set) throws FlagIEC1107ConnectionException,IOException {
        byte[] dataBlock=null;
        long timeout = System.currentTimeMillis() + AUTHENTICATE_REARM; // After 4,5 min, do authentication before continue! otherwise we can receive ERR5, password timeout!
        if (dataLen <= 0) throw new FlagIEC1107ConnectionException("ABBA1700DataIdentity, doReadRawRegister, wrong dataLength ("+dataLen+")!");
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int packetid = ((dataLen/64) + ((dataLen%64)==0?0:1)) * set + 1; // calculate packetid
        int dataLength=dataLen;
        while (dataLength > 0) {
            int len = ((dataLength/64)>0) ? 64: dataLength%64;
            dataLength-=64;
            StringBuffer strbuff = new StringBuffer();
            strbuff.append(dataID);
            strbuff.append(buildPacketID(packetid++,3));
            strbuff.append('(');
            strbuff.append(Integer.toHexString(len));
            strbuff.append(')');
            getABBA1700DataIdentityFactory().getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ1,strbuff.toString().getBytes());
            byte[] ba = getABBA1700DataIdentityFactory().getProtocolLink().getFlagIEC1107Connection().receiveData();

            if (ba.length != (len*2))
                throw new FlagIEC1107ConnectionException("ABBA1700DataIdentity, doReadRawRegister, data length received ("+ba.length+") is different from data length requested ("+(len*2)+") !");

            String str = new String(ba);
            // KV 19012004
            if (str.indexOf("ERR") != -1) {
                String exceptionId = str.substring(str.indexOf("ERR"),str.indexOf("ERR")+4);
                throw new FlagIEC1107ConnectionException("ABBA1700DataIdentity, doReadRawRegister, "+getABBA1700DataIdentityFactory().getMeterExceptionInfo().getExceptionInfo(exceptionId));
            }
            data.write(ba);

            if (((long) (System.currentTimeMillis() - timeout)) > 0) {
                timeout = System.currentTimeMillis() + AUTHENTICATE_REARM; // arm again...
                getABBA1700DataIdentityFactory().getProtocolLink().getFlagIEC1107Connection().authenticate();
            }

        } // while (dataLength > 0)
        dataBlock = ProtocolUtils.convert2ascii(data.toByteArray());
        return dataBlock;
    }

    private String buildPacketID(int packetID,int length) {
        String str=Integer.toHexString(packetID);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length())
            for (int i=0;i<(length-str.length());i++)
                strbuff.append('0');
        strbuff.append(str);
        return strbuff.toString();
    }

    protected void resetRegdata() {
       dataBlocks = new byte[getSets()][];
    }

    /**
     * Getter for property sets.
     * @return Value of property sets.
     */
    public int getSets() {
        return sets;
    }

} // public class ABB1700RawRegister
