package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**@author fbo */

public class ABBA230DataIdentity {

    static protected final boolean STREAMEABLE=true;
    static protected final boolean NOT_STREAMEABLE=false;

    private byte[][] dataBlocks=null;

    private String dataId;
    private int length;
    private int sets;
    private boolean streameable;
    private ABBA230DataIdentityFactory dataIdentityFactory=null;

    /** Create new A230identity
     * @param dataId
     * @param dataIdentityFactory
     * @param length in bytes
     * @param sets nr of sets
     * @param streameable
     */
    ABBA230DataIdentity(String dataId, int length, int sets, boolean streameable, ABBA230DataIdentityFactory dataIdentityFactory) {
        this.dataId = dataId;
        this.length = length;
        this.sets = sets;
        this.dataBlocks=new byte[sets][];
        this.streameable = streameable;
        this.dataIdentityFactory = dataIdentityFactory;
    }

    ABBA230DataIdentity(String dataId, ABBA230DataIdentityFactory dataIdentityFactory) {
        this.dataId = dataId;
        this.length = -1;
        this.sets = -1;
        this.dataBlocks=null;
        this.streameable = false;
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
    byte[] read(boolean cached,int dataLength, int set) throws FlagIEC1107ConnectionException,IOException {
        if ((!cached) || (dataBlocks[set] == null)) {
            boolean useIec1107 = dataIdentityFactory.getProtocolLink().isIEC1107Compatible();
            if( streameable && !useIec1107 ) {
                int nr256Sets = ( (dataLength/256) + ( ( (dataLength%256) > 0 ) ? 1 : 0  ) );
                int setLength = nr256Sets * 256;
                int nrBlocks = nr256Sets * getSets();

                byte [] r = doReadRawRegisterStream( cached, nrBlocks );
                for( int i = 0; i < getSets(); i ++ ) {
                	dataBlocks[i] = ProtocolUtils.getSubArray2(r,setLength*i, getLength());
                }
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
    byte[] readStream(boolean cached, int nrOfBlocks) throws FlagIEC1107ConnectionException,IOException {
        if (getSets() != 1) {
			throw new IOException("ABBA230DataIdentity, readRawRegisterStream, error nr of sets != 1 !!!, use of method not allowed");
		}
        if ((!cached) || (dataBlocks[0] == null)) {
            dataBlocks[0] = doReadRawRegisterStream(cached,nrOfBlocks);
        }
        return dataBlocks[0];
    }


    protected void writeRawRegister(int packet, String value) throws FlagIEC1107ConnectionException,IOException {
        String data = dataId+ProtocolUtils.buildStringDecimal(packet,3)+"("+value+")";
        String retVal = dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrameAndReturn(FlagIEC1107Connection.WRITE1,data.getBytes());
        if ((retVal != null) && (retVal.indexOf("ERR") != -1)) {
			throw new IOException(retVal+" received! Write command failed! Possibly wrong password level!");
		}
    }
    protected void writeRawRegisterHex(int packet, String value) throws FlagIEC1107ConnectionException,IOException {
        String data = dataId+ProtocolUtils.buildStringHex(packet,3)+"("+value+")";
        String retVal = dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrameAndReturn(FlagIEC1107Connection.WRITE1,data.getBytes());
        if ((retVal != null) && (retVal.indexOf("ERR") != -1)) {
			throw new IOException(retVal+" received! Write command failed! Possibly wrong password level!");
		}
    }

    /** Write value to register.
     * @param value
     * @throws com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException
     * @throws java.io.IOException
     */
    void writeRawRegister(String value) throws FlagIEC1107ConnectionException,IOException {
        String data = dataId+"001("+value+")";
        String retVal = dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrameAndReturn(FlagIEC1107Connection.WRITE1,data.getBytes());
        if ((retVal != null) && (retVal.indexOf("ERR") != -1)) {
			throw new IOException(retVal+" received! Write command failed! Possibly wrong password level!");
		}
        dataBlocks = new byte[getSets()][];
    }

    private byte[] doReadRawRegisterStream( boolean cached, int nrOfBlocks) throws FlagIEC1107ConnectionException,IOException {
        byte[] dataBlock=null;
        String data = dataId+"001("+Integer.toHexString(nrOfBlocks)+")";

        int iRetries = 0;
        while(true) {
            try {
                dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READSTREAM,data.getBytes());
                dataBlock = dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().receiveStreamData();
                break;
            } catch(FlagIEC1107ConnectionException e) {
                if (iRetries++ >= dataIdentityFactory.getProtocolLink().getNrOfRetries()) {
					throw e;
				}
                dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().breakStreamingMode();
            }
        }

        String str = new String(dataBlock);
        if (str.indexOf("ERR") != -1) {
            String exceptionId = str.substring(str.indexOf("ERR"),str.indexOf("ERR")+4);
            throw new FlagIEC1107ConnectionException("ABBA230DataIdentity, readRawRegisterStream, "+dataIdentityFactory.getMeterExceptionInfo().getExceptionInfo(exceptionId));
        }
        return dataBlock;
    }

    private static final int AUTHENTICATE_REARM=270000;

    // normal iec1107 read method
    // read register in the meter
    private byte[] doReadRawRegister(int dataLen,int set) throws FlagIEC1107ConnectionException,IOException {
        byte[] dataBlock=null;
        long timeout = System.currentTimeMillis() + AUTHENTICATE_REARM; // After 4,5 min, do authentication before continue! otherwise we can receive ERR5, password timeout!
        if (dataLen <= 0) {
			throw new FlagIEC1107ConnectionException("ABBA230DataIdentity, doReadRawRegister, wrong dataLength ("+dataLen+")!");
		}
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int packetid = ((dataLen/64) + ((dataLen%64)==0?0:1)) * set + 1; // calculate packetid
        int dataLength=dataLen;
        while (dataLength > 0) {
            int len = ((dataLength/64)>0) ? 64: dataLength%64;
            dataLength-=64;
            StringBuffer strbuff = new StringBuffer();
            strbuff.append(dataId);
            strbuff.append(buildPacketID(packetid++,3));
            strbuff.append('(');
            strbuff.append(ProtocolUtils.buildStringHex(len,2));
            strbuff.append(')');
            dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ1,strbuff.toString().getBytes());
            byte[] ba = dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().receiveData();


            String str = new String(ba);
            if (str.indexOf("ERR") != -1) {
                String exceptionId = str.substring(str.indexOf("ERR"),str.indexOf("ERR")+4);
                throw new FlagIEC1107ConnectionException("ABBA230DataIdentity, doReadRawRegister, "+dataIdentityFactory.getMeterExceptionInfo().getExceptionInfo(exceptionId));
            }

            if (ba.length != (len*2)) {
				throw new FlagIEC1107ConnectionException("ABBA230DataIdentity, doReadRawRegister, data length received ("+ba.length+") is different from data length requested ("+(len*2)+") !");
			}

            data.write(ba);

            if (((long) (System.currentTimeMillis() - timeout)) > 0) {
                timeout = System.currentTimeMillis() + AUTHENTICATE_REARM; // arm again...
                dataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().authenticate();
            }

        } // while (dataLength > 0)
        dataBlock = ProtocolUtils.convert2ascii(data.toByteArray());
        return dataBlock;
    }

    private String buildPacketID(int packetID,int length) {
        String str=Integer.toHexString(packetID);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length()) {
			for (int i=0;i<(length-str.length());i++) {
				strbuff.append('0');
			}
		}
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
