package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.shadow.CommunicationProfileShadow;

import java.util.List;

/**
 * Straightforward implementation of the <CODE>CommunicationSchedulerFullProtocolShadow</CODE> interface
 */
public class CommunicationSchedulerFullProtocolShadowImpl implements CommunicationSchedulerFullProtocolShadow{

    private RtuFullProtocolShadow rtuFullShadow;
    private CommunicationProfileShadow communicationProfileShadow;
    private List<RtuRegisterFullProtocolShadow> rtuRegisterFullShadowList;
    private List<RtuMessage> rtuMessageList;
    private List<CommunicationSchedulerFullProtocolShadow> slaveCommunicationFullShadowList;

    public CommunicationSchedulerFullProtocolShadowImpl() {
    }

    /**
     * @return the used <CODE>RtuFullProtocolShadow</CODE>
     */
    public RtuFullProtocolShadow getRtuShadow() {
        return this.rtuFullShadow;
    }

    public void setRtuFullProtocolShadow(final RtuFullProtocolShadow rtuShadow) {
        this.rtuFullShadow = rtuShadow;
    }

    /**
     * @return the used <CODE>CommunicationProfileShadow</CODE>
     */
    public CommunicationProfileShadow getCommunicationProfileShadow() {
        return this.communicationProfileShadow;
    }

    public void setCommunicationProfileShadow(final CommunicationProfileShadow communicationProfileShadow) {
        this.communicationProfileShadow = communicationProfileShadow;
    }

    /**
     * @return the used <CODE>RtuRegisterFullProtocolShadow</CODE>
     */
    public List<RtuRegisterFullProtocolShadow> getRtuRegisterFullProtocolShadowList() {
        return this.rtuRegisterFullShadowList;
    }

    public void setRtuRegisterFullProtocolShadow(final List<RtuRegisterFullProtocolShadow> rtuRegisterShadowList) {
        this.rtuRegisterFullShadowList = rtuRegisterShadowList;
    }

    /**
     * @return the list of <CODE>RtuMessageShadows</CODE> of this device
     */
    public List<RtuMessage> getRtuMessageList() {
        return this.rtuMessageList;
    }

    public void setRtuMessageList(final List<RtuMessage> rtuMessageList) {
        this.rtuMessageList = rtuMessageList;
    }

    /**
     * @return the list of <CODE>CommunicationSchedulerFullProtocolShadows</CODE> of all slaveDevices
     */
    public List<CommunicationSchedulerFullProtocolShadow> getSlaveCommunicationSchedulerFullProtocolShadowsList() {
        return this.slaveCommunicationFullShadowList;
    }

    public void setSlaveCommunicationSchedulerFullProtocolShadowsList(final List<CommunicationSchedulerFullProtocolShadow> slaveCommunicationSchedulerShadows) {
        this.slaveCommunicationFullShadowList = slaveCommunicationSchedulerShadows;
    }
}
