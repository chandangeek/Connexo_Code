package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComServer;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/3/14.
 */
public class ComSessionResource {

    private static final String LOG_LEVELS_FILTER_PROPERTY = "logLevels";
    private static final String LOG_TYPES_FILTER_PROPERTY = "logTypes";
    private static final String CONNECTIONS_FILTER_ITEM = "Connections";
    private static final String COMMUNICATIONS_FILTER_ITEM = "Communications";

    private final ResourceHelper resourceHelper;
    private final ConnectionTaskService connectionTaskService;
    private final ComSessionInfoFactory comSessionInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory;
    private final JournalEntryInfoFactory journalEntryInfoFactory;

    @Inject
    public ComSessionResource(ResourceHelper resourceHelper, ConnectionTaskService connectionTaskService, ComSessionInfoFactory comSessionInfoFactory, ExceptionFactory exceptionFactory, ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory, JournalEntryInfoFactory journalEntryInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.connectionTaskService = connectionTaskService;
        this.comSessionInfoFactory = comSessionInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.comTaskExecutionSessionInfoFactory = comTaskExecutionSessionInfoFactory;
        this.journalEntryInfoFactory = journalEntryInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public ComSessionsInfo getConnectionMethodHistory(@PathParam("mRID") String mrid, @PathParam("connectionMethodId") long connectionMethodId, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        List<ComSession> comSessions = connectionTaskService.findAllSessionsFor(connectionTask).stream().sorted((c1, c2) -> c2.getStartDate().compareTo(c1.getStartDate())).collect(toList());
        List<ComSessionInfo> comSessionsInPage = ListPager.of(comSessions).from(queryParameters).find().stream()
                .sorted((cs1, cs2) -> cs2.getStartDate().compareTo(cs1.getStartDate()))
                .map(comSessionInfoFactory::from).collect(toList());
        PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("comSessions", comSessionsInPage, queryParameters);
        ComSessionsInfo info = new ComSessionsInfo();
        info.connectionMethod = connectionTask.getName();
        info.comSessions = pagedInfoList.getInfos();
        info.total = pagedInfoList.getTotal();
        return info;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{comSessionId}")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public ComSessionInfo getComSession(@PathParam("mRID") String mrid, @PathParam("connectionMethodId") long connectionMethodId, @PathParam("comSessionId") long comSessionId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        ComSession comSession = getComSessionOrThrowException(comSessionId, connectionTask);
        ComSessionInfo info = comSessionInfoFactory.from(comSession);
        info.connectionMethod = new IdWithNameInfo(connectionTask);
        return info;
    }

    @GET
    @Path("{comSessionId}/comtaskexecutionsessions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public ComTaskExecutionSessionsInfo getComTaskExecutionSessions(@PathParam("mRID") String mrid, @PathParam("connectionMethodId") long connectionMethodId, @PathParam("comSessionId") long comSessionId, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        ComSession comSession = getComSessionOrThrowException(comSessionId, connectionTask);
        List<ComTaskExecutionSession> comTaskExecutionSessionsInPage = ListPager.of(comSession.getComTaskExecutionSessions()).from(queryParameters).find();
        List<ComTaskExecutionSessionInfo> comTaskExecutionSessionInfos = comTaskExecutionSessionsInPage.stream().map(comTaskExecutionSessionInfoFactory::from).collect(toList());
        ComTaskExecutionSessionsInfo info = new ComTaskExecutionSessionsInfo();
        PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("comTaskExecutionSessions", comTaskExecutionSessionInfos, queryParameters);
        info.device = device.getName();
        info.total = pagedInfoList.getTotal();
        info.comTaskExecutionSessions = pagedInfoList.getInfos();
        return info;
    }

    @GET
    @Path("{comSessionId}/journals")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public PagedInfoList getHybridJournalEntries(@PathParam("mRID") String mrid,
                                                 @PathParam("connectionMethodId") long connectionMethodId,
                                                 @PathParam("comSessionId") long comSessionId,
                                                 @BeanParam JsonQueryFilter jsonQueryFilter,
                                                 @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        ComSession comSession = getComSessionOrThrowException(comSessionId, connectionTask);
        int start = 0;
        int limit = Integer.MAX_VALUE;
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            start = queryParameters.getStart().get() + 1;
            limit = queryParameters.getLimit().get();
        }

        List<JournalEntryInfo> infos = new ArrayList<>();
        EnumSet<ComServer.LogLevel> logLevels = EnumSet.noneOf(ComServer.LogLevel.class);
        if (jsonQueryFilter.hasProperty(LOG_LEVELS_FILTER_PROPERTY)) {
            jsonQueryFilter.getPropertyList(LOG_LEVELS_FILTER_PROPERTY, new LogLevelAdapter()).stream().forEach(logLevels::add);
        } else {
            logLevels = EnumSet.allOf(ComServer.LogLevel.class);
        }
        if (jsonQueryFilter.hasProperty(LOG_TYPES_FILTER_PROPERTY)) {
            List<String> logTypes = jsonQueryFilter.getStringList(LOG_TYPES_FILTER_PROPERTY);
            if (logTypes.contains(CONNECTIONS_FILTER_ITEM)) {
                if (logTypes.contains(COMMUNICATIONS_FILTER_ITEM)) {
                    comSession.getAllLogs(logLevels, start, limit).stream().forEach(e -> infos.add(journalEntryInfoFactory.asInfo(e)));
                } else {
                    comSession.getJournalEntries(logLevels).from(queryParameters).sorted("timestamp", false).stream().forEach(e -> infos.add(journalEntryInfoFactory.asInfo(e)));
                }
            } else {
                if (logTypes.contains(COMMUNICATIONS_FILTER_ITEM)) {
                    comSession.getCommunicationTaskJournalEntries(logLevels).from(queryParameters).sorted("timestamp", false).stream().forEach(e -> infos.add(journalEntryInfoFactory.asInfo(e)));
                } else {
                    comSession.getAllLogs(logLevels, start, limit).stream().forEach(e -> infos.add(journalEntryInfoFactory.asInfo(e)));
                }
            }
        } else {
            comSession.getAllLogs(logLevels, start, limit).stream().forEach(e -> infos.add(journalEntryInfoFactory.asInfo(e)));
        }
        return PagedInfoList.fromPagedList("journals", infos, queryParameters);
    }

    private ComSession getComSessionOrThrowException(long comSessionId, ConnectionTask<?, ?> connectionTask) {
        List<ComSession> comSessions = connectionTaskService.findAllSessionsFor(connectionTask).stream().sorted((c1, c2) -> c1.getStartDate().compareTo(c2.getStartDate())).collect(toList());
        Optional<ComSession> comSessionOptional = comSessions.stream().filter(c -> c.getId() == comSessionId).findFirst();
        if (!comSessionOptional.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_SESSION_ON_CONNECTION_METHOD);
        }
        return comSessionOptional.get();
    }

}