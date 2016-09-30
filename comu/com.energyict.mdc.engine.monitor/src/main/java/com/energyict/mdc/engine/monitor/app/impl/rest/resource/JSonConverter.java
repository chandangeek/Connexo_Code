package com.energyict.mdc.engine.monitor.app.impl.rest.resource;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.DateTimeFormatGenerator;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.monitor.CollectedDataStorageStatistics;
import com.energyict.mdc.engine.monitor.ComServerOperationalStatistics;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.monitor.InboundComPortMonitor;
import com.energyict.mdc.engine.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.StatusService;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Converting (monitoring) Java Objects to JSONObjects
 * Date: 23/10/13
 * Time: 10:14
 */
public class JSonConverter {

    private final static long K = 1024L;
    private static final int DAYS_IN_WEEK = 7;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int HOURS_IN_DAY = 24;
    private static final int SECONDS_IN_HOUR = MINUTES_IN_HOUR * SECONDS_IN_MINUTE;
    private static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * HOURS_IN_DAY;
    private static final int SECONDS_IN_WEEK = SECONDS_IN_DAY * DAYS_IN_WEEK;
    private static final int DAYS_IN_MONTH = 31;
    private static final int SECONDS_IN_MONTH = SECONDS_IN_DAY * DAYS_IN_MONTH;
    private static final int DAYS_IN_YEAR = 365;
    private static final int SECONDS_IN_YEAR = SECONDS_IN_DAY * DAYS_IN_YEAR;

    private StatusService statusService;
    private EngineConfigurationService engineConfigurationService;
    private ThreadPrincipalService threadPrincipalService;
    private UserService userService;

    private JSONObject lastConverted = null;
    private JSONObject convertedArray[] = null;

    @Inject
    public JSonConverter(StatusService statusService,
                         EngineConfigurationService engineConfigurationService,
                         ThreadPrincipalService threadPrincipalService,
                         UserService userService){
        this.statusService = statusService;
        this.engineConfigurationService = engineConfigurationService;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
    }

    public synchronized JSONObject asReadEvent() throws JSONException {
        JSONObject readEvent = new JSONObject();
        readEvent.put("event", "read");
        if (lastConverted != null){
            readEvent.put("data", lastConverted);
        }
        lastConverted = null;
        return readEvent;
    }

    public synchronized JSONObject asArray(String arrayName) throws JSONException {
        JSONObject result = new JSONObject();
        result.put(arrayName, convertedArray);
        setLastConVerted(result);
        return result;
    }

    synchronized void setLastConVerted(JSONObject jsonObject) {
        this.lastConverted = jsonObject;
    }

    public void setConvertedArray(JSONObject[] convertedArray) {
        this.convertedArray = convertedArray;
    }

    public JSonConverter convertDetails() throws JSONException {
        ComServerStatus status = statusService.getStatus();
        if (status != null && status.isRunning()) {
            ComServerOperationalStatistics operationalStatistics = status.getComServerMonitor().getOperationalStatistics();

            JSONObject result = new JSONObject();
            result.put("serverId", status.getComServerId());
            result.put("currentDate", format(Instant.now(), DateTimeFormatGenerator.Mode.SHORT, DateTimeFormatGenerator.Mode.SHORT));
            if (status.isRunning() && operationalStatistics != null) {
                Date startTime = operationalStatistics.getStartTimestamp();
                if (startTime != null) {
                    String startedAndDurationText = format(startTime.toInstant(), DateTimeFormatGenerator.Mode.SHORT, DateTimeFormatGenerator.Mode.SHORT);
                    TimeDuration duration = operationalStatistics.getRunningTime();
                    if (duration != null) {
                        startedAndDurationText += " (" + formatDuration(duration) + ")";
                    }
                    result.put("started", startedAndDurationText);
                }
            }
            result.put("serverName", status.getComServerName());

            setLastConVerted(result);
        }
        return this;
    }

