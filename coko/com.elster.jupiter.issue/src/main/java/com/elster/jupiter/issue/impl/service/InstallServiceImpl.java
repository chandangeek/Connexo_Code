package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.issue.impl.module.Installer;
import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.issue.install", service = InstallService.class, property = "name=" + IssueService.COMPONENT_NAME)
public class InstallServiceImpl implements InstallService {
    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile IssueMainService issueMainService;

    // TODO delete when events will be defined by MDC
    private volatile IssueHelpService issueHelpService;

    public InstallServiceImpl(){}

    @Inject
    // TODO remove parameter when events will be defined by MDC
    public InstallServiceImpl(IssueMappingService issueMappingService, IssueMainService issueMainService,
                              IssueHelpService issueHelpService, MessageService messageService) {
        setIssueMappingService(issueMappingService);
        setIssueMainService(issueMainService);
        setMessageService(messageService);

        setIssueHelpService(issueHelpService);

        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public void install() {
        new Installer(dataModel, issueMainService, messageService).install(true);
        // TODO delete when events will be defined by MDC
        issueHelpService.setEventTopics();
    }

    @Reference
    public void setIssueMappingService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }
    @Reference
    public void setIssueMainService(IssueMainService issueMainService) {
        this.issueMainService = issueMainService;
    }
    // TODO delete when events will be defined by MDC
    @Reference
    public void setIssueHelpService(IssueHelpService issueHelpService) {
        this.issueHelpService = issueHelpService;
    }
    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
}
