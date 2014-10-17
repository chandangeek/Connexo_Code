package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.model.ComServer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/3/14.
 */
public class ComSessionResource {

    private static final String LOG_LEVELS_FILTER_PROPERTY = "logLevels";
    private static final String LOG_TYPES_FILTER_PROPERTY = "logTypes";
    private static final String CONNECTIONS_FILTER_ITEM = "connections";
    private static final String COMMUNICATIONS_FILTER_ITEM = "communications";

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
    @Produces(MediaType.APPLICATION_JSON)
    public ComSessionsInfo getConnectionMethodHistory(@PathParam("mRID") String mrid, @PathParam("connectionMethodId") long connectionMethodId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        List<ComSession> comSessions = connectionTaskService.findAllSessionsFor(connectionTask).stream().sorted((c1, c2) -> c1.getStartDate().compareTo(c2.getStartDate())).collect(toList());
        List<ComSessionInfo> comSessionsInPage = ListPager.of(comSessions).from(queryParameters).find().stream()
                .sorted((cs1, cs2)->cs2.getStartDate().compareTo(cs1.getStartDate()))
                .map(comSessionInfoFactory::from).collect(toList());
        PagedInfoList pagedInfoList = PagedInfoList.asJson("comSessions", comSessionsInPage, queryParameters);
        ComSessionsInfo info = new ComSessionsInfo();
        info.connectionMethod = connectionTask.getName();
        info.comSessions = pagedInfoList.getInfos();
        info.total = pagedInfoList.getTotal();
        return info;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{comSessionId}")
    public ComSessionInfo getComSession(@PathParam("mRID") String mrid, @PathParam("connectionMethodId") long connectionMethodId, @PathParam("comSessionId") long comSessionId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        ComSession comSession = getComSessionOrThrowException(comSessionId, connectionTask);
        ComSessionInfo info = comSessionInfoFactory.from(comSession);
        info.connectionMethod = connectionTask.getName();
        return info;
    }

    @GET
    @Path("{comSessionId}/comtaskexecutionsessions")
    @Produces(MediaType.APPLICATION_JSON)
    public ComTaskExecutionSessionsInfo getComTaskExecutionSessions(@PathParam("mRID") String mrid, @PathParam("connectionMethodId") long connectionMethodId, @PathParam("comSessionId") long comSessionId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        ComSession comSession = getComSessionOrThrowException(comSessionId, connectionTask);
        List<ComTaskExecutionSession> comTaskExecutionSessionsInPage = ListPager.of(comSession.getComTaskExecutionSessions()).from(queryParameters).find();
        List<ComTaskExecutionSessionInfo> comTaskExecutionSessionInfos = comTaskExecutionSessionsInPage.stream().map(comTaskExecutionSessionInfoFactory::from).collect(toList());
        ComTaskExecutionSessionsInfo info = new ComTaskExecutionSessionsInfo();
        PagedInfoList pagedInfoList = PagedInfoList.asJson("comTaskExecutionSessions", comTaskExecutionSessionInfos, queryParameters);
        info.device=device.getName();
        info.total=pagedInfoList.getTotal();
        info.comTaskExecutionSessions= pagedInfoList.getInfos();
        return info;
    }

    @GET
    @Path("{comSessionId}/journals")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getHybridJournalEntries(@PathParam("mRID") String mrid,
                                                 @PathParam("connectionMethodId") long connectionMethodId,
                                                 @PathParam("comSessionId") long comSessionId,
                                                 @BeanParam JsonQueryFilter jsonQueryFilter,
                                                 @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        ComSession comSession = getComSessionOrThrowException(comSessionId, connectionTask);
        int start=1;
        int limit=Integer.MAX_VALUE;
        if (queryParameters.getStart()!=null && queryParameters.getLimit()!=0) {
            start=queryParameters.getStart()+1;
            limit=queryParameters.getLimit();
        }

        List<JournalEntryInfo> infos = new ArrayList<>();
        EnumSet<ComServer.LogLevel> logLevels = EnumSet.noneOf(ComServer.LogLevel.class);
        if (jsonQueryFilter.getProperty(LOG_LEVELS_FILTER_PROPERTY) != null) {
            jsonQueryFilter.getPropertyList(LOG_LEVELS_FILTER_PROPERTY, new LogLevelAdapter()).stream().forEach(logLevels::add);
        }
        if (jsonQueryFilter.getProperty(LOG_TYPES_FILTER_PROPERTY) != null) {
            List<String> logTypes = jsonQueryFilter.getPropertyList(LOG_TYPES_FILTER_PROPERTY);
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
                    // User didn't select anything and is getting just that...
                }
            }
        } else {
            comSession.getAllLogs(logLevels, start, limit).stream().forEach(e -> infos.add(journalEntryInfoFactory.asInfo(e)));
        }
        return PagedInfoList.asJson("journals", infos, queryParameters);
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

class ComSessionsInfo {
    public String connectionMethod;
    public int total;
    public List<?> comSessions;
}

class ComTaskExecutionSessionsInfo {
    public String device;
    public int total;
    public List<?> comTaskExecutionSessions;
}

