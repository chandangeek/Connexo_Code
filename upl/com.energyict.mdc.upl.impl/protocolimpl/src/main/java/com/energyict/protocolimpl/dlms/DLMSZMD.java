/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the Siemens ZMD DLMS profile implementation
 * <BR>
 * <B>@beginchanges</B><BR>
KV|08042003|Initial version
KV|08102003|Set default of RequestTimeZone to 0
KV|10102003|generate OTHER MeterEvent when statusbit is not supported
KV|27102003|changed code for correct dst transition S->W
KV|20082004|Extended with obiscode mapping for register reading
KV|17032005|improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Improved registerreading, configuration data
KV|31032005|Handle DataContainerException
KV|15072005|applyEvents() done AFTER getting the logbook!
KV|10102006|extension to support cumulative values in load profile
KV|10102006|fix to support 64 bit values in load profile
 * @endchanges
 */

package com.energyict.protocolimpl.dlms;


import com.energyict.dlms.*;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.siemenszmd.*;

import java.io.IOException;
import java.util.*;

@Deprecated /** Deprecated as of jan 2012 - please use the new SmartMeter protocol (com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD) instead. **/
public class DLMSZMD extends DLMSSN implements RegisterProtocol, DemandResetProtocol, MessageProtocol, TimeOfUseMessaging {
    private static final byte DEBUG=0;

    private final MessageProtocol messageProtocol;

    int eventIdIndex;

    public DLMSZMD() {
        this.messageProtocol = new ZmdMessages(this);
    }

    protected String getDeviceID() {
        return "LGZ";
    }

    // Interval List
    private static final byte IL_CAPUTURETIME=0;
    private static final byte IL_EVENT=12;
    private static final byte IL_DEMANDVALUE=13;

    // Event codes as interpreted by MV90 for the Siemens ZMD meter
    private static final long EV_NORMAL_END_OF_INTERVAL=0x00800000;
    private static final long EV_START_OF_INTERVAL=     0x00080000;
    private static final long EV_FATAL_ERROR=           0x00000001;
    private static final long EV_CORRUPTED_MEASUREMENT= 0x00000004;
    private static final long EV_SUMMER_WINTER=         0x00000008;
    private static final long EV_TIME_DATE_ADJUSTED=    0x00000020;
    private static final long EV_POWER_UP=              0x00000040;
    private static final long EV_POWER_DOWN=            0x00000080;
    private static final long EV_EVENT_LOG_CLEARED=     0x00002000;
    private static final long EV_LOAD_PROFILE_CLEARED=  0x00004000;
    //private static final long EV_CAPTURED_EVENTS=       0x008860E5; // Add new events...

    //KV 27102003
    public Calendar initCalendarSW(boolean protocolDSTFlag,TimeZone timeZone) {
        Calendar calendar;
        if (protocolDSTFlag) {
			calendar = Calendar.getInstance(ProtocolUtils.getSummerTimeZone(timeZone));
		} else {
			calendar = Calendar.getInstance(ProtocolUtils.getWinterTimeZone(timeZone));
		}
        return calendar;
    }

    /** ProtocolVersion **/
    public String getProtocolVersion() {
        return "$Date$";
    }

    protected void getEventLog(ProfileData profileData,Calendar fromCalendar, Calendar toCalendar) throws IOException {
        DataContainer dc = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCalendar,toCalendar);

        if (DEBUG>=1) {
			dc.printDataContainer();
		}

        if (DEBUG>=1) {
           getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getCaptureObjectsAsDataContainer().printDataContainer();
        }

        int index=0;
        if (eventIdIndex == -1) {
            Iterator it = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getCaptureObjects().iterator();
            while(it.hasNext()) {
                CapturedObject capturedObject = (CapturedObject)it.next();
                if (capturedObject.getLogicalName().getObisCode().equals(ObisCode.fromString("0.0.96.240.12.255")) &&
                    (capturedObject.getAttributeIndex() == 2) &&
                    (capturedObject.getClassId() == 3)) {
					break;
				} else {
					index++;
				}
            }
        }

