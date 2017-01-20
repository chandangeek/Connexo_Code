package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.StartupFinishedListener;
import com.elster.jupiter.upgrade.UpgradeCheckList;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.concurrent.CopyOnWriteServiceContainer;
import com.elster.jupiter.util.concurrent.OptionalServiceContainer;

import com.google.common.collect.ImmutableMap;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.test;
import static com.elster.jupiter.util.streams.Predicates.not;

@Component(name = "com.elster.jupiter.upgrade", immediate = true, service = {UpgradeService.class, EventHandler.class},
        property = {"osgi.command.scope=upgrade", "osgi.command.function=init", "osgi.command.function=runDifferences", EventConstants.EVENT_TOPIC + "=org/osgi/framework/FrameworkEvent/STARTED"})
public final class UpgradeServiceImpl implements UpgradeService, EventHandler {

    private volatile BootstrapService bootstrapService;
    private volatile TransactionService transactionService;
    private volatile DataModelUpgrader dataModelUpgrader;
    private volatile OrmService ormService;
    private volatile FileSystem fileSystem;
    private State state;
    private Map<InstallIdentifier, UpgradeClasses> registered = new HashMap<>();
    private final Logger logger = Logger.getLogger("upgrade");
    private UserInterface userInterface = new ConsoleUserInterface();
    private OptionalServiceContainer<UpgradeCheckList> checkLists = new CopyOnWriteServiceContainer<>();
    private Set<InstallIdentifier> checked = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Queue<StartupFinishedListener> startupFinishedListeners = new ConcurrentLinkedQueue<>();
    private volatile boolean complete;
    private boolean differences;

    public UpgradeServiceImpl() {
        Logger flywayLogger = Logger.getLogger("org.flywaydb");
        flywayLogger.setUseParentHandlers(false);
        Arrays.stream(flywayLogger.getHandlers())
                .forEach(flywayLogger::removeHandler);
        flywayLogger.addHandler(new ForwardingHandler(logger));
        logger.setUseParentHandlers(false);
    }

    @Inject
    public UpgradeServiceImpl(BootstrapService bootstrapService, TransactionService transactionService, OrmService ormService, BundleContext bundleContext, FileSystem fileSystem) {
        setBootstrapService(bootstrapService);
        setOrmService(ormService);
        setTransactionService(transactionService);
        setFileSystem(fileSystem);

        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        String upgradeProperty = bundleContext.getProperty("upgrade");
        boolean doUpgrade = upgradeProperty != null && Boolean.parseBoolean(upgradeProperty);
        String differencesProperty = bundleContext.getProperty("differences");
        this.differences = differencesProperty != null && Boolean.parseBoolean(differencesProperty);
        state = doUpgrade ? new UpgraderState() : new CheckState();
        state.addFilter();
        startupFinishedListeners.add(new ReportingFinishedListener());
        if (differences) {
            startupFinishedListeners.add(new StartDiffCheckListener());
        }
    }

    @Deactivate
    public void deactivate() {
        state.removeFilter();
        startupFinishedListeners.clear();
    }

    @Override
    public void register(InstallIdentifier installIdentifier, DataModel dataModel, Class<? extends FullInstaller> installerClass, Map<Version, Class<? extends Upgrader>> upgraders) {
        UpgradeClasses newUpgradeClasses = new UpgradeClasses(installerClass, upgraders);
        if (registered.containsKey(installIdentifier)) {
            UpgradeClasses currentlyRegistered = registered.get(installIdentifier);
            if (!currentlyRegistered.precedes(newUpgradeClasses)) {
                throw new IllegalStateException("Duplicate registration for key " + installIdentifier + " conflicting installers : " + currentlyRegistered + " and " + newUpgradeClasses);
            }
        }
        registered.put(installIdentifier, newUpgradeClasses);

        Flyway flyway = createFlyway(installIdentifier);

        flyway.setResolvers(new MigrationResolverImpl(dataModel, dataModelUpgrader, transactionService, installerClass, upgraders, logger));

        try {
            verifyExpected(installIdentifier);
            state.perform(flyway, installIdentifier);
            verifyComplete();
        } catch (RuntimeException e) {
            String message = "Upgrade of " + installIdentifier + " failed with an exception.";
            logger.log(Level.SEVERE, message, e);
            userInterface.notifyUser(message, e);
            System.exit(5);
        }
    }

