package com.energyict.mdc.engine.monitor.impl.rest.resource;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.*;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.monitor.OperationalStatistics;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.StatusService;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

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

    public enum LocalOrRemote {
        LOCAL, REMOTE;

        @Override
        public String toString() {
            String name = super.toString();
            return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
    }

    private StatusService statusService;
    private EngineConfigurationService engineConfigurationService;
    private ThreadPrincipalService threadPrincipalService;
    private UserService userService;

  //  private ComServerMonitor monitor;
  //  private ComServerDAO comServerDAO;
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
//        ComServer comServer =  comServerDAO.getThisComServer();

//        JSONObject result = new JSONObject();
//        result.put("rootName", ((ServerComServer) comServer).getRootName());
//        result.put("serverId", comServer.getId());
//        result.put("localOrRemote", comServer.isRemote() ? LocalOrRemote.REMOTE : LocalOrRemote.LOCAL);
//        if (Clocks.getAppServerClock() != null) {
//            result.put("currentDate", format(Clocks.getAppServerClock().now()));
//        }
//        OperationalStatistics operationalStatistics = monitor.getOperationalStatistics();
//        if (operationalStatistics != null) {
//            Date startTime = operationalStatistics.getStartTimestamp();
//            if (startTime != null) {
//                String startedAndDurationText = format(startTime);
//                TimeDuration duration = operationalStatistics.getRunningTime();
//                if (duration != null) {
//                    startedAndDurationText += " (" + formatDuration(duration) + ")";
//                }
//                result.put("started", startedAndDurationText);
//            }
//        }
//        result.put("serverName", comServer.getName());

        ComServerStatus status = statusService.getStatus();
        OperationalStatistics operationalStatistics= status.getOperationalStatistics();

        JSONObject result = new JSONObject();
        result.put("rootName", "No rootName available in Connexo");
        result.put("serverId", status.getComServerId());
        result.put("localOrRemote", status.getComServerType().toString());
        result.put("currentDate", "2525/05/25 25:25:25");
        if (status.isRunning() && operationalStatistics != null) {
            Date startTime = operationalStatistics.getStartTimestamp();
            if (startTime != null) {
                String startedAndDurationText = format(startTime);
                TimeDuration duration = operationalStatistics.getRunningTime();
                if (duration != null) {
                    startedAndDurationText += " (" + formatDuration(duration) + ")";
                }
                result.put("started", startedAndDurationText);
            }
        }
        result.put("serverName", status.getComServerName());

        setLastConVerted(result);
        return this;
    }

    public JSonConverter convertGeneralInfo() throws JSONException {
//        ComServer comServer =  comServerDAO.getThisComServer();

//        JSONObject result = new JSONObject();
//        TimeDuration changesInterPollDelay = comServer.getChangesInterPollDelay();
//        if (changesInterPollDelay != null) {
//            JSONObject jsDuration = new JSONObject();
//            jsDuration.put("count", changesInterPollDelay.getCount());
//            jsDuration.put("time-unit", TimeDuration.getTimeUnitDescription(changesInterPollDelay.getTimeUnitCode()));
//            result.put("changeDetectionFrequency", jsDuration);
//        }
//        result.put("changeDetectionNextRun", format(monitor.nextPollingForChanges()));
//        TimeDuration schedulingInterPollDelay = comServer.getSchedulingInterPollDelay();
//        if (schedulingInterPollDelay != null) {
//            JSONObject jsDuration = new JSONObject();
//            jsDuration.put("count", schedulingInterPollDelay.getCount());
//            jsDuration.put("time-unit", TimeDuration.getTimeUnitDescription(schedulingInterPollDelay.getTimeUnitCode()));
//            result.put("pollingFrequency", jsDuration);
//        }

        JSONObject result = new JSONObject();
        JSONObject jsDuration = new JSONObject();
        jsDuration.put("count", 5);
        jsDuration.put("time-unit", "minutes");
        result.put("changeDetectionFrequency", jsDuration);

        result.put("changeDetectionNextRun", "2626/06/26 26:26:26");

        jsDuration = new JSONObject();
        jsDuration.put("count", 3);
        jsDuration.put("time-unit", "hours");
        result.put("pollingFrequency", jsDuration);


        setLastConVerted(result);
        return this;
    }

    public synchronized JSonConverter convertRunningInfo() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("numberOfEvents", "a lot");
        result.put("maxMemory", "" + Runtime.getRuntime().maxMemory() / K / K);
        result.put("usedMemory", "" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / K / K));
        result.put("unit", "MB");

        setLastConVerted(result);
        return this;
    }

    public synchronized JSonConverter convertConnectedRemoteServers(boolean active) throws JSONException {
//        ComServer comServer =  comServerDAO.getThisComServer();

        List<JSONObject> remotes = new ArrayList<>();
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

            for (int i=0; i< 2; i++) {
                JSONObject server = new JSONObject();
                server.put("id", i);
                server.put("name", "Remote Communication Server" + i);
                server.put("active", i%2 == 0 );
                server.put("lastSeen", "2323/03/23 23:23:23");

                remotes.add(server);
            }
//        }
        setConvertedArray(remotes.toArray(new JSONObject[remotes.size()]));
        return this;
    }

    public synchronized JSonConverter convertCommunicationPorts(boolean active) throws JSONException {
        List<JSONObject> comPorts = new ArrayList<>();
//        for (ComPort each : monitor.getComServer().getComPorts()) {
//
//            ComPortServerProcess processForEach = monitor.getServerProcess(each);
//            boolean activeProcess = (processForEach != null);
//
//            if (active == activeProcess) {
//                JSONObject jsonComPort = new JSONObject();
//                jsonComPort.put("id", each.getId());
//                jsonComPort.put("name", each.getName());
//                jsonComPort.put("description", each.getDescription());
//                jsonComPort.put("inbound", each.isInbound());
//                if (activeProcess){
//                    jsonComPort.put("lastSeen", format(processForEach.getLastPortPollDate()));
//                    jsonComPort.put("threads", processForEach.getThreadCount());
//                }
//                comPorts.add(jsonComPort);
//            }
//        }
        for (int i=0; i<5; i++) {
            JSONObject jsonComPort = new JSONObject();
            jsonComPort.put("id",1000 + i);
            jsonComPort.put("name", "ComPort " + 1000 + i);
            jsonComPort.put("description", "Description for " + 1000 + i);
            jsonComPort.put("inbound", i%2 == 0);
            jsonComPort.put("lastSeen", "2323/03/23 23:23:23");
            jsonComPort.put("threads", i);

            comPorts.add(jsonComPort);
        }
        setConvertedArray(comPorts.toArray(new JSONObject[comPorts.size()]));
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
  //      CollectedDataStorageStatistics statistics = monitor.getCollectedDataStorageStatistics();
        JSONObject result = new JSONObject();
        result.put("time", "2525/05/25 25:25:25");
        result.put("load", 88);
        result.put("threads", 25);
        result.put("priority", 8);
        result.put("capacity", 99);
        result.put("currentSize", "big");
        setLastConVerted(result);
        return this;
    }

    public synchronized JSonConverter convertThreadsInUse() throws JSONException {
        int threadCount = 0;
        int activeThreadCount = 0;

//        for (ComPort each: monitor.getComServer().getComPorts()){
//            if (each.isInbound()){
//                continue;
//            }
//            Integer threadCountForEach = getThreadCount(each);
//            if (threadCountForEach != null)
//                threadCount += threadCountForEach;
//
//            Integer activeThreadCountForEach = getActiveThreadCount(each);
//            if (activeThreadCountForEach != null)
//                activeThreadCount += activeThreadCountForEach;
//        }

        JSONObject threadsInUse = new JSONObject();
        threadsInUse.put("name", "inUse");
        threadsInUse.put("data", activeThreadCount);

        JSONObject threadsNotInUse = new JSONObject();
        threadsNotInUse.put("name", "notInUse");
        threadsNotInUse.put("data", threadCount - activeThreadCount);

        JSONObject result = new JSONObject();
        result.put("threads", new JSONObject[] {threadsInUse, threadsNotInUse});
        setLastConVerted(result);
        return this;
    }

    private JSONObject[] getComPortPools(Boolean active) throws JSONException{
//        Map<ComPortPool, List<ComPort>> activeMapping = getComPortPoolMapping(active == null ? null : true);
//        HashSet<ComPortPool> notInactiveSet = new HashSet<>();
//        if (active!= null && !active){
//            for (ComPortPool each: getComPortPoolMapping(false).keySet()){
//                if (!activeMapping.containsKey(each)){
//                    notInactiveSet.add(each);
//                }
//            }
//        }
//        Set<ComPortPool> comPortPools;
//        if (active == null || active){
//            comPortPools = activeMapping.keySet();
//        }else{
//            comPortPools = notInactiveSet;
//        }
//        List<JSONObject>  jsonPools = new ArrayList<>();
//        for (ComPortPool each: comPortPools){
//            JSONObject jsonPool = new JSONObject();
//            jsonPool.put("id", each.getId());
//            jsonPool.put("name", each.getName());
//            jsonPool.put("description", each.getDescription());
//            jsonPool.put("inbound", each.isInbound());
//            jsonPool.put("active", active);
//            if (activeMapping.containsKey(each)){
//                List<ComPort> comPorts = activeMapping.get(each);
//                jsonPool.put("ports", comPorts.size());
//                int threadCount = 0;
//                for (ComPort comPort: comPorts){
//                    Integer comPortThreadCount  =  getThreadCount(comPort);
//                    if (comPortThreadCount != null)
//                        threadCount += comPortThreadCount;
//                }
//                jsonPool.put("threads", threadCount);
//            }
//            jsonPools.add(jsonPool);
//        }
        List<JSONObject>  jsonPools = new ArrayList<>();
        for (int i=0; i < 7; i++){
            JSONObject jsonPool = new JSONObject();
            jsonPool.put("id", i);
            jsonPool.put("name", "Comport Pool " + i);
            jsonPool.put("description", "Description for Comport Pool " + i);
            jsonPool.put("inbound", i%2 == 0);
            jsonPool.put("active", active);
            jsonPool.put("ports", "3");
            jsonPool.put("threads", 1);

            jsonPools.add(jsonPool);
        }
        return jsonPools.toArray(new JSONObject[jsonPools.size()]);
    }
      /*
    private Map<ComPortPool, List<ComPort>> getComPortPoolMapping(Boolean active){
        List<ComPortPool> comPortPools = comServerDAO.getComPortPools();

        Map<ComPortPool, List<ComPort>> mapping = new HashMap<>();
        for (ComPort each : monitor.getComServer().getComPorts()) {
            ComPortPool pool;
            if (each.isInbound()){
                pool = ((InboundComPort) each).getComPortPool();
            }else{
                pool = findOutboundComportPool(comPortPools, each);
            }
            if (pool != null && (active == null || active == isActiveProcess(each))) {
                List<ComPort> ports = mapping.get(pool);
                if (ports == null){
                    ports = new ArrayList<>();
                    mapping.put(pool, ports);
                }
                ports.add(each);
            }
        }
        return mapping;
    }

    private ComPortPool findOutboundComportPool(List<ComPortPool> possible, ComPort comPort){
        for (ComPortPool each: possible) {
            if (each.isInbound()){
                continue;
            }
            List<OutboundComPort> comports = ((OutboundComPortPool) each).getComPorts();
            for (OutboundComPort outboundComPort: comports){
                 if (outboundComPort.getId() == comPort.getId()){
                     return each;
                 }
            }
        }
        return null;
    }

    private boolean isActiveProcess(ComPort comPort){
        return monitor.getServerProcess(comPort) != null;
    }

    private Integer getThreadCount(ComPort comPort){
        if (comPort.isInbound()){
            ComPortListener comPortListener = (ComPortListener) monitor.getServerProcess(comPort);
            if (comPortListener != null){
                return comPortListener.getThreadCount();
            }
        } else{
            ScheduledComPort scheduledComPort = (ScheduledComPort) monitor.getServerProcess(comPort);
            if (scheduledComPort != null){
                return scheduledComPort.getThreadCount();
            }
        }
        return null;
    }

    private Integer getActiveThreadCount(ComPort comPort){
        if (comPort.isInbound()){
            ComPortListener comPortListener = (ComPortListener) monitor.getServerProcess(comPort);
            if (comPortListener != null){
                return comPortListener.getThreadCount();
            }
        } else{
            ScheduledComPort scheduledComPort = (ScheduledComPort) monitor.getServerProcess(comPort);
            if (scheduledComPort != null){
                return scheduledComPort.getActiveThreadCount();
            }
        }
        return null;
    }
    */

    private String format(Date date){
        return date!=null ? getLongDateFormatForCurrentUser().format(date.toInstant()) : "";
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
        for (Pair pair : quantitiesToTest) {
            int quantity = (Integer)pair.getFirst();
            int timeUnit = (Integer)pair.getLast();
            if (numberOfSeconds >= quantity) {
                builder.append( numberOfSeconds / quantity + " " + TimeDuration.getTimeUnitDescription(timeUnit));
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
            builder.append( numberOfSeconds + " " + TimeDuration.getTimeUnitDescription(Calendar.SECOND));
        }
        return builder.toString();
    }


    private DateTimeFormatter getLongDateFormatForCurrentUser(){
        UserPreferencesService preferencesService = this.userService.getUserPreferencesService();
        Principal principal = threadPrincipalService.getPrincipal();
        String dateFormat = "HH:mm:ss EEE dd MMM ''yy"; // default backend date format
        Locale locale = Locale.ENGLISH;
        if (principal instanceof User){
            Optional<UserPreference> dateFormatPref = preferencesService.getPreferenceByKey((User) principal, FormatKey.LONG_DATETIME);
            if (dateFormatPref.isPresent()){
                dateFormat = dateFormatPref.get().getFormatBE();
            }
            locale = ((User) principal).getLocale().orElse(locale);
        }
        DateTimeFormatterBuilder formatterBuilder = new DateTimeFormatterBuilder();
        return formatterBuilder.appendPattern(dateFormat).toFormatter(locale);
    }


}