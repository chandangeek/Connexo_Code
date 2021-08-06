package com.energyict.mdc.engine.offline;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.TableSpecs;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.offline.core.DefaultTranslatorProvider;
import com.energyict.mdc.engine.offline.core.FormatProvider;
import com.energyict.mdc.engine.offline.core.RegistryConfiguration;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.registry.ComServerMobileGuiJConfigEntries;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.dialogs.ExceptionDialog;
import com.energyict.mdc.engine.offline.gui.dialogs.LoggingExceptionDialog;
import com.energyict.mdc.engine.offline.gui.util.EisIcons;
import com.energyict.mdc.engine.offline.gui.windows.LogonDlg;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.LibraryType;
import com.energyict.mdc.upl.io.ModemType;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.io.SocketService;
import com.energyict.mdc.upl.meterdata.LogBook;
import com.energyict.mdc.upl.meterdata.identifiers.*;
import com.energyict.obis.ObisCode;
import com.jidesoft.plaf.LookAndFeelFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.offline.OfflineEngine",
        service = {OfflineEngineService.class, TranslationKeyProvider.class},
        property = {"name=" + OfflineEngine.COMPONENTNAME},
        immediate = true)
public class OfflineEngine implements OfflineEngineService, TranslationKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(OfflineEngine.class.getName());

    private static OfflineFrame mainFrame;

    public OfflineEngine() {
    }

    /**
     * Start the offline comserver application
     */
    @Activate
    public final void activate(BundleContext bundleContext) {
        FormatProvider.instance.set(new DefaultFormatProvider());
        TranslatorProvider.instance.set(new DefaultTranslatorProvider(OfflineThesaurus.from(getKeys())));
        bundleContext.registerService(TranslationKeyProvider.class, this, null);
        startUIThread();
    }

    private void startUIThread() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    startUI();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startUI() throws InvocationTargetException, InterruptedException {
        LoggingExceptionDialog dialog = new LoggingExceptionDialog();
        dialog.setIconImage(((ImageIcon) EisIcons.EISERVER_ICON).getImage());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);   //Not allowed to just close, force the user to exit in case of exception at this point.
        Thread.setDefaultUncaughtExceptionHandler(dialog);  //Don't change this, its name is used as initial vector for encryption/decryption!

        // Set the windows Look & Feel
        com.jidesoft.utils.Lm.verifyLicense("Energy ICT", "EIServer", "df3o1B.Aze:sq7r6fOi:n9xHSRMHfcJ");
        LookAndFeelFactory.installDefaultLookAndFeelAndExtension();

        // To handle the registry configuration
        RegistryConfiguration.createDefault(OfflineFrame.class, ComServerMobileGuiJConfigEntries.getMap());

        //Create the main form, don't show it yet
        createUI();

        //Login
        LogonDlg logonDlg = new LogonDlg(mainFrame, true);
        boolean canceled, logonOK;
        do {
            logonOK = logonDlg.logon();
            canceled = logonDlg.isCanceled();
        } while (!canceled && !logonOK);

        if (!canceled) {
            //Now show the main form
            showUI();

            //From now on closing the 'Unhandled Exception" window is possible
            dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

            //After that, start the backend on the main thread
            mainFrame.startOfflineExecuter();
        } else {
            OfflineEngine.exitSystem(0);
        }
    }


    /**
     * Create the main form, don't show it yet. This is invoked on the java UI thread.
     */
    private void createUI() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    mainFrame = new OfflineFrame(new RunningComServerServiceProvider());
                    mainFrame.setVisible(false);
                    UserEnvironment.getDefault().put(mainFrame.getClass().getName(), mainFrame);
                    UserEnvironment.getDefault().put(ExceptionDialog.PARENTFRAME, mainFrame);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Thread.currentThread().setContextClassLoader(tccl);
                }
            }
        });
    }

    /**
     * Show the main form. This is invoked on the java UI thread.
     */
    private void showUI() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                mainFrame.setLocationRelativeTo(null); // center on the screen
                mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
                mainFrame.setVisible(true);
                mainFrame.setWaitingCursor(true);
            }
        });
    }

    @Deactivate
    public void deactivate() {
        mainFrame.doClose();
    }

    @Deactivate
    public static void exitSystem(int exitCode) {
        mainFrame.doClose();
        Runtime.getRuntime().halt(exitCode);
    }

    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.UI;
    }

    @Override
    public List<TranslationKey> getKeys() {
        try {
            return SimpleTranslationKey.loadFromInputStream(this.getClass().getClassLoader().getResourceAsStream("i18n.properties"));
        } catch (IOException e) {
            LOGGER.severe("Failed to load translations for the '" + COMPONENTNAME + "' component bundle.");
        }
        return null;
    }

    @Override
    public NlsService nlsService() {
        return this.nlsService;
    }

    @Override
    public Thesaurus thesaurus() {
        return this.thesaurus;
    }

    @Override
    public IdentificationService identificationService() {
        return this.identificationService;
    }

    @Override
    public Optional<DeviceCache> findDeviceCacheByDevice(Device device) {
        return dataModel.mapper(DeviceCache.class).getUnique("device", device);
    }

    @Override
    public Optional<DeviceCache> findDeviceCacheByDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        Device device = deviceService
                .findDeviceByIdentifier(deviceIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Device with identifier " + deviceIdentifier.toString() + " does not exist"));

        return dataModel.mapper(DeviceCache.class).getUnique("device", device);
    }

    @Override
    public DeviceCache newDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCache deviceProtocolCache) {
        Device device = deviceService
                .findDeviceByIdentifier(deviceIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Device with identifier " + deviceIdentifier.toString() + " does not exist"));

        final DeviceCacheImpl deviceCache = dataModel.getInstance(DeviceCacheImpl.class).initialize(device, deviceProtocolCache);
        deviceCache.save();
        return deviceCache;
    }

    @Override
    public void register(DeactivationNotificationListener deactivationNotificationListener) {
        this.deactivationNotificationListeners.add(deactivationNotificationListener);
    }

    @Override
    public void unregister(DeactivationNotificationListener deactivationNotificationListener) {
        this.deactivationNotificationListeners.remove(deactivationNotificationListener);
    }

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile Clock clock;
    private volatile OrmService ormService;
    private volatile NlsService nlsService;
    private volatile MeteringService meteringService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile HexService hexService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile IssueService issueService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile PriorityComTaskService priorityComTaskService;
    private volatile LogBookService logBookService;
    private volatile DeviceMessageService deviceMessageService;
    private volatile DeviceService deviceService;
    private volatile RegisterService registerService;
    private volatile LoadProfileService loadProfileService;
    private volatile TopologyService topologyService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private volatile StatusService statusService;
    private volatile ManagementBeanFactory managementBeanFactory;
    private volatile UserService userService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile SocketService socketService;
    private volatile SerialComponentService serialATComponentService;
    private volatile FirmwareService firmwareService;
    private volatile UpgradeService upgradeService;
    private volatile AppService appService;
    private volatile EngineService engineService;
    private volatile SecurityManagementService securityManagementService;
    private volatile List<DeactivationNotificationListener> deactivationNotificationListeners = new CopyOnWriteArrayList<>();
    private OptionalIdentificationService identificationService = new OptionalIdentificationService();
    private volatile TimeOfUseCampaignService timeOfUseCampaignService;
    private volatile CustomPropertySetService customPropertySetService;

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Meter Data Collection Engine");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.ormService = ormService;
        this.dataModel = dataModel;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.AT + "))")
    public void setSerialATComponentService(SerialComponentService serialATComponentService) {
        this.serialATComponentService = serialATComponentService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void addIdentificationService(IdentificationService identificationService) {
        this.identificationService.set(identificationService);
    }

    @SuppressWarnings("unused")
    public void removeIdentificationService(IdentificationService identificationService) {
        this.identificationService.clear();
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setPriorityComTaskService(PriorityComTaskService priorityComTaskService) {
        this.priorityComTaskService = priorityComTaskService;
    }

    @Reference
    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setRegisterService(RegisterService registerService) {
        this.registerService = registerService;
    }

    @Reference
    public void setLoadProfileService(LoadProfileService loadProfileService) {
        this.loadProfileService = loadProfileService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setHexService(HexService hexService) {
        this.hexService = hexService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    @Reference
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    @Reference
    public void setManagementBeanFactory(ManagementBeanFactory managementBeanFactory) {
        this.managementBeanFactory = managementBeanFactory;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.UI);
    }

    @Reference
    public void setTimeOfUseCampaignService(TimeOfUseCampaignService timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    private class RunningComServerServiceProvider implements RunningComServerImpl.ServiceProvider {
        @Override
        public Thesaurus thesaurus() {
            return thesaurus;
        }

        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return deviceConfigurationService;
        }

        @Override
        public IssueService issueService() {
            return issueService;
        }

        @Override
        public ManagementBeanFactory managementBeanFactory() {
            return managementBeanFactory;
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return threadPrincipalService;
        }

        @Override
        public UserService userService() {
            return userService;
        }

        @Override
        public NlsService nlsService() {
            return nlsService;
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return protocolPluggableService;
        }

        public DeviceMessageSpecificationService deviceMessageSpecificationService() {
            return deviceMessageSpecificationService;
        }

        @Override
        public SocketService socketService() {
            return socketService;
        }

        @Override
        public HexService hexService() {
            return hexService;
        }

        @Override
        public SerialComponentService serialAtComponentService() {
            return serialATComponentService;
        }

        @Override
        public MeteringService meteringService() {
            return meteringService;
        }

        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return engineConfigurationService;
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return connectionTaskService;
        }

        @Override
        public CommunicationTaskService communicationTaskService() {
            return communicationTaskService;
        }

        @Override
        public PriorityComTaskService priorityComTaskService() {
            return priorityComTaskService;
        }

        @Override
        public OrmService ormService() {
            return ormService;
        }

        @Override
        public DeviceService deviceService() {
            return deviceService;
        }

        @Override
        public TopologyService topologyService() {
            return topologyService;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return mdcReadingTypeUtilService;
        }

        @Override
        public RegisterService registerService() {
            return registerService;
        }

        @Override
        public LoadProfileService loadProfileService() {
            return loadProfileService;
        }

        @Override
        public LogBookService logBookService() {
            return logBookService;
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return deviceMessageService;
        }

        @Override
        public EngineService engineService() {
            return engineService;
        }

        @Override
        public TransactionService transactionService() {
            return transactionService;
        }

        @Override
        public EventService eventService() {
            return eventService;
        }

        @Override
        public IdentificationService identificationService() {
            return identificationService;
        }

        @Override
        public FirmwareService firmwareService() {
            return firmwareService;
        }

        @Override
        public SecurityManagementService securityManagementService() {
            return securityManagementService;
        }

        @Override
        public CustomPropertySetService customPropertySetService() {
            return customPropertySetService;
        }

        @Override
        public TimeOfUseCampaignService touService() {
            return timeOfUseCampaignService;
        }
    }

    private static class OptionalIdentificationService implements IdentificationService {
        private AtomicReference<Optional<IdentificationService>> identificationService = new AtomicReference<>(Optional.empty());

        private void set(IdentificationService identificationService) {
            this.identificationService.set(Optional.of(identificationService));
        }

        private void clear() {
            this.identificationService.set(Optional.<IdentificationService>empty());
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByDatabaseId(long id) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByDatabaseId(id))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByMRID(String mRID) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByMRID(mRID))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByDeviceName(String deviceName) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByDeviceName(deviceName))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierBySerialNumber(String serialNumber) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierBySerialNumber(serialNumber))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByCallHomeId(String callHomeId) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByCallHomeId(callHomeId))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierForAlreadyKnownDevice(long deviceId, String deviceMrId) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierForAlreadyKnownDevice(deviceId, deviceMrId))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public RegisterIdentifier createRegisterIdentifierByAlreadyKnownRegister(com.energyict.mdc.upl.meterdata.Register register) {
            return this.identificationService
                    .get()
                    .map(s -> s.createRegisterIdentifierByAlreadyKnownRegister(register))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByProperty(String propertyName, String propertyValue) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByProperty(propertyName, propertyValue))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public DeviceIdentifier createDeviceIdentifierByConnectionTaskProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue) {
            return this.identificationService
                    .get()
                    .map(s -> s.createDeviceIdentifierByConnectionTaskProperty(connectionTypeClass, propertyName, propertyValue))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LoadProfileIdentifier createLoadProfileIdentifierByDatabaseId(long id, ObisCode obisCode, DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLoadProfileIdentifierByDatabaseId(id, obisCode, deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LoadProfileIdentifier createLoadProfileIdentifierForAlreadyKnownLoadProfile(com.energyict.mdc.upl.meterdata.LoadProfile loadProfile, ObisCode obisCode) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLoadProfileIdentifierForAlreadyKnownLoadProfile(loadProfile, obisCode))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LoadProfileIdentifier createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(loadProfileObisCode, deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LoadProfileIdentifier createLoadProfileIdentifierForFirstLoadProfileOnDevice(DeviceIdentifier deviceIdentifier, ObisCode obisCode) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLoadProfileIdentifierForFirstLoadProfileOnDevice(deviceIdentifier, obisCode))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LogBookIdentifier createLogbookIdentifierByDatabaseId(long id, ObisCode obisCode, DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLogbookIdentifierByDatabaseId(id, obisCode, deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LogBookIdentifier createLogbookIdentifierByObisCodeAndDeviceIdentifier(ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLogbookIdentifierByObisCodeAndDeviceIdentifier(logbookObisCode, deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public LogBookIdentifier createLogbookIdentifierForAlreadyKnownLogbook(LogBook logBook, DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createLogbookIdentifierForAlreadyKnownLogbook(logBook, deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

        @Override
        public MessageIdentifier createMessageIdentifierForAlreadyKnownMessage(long id, DeviceIdentifier deviceIdentifier) {
            return this.identificationService
                    .get()
                    .map(s -> s.createMessageIdentifierForAlreadyKnownMessage(id, deviceIdentifier))
                    .orElseThrow(IdentificationServiceMissingException::new);
        }

    }

    private static class IdentificationServiceMissingException extends RuntimeException {
        private IdentificationServiceMissingException() {
            super("IdentificationService missing in EngineService");
        }
    }

    public static class OfflineThesaurus implements Thesaurus {
        private Map<String, String> translations;

        private OfflineThesaurus(Collection<TranslationKey> translationKeys) {
            translations = translationKeys.stream()
                    .collect(Collectors.toMap(TranslationKey::getKey, TranslationKey::getDefaultFormat));
        }

        public static OfflineThesaurus from(Collection<TranslationKey> translationKeys) {
            return new OfflineThesaurus(translationKeys);
        }

        @Override
        public String getString(String key, String defaultMessage) {
            return Optional.ofNullable(translations.get(key)).orElse(defaultMessage);
        }

        @Override
        public String getString(Locale locale, String key, String defaultMessage) {
            return Optional.ofNullable(translations.get(key)).orElse(defaultMessage);
        }

        @Override
        public NlsMessageFormat getFormat(MessageSeed seed) {
            return new OfflineNlsMessageFormat(seed);
        }

        @Override
        public NlsMessageFormat getFormat(TranslationKey key) {
            return new OfflineNlsMessageFormat(key);
        }

        @Override
        public NlsMessageFormat getSimpleFormat(MessageSeed seed) {
            return new OfflineNlsMessageFormat(seed);
        }

        @Override
        public Map<String, String> getTranslationsForCurrentLocale() {
            return translations;
        }

        @Override
        public boolean hasKey(String key) {
            return translations.containsKey(key);
        }

        @Override
        public Thesaurus join(Thesaurus thesaurus) {
            return this;
        }

        @Override
        public DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter) {
            return DateTimeFormatter.ISO_DATE_TIME;
        }

        @Override
        public String interpolate(String messageTemplate, Context context) {
            return messageTemplate;
        }

        @Override
        public String interpolate(String messageTemplate, Context context, Locale locale) {
            return messageTemplate;
        }
    }

    private static class OfflineNlsMessageFormat implements NlsMessageFormat {
        private final String defaultFormat;

        OfflineNlsMessageFormat(MessageSeed seed) {
            this.defaultFormat = seed.getDefaultFormat();
        }

        OfflineNlsMessageFormat(TranslationKey key) {
            this.defaultFormat = key.getDefaultFormat();
        }

        @Override
        public String format(Object... args) {
            return MessageFormat.format(defaultFormat, args);
        }

        @Override
        public String format(Locale locale, Object... args) {
            return MessageFormat.format(defaultFormat, args);
        }
    }

}
