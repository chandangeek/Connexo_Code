/**
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the DLMS COSEM meter protocol of the Actaris SL7000 meter with LN referencing.
 * <BR>
 * <B>@beginchanges</B><BR>
 * 	JM|22052009|Fixed billing point issue in ACE6000 and SL7000 DLMS protocols.
 * @endchanges
 */

package com.energyict.protocolimpl.dlms.actarisace6000;

import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class ACE6000 implements DLMSCOSEMGlobals, MeterProtocol, HHUEnabler, ProtocolLink, CacheMechanism, RegisterProtocol {
    private static final byte DEBUG=0;  // KV 16012004 changed all DEBUG values

    private static final byte[] profileLN={0,0,99,1,0,(byte)255};
    private static final int iNROfIntervals = 50000;

    private int iInterval=0;
    private ScalerUnit[] demandScalerUnits=null;
    String version=null;
    String serialnr=null;
    String nodeId;

    private String strID=null;
    private String strPassword=null;
    private String serialNumber=null;

    private int iHDLCTimeoutProperty;
    private int iProtocolRetriesProperty;
//    private int iDelayAfterFailProperty;
    private int iSecurityLevelProperty;
    private int iRequestTimeZone;
    private int iRoundtripCorrection;
    private int iClientMacAddress;
    private int iServerUpperMacAddress;
    private int iServerLowerMacAddress;
    private String firmwareVersion;
    private int alarmStatusFlagChannel;

    //private boolean boolAbort=false;

    CapturedObjects capturedObjects=null;

    DLMSConnection dlmsConnection=null;
    CosemObjectFactory cosemObjectFactory=null;
    StoredValuesImpl storedValuesImpl=null;

    ObisCodeMapper ocm=null;

    // Lazy initializing
    int numberOfChannels=-1;
    int configProgramChanges=-1;

    private int statusAlarmChannelIndex = -1;


    // interval alarm status flags
    private static final int EXTERNAL_CLOCK_INCOHERENCE = 1;
    private static final int NON_VOLATILE_MEMORY_NON_FATAL_ERROR = 2;
    private static final int COVER_OPENING = 4;
    private static final int CLOCK_LOSS = 8;
    private static final int WATCH_DOG_RESET = 16;
    private static final int CURRENT_REVERSAL_PHASE_1 = 32;
    private static final int CURRENT_REVERSAL_PHASE_2 = 64;
    private static final int CURRENT_REVERSAL_PHASE_3 = 128;
    private static final int TEMPERATURE_ALARM = 256;
    private static final int VOLTAGE_CUT_PHASE_1 = 512;
    private static final int VOLTAGE_CUT_PHASE_2 = 1024;
    private static final int VOLTAGE_CUT_PHASE_3 = 2048;
    private static final int BATTERY = 4096;
    private static final int EXCESS_DEMAND = 8192;
    private static final int PARAMETER_PROGRAMMING = 16384;

    // DLMS PDU offsets
    private static final byte DL_COSEMPDU_DATA_OFFSET=0x07;

    // Added for MeterProtocol interface implementation
    private Logger logger=null;
    private TimeZone timeZone=null;
//    private Properties properties=null;

    // filled in when getTime is invoked!
//    private int dstFlag; // -1=unknown, 0=not set, 1=set

    private DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("SLB");
    private DLMSCache dlmsCache=new DLMSCache();
    private int extendedLogging;
    int addressingMode;
    int connectionMode;

    /** Creates a new instance of ACE6000, empty constructor*/
    public ACE6000()
    {
    }

    public DLMSConnection getDLMSConnection() {
       return dlmsConnection;
    }

    /** initializes the receiver
     * @param inputStream <br>
     * @param outputStream <br>
     * @param timeZone <br>
     * @param logger <br>
     */
    public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;

        // lazy initializing
        numberOfChannels = -1;
        configProgramChanges = -1;
        iInterval = 0;
        demandScalerUnits = null;
        version = null;
        serialnr = null;

        try {
            cosemObjectFactory = new CosemObjectFactory(this);
            storedValuesImpl = new StoredValuesImpl(cosemObjectFactory);
            if (connectionMode == 0)
                dlmsConnection=new HDLCConnection(inputStream,outputStream,iHDLCTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress,iServerUpperMacAddress,addressingMode);
            else
                dlmsConnection=new TCPIPConnection(inputStream,outputStream,iHDLCTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
        }
        catch(DLMSConnectionException e) {
           //logger.severe ("dlms: Device clock is outside tolerance window. Setting clock");
           throw new IOException(e.getMessage());
        }
        //boolAbort = false;
    }


    byte[] aarqlowlevel17={
    (byte)0xE6,(byte)0xE6,(byte)0x00,
    (byte)0x60, // AARQ
    (byte)0x37, // bytes to follow
    (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
    (byte)0xAA,(byte)0x02,(byte)0x07,(byte)0x80, // ACSE requirements
    (byte)0xAB,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x02,(byte)0x01};
    //(byte)0xAC,(byte)0x0A,(byte)0x04}; //,(byte)0x08,(byte)0x41,(byte)0x42,(byte)0x43,(byte)0x44,(byte)0x45,(byte)0x46,(byte)0x47,(byte)0x48,

    byte[] aarqlowlevel17_2={
    (byte)0xBE,(byte)0x0F,(byte)0x04,(byte)0x0D,
    (byte)0x01, // initiate request
    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
    (byte)0x06,  // dlms version nr
    (byte)0x5F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x1D, // proposed conformance
    (byte)0x21,(byte)0x34};

    byte[] aarqlowlevelANY={
    (byte)0xE6,(byte)0xE6,(byte)0x00,
    (byte)0x60, // AARQ
    (byte)0x35, // bytes to follow
    (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,
    (byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
    (byte)0x8A,(byte)0x02,(byte)0x07,(byte)0x80, // ACSE requirements
    (byte)0x8B,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x02,(byte)0x01};
    //(byte)0xAC}; //,(byte)0x0A,(byte)0x80}; //,(byte)0x08,(byte)0x41,(byte)0x42,(byte)0x43,(byte)0x44,(byte)0x45,(byte)0x46,(byte)0x47,(byte)0x48,

    byte[] aarqlowlevelANY_2={(byte)0xBE,(byte)0x0F,(byte)0x04,(byte)0x0D,
    (byte)0x01, // initiate request
    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
    (byte)0x06,  // dlms version nr
    (byte)0x5F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x1D, // proposed conformance
    (byte)0x21,(byte)0x34};

    byte[] aarqlowestlevel={
    (byte)0xE6,(byte)0xE6,(byte)0x00,
    (byte)0x60, // AARQ
    (byte)0x1C, // bytes to follow
    (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
    (byte)0xBE,(byte)0x0F,(byte)0x04,(byte)0x0D,
    (byte)0x01, // initiate request
    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
    (byte)0x06,  // dlms version nr
    (byte)0x5F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x1D, // proposed conformance
    (byte)0xFF,(byte)0xFF};

    private byte[] getLowLevelSecurity() {
       if ("1.7".compareTo(firmwareVersion) == 0) {
            return buildaarq(aarqlowlevel17,aarqlowlevel17_2);
       }
       else {
           return buildaarq(aarqlowlevelANY,aarqlowlevelANY_2);
       }
    }

    private byte[] buildaarq(byte[] aarq1,byte[] aarq2) {
       byte[] aarq=null;
       int i,t=0;
       // prepare aarq buffer
       aarq = new byte[3+aarq1.length+1+strPassword.length()+aarq2.length];
       // copy aarq1 to aarq buffer
       for (i=0;i<aarq1.length;i++)
           aarq[t++] = aarq1[i];

       // calling authentification
       aarq[t++] = (byte)0xAC; // calling authentification tag
       aarq[t++] = (byte)(strPassword.length()+2); // length to follow
       aarq[t++] = (byte)0x80; // tag representation
       // copy password to aarq buffer
       aarq[t++] = (byte)strPassword.length();
       for (i=0;i<strPassword.length();i++)
           aarq[t++] = (byte)strPassword.charAt(i);


       // copy in aarq2 to aarq buffer
       for (i=0;i<aarq2.length;i++)
           aarq[t++] = aarq2[i];

       aarq[4] = (byte)(((int)aarq.length&0xFF)-5); // Total length of frame - headerlength

       return aarq;
    }

 /**
 * Method to request the Application Association Establishment for a DLMS session.
 * @exception IOException
 */
    public void requestApplAssoc() throws IOException {
       byte[] aarq;
       aarq = getLowLevelSecurity();
       doRequestApplAssoc(aarq);
    } // public void requestApplAssoc() throws IOException

    private void requestApplAssoc(int iLevel) throws IOException {
       byte[] aarq;
       if (iLevel == 0) {
           aarq = aarqlowestlevel;
       }
       else if (iLevel == 1) {
           aarq = getLowLevelSecurity();
       }
       else {
           aarq = getLowLevelSecurity();
       }
       doRequestApplAssoc(aarq);

    } // public void requestApplAssoc(int iLevel) throws IOException

    private void doRequestApplAssoc(byte[] aarq) throws IOException {
       byte[] responseData;
       responseData = getDLMSConnection().sendRequest(aarq);
       CheckAARE(responseData);
       if (DEBUG >= 2) ProtocolUtils.printResponseData(responseData);

    } // public void doRequestApplAssoc(int iLevel) throws IOException

    private static final byte AARE_APPLICATION_CONTEXT_NAME = (byte)0xA1;
    private static final byte AARE_RESULT = (byte)0xA2;
    private static final byte AARE_RESULT_SOURCE_DIAGNOSTIC = (byte)0xA3;
    private static final byte AARE_USER_INFORMATION = (byte)0xBE;

    private static final byte AARE_TAG=0x61;

    private static final byte ACSE_SERVICE_USER = (byte)0xA1;
    private static final byte ACSE_SERVICE_PROVIDER = (byte)0xA2;

    private static final byte DLMS_PDU_INITIATE_RESPONSE = (byte)0x08;
    private static final byte DLMS_PDU_CONFIRMED_SERVICE_ERROR = (byte)0x0E;

    private void CheckAARE(byte[] responseData) throws IOException
    {
       int i;
       int iLength;
       String strResultSourceDiagnostics="";
       InitiateResponse initiateResponse=new InitiateResponse();

       i=0;
       while(true)
       {
          if (responseData[i] == AARE_TAG)
          {
             i+=2; // skip tag & length
             while(true)
             {
                if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME)
                {
                   i++; // skip tag
                   i += responseData[i]; // skip length + data
                } // if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME)

                else if (responseData[i] == AARE_RESULT)
                {
                   i++; // skip tag
                   if ((responseData[i] == 3) &&
                       (responseData[i+1] == 2) &&
                       (responseData[i+2] == 1) &&
                       (responseData[i+3] == 0))
                   {
                      // Result OK
                      return;
                   }
                   i += responseData[i]; // skip length + data
                } // else if (responseData[i] == AARE_RESULT)

                else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC)
                {
                     i++; // skip tag
                     if (responseData[i] == 5) // check length
                     {
                         if (responseData[i+1] == ACSE_SERVICE_USER)
                         {
                             if ((responseData[i+2] == 3) &&
                                 (responseData[i+3] == 2) &&
                                 (responseData[i+4] == 1))
                             {
                                 if (responseData[i+5] == 0x00)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER";
                                 else if (responseData[i+5] == 0x01)
                                     strResultSourceDiagnostics += ", ACSE_SERVICE_USER, no reason given";
                                 else if (responseData[i+5] == 0x02)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Application Context Name Not Supported";
                                 else if (responseData[i+5] == 0x0B)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Not Recognised";
                                 else if (responseData[i+5] == 0x0C)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Required";
                                 else if (responseData[i+5] == 0x0D)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Failure";
                                 else if (responseData[i+5] == 0x0E)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Required";
                                 else throw new IOException("Application Association Establishment failed, ACSE_SERVICE_USER, unknown result!");
                             }
                             else
                             {
                                 throw new IOException("Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_USER,  wrong tag");
                             }
                         } // if (responseData[i+1] == ACSE_SERVICE_USER)
                         else if (responseData[i+1] == ACSE_SERVICE_PROVIDER)
                         {
                             if ((responseData[i+2] == 3) &&
                                 (responseData[i+3] == 2) &&
                                 (responseData[i+4] == 1))
                             {
                                 if (responseData[i+5] == 0x00)
                                    strResultSourceDiagnostics +=", ACSE_SERVICE_PROVIDER!";
                                 else if (responseData[i+5] == 0x01)
                                    strResultSourceDiagnostics +=", ACSE_SERVICE_PROVIDER, No Reason Given!";
                                 else if (responseData[i+5] == 0x02)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Common ACSE Version!";
                                 else throw new IOException("Application Association Establishment Failed, ACSE_SERVICE_PROVIDER, unknown result");
                             }
                             else throw new IOException("Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_PROVIDER,  wrong tag");
                         } // else if (responseData[i+1] == ACSE_SERVICE_PROVIDER)
                         else throw new IOException("Application Association Establishment Failed, result_source_diagnostic,  wrong tag");
                     }
                     else
                     {
                         throw new IOException("Application Association Establishment Failed, result_source_diagnostic, wrong length");
                     }

                     i += responseData[i]; // skip length + data
                } // else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC)

                else if (responseData[i] == AARE_USER_INFORMATION)
                {
                   i++; // skip tag
                   if (responseData[i+2] > 0) { // length of octet string
                       if (DLMS_PDU_INITIATE_RESPONSE == responseData[i+3]) {
                           initiateResponse.bNegotiatedQualityOfService=responseData[i+4];
                           initiateResponse.bNegotiatedDLMSVersionNR=responseData[i+5];
                           initiateResponse.lNegotiatedConformance=(ProtocolUtils.getInt(responseData,i+8)&0x00FFFFFF); // conformance has only 3 bytes, 24 bit
                           initiateResponse.sServerMaxReceivePduSize=ProtocolUtils.getShort(responseData,i+12);
                           initiateResponse.sVAAName=ProtocolUtils.getShort(responseData,i+14);
                           /*
                           System.out.println(initiateResponse.bNegotiatedDLMSVersionNR + " "+
                                              initiateResponse.bNegotiatedQualityOfService + " "+
                                              initiateResponse.lNegotiatedConformance + " "+
                                              initiateResponse.sServerMaxReceivePduSize + " " +
                                              initiateResponse.sVAAName);
                           */

                       }
                       else if (DLMS_PDU_CONFIRMED_SERVICE_ERROR == responseData[i+3])
                       {
                           if (0x01 == responseData[i+4])
                               strResultSourceDiagnostics += ", InitiateError";
                           else if (0x02 == responseData[i+4])
                               strResultSourceDiagnostics += ", getStatus";
                           else if (0x03 == responseData[i+4])
                               strResultSourceDiagnostics += ", getNameList";
                           else if (0x13 == responseData[i+4])
                               strResultSourceDiagnostics += ", terminateUpload";
                           else throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown ConfirmedServiceError choice");

                           if (0x06 != responseData[i+5])
                               strResultSourceDiagnostics += ", No ServiceError tag";

                           if (0x00 == responseData[i+6])
                               strResultSourceDiagnostics += "";
                           else if (0x01 == responseData[i+6])
                               strResultSourceDiagnostics += ", DLMS version too low";
                           else if (0x02 == responseData[i+6])
                               strResultSourceDiagnostics += ", Incompatible conformance";
                           else if (0x03 == responseData[i+6])
                               strResultSourceDiagnostics = ", pdu size too short";
                           else if (0x04 == responseData[i+6])
                               strResultSourceDiagnostics = ", refused by the VDE handler";
                           else throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons ");
                       }
                       else
                       {
                           throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons!");
                       }

                   } // if (responseData[i+2] > 0) --> length of the octet string

                   i += responseData[i]; // skip length + data
                } // else if (responseData[i] == AARE_USER_INFORMATION)
                else
                {
                   i++; // skip tag
                   // Very tricky, suppose we receive a length > 128 because of corrupted data,
                   // then if we keep byte, it is signed and we can enter a LOOP because length will
                   // be subtracted from i!!!
                   i += (((int)responseData[i])&0x000000FF); // skip length + data
                }

                if (i++ >= (responseData.length-1))
                {
                    i=(responseData.length-1);
                    break;
                }
             } // while(true)

          } // if (responseData[i] == AARE_TAG)

          if (i++ >= (responseData.length-1))
          {
              i=(responseData.length-1);
              break;
          }
       } // while(true)

       throw new IOException("Application Association Establishment Failed"+strResultSourceDiagnostics);

    } // void CheckAARE(byte[] responseData) throws IOException

    private CapturedObjects getCapturedObjects()  throws UnsupportedException, IOException {
        if (capturedObjects == null) {
           byte[] responseData;
           int i;
           DataContainer dataContainer = null;
           try {
               ProfileGeneric profileGeneric = getCosemObjectFactory().getLoadProfile().getProfileGeneric();
               meterConfig.setCapturedObjectList(profileGeneric.getCaptureObjectsAsUniversalObjects());
               dataContainer = profileGeneric.getCaptureObjectsAsDataContainer();

               capturedObjects = new CapturedObjects(dataContainer.getRoot().element.length);
               for (i=0;i<dataContainer.getRoot().element.length;i++) {
                  capturedObjects.add(i,
                                      dataContainer.getRoot().getStructure(i).getInteger(0),
                                      dataContainer.getRoot().getStructure(i).getOctetString(1).getArray(),
                                      dataContainer.getRoot().getStructure(i).getInteger(2));
               }
           }
           catch (java.lang.ClassCastException e) {
               System.out.println("Error retrieving object: "+e.getMessage());
           }
           catch(java.lang.ArrayIndexOutOfBoundsException e) {
               System.out.println("Index error: "+e.getMessage());
           }

        } // if (capturedObjects == null)

        return capturedObjects;

    } // private CapturedObjects getCapturedObjects()  throws UnsupportedException, IOException


    protected boolean containsStatusAlarmChannel(int numberOfChannels) throws IOException {
        CapturedObjects capturedObjects = getCapturedObjects();
        for (int i = 0; i < numberOfChannels; i++) {
            if (isAlarmStatusChannel(i)) {
                statusAlarmChannelIndex = i;
                return true;
            }
        }
        return false;
    }

    protected boolean isAlarmStatusChannel(int index) throws IOException {
        return
            ((getCapturedObjects().isChannelData(index)) &&
             (getCapturedObjects().getProfileDataChannel(index).toString().startsWith("1.1.96")));
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (numberOfChannels == -1) {
            numberOfChannels = getCapturedObjects().getNROfChannels();
            boolean containsStatusAlarmChannel =
                containsStatusAlarmChannel(numberOfChannels);
            if ((alarmStatusFlagChannel == 0) && (containsStatusAlarmChannel)) {
                numberOfChannels = numberOfChannels - 1;
            }
        }
        return numberOfChannels;
    } // public int getNumberOfChannels() throws IOException


/**
 * Method that requests the recorder interval in min.
 * Hardcoded for SL7000 meter to 15 min.
 * @return Remote meter 'recorder interval' in min.
 * @exception IOException
 */
    public int getProfileInterval() throws IOException,UnsupportedException{
        if (iInterval == 0) {
           byte[] LN = {0,0,(byte)136,0,1,(byte)255};
           DataContainer dataContainer = doRequestAttribute((short)1,LN, (byte)2);
           iInterval = dataContainer.getRoot().getInteger(0) * 60;
        }
        return iInterval;
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        int iNROfIntervals = getNROfIntervals();
        Calendar fromCalendar = ProtocolUtils.getCalendar(timeZone);
        fromCalendar.add(Calendar.MINUTE,(-1)*iNROfIntervals*(getProfileInterval()/60));
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(timeZone),includeEvents);
    }

    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(timeZone),includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }

    private ProfileData doGetProfileData(Calendar fromCalendar,Calendar toCalendar,boolean includeEvents) throws IOException {
        byte bNROfChannels = (byte)getNumberOfChannels();
        return doGetDemandValues(fromCalendar,
                                 bNROfChannels,
                                 includeEvents);
    }

    private ProfileData doGetDemandValues(Calendar fromCalendar, byte bNROfChannels,  boolean includeEvents) throws IOException {
        int channelCount = getNumberOfChannels();
        if ((alarmStatusFlagChannel == 0) &&
                (containsStatusAlarmChannel(numberOfChannels)))
            channelCount = channelCount + 1;

        ProfileData profileData = new ProfileData();
        DataContainer dataContainer = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getBuffer(fromCalendar);
        ScalerUnit[] scalerunit = new ScalerUnit[bNROfChannels];

        int channelIndex = 0;
        for (int i=0;i<channelCount;i++) {
           if ((i != this.statusAlarmChannelIndex) ||
                    (alarmStatusFlagChannel == 1)) {
               scalerunit[channelIndex] = getMeterDemandRegisterScalerUnit(i);
               profileData.addChannel(new ChannelInfo(i,
                                                  "dlmsACE6000_channel_"+i,
                                                  scalerunit[i].getUnit()));
               channelIndex++;
           }
        }
        buildProfileData(bNROfChannels,dataContainer,profileData,scalerunit);

        if (includeEvents) {
            profileData.getMeterEvents().addAll(getLogbookData());
            // Apply the events to the channel statusvalues
            profileData.applyEvents(getProfileInterval()/60);
        }


        return profileData;
    }



    private List getLogbookData() throws IOException {
        Logbook logbook = new Logbook(timeZone);
        return logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromByteArray(LOGBOOK_PROFILE_LN)).getBuffer());
    }


    private Calendar setCalendar(Calendar cal, DataStructure dataStructure,byte btype) throws IOException {

        Calendar calendar = (Calendar)cal.clone();

        if (dataStructure.getOctetString(0).getArray()[0] != -1)
              calendar.set(Calendar.YEAR,(((int)dataStructure.getOctetString(0).getArray()[0]&0xff)<<8)|
                                         (((int)dataStructure.getOctetString(0).getArray()[1]&0xff)));


        if (dataStructure.getOctetString(0).getArray()[2] != -1)
              calendar.set(Calendar.MONTH,((int)dataStructure.getOctetString(0).getArray()[2]&0xff)-1);


        if (dataStructure.getOctetString(0).getArray()[3] != -1)
              calendar.set(Calendar.DAY_OF_MONTH,((int)dataStructure.getOctetString(0).getArray()[3]&0xff));


        if (dataStructure.getOctetString(0).getArray()[5] != -1)
              calendar.set(Calendar.HOUR_OF_DAY,((int)dataStructure.getOctetString(0).getArray()[5]&0xff));
        else
              calendar.set(Calendar.HOUR_OF_DAY,0);


        if (btype == 0)
        {
            if (dataStructure.getOctetString(0).getArray()[6] != -1)
                  calendar.set(Calendar.MINUTE,(((int)dataStructure.getOctetString(0).getArray()[6]&0xff)/(getProfileInterval()/60))*(getProfileInterval()/60));
            else
                  calendar.set(Calendar.MINUTE,0);

            calendar.set(Calendar.SECOND,0);
        }
        else
        {
            if (dataStructure.getOctetString(0).getArray()[6] != -1)
                  calendar.set(Calendar.MINUTE,((int)dataStructure.getOctetString(0).getArray()[6]&0xff));
            else
                  calendar.set(Calendar.MINUTE,0);

            if (dataStructure.getOctetString(0).getArray()[7] != -1)
                  calendar.set(Calendar.SECOND,((int)dataStructure.getOctetString(0).getArray()[7]&0xff));
            else
                  calendar.set(Calendar.SECOND,0);
        }

        // if DSA, add 1 hour
        if (dataStructure.getOctetString(0).getArray()[11] != -1)
           if ((dataStructure.getOctetString(0).getArray()[11] & (byte)0x80) == 0x80)
               calendar.add(Calendar.HOUR_OF_DAY,-1);

        return calendar;

    } // private void setCalendar(Calendar calendar, DataStructure dataStructure,byte bBitmask)

    // status bitstring has 6 used bits
    private static final int EV_WATCHDOG_RESET=0x04;
    private static final int EV_DST=0x08;
    //private static final int EV_EXTERNAL_CLOCK_SYNC=0x10;
    //private static final int EV_CLOCK_SETTINGS=0x20;
    private static final int EV_ALL_CLOCK_SETTINGS=0x30;
    private static final int EV_POWER_FAILURE=0x40;
    private static final int EV_START_OF_MEASUREMENT=0x80;

    private Calendar parseProfileStartDate(DataStructure dataStructure,Calendar calendar) throws IOException {
        if (isNewDate(dataStructure.getStructure(0).getOctetString(0).getArray()))
            calendar = setCalendar(calendar,dataStructure.getStructure(0),(byte)0x00);
        return calendar;
    }

    private Calendar parseProfileStartTime(DataStructure dataStructure,Calendar calendar) throws IOException {
        if (isNewTime(dataStructure.getStructure(0).getOctetString(0).getArray()))
            calendar = setCalendar(calendar,dataStructure.getStructure(0),(byte)0x00);
        return calendar;
    }

    private boolean isNewDate(byte[] array) {
         if ((array[0] != -1) &&
             (array[1] != -1) &&
             (array[2] != -1) &&
             (array[3] != -1))
            return true;
         else
            return false;
    }

    private boolean isNewTime(byte[] array) {
         if ((array[5] != -1) &&
             (array[6] != -1) &&
             (array[7] != -1))
            return true;
         else
            return false;
    }

    private boolean parseStart(DataStructure dataStructure, Calendar calendar, ProfileData profileData) throws IOException {
        calendar = setCalendar(calendar,dataStructure.getStructure(0),(byte)0x01);
        if (DEBUG >=1) System.out.print("event: "+calendar.getTime());
        if ((dataStructure.getStructure(0).getInteger(1) & EV_ALL_CLOCK_SETTINGS) != 0) { // time set before
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.SETCLOCK_AFTER,
                                               (int)dataStructure.getStructure(0).getInteger(1)));
        }
        if ((dataStructure.getStructure(0).getInteger(1) & EV_POWER_FAILURE) != 0) { // power down
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.POWERUP,
                                               (int)EV_POWER_FAILURE));
        }
        if ((dataStructure.getStructure(0).getInteger(1) & EV_WATCHDOG_RESET) != 0) { // watchdog
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.WATCHDOGRESET,
                                               (int)EV_WATCHDOG_RESET));
        }
        if ((dataStructure.getStructure(0).getInteger(1) & EV_DST) != 0) { // watchdog
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.SETCLOCK_AFTER,
                                               (int)EV_DST));
        }
        return true;
    }

    private boolean parseEnd(DataStructure dataStructure, Calendar calendar, ProfileData profileData) throws IOException {
        calendar = setCalendar(calendar,dataStructure.getStructure(1),(byte)0x01);
        if (DEBUG >=1) System.out.print("event: "+calendar.getTime());

        if ((dataStructure.getStructure(1).getInteger(1) & EV_ALL_CLOCK_SETTINGS) != 0) { // time set before
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.SETCLOCK_BEFORE,
                                               (int)dataStructure.getStructure(1).getInteger(1)));
        }

        if ((dataStructure.getStructure(1).getInteger(1) & EV_POWER_FAILURE) != 0) { // power down
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.POWERDOWN,
                                               (int)EV_POWER_FAILURE));
           return true; // KV 16012004
        }

        /* No WD event added cause time is set to 00h00'00" */
        if ((dataStructure.getStructure(1).getInteger(1) & EV_DST) != 0) { // power down
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.SETCLOCK_BEFORE,
                                               (int)EV_DST));
           return true;
        }

        return false;
        //return true; // KV 16012004
    }

    private boolean parseTime1(DataStructure dataStructure, Calendar calendar, ProfileData profileData) throws IOException {
        calendar = setCalendar(calendar,dataStructure.getStructure(2),(byte)0x01);
        if (DEBUG >=1) System.out.print("event: "+calendar.getTime());

        if ((dataStructure.getStructure(2).getInteger(1) & EV_ALL_CLOCK_SETTINGS) != 0) { // time set before
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.SETCLOCK_BEFORE,
                                               (int)dataStructure.getStructure(2).getInteger(1)));
        }

        if ((dataStructure.getStructure(2).getInteger(1) & EV_POWER_FAILURE) != 0) {// power down
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.POWERDOWN,
                                               (int)EV_POWER_FAILURE));
        }
        return true;
    }

    private boolean parseTime2(DataStructure dataStructure, Calendar calendar, ProfileData profileData) throws IOException {
        calendar = setCalendar(calendar,dataStructure.getStructure(3),(byte)0x01);
        if (DEBUG >=1) System.out.print("event: "+calendar.getTime());

        if ((dataStructure.getStructure(3).getInteger(1) & EV_ALL_CLOCK_SETTINGS) != 0) { // time set before
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.SETCLOCK_AFTER,
                                               (int)dataStructure.getStructure(3).getInteger(1)));
        }

        if ((dataStructure.getStructure(3).getInteger(1) & EV_POWER_FAILURE) != 0) {// power down
           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                               (int)MeterEvent.POWERUP,
                                               (int)EV_POWER_FAILURE));
        }
        return true;
    }

    private void buildProfileData(byte bNROfChannels, DataContainer dataContainer,ProfileData profileData,ScalerUnit[] scalerunit)  throws IOException
    {
        byte bDOW;
        Calendar calendar=null,calendarEV=null;
        int i,t;
        boolean currentAdd=true,previousAdd=true;
        IntervalData previousIntervalData=null,currentIntervalData;

        int value = dataContainer.getRoot().element.length;

        if (dataContainer.getRoot().element.length == 0)
           throw new IOException("No entries in object list.");

        if (iRequestTimeZone != 0)
            calendar = ProtocolUtils.getCalendar(false,requestTimeZone());
        else
            calendar = ProtocolUtils.initCalendar(false,timeZone);

        //if (DEBUG >=1) dataContainer.printDataContainer();

        for (i=0;i<dataContainer.getRoot().element.length;i++) { // for all retrieved intervals


            if (dataContainer.getRoot().getStructure(i).isStructure(0)) {
                calendar = parseProfileStartDate(dataContainer.getRoot().getStructure(i),calendar); // new date?
                calendar = parseProfileStartTime(dataContainer.getRoot().getStructure(i),calendar); // new time?
                if (DEBUG >=1) System.out.println("new calendar reference: "+calendar.getTime());
            }


            // Start of interval
            if (dataContainer.getRoot().getStructure(i).isStructure(0)) {
                currentAdd = parseStart(dataContainer.getRoot().getStructure(i),calendar,profileData);
            }
            // End of interval
            if (dataContainer.getRoot().getStructure(i).isStructure(1)) {
                currentAdd = parseEnd(dataContainer.getRoot().getStructure(i),calendar,profileData);
            }
            // time1
            if (dataContainer.getRoot().getStructure(i).isStructure(2)) {
                currentAdd = parseTime1(dataContainer.getRoot().getStructure(i),calendar,profileData);
            }
            // Time2
            if (dataContainer.getRoot().getStructure(i).isStructure(3)) {
                currentAdd = parseTime2(dataContainer.getRoot().getStructure(i),calendar,profileData);
            }

            // KV 16012004
            //calendar.add(calendar.MINUTE,(getProfileInterval()/60));
            //profileData.addInterval(getIntervalData(dataContainer.getRoot().getStructure(i), calendar));



            // Adjust calendar for interval with profile interval period
            if (currentAdd) {
                calendar.add(calendar.MINUTE,(getProfileInterval()/60));
            }

            currentIntervalData = getIntervalData(dataContainer.getRoot().getStructure(i), calendar);


            // KV 16012004
            if (DEBUG >=1) {
                dataContainer.getRoot().getStructure(i).print();
                System.out.println();
            }

            if (currentAdd & !previousAdd) {
               if (DEBUG>=1) System.out.println ("add intervals together...");
               currentIntervalData = addIntervalData(currentIntervalData,previousIntervalData);
            }


            // Add interval data...
            if (currentAdd) {
                profileData.addInterval(currentIntervalData);
            }

            previousIntervalData=currentIntervalData;
            previousAdd=currentAdd;

        } // for (i=0;i<dataContainer.getRoot().element.length;i++) // for all retrieved intervals

    } // private void buildProfileData(byte bNROfChannels, DataContainer dataContainer)  throws IOException


    private IntervalData addIntervalData(IntervalData currentIntervalData,IntervalData previousIntervalData) {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int current,i;
        for (i=0;i<currentCount;i++) {
            current = ((Number)currentIntervalData.get(i)).intValue()+((Number)previousIntervalData.get(i)).intValue();
            intervalData.addValue(new Integer(current));
        }
        return intervalData;
    }

    private IntervalData getIntervalData(DataStructure dataStructure,Calendar calendar) throws UnsupportedException, IOException {
        // Add interval data...
        int channelIndex = 0;
        int protocolStatus = 0;
        IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()));
        for (int t=0;t<getCapturedObjects().getNROfObjects();t++) {
            if (getCapturedObjects().isChannelData(t)) {
                Integer value = new Integer(dataStructure.getInteger(t));
                if (channelIndex == statusAlarmChannelIndex)
                    protocolStatus = value.intValue();
                if ((channelIndex != this.statusAlarmChannelIndex) ||
                    (alarmStatusFlagChannel == 1))
                    intervalData.addValue(value);
                channelIndex++;
            }
        }
        mapIntervalStatus2EIStatus(intervalData, protocolStatus);
        return intervalData;
    }

    protected void mapIntervalStatus2EIStatus(IntervalData intervalData, int intervalStatus) {
        if ((intervalStatus & WATCH_DOG_RESET) == WATCH_DOG_RESET)
            intervalData.addEiStatus(IntervalStateBits.WATCHDOGRESET);
        if (((intervalStatus & CURRENT_REVERSAL_PHASE_1) == CURRENT_REVERSAL_PHASE_1) ||
            ((intervalStatus & CURRENT_REVERSAL_PHASE_2) == CURRENT_REVERSAL_PHASE_2) ||
            ((intervalStatus & CURRENT_REVERSAL_PHASE_3) == CURRENT_REVERSAL_PHASE_3))
            intervalData.addEiStatus(IntervalStateBits.REVERSERUN);
        if (((intervalStatus & VOLTAGE_CUT_PHASE_1) == VOLTAGE_CUT_PHASE_1) ||
            ((intervalStatus & VOLTAGE_CUT_PHASE_2) == VOLTAGE_CUT_PHASE_2) ||
            ((intervalStatus & VOLTAGE_CUT_PHASE_3) == VOLTAGE_CUT_PHASE_3))
            intervalData.addEiStatus(IntervalStateBits.PHASEFAILURE);
        if ((intervalStatus & BATTERY) == BATTERY)
            intervalData.addEiStatus(IntervalStateBits.BATTERY_LOW);
        if ( ((intervalStatus & EXTERNAL_CLOCK_INCOHERENCE) == EXTERNAL_CLOCK_INCOHERENCE) ||
             ((intervalStatus & NON_VOLATILE_MEMORY_NON_FATAL_ERROR) == NON_VOLATILE_MEMORY_NON_FATAL_ERROR) ||
             ((intervalStatus & COVER_OPENING) == COVER_OPENING) ||
             ((intervalStatus & CLOCK_LOSS) == CLOCK_LOSS) ||
             ((intervalStatus & TEMPERATURE_ALARM) == TEMPERATURE_ALARM) ||
             ((intervalStatus & EXCESS_DEMAND) == EXCESS_DEMAND) ||
             ((intervalStatus & PARAMETER_PROGRAMMING) == PARAMETER_PROGRAMMING))
            intervalData.addEiStatus(IntervalStateBits.OTHER);
    }

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

     private ScalerUnit getMeterDemandRegisterScalerUnit(int iChannelNR) throws IOException {
        if (iChannelNR < getNumberOfChannels()) {
            if (demandScalerUnits == null) {
                demandScalerUnits = new ScalerUnit[getNumberOfChannels()];
                byte[] LN = {0,0,99,(byte)128,1,(byte)255};
                DataContainer dataContainer = doRequestAttribute((short)7, LN,  (byte) 2);

                for(int i=0;i<getNumberOfChannels();i++) {
                   int scale = dataContainer.getRoot().getStructure(0).getStructure(i*2+1).getInteger(0);
                   int unit = dataContainer.getRoot().getStructure(0).getStructure(i*2+1).getInteger(1);
                   demandScalerUnits[i] = new ScalerUnit(scale,unit);
                }
            }

            return demandScalerUnits[iChannelNR];
        }
        else throw new IOException("getMeterDemandRegisterScalerUnit, invalid channelid ("+iChannelNR+")");
     }

    private int getNROfIntervals() throws IOException
    {
        // TODO fix amound for the moment
        return iNROfIntervals;
    } // private int getNROfIntervals() throws IOException
