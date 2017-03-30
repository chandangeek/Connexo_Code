/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EclConnection.java
 *
 * Created on 26. August 2004, 16:18
 */

package com.energyict.protocolimpl.gmc.base;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;

import com.energyict.dialer.connection.Connection;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
/**
 *
 * @author  weinert
 */
public class EclConnection extends Connection implements ProtocolConnection {

    private static final byte DEBUG=0;
    private static final int DELAY_AFTER_BREAK=2000; // KV 06102003

    ByteArrayOutputStream echoByteArrayOutputStream = new ByteArrayOutputStream();
//    ByteArrayInputStream echoByteArrayInputStream;

    private static final int TIMEOUT=600000;

    private static final byte UNKNOWN_ERROR=-1;
    private static final byte TIMEOUT_ERROR=-2;
    private static final byte SECURITYLEVEL_ERROR=-3; // KV 06072004

    private int iMaxRetries;

    // General attributes
//    private boolean boolAbort=false;
    private int iProtocolTimeout;
//    private int protocolCompatible;
//    ProtocolChannelMap protocolChannelMap;

    private static final byte SOH=0x01;
    private static final byte STX=0x02;
    private static final byte ETX=0x0A;
    private static final byte SUB=0x1A;
    private static final byte EOT=0x04;
    private static final byte ACK=0x06;
    private static final byte NAK=0x15;
    private static final byte CR=0x0D;
     private static final byte LF=0x0A;

//    private long lForceDelay;
//    private int iEchoCancelling;

//    private Encryptor encryptor;
    String strID=null;

    /** Creates a new instance of EclConnection */
    public EclConnection(InputStream inputStream,
    OutputStream outputStream,
    int iTimeout,
    int iMaxRetries,
    long lForceDelay,
    int iEchoCancelling,
    int protocolCompatible,
    Encryptor encryptor) throws ConnectionException {
        super(inputStream, outputStream, lForceDelay, iEchoCancelling);
        this.iMaxRetries = iMaxRetries;
//        this.lForceDelay = lForceDelay;
//        this.iEchoCancelling = iEchoCancelling;
//        this.protocolCompatible = protocolCompatible;
//        this.protocolChannelMap = protocolChannelMap;
//        this.encryptor=encryptor;
//        boolAbort = false;
        iProtocolTimeout=iTimeout;
    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {
        this.strID=strID;
        return null;
    }




    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }

    // KV 04122006
    public void setTimeDateString(String timeDateString) throws NestedIOException, ProtocolConnectionException {
        int retries=0;
        String strBuf = (char)STX + strID + ":,time//=" + timeDateString + (char)ETX;

        while(true) {
            try {

                sendRawData(strBuf.getBytes());
                receiveSUB();
                break;
            }
            catch (ConnectionException e) {
                if (retries++ >=iMaxRetries)
                    throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
                else {
                    //       sendBreak();
                    delay(DELAY_AFTER_BREAK); // KV 06102003
                }
            }
        } // while(true)

    } // public void setTimeDateString(String timeDateString) throws NestedIOException, ProtocolConnectionException {

    // KV 04122006
    public String getTimeDateString() throws NestedIOException, ProtocolConnectionException {
        int retries=0;
        String strBuf = (char)STX + strID + ":,time//" + (char)ETX;

        while(true) {
            try {

                sendRawData(strBuf.getBytes());
                // KV 16122003
                String receivedString = new String(receiveRawData());
                return receivedString;
            }
            catch (ConnectionException e) {
                if (retries++ >=iMaxRetries)
                    throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
                else {
                    //       sendBreak();
                    delay(DELAY_AFTER_BREAK); // KV 06102003
                }
            }
        } // while(true)

    } // public Date getTime() throws NestedIOException, ProtocolConnectionException


