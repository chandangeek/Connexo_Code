package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**@author fbo */

public class ABBA1140DataIdentity {
    
    static protected final boolean STREAMEABLE=true;
    static protected final boolean NOT_STREAMEABLE=false;
    
    private byte[][] dataBlocks=null;
    
    private String dataId;
    private int length;
    private int sets;
    private boolean streameable;
    private ABBA1140DataIdentityFactory dataIdentityFactory=null;
    
    /** Create new ABBA1140identity
     * @param dataId 
     * @param dataIdentityFactory 
     * @param length in bytes
     * @param sets nr of sets
     * @param streameable 
     */
    ABBA1140DataIdentity(
            String dataId, int length, int sets, boolean streameable, ABBA1140DataIdentityFactory dataIdentityFactory) {
        this.dataId = dataId;
        this.length = length;
        this.sets = sets;
        this.dataBlocks=new byte[sets][];
        this.streameable = streameable;
        this.dataIdentityFactory = dataIdentityFactory;
    }
    
    int getLength() {
        return length;
    }

    int getSets() {
        return sets;
    }
    
    boolean isStreameable() {
        return streameable;
    }
    
    /** Read this DataIdentity
     * @param cached 
     * @param dataLength 
     * @param set index to start the reading from 
     * @throws com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException 
     * @throws java.io.IOException 
     * @return 
     */
    byte[] read(boolean cached,int dataLength, int set) throws IOException {
        if ((!cached) || (dataBlocks[set] == null)) {
            boolean useIec1107 = dataIdentityFactory.getProtocolLink().isIEC1107Compatible();
            if( streameable && !useIec1107 ) {
                int nr256Sets = ( (dataLength/256) + ( ( (dataLength%256) > 0 ) ? 1 : 0  ) ); 
                int setLength = nr256Sets * 256;
                int nrBlocks = nr256Sets * getSets();
                
                byte [] r = doReadRawRegisterStream( cached, nrBlocks );
                for( int i = 0; i < getSets(); i ++ ) 
                    dataBlocks[i] = ProtocolUtils.getSubArray2(r,setLength*i, getLength());
            } else {
                dataBlocks[set] = doReadRawRegister(dataLength,set);
            }
        }
        return dataBlocks[set];
    }
    
    /** Read register as Stream.
     * @param cached 
     * @param nrOfBlocks 
     * @throws com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException 
     * @throws java.io.IOException 
     * @return 
     */
    byte[] readStream(boolean cached, int nrOfBlocks) throws IOException {
        if (getSets() != 1)
            throw new IOException("ABBA1140DataIdentity, readRawRegisterStream, error nr of sets != 1 !!!, use of method not allowed");
        if ((!cached) || (dataBlocks[0] == null)) {
            dataBlocks[0] = doReadRawRegisterStream(cached,nrOfBlocks);
        }
        return dataBlocks[0];
    }
    
    /** Write value to register.
     * @param value 
     * @throws com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException 
     * @throws java.io.IOException 
     */
    void writeRawRegister(String value) throws IOException {
        String data = dataId+"001("+value+")";
        String retVal = dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrameAndReturn(FlagIEC1107Connection.WRITE1,data.getBytes());
        if ((retVal != null) && (retVal.indexOf("ERR") != -1))
            throw new IOException(retVal+" received! Write command failed! Possibly wrong password level!");
        dataBlocks = new byte[getSets()][];
    }
    
    private byte[] doReadRawRegisterStream( boolean cached, int nrOfBlocks) throws IOException {
        byte[] dataBlock=null;
        String data = dataId+"001("+Integer.toHexString(nrOfBlocks)+")";
        
        int iRetries = 0;
        while(true) {
            try {
                dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READSTREAM,data.getBytes());
                dataBlock = dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().receiveStreamData();
                break;
            } catch(FlagIEC1107ConnectionException e) {
                if (iRetries++ >= dataIdentityFactory.getProtocolLink().getNrOfRetries())
                    throw e;
                dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().breakStreamingMode();
            }
        }
        
