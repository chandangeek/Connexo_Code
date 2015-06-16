package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.protocol.inbound.general.frames.parsing.InboundParameters;
import com.energyict.protocolimplv2.identifiers.CallHomeIdPlaceHolder;
import com.energyict.protocolimplv2.identifiers.DialHomeIdPlaceHolderDeviceIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract super class for all inbound frames.
 * Parsing depends on the type of the frame, but has a common part for received parameters
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/06/12
 * Time: 17:07
 * Author: khe
 */
public abstract class AbstractInboundFrame {

    protected final CallHomeIdPlaceHolder callHomeIdPlaceHolder;

    public enum FrameType {
        REQUEST,
        EVENT,
        EVENTP0,
        REGISTER,
        DEPLOY
    }

    private String frame;
    private InboundParameters inboundParameters = null;
    private List<CollectedData> collectedDatas;
    private String[] parameters = new String[0];
    private DialHomeIdPlaceHolderDeviceIdentifier deviceIdentifierByDialHomeIdPlaceHolder;

    protected abstract FrameType getType();

    public AbstractInboundFrame(String frame, CallHomeIdPlaceHolder callHomeIdPlaceHolder) {
        this.callHomeIdPlaceHolder = callHomeIdPlaceHolder;
        this.frame = frame;
        parse();
    }

    public List<CollectedData> getCollectedDatas() {
        if (collectedDatas == null) {
            collectedDatas = new ArrayList<CollectedData>();
        }
        return collectedDatas;
    }

    public String getFrame() {
        return frame;
    }

    public String[] getParameters() {
        return parameters;
    }

    public String getSerialNumber() {
        return getInboundParameters().getSerialNumber();
    }

    public InboundParameters getInboundParameters() {
        if (inboundParameters == null) {
            inboundParameters = new InboundParameters();
        }
        return inboundParameters;
    }

    private void parse() {
        buildParameterList();
        inboundParameters = new InboundParameters(parameters);  //All frames contain some parameters, parse them here
        inboundParameters.parse();
        this.callHomeIdPlaceHolder.setSerialNumber(getInboundParameters().getSerialNumber());
        doParse();
    }

    public abstract void doParse();   //Parsing of meter data is specific for every sub class

    public boolean isRequest() {
        return getType() == FrameType.REQUEST;
    }

    public boolean isEvent() {
        return getType() == FrameType.EVENT;
    }

    public boolean isEventP0() {
        return getType() == FrameType.EVENTP0;
    }

    public boolean isDeploy() {
        return getType() == FrameType.DEPLOY;
    }

    public boolean isRegister() {
        return getType() == FrameType.REGISTER;
    }

    protected void buildParameterList() {            //Build all parameters and values, they are comma separated
        String[] fullParameters = new String[0];
        int beginIndex = frame.indexOf('>');
        int endIndex = frame.lastIndexOf('<');
        if (endIndex > beginIndex) {
            String content = frame.substring(beginIndex + 1, endIndex);
            fullParameters = content.split(",");
        }

        parameters = new String[fullParameters.length];
        int count = 0;
        for (String parameter : fullParameters) {
            parameters[count++] = parameter.trim();
        }
    }

    DialHomeIdPlaceHolderDeviceIdentifier getDeviceIdentifierByDialHomeIdPlaceHolder() {
        if (this.deviceIdentifierByDialHomeIdPlaceHolder == null) {
            this.deviceIdentifierByDialHomeIdPlaceHolder = new DialHomeIdPlaceHolderDeviceIdentifier(callHomeIdPlaceHolder);
        }
        return this.deviceIdentifierByDialHomeIdPlaceHolder;
    }
}