    public JSonConverter convertGeneralInfo() throws JSONException {
        ComServerStatus status = statusService.getStatus();
        if (status != null && status.isRunning()) {
            ComServerOperationalStatistics operationalStatistics = status.getComServerMonitor().getOperationalStatistics();
            JSONObject result = new JSONObject();
            TimeDuration changesInterPollDelay = operationalStatistics.getChangesInterPollDelay();
            Date lastCheckForChanges = operationalStatistics.getLastCheckForChangesTimestamp().orElse(null);
            if (changesInterPollDelay != null) {
                int numberOf = changesInterPollDelay.getCount();
                String unit = changesInterPollDelay.getTimeUnit().getDescription();
                if (numberOf == 1) {
                    unit = unit.substring(0, unit.length() - 1);
                }
                JSONObject jsDuration = new JSONObject();
                jsDuration.put("count", numberOf);
                jsDuration.put("time-unit", unit);
                result.put("changeDetectionFrequency", jsDuration);
                if (lastCheckForChanges != null) {
                    result.put("changeDetectionNextRun",
                        format(
                            lastCheckForChanges.toInstant().plusSeconds(changesInterPollDelay.getSeconds()),
                            DateTimeFormatGenerator.Mode.LONG,
                            DateTimeFormatGenerator.Mode.LONG
                        )
                    );
                }
            }

            TimeDuration schedulingInterPollDelay = operationalStatistics.getSchedulingInterPollDelay();
            if (schedulingInterPollDelay != null) {
                int numberOf = schedulingInterPollDelay.getCount();
                String unit = schedulingInterPollDelay.getTimeUnit().getDescription();
                if (numberOf == 1) {
                    unit = unit.substring(0, unit.length() - 1);
                }
                JSONObject jsDuration = new JSONObject();
                jsDuration.put("count", numberOf);
                jsDuration.put("time-unit", unit);
                result.put("pollingFrequency", jsDuration);
            }

            ComServer comServer = engineConfigurationService.findComServer(status.getComServerId()).orElse(null);
            if (comServer != null) {
                result.put("eventRegistrationUri", comServer.getEventRegistrationUriIfSupported());
            }
            setLastConVerted(result);
        }
        return this;
    }

