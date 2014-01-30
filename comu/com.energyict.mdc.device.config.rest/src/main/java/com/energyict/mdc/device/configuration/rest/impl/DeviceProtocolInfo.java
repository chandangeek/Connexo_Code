package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class DeviceProtocolInfo {
    @JsonProperty("name")
    private String name;
    @JsonProperty("serviceCategory")
    @XmlJavaTypeAdapter(ServiceKindAdapter.class)
    private ServiceKind serviceKind;
    @JsonProperty("deviceFunction")
    @XmlJavaTypeAdapter(DeviceFunctionAdapter.class)
    private DeviceFunction deviceFunction;

    public DeviceProtocolInfo(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.name = deviceProtocolPluggableClass.getName();
        this.deviceFunction = deviceProtocolPluggableClass.getDeviceProtocol().getDeviceFunction();
        this.serviceKind = deviceFunction.getServiceKind();
    }
}