        String str = new String(dataBlock);
        if (str.indexOf("ERR") != -1) {
            String exceptionId = str.substring(str.indexOf("ERR"),str.indexOf("ERR")+4);
            throw new FlagIEC1107ConnectionException("ABBA1140DataIdentity, readRawRegisterStream, "+dataIdentityFactory.getMeterExceptionInfo().getExceptionInfo(exceptionId));
        }
        return dataBlock;
    }
    
    private static final int AUTHENTICATE_REARM=270000;
    
    // normal iec1107 read method
    // read register in the meter
    private byte[] doReadRawRegister(int dataLen,int set) throws IOException {
        byte[] dataBlock=null;
        long timeout = System.currentTimeMillis() + AUTHENTICATE_REARM; // After 4,5 min, do authentication before continue! otherwise we can receive ERR5, password timeout!
        if (dataLen <= 0) throw new FlagIEC1107ConnectionException("ABBA1140DataIdentity, doReadRawRegister, wrong dataLength ("+dataLen+")!");
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int packetid = ((dataLen/64) + ((dataLen%64)==0?0:1)) * set + 1; // calculate packetid
        int dataLength=dataLen;
        byte[] previousReceivedLoadProfileBytes = new byte[] {};
        while (dataLength > 0) {
            int len = ((dataLength/64)>0) ? 64: dataLength%64;
            dataLength-=64;
            StringBuffer strbuff = new StringBuffer();
            strbuff.append(dataId);
            strbuff.append(buildPacketID(packetid++,3));
            strbuff.append('(');
            strbuff.append(Integer.toHexString(len));
            strbuff.append(')');
            dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ1,strbuff.toString().getBytes());
            byte[] ba = dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().receiveData();

            if (ABBA1140RegisterFactory.loadProfileKey.equalsIgnoreCase(dataId)) {
                if (isCurrentReceivedDataDuplicate(previousReceivedLoadProfileBytes, ba)) {
                    throw new ProtocolException("Received the same data twice, unrecoverable error!");
                } else {
                    previousReceivedLoadProfileBytes = ba;
                }
            }
            
            if (ba.length != (len*2))
                throw new FlagIEC1107ConnectionException("ABBA1140DataIdentity, doReadRawRegister, data length received ("+ba.length+") is different from data length requested ("+(len*2)+") !");
            
            String str = new String(ba);
            if (str.indexOf("ERR") != -1) {
                String exceptionId = str.substring(str.indexOf("ERR"),str.indexOf("ERR")+4);
                throw new FlagIEC1107ConnectionException("ABBA1140DataIdentity, doReadRawRegister, "+dataIdentityFactory.getMeterExceptionInfo().getExceptionInfo(exceptionId));
            }
            data.write(ba);
            
            if (System.currentTimeMillis() - timeout > 0) {
                timeout = System.currentTimeMillis() + AUTHENTICATE_REARM; // arm again...
                dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().authenticate();
            }
            
        } // while (dataLength > 0)
        dataBlock = ProtocolUtils.convert2ascii(data.toByteArray());
        return dataBlock;
    }

    private static boolean isCurrentReceivedDataDuplicate(byte[] previousReceivedBytes, byte[] currentReceivedBytes) {
        return currentReceivedBytes.length == previousReceivedBytes.length &&
                Arrays.equals(currentReceivedBytes, previousReceivedBytes) &&
                !containsOnlyAsciiF(currentReceivedBytes);
    }

    private static boolean containsOnlyAsciiF(byte[] inputBytes) {
        for (byte item : inputBytes) {
            if (item != (byte) 0x46) {
                return false;
            }
        }
        return true;
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
    
    public String toString(){
        return new StringBuffer()
            .append( "DataIdentity[ dataId=" + dataId + ", length=" + length )
            .append( ", sets= " + sets + ", streamable=" + streameable + "]" )
            .toString();
    }
    
}
