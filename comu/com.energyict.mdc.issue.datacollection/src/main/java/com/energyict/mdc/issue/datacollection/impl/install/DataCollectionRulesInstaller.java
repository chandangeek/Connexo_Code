package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.elster.jupiter.orm.Version.version;

@Component(
        name = "com.energyict.mdc.issue.datacollection.impl.install.IdenpendentInstaller",
        service = {TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + DataCollectionRulesInstaller.COMPONENT_NAME},
        immediate = true
)
public class DataCollectionRulesInstaller implements TranslationKeyProvider, MessageSeedProvider, FullInstaller {

    public static final String COMPONENT_NAME = "ID1";

    private volatile OrmService ormService;
    private volatile IssueService issueService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile UpgradeService upgradeService;
    private volatile TimeService timeService;

    // This will be BasicDataCollectionRuleTemplate
    private volatile CreationRuleTemplate creationRuleTemplate;

    private DataCollectionRulesUpgrader dataCollectionRulesUpgrader;

    public DataCollectionRulesInstaller() {
    }

    @Inject
    public DataCollectionRulesInstaller(DataCollectionRulesUpgrader idependentDataCollectionRulesUpgrader) {
        this.dataCollectionRulesUpgrader = idependentDataCollectionRulesUpgrader;
    }

    @Override
    public void install(final DataModelUpgrader dataModelUpgrader, final Logger logger) {
        doTry("Upgrade data collection creation rules from 10.6 -> 10.7", () -> dataCollectionRulesUpgrader.migrate(dataModelUpgrader), logger);
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(OrmService.class).toInstance(ormService);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueDataCollectionService.class).toInstance(issueDataCollectionService);
                bind(TimeService.class).toInstance(timeService);
            }
        });

        upgradeService.register(
                InstallIdentifier.identifier("MultiSense", COMPONENT_NAME),
                dataModel,
                DataCollectionRulesInstaller.class,
                ImmutableMap.of(
                        version(10, 7), DataCollectionRulesUpgrader.class
                )
        );
    }

    @Reference
    public void setOrmService(final OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setIssueService(final IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueDataCollectionService(final IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    //    @Reference(target = "(component.name=com.energyict.mdc.issue.datacollection.BasicDatacollectionRuleTemplate)")
    @Reference(target = "(name=BasicDataCollectionRuleTemplate)")
    public void setCreationRuleTemplate(final CreationRuleTemplate creationRuleTemplate) {
        this.creationRuleTemplate = creationRuleTemplate;
    }

    @Reference
    public void setUpgradeService(final UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTimeService(final TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}
