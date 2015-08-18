package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/field")
public class DeviceFieldResource extends FieldResource {
    
    private final DeviceService deviceService;

    @Inject
    public DeviceFieldResource(NlsService nlsService, DeviceService deviceService) {
        super(nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST));
        this.deviceService = deviceService;
    }
    
    @GET
    @Path("/enddevicedomains")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceDomains() {
        return asJsonArrayObjectWithTranslation("domains", "domain", new EndDeviceDomainAdapter().getClientSideValues());
    }
    
    @GET
    @Path("/enddevicesubdomains")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceSubDomains() {
        return asJsonArrayObjectWithTranslation("subDomains", "subDomain", new EndDeviceSubDomainAdapter().getClientSideValues());
    }
    
    @GET
    @Path("/enddeviceeventoractions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceEventOrActions() {
        return asJsonArrayObjectWithTranslation("eventOrActions", "eventOrAction", new EndDeviceEventOrActionAdapter().getClientSideValues());
    }

    @GET
    @Path("/loglevels")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Object getLogLevels() {
        return asJsonArrayObjectWithTranslation("logLevels", "logLevel", new LogLevelAdapter().getClientSideValues());
    }
    
    @GET
    @Path("/gateways")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public PagedInfoList getGateways(@QueryParam("search") String search, @QueryParam("excludeDeviceMRID") String excludeDeviceMRID, @BeanParam JsonQueryParameters queryParameters) {
        Condition condition = Condition.TRUE;
        if (!Checks.is(search).emptyOrOnlyWhiteSpace()) {
            condition = condition.and(Where.where("mRID").likeIgnoreCase('*' + search + '*'));
        }
        if (!Checks.is(excludeDeviceMRID).emptyOrOnlyWhiteSpace()) {
            condition = condition.and(Where.where("mRID").isNotEqual(excludeDeviceMRID));
        }
        condition = condition.and(Where.where("deviceConfiguration.gatewayType").isNotEqual(GatewayType.NONE));
        List<Device> devices = deviceService.findAllDevices(condition).from(queryParameters).sorted("mRID", true).find();
        List<IdWithNameInfo> infos = devices.stream().map(d -> new IdWithNameInfo(d.getId(), d.getmRID())).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("gateways", infos, queryParameters);
    }
}
