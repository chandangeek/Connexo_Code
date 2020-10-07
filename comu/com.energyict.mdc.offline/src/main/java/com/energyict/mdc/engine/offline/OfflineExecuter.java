package com.energyict.mdc.engine.offline;


import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.JSONTypeMapperProvider;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningOfflineComServerImpl;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.ComJobState;
import com.energyict.mdc.engine.impl.core.offline.OfflineActionExecuter;
import com.energyict.mdc.engine.impl.core.offline.OfflineActions;
import com.energyict.mdc.engine.impl.core.offline.OfflineComServerProperties;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.OfflineComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.RemoteJSONTypeMapperProvider;
import com.energyict.mdc.engine.impl.core.remote.RemoteProperties;
import com.energyict.mdc.engine.offline.core.ComServerEventMonitoringClientLauncher;
import com.energyict.mdc.engine.offline.core.FormatProvider;
import com.energyict.mdc.engine.offline.core.OfflineLogging;
import com.energyict.mdc.engine.offline.core.StoringThread;
import com.energyict.mdc.engine.offline.core.exception.SyncException;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.persist.BusinessDataPersister;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.swing.*;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2014 - 14:46
 */
public class OfflineExecuter implements OfflineActionExecuter {

    private static final String LOGGERNAME = "ComServerMobileLogFile";
    private static final String COMSERVER_PROTOCOLLOGDIR = "log/protocol";
    private static final String COMSERVER_GLOBALLOGDIR = "log/global";
    private static final String COMSERVER_GLOBALLOGFILE = COMSERVER_GLOBALLOGDIR + "\\globallog.txt";

    private User comServerUser;
    private final RunningComServerImpl.ServiceProvider serviceProvider;

    /**
     * Used to persist business data (e.g. comserver, comjob, ...) to a file (using JSON marshalling)
     */
    private final BusinessDataPersister businessDataPersister;

    /**
     * List of actions that need to be executed by this working thread.
     * The UI can add a new action to this list if the user triggers something in the UI.
     * <p/>
     * This is a blocking queue that is thread safe.
     */
    private final BlockingQueue<OfflineActions> actions = new ArrayBlockingQueue<>(10);

    /**
     * Used to query business objects from the database over a web socket, using JSON serialization
     */
    private ComServerDAO remoteComServerDAO;

    /**
     * The DAO that is used by the comserver that runs completely offline.
     * Every time the user chooses to execute a comjob in the UI, this comjob and it's relevant information is added in the DAO.
     * Note that this DAO also holds the collected data and comsession information that is created while executing the comjob.
     */
    private OfflineComServerDAOImpl offlineComServerDAO;

    /**
     * Used to append logging in the proper logfiles
     */
    private OfflineLogging offlineLogging;

    /**
     * The comserver business object configured in EIServer, representing this application.
     * Note that we will overrule its 'schedulingInterPollDelay' to 1 second.
     * This is the first object that will be requested from the EIServer database.
     */
    private OfflineComServer offlineComServer;

    /**
     * Indicates if the application is busy storing collected data
     */
    private AtomicBoolean busyStoringCollectedData = new AtomicBoolean(false);

    /**
     * The first (and only) comport of the comserver business object
     * It is this comport that is checked for executable comjobs.
     */
    private OutboundComPort comPort;

    /**
     * The comserver that runs offline and executes comjobs
     */
    private RunningComServer runningComServer;
    private ComServerEventMonitoringClientLauncher eventMonitoringClient;
    /**
     * Thread that stores executed models
     */
    private StoringThread storingThread;
    private boolean storingEndedClean = true;

    public OfflineExecuter(BusinessDataPersister businessDataPersister, RunningComServerImpl.ServiceProvider serviceProvider) {
        this.businessDataPersister = businessDataPersister;
        this.serviceProvider = serviceProvider;
        initProviders();
    }

    public void init() {
        offlineComServer = getBusinessDataPersister().loadComServer();  //Can be null if the file doesn't exist yet
        initLogMechanism();
    }

    private void initProviders() {
        FormatProvider.instance.set(new DefaultFormatProvider());
        JSONTypeMapperProvider.instance.set(new RemoteJSONTypeMapperProvider());
    }

    public RunningComServer getRunningComServer() {
        if (runningComServer == null) {
            startRunningComServer();
        } else {
            assureEventMonitoringWebSocketConnectionIsStillValid();
        }
        return runningComServer;
    }

