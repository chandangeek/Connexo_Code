package com.energyict.mdc.protocol.inbound.general;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.general.frames.AbstractInboundFrame;

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
        boolean notTimedOut = true;
        while (notTimedOut) {
            try {
                AbstractInboundFrame fullFrame = getInboundConnection().readAndAckInboundFrame();
                this.setSerialNumber(fullFrame.getSerialNumber());
                addCollectedData(fullFrame);

                if (fullFrame.isRequest() && fullFrame.getInboundParameters().getComPort() != null) {
                    //TODO send ESC commands?
                }
            } catch (InboundTimeOutException e) {
                notTimedOut = false;
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
        if (responseType == DiscoverResponseType.SUCCESS) {
            getInboundConnection().ackFrames();
        }
    }
}