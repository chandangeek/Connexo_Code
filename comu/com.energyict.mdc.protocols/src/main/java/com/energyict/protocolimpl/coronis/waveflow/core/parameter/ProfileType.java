package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class ProfileType extends AbstractParameter {

    public ProfileType(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.ProfileType;
    }

    public static final int TYPE_4INPUTS = 0x00;
    public static final int REEDINPUT_AND_SIMPLE_BACKFLOW = 0x08;
    public static final int REEDINPUT_AND_ADVANCED_BACKFLOW = 0x09;
    public static final int CYBLEINPUT_AND_ADVANCED_BACKFLOW = 0x0D;
    public static final int SINGELEREEDINPUT_AND_VALVE_CONTROL = 0x02;
    public static final int REEDINPUT_AND_ADVANCED_BACKFLOW_AND_VALVE_CONTROL = 0x0B;

    public static final String DESCRIPTION_TYPE_4INPUTS = "4 input profile in Waveflow";
    public static final String DESCRIPTION_REEDINPUT_AND_SIMPLE_BACKFLOW = "2 double reed input + backflow detection by monthly flags";
    public static final String DESCRIPTION_REEDINPUT_AND_ADVANCED_BACKFLOW = "2 double reed input + backflow detection by event storage";
    public static final String DESCRIPTION_CYBLEINPUT_AND_ADVANCED_BACKFLOW = "2 CYBLE heads input backflow detection by event storage";
    public static final String DESCRIPTION_SINGELEREEDINPUT_AND_VALVE_CONTROL = "1 simple reed input + management of a valve";
    public static final String DESCRIPTION_REEDINPUT_AND_ADVANCED_BACKFLOW_AND_VALVE_CONTROL = "1 double reed input + backflow detection by event storage + management of a valve";

    private int type;

    public int getType() {
        return type;
    }

    public String getDescription() {
        switch (getType()) {
            case TYPE_4INPUTS:
                return DESCRIPTION_TYPE_4INPUTS;
            case REEDINPUT_AND_SIMPLE_BACKFLOW:
                return DESCRIPTION_REEDINPUT_AND_SIMPLE_BACKFLOW;
            case REEDINPUT_AND_ADVANCED_BACKFLOW:
                return DESCRIPTION_REEDINPUT_AND_ADVANCED_BACKFLOW;
            case CYBLEINPUT_AND_ADVANCED_BACKFLOW:
                return DESCRIPTION_CYBLEINPUT_AND_ADVANCED_BACKFLOW;
            case SINGELEREEDINPUT_AND_VALVE_CONTROL:
                return DESCRIPTION_SINGELEREEDINPUT_AND_VALVE_CONTROL;
            case REEDINPUT_AND_ADVANCED_BACKFLOW_AND_VALVE_CONTROL:
                return DESCRIPTION_REEDINPUT_AND_ADVANCED_BACKFLOW_AND_VALVE_CONTROL;
            default:
                return "";
        }
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        type = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) type};
    }

    public boolean supportsSimpleBackflowDetection() {
        return (getType() == ProfileType.REEDINPUT_AND_SIMPLE_BACKFLOW);
    }

    public boolean supportsAdvancedBackflowDetection() {
        return (getType() == ProfileType.REEDINPUT_AND_ADVANCED_BACKFLOW) || (getType() == ProfileType.CYBLEINPUT_AND_ADVANCED_BACKFLOW) || (getType() == ProfileType.REEDINPUT_AND_ADVANCED_BACKFLOW_AND_VALVE_CONTROL);
    }

    public boolean supportsReedFaultDetection() {
        return (getType() == REEDINPUT_AND_SIMPLE_BACKFLOW) || (getType() == REEDINPUT_AND_ADVANCED_BACKFLOW) || (getType() == REEDINPUT_AND_ADVANCED_BACKFLOW_AND_VALVE_CONTROL) || (getType() == SINGELEREEDINPUT_AND_VALVE_CONTROL);
    }

    public boolean isOfType4Iputs() {
        return (getType() == ProfileType.TYPE_4INPUTS);
    }

    public boolean supportsWaterValveControl() {
        return (getType() == SINGELEREEDINPUT_AND_VALVE_CONTROL) || (getType() == REEDINPUT_AND_ADVANCED_BACKFLOW_AND_VALVE_CONTROL);
    }
}
