package com.energyict.mdc.issue.datacollection.impl;

import com.elster.jupiter.issue.share.cep.ActionLoadFailedException;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskNowAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryConnectionTaskAction;
import com.google.inject.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.MessageInterpolator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.issue.datacollection.actions.factory", service = IssueActionFactory.class, immediate = true)
public class DataCollectionActionsFactory implements IssueActionFactory {
    private static final Logger LOG = Logger.getLogger(DataCollectionActionsFactory.class.getName());
    public static final String ID = DataCollectionActionsFactory.class.getName();

    private volatile Thesaurus thesaurus;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile IssueService issueService;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    public DataCollectionActionsFactory() {
    }

    @Inject
    public DataCollectionActionsFactory(NlsService nlsService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, IssueService issueService) {
        setThesaurus(nlsService);
        setConnectionTaskService(connectionTaskService);
        setCommunicationTaskService(communicationTaskService);

        activate();
    }

    @Activate
    public void activate() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(IssueService.class).toInstance(issueService);
            }
        });

        addDefaultActions();
    }

    public IssueAction createIssueAction(String issueActionClassName) {
        Provider<? extends IssueAction> provider = actionProviders.get(issueActionClassName);
        if (provider == null) {
            throw new ActionLoadFailedException(thesaurus);
        }
        return provider.get();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Reference
    public final void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public final void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    private void addDefaultActions() {
        try {
            actionProviders.put(RetryConnectionTaskAction.class.getName(), injector.getProvider(RetryConnectionTaskAction.class));
            actionProviders.put(RetryCommunicationTaskAction.class.getName(), injector.getProvider(RetryCommunicationTaskAction.class));
            actionProviders.put(RetryCommunicationTaskNowAction.class.getName(), injector.getProvider(RetryCommunicationTaskNowAction.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}
