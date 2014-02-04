package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.Model;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class DeviceProtocolInfo {

    @JsonProperty("communicationProtocolName")
    public String name;
    @JsonProperty("serviceCategory")
    @XmlJavaTypeAdapter(ServiceKindAdapter.class)
    public ServiceKind serviceKind;
    @JsonProperty("deviceFunction")
    @XmlJavaTypeAdapter(DeviceFunctionAdapter.class)
    public DeviceFunction deviceFunction;

    public DeviceProtocolInfo(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.name = deviceProtocolPluggableClass.getName();
        this.deviceFunction = deviceProtocolPluggableClass.getDeviceProtocol().getDeviceFunction();
        this.serviceKind = deviceFunction.getServiceKind();
    }
}
