package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality to create a fullShadow object which will contain all necessary information for a protocol to execute his tasks
 */
public class CommunicationSchedulerFullProtocolShadowBuilder {

    /**
     * Hidden constructor
     */
    private CommunicationSchedulerFullProtocolShadowBuilder() {
    }

    /**
     * Create a <CODE>CommunicationSchedulerFullProtocolShadow</CODE> from the given <CODE>CommunicationScheduler</CODE>
     *
     * @param scheduler the given <CODE>CommunicationScheduler</CODE>
     * @return the created fullShadowObject
     */
    public static CommunicationSchedulerFullProtocolShadow createCommunicationSchedulerFullProtocolShadow(final CommunicationScheduler scheduler) {
        return createCommunicationSchedulerFullProtocolShadow(scheduler.getRtu(), scheduler);
    }

    /**
     * Create a <CODE>CommunicationSchedulerFullProtocolShadow</CODE> from the given <CODERtu</CODE> and <CODE>CommunicationScheduler</CODE>
     *
     * @param rtu       the given <CODE>Rtu</CODE>
     * @param scheduler the given <CODE>CommunicationScheduler</CODE>
     * @return the created fullShadowObject
     */
    public static CommunicationSchedulerFullProtocolShadow createCommunicationSchedulerFullProtocolShadow(final Rtu rtu, final CommunicationScheduler scheduler) {
        CommunicationSchedulerFullProtocolShadow csfps = new CommunicationSchedulerFullProtocolShadowImpl();
        csfps.setCommunicationProfileShadow(scheduler.getCommunicationProfile().getShadow());
        csfps.setRtuFullProtocolShadow(RtuFullProtocolShadowBuilder.createRtuFullProtocolShadow(rtu));
        csfps.setRtuMessageList(createRtuMessages(rtu));
        csfps.setRtuRegisterFullProtocolShadow(createRtuRegisterShadows(rtu, scheduler.getCommunicationProfile()));
        csfps.setSlaveCommunicationSchedulerFullProtocolShadowsList(createSlaveCommunicationSchedulerFullShadows(rtu, scheduler));
        return csfps;
    }

    /**
     * Create a <CODE>List</CODE> of <CODE>CommunicationSchedulerFullProtocolShadow</CODE> of all slave devices from the given <CODE>Rtu</CODE>
     *
     * @param rtu       the given <CODE>Rtu</CODE>
     * @param scheduler the <CODE>CommunicationScheduler</CODE> of the Master <CODE>Rtu</CODE>
     * @return the freshly created <CODE>List</CODE>
     */
    private static List<CommunicationSchedulerFullProtocolShadow> createSlaveCommunicationSchedulerFullShadows(final Rtu rtu, final CommunicationScheduler scheduler) {
        List<CommunicationSchedulerFullProtocolShadow> slaveFullShadowList = new ArrayList<CommunicationSchedulerFullProtocolShadow>();
        for (Rtu slave : rtu.getDownstreamRtus()) {
            slaveFullShadowList.add(createCommunicationSchedulerFullProtocolShadow(slave, scheduler));
        }
        return slaveFullShadowList;
    }

    /**
     * Create a <CODE>List</CODE> of <CODE>RtuRegisterFullProtocolShadow</CODE>. Use the given <CODE>CommunicationProfile</CODE> to determine whether the register
     * is is the readingGroup of this <CODE>CommunicationProfile</CODE>, if not then don't add it to the list.
     *
     * @param rtu                  the <CODE>Rtu</CODE> to gather the registers from
     * @param communicationProfile the used <CODE>CommunicationProfile</CODE>
     * @return the newly created List
     */
    private static List<RtuRegisterFullProtocolShadow> createRtuRegisterShadows(final Rtu rtu, final CommunicationProfile communicationProfile) {
        List<RtuRegisterFullProtocolShadow> rtuRegisterShadowList = new ArrayList<RtuRegisterFullProtocolShadow>();

        for (RtuRegister register : rtu.getRegisters()) {
            if ((register.getGroup() == null && communicationProfile.getRtuRegisterGroups().isEmpty()) || communicationProfile.getRtuRegisterGroups().contains(register.getGroup())) {
                rtuRegisterShadowList.add(RtuRegisterFullProtocolShadowBuilder.createRtuRegisterFullProtocolShadow(register));
            }
        }

        return rtuRegisterShadowList;
    }

    /**
     * Creates a <CODE>List</CODE> of <b>Pending AND Sent</b> <CODE>RtuMessage</CODE>
     *
     * @param rtu the <CODE>Rtu</CODE> which holds the messages
     * @return the generated list
     */
    private static List<RtuMessage> createRtuMessages(final Rtu rtu) {
        List<RtuMessage> messageShadowList = new ArrayList<RtuMessage>();
        for (RtuMessage rtuMessage : rtu.getPendingMessages()) {
            messageShadowList.add(rtuMessage);
        }
        for (RtuMessage rtuMessage : rtu.getSentMessages()) {
            messageShadowList.add(rtuMessage);
        }
        return messageShadowList;
    }

}
