/*
 * ParameterDay.java
 *
 * Created on 12 maart 2004, 14:35
 */

package com.energyict.protocolimpl.pact.core.survey.discrete;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;
import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingsInterpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class SurveyDay {

    private static final int DEBUG=0;

    private List intervalDatas = new ArrayList(); // of type IntervalData

    // profile status flags
    public static final int PHASE_FAILURE = 0x8000;
    public static final int REVERSE_RUNNING = 0x4000;
    public static final int DATA_CHANGED = 0x2000;
    public static final int TIME_SET = 0x1000;

    // statusflags channel (eType from 0xC0 to 0xCF)

    // KV 29082006 We have added two new flags for compliance with UK Codes of Practice 2, 3 and 5.
    //             The flags are 'battery monitoring/clock fail' and 'outstation error'.
    //             These will be supported in Premier CLEM C5G8GR08 and any subsequent variants.
    public static final int OUTSTATION_ERROR = 0x0010;
    public static final int BATTERY_FAIL = 0x0008;

    public static final int OTHERDATA_CHANGED = 0x0004;
    public static final int TIMEDATE_CHANGED = 0x0002;
    public static final int DATA_INVALID = 0x0001;




    private EndOfDayBlock[] eods;
    private int nrOfIntervals;
    private int mask;
    private MeterReadingsInterpreter mri;
    private int profileInterval;
    private TimeZone timeZone;
    private boolean statusFlagChannel;

    /** Creates a new instance of ParameterDay */
    public SurveyDay(MeterReadingsInterpreter mri, TimeZone timeZone, boolean statusFlagChannel) {
        this.timeZone=timeZone;
        this.nrOfIntervals = (mri.getSurveyInfo().getBlocks()-1)*4; // without EOD block, 16 bit intervals
        this.mri=mri;
        this.statusFlagChannel=statusFlagChannel;
        this.profileInterval = (86400/nrOfIntervals); // in seconds
        int profileTypeIndex = mri.getSurveyFlagsInfo().getSurtyp()-38;
        this.mask = DiscreteSurvey.VALUEMASK[profileTypeIndex];
    }


    private int mapStatusFlags(int protocolStatus) {
        int eiStatus=0;
        if ((protocolStatus & PHASE_FAILURE) == PHASE_FAILURE) {
            eiStatus |= IntervalStateBits.PHASEFAILURE;
        }
        if ((protocolStatus & REVERSE_RUNNING) == REVERSE_RUNNING) {
            eiStatus |= IntervalStateBits.REVERSERUN;
        }
        if ((protocolStatus & DATA_CHANGED) == DATA_CHANGED) {
            eiStatus |= IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if ((protocolStatus & TIME_SET) == TIME_SET) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }
        return eiStatus;
    } // private int mapStatusFlags(int protocolStatus)

    private int mapStatusFlagsChannel(int protocolStatus) {
        int eiStatus=0;
        if ((protocolStatus & OTHERDATA_CHANGED) == OTHERDATA_CHANGED) {
            eiStatus |= IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if ((protocolStatus & TIMEDATE_CHANGED) == TIMEDATE_CHANGED) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }

        // KV 29082006
        if ((protocolStatus & BATTERY_FAIL) == BATTERY_FAIL) {
            eiStatus |= IntervalStateBits.BATTERY_LOW;
        }
        if ((protocolStatus & OUTSTATION_ERROR) == OUTSTATION_ERROR) {
            eiStatus |= IntervalStateBits.DEVICE_ERROR;
        }
        return eiStatus;
    } // private int mapStatusFlagsChannel(int protocolStatus)


    /*
     * @param data full load survey data from most recent day to last day
     * @param day index 0 for today, -1 for today -1 day, etc...
     */
    public void parseData(byte[] data, int day) throws IOException {
        getEndOfDayBlocks(data,day);
        if (channelValid()) {
			getRawIndexes(data,day);
		}
    } // public void parseData(byte[] data)

    /*
     *  If one of the channels is invalid marked in the EOD block, no need to
     *  add IntervalData. Leave gap!
     */
    private boolean channelValid() {
        int nrOfChannels = mri.getSurveyInfo().getNrOfChannels();
        for (int channel = 0; channel < nrOfChannels; channel++) {
            // if one of the channels is invalid
            if (!eods[channel].isValid()) {
				return false;
			}
        }
        return true;
    } // private boolean channelValid()

    /*
     * parse all EOD blocks for a certain day
     * @param data full load survey data from most recent day to last day
     * @param day index 0 for today, -1 for today -1 day, etc...
     */
    private void getEndOfDayBlocks(byte[] data, int day) throws IOException {
        int nrOfChannels = mri.getSurveyInfo().getNrOfChannels();
        eods = new EndOfDayBlock[mri.getSurveyInfo().getNrOfChannels()];
        for (int channel = 0; channel < nrOfChannels; channel++) {
            // get eod block using day and channel
            int eodIndex = (((nrOfChannels-1)-channel)*mri.getSurveyInfo().getBlocks() + (mri.getSurveyInfo().getBlocks()-1)) * 8; // point to the right channel
            eodIndex += (day*(-1))*(mri.getSurveyInfo().getBlocks()*8*nrOfChannels); // in the right day
            try {
                eods[channel] = new EndOfDayBlock(ProtocolUtils.getSubArray2(data,eodIndex,8),timeZone);
                if (DEBUG >= 1) {
					System.out.println("KV_DEBUG (SurveyDay)>"+eods[channel].toString());
				}
            }
            catch(ArrayIndexOutOfBoundsException e) {
                throw new IOException("Invalid eodIndex ("+eodIndex+"), day is invalid!, "+e.toString());
            }
        } // for (int channel = 0; channel < mri.getSurveyInfo().getNrOfChannels(); channel++)
    } // private void getEndOfDayBlocks(byte[] data, int day)

    /*
     *  Build List with IntervalData objects for day using data
     *  @param day used to point into data
     *  @param data full load survey data from most recent day to last day
     */
    private void getRawIndexes(byte[] data, int day) throws IOException {
        int nrOfChannels = mri.getSurveyInfo().getNrOfChannels();
        Calendar calendar = (Calendar)eods[0].getDate().clone(); // get date of first channel
        if (DEBUG >= 1) {
			System.out.println("KV_DEBUG (SurveyDay)> EOD time = "+calendar.getTime());
		}
        // get raw index from 00:00 to end of the day
        for (int interval=0; interval < nrOfIntervals ; interval++) {
            IntervalData intervalData = new IntervalData(((Calendar)(calendar.clone())).getTime());
            if (DEBUG >= 2) {
				if (interval > 0) {
					System.out.println();
				}
			}
            if (DEBUG >= 2) {
				System.out.print("KV_DEBUG (SurveyDay)> "+intervalData.getEndTime());
			}

            int intervalIndex = interval*2;
            for (int channel = 0; channel < mri.getSurveyInfo().getNrOfChannels(); channel++) {
                // get values using day, channel and interval
                int dayIndex = (day*(-1))*(mri.getSurveyInfo().getBlocks()*8*nrOfChannels); // points to the right day
                int channelIndex = (((nrOfChannels-1)-channel)*mri.getSurveyInfo().getBlocks()) * 8; // point to the right channel at 00:00
                int valueIndex = dayIndex + channelIndex + intervalIndex; // point to the right value in the right channel on the right day
                try {
                    int value = ProtocolUtils.short2int(ProtocolUtils.getShortLE(data, valueIndex));

                    // If one of the channels hase a
                    // value == 0xFFFF, break and don't add intervalData to list
                    if (value == 0xFFFF) {
                        intervalData = null;
                        break;
                    }

                    // if statusflags channel exist, use it to set ProtocolStatus and EIStatus
                    // Following mail from Martin Kempf, this status flags channel is introduced
                    // for the Code 5 issue 2 meters C5G... and C5D... CLEMS to add extra info to meters
                    // when bit 13 is set in type 38 load surveys.
                    // We should check if meter is C5 Issue 2 and then if bit 13 of first channel of load profile is set, then
                    // we should check the bits of the extra channel.
                    // I do not check bit 13 but always check all status flags!
                    if (EnergyTypeCode.isStatusFlagsChannel(eods[channel].getEtype())) {
                        // extract flags from first channel
                        intervalData.addProtocolStatus(value);
                        intervalData.addEiStatus(mapStatusFlagsChannel(value));
                        if ((value & DATA_INVALID) == DATA_INVALID) {
                            intervalData = null;
                            break;
                        }
                    }
                    else {
                        // If the channel is no statusflag channel, it might have statusbits
                        // bit 15..13or12
                        int protocolStatus = value & (mask^0xFFFF);
                        intervalData.addProtocolStatus(protocolStatus);
                        intervalData.addStatus(mapStatusFlags(protocolStatus));
                        value &= mask;
                    }

                    if (statusFlagChannel || ((!(EnergyTypeCode.isStatusFlagsChannel(eods[channel].getEtype()))) && (!statusFlagChannel))) {
                       if (DEBUG >= 2) {
						System.out.print(" "+channel+":"+value);
					}
                       intervalData.addValue(new Integer(value));
                    }
                }
                catch(ArrayIndexOutOfBoundsException e) {
                    throw new IOException("Invalid valueIndex ("+valueIndex+"), interval "+interval+" for channel "+channel+" is invalid!, "+e.toString());
                }
            } // for (int channel = 0; channel < mri.getSurveyInfo().getNrOfChannels(); channel++)

            if (intervalData != null) {
                intervalDatas.add(intervalData);
                if (DEBUG >= 2) {
					System.out.println("KV_DEBUG (SurveyDay)> "+intervalData);
					//System.out.println(intervalData.toString());
				}
            }
            //else
              //  System.out.println("KV_DEBUG> missing value");

            calendar.add(Calendar.SECOND, profileInterval); // adjust interval...

        } // for (int interval; interval < nrOfIntervals ; interval++)

    } // private void getRawIndexes(byte[] data, int day)


    /** Getter for property intervalDatas.
     * @return Value of property intervalDatas.
     *
     */
    public java.util.List getIntervalDatas() {
        return intervalDatas;
    }

    /** Setter for property intervalDatas.
     * @param intervalDatas New value of property intervalDatas.
     *
     */
    public void setIntervalDatas(java.util.List intervalDatas) {
        this.intervalDatas = intervalDatas;
    }

    /** Getter for property eods.
     * @return Value of property eods.
     *
     */
    public com.energyict.protocolimpl.pact.core.survey.discrete.EndOfDayBlock[] getEods() {
        return this.eods;
    }

    /** Setter for property eods.
     * @param eods New value of property eods.
     *
     */
    public void setEods(com.energyict.protocolimpl.pact.core.survey.discrete.EndOfDayBlock[] eods) {
    	if(eods != null){
    		this.eods = eods.clone();
    	}
    }

} // public class ParameterDay