    private void assureEventMonitoringWebSocketConnectionIsStillValid() {
        if (!eventMonitoringClient.webSocketConnectionStillValid()) {
            eventMonitoringClient.startEventMonitoringClient(); // Restart event monitoring client
        }

    }

    public boolean comServerIsStarted() {
        return runningComServer != null;
    }

    /**
     * Start the running comserver that will execute the pending comjobs while being completely offline.
     * This is the normal online comserver, that uses an offline DAO that already holds all necessary information.
     * <p/>
     * Note that the polling interval is overruled here to 1 seconds, so the pending comjob is executed immediately.
     */
    private void startRunningComServer() {
        if (offlineComServer != null) {
            runningComServer = new RunningOfflineComServerImpl(offlineComServer, getOfflineComServerDAO(), serviceProvider);
            runningComServer.start();

            eventMonitoringClient = new ComServerEventMonitoringClientLauncher(this);
            eventMonitoringClient.startEventMonitoringClient();
            getOfflineComServerDAO().notifyEventMonitorIsStarted();
        } else {
            throw new ApplicationException("Cannot start a running ComServer, the offline comserver business object is null");
        }
    }

    private OfflineComServerDAOImpl getOfflineComServerDAO() {
        if (offlineComServerDAO == null) {
            offlineComServerDAO = new OfflineComServerDAOImpl(offlineComServer, new OfflineComServerDaoServiceProvider(), comServerUser, this);
        }
        return offlineComServerDAO;
    }

    /**
     * Start the remote ComServer DAO that can query all necessary business objects using JSON serializing a web socket
     * This is the exact same component that is used in the remote ComServer.
     */
    private void startRemoteComServerDAO() {
        Properties properties = OfflineComServerProperties.getInstance().getProperties();
        RemoteProperties remoteProperties = new RemoteProperties(properties);
        String remoteQueryApiUrl = remoteProperties.getRemoteQueryApiUrl();
        if (remoteQueryApiUrl == null) {
            throw new ApplicationException("Cannot start remote communication, missing remoteQueryApiUrl in properties!");
        }
        remoteComServerDAO = new RemoteComServerDAOImpl(remoteProperties, new RemoteComServerDaoServiceProvider());
        try {
            remoteComServerDAO.start();
        } catch (ApplicationException e) {
            remoteComServerDAO = null;  //reset the instance if it was not started correctly
            throw e;
        }
    }

    /**
     * Stop the remote Comserver DAO
     */
    public void stopRemoteComServerDAO() {
        stopRemoteComServerDAO(true);
    }

    /**
     * @param clean indicating if there's still a connection available at this point.
     */
    public void stopRemoteComServerDAO(boolean clean) {
        try {
            if (remoteComServerDAO != null) {
                if (clean) {
                    ((RemoteComServerDAOImpl) remoteComServerDAO).disconnectRemoteComServer(offlineComServer);
                }
                remoteComServerDAO.shutdown();
            }
        } finally {
            remoteComServerDAO = null;
        }
    }

    /**
     * The remote DAO that uses the websocket of an online comserver to reach the EIServer database.
     * It is up to the developer to catch and handle all DataAccessExceptions that come from the usage of this DAO.
     * A DataAccessExceptions indicates something went wrong while communicating, or the connection was lost.
     */
    public ComServerDAO getRemoteComServerDAO() {
        if (remoteComServerDAO == null) {
            startRemoteComServerDAO();
        }
        return remoteComServerDAO;
    }

    private String getSystemName() {
        String systemName = System.getProperty(ComServer.SYSTEM_NAME_PROPERTY);
        if (systemName == null) {
            systemName = HostName.getCurrent();
        }
        return systemName;
    }

    private void initLogMechanism() {
        // close first...
        if (this.offlineLogging != null) {
            this.offlineLogging.closeGlobalLogging();
        }

        // Instantiate log mechanism
        this.offlineLogging = new OfflineLogging(this, LOGGERNAME);
        // Create log directories if not exist
        this.offlineLogging.createIndividualLogDirectories(this.businessDataPersister.getSystemDir() + "/" + COMSERVER_PROTOCOLLOGDIR);
        this.offlineLogging.createGlobalLogDirectory(this.businessDataPersister.getSystemDir() + "/" + COMSERVER_GLOBALLOGDIR);
        // init logging
        this.offlineLogging.initGlobalLogging(getLogLevel(), 500000, this.businessDataPersister.getSystemDir() + "/" + COMSERVER_GLOBALLOGFILE);
    }

