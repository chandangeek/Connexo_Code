package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterFilter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.data.DeviceService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("/flowprocesses")
public class ProcessResource {

    public final static String USAGE_POINT_VARIABLE_ID = "usagePointId";

    private enum ProcessObjectType {
        DEVICE("device", "deviceId"),
        ALARM("devicealarm", "alarmId"),
        DATA_COLLECTION_ISSUE("datacollectionissue", "issueId"),
        ISSUE_TYPE_NAME("devicelifecycleissue", "issueLifecycleId"),
        TASK_ISSUE("taskissue", "issueTaskId");

        private final String variableId;
        private final String type;

        ProcessObjectType(String type, String variableId) {
            this.type = type;
            this.variableId = variableId;
        }

        public String getType() {
            return type;
        }

        public String getVariableId() {
            return variableId;
        }
    }

    private final DeviceService deviceService;
    private final BpmService bpmService;
    private final String errorNotFoundMessage;
    private final String errorInvalidMessage;
    private final DeviceAlarmService deviceAlarmService;
    private final MeteringService meteringService;
    private final IssueService issueService;
    private final Thesaurus thesaurus;

    @Inject
    public ProcessResource(
            DeviceService deviceService,
            DeviceAlarmService deviceAlarmService,
            BpmService bpmService,
            MeteringService meteringService,
            IssueService issueService,
            Thesaurus thesaurus) {
        this.deviceService = deviceService;
        this.bpmService = bpmService;
        this.deviceAlarmService = deviceAlarmService;
        this.meteringService = meteringService;
        this.issueService = issueService;
        this.thesaurus = thesaurus;

        this.errorNotFoundMessage = "Connexo Flow is not available.";
        this.errorInvalidMessage = "Invalid response received, please check your Flow version.";

    }

