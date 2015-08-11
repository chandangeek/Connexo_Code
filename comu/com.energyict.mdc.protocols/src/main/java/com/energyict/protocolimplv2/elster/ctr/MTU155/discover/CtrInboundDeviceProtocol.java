package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocolimplv2.elster.ctr.MTU155.GprsRequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;
import com.energyict.protocols.impl.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.protocols.mdc.inbound.general.AbstractDiscover;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.inject.Inject;
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
 * @author: sva
 * @since: 26/10/12 (11:40)
 */
public class CtrInboundDeviceProtocol extends AbstractDiscover {

    private static final String DEVICE_TYPE_KEY ="Device type";
    private static final String MTU155_DEVICE_TYPE = "MTU155";
    private static final String EK155_DEVICE_TYPE = "EK155";

    DeviceIdentifier deviceIdentifier;
    private RequestFactory requestFactory;

    @Inject
    public CtrInboundDeviceProtocol(PropertySpecService propertySpecService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, IdentificationService identificationService) {
        super(propertySpecService, issueService, readingTypeUtilService, thesaurus, identificationService);
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
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }

        return DiscoverResultType.IDENTIFIER;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (!(responseType.equals(DiscoverResponseType.SUCCESS) || responseType.equals(DiscoverResponseType.DATA_ONLY_PARTIALLY_HANDLED))) {
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
        this.deviceIdentifier = getIdentificationService().createDeviceIdentifierByConnectionTaskProperty(CTRInboundDialHomeIdConnectionType.class, DeviceProtocolProperty.callHomeId.name(), callHomeID);
    }

    private RequestFactory getRequestFactory() {
        if (requestFactory == null) {
            requestFactory = new GprsRequestFactory(
                    getComChannel(),
                    getContext().getLogger(),
                    new MTU155Properties(getTypedProperties(), getPropertySpecService()),
                    TimeZone.getDefault(),  //Timezone not known - using the default one
                    isEK155Device(), getPropertySpecService());
        }
        return requestFactory;
    }

    protected void setRequestFactory(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(getDeviceTypePropertySpec());
        return propertySpecs;
    }

    private PropertySpec getDeviceTypePropertySpec() {
        return getPropertySpecService().stringPropertySpecWithValuesAndDefaultValue(DEVICE_TYPE_KEY, true, MTU155_DEVICE_TYPE, MTU155_DEVICE_TYPE, EK155_DEVICE_TYPE);
    }

    public String getDeviceTypeProperty() {
        return getTypedProperties().getStringProperty(DEVICE_TYPE_KEY);
    }

    private boolean isEK155Device() {
        return getDeviceTypeProperty().equals(EK155_DEVICE_TYPE);
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-11-08 13:54:32 +0100 (Fri, 08 Nov 2013) $";
    }
}