    public synchronized JSonConverter convertRunningInfo() throws JSONException {
        ComServerStatus status = statusService.getStatus();
        if (status != null && status.isRunning()) {
            EventAPIStatistics eventAPIStatistics = status.getComServerMonitor().getEventApiStatistics();

            JSONObject result = new JSONObject();
            result.put("numberOfEvents", eventAPIStatistics.getNumberOfEvents());
            result.put("maxMemory", "" + Runtime.getRuntime().maxMemory() / K / K);
            result.put("usedMemory", "" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / K / K));
            result.put("unit", "MB");

            setLastConVerted(result);
        }
        return this;
    }

    public synchronized JSonConverter convertConnectedRemoteServers(boolean active) throws JSONException {
        //TODO: Remote ComServers not yet supported in Connexo
//        ComServer comServer =  comServerDAO.getThisComServer();
//        List<JSONObject> remotes = new ArrayList<>();
//        if (comServer.isOnline()){
//            List<RemoteComServer> remoteComServers = ManagerFactory.getCurrent().getComServerFactory().findRemoteComServersWithOnlineComServer((OnlineComServer) monitor.getComServer());
//            QueryAPIStatistics queryAPIStatistics = monitor.getQueryApiStatistics();
//            Map<String, Date> registeredClients = new HashMap<>();
//            if (queryAPIStatistics != null){
//                registeredClients.putAll(monitor.getQueryApiStatistics().getRegisteredClients());
//            }
//            for (RemoteComServer each : remoteComServers) {
//                if (each.isActive() == active) {
//                    JSONObject server = new JSONObject();
//                    server.put("id", each.getId());
//                    server.put("name", each.getName());
//                    server.put("active", each.isActive());
//                    Date lastSeen = registeredClients.get(each.getName());
//                    if (lastSeen != null){
//                       server.put("lastSeen", format(lastSeen));
//                    }
//                    remotes.add(server);
//                }
//            }
//        }
//        setConvertedArray(remotes.toArray(new JSONObject[remotes.size()]));
        return this;
    }

    public synchronized JSonConverter convertCommunicationPorts(boolean active) throws JSONException {
        ComServerStatus status = statusService.getStatus();
        if (status != null && status.isRunning()) {
            List<ScheduledComPortMonitor> scheduledComportMonitors = status.getScheduledComportMonitors();
            List<InboundComPortMonitor> inboundComportMonitors = status.getInboundComportMonitors();
            ComServer comServer = engineConfigurationService.findComServer(status.getComServerId()).orElse(null);

            List<JSONObject> comPorts = new ArrayList<>();

            for (ComPort each : comServer.getComPorts().stream()
                    .sorted(Comparator.comparing(ComPort::getName))
                    .collect(Collectors.toList())) {
                boolean activeProcess;
                String lastSeen = null;
                if (each.isInbound()) {
                    Optional<InboundComPortMonitor> monitor = inboundComportMonitors.stream().filter(m -> m.isMonitoring(each)).findFirst();
                    activeProcess = (monitor.isPresent());
                } else {
                    Optional<ScheduledComPortMonitor> monitor = scheduledComportMonitors.stream().filter(m -> m.isMonitoring(each)).findFirst();
                    activeProcess = (monitor.isPresent());
                    if (monitor.isPresent()) {
                        Optional<Date> lastCheckForWork = monitor.get().getOperationalStatistics().getLastCheckForWorkTimestamp();
                        if (lastCheckForWork.isPresent()) {
                            lastSeen = format(lastCheckForWork.get().toInstant(), DateTimeFormatGenerator.Mode.LONG, DateTimeFormatGenerator.Mode.LONG);
                        }
                    }
                }
                if (active == activeProcess) {
                    JSONObject jsonComPort = new JSONObject();
                    jsonComPort.put("id", each.getId());
                    jsonComPort.put("name", each.getName());
                    jsonComPort.put("description", each.getDescription());
                    jsonComPort.put("inbound", each.isInbound());
                    if (lastSeen != null) {
                        jsonComPort.put("lastSeen", lastSeen);
                    }
                    if (activeProcess) {
                        jsonComPort.put("threads", getThreadCount(each));
                    }
                    comPorts.add(jsonComPort);
                }
            }
            setConvertedArray(comPorts.toArray(new JSONObject[comPorts.size()]));
        }
        return this;
    }

    public synchronized JSonConverter convertCommunicationPortPools() throws JSONException {
        setConvertedArray(getComPortPools(null));
        return this;
    }

    public synchronized JSonConverter convertCommunicationPortPools(boolean active) throws JSONException {
        setConvertedArray(getComPortPools(active));
        return this;
    }

    public synchronized JSonConverter convertCollectedDataStorageStatistics() throws JSONException {
        ComServerStatus status = statusService.getStatus();
        if (status != null && status.isRunning()) {
            CollectedDataStorageStatistics statistics = status.getComServerMonitor().getCollectedDataStorageStatistics();
            JSONObject result = new JSONObject();
            result.put("time", Instant.now().toEpochMilli());
            result.put("load", statistics.getLoadPercentage());
            result.put("threads", statistics.getNumberOfThreads());
            result.put("priority", statistics.getThreadPriority());
            result.put("capacity", statistics.getCapacity());
            result.put("currentSize", statistics.getCurrentSize());
            setLastConVerted(result);
        }
        return this;
    }

    public synchronized JSonConverter convertThreadsInUse() throws JSONException {
        ComServerStatus status = statusService.getStatus();
        if (status != null && status.isRunning()) {
            List<ScheduledComPortMonitor> scheduledComportMonitors = status.getScheduledComportMonitors();
            ComServer comServer = engineConfigurationService.findComServer(status.getComServerId()).orElse(null);

            int threadCount = 0;
            int activeThreadCount = 0;

            for (ComPort each : comServer.getComPorts()) {
                if (!each.isInbound()) {
                    Optional<ScheduledComPortMonitor> monitor = scheduledComportMonitors.stream().filter(m -> m.isMonitoring(each)).findFirst();

                    threadCount += getThreadCount(each);
                    if (monitor.isPresent()) {
                        activeThreadCount += getThreadCount(each);
                    }
                }
            }

            JSONObject threadsInUse = new JSONObject();
            threadsInUse.put("name", "inUse");
            threadsInUse.put("data", activeThreadCount);

            JSONObject threadsNotInUse = new JSONObject();
            threadsNotInUse.put("name", "notInUse");
            threadsNotInUse.put("data", threadCount - activeThreadCount);

            JSONObject result = new JSONObject();
            result.put("threads", new JSONObject[]{threadsInUse, threadsNotInUse});
            setLastConVerted(result);
        }
        return this;
    }

    private JSONObject[] getComPortPools(Boolean active) throws JSONException{
        Map<ComPortPool, List<ComPort>> activeMapping = getComPortPoolMapping(active == null ? null : true);
        HashSet<ComPortPool> notInactiveSet = new HashSet<>();
        if (active!= null && !active){
            for (ComPortPool each: engineConfigurationService.findAllComPortPools()){
                if (!activeMapping.containsKey(each)){
                    notInactiveSet.add(each);
                }
            }
        }
        Set<ComPortPool> comPortPools;
        if (active == null || active){
            comPortPools = activeMapping.keySet();
        }else{
            comPortPools = notInactiveSet;
        }
        List<JSONObject>  jsonPools = new ArrayList<>();
        for (ComPortPool each: comPortPools){
            JSONObject jsonPool = new JSONObject();
            jsonPool.put("id", each.getId());
            jsonPool.put("name", each.getName());
            jsonPool.put("description", each.getDescription());
            jsonPool.put("inbound", each.isInbound());
            jsonPool.put("active", active);
            if (activeMapping.containsKey(each)){
                List<ComPort> comPorts = activeMapping.get(each);
                jsonPool.put("ports", comPorts.size());
                jsonPool.put("threads", comPorts.stream().mapToInt(this::getThreadCount).sum());
            }
            jsonPools.add(jsonPool);
        }
        jsonPools = jsonPools.stream().sorted((p1, p2) -> this.getName(p1).compareTo(this.getName(p2))).collect(Collectors.toList());
        return jsonPools.toArray(new JSONObject[jsonPools.size()]);
    }

    private String getName(JSONObject jsonPool){
        try{
            return (String) jsonPool.get("name");
        }catch (JSONException e){
            return "";
        }
    }

    private Map<ComPortPool, List<ComPort>> getComPortPoolMapping(Boolean active){
        ComServerStatus status = statusService.getStatus();
        ComServer comServer = engineConfigurationService.findComServer(status.getComServerId()).orElse(null);
        List<ComPortPool> comPortPools = engineConfigurationService.findAllComPortPools();

        Map<ComPortPool, List<ComPort>> mapping = new HashMap<>();
        for (ComPort each : comServer.getComPorts()) {
            Optional<ComPortPool> pool = Optional.empty();
            if (each.isInbound()){
                InboundComPortPool inboundComPortPool = ((InboundComPort) each).getComPortPool();
                if (inboundComPortPool != null) {
                    pool = Optional.of(inboundComPortPool);
                }
            }else{
                pool = findOutboundComportPool(comPortPools, each);
            }
            if (pool.isPresent() && (active == null || active == hasActiveProcess(each))) {
                List<ComPort> ports = mapping.get(pool.get());
                if (ports == null){
                    ports = new ArrayList<>();
                    mapping.put(pool.get(), ports);
                }
                ports.add(each);
            }
        }
        return mapping;
    }

    private Optional<ComPortPool> findOutboundComportPool(List<ComPortPool> possible, ComPort comPort){
        return possible.stream().filter(((Predicate<ComPortPool>) ComPortPool::isInbound).negate().and(p -> this.comPortPoolIncludesComPort(p, comPort))).findFirst();
    }

    private boolean hasActiveProcess(ComPort comPort){
        ComServerStatus status = statusService.getStatus();
        if (comPort.isInbound()){
            return status.getInboundComportMonitors().stream().anyMatch(m -> m.isMonitoring(comPort));
        }else {
            return status.getScheduledComportMonitors().stream().anyMatch(m -> m.isMonitoring(comPort));
        }
    }

    private int getThreadCount(ComPort comPort){
        return comPort.getNumberOfSimultaneousConnections();
    }

    private boolean comPortPoolIncludesComPort(ComPortPool pool, ComPort port){
        return pool.getComPorts().stream().anyMatch(p -> p.getId() == port.getId());
    }

    private String format(Instant date, DateTimeFormatGenerator.Mode dateFormatMode, DateTimeFormatGenerator.Mode timeFormatMode) {
        return date!=null ? getDateFormatForCurrentUser(dateFormatMode, timeFormatMode).format(LocalDateTime.ofInstant(date, ZoneId.systemDefault())) : "";
    }

    private static String formatDuration(TimeDuration duration) {
        StringBuilder builder = new StringBuilder();
        int numberOfSeconds = duration.getSeconds();
        int numberOfUnits = 0;
        int maxNumberOfUnits = 2;

        List<Pair<Integer, TimeDuration>> quantitiesToTest = new ArrayList<>();
        quantitiesToTest.add(Pair.of(SECONDS_IN_YEAR, new TimeDuration(1, Calendar.YEAR)));
        quantitiesToTest.add(Pair.of(SECONDS_IN_MONTH, new TimeDuration(1,Calendar.MONTH)));
        quantitiesToTest.add(Pair.of(SECONDS_IN_WEEK, new TimeDuration(1, Calendar.WEEK_OF_YEAR)));
        quantitiesToTest.add(Pair.of(SECONDS_IN_DAY, new TimeDuration(1, Calendar.DAY_OF_MONTH)));
        quantitiesToTest.add(Pair.of(SECONDS_IN_HOUR, new TimeDuration(1, Calendar.HOUR_OF_DAY)));
        quantitiesToTest.add(Pair.of(SECONDS_IN_MINUTE,new TimeDuration(1, Calendar.MINUTE)));
        for (Pair<Integer, TimeDuration> pair : quantitiesToTest) {
            int quantity = pair.getFirst();
            TimeDuration timeDuration = pair.getLast();
            if (numberOfSeconds >= quantity) {
                int numberOf = numberOfSeconds / quantity;
                String timeUnitDescription = timeDuration.getTimeUnit().getDescription();
                if (numberOf == 1){
                    timeUnitDescription = timeUnitDescription.substring(0, timeUnitDescription.length() - 1);
                }
                builder.append(numberOf).append(" ").append(timeUnitDescription);
                numberOfSeconds = numberOfSeconds % quantity;
                numberOfUnits++;
                if (numberOfUnits >= maxNumberOfUnits) {
                    break;
                } else {
                    builder.append(" ");
                }
            }
        }
        if (numberOfUnits < 2 && numberOfSeconds > 0) {
            String timeUnitDescription = TimeDuration.getTimeUnitDescription(Calendar.SECOND);
            if (numberOfSeconds == 1){
                timeUnitDescription = timeUnitDescription.substring(0, timeUnitDescription.length() - 1);
            }
            builder.append(numberOfSeconds).append(" ").append(timeUnitDescription);
        }
        return builder.toString();
    }

    private DateTimeFormatter getDateFormatForCurrentUser(DateTimeFormatGenerator.Mode dateFormatMode, DateTimeFormatGenerator.Mode timeFormatMode){
        return DateTimeFormatGenerator.getDateFormatForUser(dateFormatMode, timeFormatMode, userService.getUserPreferencesService(), threadPrincipalService.getPrincipal());
    }
}
