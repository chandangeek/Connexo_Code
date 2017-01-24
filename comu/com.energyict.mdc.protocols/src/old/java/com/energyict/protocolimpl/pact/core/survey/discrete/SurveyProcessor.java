/*
 * SurveyProcessor.java
 *
 * Created on 16 maart 2004, 17:21
 * changes:
 * KV 02/12/2004 check for doubles changed...
 */

package com.energyict.protocolimpl.pact.core.survey.discrete;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.protocolimpl.pact.core.common.ChannelMap;
import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;
import com.energyict.protocolimpl.pact.core.common.PACTProtocolException;
import com.energyict.protocolimpl.pact.core.common.PactUtils;
import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingsInterpreter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class SurveyProcessor {

    private static final int DEBUG=0;

    private List surveyDays;
    private ChannelMap channelMap;
    private MeterReadingsInterpreter mri;
    private List intervalDatas;
    private int modulo;
    private SurveyDay surveyRecentDay;
    private int nrOfChannels;
    private int nrOfIntervals=0;
    private int profileInterval;
    private boolean statusFlagChannel;

    /** Creates a new instance of DiscreteProcessor */
    public SurveyProcessor(List surveyDays, ChannelMap channelMap, MeterReadingsInterpreter mri, boolean statusFlagChannel) {
        this.surveyDays=surveyDays;
        this.channelMap=channelMap;
        this.mri=mri;
        this.statusFlagChannel=statusFlagChannel;
        init();
    }

    private SurveyDay getMostRecentSurveyDay() {
        if (getMri().getProtocolLink().getPACTMode().isPAKNET()) {
            return (SurveyDay)surveyDays.get(surveyDays.size()-1);
        }
        else {
            return (SurveyDay)surveyDays.get(0);
        }
    }

    private void printSurveyDays() {
        for (int day=0;day<surveyDays.size();day++) {
            SurveyDay sd = (SurveyDay)surveyDays.get(day);
            System.out.println("day "+day);
            for (int eod=0;eod<sd.getEods().length;eod++) {
                System.out.println("      "+sd.getEods()[eod]);
            }
        }
    }

    private void init() {

       surveyRecentDay = getMostRecentSurveyDay();

if (DEBUG>=1) {
       printSurveyDays();
}

        nrOfChannels = getNrOfChannels();
        profileInterval = (86400/((mri.getSurveyInfo().getBlocks()-1)*4)); // in seconds
        buildIntervalDatas();
        modulo = DiscreteSurvey.MODULO[mri.getSurveyFlagsInfo().getSurtyp()-38];
        nrOfIntervals = getIntervalDatas().size();
    }


    /*
     *  Process all surveydays, remove doubles and shift the flags over one interval
     *
     */
    private void buildIntervalDatas() {

        // Copy all surveydays to 1 linear intervaldata list
        intervalDatas = new ArrayList();
        Iterator it = surveyDays.iterator();
        while(it.hasNext()) {
            SurveyDay surveyDay = (SurveyDay)it.next();
            surveyDay.getIntervalDatas();
            intervalDatas.addAll(surveyDay.getIntervalDatas());
        } // while(it.hasNext())
        Collections.sort(intervalDatas);

        // KV 25082004
        // Check for doubles and copy the status flags
        if (intervalDatas.size() > 0) {
            IntervalData start,end=null,check;
            check = (IntervalData)intervalDatas.get(intervalDatas.size()-1);
            for (int i=intervalDatas.size()-2;i>=0;i--) {
                if (DEBUG>=2) {
					System.out.println(check);
				}
                start = (IntervalData)intervalDatas.get(i);
                if (start.getEndTime().compareTo(check.getEndTime()) == 0) {
                    if (mri.getProtocolLink().getPACTMode().isPAKNET()) {
						intervalDatas.remove(i+1);
					} else {
						intervalDatas.remove(i);
					}
                    if (end != null) {
						end.copyStatus(start);
					}
                }
                else {
                    if (end != null) {
						end.copyStatus(check);
					}
                    end = check;
                }
                check = start;
            } // for (int i=intervalDatas.size()-2;i>=0;i--)
        } // if (intervalDatas.size() > 0)

        // check for day boundary cross...
        /*
         *  Reorder timestamps. interval(Timestamp) = interval-1(Timestamp) + (profileInterval*1000);
         *  2/12/2004 16:00  A
         *            16:30  B    = B-A consumption   16u35 power down -> interval end date = 16:30 !!!!
         *  3/12/2004 8:00   C    = C-B consumption   8u15 power up -> interval end date = 17:00 FLAG POWERDOWN AND NOT 8:00 !!!!
         *            8:30   D    = D-C consumption   normal interval -> interval end date = 8:30 FLAG POWERUP
         */
         if (intervalDatas.size() > 0) {
            IntervalData start,end=null,check;
            check = (IntervalData)intervalDatas.get(intervalDatas.size()-1);
            for (int i=intervalDatas.size()-2;i>=0;i--) {
                start = (IntervalData)intervalDatas.get(i);
                check = new IntervalData(new Date(start.getEndTime().getTime()+(profileInterval*1000)),check.getEiStatus(),check.getProtocolStatus(),check.getTariffCode(),check.getIntervalValues());
                intervalDatas.set(i+1,check); // replace IntervalData
                check = start;
            } // for (int i=intervalDatas.size()-2;i>=0;i--)
        } // if (intervalDatas.size() > 0)
    } // private void buildIntervalDatas()


    private int[] getValues(int channel) {
       int[] values = new int[getIntervalDatas().size()];
       for (int interval = 0;interval < nrOfIntervals; interval++) {
            IntervalData intervalData = (IntervalData)getIntervalDatas().get(interval); // get IntervalData
            values[interval] = ((IntervalValue)intervalData.getIntervalValues().get(channel)).getNumber().intValue(); // get IntervalValue for channel
       }
       return values;
    }

    private void addProcessedValue(int interval,int channel,BigDecimal processedValue) {
       IntervalData intervalData = (IntervalData)getIntervalDatas().get(interval);
       IntervalValue intervalValue = (IntervalValue)intervalData.getIntervalValues().get(channel);
       intervalValue.setNumber(processedValue);
    }

    private EndOfDayBlock getEndOfDayBlock(int channel) {
        int index=0;
        if (!statusFlagChannel) {
           for(int i=0;i<surveyRecentDay.getEods().length;i++) {
               if (!(EnergyTypeCode.isStatusFlagsChannel(surveyRecentDay.getEods()[i].getEtype()))) {
                  if (index == channel) {
					return surveyRecentDay.getEods()[i];
				}
                  index++;
               }
           }
        }
        else {
           return surveyRecentDay.getEods()[channel];
        }
        return null;
    }

    private int getNrOfChannels() {
        int count=0;
        if (!statusFlagChannel) {
            for(int i=0;i<surveyRecentDay.getEods().length;i++) {
				if (!(EnergyTypeCode.isStatusFlagsChannel(surveyRecentDay.getEods()[i].getEtype()))) {
					count++;
				}
			}
        } else {
			count=surveyRecentDay.getEods().length;
		}
        return count;
    }

    public void process() throws PACTProtocolException {
        // TO DO
        // f(energyType) and f(channelfunction) do calculation...
        // energyType: via EOD block in surveyDay...
        // channelfunction: via channelmap...

        BigDecimal processedValue;
        for (int channel=0; channel<nrOfChannels; channel++) {
            EndOfDayBlock eod = getEndOfDayBlock(channel);  // use EOD of most recent day
            if (DEBUG>=1) {
				System.out.println("most recent day's EOD block="+eod);
			}

                int[] values = getValues(channel); // copy all integer raw values to array
                int start = values[0]; // get first value;
                for(int interval=1; interval<nrOfIntervals; interval++) {
                    int end = values[interval];
                    switch(channelMap.getChannelFunction(channel)) {
                        case ChannelMap.FUNCTION_CUMULATIVE:
                        case ChannelMap.FUNCTION_DEFAULT: {
                            // leave intervaldatas as is!
                            if ((eod.getEtype() >= 0xC0) && (eod.getEtype() <= 0xCF)) { // flags parameter
                                processedValue = BigDecimal.valueOf(end);
                            } else {
								processedValue = BigDecimal.valueOf(end);
							}
                        } break; // FUNCTION_DEFAULT

                        case ChannelMap.FUNCTION_SURVEY_ADVANCE: {
                            if ((eod.getEtype() >= 0xC0) && (eod.getEtype() <= 0xCF)) { // flags parameter
                                processedValue = BigDecimal.valueOf(end);
                            } else {
								processedValue = PactUtils.convert2BigDecimal(getSurveyAdvance(start, end, modulo, eod));
							}
                        } break; // FUNCTION_SURVEY_ADVANCE

                        case ChannelMap.FUNCTION_ACTUAL_ADVANCE: {
                            if ((eod.getEtype() >= 0xC0) && (eod.getEtype() <= 0xCF)) { // flags parameter
                                processedValue = BigDecimal.valueOf(end);
                            }
                            else if ((eod.getEtype() >= 0x00) && (eod.getEtype() <= 0x25)) {
								processedValue = PactUtils.convert2BigDecimal(getSurveyAdvance(start, end, modulo, eod));
							} else {
								processedValue = PactUtils.convert2BigDecimal(getActualAdvance(start, end, modulo, eod));
							}
                        } break; // FUNCTION_ACTUAL_ADVANCE

                        case ChannelMap.FUNCTION_DEMAND: {
                            if ((eod.getEtype() >= 0xC0) && (eod.getEtype() <= 0xCF)) { // flags parameter
                                processedValue = BigDecimal.valueOf(end);
                            }
                            else if ((eod.getEtype() >= 0x00) && (eod.getEtype() <= 0x25)) {
								processedValue = PactUtils.convert2BigDecimal(getSurveyAdvance(start, end, modulo, eod));
							} else {
								processedValue = PactUtils.convert2BigDecimal(getDemand(start, end, modulo, profileInterval, eod));
							}
                        } break; // FUNCTION_DEMAND

                        default:
                            throw new PACTProtocolException("Invalid channel function "+channelMap.getChannelFunction(channel)+", correct first");

                    } // switch(channelMap.getChannelFunction(channel))

                    // Write new processedValue to the right IntervalValue
                    //processedValue = processedValue.setScale(FRACTIONAL_RESULT,BigDecimal.ROUND_HALF_UP);



                    addProcessedValue(interval,channel,processedValue);

                    // keep end as start value for next calculation
                    start=end;
                } // for(int interval=0; interval<nrOfIntervals; interval++) {
            //} // if (statusFlagChannel || ((!(EnergyTypeCode.isStatusFlagsChannel(eod.getEtype()))) && (!statusFlagChannel)))
        } // for (int channel = 0; channel<nrOfChannels; channel++)
        getIntervalDatas().remove(0); // remove first IntervalData from List
    } // public void process()



    /*
     *  KV/KH 08072004
     *  Because of rounding issues for temporary results, the end result will not be exact! therefor we changed the calculation
     *  to double, which is in fact not correct but the end result is correct.
     *  The end result is transferred into a BigDecimal with PactUtils.FRACTIONALDIGITS scale!
     *  the original methods are still in comment!
     */

    // Value parse calculation methods
    private double getSurveyAdvance(int startVal, int endVal, int modulo, EndOfDayBlock eod) {
        // Calculate OFFSET
        double OFFSET;
        // instantaneous parameters
        if ((eod.getEtype() >= 0x00) && (eod.getEtype() <= 0x25)) {
           OFFSET = (double)modulo/(double)2;
        }
        else {
           if (eod.isFlagsSGN()) {
			OFFSET = (double)modulo/(double)2;
		} else {
			OFFSET = 0;
		}
        }

        // Calculate delta
        double delta;
        // instantaneous parameters
        if ((eod.getEtype() >= 0x00) && (eod.getEtype() <= 0x25)) {
			delta = (double)endVal;
		} else {
			delta = ((double)endVal-(double)startVal);
		}
        return ((delta + (double)modulo + OFFSET) % (double)modulo) - OFFSET;
    } //  private double getSurveyAdvance(int startVal, int endVal)

    private double getActualAdvance(int startVal, int endVal, int modulo, EndOfDayBlock eod) {
        double SURFAC = mri.getSurveyFlagsInfo().getSurfac();
        double surveyAdvance = getSurveyAdvance(startVal,endVal,modulo,eod);
        double exp = Math.pow(10, mri.getSurveyFlagsInfo().getMeterFactorExp());
        double actualAdvance;
        // non-elektrical parameters
        if ((eod.getEtype() >= 0xB0) && (eod.getEtype() <= 0xBF)) {
           actualAdvance = surveyAdvance * exp;
        }
        // energy parameters
        else {
           actualAdvance = surveyAdvance * exp;
           actualAdvance = actualAdvance /(SURFAC * eod.getFlagsDi());
        }

        return actualAdvance;
    } // private double getActualAdvance(int startVal, int endVal)

    private double getDemand(int startVal, int endVal, int modulo, int profileInterval, EndOfDayBlock eod) {
        double actualAdvance = getActualAdvance(startVal,endVal,modulo,eod);
        actualAdvance = actualAdvance * ((double)3600/(double)profileInterval);
        return actualAdvance;
    }

/*
    // Value parse calculation methods
    private BigDecimal getSurveyAdvance(int startVal, int endVal, int modulo, EndOfDayBlock eod) {
        // Calculate OFFSET
        int OFFSET;
        // instantaneous parameters
        if ((eod.getEtype() >= 0x00) && (eod.getEtype() <= 0x25)) {
           OFFSET = modulo/2;
        }
        else {
           if (eod.isFlagsSGN()) OFFSET = modulo/2;
           else OFFSET = 0;
        }

        // Calculate delta
        int delta;
        // instantaneous parameters
        if ((eod.getEtype() >= 0x00) && (eod.getEtype() <= 0x25))
           delta = endVal;
        else
           delta = (endVal-startVal);

        long surveyAdvance = (long)(((delta + modulo + OFFSET) % modulo) - OFFSET);

        BigDecimal temp = BigDecimal.valueOf(surveyAdvance);
        temp = temp.setScale(PactUtils.FRACTIONAL_DIGITS);
        return temp;
    } //  private BigDecimal getSurveyAdvance(int startVal, int endVal)

    private BigDecimal getActualAdvance(int startVal, int endVal, int modulo, EndOfDayBlock eod) {
        int SURFAC = mri.getSurveyFlagsInfo().getSurfac();
        BigDecimal surveyAdvance = getSurveyAdvance(startVal,endVal,modulo,eod);

        //int exp = (int)Math.pow(10,mri.getSurveyFlagsInfo().getMeterFactorExp());

        BigDecimal exp = new BigDecimal(Math.pow(10, mri.getSurveyFlagsInfo().getMeterFactorExp()));
        // reduce scale
        exp=exp.setScale((mri.getSurveyFlagsInfo().getMeterFactorExp()<0?Math.abs(mri.getSurveyFlagsInfo().getMeterFactorExp()):0),BigDecimal.ROUND_HALF_UP);


        BigDecimal actualAdvance=null;
        // non-elektrical parameters
        if ((eod.getEtype() >= 0xB0) && (eod.getEtype() <= 0xBF)) {
           //actualAdvance = surveyAdvance.multiply(BigDecimal.valueOf(exp));
           actualAdvance = surveyAdvance.multiply(exp);
        }
        // energy parameters
        else {
           //actualAdvance = surveyAdvance.multiply(BigDecimal.valueOf(exp));
           actualAdvance = surveyAdvance.multiply(exp);
           actualAdvance = actualAdvance.divide(BigDecimal.valueOf(SURFAC * eod.getFlagsDi()),BigDecimal.ROUND_HALF_UP);
        }

        return actualAdvance;
    } // private BigDecimal getActualAdvance(int startVal, int endVal)

    private BigDecimal getDemand(int startVal, int endVal, int modulo, int profileInterval, EndOfDayBlock eod) {
        BigDecimal actualAdvance = getActualAdvance(startVal,endVal,modulo,eod);
        BigDecimal temp = actualAdvance.multiply(BigDecimal.valueOf(3600));
        temp = temp.divide(BigDecimal.valueOf(profileInterval),BigDecimal.ROUND_HALF_UP);
        return temp;
    }
*/

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
    private void setIntervalDatas(java.util.List intervalDatas) {
        this.intervalDatas = intervalDatas;
    }

    /**
     * Getter for property mri.
     * @return Value of property mri.
     */
    public com.energyict.protocolimpl.pact.core.meterreading.MeterReadingsInterpreter getMri() {
        return mri;
    }
}