    @GET
    @Path("/processes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public ProcessHistoryGenInfos getProcessAll(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String jsonContent;
        int total = -1;
        JSONArray arr = null;
        try {
            String rest = "/services/rest/tasks/process/allprocesses";
            String req = getQueryParam(queryParameters);

            if (!"".equals(req)) {
                rest += req;
            }

            jsonContent = bpmService.getBpmServer().doGet(rest, auth);

            if (jsonContent != null && !"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                total = Integer.valueOf(obj.get("total").toString());
                arr = obj.getJSONArray("processHistories");
            }

        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        }

        ProcessHistoryGenInfos processGeneralInfos = new ProcessHistoryGenInfos(arr);

        if (total > 0) {
            processGeneralInfos.total = total;
        }


        /* Due to flow do not know devices, alarms and issues names we have to fill this fields here */
        for (int i = 0; i < processGeneralInfos.processHistories.size(); i++) {
            if (processGeneralInfos.getVariableId(i).equals(ProcessObjectType.DEVICE.getVariableId())) {
                String mrId = processGeneralInfos.getValue(i);
                Optional<Device> device = deviceService.findDeviceByMrid(mrId);
                if (device.isPresent()) {
                    processGeneralInfos.setObjectName(i, device.get().getName());
                } else {
                    /*This case can happen when you clear Connexo database.
                     * But Flow database still contains information about previously created processes */
                    System.out.println("There is no corresponding device in database");
                }

            }
            if (processGeneralInfos.getVariableId(i).equals(ProcessObjectType.ALARM.getVariableId())) {
                String alarmId = processGeneralInfos.getValue(i);
                Optional<? extends DeviceAlarm> tmpAlarm = deviceAlarmService.findAlarm(Long.valueOf(alarmId));
                if (tmpAlarm.isPresent()) {
                    processGeneralInfos.setObjectName(i, tmpAlarm.get().getIssueId() + ": " + tmpAlarm.get().getTitle());
                    processGeneralInfos.setCorrDeviceName(i, tmpAlarm.get().getDevice().getName());
                } else {
                    /*This case can happen when you clear Connexo database.
                     * But Flow database still contains information about previously created processes */
                    System.out.println("There is no corresponding alarm in database");
                }
            }

            if (processGeneralInfos.getVariableId(i).equals(ProcessObjectType.DATA_COLLECTION_ISSUE.getVariableId())) {
                String issueId = processGeneralInfos.getValue(i);
                Optional<? extends Issue> tmpIssue = issueService.findIssue(Long.valueOf(issueId));
                if (tmpIssue.isPresent()) {
                    processGeneralInfos.setObjectName(i, tmpIssue.get().getIssueId() + ": " + tmpIssue.get().getTitle());
                    if (tmpIssue.get().getDevice() != null) {
                        processGeneralInfos.setCorrDeviceName(i, tmpIssue.get().getDevice().getName());
                    }
                    processGeneralInfos.setIssueType(i, tmpIssue.get().getReason().getIssueType().getKey());
                } else {
                    System.out.println("There is no corresponding issue in database");
                }
            }

            if (processGeneralInfos.getVariableId(i).equals(USAGE_POINT_VARIABLE_ID)) {
                final String usagePointMRID = processGeneralInfos.getValue(i);
                Optional<UsagePoint> usagePoint = meteringService.findUsagePointByMRID(usagePointMRID);
                if (usagePoint.isPresent()) {
                    processGeneralInfos.setObjectName(i, usagePoint.get().getName());
                } else {
                    System.out.println("There is no corresponding usagePoint in database");
                }
            }
        }
        return processGeneralInfos;
    }

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public ProcessInfosForFilter getProcessesTypes(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        MultivaluedMap<String, String> filterProperties = uriInfo.getQueryParameters();

        /*Can get certain type of processes*/
        if (filterProperties.get("type") != null) {

            List<String> typesList = Arrays.asList(filterProperties.get("type").get(0).split(","));

            List<BpmProcessDefinition> activeProcesses = bpmService.getAllBpmProcessDefinitions();
            ProcessInfosForFilter processInfos = getBpmProcessDefinitions(auth);

            processInfos.processes = processInfos.processes.stream()
                    .filter(s -> activeProcesses.stream()
                            .anyMatch(a -> a.getProcessName().equals(s.name) &&
                                    a.getVersion().equals(s.version) &&
                                    a.getAssociationProvider().isPresent() &&
                                    typesList.stream().anyMatch(type -> type.equals(a.getAssociation()))))
                    .collect(Collectors.toList());


            processInfos.total = processInfos.processes.size();
            return processInfos;
        } else { /* Get all types of processes */

            List<BpmProcessDefinition> activeProcesses = bpmService.getAllBpmProcessDefinitions();
            ProcessInfosForFilter processInfos = getBpmProcessDefinitions(auth);

            processInfos.processes = processInfos.processes.stream()
                    .filter(s -> activeProcesses.stream()
                            .anyMatch(a -> a.getProcessName().equals(s.name) &&
                                    a.getVersion().equals(s.version) &&
                                    a.getAssociationProvider().isPresent()))
                    .collect(Collectors.toList());

            processInfos.total = processInfos.processes.size();

            return processInfos;
        }
    }

    @GET
    @Path("/deviceobjects")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public Response getDeviceObjects(@BeanParam StandardParametersBean params) {
        String searchText = params.getFirst("like");
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? "*" + searchText + "*" : "*";
        MeterFilter filter = new MeterFilter();
        filter.setName(dbSearchText);
        List<Meter> listMeters = meteringService.findMeters(filter).paged(params.getStart(), params.getLimit()).find();
        return Response.ok().entity(listMeters.stream().map(alm -> new DeviceObjectInfo(alm.getId(), alm.getName(), alm.getMRID())).collect(Collectors.toList())).build();
    }

    @GET
    @Path("/usagepointobjects")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public Response getUsagePointObjects(@BeanParam StandardParametersBean params) {
        String searchText = params.getFirst("like");
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? "*" + searchText + "*" : "*";
        UsagePointFilter filter = new UsagePointFilter();
        filter.setName(dbSearchText);
        Finder<UsagePoint> finder = meteringService.getUsagePoints(filter);
        List<UsagePoint> listUsagePoints = finder.find();
        return Response.ok()
                .entity(listUsagePoints.stream()
                        .map(alm -> new DeviceObjectInfo(alm.getId(), alm.getName(), alm.getMRID()))
                        .collect(Collectors.toList()))
                .build();
    }

