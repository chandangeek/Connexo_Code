/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.inbound.general.frames;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.mdc.inbound.general.frames.parsing.InboundParameters;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInboundFrame {

    private final IssueService issueService;
    private final IdentificationService identificationService;

    public enum FrameType {
        REQUEST,
        EVENT,
        EVENTP0,
        REGISTER,
        DEPLOY
    }

    private String frame;
    private BaseDevice device = null;
    private InboundParameters inboundParameters = null;
    private List<CollectedData> collectedDatas;
    private String[] parameters = new String[0];
    private DeviceIdentifier deviceIdentifier = null;

    protected abstract FrameType getType();

    public AbstractInboundFrame(String frame, IssueService issueService, IdentificationService identificationService) {
        this.frame = frame;
        this.issueService = issueService;
        this.identificationService = identificationService;
        parse();
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    public List<CollectedData> getCollectedDatas() {
        if (collectedDatas == null) {
            collectedDatas = new ArrayList<>();
        }
        return collectedDatas;
    }

    public String getFrame() {
        return frame;
    }

    public BaseDevice getDevice() {
        return device;
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
        this.deviceIdentifier = this.identificationService.createDeviceIdentifierByCallHomeId(inboundParameters.getSerialNumber());
        if (findDevice()) {
            doParse();
        }
    }

    /**
     * Find the unique device, based on its serialNumber.
     *
     * @return true if an unique device has been found
     *         false if no unique device could be found
     */
    private boolean findDevice() {
        device = getDeviceIdentifier().findDevice();
        return device != null;
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

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public IdentificationService getIdentificationService() {
        return identificationService;
    }
}