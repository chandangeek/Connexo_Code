package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.impl.actions.webelements.factories.AlarmProcessComboboxFactory;
import com.elster.jupiter.issue.impl.actions.webelements.factories.AssigneeElementGroupFactory;
import com.elster.jupiter.issue.impl.actions.webelements.factories.CloseIssueFormFactory;
import com.elster.jupiter.issue.impl.actions.webelements.factories.EndPointDropdownFactory;
import com.elster.jupiter.issue.impl.actions.webelements.factories.ProcessComboxFactory;
import com.elster.jupiter.issue.share.PropertyFactoriesProvider;
import com.elster.jupiter.issue.share.PropertyFactory;
import com.elster.jupiter.issue.share.entity.WebElementFactoryNotFound;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.MessageInterpolator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.issue.webelements.factory", service = PropertyFactoriesProvider.class, immediate = true)
public class PropertyFactoriesProviderImpl implements PropertyFactoriesProvider {

    private static final Logger LOG = Logger.getLogger(PropertyFactoriesProviderImpl.class.getName());
    private static final String ID = PropertyFactoriesProviderImpl.class.getName();

    private final Map<PropertyType, Provider<? extends PropertyFactory>> propertyFactoriesProvider = new ConcurrentHashMap<>();

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile BpmService bpmService;

    private Injector injector;

    /**
     * OSGi framework needs a default constructor in order to create this component
     */
    @SuppressWarnings("unused")
    public PropertyFactoriesProviderImpl() {
    }

    /**
     * We need this constructor for testing purposes
     */
    @Inject
    public PropertyFactoriesProviderImpl(final NlsService nlsService, final UserService userService, final IssueService issueService, final ThreadPrincipalService threadPrincipalService, final OrmService ormService, final PropertySpecService propertySpecService, final EndPointConfigurationService endPointConfigurationService, final BpmService bpmService) {
        setThesaurus(nlsService);
        setUserService(userService);
        setIssueService(issueService);
        setThreadPrincipalService(threadPrincipalService);
        setOrmService(ormService);
        setPropertySpecService(propertySpecService);
        setEndPointConfigurationService(endPointConfigurationService);
        setBpmService(bpmService);
        activate();
    }

    @Activate
    public void activate() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(NlsService.class).toInstance(nlsService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(IssueService.class).toInstance(issueService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(BpmService.class).toInstance(bpmService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
            }
        });
        addDefaultWebElementFactories();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public PropertyFactory getFactory(PropertyType type) {

        final Provider<? extends PropertyFactory> provider = propertyFactoriesProvider.get(type);

        if (provider == null) {
            throw new WebElementFactoryNotFound(thesaurus, type.toString());
        }

        return provider.get();
    }

    @Reference
    public final void setThesaurus(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setBpmService(final BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.getDataModel(IssueService.COMPONENT_NAME).orElse(null);
    }

    private void addDefaultWebElementFactories() {
        try {
            propertyFactoriesProvider.put(PropertyType.ENDPOINT_COMBOBOX, injector.getProvider(EndPointDropdownFactory.class));
            propertyFactoriesProvider.put(PropertyType.PROCESS_COMBOBOX, injector.getProvider(ProcessComboxFactory.class));
            propertyFactoriesProvider.put(PropertyType.ALARM_PROCESS_COMBOBOX, injector.getProvider(AlarmProcessComboboxFactory.class));
            propertyFactoriesProvider.put(PropertyType.ASSIGN_ISSUE_FORM, injector.getProvider(AssigneeElementGroupFactory.class));
            propertyFactoriesProvider.put(PropertyType.CLOSE_ISSUE_FORM, injector.getProvider(CloseIssueFormFactory.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}
