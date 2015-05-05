package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Register;
import java.net.URI;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 4/30/15.
 */
public class RegisterInfoFactory {

    public RegisterInfo plain(Register register) {
        RegisterInfo registerInfo = new RegisterInfo();
        registerInfo.id = register.getRegisterSpecId();
        return registerInfo;
    }

    public RegisterInfo asHypermedia(Register register, UriInfo uriInfo) {
        RegisterInfo registerInfo = plain(register);
        registerInfo.self = Link.fromUri(getURI(register, uriInfo)).rel("self").build();
        return registerInfo;
    }

    public HalInfo asHal(Register register, UriInfo uriInfo) {
        RegisterInfo registerInfo = plain(register);
        URI uri = getURI(register, uriInfo);
        return HalInfo.wrap(registerInfo, uri);
    }

    private URI getURI(Register register, UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(RegisterResource.class, "getRegister").build(register.getDevice().getmRID(), register.getRegisterSpecId());
    }
}