    public String getOldestPeriod(Date date) throws NestedIOException, ProtocolConnectionException {
        byte[] byBuf = null;
        byte[]  byStat; //  = new byte[10]; // KV 22072005
        short   sChannelCount;
        int			iStartByte = 0;
        String	strBuf;
        int retries=0;


        /* Sendetelegramm zusammenstellen */
        //strBuf = STX + strID + ":,eint/##& 1 *" + ETX;
        strBuf = (char)STX + strID + ":,eint/##& 1 *" + (char)ETX;

        while(true) {
            try {

                sendRawData(strBuf.getBytes());
                // KV 16122003
                byBuf = receiveRawData();

		sChannelCount = 0;
                byStat = strID.getBytes();

               	for(int i=0;i < byBuf.length;i++)
			if(byBuf[i] == 0x3B)
				sChannelCount++;
		if(sChannelCount != 2)
                    throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);


		/* Startkennung */
		if((byBuf[0] != 0x0D) && (byBuf[1] != 0x0A))
			throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);


		if(byStat[0] != byBuf[2])
			throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);


		// Station mit Kennungslnge 1
		if(strID.length() == 1)
		{
			iStartByte = 4;
			if(byBuf[3] != 0x3A)
				throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);

		}
		// Station mit Kennungslnge 2
		else
		{
			iStartByte = 5;
			if((byBuf[3] != byStat[1]) || (byBuf[4] != 0x3A))
				throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);

		}

		/* Datum- und Zeitwerte setzen */


                  /* Datum- und Zeitwerte setzen */
                  String telegram = bufferToString(byBuf,iStartByte,17);

                   // System.out.println(telegram);
                  //  if (ti.evaluateString(strDate, "dd.mm.yy;HH:MM:SS") != E_OK)
                  //      throw new ProtocolConnectionException("FlagECLConnection: invalid Date",SECURITYLEVEL_ERROR);

                return telegram;

            }
            catch (ConnectionException e) {
                if (retries++ >=iMaxRetries)
                    throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
                else {
                    //       sendBreak();
                    delay(DELAY_AFTER_BREAK); // KV 06102003
                }
            }
        } // while(true)
    }



    public String getLatestPeriod(Calendar date) throws NestedIOException, ProtocolConnectionException {
        byte[] byBuf = null;
        byte[]  byStat; //  = new byte[10]; // KV 22072005
        short   sChannelCount;
        int			iStartByte = 0;
        String	strBuf;
        int retries=0;

        /* Sendetelegramm zusammenstellen */
        //strBuf = STX + strID + ":,eint/##& 1 *" + ETX;
       // strBuf = (char)STX + strID + ":,eint/##& 1 *" + (char)ETX;
       	/* Uhrzeit-String zusammenstellen */


        String strDateTxt = ( date.get(Calendar.DAY_OF_MONTH) + "."+ (date.get(Calendar.MONTH)+1)+ "."+ date.get(Calendar.YEAR)+ " " + date.get(Calendar.HOUR_OF_DAY)+":" + date.get(Calendar.MINUTE)+ ":"+date.get(Calendar.SECOND));
		/* Sendetelegramm zusammenstellen */
	strBuf = (char) STX + strID + ":,index " + strDateTxt + ",eint/##& 1 . 1" + (char)ETX;



        while(true) {
            try {

                sendRawData(strBuf.getBytes());
                // KV 16122003
                byBuf = receiveRawData();

                /* Telegramm-Plausibilitt, berprfung der Semikolons */
		sChannelCount = 0;
                byStat = strID.getBytes();

               	for(int i=0;i < byBuf.length;i++)
			if(byBuf[i] == 0x3B)
				sChannelCount++;
		if(sChannelCount != 2)
                    throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);


		/* Startkennung */
		if((byBuf[0] != 0x0D) && (byBuf[1] != 0x0A))
			throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);


		/* berprfung der Stationskennung */
		if(byStat[0] != byBuf[2])
			throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);


		// Station mit Kennungslnge 1
		if(strID.length() == 1)
		{
			iStartByte = 4;
			if(byBuf[3] != 0x3A)
				throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);

		}
		// Station mit Kennungslnge 2
		else
		{
			iStartByte = 5;
			if((byBuf[3] != byStat[1]) || (byBuf[4] != 0x3A))
				throw new ProtocolConnectionException("FlagECLConnection: invalid SecurityLevel",SECURITYLEVEL_ERROR);

		}

		/* Datum- und Zeitwerte setzen */


                  /* Datum- und Zeitwerte setzen */
                  String telegram = bufferToString(byBuf,iStartByte,17);

                 //   System.out.println(telegram);
                  //  if (ti.evaluateString(strDate, "dd.mm.yy;HH:MM:SS") != E_OK)
                  //      throw new ProtocolConnectionException("FlagECLConnection: invalid Date",SECURITYLEVEL_ERROR);

                return telegram;

            }
            catch (ConnectionException e) {
                if (retries++ >=iMaxRetries)
                    throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
                else {
                    //       sendBreak();
                    delay(DELAY_AFTER_BREAK); // KV 06102003
                }
            }
        } // while(true)
    }




  public byte[] receiveRawDataFrame()throws NestedIOException, ProtocolConnectionException {
    byte[] byBuf = null;
    int retries=0;
   try {
        byBuf = receiveRawData();
        return byBuf;
   }
    catch (ConnectionException e) {
        if (retries++ >=iMaxRetries)
            throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
        else {
            //       sendBreak();
            delay(DELAY_AFTER_BREAK); // KV 06102003
        }
    }

    return byBuf;
 }

 public void sendLONCommandFrame(int channel) throws NestedIOException, ProtocolConnectionException{
      String strBuf = "";
     //  ProtocolChannelMap channelMap = new ProtocolChannelMap();
      int retries=0;
      /* Telegrammaufbau Sendetelegramm */
	//KV changed 05052006 strBuf = "LONZW " + channel + (char) CR + (char)LF;
       strBuf = strID + ": "+"LONZW " + channel + (char) CR + (char)LF;
       try {
           sendRawData(strBuf.getBytes());
       }
       catch (ConnectionException e) {
         if (retries++ >=iMaxRetries)
             throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
         else {
           //       sendBreak();
           delay(DELAY_AFTER_BREAK); // KV 06102003
         }
       }
 }

 public byte[] receiveLONFrame()throws NestedIOException, ProtocolConnectionException {
    byte[] byBuf = null;
    int retries=0;
   try {
        byBuf = receiveRawData();
        return byBuf;
   }
    catch (ConnectionException e) {
        if (retries++ >=iMaxRetries)
            throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
        else {
            //       sendBreak();
            delay(DELAY_AFTER_BREAK); // KV 06102003
        }
    }

  return byBuf;
 }

