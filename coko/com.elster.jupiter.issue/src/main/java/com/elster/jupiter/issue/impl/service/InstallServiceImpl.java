package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.issue.impl.module.Installer;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.issue.install", service = InstallService.class, property = "name=" + IssueService.COMPONENT_NAME, immediate = true)
public class InstallServiceImpl implements InstallService {
    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile IssueAssignmentService issueAssignmentService;

    // TODO delete when events will be defined by MDC
    private volatile IssueHelpService issueHelpService;

    public InstallServiceImpl(){}

    @Inject
    // TODO remove parameter when events will be defined by MDC
    public InstallServiceImpl(
            MessageService messageService,
            MeteringService meteringService,
            UserService userService,
            IssueService issueService,
            IssueAssignmentService issueAssignmentService,
            IssueMappingService issueMappingService,
            IssueHelpService issueHelpService) {

        setMessageService(messageService);
        setMeteringService(meteringService);
        setUserService(userService);
        setIssueService(issueService);
        setIssueAssignmentService(issueAssignmentService);
        setIssueHelpService(issueHelpService);
        setIssueMappingService(issueMappingService);

        activate();
    }

    @Activate
    public void activate(){
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(UserService.class).toInstance(userService);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueAssignmentService.class).toInstance(issueAssignmentService);
            }
        });
        if (!dataModel.isInstalled()) {
            install();
        }
    }
    @Override
    public void install() {
        new Installer(dataModel, issueService, messageService).install(true);
        // TODO delete when events will be defined by MDC
        issueHelpService.setEventTopics();
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    @Reference
    public void setIssueAssignmentService(IssueAssignmentService issueAssignmentService) {
        this.issueAssignmentService = issueAssignmentService;
    }
    @Reference
    public void setIssueHelpService(IssueHelpService issueHelpService) {
        this.issueHelpService = issueHelpService;
    }
    @Reference
    public void setIssueMappingService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }
}
