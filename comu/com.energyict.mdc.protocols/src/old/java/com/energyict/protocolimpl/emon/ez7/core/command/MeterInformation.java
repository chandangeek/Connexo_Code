/*
 * MeterInformation.java
 *
 * Created on 26 mei 2005, 15:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Koen
 */
public class MeterInformation extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="VM";
    private static final int NR_OF_CHANNELS=8;

    int[] voltageMeterId=new int[NR_OF_CHANNELS];
    int[] currentMeterId=new int[NR_OF_CHANNELS];
    int[] ctSets = new int[NR_OF_CHANNELS];
    int[] inpType = new int[NR_OF_CHANNELS];

    public static final int MANUFACTURER_OTHERS=0;
    public static final int MANUFACTURER_EMON=1;
    int[] manufacturer = new int[NR_OF_CHANNELS];

    public static final int ENERGY_TYPE_OTHERS=0;
    public static final int ENERGY_TYPE_ELECTRIC=1;
    public static final int ENERGY_TYPE_WATER=2;
    public static final int ENERGY_TYPE_GAS=3;
    public static final int ENERGY_TYPE_ELECTRIC_PULSE_RATE_H=4;
    public static final int ENERGY_TYPE_ELECTRIC_DUAL_CHANNELS_PULSE_RATE_L=5;
    public static final int ENERGY_TYPE_ELECTRIC_DUAL_CHANNELS_PULSE_RATE_H=6;
    public static final int ENERGY_TYPE_RESERVED=7;
    int[] energyType = new int[NR_OF_CHANNELS];

    int[] pulseFactor = new int[NR_OF_CHANNELS];
    int[] kWhMultiplies = new int[NR_OF_CHANNELS];
    int[] pulseRatePerMinute = new int[NR_OF_CHANNELS];
    int[] cTRatio = new int[NR_OF_CHANNELS];
    int[] pTRatio = new int[NR_OF_CHANNELS];



    /** Creates a new instance of MeterInformation */
    public MeterInformation(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MeterInformation:\n");
        for(int channel=0;channel<NR_OF_CHANNELS;channel++) {
           builder.append("voltageMeterId=")
                   .append(getVoltageMeterId(channel))
                   .append(" currentMeterId=")
                   .append(getCurrentMeterId(channel))
                   .append(" ctSets=")
                   .append(getCtSets(channel))
                   .append(" inpType=")
                   .append(getInpType(channel))
                   .append(" manufacturer=")
                   .append(getManufacturer(channel))
                   .append(" energyType=")
                   .append(getEnergyType(channel))
                   .append(" pulseFactor=")
                   .append(getPulseFactor(channel))
                   .append(" kWhMultiplies=")
                   .append(getKWhMultiplies(channel))
                   .append(" pulseRatePerMinute=")
                   .append(getPulseRatePerMinute(channel))
                   .append(" cTRatio=")
                   .append(getCTRatio(channel))
                   .append(" pTRatio=")
                   .append(getPTRatio(channel))
                   .append("\n");
           builder.append("calculated pulseValue=").append(getPulseValue(channel)).append("\n");
        }
        return builder.toString();
    }

    public boolean isElectricEnergyType(int channel) {
        if ((getEnergyType(channel) == ENERGY_TYPE_ELECTRIC) ||
            (getEnergyType(channel) == ENERGY_TYPE_ELECTRIC_DUAL_CHANNELS_PULSE_RATE_H) ||
            (getEnergyType(channel) == ENERGY_TYPE_ELECTRIC_DUAL_CHANNELS_PULSE_RATE_L) ||
            (getEnergyType(channel) == ENERGY_TYPE_ELECTRIC_PULSE_RATE_H)) {
            return true;
        }
        else {
            return false; // other, gas, water, reserved
        }
    }

    public void build() throws IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    protected void parse(byte[] data) {
        if (DEBUG>=1) {
            System.out.println(new String(data));
        }

        CommandParser cp = new CommandParser(data);

        List values = cp.getValues("LINE-3");
        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
            int value = Integer.parseInt((String)values.get(channel),16);
            voltageMeterId[channel]= value&0x000F;
            currentMeterId[channel]= (value&0x00F0)>>4;
            ctSets[channel]= (value&0x0300)>>8;
            inpType[channel]= (value&0x0C00)>>10;
            manufacturer[channel]= (value&0x1000)>>12;
            energyType[channel]= (value&0xE000)>>13;
        }


        List values4 = cp.getValues("LINE-4");
        List values5 = cp.getValues("LINE-5");
        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
            int value4 = Integer.parseInt((String)values4.get(0),16);
            int value5 = Integer.parseInt((String)values5.get(0),16);
            pulseFactor[channel]= (value4&0xFF00)>>8;
            kWhMultiplies[channel]= (((value4&0x00FF)<<16)|value5)/100;
        }

        values = cp.getValues("LINE-6");
        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
            int value = Integer.parseInt((String)values.get(channel),16);
            if (pulseFactor[channel]<=8) {
                pulseRatePerMinute[channel] = value / 100;
            }
            else if ((pulseFactor[channel]>8) && (pulseFactor[channel]<=64)) {
                pulseRatePerMinute[channel] = value / 10;
            }
            else {
                pulseRatePerMinute[channel] = value;
            }
        }

        values = cp.getValues("LINE-7");
        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
            int value = Integer.parseInt((String)values.get(channel),16);
            cTRatio[channel]=value;
        }
        values = cp.getValues("LINE-8");
        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
            int value = Integer.parseInt((String)values.get(channel),16);
            pTRatio[channel]=value;
        }
    }


    // Page 28..31 of the EZ7 protocoldocumentation. Needs more clearification
    public double getPulseValue(int channel) {
        double pulseValue=1;
        if (manufacturer[channel] == MANUFACTURER_EMON) {
           if (voltageMeterId[channel] < 15)
               // change 1 by 1000 if you want watts and vars...
               // if csets == 0, use 1!!! (KV 27052005)
           {
               pulseValue = (1 * (double) (kWhMultiplies[channel] * (ctSets[channel] == 0 ? 1 : ctSets[channel]))) / (256 * pulseFactor[channel]);
           }
           else {
               pulseValue = cTRatio[channel] * pTRatio[channel];
           }
        }
        return pulseValue;
    }




    private BigDecimal convert2BigDecimal(double val, int channel) {
        BigDecimal bd = new BigDecimal(val);
        bd = bd.setScale(ez7CommandFactory.getEz7().getProtocolChannelValue(channel),BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    // Helper method that to calculate engineering values based upon
    // the configured ChannelMap info and this MeterInformation
    // @return double value
    public BigDecimal calculateValue(int channel, long value) {
        if (ez7CommandFactory.getEz7().getProtocolChannelValue(channel) == -1) {
            return BigDecimal.valueOf(value);
        }
        else {
//            if (ez7CommandFactory.getEz7().getProtocolChannelValue(channel)<10) {
//                double multiplier = Math.pow((double)10, (double)(ez7CommandFactory.getEz7().getProtocolChannelValue(channel)));
//                val = (double)Math.round(value*getPulseValue(channel)*multiplier)/multiplier;
//            }
//            else val=value*getPulseValue(channel);
            double val;
            val=value*getPulseValue(channel);
            return convert2BigDecimal(val, channel);
        }
    }

    // Get the EnergyUnit based on
    public ChannelInfo getChannelInfo(int channel, boolean energy) {
          Unit unit = doGetUnit(channel,energy);
          ChannelInfo chi = new ChannelInfo(channel,"EZ7 channel "+(channel+1),unit);
          BigDecimal bd = new BigDecimal(""+getPulseValue(channel));
          chi.setMultiplier(bd);
          return chi;
    }

    public Unit getUnit(int channel, boolean energy) {
       if ((ez7CommandFactory.getEz7().getProtocolChannelValue(channel) != -1) && (isElectricEnergyType(channel))) {
           return doGetUnit(channel, energy);
       }
       else {
           return Unit.get("");
       }
    }

    private Unit doGetUnit(int channel, boolean energy) {
       if ((channel%2) == 0) {
           return energy ? Unit.get("kW").getVolumeUnit() : Unit.get("kW");
       }
       else {
           return energy ? Unit.get("kvar").getVolumeUnit() : Unit.get("kvar");
       }
    }

    public int getVoltageMeterId(int channel) {
        return voltageMeterId[channel];
    }

    public int getCurrentMeterId(int channel) {
        return currentMeterId[channel];
    }

    public int getCtSets(int channel) {
        return ctSets[channel];
    }

    public int getInpType(int channel) {
        return inpType[channel];
    }

    public int getManufacturer(int channel) {
        return manufacturer[channel];
    }

    public int getEnergyType(int channel) {
        return energyType[channel];
    }

    public int getPulseFactor(int channel) {
        return pulseFactor[channel];
    }

    public int getKWhMultiplies(int channel) {
        return kWhMultiplies[channel];
    }
    public int getPulseRatePerMinute(int channel) {
        return pulseRatePerMinute[channel];
    }

    public int getCTRatio(int channel) {
        return cTRatio[channel];
    }
    public int getPTRatio(int channel) {
        return pTRatio[channel];
    }
}