    @Override
    public void handleEvent(Event event) {
        if (!complete) {
            String uninstalled = checkLists.getServices()
                    .stream()
                    .map(UpgradeCheckList::componentsToInstall)
                    .flatMap(Collection::stream)
                    .filter(not(checked::contains))
                    .sorted(Comparator.comparing(InstallIdentifier::application).thenComparing(InstallIdentifier::name))
                    .map(InstallIdentifier::toString)
                    .collect(Collectors.joining("\n"));
            userInterface.notifyUser("Container startup completed with missing components " + uninstalled);
        }
    }

    private Flyway createFlyway(InstallIdentifier installIdentifier) {
        Flyway flyway = new Flyway();
        flyway.setSkipDefaultResolvers(true);
        DataSource dataSource = bootstrapService.createDataSource();
        flyway.setDataSource(dataSource);
        flyway.setTable("FLYWAYMETA." + installIdentifier.name());
        flyway.setBaselineVersionAsString("0.0");
        flyway.setBaselineOnMigrate(true);
        return flyway;
    }

    @Override
    public boolean isInstalled(InstallIdentifier installIdentifier, Version version) {
        Flyway flyway = createFlyway(installIdentifier);
        MigrationInfoService info = flyway.info();
        return Arrays.stream(info.applied())
                .map(MigrationInfo::getVersion)
                .anyMatch(migrationVersion -> MigrationVersion.fromVersion(version.toString())
                        .equals(migrationVersion));
    }

    @Reference
    public void setBootstrapService(BootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModelUpgrader = ormService.getDataModelUpgrader(logger);
        this.ormService = ormService;
    }

    @Reference
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
    public void addCheckList(UpgradeCheckList upgradeCheckList) {
        checkLists.register(upgradeCheckList);
    }

    public void removeCheckList(UpgradeCheckList upgradeCheckList) {
        checkLists.unregister(upgradeCheckList);
    }

    private void verifyExpected(InstallIdentifier installIdentifier) {
        checked.add(installIdentifier);
        checkLists.poll(test(this::matchApplication).with(installIdentifier))
                .map(upgradeCheckList -> (Runnable) () -> {
                        if (!expected(upgradeCheckList, installIdentifier)) {
                            //TODO end startup, but first CXO-2089 needs to be addressed, for now just log a warning
                            this.unexpectedComponentInstalled(installIdentifier);
                        }
                    })
                .orElse(() -> this.unexpectedComponentInstalled(installIdentifier))
                .run();
    }

    private void unexpectedComponentInstalled(InstallIdentifier installIdentifier) {
        logger.severe("Unexpected component installed : " + installIdentifier);
        if (this.differences && this.complete) {
            startupFinishedListeners.add(new StartDiffCheckListener());
        }
    }

    @Override
    public void addStartupFinishedListener(StartupFinishedListener startupFinishedListener) {
        if (complete) {
            startupFinishedListener.onStartupComplete();
            return;
        }
        startupFinishedListeners.add(startupFinishedListener);
    }

    private void verifyComplete() {
        complete = checkLists
                        .getServices()
                        .stream()
                        .map(UpgradeCheckList::componentsToInstall)
                        .flatMap(Collection::stream)
                        .allMatch(checked::contains);
        if (complete) {
            startupFinishedListeners.forEach(StartupFinishedListener::onStartupComplete);
            startupFinishedListeners.clear();
        }
        String remaining = checkLists.getServices()
                .stream()
                .map(UpgradeCheckList::componentsToInstall)
                .flatMap(Collection::stream)
                .filter(not(checked::contains))
                .map(InstallIdentifier::name)
                .collect(Collectors.joining("\t"));
        System.out.println("Remaining : " + remaining);
    }

    private boolean matchApplication(UpgradeCheckList upgradeCheckList, InstallIdentifier installIdentifier) {
        return upgradeCheckList.application().equals(installIdentifier.application());
    }

    private boolean expected(UpgradeCheckList upgradeCheckList, InstallIdentifier installIdentifier) {
        return upgradeCheckList.componentsToInstall().contains(installIdentifier);
    }

