package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.protocol.inbound.general.AbstractDiscover;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.GprsRequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Inbound device discovery created for the CTR protocol base (as used by MTU155 and EK155 DeviceProtocols)
 * In this case, a meter opens an inbound connection to the comserver but it doesn't send any frames.
 * We should send an unencrypted request for identification to know which RTU and schedule has to be executed.
 * Extra requests are sent in the normal protocol session (e.g. fetch meter data).
 * <p/>
 *
 * @author sva
 * @since 26/10/12 (11:40)
 */
public class CtrInboundDeviceProtocol extends AbstractDiscover {

    private static final String DEVICE_TYPE_KEY ="DeviceType";
    private static final String MTU155_DEVICE_TYPE = "MTU155";
    private static final String EK155_DEVICE_TYPE = "EK155";

    DeviceIdentifier deviceIdentifier;
    private RequestFactory requestFactory;

    public CtrInboundDeviceProtocol(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

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
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        }

        return DiscoverResultType.IDENTIFIER;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (!responseType.equals(DiscoverResponseType.SUCCESS) || responseType == DiscoverResponseType.DATA_ONLY_PARTIALLY_HANDLED) {
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
        this.deviceIdentifier = new CTRDialHomeIdDeviceIdentifier(callHomeID);
    }

    private RequestFactory getRequestFactory() {
        if (requestFactory == null) {
            requestFactory = new GprsRequestFactory(
                    getComChannel(),
                    getContext().getLogger(),
                    new MTU155Properties(getTypedProperties()),
                    TimeZone.getDefault(),  //Timezone not known - using the default one
                    isEK155Device());
        }
        return requestFactory;
    }

    protected void setRequestFactory(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(
                UPLPropertySpecFactory
                        .specBuilder(DEVICE_TYPE_KEY, true, this.getPropertySpecService()::stringSpec)
                        .setDefaultValue(MTU155_DEVICE_TYPE)
                        .addValues(MTU155_DEVICE_TYPE, EK155_DEVICE_TYPE)
                        .markExhaustive()
                        .finish());
        return propertySpecs;
    }

    public String getDeviceTypeProperty() {
        return getTypedProperties().getStringProperty(DEVICE_TYPE_KEY);
    }

    private boolean isEK155Device() {
        return getDeviceTypeProperty().equals(EK155_DEVICE_TYPE);
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

}