    @GET
    @Path("/alarmobjects")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public Response getAlarmObjects(@BeanParam StandardParametersBean params) {

        String searchText = params.getFirst("like");

        DeviceAlarmFilter filter = new DeviceAlarmFilter();
        if (searchText != null && !searchText.isEmpty()) {
            filter.setAlarmId(searchText);
        }
        Finder<? extends DeviceAlarm> finder = deviceAlarmService.findAlarms(filter);
        List<? extends DeviceAlarm> deviceAlarms = finder.find();

        List<DeviceObjectInfo> deviceAlarmInfos = deviceAlarms.stream()
                .map(alm -> new DeviceObjectInfo(alm.getId(), alm.getIssueId(), Long.toString(alm.getId())))
                .collect(Collectors.toList());

        return Response.ok().entity(deviceAlarmInfos).build();
    }

    @GET
    @Path("/issueobjects")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public Response getIssueObjects(@BeanParam StandardParametersBean params) {

        String searchText = params.getFirst("like");

        IssueFilter filter = issueService.newIssueFilter();
        if (searchText != null && !searchText.isEmpty()) {
            filter.setIssueId(searchText);
        }
        Finder<? extends Issue> finder = issueService.findIssues(filter);
        List<? extends Issue> issues = finder.find();

        List<DeviceObjectInfo> deviceIssueInfos = issues.stream()
                .map(iss -> new DeviceObjectInfo(iss.getId(), iss.getIssueId(), Long.toString(iss.getId())))
                .collect(Collectors.toList());

        return Response.ok().entity(deviceIssueInfos).build();
    }