// public void sendRawCommandFrame(String strDate,long iGetPeriods,int iNumChannels) throws NestedIOException, ProtocolConnectionException{
 public void sendRawCommandFrame(String strDate,long iGetPeriods,int iNumChannels, int startChannel) throws NestedIOException, ProtocolConnectionException{

      String strBuf = "";
     //  ProtocolChannelMap channelMap = new ProtocolChannelMap();
      int retries=0;
      /* Telegrammaufbau Sendetelegramm */
//	strBuf = (char) STX + strID + ":,Index" + " " + strDate + ",EINT/##& 1.." + Integer.toString(iNumChannels) +  " . " + iGetPeriods + (char) ETX;
	strBuf = (char) STX + strID + ":,Index" + " " + strDate + ",EINT/##& "+startChannel+".." + ((iNumChannels-1)+startChannel) +  " . " + iGetPeriods + (char) ETX;
       try {
           sendRawData(strBuf.getBytes());
       }
       catch (ConnectionException e) {
         if (retries++ >=iMaxRetries)
             throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
         else {
           //       sendBreak();
           delay(DELAY_AFTER_BREAK); // KV 06102003
         }
       }

 }


    /** Translate byte to string
     * @param iPos Startoffset
     * @param iLen count of Bytes
     */
    // 14.12.98 VF
    public static String bufferToString(byte[] byData,
            int iPos, int iLen)
    {
            StringBuffer sbBuffer = new StringBuffer(iLen);
            int iEnd = iPos + iLen;

            for (int i = iPos; i < iEnd; i++)

                sbBuffer.append((char) (0xff & byData[i]));

            return sbBuffer.toString();
    }

    private static final byte STATE_WAIT_FOR_START=0;
    private static final byte STATE_WAIT_FOR_LENGTH=1;
    private static final byte STATE_WAIT_FOR_DATA=2;
    private static final byte STATE_WAIT_FOR_END=3;
    private static final byte STATE_WAIT_FOR_CHECKSUM=4;





    public byte[] receiveRawData() throws NestedIOException, ConnectionException, ProtocolConnectionException {
        return doReceiveData();
    }

    // 09.10.00 RW

     public String getStrID(){
        return strID;
     }





    public byte[] doReceiveData() throws NestedIOException, ConnectionException, ProtocolConnectionException {
        long lMSTimeout,lMSTimeoutInterFrame;
        int iNewKar;
        int iState;
        int iLength=0;
        byte[] receiveBuffer;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream resultArrayOutputStream  = new ByteArrayOutputStream();
        byte calculatedChecksum;
        boolean end;

        // init
        iState=STATE_WAIT_FOR_START;
        end=false;
        lMSTimeout = System.currentTimeMillis() + TIMEOUT;
        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
        resultArrayOutputStream.reset();
        byteArrayOutputStream.reset();

        if (DEBUG == 1) System.out.println("doReceiveData(...):");
        copyEchoBuffer();

        while(true) {

            if ((iNewKar = readIn()) != -1) {
                 //if (DEBUG == 1) ProtocolUtils.outputHex( ((int)iNewKar));
                if (DEBUG == 1) System.out.print((char)iNewKar);
                switch(iState) {
                    case STATE_WAIT_FOR_START: {

                        if ((byte)iNewKar == CR) {


                            iState = STATE_WAIT_FOR_END;
                            byteArrayOutputStream.write(iNewKar);
                        }


                        //                        if ((byte)iNewKar == NAK) {
                        //                            System.out.println("NAK RECEIVED...");
                        //                        }

                    } break; // STATE_WAIT_FOR_START

                    case STATE_WAIT_FOR_END: {                        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                        if ((byte)iNewKar == SUB) {
                            end = true;

                        }
                        byteArrayOutputStream.write(iNewKar);

                         if (end)
                         {// remove head and tail from byteArrayOutputStream.toByteArray()...
                            byte[] data = new byte[byteArrayOutputStream.toByteArray().length];
                            for (int i=0;i<(byteArrayOutputStream.toByteArray().length);i++)
                                data[i] = byteArrayOutputStream.toByteArray()[i];
                            try {
                                resultArrayOutputStream.write(data);
                            } catch ( IOException e) {
                                throw new ProtocolConnectionException("receiveStreamData(), IOException, "+e.getMessage());
                            }

                            return resultArrayOutputStream.toByteArray();
                         }


                    } break; // STATE_WAIT_FOR_END



                } // switch(iState)

            } // if ((iNewKar = readIn()) != -1)

            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new ProtocolConnectionException("doReceiveData() response timeout error",TIMEOUT_ERROR);
            }
            if (((long) (System.currentTimeMillis() - lMSTimeoutInterFrame)) > 0) {
                throw new ProtocolConnectionException("doReceiveData() interframe timeout error",TIMEOUT_ERROR);
            }


        } // while(true)

    } // public byte[] doReceiveData(String str) throws ProtocolConnectionException
    public void receiveSUB() throws NestedIOException, ConnectionException, ProtocolConnectionException {
        long lMSTimeout,lMSTimeoutInterFrame;
        int iNewKar;
        lMSTimeout = System.currentTimeMillis() + TIMEOUT;
        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;

        if (DEBUG == 1) System.out.println("doReceiveData(...):");
        copyEchoBuffer();

        while(true) {
            if ((iNewKar = readIn()) != -1) {
                System.out.print((char)iNewKar);
                if ((byte)iNewKar == SUB) {
                    return;
                }
            } // if ((iNewKar = readIn()) != -1)

            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new ProtocolConnectionException("doReceiveData() response timeout error",TIMEOUT_ERROR);
            }
            if (((long) (System.currentTimeMillis() - lMSTimeoutInterFrame)) > 0) {
                throw new ProtocolConnectionException("doReceiveData() interframe timeout error",TIMEOUT_ERROR);
            }
        } // while(true)
    } // public void receiveSUB() throws ProtocolConnectionException

}