        for (int i=0;i<dc.getRoot().getNrOfElements();i++) {
           Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
           int id=0;
           if (eventIdIndex == -1) {
               id = dc.getRoot().getStructure(i).getInteger(index);
           }
           else {
               id = dc.getRoot().getStructure(i).convert2Long(eventIdIndex).intValue();
           }
           MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime);
           if (meterEvent != null) {
			profileData.addEvent(meterEvent);
		}
        }
    }

    @Override
    protected SecurityProvider getSecurityProvider() {
        return new ZMDSecurityProvider(getProperties());
    }

    /**
     * Configure the {@link com.energyict.dlms.aso.ConformanceBlock} which is used for the DLMS association.
     *
     * @return the conformanceBlock, if null is returned then depending on the reference,
     *         the default value({@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_LN_CONFORMANCE_BLOCK} or {@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_SN_CONFORMANCE_BLOCK}) will be used
     */
    @Override
    protected ConformanceBlock configureConformanceBlock() {
        return new ConformanceBlock(1573408L);
    }

    protected void buildProfileData(byte bNROfChannels,ProfileData profileData,ScalerUnit[] scalerunit,UniversalObject[] intervalList)  throws IOException {
        byte bDOW;
        Calendar stdCalendar=null;
        Calendar dstCalendar=null;
        Calendar calendar=null;
        int i,t;
        IntervalData savedIntervalData=null;

        if (isRequestTimeZone()) {
            stdCalendar = ProtocolUtils.getCalendar(false,requestTimeZone());
            dstCalendar = ProtocolUtils.getCalendar(true,requestTimeZone());
        }
        else { // KV 27102003
            stdCalendar = initCalendarSW(false,getTimeZone());
            dstCalendar = initCalendarSW(true,getTimeZone());
        }

        if (DEBUG >= 1) {
			System.out.println("intervalList.length = "+intervalList.length);
		}

        for (i=0;i<intervalList.length;i++) {

            // KV 27102003
            if (intervalList[i].getField(IL_CAPUTURETIME+11) != 0xff) {
                if ((intervalList[i].getField(IL_CAPUTURETIME+11)&0x80) == 0x80) {
					calendar = dstCalendar;
				} else {
					calendar = stdCalendar;
				}
            } else {
				calendar = stdCalendar;
			}

            // Build Timestamp
            calendar.set(Calendar.YEAR,(int)((intervalList[i].getField(IL_CAPUTURETIME)<<8) |
            intervalList[i].getField(IL_CAPUTURETIME+1)));
            calendar.set(Calendar.MONTH,(int)intervalList[i].getField(IL_CAPUTURETIME+2)-1);
            calendar.set(Calendar.DAY_OF_MONTH,(int)intervalList[i].getField(IL_CAPUTURETIME+3));
            calendar.set(Calendar.HOUR_OF_DAY,(int)intervalList[i].getField(IL_CAPUTURETIME+5));
            calendar.set(Calendar.MINUTE,(int)intervalList[i].getField(IL_CAPUTURETIME+6));
            calendar.set(Calendar.SECOND,(int)intervalList[i].getField(IL_CAPUTURETIME+7));

            int iField = (int)intervalList[i].getField(IL_EVENT); // & (int)EV_CAPTURED_EVENTS; // KV 10102003, include all bits...
            iField &= (EV_NORMAL_END_OF_INTERVAL ^ 0xffffffff); // exclude EV_NORMAL_END_OF_INTERVAL bit
            iField &= (EV_SUMMER_WINTER ^ 0xffffffff); // exclude EV_SUMMER_WINTER bit // KV 10102003
            for (int bit=0x1;bit!=0;bit<<=1) {
                if ((iField & bit) != 0) {
                    profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                    (int)mapLogCodes(bit),
                    (int)bit));
                }
            } // for (int bit=0x1;bit!=0;bit<<=1)

            // KV 12112002 following the Siemens integration handbook, only exclude profile entries where
            // status & EV_START_OF_INTERVAL is true
            // SVA Update: profile entries where status & EV_LOAD_PROFILE_CLEARED is true can also be left out.

            if (((intervalList[i].getField(IL_EVENT) & EV_START_OF_INTERVAL) == 0) && ((intervalList[i].getField(IL_EVENT) & EV_LOAD_PROFILE_CLEARED) == 0)) {

                // In case the EV_NORMAL_END_OF_INTERVAL bit is not set, calendar is possibly
                // not aligned to interval boundary caused by an event
                if ((intervalList[i].getField(IL_EVENT) & EV_NORMAL_END_OF_INTERVAL) == 0) {
                    // Following code does the aligning
                    int rest = (int)(calendar.getTime().getTime()/1000) % getProfileInterval();
                    if (DEBUG >= 1) {
						System.out.print(calendar.getTime()+" "+calendar.getTime().getTime()+", timestamp adjusted with "+(getProfileInterval() - rest)+" sec.");
					}
                    if (rest > 0) {
						calendar.add(Calendar.SECOND, getProfileInterval() - rest);
					}
                }
                else {
                    if (DEBUG >= 1) {
						System.out.print(calendar.getTime()+" "+calendar.getTime().getTime()+", statusbits = "+Integer.toHexString(iField));
					}
                }

                // Fill profileData
                IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()));

                for (t=0;t<bNROfChannels;t++) {
                    Long val = new Long(intervalList[i].getField(IL_DEMANDVALUE+t));
                    intervalData.addValue(val);
                    if (DEBUG >= 1) {
						System.out.print(", value = "+val.longValue());
					}
                }

                if ((intervalList[i].getField(IL_EVENT) & EV_CORRUPTED_MEASUREMENT) != 0) {
					intervalData.addStatus(IntervalData.CORRUPTED);
				}

                // In case the EV_NORMAL_END_OF_INTERVAL bit is not set, save the interval and add it to the
                // next or save as separate!
                if ((intervalList[i].getField(IL_EVENT) & EV_NORMAL_END_OF_INTERVAL) != 0) {
                    if (savedIntervalData != null) {
                        if (savedIntervalData.getEndTime().equals(intervalData.getEndTime())) {
                            profileData.addInterval(addIntervalData(savedIntervalData,intervalData));
                        }
                        else {
                            profileData.addInterval(savedIntervalData);
                            profileData.addInterval(intervalData);
                        }
                        savedIntervalData = null;
                    } else {
						profileData.addInterval(intervalData);
					}
                }
                else {
                    // KV 15122003 cumulate multiple powerfails during an interval
                    if (savedIntervalData == null) {
						savedIntervalData = intervalData;
					} else {
                        // if new event crosses intervalboundary, save the cumulated data to nearest interval
                        // and save new data for next interval...
                        if (getNrOfIntervals(savedIntervalData) < getNrOfIntervals(intervalData)) {
                           roundUp2nearestInterval(savedIntervalData);
                           profileData.addInterval(savedIntervalData);
                           savedIntervalData = intervalData;
                        }
                        else {
                           savedIntervalData = addIntervalData(savedIntervalData,intervalData);
                        }
                    }
                }

            } // if ((intervalList[i].getField(IL_EVENT) & EV_START_OF_INTERVAL) == 0)

            if (DEBUG >= 1) {
				System.out.println();
			}

        } // for (i=0;i<intervalList.length;i++) {

    } // ProfileData buildProfileData(...)

    // KV 15122003
    private void roundDown2nearestInterval(IntervalData intervalData) throws IOException {
        int rest = (int)(intervalData.getEndTime().getTime()/1000) % getProfileInterval();
        if (rest > 0) {
			intervalData.getEndTime().setTime(((intervalData.getEndTime().getTime()/1000) - rest) * 1000);
		}
    }

    // KV 15122003
    private void roundUp2nearestInterval(IntervalData intervalData) throws IOException {
        int rest = (int)(intervalData.getEndTime().getTime()/1000) % getProfileInterval();
        if (rest > 0) {
			intervalData.getEndTime().setTime(((intervalData.getEndTime().getTime()/1000) + (getProfileInterval() - rest)) * 1000);
		}
    }

    // KV 15122003
    private int getNrOfIntervals(IntervalData intervalData) throws IOException {
        return (int)(intervalData.getEndTime().getTime()/1000) / getProfileInterval();
    }

    // KV 15122003 changed
    private IntervalData addIntervalData(IntervalData cumulatedIntervalData,IntervalData currentIntervalData) throws IOException {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int i;
        long current;
        for (i=0;i<currentCount;i++) {
            if (getMeterConfig().getChannelObject(i).isCapturedObjectCumulative()) {
				current = ((Number)currentIntervalData.get(i)).longValue();
			} else {
				current = ((Number)currentIntervalData.get(i)).longValue()+((Number)cumulatedIntervalData.get(i)).longValue();
			}
            intervalData.addValue(new Long(current));
        }
        return intervalData;
    }

    private long mapLogCodes(long lLogCode) {
        switch((int)lLogCode) {
            case (int)EV_FATAL_ERROR: return(MeterEvent.FATAL_ERROR);
            case (int)EV_CORRUPTED_MEASUREMENT: return(MeterEvent.OTHER);
            case (int)EV_TIME_DATE_ADJUSTED: return(MeterEvent.SETCLOCK);
            case (int)EV_POWER_UP: return(MeterEvent.POWERUP);
            case (int)EV_POWER_DOWN: return(MeterEvent.POWERDOWN);
            case (int)EV_EVENT_LOG_CLEARED: return(MeterEvent.OTHER);
            case (int)EV_LOAD_PROFILE_CLEARED: return(MeterEvent.CLEAR_DATA);
            default: return(MeterEvent.OTHER);
        } // switch(lLogCode)
    } // private void mapLogCodes(long lLogCode)

    /**
     * Return the serialNumber of the device.
     *
     * @return the serialNumber of the device.
     * @throws java.io.IOException if an error occurs during the read
     */
    @Override
    public String getSerialNumber() throws IOException {
        /** The serial number is present in a reserved object: COSEM Logical device name object
         * In order to facilitate access using SN referencing, this object has a reserved short name by DLMS/COSEM convention: 0xFD00.
         * See topic 'Reserved base_names for special COSEM objects' in the DLMS Blue Book.
         **/
        String retrievedSerial = getCosemObjectFactory().getGenericRead(0xFD00, DLMSUtils.attrLN2SN(2)).getString();
        if (retrievedSerial.startsWith("LGZ")) {
            return retrievedSerial.substring(3);
        } else {
            return retrievedSerial;
        }
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
					throw new MissingPropertyException(key + " key missing");
				}
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            // KV 19012004
            if ((strID != null) &&(strID.length()>16)) {
				throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
			}

            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            //if (strPassword.length()!=8) throw new InvalidPropertyException("Password must be exact 8 characters.");
            iHDLCTimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","5").trim());
            iDelayAfterFailProperty=Integer.parseInt(properties.getProperty("DelayAfterfail","3000").trim());
            iRequestTimeZone=Integer.parseInt(properties.getProperty("RequestTimeZone","0").trim());
            iRequestClockObject=Integer.parseInt(properties.getProperty("RequestClockObject","0").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iClientMacAddress=Integer.parseInt(properties.getProperty("ClientMacAddress","32").trim());
            iServerUpperMacAddress=Integer.parseInt(properties.getProperty("ServerUpperMacAddress","1").trim());
            iServerLowerMacAddress=Integer.parseInt(properties.getProperty("ServerLowerMacAddress","0").trim());
            eventIdIndex=Integer.parseInt(properties.getProperty("EventIdIndex","-1").trim()); // ZMD=1, ZMQ=2

        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException("DLMS ZMD, validateProperties, NumberFormatException, "+e.getMessage());
        }
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
			ObisCodeMapper ocm = new ObisCodeMapper(getCosemObjectFactory(), getMeterConfig(), this);
			return ocm.getRegisterValue(obisCode);
		} catch (Exception e) {
			throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
		}
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    /**
     * Execute a billing reset on the device. After receiving the 'Demand Reset'
     * command the meter executes a demand reset by doing a snap shot of all
     * energy and demand registers.
     *
     * @throws java.io.IOException
     */
    public void resetDemand() throws IOException {
        GenericInvoke gi = new GenericInvoke(this, new ObjectReference(getMeterConfig().getObject(new DLMSObis(ObisCode.fromString("0.0.240.1.0.255").getLN(), (short)10100, (short)0)).getBaseName()),6);
        gi.invoke(new Integer8(0).getBEREncodedByteArray());
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        this.messageProtocol.applyMessages(messageEntries);
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messageProtocol.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return this.messageProtocol.getMessageCategories();
    }

    public String writeMessage(final Message msg) {
        return this.messageProtocol.writeMessage(msg);
    }

    public String writeTag(final MessageTag tag) {
        return this.messageProtocol.writeTag(tag);
    }

    public String writeValue(final MessageValue value) {
        return this.messageProtocol.writeValue(value);
    }

    /**
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return ((ZmdMessages) this.messageProtocol).getTimeOfUseMessageBuilder();
    }

    /**
     * Get the TimeOfUseMessagingConfig object that contains all the capabilities for the current protocol
     *
     * @return the config object
     */
    public TimeOfUseMessagingConfig getTimeOfUseMessagingConfig() {
        return ((ZmdMessages) this.messageProtocol).getTimeOfUseMessagingConfig();
    }
}
