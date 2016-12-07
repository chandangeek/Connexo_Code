package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

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
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public DeviceFieldResource(Thesaurus thesaurus, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(thesaurus);
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceService = deviceService;
    }
    
    @GET @Transactional
    @Path("/enddevicedomains")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceDomains() {
        return asJsonArrayObjectWithTranslation("domains", "domain", new EndDeviceDomainAdapter().getClientSideValues());
    }
    
    @GET @Transactional
    @Path("/enddevicesubdomains")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceSubDomains() {
        return asJsonArrayObjectWithTranslation("subDomains", "subDomain", new EndDeviceSubDomainAdapter().getClientSideValues());
    }
    
    @GET @Transactional
    @Path("/enddeviceeventoractions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceEventOrActions() {
        return asJsonArrayObjectWithTranslation("eventOrActions", "eventOrAction", new EndDeviceEventOrActionAdapter().getClientSideValues());
    }

    @GET @Transactional
    @Path("/loglevels")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Object getLogLevels() {
        return asJsonArrayObjectWithTranslation("logLevels", "logLevel", new LogLevelAdapter().getClientSideValues());
    }
    
    @GET @Transactional
    @Path("/gateways")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public PagedInfoList getGateways(@QueryParam("search") String search, @QueryParam("excludeDeviceName") String excludeDeviceName, @BeanParam JsonQueryParameters queryParameters) {
        Condition condition = Condition.TRUE;
        if (!Checks.is(search).emptyOrOnlyWhiteSpace()) {
            condition = condition.and(Where.where("name").likeIgnoreCase('*' + search + '*'));
        }
        if (!Checks.is(excludeDeviceName).emptyOrOnlyWhiteSpace()) {
            condition = condition.and(Where.where("name").isNotEqual(excludeDeviceName));
        }
        condition = condition.and(Where.where("deviceConfiguration.gatewayType").isNotEqual(GatewayType.NONE));
        List<Device> devices = deviceService.findAllDevices(condition).from(queryParameters).sorted("name", true).find();
        List<IdWithNameInfo> infos = devices.stream().map(d -> new IdWithNameInfo(d.getId(), d.getName())).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("gateways", infos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/calendartypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Object getCalendarTypes() {
        List calendarTypes = deviceMessageSpecificationService
                .findMessageSpecById(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE.dbValue())
                .get()
                .getPropertySpec(DeviceMessageConstants.activityCalendarTypeAttributeName)
                .get()
                .getPossibleValues()
                .getAllValues();

        return asJsonArrayObjectWithTranslation("calendarTypes", "calendarType", calendarTypes);
    }

    @GET
    @Transactional
    @Path("/contracts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Object getContracts() {
        List contracts = deviceMessageSpecificationService
                .findMessageSpecById(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT.dbValue())
                .get()
                .getPropertySpec(DeviceMessageConstants.contractAttributeName)
                .get()
                .getPossibleValues()
                .getAllValues();

        return asJsonArrayObjectWithTranslation("contracts", "contract", contracts);
    }
}
