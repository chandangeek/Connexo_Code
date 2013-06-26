package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.inbound.ServletBasedInboundDeviceProtocol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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

    protected TypedProperties properties;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected InboundDiscoveryContext context;

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
    public String getVersion() {
        return "$Date: 2013-05-28 12:16:55 +0200 (di, 28 mei 2013) $";
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return this.context;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Arrays.asList(
                sourcePropertySpec(),
                authenticationPropertySpec());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }

    private PropertySpec sourcePropertySpec() {
        return PropertySpecFactory.stringPropertySpec(SOURCE_PROPERTY_NAME);
    }

    protected String sourcePropertyValue() {
        return (String) this.getProperty(SOURCE_PROPERTY_NAME);
    }

    private PropertySpec authenticationPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(AUTHENTICATION_PROPERTY_NAME);
    }

    protected String authenticationPropertyValue() {
        return (String) this.getProperty(AUTHENTICATION_PROPERTY_NAME);
    }

    protected Object getProperty(String propertyName) {
        return this.properties.getProperty(propertyName);
    }
}


