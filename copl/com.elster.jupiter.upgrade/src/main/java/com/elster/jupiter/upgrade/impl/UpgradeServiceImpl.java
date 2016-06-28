package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableMap;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.upgrade", immediate = true, service = UpgradeService.class,
        property = {"osgi.command.scope=upgrade", "osgi.command.function=init"})
public final class UpgradeServiceImpl implements UpgradeService {

    private volatile BootstrapService bootstrapService;
    private volatile TransactionService transactionService;
    private volatile DataModelUpgrader dataModelUpgrader;
    private volatile OrmService ormService;
    private boolean doUpgrade;
    private Map<InstallIdentifier, UpgradeClasses> registered = new HashMap<>();
    private final Logger logger = Logger.getLogger("com.elster.jupiter.upgrade");

    public UpgradeServiceImpl() {
    }

    @Inject
    public UpgradeServiceImpl(BootstrapService bootstrapService, TransactionService transactionService, OrmService ormService, BundleContext bundleContext) {
        this();
        setBootstrapService(bootstrapService);
        setOrmService(ormService);
        setTransactionService(transactionService);

        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        String upgradeProperty = bundleContext.getProperty("upgrade");
        doUpgrade = upgradeProperty != null && Boolean.parseBoolean(upgradeProperty);
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
            if (doUpgrade) {
                flyway.migrate();
            } else {
                MigrationInfoService migrationInfoService = flyway.info();
                if (migrationInfoService.pending().length != 0) {
                    throw new RuntimeException("Upgrade needed for " + installIdentifier);
                }
            }
        } catch (RuntimeException e) { // TODO (maybe separate exc handling for both modes?)
            e.printStackTrace();
            throw e;
        }
    }

    private Flyway createFlyway(InstallIdentifier installIdentifier) {
        Flyway flyway = new Flyway();
        DataSource dataSource = bootstrapService.createDataSource();
        flyway.setDataSource(dataSource);
        flyway.setTable("FLYWAYMETA." + installIdentifier);
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
        DataModel dataModel = ormService.getDataModel(dataModelCode).orElseThrow(() -> new IllegalArgumentException("No such data model registered."));
        DataModelUpgrader dataModelUpgrader = ormService.getDataModelUpgrader(logger);
        dataModelUpgrader.upgrade(dataModel, Version.latest());
    }

    public void init(String dataModelCode, String version) {
        DataModel dataModel = ormService.getDataModel(dataModelCode).orElseThrow(() -> new IllegalArgumentException("No such data model registered."));
        DataModelUpgrader dataModelUpgrader = ormService.getDataModelUpgrader(logger);
        dataModelUpgrader.upgrade(dataModel, Version.version(version));
    }
}