    @POST
    @Transactional
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_BPM)
    public Response validate(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth,
                             ProcessHistoryGenInfos request) {
        if (request == null || request.processHistories == null || request.processHistories.isEmpty()) {
            request = getProcessAll(uriInfo, auth);
        }
        Errors errors = new Errors(thesaurus);
        Map<Pair<String, String>, List<ProcessHistoryGenInfo>> processes = request.processHistories.stream()
                .collect(Collectors.groupingBy(process -> Pair.of(process.name, process.version)));
        if (processes.size() > 1) {
            throw new LocalizedException(thesaurus, MessageSeeds.CANT_RERUN_SEVERAL_PROCESSES) {
            };
        }
        Pair<String, String> nameAndVersion = processes.keySet().stream().findAny()
                .orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.CANT_RERUN_NO_PROCESSES) {
                });
        String name = nameAndVersion.getFirst();
        String version = nameAndVersion.getLast();
        BpmProcessDefinition definition = bpmService.getBpmProcessDefinition(name, version)
                .orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.NO_SUCH_PROCESS_DEFINITION) {
                });
        if (!definition.getStatus().equals("ACTIVE")) {
            throw new LocalizedException(thesaurus, MessageSeeds.PROCESS_IS_NOT_ACTIVE, name, version) {
            };
        }

        String typeString = definition.getAssociationProvider()
                .orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.PROCESS_OBJECT_TYPE_NOT_FOUND, name, version) {
                })
                .getType();
        ProcessObjectType type = Arrays.stream(ProcessObjectType.values())
                .filter(item -> item.getType().equalsIgnoreCase(typeString))
                .findAny()
                .orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.PROCESS_OBJECT_TYPE_NOT_FOUND, name, version) {
                });
        List<ProcessHistoryGenInfo> appropriateInstances = processes.get(nameAndVersion).stream()
                .filter(compatibleObjects(type, errors))
                .filter(consistentObjects(type, definition, errors))
                .filter(objectsWithoutRunningProcess(auth, errors))
                .filter(uniqueObjects(errors))
                .collect(Collectors.toList());

        return Response.ok(new ProcessHistoryGenInfos(appropriateInstances, request.processHistories.size(), errors.getErrorsInfo())).build();
    }

    private static Predicate<ProcessHistoryGenInfo> compatibleObjects(ProcessObjectType type, Errors errors) {
        return info -> {
            boolean compatible = info.variableId.equals(type.getVariableId());
            if (!compatible) {
                errors.addError(MessageSeeds.OBJECTS_FILTERED_TYPE_NOT_COMPATIBLE, info.getObjectName());
            }
            return compatible;
        };
    }

    private Predicate<ProcessHistoryGenInfo> consistentObjects(ProcessObjectType type, BpmProcessDefinition definition, Errors errors) {
        switch (type) {
            case ALARM:
                Set<String> allowedAlarmReasons = getSetOfValueIds(definition, "alarmReasons");
                return info -> {
                    Optional<? extends DeviceAlarm> alarmOptional = deviceAlarmService.findAlarm(Long.parseLong(info.getValue()));
                    if (!alarmOptional.isPresent()) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_FOUND, Checks.is(info.getObjectName()).empty() ? info.getValue() : info.getObjectName());
                        return false;
                    }
                    String alarmReason = alarmOptional.get().getReason().getKey();
                    if (!allowedAlarmReasons.contains(alarmReason)) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_CONSISTENT, info.getObjectName());
                        return false;
                    }
                    return true;
                };
            case DEVICE:
                Set<String> allowedStateIds = getSetOfValueIds(definition, DeviceResource.PROCESS_KEY_DEVICE_STATES);
                return info -> {
                    Optional<Device> deviceOptional = deviceService.findDeviceByMrid(info.getValue());
                    if (!deviceOptional.isPresent()) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_FOUND, Checks.is(info.getObjectName()).empty() ? info.getValue() : info.getObjectName());
                        return false;
                    }
                    String stateId = Long.toString(deviceOptional.get().getState().getId());
                    if (!allowedStateIds.contains(stateId)) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_CONSISTENT, info.getObjectName());
                        return false;
                    }
                    return true;
                };
            case DATA_COLLECTION_ISSUE:
                Set<String> allowedIssueReasons = getSetOfValueIds(definition, "issueReasons");
                return info -> {
                    Optional<? extends Issue> issueOptional = issueService.findIssue(Long.parseLong(info.getValue()));
                    if (!issueOptional.isPresent()) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_FOUND, Checks.is(info.getObjectName()).empty() ? info.getValue() : info.getObjectName());
                        return false;
                    }
                    String issueReason = issueOptional.get().getReason().getKey();
                    if (!allowedIssueReasons.contains(issueReason)) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_CONSISTENT, info.getObjectName());
                        return false;
                    }
                    return true;
                };
            case ISSUE_TYPE_NAME:
                Set<String> allowedLifecycleIssueReasons = getSetOfValueIds(definition, DeviceResource.PROCESS_LIFECYCLE_ISSUE_STATES);
                return info -> {
                    Optional<? extends Issue> issueOptional = issueService.findIssue(Long.parseLong(info.getValue()));
                    if (!issueOptional.isPresent()) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_FOUND, Checks.is(info.getObjectName()).empty() ? info.getValue() : info.getObjectName());
                        return false;
                    }
                    String issueReason = issueOptional.get().getReason().getKey();
                    if (!allowedLifecycleIssueReasons.contains(issueReason)) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_CONSISTENT, info.getObjectName());
                        return false;
                    }
                    return true;
                };
            case TASK_ISSUE:
                Set<String> allowedTaskIssueReasons = getSetOfValueIds(definition, DeviceResource.PROCESS_TASK_ISSUE_STATES);
                return info -> {
                    Optional<? extends Issue> issueOptional = issueService.findIssue(Long.parseLong(info.getValue()));
                    if (!issueOptional.isPresent()) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_FOUND, Checks.is(info.getObjectName()).empty() ? info.getValue() : info.getObjectName());
                        return false;
                    }
                    String issueReason = issueOptional.get().getReason().getKey();
                    if (!allowedTaskIssueReasons.contains(issueReason)) {
                        errors.addError(MessageSeeds.OBJECTS_FILTERED_NOT_CONSISTENT, info.getObjectName());
                        return false;
                    }
                    return true;
                };
            default:
                throw new IllegalStateException("There is no switch case for " + ProcessObjectType.class.getSimpleName() + '.' + type.name());
        }
    }

    private static Set<String> getSetOfValueIds(BpmProcessDefinition definition, String propertyName) {
        return Optional.ofNullable(definition.getProperties().get(propertyName))
                .filter(List.class::isInstance)
                .map(list -> (List<?>) list)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(HasIdAndName.class::isInstance)
                .map(HasIdAndName.class::cast)
                .map(HasIdAndName::getId)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private Predicate<ProcessHistoryGenInfo> objectsWithoutRunningProcess(String auth, Errors errors) {
        return info -> {
            boolean running = info.status.equals("1");
            if (!running) {
                running = bpmService.getRunningProcesses(auth, "?variableid=" + info.variableId + "&variablevalue=" + info.value).processes.stream()
                        .anyMatch(item -> info.name.equals(item.name) && info.version.equals(item.version) && item.status.equals("1"));
            }
            if (running) {
                errors.addError(MessageSeeds.OBJECTS_FILTERED_ALREADY_RUNNING, info.getObjectName());
            }
            return !running;
        };
    }

    private static Predicate<ProcessHistoryGenInfo> uniqueObjects(Errors errors) {
        Set<String> seen = new HashSet<>();
        return info -> {
            boolean unique = seen.add(info.getValue());
            if (!unique) {
                errors.addError(MessageSeeds.OBJECTS_FILTERED_DUPLICATED, info.getObjectName());
            }
            return unique;
        };
    }

    private static class Errors {
        private final Thesaurus thesaurus;
        private final Map<MessageSeed, ObjectsWithErrorCounter> errorsMap = new HashMap<>();

        private Errors(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        private void addError(MessageSeed messageSeed, String objectName) {
            errorsMap.computeIfAbsent(messageSeed, message -> {
                ObjectsWithErrorCounter counter = new ObjectsWithErrorCounter();
                if (MessageSeeds.OBJECTS_FILTERED_DUPLICATED == messageSeed) {
                    counter.add(objectName);
                }
                return counter;

            }).add(objectName);
        }

        private List<ErrorInfo> getErrorsInfo() {
            return errorsMap.entrySet().stream()
                    .map(messageAndObjectNames -> {
                        String objectNames = messageAndObjectNames.getValue().stream()
                                .collect(Collectors.joining(", "));
                        return new ErrorInfo(thesaurus.getSimpleFormat(messageAndObjectNames.getKey()).format(messageAndObjectNames.getValue().size()),
                                thesaurus.getFormat(TranslationKeys.OBJECTS).format(objectNames));
                    })
                    .collect(Collectors.toList());
        }
    }

    private static class ObjectsWithErrorCounter extends HashSet<String> {

        private int total = 0;

        @Override
        public boolean add(String name) {
            total++;
            return super.add(name);
        }

        @Override
        public int size() {
            return total;
        }
    }


    /* Used to get all processes deployed on flow */
    private ProcessInfosForFilter getBpmProcessDefinitions(String auth) {
        String jsonContent;
        JSONArray arr = null;

        try {
            jsonContent = bpmService.getBpmServer().doGet("/services/rest/server/queries/processes/definitions?page=0&pageSize=1000", auth);
            if (jsonContent != null && !"".equals(jsonContent)) {
                JSONObject jsnobject = new JSONObject(jsonContent);
                arr = jsnobject.getJSONArray("processes");
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        }
        return new ProcessInfosForFilter(arr);
    }


    private String getQueryParam(QueryParameters queryParam) {
        StringBuilder req = new StringBuilder();
        int i = 0;
        for (String theKey : queryParam.keySet()) {
            if (i > 0) {
                req.append("&");
            } else {
                req.append("?");
            }
            req.append(theKey).append("=").append(queryParam.getFirst(theKey));
            i++;
        }
        return req.toString();
    }

}