    public Level getLogLevel() {
        Level level;
        if (offlineComServer == null) {
            level = Level.ALL;
        } else {
            level = parseComServerLogLevel(offlineComServer.getServerLogLevel());
        }
        return level;
    }

    protected Level getProtocolLogLevel() {
        Level level;
        if (offlineComServer == null) {
            level = Level.ALL;
        } else {
            level = parseComServerLogLevel(offlineComServer.getCommunicationLogLevel());
        }
        return level;
    }

    private Level parseComServerLogLevel(ComServer.LogLevel logLevel) {
        switch (logLevel) {
            case ERROR:
                return Level.SEVERE;
            case WARN:
                return Level.WARNING;
            case INFO:
                return Level.INFO;
            case DEBUG:
                return Level.FINEST;
            case TRACE:
            default:
                return Level.ALL;
        }
    }

    public void log(Level level, String str) {
        getLogging().log(level, str);
    }

    public BusinessDataPersister getBusinessDataPersister() {
        return businessDataPersister;
    }

    public OfflineLogging getLogging() {
        return offlineLogging;
    }

    public boolean isComServerObjectAvailable() {
        return (offlineComServer != null);
    }

    /**
     * @throws DataAccessException in case of a communication problem. It is handled in the ConfigPnl (that triggered this action)
     */
    private void getThisComServer() throws SyncException, DataAccessException {
        ComServer comServer = getRemoteComServerDAO().getComServer(getSystemName());

        if (comServer == null) {
            throw new ApplicationException("No ComServer with name '" + getSystemName() + "' is configured in EIServer");
        }

        if (comServer instanceof OfflineComServer) {
            offlineComServer = ((OfflineComServer) comServer);
        } else {
            throw new SyncException("Error while reading the ComServer business object. Expected an OfflineComServer but received: " + comServer.getClass().getSimpleName());
        }
    }

    /**
     * Query the comserver business object, do not reset anything
     */
    public void warmBoot() throws SyncException, IOException, DataAccessException {

        getThisComServer();

        //Store object as JSON string in file
        getBusinessDataPersister().saveComServer(offlineComServer);
        initLogMechanism();
    }

    /**
     * Query the comserver shadow and reset it's comtasks (busy ==> pending)
     */
    public void coldBoot() throws SyncException, IOException, DataAccessException {
        getThisComServer();

        //Reset the busy comtaskexecutions back to pending so they can be picked up again
        remoteComServerDAO.releaseInterruptedTasks(getComPort());

        //Store comserver object as JSON string in file
        getBusinessDataPersister().saveComServer(offlineComServer);
        initLogMechanism();
    }

    public OutboundComPort getComPort() {
        if (comPort == null) {
            if (offlineComServer == null) {
                return null;
            }

            List<ComPort> comPorts = offlineComServer.getComPorts();
            if (comPorts.size() != 1) {
                throw new ApplicationException("Error: expected ComServer '" + offlineComServer.getName() + "' to have only one comport, but it has " + comPorts.size());
            }

            ComPort firstComPort = comPorts.get(0);
            if (firstComPort instanceof OutboundComPort) {
                this.comPort = (OutboundComPort) firstComPort;
            } else {
                throw new ApplicationException("Inbound comport '" + firstComPort.getName() + "' is not supported, only outbound comports are supported.");
            }
        }
        return comPort;
    }

    public ComServer getOfflineComServer() {
        return offlineComServer;
    }

    public String getComServerInfo() {
        if (offlineComServer == null) {
            return null;
        } else {
            String comPortDisplayString = "null";
            if (getComPort() != null) {
                comPortDisplayString = getComPort().getName();
            }
            return getComServerName() + "." + comPortDisplayString;
        }
    }

    public String getComServerName() {
        if (offlineComServer != null) {
            return offlineComServer.getName();
        } else {
            return getSystemName();
        }
    }

    /**
     * Provide the model to the DAO. The running comserver will pick up the comjob and execute it.
     */
    public void executeComJob(ComJobExecutionModel model) {
        ComChannel.abortCommunication.set(false);   // Make sure aborting of communication is disabled
        getRunningComServer();  //Making sure it is started
        model.setState(ComJobState.Executing);
        getOfflineComServerDAO().setComJobExecutionModel(model); //Provide all the necessary information about the comjob to the DAO
    }

