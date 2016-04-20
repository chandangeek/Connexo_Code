package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfoService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;
import java.util.List;

@Component(name = "com.elster.jupiter.upgrade", immediate = true, service = UpgradeService.class)
public class UpgradeServiceImpl implements UpgradeService {

    private volatile BootstrapService bootstrapService;
    private volatile TransactionService transactionService;
    private volatile DataModelUpgrader dataModelUpgrader;
    private boolean doUpgrade;

    @Activate
    public void activate(BundleContext bundleContext) {
        String upgradeProperty = bundleContext.getProperty("upgrade");
        doUpgrade = upgradeProperty != null && Boolean.parseBoolean(upgradeProperty);
    }

    @Override
    public void register(InstallIdentifier installIdentifier, DataModel dataModel, Class<? extends FullInstaller> installerClass, List<Class<? extends Upgrader>> upgraders) {
        Flyway flyway = new Flyway();
        DataSource dataSource = bootstrapService.createDataSource();
        flyway.setDataSource(dataSource);

//        flyway.setLocations("com.elster.jupiter.flyway1.impl.upgrade");
        flyway.setTable("FLYWAYMETA." + installIdentifier);
        flyway.setBaselineVersionAsString("0.0");
        flyway.setBaselineOnMigrate(true);

        flyway.setResolvers(new MigrationResolverImpl(dataModel, dataModelUpgrader, transactionService, installerClass, upgraders));

        try {
            if (doUpgrade) {
                flyway.migrate();
            } else {
                MigrationInfoService migrationInfoService = flyway.info();
                if (migrationInfoService.pending().length != 0) {
                    throw new RuntimeException("Upgrade needed for " + installIdentifier);
                }
            }
        } catch (RuntimeException e) { // TODO (maybe seperate exc handling for both modes?)
            e.printStackTrace();
            throw e;
        }
    }

    @Reference
    public void setBootstrapService(BootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setOrmService(OrmService ormService) {
        this.dataModelUpgrader = ormService.getDataModelUpgrader();
    }
}
