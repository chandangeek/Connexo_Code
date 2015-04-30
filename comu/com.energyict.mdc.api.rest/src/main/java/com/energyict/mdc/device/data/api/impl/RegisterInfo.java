package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Register;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegisterInfo implements UriProvider {
    public long id;

    public RegisterInfo() {
    }

    public static RegisterInfo from(Register register) {
        RegisterInfo registerInfo = new RegisterInfo();
        registerInfo.id = register.getRegisterSpecId();
        return registerInfo;
    }

    public static List<RegisterInfo> from(List<Register> registers) {
        return registers.stream().map(RegisterInfo::from).collect(Collectors.toList());
    }

    public URI uri(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(RegisterResource.class).path("{id}").build(this.id);
    }

}