    private static final class UpgradeClasses {
        private Class<? extends FullInstaller> fullInstallerClass;
        private Map<Version, Class<? extends Upgrader>> upgraderClasses;

        public UpgradeClasses(Class<? extends FullInstaller> fullInstallerClass, Map<Version, Class<? extends Upgrader>> upgraderClasses) {
            this.fullInstallerClass = fullInstallerClass;
            this.upgraderClasses = ImmutableMap.copyOf(upgraderClasses);
        }

        boolean precedes(UpgradeClasses possibleSuccessor) {
            return equal(fullInstallerClass, possibleSuccessor.fullInstallerClass)
                    && upgraderClasses.size() <= possibleSuccessor.upgraderClasses.size()
                    && possibleSuccessor.upgraderClasses.entrySet().stream()
                    .allMatch(entry -> {
                        Class<? extends Upgrader> other = upgraderClasses.get(entry.getKey());
                        return other != null && equal(entry.getValue(), other);
                    });
        }

        private boolean equal(Class<?> clazz1, Class<?> clazz2) {
            return clazz1.getName().equals(clazz2.getName());
        }

        @Override
        public String toString() {
            return "UpgradeClasses{" +
                    "fullInstallerClass=" + fullInstallerClass +
                    ", upgraderClasses=" + upgraderClasses +
                    '}';
        }
    }

    @Override
    public DataModel newNonOrmDataModel() {
        return new InjectOnly();
    }

    public void init(String dataModelCode) {
        DataModel dataModel = ormService.getDataModel(dataModelCode)
                .orElseThrow(() -> new IllegalArgumentException("No such data model registered."));
        DataModelUpgrader dataModelUpgrader = ormService.getDataModelUpgrader(logger);
        dataModelUpgrader.upgrade(dataModel, Version.latest());
    }

    public void init(String dataModelCode, String version) {
        DataModel dataModel = ormService.getDataModel(dataModelCode)
                .orElseThrow(() -> new IllegalArgumentException("No such data model registered."));
        DataModelUpgrader dataModelUpgrader = ormService.getDataModelUpgrader(logger);
        dataModelUpgrader.upgrade(dataModel, Version.version(version));
    }

    private interface State {
        void perform(Flyway flyway, InstallIdentifier installIdentifier);

        void addFilter();

        void removeFilter();
    }

    private class UpgraderState implements State {
        @Override
        public void perform(Flyway flyway, InstallIdentifier installIdentifier) {
            String message = "Upgrading " + installIdentifier.name();
            userInterface.notifyUser(message);
            flyway.migrate();
        }

        @Override
        public void addFilter() {
        }

        @Override
        public void removeFilter() {
        }
    }

    private class CheckState implements State {
        private final Filter filter = record -> false;

        @Override
        public void perform(Flyway flyway, InstallIdentifier installIdentifier) {
            MigrationInfoService migrationInfoService = flyway.info();
            if (migrationInfoService.pending().length != 0) {
                String message = "Upgrade needed for " + installIdentifier;
                logger.log(Level.SEVERE, message);
                userInterface.notifyUser(message);
                System.exit(4);
            }
        }

        @Override
        public void addFilter() {
            logger.setFilter(filter);
        }

        @Override
        public void removeFilter() {
            logger.setFilter(null);
        }
    }

    private interface UserInterface {
        void notifyUser(String message);

        void notifyUser(String message, Exception e);
    }

    private static class ConsoleUserInterface implements UserInterface {
        @Override
        public void notifyUser(String message) {
            System.out.println(message);
        }

        @Override
        public void notifyUser(String message, Exception e) {
            e.printStackTrace();
            System.out.println(message);
        }
    }

    private static class ForwardingHandler extends Handler {

        private final Logger forwardingTarget;

        private ForwardingHandler(Logger forwardingTarget) {
            this.forwardingTarget = forwardingTarget;
        }

        @Override
        public void publish(LogRecord record) {
            forwardingTarget.log(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    private class ReportingFinishedListener implements StartupFinishedListener {
        @Override
        public void onStartupComplete() {
            userInterface.notifyUser("All components have been installed.");
        }
    }

    private class StartDiffCheckListener implements StartupFinishedListener {
        @Override
        public void onStartupComplete() {
            ormService.getDataModelDifferences(logger).findDifferences();
        }
    }

}