/**
 * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
 * @exception IOException
 */
    public void setTime() throws IOException
    {
       Calendar calendar=null;
       if (iRequestTimeZone != 0)
           calendar = ProtocolUtils.getCalendar(false,requestTimeZone());
       else
           calendar = ProtocolUtils.initCalendar(false,timeZone);
       calendar.add(Calendar.MILLISECOND,iRoundtripCorrection);
       doSetTime(calendar);
    } // public void setTime() throws IOException

    private void doSetTime(Calendar calendar) throws IOException
    {
       byte[] byteTimeBuffer = new byte[14];

       byteTimeBuffer[0]=TYPEDESC_OCTET_STRING;
       byteTimeBuffer[1]=12; // length
       byteTimeBuffer[2]=(byte)(calendar.get(calendar.YEAR) >> 8);
       byteTimeBuffer[3]=(byte)calendar.get(calendar.YEAR);
       byteTimeBuffer[4]=(byte)(calendar.get(calendar.MONTH)+1);
       byteTimeBuffer[5]=(byte)calendar.get(calendar.DAY_OF_MONTH);
       byte bDOW = (byte)calendar.get(calendar.DAY_OF_WEEK);
       byteTimeBuffer[6]=bDOW--==1?(byte)7:bDOW;
       byteTimeBuffer[7]=(byte)calendar.get(calendar.HOUR_OF_DAY);
       byteTimeBuffer[8]=(byte)calendar.get(calendar.MINUTE);
       byteTimeBuffer[9]=(byte)calendar.get(calendar.SECOND);
       byteTimeBuffer[10]=(byte)0xFF;
       byteTimeBuffer[11]=(byte)0xFF; //0x80;
       byteTimeBuffer[12]=(byte)0xFF; //0x00;
       if (timeZone.inDaylightTime(calendar.getTime()))
           byteTimeBuffer[13]=(byte)0x80; //0x00;
       else
           byteTimeBuffer[13]=(byte)0x00; //0x00;

       getCosemObjectFactory().writeObject(ObisCode.fromString("0.0.1.0.0.255"),8,2, byteTimeBuffer);

    } // private void doSetTime(Calendar calendar)

    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock();
        Date date = clock.getDateTime();
        //dstFlag = clock.getDstFlag();
        return date;
    }

    private boolean verifyMeterID() throws IOException {
        if ((strID == null) || ("".compareTo(strID)==0) || (strID.compareTo(getSerialNumber()) == 0))
            return true;
        else
            return false;
    }

    // KV 19012004
    private boolean verifyMeterSerialNR() throws IOException {
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0) || (serialNumber.compareTo(getSerialNumber()) == 0))
            return true;
        else
            return false;
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (configProgramChanges == -1)
           configProgramChanges = (int)getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        return configProgramChanges;
    } // public int requestConfigurationProgramChanges() throws IOException


    /**
     * This method requests for the COSEM object SAP.
     * @exception IOException
     */
    public void requestSAP() throws IOException {
        String devID =  (String)getCosemObjectFactory().getSAPAssignment().getLogicalDeviceNames().get(0);
        if ((strID != null) && ("".compareTo(strID) != 0)) {
            if (strID.compareTo(devID) != 0) {
                throw new IOException("DLMSSN, requestSAP, Wrong DeviceID!, settings="+strID+", meter="+devID);
            }
        }
    } // public void requestSAP() throws IOException

    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
        }
        catch(DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }
        try {
            requestApplAssoc(iSecurityLevelProperty);

            try {

               // requestSAP();  // KV 08102004 R/W denied to read SAP!!!!!
               //System.out.println("cache="+dlmsCache.getObjectList()+", confchange="+dlmsCache.getConfProgChange()+", ischanged="+dlmsCache.isChanged());
                try { // conf program change and object list stuff
                    int iConf;

                    if (dlmsCache.getObjectList() != null) {
                        meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                        try {

                            iConf = requestConfigurationProgramChanges();
                        }
                        catch(IOException e) {
                            iConf=-1;
                            logger.severe("DLMSZMD: Configuration change count not accessible, request object list.");
                            requestObjectList();
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                        }

                        if (iConf != dlmsCache.getConfProgChange()) {
                        // KV 19112003 ************************** DEBUGGING CODE ********************************
                        //System.out.println("!!!!!!!!!! DEBUGGING CODE FORCED DLMS CACHE UPDATE !!!!!!!!!!");
                        //if (true) {
                        // ****************************************************************************
                            logger.severe("DLMSZMD: Configuration changed, request object list.");
                            requestObjectList();           // request object list again from rtu
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                        }
                    }
                    else { // Cache not exist
                        logger.info("DLMSZMD: Cache does not exist, request object list.");
                        requestObjectList();
                        try {
                            iConf = requestConfigurationProgramChanges();

                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                        }
                        catch(IOException e) {
                            iConf=-1;
                        }
                    }

                    if (!verifyMeterID())
                        throw new IOException("DLMSLN7000, connect, Wrong DeviceID!, settings="+strID+", meter="+getSerialNumber());

                    // KV 19012004
                    if (!verifyMeterSerialNR())
                        throw new IOException("DLMSLN7000, connect, Wrong SerialNR!, settings="+serialNumber+", meter="+getSerialNumber());


                    if (extendedLogging >= 1)
                       logger.info(getRegistersInfo(extendedLogging));

                }
                catch(IOException e) {
                    throw new IOException("connect() error, "+e.getMessage());
                }

            }
            catch(IOException e) {
                throw new IOException(e.getMessage());
            }
        }
        catch(IOException e) {
            throw new IOException(e.getMessage());
        }

        validateSerialNumber(); // KV 19012004

    } // public void connect() throws IOException


    private String getAllMaximumDemandRegisterInfos(boolean billingPoint) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        Iterator it;
        // All maximum demands (profile generic objects)
        List allMaximumDemandProfiles = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.98.133.6.255")).getCaptureObjects();
        it = allMaximumDemandProfiles.iterator();
        while(it.hasNext()) {
            CapturedObject capturedObjectMDProfile = (CapturedObject)it.next();
            // get the first of the list of captured objects
            List allMaximumDemands = getCosemObjectFactory().getProfileGeneric(capturedObjectMDProfile.getLogicalName().getObisCode()).getCaptureObjects();
            CapturedObject capturedObject = (CapturedObject)allMaximumDemands.get(0);
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+(billingPoint?" (billing point)\n":"\n"));
        }
        return strBuff.toString();
    }
    private String getAllDemandRegisterInfos(boolean billingPoint) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        Iterator it;
        // All demands
        it = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.98.133.5.255")).getCaptureObjects().iterator();
        while(it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject)it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+(billingPoint?" (billing point)\n":"\n"));
        }
        return strBuff.toString();
    }
    private String getAllCumulativeMaximumDemandRegisterInfos(boolean billingPoint) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        Iterator it;
        // All cumulative maximum demands
        it = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.98.133.90.255")).getCaptureObjects().iterator();
        while(it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject)it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+(billingPoint?" (billing point)\n":"\n"));
        }
        return strBuff.toString();
    }

    private String getAllEnergyRates(boolean billingPoint) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        Iterator it;
        // All energy rates
        it = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("255.255.98.133.1.255")).getCaptureObjects().iterator();
        while(it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject)it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+(billingPoint?" (billing point)\n":"\n"));
        }
        return strBuff.toString();
    }

    private String getAllTotalEnergies(boolean billingPoint) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        Iterator it;
        // All total energies
        it = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("255.255.98.133.2.255")).getCaptureObjects().iterator();
        while(it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject)it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+(billingPoint?" (billing point)\n":"\n"));
        }
        return strBuff.toString();
    }


    /*
     *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        Iterator it;

        // all total and rate values...
        strBuff.append("********************* All instantiated objects in the meter *********************\n");
        for (int i=0;i<getMeterConfig().getInstantiatedObjectList().length;i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            strBuff.append(uo.getObisCode().toString()+" "+uo.getObisCode().getDescription()+"\n");
        }

        strBuff.append(getAllTotalEnergies(false));
        strBuff.append(getAllEnergyRates(false));
        strBuff.append(getAllDemandRegisterInfos(false));
        strBuff.append(getAllMaximumDemandRegisterInfos(false));
        strBuff.append(getAllCumulativeMaximumDemandRegisterInfos(false));

        // all billing points values...
        strBuff.append("********************* Objects captured into billing points *********************\n");
        strBuff.append("The ACE6000 has 18 billingpoints for most electricity related registers registers.\n");
        strBuff.append("For more specific rfegisters like instantaneous values etc, refer to the manufacturers.\n");

        strBuff.append(getAllTotalEnergies(true));
        strBuff.append(getAllEnergyRates(true));
        strBuff.append(getAllDemandRegisterInfos(true));
        strBuff.append(getAllMaximumDemandRegisterInfos(true));
        strBuff.append(getAllCumulativeMaximumDemandRegisterInfos(true));

        strBuff.append("********************* Objects captured into load profile *********************\n");
        it = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects().iterator();
        while(it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject)it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (load profile)\n");
        }

        return strBuff.toString();
    }



    public void disconnect() throws IOException {
       try {
          if (dlmsConnection != null) getDLMSConnection().disconnectMAC();
       }
       catch(DLMSConnectionException e) {
          logger.severe("DLMSLN: disconnect(), "+e.getMessage());
       }
    } // public void disconnect() throws IOException

    class InitiateResponse
    {
       protected byte bNegotiatedQualityOfService;
       protected byte bNegotiatedDLMSVersionNR;
       protected long lNegotiatedConformance;
       protected short sServerMaxReceivePduSize;
       protected short sVAAName;

       InitiateResponse()
       {
           bNegotiatedQualityOfService=0;
           bNegotiatedDLMSVersionNR=0;
           lNegotiatedConformance=0;
           sServerMaxReceivePduSize=0;
           sVAAName=0;
       }
    }

    /**
     * This method requests for the COSEM object list in the remote meter. A list is byuild with LN and SN references.
     * This method must be executed before other request methods.
     * @exception IOException
     */
    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
    } // public void requestObjectList() throws IOException




    public String requestAttribute(short sIC,byte[] LN,byte bAttr) throws IOException {
        return doRequestAttribute(sIC,LN, bAttr).print2strDataContainer();
    } // public String requestAttribute(short sIC,byte[] LN,byte bAttr ) throws IOException


    private DataContainer doRequestAttribute(int classId,byte[] ln,int lnAttr) throws IOException {
       DataContainer dc = getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(ln),DLMSUtils.attrLN2SN(lnAttr),classId).getDataContainer();
       return dc;
    } // public DataContainer doRequestAttribute(short sIC,byte[] LN,byte bAttr ) throws IOException

    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) return;
        String sn = (String)getSerialNumber();
        if ((sn != null) && (sn.compareTo(serialNumber) == 0)) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+serialNumber);
    }

    public String getSerialNumber() throws IOException {
        if (serialnr==null) {
            UniversalObject uo = meterConfig.getSerialNumberObject();
            serialnr = getCosemObjectFactory().getGenericRead(uo).getString();
        }
        return serialnr;
    } // public String getSerialNumber() throws IOException

    public String getProtocolVersion() {
        return "$Date$";
    }
    public String getFirmwareVersion() throws IOException,UnsupportedException {
        if (version == null) {
           StringBuffer strbuff=new StringBuffer();
           try {
               UniversalObject uo = meterConfig.getVersionObject();
               DataContainer dataContainer = getCosemObjectFactory().getGenericRead(uo).getDataContainer();
               /* 020211011104 --> structure, 2 elements, 1 en 4 --> 1.4
                  Voor de root moet je geen structure opvragen! */
               version = dataContainer.getRoot().getOctetString(0).toString();
               /*strbuff.append(String.valueOf(dataContainer.getRoot().getInteger(0)));
               strbuff.append(".");
               strbuff.append(String.valueOf(dataContainer.getRoot().getInteger(1)));
               version=strbuff.toString();*/
           }
           catch(IOException e) {
               throw new IOException("DLMSLNSL7000, getFirmwareVersion, Error, "+e.getMessage());
           }
        }
        return version;
    }

    /** this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    public void setProperties(Properties properties) throws MissingPropertyException , InvalidPropertyException {
        validateProperties(properties);
        //this.properties = properties;
    }

    /** <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException
    {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException (key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            if ((strID != null) && (strID.length()>16)) throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            //if (strPassword.length()!=8) throw new InvalidPropertyException("Password must be exact 8 characters.");
            iHDLCTimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","5").trim());
            //iDelayAfterFailProperty=Integer.parseInt(properties.getProperty("DelayAfterfail","3000").trim());
            iSecurityLevelProperty=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
            iRequestTimeZone=Integer.parseInt(properties.getProperty("RequestTimeZone","0").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());

            iClientMacAddress=Integer.parseInt(properties.getProperty("ClientMacAddress","1").trim());
            iServerUpperMacAddress=Integer.parseInt(properties.getProperty("ServerUpperMacAddress","17").trim());
            iServerLowerMacAddress=Integer.parseInt(properties.getProperty("ServerLowerMacAddress","17").trim());
            firmwareVersion=properties.getProperty("FirmwareVersion","ANY");
            nodeId=properties.getProperty(MeterProtocol.NODEID,"");
            // KV 19012004 get the serialNumber
            serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0"));
            addressingMode=Integer.parseInt(properties.getProperty("AddressingMode","-1"));
            connectionMode = Integer.parseInt(properties.getProperty("Connection","0")); // 0=HDLC, 1= TCP/IP
            alarmStatusFlagChannel = Integer.parseInt(properties.getProperty("StatusFlagChannel","0"));
        }
        catch (NumberFormatException e) {
           throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "+e.getMessage());
        }


    }

    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @return the register value
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     * @throws NoSuchRegisterException <br>
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return doGetRegister(name);
    }

    private String doGetRegister(String name) throws IOException {
        boolean classSpecified=false;
        if (name.indexOf(':') >= 0)
            classSpecified=true;
        DLMSObis ln = new DLMSObis(name);
        if (ln.isLogicalName()) {
            if (classSpecified)
               return requestAttribute(ln.getDLMSClass(),ln.getLN(), (byte)ln.getOffset());
            else {
               UniversalObject uo = getMeterConfig().getObject(ln);
               return getCosemObjectFactory().getGenericRead(uo).getDataContainer().print2strDataContainer();
            }
        }
        else throw new NoSuchRegisterException("DLMSLNSL7000,getRegister, register "+name+" does not exist.");
    }

    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @param value <br>
     * @throws IOException <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException <br>
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException();
    }

    /** this implementation throws UnsupportedException. Subclasses may override
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    /** the implementation returns both the address and password key
     * @return a list of strings
     */
    public List getRequiredKeys() {
        List result = new ArrayList(0);

        return result;
    }

    /** this implementation returns an empty list
     * @return a list of strings
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("FirmwareVersion");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("AddressingMode");
        result.add("AlarmStatusFlagChannel");
        return result;
    }

    public int requestTimeZone() throws IOException {
       // All time reporting is UTC for the SL7000
       return (0);
    }

    public void setCache(Object cacheObject) {
        this.dlmsCache=(DLMSCache)cacheObject;
    }
    public Object getCache() {
        return dlmsCache;
    }
    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        if (rtuid != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
            RtuDLMS rtu = new RtuDLMS(rtuid);
            try {
               return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
            }
            catch(NotFoundException e) {
               return new DLMSCache(null,-1);
            }
        }
        else throw new com.energyict.cbo.BusinessException("invalid RtuId!");
    }
    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException,com.energyict.cbo.BusinessException {
        if (rtuid != 0) {
            DLMSCache dc = (DLMSCache)cacheObject;
            if (dc.isChanged()) {
                RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
                RtuDLMS rtu = new RtuDLMS(rtuid);
                rtuCache.saveObjectList(dc.getObjectList());
                rtu.setConfProgChange(dc.getConfProgChange());
            }
        }
        else throw new com.energyict.cbo.BusinessException("invalid RtuId!");
    }

    public void release() throws IOException {
    }

    // implementation oh HHUEnabler interface
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel,false);
    }
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
              (HHUSignOn)new IEC1107HHUConnection(commChannel,iHDLCTimeoutProperty,iProtocolRetriesProperty,300,0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn,nodeId);
    }
     public byte[] getHHUDataReadout() {
         return getDLMSConnection().getHhuSignOn().getDataReadout();
     }

    public Logger getLogger() {
        return logger;
    }

    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }

    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    public int getRoundTripCorrection() {
        return iRoundtripCorrection;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
    }

    /**
     * Getter for property cosemObjectFactory.
     * @return Value of property cosemObjectFactory.
     */
    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public String getFileName() {


        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR)+"_"+(calendar.get(Calendar.MONTH)+1)+"_"+calendar.get(Calendar.DAY_OF_MONTH)+"_"+strID+"_"+strPassword+"_"+serialNumber+"_"+iServerUpperMacAddress+"_DLMSSL7000.cache";
    }

    public StoredValues getStoredValues() {
        return (StoredValues)storedValuesImpl;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
			if (ocm == null)
			    ocm = new ObisCodeMapper(getCosemObjectFactory(), meterConfig);
			return ocm.getRegisterValue(obisCode);
		} catch (Exception e) {
			throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
		}
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

} // public class DLMSProtocolLN extends MeterProtocol

