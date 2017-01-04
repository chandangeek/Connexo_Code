package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ServletBasedInboundDeviceProtocol;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.ServletBasedInboundDeviceProtocol} interface
 * that will support inbound SMS communication for the CTR protocols (MTU155 and EK155).
 *
 * @author sva
 * @since 24/06/13 - 13:53
 */
public abstract class AbstractSMSServletBasedInboundDeviceProtocol implements ServletBasedInboundDeviceProtocol {

    public static final String SOURCE_PROPERTY_NAME = "API_source";
    public static final String AUTHENTICATION_PROPERTY_NAME = "API_authentication";

    private final PropertySpecService propertySpecService;
    protected TypedProperties properties;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected InboundDiscoveryContext context;

    protected AbstractSMSServletBasedInboundDeviceProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public void init(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public String getAdditionalInformation() {
        return ""; //No additional info available
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return this.context;
    }

    @Override
    public void setProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        this.properties = TypedProperties.copyOf(properties);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                    sourcePropertySpec(),
                    authenticationPropertySpec());
    }

    private PropertySpec sourcePropertySpec() {
        return this.stringSpec(SOURCE_PROPERTY_NAME);
    }

    protected String sourcePropertyValue() {
        return (String) this.getProperty(SOURCE_PROPERTY_NAME);
    }

    private PropertySpec authenticationPropertySpec() {
        return this.stringSpec(AUTHENTICATION_PROPERTY_NAME);
    }

    protected String authenticationPropertyValue() {
        return (String) this.getProperty(AUTHENTICATION_PROPERTY_NAME);
    }

    protected Object getProperty(String propertyName) {
        return this.properties.getProperty(propertyName);
    }

    protected PropertySpec stringSpec(String name) {
        return UPLPropertySpecFactory
                    .specBuilder(name, true, this.propertySpecService::stringSpec)
                    .finish();
    }
    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }
}


