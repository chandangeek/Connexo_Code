package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.general.AbstractDiscover;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.comchannels.ComChannelInputStreamAdapter;
import com.energyict.protocolimplv2.comchannels.ComChannelOutputStreamAdapter;
import com.energyict.protocolimplv2.elster.ctr.MTU155.GprsRequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;

import java.util.TimeZone;

/**
 * Inbound device discovery created for the CTR protocol (as used by MTU155 and EK155 DeviceProtocols)
 * In this case, a meter opens an inbound connection to the comserver but it doesn't send any frames.
 * We should send an unencrypted request for identification to know which RTU and schedule has to be executed.
 * Extra requests are sent in the normal protocol session (e.g. fetch meter data).
 * <p/>
 *
 * @author: sva
 * @since: 26/10/12 (11:40)
 */
public class CtrInboundDeviceProtocol extends AbstractDiscover {

    DeviceIdentifier deviceIdentifier;
    private RequestFactory requestFactory;

    @Override
    public DiscoverResultType doDiscovery() {
        try {
            IdentificationResponseStructure identStruct = getRequestFactory().getIdentificationStructure();
            CTRAbstractValue<String> pdrObject = identStruct != null ? identStruct.getPdr() : null;
            String pdr = pdrObject != null ? pdrObject.getValue() : null;
            if (pdr == null) {
                throw new CTRException("Unable to detect meter. PDR value was 'null'!");
            }
            setCallHomeID(pdr);
        } catch (CTRException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }

        return DiscoverResultType.IDENTIFIER;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (!responseType.equals(DiscoverResponseType.SUCCESS)) {
            logOff();
        }
    }

    private void logOff() {
        getRequestFactory().sendEndOfSession();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    protected void setCallHomeID(String callHomeID) {
        this.deviceIdentifier = new DialHomeIdDeviceIdentifier(callHomeID);
    }

    private RequestFactory getRequestFactory() {
        if (requestFactory == null) {
            requestFactory = new GprsRequestFactory(new ComChannelInputStreamAdapter(getComChannel()),
                    new ComChannelOutputStreamAdapter(getComChannel()),
                    getContext().getLogger(),
                    new MTU155Properties(getTypedProperties()),
                    TimeZone.getDefault());
        }
        return requestFactory;
    }

    protected void setRequestFactory(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}