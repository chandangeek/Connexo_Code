package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkScheduleResource {
    private static final String LOAD_ALL_DEVICES = "all";

    private final DeviceDataService deviceDataService;
    private final ResourceHelper resourceHelper;
    private final SchedulingService schedulingService;
    private final Thesaurus thesaurus;

    @Inject
    public BulkScheduleResource(DeviceDataService deviceDataService, ResourceHelper resourceHelper, SchedulingService schedulingService, Thesaurus thesaurus) {
        this.deviceDataService = deviceDataService;
        this.resourceHelper = resourceHelper;
        this.schedulingService = schedulingService;
        this.thesaurus = thesaurus;
        DeviceHolder.deviceDataService = deviceDataService;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response addComScheduleToDeviceSet(BulkRequestInfo request, @BeanParam QueryParameters queryParameters){
        BulkAction action = new BulkAction() {
            @Override
            public void doAction(Device device, ComSchedule schedule) {
                device.newScheduledComTaskExecution(schedule).add();
            }
        };
        ComSchedulesBulkInfo response = processBulkAction(request, action, queryParameters);
        return Response.ok(response.build()).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComScheduleFromDeviceSet(BulkRequestInfo request, @BeanParam QueryParameters queryParameters){
        BulkAction action = new BulkAction() {
            @Override
            public void doAction(Device device, ComSchedule schedule) {
                device.removeComSchedule(schedule);
            }
        };
        ComSchedulesBulkInfo response = processBulkAction(request, action, queryParameters);
        return Response.ok(response.build()).build();
    }

    private ComSchedulesBulkInfo processBulkAction(BulkRequestInfo request, BulkAction action, QueryParameters queryParameters) {
        ComSchedulesBulkInfo response = new ComSchedulesBulkInfo();
        Map<String, DeviceHolder> deviceMap = getDeviceMapForBulkAction(request, response, queryParameters);
        for (Long scheduleId : request.scheduleIds) {
            Optional<ComSchedule> scheduleRef = schedulingService.findSchedule(scheduleId);
            if (!scheduleRef.isPresent()){
                String failMessage = MessageSeeds.NO_SUCH_COM_SCHEDULE.format(thesaurus, scheduleId);
                response.nextAction(failMessage).failCount = deviceMap.size();
            } else {
                processScheduleForBulkAction(deviceMap, scheduleRef.get(), action, response);
            }
        }
        return response;
    }

    private void processScheduleForBulkAction(Map<String, DeviceHolder> deviceMap, ComSchedule schedule, BulkAction action, ComSchedulesBulkInfo response) {
        response.nextAction(schedule.getName());
        for (DeviceHolder device : deviceMap.values()) {
            processSchedule(device, schedule, action, response);
        }
    }

    private void processSchedule (DeviceHolder holder, ComSchedule schedule, BulkAction action, ComSchedulesBulkInfo response) {
        Device device = holder.get();
        try{
            action.doAction(device, schedule);
            device.save();
            response.success();
        } catch (LocalizedException localizedEx){
            response.fail(DeviceInfo.from(device), localizedEx.getLocalizedMessage(), localizedEx.getClass().getSimpleName());
        } catch (ConstraintViolationException validationException){
            response.fail(DeviceInfo.from(device),getMessageForConstraintViolation(validationException, device, schedule),
                    validationException.getClass().getSimpleName());
            holder.obsolete();
        }
    }

    private Map<String, DeviceHolder> getDeviceMapForBulkAction(BulkRequestInfo request, ComSchedulesBulkInfo response, QueryParameters queryParameters) {
        Map<String, DeviceHolder> deviceMap = new HashMap<>();
        if(queryParameters.getBoolean(LOAD_ALL_DEVICES)) {

            List<Device> devices = deviceDataService.findAllDevices();
            for(Device device : devices) {
                deviceMap.put(device.getmRID(), new DeviceHolder(device));
            }
        } else {
            for (String mrid : request.deviceMRIDs) {
                try {
                    deviceMap.put(mrid, new DeviceHolder(resourceHelper.findDeviceByMrIdOrThrowException(mrid)));
                } catch (LocalizedException ex){
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.mRID = mrid;
                    response.generalFail(deviceInfo, ex.getLocalizedMessage(), ex.getClass().getSimpleName());
                }
            }
        }
        return deviceMap;
    }

    private String getMessageForConstraintViolation(ConstraintViolationException ex, Device device, ComSchedule schedule) {
        if (ex.getConstraintViolations() != null && ex.getConstraintViolations().size() > 0){
            return ex.getConstraintViolations().iterator().next().getMessage();
        }
        return MessageSeeds.DEVICE_VALIDATION_BULK_MSG.format(thesaurus, schedule.getName(), device.getName());
    }

    private static interface BulkAction {
        public void doAction(Device device, ComSchedule schedule);
    }

    private static final class DeviceHolder {
        private static DeviceDataService deviceDataService;

        private Device device;
        private boolean obsolete;


        private DeviceHolder(Device device) {
            this.device = device;
        }

        public Device get(){
            // We need to reload device because comTaskExecution collection
            // could contain bad schedule from previous iteration
            if (obsolete){
                reload();
            }
            return device;
        }

        private void reload(){
            device = deviceDataService.findByUniqueMrid(device.getmRID());
            obsolete = false;
        }

        public void obsolete(){
            obsolete = true;
        }
    }
}
