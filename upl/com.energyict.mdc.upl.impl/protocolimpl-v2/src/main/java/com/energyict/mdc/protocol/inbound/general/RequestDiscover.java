package com.energyict.mdc.protocol.inbound.general;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.general.frames.AbstractInboundFrame;
import com.energyict.protocolimplv2.MdcManager;

/**
 * In the case of RequestDiscover, a meter starts an inbound session and pushes its serial number and meter data.
 * There are no extra requests sent by the comserver.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/06/12
 * Time: 14:50
 */
public class RequestDiscover extends AbstractDiscover {

    @Override
    public DiscoverResultType doDiscovery() {
        ComChannel comChannel = this.getComChannel();
        this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty()));
        boolean receivedValidFrame = false;
        boolean notTimedOut = true;
        while (notTimedOut) {
            try {
                AbstractInboundFrame fullFrame = getInboundConnection().readAndAckInboundFrame();
                receivedValidFrame = true;
                this.setSerialNumber(fullFrame.getSerialNumber());
                addCollectedData(fullFrame);

                if (fullFrame.isRequest() && (getCollectedData() == null || getCollectedData().isEmpty())) { // In case first frame is a 'request', then handle as identifier
                    return DiscoverResultType.IDENTIFIER;                                                    // instead of waiting for timeout
                }
            } catch (InboundTimeOutException e) {
                if (!receivedValidFrame) { // Timeout during receive of the first frame
                    throw MdcManager.getComServerExceptionFactory().createInboundTimeOutException(e.getMessage());
                }
                notTimedOut = false;    // Timeout during receive of additional frames
            }
        }
        return DiscoverResultType.DATA;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (responseType == DiscoverResponseType.SUCCESS || responseType == DiscoverResponseType.DATA_ONLY_PARTIALLY_HANDLED) {
            getInboundConnection().ackFrames();
        }
    }
}