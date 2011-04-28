package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.shadow.CommunicationProfileShadow;
import com.energyict.mdw.shadow.RtuMessageShadow;

import java.util.List;

/**
 * Contains logical information from a {@link com.energyict.mdw.core.CommunicationScheduler}
 */
public interface CommunicationSchedulerFullProtocolShadow {

    /**
     * @return the used <CODE>RtuFullProtocolShadow</CODE>
     */
    RtuFullProtocolShadow getRtuShadow();

    void setRtuFullProtocolShadow(RtuFullProtocolShadow rtuShadow);

    /**
     * @return the used <CODE>CommunicationProfileShadow</CODE>
     */
    CommunicationProfileShadow getCommunicationProfileShadow();

    void setCommunicationProfileShadow(CommunicationProfileShadow communicationProfileShadow);

    /**
     * @return the used <CODE>RtuRegisterFullProtocolShadow</CODE>
     */
    List<RtuRegisterFullProtocolShadow> getRtuRegisterFullProtocolShadowList();

    void setRtuRegisterFullProtocolShadow(List<RtuRegisterFullProtocolShadow> rtuRegisterShadowList);

    /**
     * @return the list of <CODE>RtuMessage</CODE> of this device
     */
    List<RtuMessage> getRtuMessageList();

    void setRtuMessageList(List<RtuMessage> rtuMessageList);

    /**
     * @return the list of <CODE>CommunicationSchedulerFullProtocolShadows</CODE> of all slaveDevices
     */
    List<CommunicationSchedulerFullProtocolShadow> getSlaveCommunicationSchedulerFullProtocolShadowsList();

    void setSlaveCommunicationSchedulerFullProtocolShadowsList(List<CommunicationSchedulerFullProtocolShadow> slaveCommunicationSchedulerShadows);
}
