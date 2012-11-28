package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdw.core.DeviceMessage;
import com.energyict.mdw.shadow.CommunicationProfileShadow;

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
     * @return the list of <CODE>DeviceMessage</CODE> of this device
     */
    List<DeviceMessage> getRtuMessageList();

    void setRtuMessageList(List<DeviceMessage> rtuMessageList);

    /**
     * @return the list of <CODE>CommunicationSchedulerFullProtocolShadows</CODE> of all slaveDevices
     */
    List<CommunicationSchedulerFullProtocolShadow> getSlaveCommunicationSchedulerFullProtocolShadowsList();

    void setSlaveCommunicationSchedulerFullProtocolShadowsList(List<CommunicationSchedulerFullProtocolShadow> slaveCommunicationSchedulerShadows);
}