    /**
     * Main thread of the ComServer. Receives events from the ProtocolReaders and
     * asyncdatabaseposter. Commserver dispatches tasks to the ProtocolReaders. This method must be called after instantiation of the
     * CommServer object.
     * CommServer calls the schedule method in TaskScheduler and gets tasks to execute for com ports. Then, the tasks are dispatched to ProtocolReaders.
     * CommServer main thread receives notification from a ProtocolReader when it finishes
     * communication session. If this communication session was successful, CommServer requests a <I>MeterCollection</I>
     * which contains results of the communication session. The database channel tables are updated with the data. <BR>
     * For each record inserted in the channel tables, the tables EISRTU and EISCHANNEL 'lastreading' field
     * is updates with the timestamp of the record inserted in the channel tables.
     */
    synchronized public void process() {
        try {
            serviceProvider.threadPrincipalService().runAs(comServerUser, () -> doProcess());
        } catch (Throwable e) {
            getLogging().getLogger().severe("Uncaught exception - OfflineExecuter exiting! " + e.getMessage());
            e.printStackTrace();
            throw e;        //Uncaught exception, will be handled by the ExceptionDialog
        }
    }

    /**
     * When used in cooperation with a GUI application that implements the CommServerEventListener, startGUI is called
     * when the CommServer has initialized all objects and started.
     */
    public void startGUI() {
        //This will also add an action to the working thread to fetch the pending comtasks from EIServer
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                UiHelper.getMainWindow().comServerMobileStarted();
            }
        });
    }

    private void doProcess() {
        startGUI();
        performActions();
        getLogging().getLogger().info("Shutdown successful");
    }

    private void performActions() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                performAction();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void performAction() throws InterruptedException {
        OfflineActions action = actions.take();    //Blocking queue, waiting until a next action is available
        switch (action) {
            case QueryPendingComJobs:
                queryPendingComJobs();
                break;
            case StoreCollectedData:
                storeCollectedData();
                break;
            case ComJobIsFinished:
                finishComJob();
                break;

            default:
                getLogging().getLogger().severe("Received unknown action '" + action.name() + "', skipping.");
                break;
        }
    }

    private void queryPendingComJobs() throws InterruptedException {
        UiHelper.getMainWindow().getOfflineWorker().getTaskManager().queryPendingComJobs(UiHelper.getMainWindow().getQueryDate());
        updateOfflineReadinTypes();
    }

    private void updateOfflineReadinTypes() {
        Set<ReadingType> onlineReadingTypes = getComJobModels().stream().flatMap(model -> model.getDevice().getRegisters().stream().map(Register::getReadingType)).collect(Collectors.toSet());
        if (!onlineReadingTypes.isEmpty()) {
            List<ReadingType> availableReadingTypes = serviceProvider.meteringService().getAvailableReadingTypes();
            List<String> availableReadingTypeCodes =
                    availableReadingTypes.parallelStream()
                            .map(ReadingType::getMRID)
                            .collect(Collectors.toList());
            onlineReadingTypes.parallelStream()
                    .filter(readingTypePair -> !availableReadingTypeCodes.contains(readingTypePair.getMRID()))
                    .forEach(readingType -> serviceProvider.meteringService().createReadingType(readingType.getMRID(), readingType.getAliasName()));
        }
    }

    private void finishComJob() {
        ComJobExecutionModel finishedJobModel = null;
        try {
            finishedJobModel = getOfflineComServerDAO().getFinishedQueue().take(); // TODO in case of any unexpected error (which does not trigger dao method executionFailed) the application hangs?
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (finishedJobModel != null) {
            finishedJobModel.setState(ComJobState.AwaitingStore);
            updateGUI(finishedJobModel);
        }
    }

    private void storeCollectedData() {
        if (busyStoringCollectedData.get()) {
            abortStoringCollectedData();
        } else {
            setBusyStoringCollectedData(true);

            //Launch a new thread that starts storing all executed models
            storingThread = null;
            getStoringThread().setModels(getComJobModels());
            getStoringThread().start();
        }
    }

    public void abortStoringCollectedData() {
        //Notify the thread to stop the storing
        getStoringThread().setStoring(false);

        //Wait for the storing thread, until the current model was stored (or timeout 120 sec)
        long timeoutMoment = System.currentTimeMillis() + 120000;
        while (busyStoringCollectedData.get()) {
            if (System.currentTimeMillis() < timeoutMoment) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                getLogging().log(Level.SEVERE, "Could not interrupt storing process properly, still busy after 120 seconds... Moving on.");
                break;
            }
        }
        UiHelper.getMainWindow().setStoringWasCanceled(true);
        storingThread.interrupt();
        storingThread = null;
    }

    private StoringThread getStoringThread() {
        if (storingThread == null) {
            storingThread = new StoringThread(serviceProvider, this);
        }
        return storingThread;
    }

    private List<ComJobExecutionModel> getComJobModels() {
        return UiHelper.getMainWindow().getOfflineWorker().getTaskManager().getComJobModels();
    }

    public void endStoringProgressInGUI() {
        UiHelper.getMainWindow().endStoringProgress();
        UiHelper.getMainWindow().invokeUpdateConfigPanel();
    }

    public void updateGUI(final ComJobExecutionModel model) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UiHelper.startWaitCursor(); // This is a heavy call in case the model contains a lot of data / logging, thus added wait cursor
                    UiHelper.getMainWindow().updateTasks(model);
                } finally {
                    UiHelper.stopWaitCursor();
                }
            }
        });
    }

    /**
     * When the user triggers something in the UI, the proper action is added in the queue, it will be picked up by the working thread
     */
    public void addAction(OfflineActions action) {
        actions.offer(action);
    }

    public void setBusyStoringCollectedData(boolean storing) {
        busyStoringCollectedData.set(storing);
    }

    public boolean isStoringEndedClean() {
        return storingEndedClean;
    }

    public void setStoringEndedClean(boolean endedClean) {
        this.storingEndedClean = endedClean;
    }

    public User getComServerUser() {
        return comServerUser;
    }

    public void setComServerUser(User comServerUser) {
        this.comServerUser = comServerUser;
    }

    private class ComServerDaoServiceProvider implements ComServerDAOImpl.ServiceProvider {

        @Override
        public Thesaurus thesaurus() {
            return serviceProvider.thesaurus();
        }

        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return serviceProvider.protocolPluggableService();
        }

        public DeviceMessageSpecificationService deviceMessageSpecificationService() {
            return serviceProvider.deviceMessageSpecificationService();
        }

        @Override
        public TopologyService topologyService() {
            return serviceProvider.topologyService();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return serviceProvider.engineConfigurationService();
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return serviceProvider.connectionTaskService();
        }

        @Override
        public PriorityComTaskService priorityComTaskService() {
            return serviceProvider.priorityComTaskService();
        }

        @Override
        public CommunicationTaskService communicationTaskService() {
            return serviceProvider.communicationTaskService();
        }

        @Override
        public OrmService ormService() {
            return serviceProvider.ormService();
        }

        @Override
        public DeviceService deviceService() {
            return serviceProvider.deviceService();
        }

        @Override
        public RegisterService registerService() {
            return serviceProvider.registerService();
        }

        @Override
        public LoadProfileService loadProfileService() {
            return serviceProvider.loadProfileService();
        }

        @Override
        public LogBookService logBookService() {
            return serviceProvider.logBookService();
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return serviceProvider.deviceMessageService();
        }

        @Override
        public TransactionService transactionService() {
            return serviceProvider.transactionService();
        }

        @Override
        public EngineService engineService() {
            return serviceProvider.engineService();
        }

        @Override
        public UserService userService() {
            return serviceProvider.userService();
        }

        @Override
        public EventService eventService() {
            return serviceProvider.eventService();
        }

        @Override
        public IdentificationService identificationService() {
            return serviceProvider.identificationService();
        }

        @Override
        public FirmwareService firmwareService() {
            return serviceProvider.firmwareService();
        }

        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return serviceProvider.deviceConfigurationService();
        }

        @Override
        public SecurityManagementService securityManagementService() {
            return serviceProvider.securityManagementService();
        }
    }

    private class RemoteComServerDaoServiceProvider implements RemoteComServerDAOImpl.ServiceProvider {

        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return serviceProvider.engineConfigurationService();
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return serviceProvider.threadPrincipalService();
        }

    }

    private class OfflineComServerDaoServiceProvider implements OfflineComServerDAOImpl.ServiceProvider {

        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return serviceProvider.engineConfigurationService();
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return serviceProvider.threadPrincipalService();
        }
    }

}