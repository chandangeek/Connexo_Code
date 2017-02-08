/*
 * RtuPlusBusFrames.java
 *
 * Created on 24 februari 2003, 9:37
 */

package com.energyict.protocolimpl.rtuplusbus;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;


/**
 *
 * @author  Stefan Grosjean
 */

public class RtuPlusBusFrames {

    private static final int DEBUG=0;

    private int     iNodeID;
    private long    lPassword;
    private int     iProtocolTimeoutProperty;
    private int     iProtocolRetriesProperty;
    private int     iDelayAfterFailProperty;
    private int     iRtuPlusBusProtocolVersion;
    private OutputStream    outputStream;
    private InputStream     inputStream;
    private boolean         boolAbort;
    private int     iNbrOfErrors=0;

    private static final int minServerId = 0x20;
    private static final int maxServerId = 0x7f;

    private int iServerId = minServerId;
    private int iPrevServerId = 0x1e;

    private Date prevDate = null;

    // PROTOCOL VERSION
    private static final int PROTOCOL_NT = 1;
    private static final int PROTOCOL_V4 = 2;

    // Protocol RtuPlusBus
    private static final byte FRAME_DESTINATION=1;
    private static final byte FRAME_DESTINATION_COMPL = 2;
    private static final byte FRAME_SOURCE = 3;
    private static final byte FRAME_SOURCE_COMPL = 4;
    private static final byte FRAME_DATASIZE = 5;
    private static final byte FRAME_DATASIZE_COMPL= 6;
    private static final byte FRAME_COMMAND = 7;
    private static final byte FRAME_COMMAND_COMPL = 8;
    private static final byte SOH=0x01;

    // States for the ReadFrame statemachine
    private static final int STATE_SOH = 1;
    private static final int STATE_DESTINATION = 2;
    private static final int STATE_DESTINATION_COMPL = 3;
    private static final int STATE_SOURCE = 4;
    private static final int STATE_SOURCE_COMPL = 5;
    private static final int STATE_DATASIZE = 6;
    private static final int STATE_DATASIZE_COMPL = 7;
    private static final int STATE_COMMAND = 8;
    private static final int STATE_COMMAND_COMPL = 9;
    private static final int STATE_DATA = 10;
    private static final int STATE_CHECKSUM_MSB = 11;
    private static final int STATE_CHECKSUM_LSB = 12;

    Logger logger=null;

    HalfDuplexController halfDuplexController;
    private int forcedDelay;

    // ********************** RtuPlusBus command frame *************************

