/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Class14LoadProfileConfiguration.java
 *
 * Created on 13 juli 2005, 16:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class14LoadProfileConfiguration extends AbstractClass {

    ClassIdentification classIdentification = new ClassIdentification(14,42,true);


    private static final int MAX_PROFILE_CHANNELS=4;
    private static final int DAY_HEADER_SIZE=6;

    // SPARE [3]
    int RLPSCAL;
    int LPLEN;
    int DASIZE;
    int LPMEM;
    int CHANS;
    int[] INPUTQUANTITIES = new int[MAX_PROFILE_CHANNELS];

    public String toString() {

        if ((RLPSCAL+LPLEN+DASIZE+LPMEM+CHANS)==0)
            return "NO Profile data configured in the meter!";

        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Class14LoadProfileConfiguration: RLPSCAL="+RLPSCAL);
        strBuff.append(", LPLEN="+LPLEN);
        strBuff.append(", DASIZE="+DASIZE);
        strBuff.append(", LPMEM="+LPMEM);
        strBuff.append(", CHANS="+CHANS);
        for (int i=0;i<4;i++) {
            strBuff.append(", INPUTQUANTITIES["+i+"]="+INPUTQUANTITIES[i]);
        }
        try {
            strBuff.append("\ngetDayRecordSize()="+getDayRecordSize()+", ");
            strBuff.append("getLoadProfileInterval()="+getLoadProfileInterval()+", ");
            strBuff.append("getNrOfChannels()="+getNrOfChannels()+"\n");
            for (int profileChannel=0;profileChannel<getNrOfChannels();profileChannel++) {
               strBuff.append("getMeterChannelIndex("+profileChannel+")="+getMeterChannelIndex(profileChannel)+", ");
               strBuff.append("getUnit("+profileChannel+")="+getUnit(profileChannel)+"\n");
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }


        return strBuff.toString();
    }

    /** Creates a new instance of Class14LoadProfileConfiguration */
    public Class14LoadProfileConfiguration(ClassFactory classFactory) {
        super(classFactory);
    }

    protected void parse(byte[] data) throws IOException {
        RLPSCAL = ProtocolUtils.getInt(data,3,1);
        LPLEN = ProtocolUtils.getInt(data,4,1);
        DASIZE = ProtocolUtils.getInt(data,5,2);
        LPMEM = ProtocolUtils.getInt(data,7,1);
        CHANS = ProtocolUtils.getInt(data,8,1);
        for (int i=0;i<MAX_PROFILE_CHANNELS;i++) {
           INPUTQUANTITIES[i] = ProtocolUtils.getInt(data,9+i,1);
        }
    }

    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

    public int getRLPSCAL() {
        return RLPSCAL;
    }

    public int getLPLEN() {
        return LPLEN;
    }

    public int getDASIZE() {
        return DASIZE;
    }

    public int getLPMEM() {
        return LPMEM;
    }

    public int getCHANS() {
        return CHANS;
    }

    public int[] getINPUTQUANTITIES() {
        return INPUTQUANTITIES;
    }

    public int getINPUTQUANTITY(int channel) {
        return INPUTQUANTITIES[channel];
    }


    /*************************************************************************************************
     * Derived methods to calculate profile specific parameters
     *************************************************************************************************/
    /*
     *  @result profile interval in seconds
     */
    public int getLoadProfileInterval() {
        return getLPLEN()*60;
    }
    /*
     *  @result nr of configured profile data channels
     *  @throws IOException if nr of configured channels > MAX_PROFILE_CHANNELS
     */
    public int getNrOfChannels() throws IOException {
        int nrOfChannels = getCHANS();
        if (nrOfChannels > MAX_PROFILE_CHANNELS)
            throw new IOException("Class14LoadProfileConfiguration, getNrOfChannels(), Nr of channels "+getCHANS()+" is more then what the protocol documentation's data dictionary explains ("+MAX_PROFILE_CHANNELS+") !");
        return nrOfChannels;
    }
    /*
     *  @throws IOException if calculated daylength does not match with the daylength within the class
     *  @result day length in bytes...
     */
    public int getDayRecordSize() throws IOException {
        int dalen = ((3600 / getLoadProfileInterval())*24)*2*getCHANS() + DAY_HEADER_SIZE;
        if (dalen != getDASIZE())
            throw new IOException("Class14LoadProfileConfiguration, getDayRecordSize(), class field DASIZE ("+getDASIZE()+") does not mach with calculated length ("+dalen+")");
        return dalen;
    }

    public int getIntervalsPerDay() {
        return 24 * (3600/getLoadProfileInterval());
    }
    public int getIntervalsPerHour() {
        return (3600/getLoadProfileInterval());
    }

    public Unit getUnit(int profileChannel) throws IOException {
       int channelCount=0;
       for (int meterChannel=0;meterChannel<MAX_PROFILE_CHANNELS;meterChannel++) {
           if (getINPUTQUANTITY(meterChannel) > 0) {
               if (channelCount==profileChannel) {
                   return getInputQuantityUnit(getINPUTQUANTITY(meterChannel));
               }
               channelCount++;
           }
       }
       throw new IOException("Class14LoadProfileConfiguration, getUnit, invalid profileChannel "+profileChannel);
    } // public Unit getUnit(int profileChannel) throws IOException

    /*
     *   @result The meter's channel index.
     *           E.g. 2 active channels, channel 0 and 3 enabled, means, profile data channels 0 and 1 have respectively
     *                meterchannelindex 0 and 3
     */
    public int getMeterChannelIndex(int profileChannel) throws IOException {
       int channelCount=0;
       for (int meterChannel=0;meterChannel<MAX_PROFILE_CHANNELS;meterChannel++) {
           if (getINPUTQUANTITY(meterChannel) >0) {
               if (channelCount==profileChannel) {
                   return meterChannel;
               }
               channelCount++;
           }
       }
       throw new IOException("Class14LoadProfileConfiguration, getUnit, invalid profileChannel "+profileChannel);
    } // public int getMeterChannelIndex(int profileChannel)

    private Unit getInputQuantityUnit(int inputQuantity) throws IOException {
        if ((inputQuantity == 1) || (inputQuantity == 2))
            return Unit.get("kW");
        else if ((inputQuantity >= 3) && (inputQuantity <= 4)) {
            if (classFactory.getClass8FirmwareConfiguration().isKMeterType())
                return Unit.get("kVA");
            else if (classFactory.getClass8FirmwareConfiguration().isRMeterType())
                return Unit.get("kvar");
            else
                throw new IOException("Class14LoadProfileConfiguration, getInputQuantityUnit(), Wrong inputQuantity "+inputQuantity+" for metertype "+classFactory.getClass8FirmwareConfiguration().getMeterType());
        }
        else if ((inputQuantity >= 5) && (inputQuantity <= 8)) {
            if (classFactory.getClass8FirmwareConfiguration().isRMeterType())
                return Unit.get("kvar");
            else
                throw new IOException("Class14LoadProfileConfiguration, getInputQuantityUnit(), Wrong inputQuantity "+inputQuantity+" for metertype "+classFactory.getClass8FirmwareConfiguration().getMeterType());
        }
        else if (inputQuantity == 9)
            return classFactory.getClass8FirmwareConfiguration().getBlockPhenomenonUnit(0, false);
        else if (inputQuantity == 10)
            return classFactory.getClass8FirmwareConfiguration().getBlockPhenomenonUnit(1, false);
        else throw new IOException("Class14LoadProfileConfiguration, getInputQuantityUnit, invalid inputQuantity "+inputQuantity);
    }

    public int getPhenomenon(int profileChannel) throws IOException {
       int channelCount=0;
       for (int meterChannel=0;meterChannel<MAX_PROFILE_CHANNELS;meterChannel++) {
           if (getINPUTQUANTITY(meterChannel) > 0) {
               if (channelCount==profileChannel) {
                   return getInputQuantityPhenomenon(getINPUTQUANTITY(meterChannel));
               }
               channelCount++;
           }
       }
       throw new IOException("Class14LoadProfileConfiguration, getPhenomenon, invalid profileChannel "+profileChannel);
    } // public Unit getPhenomenon(int profileChannel) throws IOException

    private int getInputQuantityPhenomenon(int inputQuantity) throws IOException {
        if ((inputQuantity == 1) || (inputQuantity == 2))
            return Class8FirmwareConfiguration.PHENOMENON_ACTIVE;
        else if ((inputQuantity >= 3) && (inputQuantity <= 4)) {
            if (classFactory.getClass8FirmwareConfiguration().isKMeterType())
                return Class8FirmwareConfiguration.PHENOMENON_APPARENT;
            else if (classFactory.getClass8FirmwareConfiguration().isRMeterType())
                return Class8FirmwareConfiguration.PHENOMENON_REACTIVE;
            else
                throw new IOException("Class14LoadProfileConfiguration, getInputQuantityPhenomenon(), Wrong inputQuantity "+inputQuantity+" for metertype "+classFactory.getClass8FirmwareConfiguration().getMeterType());
        }
        else if ((inputQuantity >= 5) && (inputQuantity <= 8)) {
            if (classFactory.getClass8FirmwareConfiguration().isRMeterType())
                return Class8FirmwareConfiguration.PHENOMENON_REACTIVE;
            else
                throw new IOException("Class14LoadProfileConfiguration, getInputQuantityPhenomenon(), Wrong inputQuantity "+inputQuantity+" for metertype "+classFactory.getClass8FirmwareConfiguration().getMeterType());
        }
        else if (inputQuantity == 9)
            return classFactory.getClass8FirmwareConfiguration().getBlockPhenomenon(0);
        else if (inputQuantity == 10)
            return classFactory.getClass8FirmwareConfiguration().getBlockPhenomenon(1);
        else throw new IOException("Class14LoadProfileConfiguration, getInputQuantityPhenomenon(), invalid inputQuantity "+inputQuantity);
    } // private int getInputQuantityPhenomenon(int inputQuantity) throws IOException
}