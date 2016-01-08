package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.inbound.ServletBasedInboundDeviceProtocol;

import com.elster.jupiter.properties.PropertySpec;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the ServletBasedInboundDeviceProtocol interface
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
    private final PropertySpecService propertySpecService;

    // Subclasses should add @Inject
    protected AbstractSMSServletBasedInboundDeviceProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.properties.setAllProperties(properties);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(sourcePropertySpec(), authenticationPropertySpec());
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

    private PropertySpec sourcePropertySpec() {
        return this.propertySpecService.stringPropertySpec(SOURCE_PROPERTY_NAME, true, "");
    }

    protected String sourcePropertyValue() {
        return (String) this.getProperty(SOURCE_PROPERTY_NAME);
    }

    private PropertySpec authenticationPropertySpec() {
        return this.propertySpecService.stringPropertySpec(AUTHENTICATION_PROPERTY_NAME, true, "");
    }

    protected String authenticationPropertyValue() {
        return (String) this.getProperty(AUTHENTICATION_PROPERTY_NAME);
    }

    protected Object getProperty(String propertyName) {
        return this.properties.getProperty(propertyName);
    }
}