    /** Creates a new instance of RtuPlusBusFrames */
    public RtuPlusBusFrames() {

       doRestart();
       setNodeID( 3 );              // Default node = 3
       setPassword( 1207895123 );   // Default password
       setProtocolProperties( 1000, 1, 100, PROTOCOL_V4 );    // Default ProtocolVersion = V4 version!
       iNbrOfErrors=0;
    }

    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController=halfDuplexController;
    }

    public void setLogger( Logger alogger )
    { this.logger = alogger;
    }

    public void setNodeID( int aiNodeID )
    { this.iNodeID = aiNodeID;
    }

    public void setPassword( long alPassword )
    { this.lPassword = alPassword;
    }

    public void setInputStream( InputStream aiStream )
    { this.inputStream = aiStream;
    }

    public void setOutputStream( OutputStream aoStream )
    { this.outputStream = aoStream;
    }

    public void setProtocolProperties( int aiTimeout, int aiRetries, int aiDelay, int aiRtuPlusBusProtocolVersion ) {
        this.iProtocolTimeoutProperty = aiTimeout;
        this.iProtocolRetriesProperty = aiRetries;
        this.iDelayAfterFailProperty = aiDelay;
        this.iRtuPlusBusProtocolVersion = aiRtuPlusBusProtocolVersion;
    }

    public void doAbort()
    { boolAbort = true;     // Set the Stop flag..
      iNbrOfErrors = 9999;  // Set Errorcount high to help stopping ..
    }

    public void doRestart()
    { boolAbort = false;
    }

    public void doWrite( byte abCommand ) throws RtuPlusBusException
    { int[] liData;
      liData = new int[0];
      doWriteAndReadI( iNodeID, abCommand, liData, false );
    }

    public void doWrite( byte abCommand, int[] aiData ) throws RtuPlusBusException
    { doWriteAndReadI( iNodeID, abCommand, aiData, false );
    }

    public int[] doWriteAndReadI( byte abCommand ) throws RtuPlusBusException
    { int[] liData;
      liData = new int[0];
      return( doWriteAndReadI( iNodeID, abCommand, liData, true ));
    }

    public int[] doWriteAndReadI( byte abCommand, int[] aiData ) throws RtuPlusBusException
    { return( doWriteAndReadI( iNodeID, abCommand, aiData, true ));
    }

    // Write with Read, returning the received data as a String
    public String doWriteAndReadS( byte abCommand ) throws RtuPlusBusException
    { int[] liRcvData;
      byte[] lbRcvData;
      String lsRcvData;
      int i;

      liRcvData = doWriteAndReadI( abCommand );

      if( liRcvData != null ) {
          lbRcvData = new byte[ liRcvData.length ];
          for( i=0 ; i< liRcvData.length ; i++ )
              lbRcvData[ i ] = (byte )( liRcvData[i] & 0xFF );
          lsRcvData = new String( lbRcvData );
          return( lsRcvData );
      }
      return "";
    }

    // Write with Read returning array of Integers
    public int[] doWriteAndReadI( int aiDest, byte abCommand, int[] aiData, boolean abReadResponse ) throws RtuPlusBusException {
        int ipos;
        int iposdata;
        int iChkSum;
        int[] liRcvData;
        byte ibDest;
        byte ibSource;
        byte ibDataSize;
        byte[] abBuffer;
        abBuffer = new byte[256];

        // Global error counter..

        try {
            while( iNbrOfErrors <= iProtocolRetriesProperty ) {

                if( iNbrOfErrors > 0 )                          // Suppose this communication will be OK!
                    iNbrOfErrors--;

                ipos=iposdata=iChkSum=0;
                ibDest =    (byte )((aiDest  ) & 0xFF);
                // Change the Source NodeID per Retry/Transmission to distinct the packets .. replaces sequence..
                ibSource =  (byte )((getNextServerId()) & 0xFF);
                ibDataSize =(byte )((aiData.length) & 0xFF);

                // Size of dataframe to be increased by one if command byte is given
                if( iRtuPlusBusProtocolVersion == PROTOCOL_V4 )      // Older Protocols (<> V4) do not consider the Command as DataByte
                    if( abCommand != 0 )
                        ibDataSize++;

                abBuffer[ipos++]                  = SOH;
                abBuffer[ipos++]                  = ibDest;
                abBuffer[ipos++]                  = (byte )(~ibDest & 0xFF );
                abBuffer[ipos++]                  = ibSource;
                abBuffer[ipos++]                  = (byte )(~ibSource & 0xFF );
                abBuffer[ipos++]                  = ibDataSize;
                abBuffer[ipos++]                  = (byte )(~ibDataSize & 0xFF);

                if( abCommand != 0 ) {

                    if (DEBUG >= 1) System.out.println("Command = " + abCommand + " " + rtuplusbus.cmdToString(abCommand) );
                    abBuffer[ipos++] = abCommand;

                    if( iRtuPlusBusProtocolVersion == PROTOCOL_V4 )           // Command not in Checksum in older protocol <> V4 !
                    { if( abCommand < 0 ) iChkSum = - (int )abCommand;
                      else iChkSum = (int )abCommand;
                    }
                }

                // Data to send, not all command have data to send!
                for( iposdata=0; iposdata < aiData.length ; iposdata++ )
                { abBuffer[ ipos++ ] = (byte )(aiData[ iposdata ] & 0xFF );
                  iChkSum += aiData[ iposdata ];
                }

                // Add the checksum
                abBuffer[ipos++] = (byte )((iChkSum / 256) & 0xFF);
                abBuffer[ipos++] = (byte )((iChkSum % 256) & 0xFF);


                try {
                   Thread.sleep(forcedDelay);
                }
                catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }

                if (halfDuplexController != null)
                    halfDuplexController.request2Send(ipos);
                outputStream.write(abBuffer, 0, ipos );
                if (halfDuplexController != null)
                    halfDuplexController.request2Receive(ipos);


                // Read the response from the RTU or not???  Some commands (f.i. WriteClock) do not send a response..
                if( abReadResponse == true ) {
                    try
                    {
                        liRcvData = doRTUReadFrame( );
                        if( liRcvData != null )
                        {

                            if (DEBUG >= 1) ProtocolUtils.printResponseData(liRcvData);

                            if( isProfileReadCommand( abCommand )  )
                            {
                                if( isNextEntry( liRcvData ) )
                                    return( liRcvData );
                                else {
                                    abCommand = rtuplusbus.CMD_READ_NEXT_RECORD;

                                }

                            }
                            else
                            {
                                return( liRcvData );
                            }

                        }
                    }
                    catch( RtuPlusBusException rbe )
                    {
                        String msg =    rbe.getLogMessage() + ", "
                                     +  ( ( rbe.getMessage() != null ) ? rbe.getMessage() : "" )
                                     + ", (handling) CMD="
                                     +  rtuplusbus.cmdToString(abCommand);
                        logger.warning( msg );
                        waitForSilence(); // for robusteness wait

                        if( DEBUG >= 1 ) System.out.println( rtuplusbus.cmdToString( abCommand ) + " " + rbe.getLogMessage() );
                        if( abCommand == rtuplusbus.CMD_READ_NEXT_RECORD )
                            abCommand = rtuplusbus.CMD_READ_SAME_RECORD;
                        else
                            if( DEBUG >= 1 ) System.out.println("do not read same!!");
                        if( DEBUG >= 1 ) System.out.println( "new command = " + rtuplusbus.cmdToString( abCommand ) );
                    }

                    iNbrOfErrors += 2;  // Add 2 when bad, substract 1 when OK
                    logger.warning( Integer.toString( ( iNbrOfErrors - 1 ) ) );

                }
                else return null;

            } // while( iNbrOfErrors <= iProtocolRetriesProperty ) {
        }
        catch (IOException e)
        {  throw new RtuPlusBusException( "Error writing RtuPlusBus Frame: " + e.getMessage(), e );
        }

        throw new RtuPlusBusException("Timeout receiving data!");

    } // public int[] doWriteAndReadI( int aiDest, int aiSource, byte abCommand, int[] aiData, boolean abReadResponse ) throws RtuPlusBusException {



    private int[] doRTUReadFrame( ) throws RtuPlusBusException {
        long lMSTimeout;
        int iCurrentState;
        int iCurrByte, iPos;
        int iDestination, iSource, iDataSize, iCommand;
        int iCheckSum, iRcvdCheckSum;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        iDestination=iSource=iDataSize=iCommand=iPos=iCheckSum=iRcvdCheckSum=0;
        lMSTimeout = System.currentTimeMillis() + iProtocolTimeoutProperty;

        try {
            iCurrentState= STATE_SOH;
            while(!boolAbort) {
                if( inputStream.available() != 0) {
                    iCurrByte = inputStream.read();
                    switch( iCurrentState ) {
                        case STATE_SOH :
                            if( iCurrByte == SOH ) {
                                iCurrentState = STATE_DESTINATION;
                            }
                            break;

                        case STATE_DESTINATION:
                            iDestination = iCurrByte;
                            iCurrentState = STATE_DESTINATION_COMPL;
                            break;

                        case STATE_DESTINATION_COMPL:
                            if( iCurrByte == (( ~ iDestination) & 0xFF) ) {
                                iCurrentState = STATE_SOURCE;
                            } else {
                                throw new RtuPlusBusException(RtuPlusBusException.DEST_COMPLEMENT);
                            }
                            break;

                        case STATE_SOURCE:
                            iSource = iCurrByte;
                            iCurrentState = STATE_SOURCE_COMPL;
                            break;

                        case STATE_SOURCE_COMPL:
                            if( iCurrByte == (( ~ iSource) & 0xFF) ) {
                                iCurrentState = STATE_DATASIZE;
                            } else {
                                throw new RtuPlusBusException(RtuPlusBusException.SRC_COMPLEMENT);
                            }
                            break;

                        case STATE_DATASIZE:
                            iDataSize = iCurrByte;
                            iCurrentState = STATE_DATASIZE_COMPL;
                            break;

                        case STATE_DATASIZE_COMPL:
                            if( iCurrByte == ((~iDataSize) & 0xFF) )
                            { iCurrentState = STATE_DATA;
                              iCheckSum = 0;
                              iPos=0;
                            }
                            else {
                                throw new RtuPlusBusException(RtuPlusBusException.DATA_SIZE_COMPLEMENT);
                            }
                            break;

                        case STATE_DATA:
                            iCheckSum += iCurrByte;
                            iPos++;
                            bos.write(iCurrByte & 0xFF);
                            if( iDataSize == iPos )  // Received all databytes ..
                            {
                                iCurrentState = STATE_CHECKSUM_MSB;
                            }
                            break;

                        case STATE_CHECKSUM_MSB:
                            iRcvdCheckSum = iCurrByte << 8;
                            iCurrentState = STATE_CHECKSUM_LSB;
                            break;

                        case STATE_CHECKSUM_LSB:
                            iRcvdCheckSum += iCurrByte;
                            if (iRcvdCheckSum == iCheckSum) {
                                //System.out.println(" iPrevServerId " + iPrevServerId);
                                if (iDestination == getServerId() ) {
                                    // Checksum is OK and Frame is for us!
                                    return(ProtocolUtils.toIntArray(bos.toByteArray()));
                                }
                                throw new RtuPlusBusException( RtuPlusBusException.SERVER_ID_SMALL );

                            }
                            else {
                                throw new RtuPlusBusException(RtuPlusBusException.CHECKSUM);
                            }

                        default:
                            break;
                    }
                }
                else {
                    Thread.sleep( 100 ); // KV 03062003
                }

                if( System.currentTimeMillis() - lMSTimeout > 0) {
                    // inputStream.close();
                    //System.out.println("timeout");
                    throw new RtuPlusBusException( "currentState=" + iCurrentState, RtuPlusBusException.TIME_OUT_ERROR );
                    //return null;  // Timeout
                }
            } // while(boolAbort==false)
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        catch( IOException e) {
            //System.out.println("IOException thrown");
            throw new RtuPlusBusException(e.getMessage(), e);
        }
        logger.warning( "." ); // -> means retry
        return null;
    }

    private int[] doRTUReadFrameOriginal(int aiServerID ) throws RtuPlusBusException {
        long lMSTimeout;
        int iCurrentState;
        int iCurrByte, iPos;
        int iDestination, iSource, iDataSize, iCommand;
        int iCheckSum, iRcvdCheckSum;
        int[] liReceivedData;
        liReceivedData = new int[0];
        /*InputStreamDecorator inputStreamD;
        inputStreamD = new InputStreamDecorator( inputStream );*/
        // KV 03062003
        BufferedInputStream inputStreamD = new BufferedInputStream(inputStream);
        //InputStream inputStreamD=inputStream;

        iDestination=iSource=iDataSize=iCommand=iPos=iCheckSum=iRcvdCheckSum=0;

        lMSTimeout = System.currentTimeMillis() + iProtocolTimeoutProperty;

        try {
            iCurrentState= STATE_SOH;
            while(!boolAbort) {
                //System.out.println(" iDataSize " + iDataSize );
                if( inputStreamD.available() != 0) {
                    iCurrByte = inputStreamD.read();
                    switch( iCurrentState ) {
                        case STATE_SOH :
                            if( iCurrByte == SOH )
                            { iCurrentState = STATE_DESTINATION;
                              inputStreamD.mark( 8 );
                            }
                            break;

                        case STATE_DESTINATION:
                            iDestination = iCurrByte;
                            iCurrentState = STATE_DESTINATION_COMPL;
                            break;

                        case STATE_DESTINATION_COMPL:
                            if( iCurrByte == (( ~ iDestination) & 0xFF) ) {
                                iCurrentState = STATE_SOURCE;
                            } else
                            { inputStreamD.reset();
                              iCurrentState = STATE_SOH;
                            }
                            break;

                        case STATE_SOURCE:
                            iSource = iCurrByte;
                            iCurrentState = STATE_SOURCE_COMPL;
                            break;

                        case STATE_SOURCE_COMPL:
                            if( iCurrByte == (( ~ iSource) & 0xFF) ) {
                                iCurrentState = STATE_DATASIZE;
                            } else
                            { inputStreamD.reset();
                              iCurrentState = STATE_SOH;
                            }
                            break;

                        case STATE_DATASIZE:
                            iDataSize = iCurrByte;
                            iCurrentState = STATE_DATASIZE_COMPL;
                            break;

                        case STATE_DATASIZE_COMPL:
                            if( iCurrByte == ((~iDataSize) & 0xFF) )
                            { iCurrentState = STATE_DATA;
                              iCheckSum = 0;
                              iPos=0;
                              liReceivedData = new int[ iDataSize ];   // instantiate the correct space ..
                            }
                            else
                            { inputStreamD.reset();
                              iCurrentState = STATE_SOH;
                            }
                            break;

                        case STATE_DATA:
                            iCheckSum += iCurrByte;
                            liReceivedData[iPos++] = iCurrByte & 0xFF;
                            if( iDataSize == iPos )  // Received all databytes ..
                            {
                                iCurrentState = STATE_CHECKSUM_MSB;
                            }
                            break;

                        case STATE_CHECKSUM_MSB:
                            iRcvdCheckSum = iCurrByte << 8;
                            iCurrentState = STATE_CHECKSUM_LSB;
                            break;

                        case STATE_CHECKSUM_LSB:
                            iRcvdCheckSum += iCurrByte;
                            if( (iRcvdCheckSum == iCheckSum) && (iDestination == aiServerID ) ) {
                                // Checksum is OK and Frame is for us!
                                inputStreamD.close();
                                return( liReceivedData );
                            }
                            else
                            { inputStreamD.reset();
                              iCurrentState = STATE_SOH;
                            }
                            break;

                        default:
                            break;
                    }
                }
                else {
                    Thread.sleep( 100 ); // KV 03062003
                }

                if( System.currentTimeMillis() - lMSTimeout > 0)
                { inputStreamD.close();
                  return null;  // Timeout
                }

            } // while(boolAbort==false)
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        catch( IOException e)
        {
            throw new RtuPlusBusException(e.getMessage());
        }
        logger.warning( "Communication Error : Timeout!" );
        return null;
    }


    public int[] doScramblePassword( long alRtuTime ) {

        int i;
        long llScrambledPassword;
        long llSeed[], llPassword[], llRtuTime[];
        long llPassw;
        llSeed     = new long[5];
        llPassword = new long[4];
        llRtuTime  = new long[4];

        llPassw = lPassword;                              // Copy the Password to local variable .. value will be modified here!
        for( i=0; i<4; i++)                               // First split the Parameters PW and RtuTijd into bytes ..
        {  llPassword[3-i] =( llPassw % 256 ) & 0xFF ;   // These bytes will be xor-ed with each other to create the new password
           llPassw = ( llPassw / 256);                   // The last time (GetTime() ) serves as the seed that changes every time
           llRtuTime[3-i] = ( alRtuTime % 256) & 0xFF ;  // Each time a GetTime() command is sent to the RTU, it keeps the last time aside as
           alRtuTime = (alRtuTime / 256);                // a seed for the password calculation...  The real password is never sent to the RTU!
        }

        llSeed[0] = llRtuTime[3];                         // Take the LSB of the time (changes most!) as the Seed to start with
        llScrambledPassword=0;                            // llScrambledPassword will contain the scrambled password to be sent to the RTU+
        for( i=0; i<4; i++ )
        { llScrambledPassword = (llScrambledPassword * 256) & 0xFFFFFFFF;            // Shift the byte left << 8
          llSeed[i+1] =  (llRtuTime[i] ^ llPassword[i]  ^ llSeed[i]) & 0xFF;         // XOR the RtuTime with the Password and the Seed, keep just the byte portion
          llScrambledPassword = (llScrambledPassword + llSeed[i+1]) & 0xFFFFFFFF;    // the new Seed is added to the PwToSend .. Repeat 4 times ..
        }

        int[] liBuffer;
        liBuffer = new int[4];                                        // aray of Integers to store the 4 bytes of the Scrambled Password
        liBuffer[3] = (int )( (llScrambledPassword >> 24) & 0xFF );   // Split the Scrambled Password into 4 bytes (stored as 4 integers)
        liBuffer[2] = (int )( (llScrambledPassword >> 16) & 0xFF );   // Use an array of integers to avoid the sign problem with bytes...
        liBuffer[1] = (int )( (llScrambledPassword >>  8) & 0xFF );
        liBuffer[0] = (int )( (llScrambledPassword      ) & 0xFF );
        return( liBuffer );                                           // Return the Scrambled Password as an array of 4 integers (LSB .. up to MSB)

    }

    /** Increment the iServerId variable to the next counter position.
     * @return the incremented iServerId
     */
    private int getNextServerId( ){
        return (iServerId >= maxServerId) ? (iServerId = minServerId + 1) : (iServerId = iServerId + 1);
    }

    /** Get the iServerId, without adjusting the counter position.
     * @return iServerId
     */
    private int getServerId( ){
        return iServerId;// iPrevServerId;
    }

    private boolean isProfileReadCommand( int c ) {
        return c == rtuplusbus.CMD_READ_NEXT_RECORD || c == rtuplusbus.CMD_READ_SAME_RECORD;
    }

    /** Before an interval is added, it has to be checked if it is,
     * actually the next interval.*/
    private boolean isNextEntry( int [] liReceivedData )
    {

        long lTimeOfRecord = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        lTimeOfRecord = (liReceivedData[4] & 0xFF) << 24;
        lTimeOfRecord +=(liReceivedData[3] & 0xFF) << 16;
        lTimeOfRecord +=(liReceivedData[2] & 0xFF) << 8;
        lTimeOfRecord +=(liReceivedData[1] & 0xFF);
        calendar.set( 1980, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.setTimeInMillis( calendar.getTimeInMillis() + ( lTimeOfRecord * 1000 ) );

        Date newDate = calendar.getTime();
        //System.out.println( "previous " + prevDate + " new " + newDate + " " + newDate.equals(  prevDate ) );
        if( prevDate != null && prevDate.equals( newDate ) )
            return false;
        else {
            prevDate = newDate;
            return true;
        }

    }

    /** Wait during 10' for the meter to stop sending */
    private void waitForSilence( ) throws IOException, RtuPlusBusException {
        if( DEBUG >= 1 ) System.out.println( "waitForSilence start " );

        try {
            Thread.currentThread().sleep( 1000 );
        } catch( InterruptedException ie ) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(ie);
        }

        long time = System.currentTimeMillis();
        int length = 0;
        while( ( length = inputStream.available() ) > 0 ) {
            int aChar = inputStream.read( new byte[length], 0, length );

            try {
                Thread.currentThread().sleep( 1000 );
            } catch( InterruptedException ie ) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(ie);
            }

            if( DEBUG >= 1 ) System.out.println( "aChar=" + aChar + " " + "waiting=" + ( System.currentTimeMillis() - time ) );
            if( System.currentTimeMillis() - time > 10000 )
                throw new RtuPlusBusException( RtuPlusBusException.TIME_OUT_ERROR );
        }
        if( DEBUG >= 1 ) System.out.println( "waitForSilence stop " + inputStream.available()  );
        return;
    }

    public static void main( String args [] ) throws Exception {

        //byte [] input = { (byte)0x01, (byte)0xa6, (byte)0x04, (byte)0xfb, (byte)0x45, (byte)0xba, (byte)0x2c };

        // byte [] input = { (byte)0x01, (byte)0x6b, (byte)0x94, (byte)0xfb, (byte)0x45, (byte)0xba, (byte)0x2c, (byte)0x04, (byte)0xf1 };

        byte [] input = {
            (byte)0x01, (byte)0x7c, (byte)0x83,
            (byte)0x04, (byte)0xfb, (byte)0xba,
            (byte)0x2c, (byte)0x48, (byte)0xbc,

            (byte)0x8f, (byte)0x2f, (byte)0x4f,
            (byte)0x00, (byte)0x01, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x1b,

            (byte)0x00, (byte)0x0f, (byte)0x00,
            (byte)0x59, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0xb2, (byte)0x00,

            (byte)0x92, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0xdb, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00,

            (byte)0x00, (byte)0x39, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x55,
            (byte)0x00, (byte)0x00, (byte)0x00,

            (byte)0x48, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x60, (byte)0x00,
            (byte)0x00, (byte)0x00
            };

        RtuPlusBusFrames rpbf = new RtuPlusBusFrames();

        ByteArrayInputStream bai = new ByteArrayInputStream( input );

        rpbf.inputStream = bai;

        //rpbf.doRTUReadFrame( 0x7c );


    }

    public void setForcedDelay(int forcedDelay) {
        this.forcedDelay = forcedDelay;
    }

    public int getIDelayAfterFailProperty() {
        return iDelayAfterFailProperty;
    }

}  // End of Class RtuPlusBusFrame
